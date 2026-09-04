package com.wsw.fitnesssystem.health.application.service.impl;

import com.wsw.fitnesssystem.health.application.dto.FitnessScoreQueryCommand;
import com.wsw.fitnesssystem.health.application.dto.FitnessScoreResult;
import com.wsw.fitnesssystem.health.application.service.FitnessScoreQueryService;
import com.wsw.fitnesssystem.health.domain.port.FitnessBonusRuleRepository;
import com.wsw.fitnesssystem.health.domain.port.FitnessLevelRuleRepository;
import com.wsw.fitnesssystem.health.domain.port.FitnessScoreRuleRepository;
import com.wsw.fitnesssystem.health.domain.port.FitnessWeightLevelRepository;
import com.wsw.fitnesssystem.health.domain.valueobject.BmiResult;
import com.wsw.fitnesssystem.health.domain.valueobject.ScoreLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * 体测评分查询服务
 * 给定性别、年级、各项目原始成绩 → 计算得分、总分、等级、加分
 *
 * @author loriyuhv
 * @version 1.0 2026/9/4 21:02
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FitnessScoreQueryServiceImpl implements FitnessScoreQueryService {

    private final FitnessScoreRuleRepository scoreRuleRepository;
    private final FitnessBonusRuleRepository bonusRuleRepository;
    private final FitnessLevelRuleRepository levelRuleRepository;
    private final FitnessWeightLevelRepository weightLevelRepository;

    /** 项目权重配置（后续可改为数据库配置） */
    private static final Map<String, Double> WEIGHT_MAP = new HashMap<>();
    static {
        WEIGHT_MAP.put("BMI", 0.15);
        WEIGHT_MAP.put("VITAL_CAPACITY", 0.15);
        WEIGHT_MAP.put("50M", 0.20);
        WEIGHT_MAP.put("SIT_AND_REACH", 0.10);
        WEIGHT_MAP.put("STANDING_LONG_JUMP", 0.10);
        WEIGHT_MAP.put("PULL_UP", 0.10);
        WEIGHT_MAP.put("RUN_1000_800", 0.20);
    }

    @Override
    public FitnessScoreResult calculate(Integer gender, Integer grade, Map<String, Double> rawScores) {
        // 1. 确定规则集ID
        Long ruleSetId = getRuleSetId(grade);

        // 2. 计算各单项得分（含加分）
        Map<String, Integer> itemScores = new HashMap<>();
        Map<String, Integer> bonusDetails = new HashMap<>();
        int totalBonus = 0;
        Integer bmiScore = null;
        String bmiLevel = null;

        for (Map.Entry<String, Double> entry : rawScores.entrySet()) {
            String itemCode = entry.getKey();
            Double rawValue = entry.getValue();

            // BMI 特殊处理：单独查询，不通过 score_rule 表
            if ("BMI".equals(itemCode)) {
                BmiResult bmiResult = weightLevelRepository.findByGenderAndBmi(gender, rawValue);
                if (bmiResult != null) {
                    itemScores.put("BMI", bmiResult.getScore());
                    bmiScore = bmiResult.getScore();
                    bmiLevel = bmiResult.getLevelName();
                } else {
                    itemScores.put("BMI", 0);
                    bmiScore = 0;
                    bmiLevel = "未知";
                }
                continue;  // BMI 没有加分，直接跳过加分逻辑
            }

            // 2.1 单项得分
            Integer score = scoreRuleRepository.findScore(ruleSetId, itemCode, gender, rawValue);
            if (score == null) {
                log.warn("未找到评分规则: ruleSetId={}, itemCode={}, gender={}, rawValue={}",
                    ruleSetId, itemCode, gender, rawValue);
                score = 0;
            }
            itemScores.put(itemCode, score);

            // 2.2 查加分（传入 isReverse 标志）
            boolean isReverse = isReverseItem(itemCode);
            Integer bonus = bonusRuleRepository.findBonus(ruleSetId, itemCode, gender, rawValue, isReverse);
            if (bonus != null && bonus > 0) {
                bonusDetails.put(itemCode, bonus);
                totalBonus += bonus;
            }
        }

        // 3. 计算加权总分
        BigDecimal weightedSum = BigDecimal.ZERO;
        for (Map.Entry<String, Integer> entry : itemScores.entrySet()) {
            String itemCode = entry.getKey();
            Integer score = entry.getValue();
            Double weight = WEIGHT_MAP.getOrDefault(itemCode, 0.0);
            BigDecimal scoreBD = BigDecimal.valueOf(score);
            BigDecimal weightBD = BigDecimal.valueOf(weight);
            weightedSum = weightedSum.add(scoreBD.multiply(weightBD));
        }

        // 4. 总分 = 加权总分 + 加分
        BigDecimal totalScore = weightedSum.add(BigDecimal.valueOf(totalBonus))
            .setScale(2, RoundingMode.HALF_UP);

        // 5. 查询等级
        ScoreLevel level = levelRuleRepository.findLevel(ruleSetId, gender, totalScore);
        String levelCode = level != null ? level.getCode() : "FAIL";
        String levelName = level != null ? level.getName() : "不及格";

        // 6. 构建结果
        return FitnessScoreResult.builder()
            .itemScores(itemScores)
            .bmiScore(bmiScore)
            .bmiLevel(bmiLevel)
            .totalBonus(totalBonus)
            .bonusDetails(bonusDetails)
            .totalScore(totalScore)
            .levelCode(levelCode)
            .levelName(levelName)
            .build();
    }

    @Override
    public Integer querySingleScore(FitnessScoreQueryCommand command) {
        Long ruleSetId = getRuleSetId(command.getGrade());
        return scoreRuleRepository.findScore(
            ruleSetId,
            command.getItemCode(),
            command.getGender(),
            command.getRawValue()
        );
    }

    @Override
    public Integer querySingleBonus(FitnessScoreQueryCommand command) {
        Long ruleSetId = getRuleSetId(command.getGrade());
        return bonusRuleRepository.findBonus(
            ruleSetId,
            command.getItemCode(),
            command.getGender(),
            command.getRawValue(),
            isReverseItem(command.getItemCode())
        );
    }

    /**
     * 根据年级确定规则集ID
     *
     * @param grade 年级
     * @return 规则集ID
     */
    private Long getRuleSetId(Integer grade) {
        if (grade == null) {
            return 1L; // 默认大一大二
        }
        if (grade <= 2) {
            return 1L; // 大一、大二 → 大一大二标准
        } else {
            return 2L; // 大三、大四 → 大三大四标准
        }
    }

    /**
     * 判断项目是否为反向项目（值越小越好）
     * 反向项目：1000米/800米跑
     *
     * @param itemCode 体测项目编码 RUN_1000_800
     * @return 正向false 反向true
     */
    private boolean isReverseItem(String itemCode) {
        return "RUN_1000_800".equals(itemCode);
    }

}
