package com.wsw.fitnesssystem.handle_excel.infrastructure.excel;

import com.alibaba.excel.EasyExcel;
import com.wsw.fitnesssystem.handle_excel.application.dto.UserExcelDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author loriyuhv
 * @version 1.0 2026/3/26 15:52
 * @since 1.0
 */
@Component
public class ExcelParser {
    public List<UserExcelDTO> parse(MultipartFile file) {
        List<UserExcelDTO> list = new ArrayList<>();

        try {
            EasyExcel.read(file.getInputStream(), UserExcelDTO.class,
                new UserExcelListener(list)).sheet().doRead();
        } catch (IOException e) {
            throw new RuntimeException("Excel解析失败", e);
        }

        return list;
    }
}
