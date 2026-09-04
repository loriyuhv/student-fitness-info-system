package com.wsw.fitnesssystem.health.infrastructure.persistence.repository;

import com.wsw.fitnesssystem.health.domain.port.FitnessLevelRuleRepository;
import com.wsw.fitnesssystem.health.domain.valueobject.ScoreLevel;
import com.wsw.fitnesssystem.health.infrastructure.persistence.mapper.FitnessLevelRuleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/4 20:55
 * @since 1.0
 */
@Repository
@RequiredArgsConstructor
public class FitnessLevelRuleRepositoryImpl implements FitnessLevelRuleRepository {

    private final FitnessLevelRuleMapper fitnessLevelRuleMapper;

    @Override
    public ScoreLevel findLevel(Long ruleSetId, Integer gender, BigDecimal totalScore) {
        // 先查性别专用，再查通用
        return fitnessLevelRuleMapper.selectLevel(ruleSetId, gender, totalScore);
    }

}
