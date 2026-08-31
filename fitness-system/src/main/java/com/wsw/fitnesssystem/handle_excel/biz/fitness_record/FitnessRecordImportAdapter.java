package com.wsw.fitnesssystem.handle_excel.biz.fitness_record;

import com.wsw.fitnesssystem.handle_excel.application.dto.FitnessExcelDTO;
import com.wsw.fitnesssystem.handle_excel.core.adapter.ImportAdapter;
import com.wsw.fitnesssystem.handle_excel.domain.enums.ExcelBizTypeEnum;
import com.wsw.fitnesssystem.handle_excel.infrastructure.persistence.db.entity.FitnessRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author loriyuhv
 * @version 1.0 2026/8/30 16:11
 * @since 1.0
 */
@Slf4j
@Component
public class FitnessRecordImportAdapter implements ImportAdapter<FitnessExcelDTO, FitnessRecord> {
    @Override
    public String getBizType() {
        return ExcelBizTypeEnum.FITNESS_RECORD_IMPORT.getCode();
    }

    @Override
    public Class<FitnessExcelDTO> getDtoClass() {
        return FitnessExcelDTO.class;
    }

    @Override
    public int getBatchSize() {
        return ImportAdapter.super.getBatchSize();
    }

    @Override
    public List<FitnessExcelDTO> validate(List<FitnessExcelDTO> batch) {
        return List.of();
    }

    @Override
    public List<FitnessRecord> convert(List<FitnessExcelDTO> dtoList) {
        return List.of();
    }

    @Override
    public int persist(List<FitnessRecord> entities) {
        return 0;
    }
}
