package com.wsw.fitnesssystem.fitness.infrastructure.persistence.repository;

import com.wsw.fitnesssystem.fitness.domain.port.FitnessItemRepository;
import com.wsw.fitnesssystem.fitness.infrastructure.persistence.mapper.FitnessItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 体测项目字典仓储实现
 *
 * @author loriyuhv
 * @version 1.0 2026/9/5
 */
@Repository
@RequiredArgsConstructor
public class FitnessItemRepositoryImpl implements FitnessItemRepository {

    private final FitnessItemMapper fitnessItemMapper;

    @Override
    public Optional<Long> findIdByCode(String itemCode) {
        return Optional.ofNullable(fitnessItemMapper.selectItemIdByCode(itemCode));
    }

}
