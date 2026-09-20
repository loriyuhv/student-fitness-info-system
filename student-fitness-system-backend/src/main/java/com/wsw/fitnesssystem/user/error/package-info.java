/**
 * user 限界上下文 —— 对外的错误码契约。
 *
 * <p><b>本包定位：</b>user 模块对外的「错误语义字典」，集中管理用户档案、
 * 账号基础信息、唯一约束等错误码。本包是 user 模块<b>公开 API</b> 的一部分。</p>
 *
 * <p><b>放置原则：</b></p>
 * <ul>
 *   <li><b>只放 user 专属错误码</b>：通用错误（参数、系统、接口）放
 *       {@link com.wsw.fitnesssystem.shared.kernel.error.CommonErrorCode}</li>
 *   <li><b>不依赖任何内部包</b>：本包被 user 内部的 domain / application /
 *       infrastructure / interfaces 依赖，不反向依赖它们</li>
 *   <li><b>不承载业务逻辑</b>：本包只有枚举，无 Bean、无工具方法</li>
 * </ul>
 *
 * <p><b>错误码枚举清单：</b></p>
 * <ul>
 *   <li>{@link com.wsw.fitnesssystem.user.error.UserErrorCode} —— 用户档案、账号、唯一约束</li>
 * </ul>
 *
 * <p><b>命名规范：</b>{@code user.{子域}.{错误}}</p>
 * <ul>
 *   <li>{@code user.not_found} —— 用户不存在</li>
 *   <li>{@code user.phone.already_exists} —— 手机号已被使用</li>
 *   <li>{@code user.email.already_exists} —— 邮箱已被使用</li>
 *   <li>{@code user.username.already_exists} —— 用户名已被占用</li>
 * </ul>
 *
 * <p><b>依赖方向（严格遵守）：</b></p>
 * <ul>
 *   <li>本包可依赖 {@code shared.kernel.error}</li>
 *   <li>本包<b>不依赖</b> {@code user.domain} / {@code user.application} /
 *       {@code user.infrastructure} / {@code user.interfaces}</li>
 *   <li>本包<b>不依赖</b> {@code interfaces} 包（HTTP 语义由接口层处理）</li>
 * </ul>
 *
 * <p><b>判断标准：</b>如果一个问题只与 user 有关、与其它上下文无关，
 * 就属于本包。凡是「跨上下文通用」的错误，一律放 {@code shared.kernel.error}。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/21
 * @since 1.0
 */
package com.wsw.fitnesssystem.user.error;