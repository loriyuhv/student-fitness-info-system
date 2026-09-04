package com.wsw.fitnesssystem.health.infrastructure.persistence.repository;

import com.wsw.fitnesssystem.health.domain.port.FitnessScoreRuleRepository;
import com.wsw.fitnesssystem.health.infrastructure.persistence.mapper.FitnessScoreRuleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/4 20:48
 * @since 1.0
 */
@Repository
@RequiredArgsConstructor
public class FitnessScoreRuleRepositoryImpl implements FitnessScoreRuleRepository {

    private final FitnessScoreRuleMapper fitnessScoreRuleMapper;

    @Override
    public Integer findScore(Long ruleSetId, String itemCode, Integer gender, Double rawValue) {
        return fitnessScoreRuleMapper.selectScore(ruleSetId, itemCode, gender, rawValue);
    }

}
