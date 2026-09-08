package com.wsw.fitnesssystem.handle_excel.application.plugin;

import com.wsw.fitnesssystem.handle_excel.application.orchestration.ImportOrchestrator;
import com.wsw.fitnesssystem.shared.exception.BizException;
import com.wsw.fitnesssystem.shared.response.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 导入插件注册表。
 * <p>
 * <b>职责：</b>收集所有已注册的 {@link ImportPlugin} 实现，按业务类型（bizType）建立索引，
 * 供运行时根据业务类型查找对应的插件实例。
 * <p>
 * <b>工作机制：</b>
 * <ul>
 *   <li>Spring 容器启动时，自动扫描所有 {@link ImportPlugin} Bean</li>
 *   <li>按 {@link ImportPlugin#getBizType()} 建立 {@code bizType → Plugin} 映射</li>
 *   <li>若同一 bizType 被多个插件实现，容器启动失败（快速失败原则）</li>
 * </ul>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/21 12:07
 * @since 1.0
 */
@Slf4j
@Component
public class ImportPluginRegistry {

    private final Map<String, ImportPlugin<?, ?>> pluginMap;

    public ImportPluginRegistry(List<ImportPlugin<?, ?>> plugins) {
        this.pluginMap = plugins
            .stream()
            .collect(Collectors.toMap(
                ImportPlugin::getBizType,
                plugin -> plugin,
                this::throwOnDuplicate
            ));

        log.info("Import plugins registered: {} types, bizTypes: [{}]",
            pluginMap.size(), String.join(", ", pluginMap.keySet())
        );
    }

    /**
     * 根据业务类型获取对应的导入插件。
     * <p><b>返回值说明：</b></p>
     * <ul>
     *     <li>返回类型为 {@code ImportPlugin<?, ?>}（通配符类型），
     *     表示插件具体的泛型参数（DTO 和 Entity 类型）由插件实现类自身决定</li>
     *     <li>调用方（如 {@link ImportOrchestrator}）在调用时会通过 Java 类型推断自动匹配具体的泛型参数</li>
     * </ul>
     *
     * <p><b>异常说明：</b></p>
     * <ul>
     *     <li>若传入的 {@code bizType} 未在容器中注册，抛出 {@link BizException}，
     *     错误码为 {@code PARAM_INVALID}</li>
     *     <li>异常信息中包含当前已注册的所有 bizType 列表，便于快速定位问题</li>
     * </ul>
     *
     * <p><b>调用示例：</b></p>
     * <pre>{@code
     * ImportPlugin<?, ?> plugin = pluginRegistry.getImportPlugin("USER_IMPORT");
     * // 调用方会自动推断泛型类型，无需显式转换
     * importOrchestrator.execute(taskId, file, plugin);
     * }</pre>
     *
     * @param bizType 业务类型编码
     * @return 对应的导入插件实例，类型为通配符
     */
    public ImportPlugin<?, ?> getPlugin(String bizType) {
        ImportPlugin<?, ?> plugin = pluginMap.get(bizType);
        if (plugin == null) {
            throw new BizException(ResultCode.PARAM_INVALID,
                "不支持的导入类型：%s，已注册类型：%s".formatted(
                    bizType, String.join(", ", pluginMap.keySet())
                )
            );
        }
        return plugin;
    }

    /**
     * 获取所有已注册的业务类型列表。
     * @return bizType 编码列表
     */
    public List<String> getAllBizTypes() {
        return List.copyOf(pluginMap.keySet());
    }

    // ================================================================
    //  私有方法
    // ================================================================

    /**
     * 处理 bizType 重复注册（快速失败）。
     * <p>
     * 若同一 bizType 被多个插件实现，Spring 容器启动时直接报错，
     * 避免运行时因不确定使用哪个实现而出现诡异行为。
     *
     * @param existing 已注册的插件
     * @param duplicate 重复的插件
     * @return 不返回，直接抛异常
     */
    private ImportPlugin<?, ?> throwOnDuplicate(ImportPlugin<?, ?> existing, ImportPlugin<?, ?> duplicate) {
        throw new IllegalStateException(String.format(
            "Duplicate bizType registration: %s, conflicting classes: %s and %s",
            existing.getBizType(),
            existing.getDtoClass().getName(),
            duplicate.getDtoClass().getName()
        ));
    }

}
