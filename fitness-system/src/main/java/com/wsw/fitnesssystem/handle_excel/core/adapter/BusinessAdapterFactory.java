package com.wsw.fitnesssystem.handle_excel.core.adapter;

import com.wsw.fitnesssystem.shared.exception.BizException;
import com.wsw.fitnesssystem.shared.response.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 业务适配器工厂
 * <li>Spring 启动时自动扫描所有 ImportAdapter 实现类，按 bizType 注册;</li>
 * <li>运行时根据 bizType 获取对应适配器</li>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/21 12:07
 * @since 1.0
 */
@Slf4j
@Component
public class BusinessAdapterFactory {

    private final Map<String, ImportAdapter<?, ?>> adapterMap;

    public BusinessAdapterFactory(List<ImportAdapter<?, ?>> adapters) {
        this.adapterMap = adapters
            .stream()
            .collect(Collectors.toMap(
                ImportAdapter::getBizType,
                adapter -> adapter,
                (a, b) -> {
                    throw new IllegalStateException(
                        "Duplicate bizType registration: " + a.getBizType()
                            + ", conflicting classes: " + a.getClass().getName()
                            + " vs " + b.getClass().getName()
                    );
                })
            );

        log.info("Excel import adapters registered: {} types, bizTypes: [{}]",
            adapterMap.size(), String.join(", ", adapterMap.keySet())
        );
    }

    /**
     * 根据业务类型获取导入适配器
     * @param bizType 业务类型编码
     * @return 导入适配器
     * @param <T> DTO类型
     * @param <E> Entity类型
     */
    @SuppressWarnings("unchecked")
    public <T, E> ImportAdapter<T, E> getImportAdapter(String bizType) {
        ImportAdapter<?, ?> adapter = adapterMap.get(bizType);
        if (adapter == null) {
            throw new BizException(ResultCode.PARAM_INVALID,
                "Unsupported import type: " + bizType + ", registered types: " + adapterMap.keySet()
            );
        }
        return (ImportAdapter<T, E>) adapter;
    }

    /**
     * 获取所有已注册的 bizType
     * @return bizType 编码列表
     */
    public List<String> getAllBizTypes() {
        return List.copyOf(adapterMap.keySet());
    }

}
