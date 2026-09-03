package com.wsw.fitnesssystem.handle_excel.core.adapter;

import com.wsw.fitnesssystem.handle_excel.core.template.ExcelImportTemplate;
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
                    // 容器启动阶段，检测到同一个bizType出现多个实现类，抛出Fail-Fast机制，Spring终止启动
                    throw new IllegalStateException(
                        "Duplicate bizType registration: %s, conflicting classes: %s vs %s".formatted(
                            a.getBizType(), a.getDtoClass().getName(), b.getDtoClass().getName()
                        )
                    );

                })
            );

        log.info("Excel import adapters registered: {} types, bizTypes: [{}]",
            adapterMap.size(), String.join(", ", adapterMap.keySet())
        );
    }

    /**
     * 根据业务类型获取对应的导入适配器。
     * <p><b>工作机制：</b></p>
     * <ul>
     *     <li>适配器在 Spring 容器启动时由 {@link BusinessAdapterFactory} 自动扫描注册</li>
     *     <li>所有实现了 {@link ImportAdapter} 接口的 Bean，按其 {@link ImportAdapter#getBizType()} 返回值建立映射</li>
     *     <li>若同一 bizType 被多个适配器实现，容器启动时会抛出 {@link IllegalStateException}，遵循快速失败原则</li>
     * </ul>
     *
     * <p><b>返回值说明：</b></p>
     * <ul>
     *     <li>返回类型为 {@code ImportAdapter<?, ?>}（通配符类型），表示适配器具体的泛型参数（DTO 和 Entity 类型）由适配器实现类自身决定</li>
     *     <li>调用方（如 {@link ExcelImportTemplate}）在调用时会通过 Java 类型推断自动匹配具体的泛型参数</li>
     * </ul>
     *
     * <p><b>异常说明：</b></p>
     * <ul>
     *     <li>若传入的 {@code bizType} 未在容器中注册，抛出 {@link BizException}，错误码为 {@code PARAM_INVALID}</li>
     *     <li>异常信息中包含当前已注册的所有 bizType 列表，便于快速定位问题</li>
     * </ul>
     *
     * <p><b>调用示例：</b></p>
     * <pre>{@code
     * ImportAdapter<?, ?> adapter = adapterFactory.getImportAdapter("USER_IMPORT");
     * // 调用方会自动推断泛型类型，无需显式转换
     * excelImportTemplate.execute(taskId, file, adapter);
     * }</pre>
     *
     * @param bizType 业务类型编码
     * @return 对应的导入适配器实例，类型为通配符
     */
    public ImportAdapter<?, ?> getImportAdapter(String bizType) {
        ImportAdapter<?, ?> adapter = adapterMap.get(bizType);
        if (adapter == null) {
            throw new BizException(ResultCode.PARAM_INVALID,
                "Unsupported import type: %s, registered types: %s".formatted(
                    bizType, String.join(", ", adapterMap.keySet())
                )
            );
        }
        return adapter;
    }

    /**
     * 获取所有已注册的 bizType
     * @return bizType 编码列表
     */
    public List<String> getAllBizTypes() {
        return List.copyOf(adapterMap.keySet());
    }

}
