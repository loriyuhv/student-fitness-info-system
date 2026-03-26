package com.wsw.fitnesssystem.handle_excel.infrastructure.excel;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.wsw.fitnesssystem.handle_excel.application.dto.UserExcelDTO;

import java.util.List;

/**
 * @author loriyuhv
 * @version 1.0 2026/3/26 15:52
 * @since 1.0
 */
public class UserExcelListener extends AnalysisEventListener<UserExcelDTO> {
    private final List<UserExcelDTO> list;

    public UserExcelListener(List<UserExcelDTO> list) {
        this.list = list;
    }

    @Override
    public void invoke(UserExcelDTO data, AnalysisContext analysisContext) {
        list.add(data);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}
