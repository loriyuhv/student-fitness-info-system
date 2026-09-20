/**
 * data_exchange 限界上下文 —— 对外的错误码契约。
 *
 * <p><b>本包定位：</b>data_exchange 模块对外的「错误语义字典」，
 * 集中管理文件导入导出、批量处理、任务生命周期等错误码。
 * 本包是 data_exchange 模块<b>公开 API</b> 的一部分。</p>
 *
 * <p><b>放置原则：</b></p>
 * <ul>
 *   <li><b>只放 data_exchange 专属错误码</b>：通用文件错误（文件不存在、上传失败等）
 *       放 {@link com.wsw.fitnesssystem.shared.kernel.error.CommonErrorCode}</li>
 *   <li><b>不依赖任何内部包</b>：本包被 data_exchange 内部的 domain /
 *       application / infrastructure / interfaces 依赖，不反向依赖它们</li>
 *   <li><b>不承载业务逻辑</b>：本包只有枚举，无 Bean、无工具方法</li>
 * </ul>
 *
 * <p><b>错误码枚举清单：</b></p>
 * <ul>
 *   <li>{@link com.wsw.fitnesssystem.data_exchange.error.DataExchangeErrorCode}
 *       —— 导入任务、批量处理</li>
 * </ul>
 *
 * <p><b>命名规范：</b>{@code data_exchange.{子域}.{错误}}</p>
 * <ul>
 *   <li>{@code data_exchange.import_task.not_found} —— 导入任务不存在</li>
 *   <li>{@code data_exchange.task.cancelled} —— 导入任务已被用户取消</li>
 * </ul>
 *
 * <p><b>与 {@code CommonErrorCode} 的边界：</b></p>
 * <ul>
 *   <li>通用文件操作（{@code common.file.not_found}）→ {@code CommonErrorCode}</li>
 *   <li>业务级任务（{@code data_exchange.import_task.not_found}）→ 本包</li>
 * </ul>
 *
 * <p><b>依赖方向（严格遵守）：</b></p>
 * <ul>
 *   <li>本包可依赖 {@code shared.kernel.error}</li>
 *   <li>本包<b>不依赖</b> data_exchange 内部的任何实现包</li>
 *   <li>本包<b>不依赖</b> {@code interfaces} 包（HTTP 语义由接口层处理）</li>
 * </ul>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/21
 * @since 1.0
 */
package com.wsw.fitnesssystem.data_exchange.error;