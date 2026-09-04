package com.wsw.fitnesssystem.health.infrastructure.persistence.repository;

import com.wsw.fitnesssystem.health.domain.port.FitnessWeightLevelRepository;
import com.wsw.fitnesssystem.health.domain.valueobject.BmiResult;
import com.wsw.fitnesssystem.health.infrastructure.persistence.mapper.FitnessWeightLevelMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/4 23:50
 * @since 1.0
 */
@Repository
@RequiredArgsConstructor
public class FitnessWeightLevelRepositoryImpl implements FitnessWeightLevelRepository {

    private final FitnessWeightLevelMapper fitnessWeightLevelMapper;

    @Override
    public BmiResult findByGenderAndBmi(Integer gender, Double bmi) {
        return fitnessWeightLevelMapper.selectByGenderAndBmi(gender, bmi);
    }

}
