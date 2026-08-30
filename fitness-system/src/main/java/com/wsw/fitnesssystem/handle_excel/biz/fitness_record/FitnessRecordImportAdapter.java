package com.wsw.fitnesssystem.handle_excel.biz.fitness_record;

import com.wsw.fitnesssystem.handle_excel.application.dto.UserExcelDTO;
import com.wsw.fitnesssystem.handle_excel.core.adapter.ImportAdapter;
import com.wsw.fitnesssystem.handle_excel.domain.enums.ExcelBizTypeEnum;
import com.wsw.fitnesssystem.handle_excel.infrastructure.persistence.db.entity.SysUser;
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
public class FitnessRecordImportAdapter implements ImportAdapter<UserExcelDTO, SysUser> {
    @Override
    public String getBizType() {
        return ExcelBizTypeEnum.FITNESS_RECORD_IMPORT.getCode();
    }

    @Override
    public Class<UserExcelDTO> getDtoClass() {
        return UserExcelDTO.class;
    }

    @Override
    public List<UserExcelDTO> validate(List<UserExcelDTO> batch) {
        return List.of();
    }

    @Override
    public List<SysUser> convert(List<UserExcelDTO> dtoList) {
        return List.of();
    }

    @Override
    public void persist(List<SysUser> entities) {

    }
}
