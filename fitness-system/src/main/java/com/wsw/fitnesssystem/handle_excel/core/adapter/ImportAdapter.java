package com.wsw.fitnesssystem.handle_excel.core.adapter;

import com.wsw.fitnesssystem.handle_excel.infrastructure.config.ExcelConstants;

import java.util.List;

/**
 * 导入适配器契约 — 各业务模块必须实现此接口
 * 中台通过此接口与具体业务解耦，新增业务只需实现该接口即可接入
 *
 * @param <T> Excel 解析对应的 DTO 类型
 * @param <E> 持久化对应的 Entity 类型
 * @author loriyuhv
 * @version 1.0 2026/8/21 11:50
 * @since 1.0
 */
public interface ImportAdapter<T, E> {

    /**
     * 业务类型标识，全局唯一
     * 示例："USER_IMPORT", "FITNESS_RECORD_IMPORT"
     * @return 具体业务类型
     */
    String getBizType();

    /**
     * Excel 对应的 DTO Class，用于 EasyExcel 反射解析
     * @return DTO Class
     */
    Class<T> getDtoClass();

    /**
     * 每批处理数量，默认 500 条
     * 可根据业务调整（如体测数据字段多，可设为 200）
     *
     * @return 每批处理数量
     */
    default int getBatchSize() {
        return ExcelConstants.DEFAULT_BATCH_SIZE;
    }

    /**
     * 业务校验
     * <p>建议实现：</p>
     * <li>1. 必填字段校验</li>
     * <li>2. 格式校验（手机号、邮箱正则）</li>
     * <li>3. 批量查重（数据库已存在的数据过滤）</li>
     * @param batch 一批 Excel DTO
     * @return 校验通过的 DTO 列表（失败的自行记录或过滤）
     */
    List<T> validate(List<T> batch);

    /**
     * 数据转换：DTO → Entity
     * <p>建议实现：</p>
     * <li>1. 字段映射</li>
     * <li>2. 默认值填充（如 campusId、locked）</li>
     * <li>3. 敏感字段处理（如密码加密）</li>
     * @param dtoList 校验通过的 DTO 列表
     * @return 待持久化的 Entity 列表
     */
    List<E> convert(List<T> dtoList);

    /**
     * 批量持久化
     *
     * @param entities 转换后的 Entity 列表
     * @return 实际成功插入的行数
     */
    int persist(List<E> entities);

    // ====== 通用元数据方法（用于错误文件生成） ======

    /**
     * Excel 列头（顺序需与 toRowData 一致）
     */
    default List<String> getHeaders() {
        return List.of();
    }

}
