package com.wsw.fitnesssystem.fitness.interfaces.web;

import com.wsw.fitnesssystem.fitness.application.dto.command.FitnessRecordSubmitCommand;
import com.wsw.fitnesssystem.fitness.application.dto.result.FitnessRecordSubmitResult;
import com.wsw.fitnesssystem.fitness.application.service.FitnessRecordSubmitService;
import com.wsw.fitnesssystem.fitness.interfaces.web.dto.request.FitnessRecordSubmitRequest;
import com.wsw.fitnesssystem.fitness.interfaces.web.dto.response.FitnessRecordDetailResponse;
import com.wsw.fitnesssystem.fitness.interfaces.web.dto.response.FitnessRecordListItemResponse;
import com.wsw.fitnesssystem.shared.application.context.RequestContextHolder;
import com.wsw.fitnesssystem.shared.domain.vb.Operator;
import com.wsw.fitnesssystem.shared.interfaces.web.response.ApiResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 体测记录提交接口
 * <p>教师前端提交一条学生体测记录：学号 + 各项目原始成绩，
 * 后端完成 评分 → 记录主表/明细落库 → 汇总更新 全链路。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/13 15:34
 * @since 1.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/fitness")
public class FitnessController {

    private final FitnessRecordSubmitService fitnessRecordSubmitService;

    /**
     * 提交一条体测记录（教师/管理员）
     *
     * @param request 请求体（学号 + 原始成绩）
     * @return 落库后的体测记录结果
     */
    @PostMapping("/records")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResult<FitnessRecordSubmitResult> submitRecord(
        @RequestBody @Valid FitnessRecordSubmitRequest request) {

        // 操作人从安全上下文取（JWT 认证过滤器注入）
        Operator operator = RequestContextHolder.getRequiredOperator();

        FitnessRecordSubmitCommand command = FitnessRecordSubmitCommand.builder()
            .studentNo(request.getStudentNo())
            .operatorUserId(operator.userId())
            .testRound(request.getTestRound())
            .testTime(request.getTestTime())
            .rawScores(request.getRawScores())
            .build();

        FitnessRecordSubmitResult result = fitnessRecordSubmitService.submit(command);

        log.info("体测记录提交成功: recordId={}, studentNo={}, operatorUserId={}",
            result.getRecordId(), request.getStudentNo(), operator.userId());

        return ApiResult.success(result);
    }


    /**
     * 查询体测记录列表（数据权限自动过滤：学生=自己，教师=本班，管理员=全部）
     *
     * @return 学生体测记录列表
     */
    @GetMapping("/records")
    @PreAuthorize("isAuthenticated()")
    public ApiResult<List<FitnessRecordListItemResponse>> listRecords() {
        return ApiResult.success(buildMockList());
    }

    /**
     * 查询某条体测记录详情
     *
     * @param recordId 体测记录ID
     * @return 体测记录详情
     */
    @GetMapping("/records/{recordId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResult<FitnessRecordDetailResponse> getRecordDetail(
        @PathVariable Long recordId) {
        return ApiResult.success(buildMockDetail(recordId));
    }

    /**
     * Mock 列表：对应前端 MOCK_RECORDS
     */
    private List<FitnessRecordListItemResponse> buildMockList() {
        return List.of(
            FitnessRecordListItemResponse.builder()
                .recordId(3L)
                .testTime(LocalDateTime.of(2025, 5, 18, 9, 30, 0))
                .testRound(3)
                .totalScore(new BigDecimal("86.5"))
                .level("良好")
                .build(),
            FitnessRecordListItemResponse.builder()
                .recordId(2L)
                .testTime(LocalDateTime.of(2024, 11, 9, 10, 20, 0))
                .testRound(2)
                .totalScore(new BigDecimal("74.2"))
                .level("及格")
                .build(),
            FitnessRecordListItemResponse.builder()
                .recordId(1L)
                .testTime(LocalDateTime.of(2024, 5, 22, 14, 0, 0))
                .testRound(1)
                .totalScore(new BigDecimal("91.8"))
                .level("优秀")
                .build()
        );
    }

    /**
     * Mock 详情：对应前端 MOCK_DETAILS
     */
    private FitnessRecordDetailResponse buildMockDetail(Long recordId) {
        if (recordId == null) {
            return null;
        }
        return switch (recordId.intValue()) {
            case 3 -> mockDetail3();
            case 2 -> mockDetail2();
            case 1 -> mockDetail1();
            default -> null;
        };
    }

