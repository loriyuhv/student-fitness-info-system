package com.wsw.fitnesssystem.fitness.application.service.impl;

import com.wsw.fitnesssystem.fitness.application.dto.FitnessScoreResult;
import com.wsw.fitnesssystem.fitness.application.dto.command.FitnessRecordSubmitCommand;
import com.wsw.fitnesssystem.fitness.application.dto.result.FitnessRecordSubmitResult;
import com.wsw.fitnesssystem.fitness.application.service.FitnessRecordSubmitService;
import com.wsw.fitnesssystem.fitness.application.service.FitnessScoreQueryService;
import com.wsw.fitnesssystem.fitness.domain.model.StudentFitnessRecord;
import com.wsw.fitnesssystem.fitness.domain.model.StudentFitnessRecordItem;
import com.wsw.fitnesssystem.fitness.domain.model.StudentFitnessSummary;
import com.wsw.fitnesssystem.fitness.domain.port.FitnessItemRepository;
import com.wsw.fitnesssystem.fitness.domain.port.FitnessRecordRepository;
import com.wsw.fitnesssystem.fitness.domain.port.FitnessSummaryRepository;
import com.wsw.fitnesssystem.shared.exception.BizException;
import com.wsw.fitnesssystem.shared.response.ResultCode;
import com.wsw.fitnesssystem.user.domain.model.StudentProfile;
import com.wsw.fitnesssystem.user.domain.port.StudentProfileRepository;
import com.wsw.fitnesssystem.user.domain.valueobject.Gender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 体测记录提交服务实现
 *
 * <p><b>完整流程（一次教师请求 → 落库）：</b></p>
 * <ol>
 *   <li>参数校验（学号 / 操作人 / 原始成绩非空、必测项目齐全）</li>
 *   <li>学号 → 学生档案（userId / gender / enrollYear），校验性别可用于评分</li>
 *   <li>入学年份 → 当前年级（用于选择规则集：大一大二 / 大三大四）</li>
 *   <li>评分引擎计算：单项得分 + 加分 + 总分 + 等级</li>
 *   <li>组装体测记录聚合（主表字段 + 明细），事务内落库（主表 + 明细）</li>
 *   <li>按 userId upsert 体测汇总（覆盖为最近一次体测）</li>
 *   <li>返回提交结果</li>
 * </ol>
 *
 * <p><b>事务边界：</b>record + items + summary 三者同事务，
 * 任一步失败整体回滚，保证不会出现"记录在、汇总不在"的半截数据。</p>
 *
 * <p>说明：本模块直接复用 user 模块的 {@link StudentProfileRepository}（模块化单体阶段），
 * 后续拆服务时可改为 health 侧自建 Port + Adapter。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/5 09:05
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FitnessRecordSubmitServiceImpl implements FitnessRecordSubmitService {

    /** 一次完整体测应包含的项目编码 */
    private static final Set<String> REQUIRED_ITEM_CODES = Set.of(
        "BMI", "VITAL_CAPACITY", "50M", "SIT_AND_REACH",
        "STANDING_LONG_JUMP", "PULL_UP", "RUN_1000_800"
    );

    /** 体测时间格式：yyyy-MM-dd HH:mm:ss */
    private static final DateTimeFormatter TEST_TIME_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final StudentProfileRepository studentProfileRepository;
    private final FitnessItemRepository fitnessItemRepository;
    private final FitnessScoreQueryService scoreQueryService;
    private final FitnessRecordRepository recordRepository;
    private final FitnessSummaryRepository summaryRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FitnessRecordSubmitResult submit(FitnessRecordSubmitCommand command) {
        // ========== 1. 参数校验 ==========
        if (command == null) {
            throw new BizException(ResultCode.PARAM_INVALID, "提交数据不能为空");
        }
        String studentNo = command.getStudentNo();
        if (StringUtils.isBlank(studentNo)) {
            throw new BizException(ResultCode.PARAM_INVALID, "学号不能为空");
        }
        if (command.getOperatorUserId() == null) {
            throw new BizException(ResultCode.PARAM_INVALID, "缺少操作人信息");
        }
        Map<String, Double> rawScores = command.getRawScores();
        if (rawScores == null || rawScores.isEmpty()) {
            throw new BizException(ResultCode.PARAM_INVALID, "体测原始成绩不能为空");
        }
        // 必测项目完整性校验：缺项会拉低总分，必须显式报错而不是静默按 0 处理
        Set<String> missingItems = REQUIRED_ITEM_CODES.stream()
            .filter(code -> rawScores.get(code) == null)
            .collect(Collectors.toSet());
        if (!missingItems.isEmpty()) {
            throw new BizException(ResultCode.PARAM_INVALID,
                "体测成绩缺少必测项目: " + String.join(", ", missingItems));
        }

        // ========== 2. 学号 → 学生档案 ==========
        StudentProfile profile = studentProfileRepository.findByStudentNo(studentNo.trim())
            .orElseThrow(() -> new BizException(ResultCode.USER_NOT_FOUND,
                "未找到学号为 " + studentNo + " 的学生档案"));
        Integer gender = resolveGenderCode(profile);
        Integer grade = resolveGrade(profile.getEnrollYear());

        // ========== 3. 评分引擎计算 ==========
        FitnessScoreResult scoreResult = scoreQueryService.calculate(gender, grade, rawScores);
        log.info("体测评分完成: studentNo={}, gender={}, grade={}, totalScore={}, level={}",
            studentNo, gender, grade, scoreResult.getTotalScore(), scoreResult.getLevelName());

        // ========== 4. 组装体测记录聚合 ==========
        LocalDateTime testTime = parseTestTime(command.getTestTime());
        StudentFitnessRecord record = StudentFitnessRecord.builder()
            .studentUserId(profile.getUserId())
            .operatorUserId(command.getOperatorUserId())
            .testTime(testTime)
            .testRound(command.getTestRound() != null ? command.getTestRound() : 1)
            .testType(1) // 1-正式
            .totalScore(scoreResult.getTotalScore())
            .totalLevel(scoreResult.getLevelName() != null
                ? scoreResult.getLevelName() : scoreResult.getLevelCode())
            .status(1)          // 1-正常
            .confirmStatus(0)   // 0-未确认
            .items(buildRecordItems(rawScores, scoreResult))
            .build();

        // ========== 5. 落库：主表 + 明细（同一事务） ==========
        StudentFitnessRecord saved = recordRepository.save(record);

        // ========== 6. 汇总 upsert（覆盖为最近一次体测） ==========
        StudentFitnessSummary summary = StudentFitnessSummary.fromRecord(profile.getUserId(), saved);
        summaryRepository.upsertByUserId(summary);

        // ========== 7. 返回结果 ==========
        return FitnessRecordSubmitResult.builder()
            .recordId(saved.getRecordId())
            .itemScores(scoreResult.getItemScores())
            .totalScore(scoreResult.getTotalScore())
            .totalLevel(scoreResult.getLevelName() != null
                ? scoreResult.getLevelName() : scoreResult.getLevelCode())
            .totalBonus(scoreResult.getTotalBonus())
            .createTime(testTime)
            .build();
    }

    // ==================== 辅助方法 ====================

    /**
     * 解析性别。学生档案未设置性别（未知）时无法选择评分规则，直接报错。
     */
    private Integer resolveGenderCode(StudentProfile profile) {
        Gender gender = profile.getGender();
        if (gender == null || gender.getCode() == Gender.UNKNOWN.getCode()) {
            throw new BizException(ResultCode.PARAM_INVALID,
                "学生 " + profile.getStudentNo() + " 未设置性别，无法进行体测评分");
        }
        return gender.getCode();
    }

    /**
     * 由入学年份推算当前年级（大一=1 … 大四=4，学年以 9 月为界）。
     * 入学年份缺失时按大一规则集兜底并告警。
     */
    private Integer resolveGrade(Integer enrollYear) {
        if (enrollYear == null) {
            log.warn("学生入学年份缺失，默认按大一（规则集1）评分");
            return 1;
        }
        LocalDate today = LocalDate.now();
        int academicYear = today.getMonthValue() >= 9
            ? today.getYear()
            : today.getYear() - 1;
        int grade = academicYear - enrollYear + 1;
        return Math.max(1, Math.min(4, grade));
    }

    /**
     * 解析体测时间（可选，缺省当前时间）。
     */
    private LocalDateTime parseTestTime(String testTime) {
        if (StringUtils.isBlank(testTime)) {
            return LocalDateTime.now();
        }
        try {
            return LocalDateTime.parse(testTime.trim(), TEST_TIME_FORMATTER);
        } catch (Exception e) {
            throw new BizException(ResultCode.PARAM_INVALID,
                "体测时间格式不正确，应为 yyyy-MM-dd HH:mm:ss");
        }
    }

    /**
     * 根据原始成绩 + 评分结果组装记录明细（含 itemValue / score）。
     * 逐项解析项目字典得到 itemId；项目不在字典中视为非法参数，fail-fast。
     */
    private List<StudentFitnessRecordItem> buildRecordItems(
        Map<String, Double> rawScores, FitnessScoreResult scoreResult) {

        Map<String, Integer> itemScores = scoreResult.getItemScores();
        List<StudentFitnessRecordItem> items = new ArrayList<>(rawScores.size());

        for (Map.Entry<String, Double> entry : rawScores.entrySet()) {
            String itemCode = entry.getKey();
            Double rawValue = entry.getValue();

            Long itemId = fitnessItemRepository.findIdByCode(itemCode)
                .orElseThrow(() -> new BizException(ResultCode.PARAM_INVALID,
                    "未知的体测项目: " + itemCode));

            Integer score = itemScores.getOrDefault(itemCode, 0);
            items.add(StudentFitnessRecordItem.fromScoreDetail(
                itemId,
                BigDecimal.valueOf(rawValue),
                score
            ));
        }
        return items;
    }

}
