/**
 * fitness 限界上下文 —— 对外的错误码契约。
 *
 * <p><b>本包定位：</b>fitness 模块对外的「错误语义字典」，集中管理体测记录、
 * 成绩计算、体测数据导入导出等错误码。本包是 fitness 模块<b>公开 API</b> 的一部分。</p>
 *
 * <p><b>放置原则：</b></p>
 * <ul>
 *   <li><b>只放 fitness 专属错误码</b>：通用错误（参数、系统、文件）放
 *       {@link com.wsw.fitnesssystem.shared.kernel.error.CommonErrorCode}</li>
 *   <li><b>不依赖任何内部包</b>：本包被 fitness 内部的 domain / application /
 *       infrastructure / interfaces 依赖，不反向依赖它们</li>
 *   <li><b>不承载业务逻辑</b>：本包只有枚举，无 Bean、无工具方法</li>
 * </ul>
 *
 * <p><b>错误码枚举清单：</b></p>
 * <ul>
 *   <li>{@link com.wsw.fitnesssystem.fitness.error.FitnessErrorCode}
 *       —— 体测数据、成绩计算</li>
 * </ul>
 *
 * <p><b>命名规范：</b>{@code fitness.{子域}.{错误}}</p>
 * <ul>
 *   <li>{@code fitness.data.not_found} —— 体测数据不存在</li>
 *   <li>{@code fitness.data.already_exist} —— 体测数据已存在</li>
 *   <li>{@code fitness.score.calculate_error} —— 成绩计算失败</li>
 *   <li>{@code fitness.data.import_error} —— 体测数据导入失败</li>
 *   <li>{@code fitness.data.export_error} —— 体测数据导出失败</li>
 * </ul>
 *
 * <p><b>依赖方向（严格遵守）：</b></p>
 * <ul>
 *   <li>本包可依赖 {@code shared.kernel.error}</li>
 *   <li>本包<b>不依赖</b> fitness 内部的任何实现包</li>
 *   <li>本包<b>不依赖</b> {@code interfaces} 包（HTTP 语义由接口层处理）</li>
 * </ul>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/21
 * @since 1.0
 */
package com.wsw.fitnesssystem.fitness.error;