    private FitnessRecordDetailResponse mockDetail3() {
        return FitnessRecordDetailResponse.builder()
            .recordId(3L)
            .student(buildMockStudent())
            .testTime(LocalDateTime.of(2025, 5, 18, 9, 30, 0))
            .testRound(3)
            .totalScore(new BigDecimal("86.5"))
            .level("良好")
            .items(buildItems(
                new int[]{100, 90, 90, 84, 76, 80, 84},
                new String[]{"22.3", "4800", "6.9", "17.5", "235", "12", "232"},
                new int[]{0, 0, 0, 0, 0, 1, 0}
            ))
            .summary(FitnessRecordDetailResponse.Summary.builder()
                .physiqueType("匀称耐力型")
                .kValue(2)
                .sportPrescription(List.of(
                    "每周 3 次、每次 30 分钟中高强度有氧跑",
                    "加强核心力量训练，每周 2 次平板支撑"
                ))
                .build())
            .build();
    }

    private FitnessRecordDetailResponse mockDetail2() {
        return FitnessRecordDetailResponse.builder()
            .recordId(2L)
            .student(buildMockStudent())
            .testTime(LocalDateTime.of(2024, 11, 9, 10, 20, 0))
            .testRound(2)
            .totalScore(new BigDecimal("74.2"))
            .level("及格")
            .items(buildItems(
                new int[]{60, 72, 64, 68, 62, 60, 74},
                new String[]{"24.6", "3520", "8.9", "9.2", "208", "7", "243"},
                new int[]{0, 0, 0, 0, 0, 0, 0}
            ))
            .summary(FitnessRecordDetailResponse.Summary.builder()
                .physiqueType("偏弱肌力型")
                .kValue(4)
                .sportPrescription(List.of(
                    "增加抗阻训练：每周 2 次俯卧撑/哑铃",
                    "耐力跑前先做 5 分钟动态热身"
                ))
                .build())
            .build();
    }

    private FitnessRecordDetailResponse mockDetail1() {
        return FitnessRecordDetailResponse.builder()
            .recordId(1L)
            .student(buildMockStudent())
            .testTime(LocalDateTime.of(2024, 5, 22, 14, 0, 0))
            .testRound(1)
            .totalScore(new BigDecimal("91.8"))
            .level("优秀")
            .items(buildItems(
                new int[]{100, 100, 95, 92, 90, 95, 88},
                new String[]{"21.4", "5060", "6.5", "20.6", "262", "18", "226"},
                new int[]{0, 0, 0, 0, 0, 1, 1}
            ))
            .summary(FitnessRecordDetailResponse.Summary.builder()
                .physiqueType("强健均衡型")
                .kValue(1)
                .sportPrescription(List.of(
                    "保持当前运动习惯，每周 2 次高强度间歇训练",
                    "注意运动后的拉伸与恢复"
                ))
                .build())
            .build();
    }

    private FitnessRecordDetailResponse.StudentBrief buildMockStudent() {
        return FitnessRecordDetailResponse.StudentBrief.builder()
            .studentNo("2023420115")
            .name("陈小宇")
            .className("计算机科学与技术2301班")
            .build();
    }

    /** 项目元数据（顺序即展示顺序，与前端 ITEM_META 对齐） */
    private static final List<ItemMeta> ITEM_META = List.of(
        new ItemMeta("BMI", "体重指数（BMI）", "kg/m²"),
        new ItemMeta("VITAL_CAPACITY", "肺活量", "ml"),
        new ItemMeta("50M", "50米跑", "s"),
        new ItemMeta("SIT_AND_REACH", "坐位体前屈", "cm"),
        new ItemMeta("STANDING_LONG_JUMP", "立定跳远", "cm"),
        new ItemMeta("PULL_UP", "引体向上（男）", "次"),
        new ItemMeta("RUN_1000_800", "1000米跑（男）", "s")
    );

    private record ItemMeta(String itemCode, String itemName, String unit) {}

    private List<FitnessRecordDetailResponse.ItemDetail> buildItems(
        int[] scores, String[] values, int[] bonuses) {

        return java.util.stream.IntStream.range(0, ITEM_META.size())
            .mapToObj(i -> {
                ItemMeta meta = ITEM_META.get(i);
                return FitnessRecordDetailResponse.ItemDetail.builder()
                    .itemCode(meta.itemCode())
                    .itemName(meta.itemName())
                    .unit(meta.unit())
                    .itemValue(new BigDecimal(values[i]))
                    .score(scores[i])
                    .bonus(bonuses[i])
                    .build();
            })
            .toList();
    }

}
