package com.wsw.fitnesssystem.health.infrastructure.persistence.repository;

import com.wsw.fitnesssystem.health.domain.port.FitnessBonusRuleRepository;
import com.wsw.fitnesssystem.health.infrastructure.persistence.mapper.FitnessBonusRuleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/4 20:53
 * @since 1.0
 */
@Repository
@RequiredArgsConstructor
public class FitnessBonusRuleRepositoryImpl implements FitnessBonusRuleRepository {

    private final FitnessBonusRuleMapper fitnessBonusRuleMapper;

    @Override
    public Integer findBonus(
        Long ruleSetId, String itemCode, Integer gender, Double rawValue, boolean isReverse
    ) {
        return fitnessBonusRuleMapper.selectBonus(ruleSetId, itemCode, gender, rawValue, isReverse);
    }

}
