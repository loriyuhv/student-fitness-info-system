package com.wsw.fitnesssystem.data_exchange.application.plugin;

import com.wsw.fitnesssystem.data_exchange.application.collector.ErrorCollector;
import com.wsw.fitnesssystem.data_exchange.application.config.ImportApplicationProperties;

import java.util.List;

/**
 * 导入插件契约。
 * <p>
 * 各业务模块通过实现此接口接入导入中台。中台通过此接口与具体业务解耦，
 * 新增业务只需新增一个实现类即可，无需修改中台核心代码。
 * </p>
 *
 * @param <T> 文件行映射的 DTO 类型
 * @param <E> 持久化对应的 Entity 类型
 * @author loriyuhv
 * @version 1.0 2026/8/21 11:50
 * @since 1.0
 */
public interface ImportPlugin<T, E> {

    /** 默认每批处理条数（仅作为后备值，实现类应使用配置覆盖） */
    int DEFAULT_BATCH_SIZE = 500;

    /**
     * 获取业务类型标识，全局唯一。
     * <p>
     * 示例：{@code "USER_IMPORT"}、{@code "FITNESS_RECORD_IMPORT"}
     * </p>
     *
     * @return 业务类型编码
     */
    String getBizType();

    /**
     * 获取 DTO 类型，用于文件解析时的反射映射。
     *
     * @return DTO Class
     */
    Class<T> getDtoClass();

    /**
     * 获取每批处理条数。
     * <p>
     * 建议实现类通过注入 {@link ImportApplicationProperties} 从配置读取。
     * </p>
     *
     * @return 每批处理条数
     */
    default int getBatchSize() {
        return DEFAULT_BATCH_SIZE;
    }

    /**
     * 业务校验。
     * <p>
     * 对单批数据进行校验，返回通过校验的数据。
     * 校验失败的数据需通过 {@link ErrorCollector} 记录，
     * 中台会统一生成错误文件。
     * </p>
     * <p>
     * <b>建议实现的校验项：</b>
     * <ul>
     *   <li>必填字段校验</li>
     *   <li>格式校验（如手机号、邮箱正则）</li>
     *   <li>批量查重（数据库已存在的数据过滤）</li>
     * </ul>
     * </p>
     *
     * @param batch 一批待校验的数据（DTO 列表）
     * @return 校验通过的数据列表
     */
    List<T> validate(List<T> batch);

    /**
     * 数据转换。
     * <p>
     * 将校验通过的 DTO 转换为待持久化的 Entity。
     * </p>
     * <p>
     * <b>建议实现：</b>
     * <ul>
     *   <li>字段映射</li>
     *   <li>默认值填充</li>
     *   <li>敏感字段处理（如密码加密）</li>
     * </ul>
     * </p>
     *
     * @param dtoList 校验通过的 DTO 列表
     * @return 待持久化的 Entity 列表
     */
    List<E> convert(List<T> dtoList);

    /**
     * 批量持久化。
     * <p>
     * 将转换后的 Entity 批量写入数据库。
     * </p>
     *
     * @param entities 待持久化的 Entity 列表
     * @return 实际成功写入的行数
     */
    int persist(List<E> entities);

    // ====== 通用元数据方法（用于错误文件生成） ======

    /**
     * 获取 Excel 列头（用于生成错误文件）。
     * <p>
     * 返回的列头顺序需与 {@link #validate} 中构建行数据的顺序一致。
     * </p>
     *
     * @return 列头列表
     */
    default List<String> getHeaders() {
        return List.of();
    }

}
