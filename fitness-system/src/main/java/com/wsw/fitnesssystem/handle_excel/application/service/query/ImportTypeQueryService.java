package com.wsw.fitnesssystem.handle_excel.application.service.query;

import com.wsw.fitnesssystem.handle_excel.application.plugin.ImportPluginRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 导入类型查询服务
 *
 * @author loriyuhv
 * @version 1.0 2026/9/8 12:51
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImportTypeQueryService {

    private final ImportPluginRegistry pluginRegistry;

    public List<String> getAllBizTypes() {
        return pluginRegistry.getAllBizTypes();
    }

}
