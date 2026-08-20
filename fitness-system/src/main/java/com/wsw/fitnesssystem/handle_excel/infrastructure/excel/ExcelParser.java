package com.wsw.fitnesssystem.handle_excel.infrastructure.excel;

import com.alibaba.excel.EasyExcel;
import com.wsw.fitnesssystem.handle_excel.application.dto.UserExcelDTO;
import com.wsw.fitnesssystem.shared.exception.BizException;
import com.wsw.fitnesssystem.shared.response.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author loriyuhv
 * @version 1.0 2026/3/26 15:52
 * @since 1.0
 */
@Slf4j
@Component
public class ExcelParser {
    /**
     * 解析磁盘文件（异步线程使用）
     * @param file 文件
     * @return 解析后的用户对象列表
     */
    public List<UserExcelDTO> parse(File file) {
        List<UserExcelDTO> list = new ArrayList<>();

        try {
            EasyExcel.read(file, UserExcelDTO.class,
                new UserExcelListener(list)).sheet().doRead();
        } catch (Exception e) {
            log.error("Excel 解析失败", e);
            throw new BizException(ResultCode.PARAM_TYPE_ERROR);
        }

        return list.stream()
                .filter(dto -> dto.getUsername() != null || dto.getNickname() != null)
                .toList();
    }
}
