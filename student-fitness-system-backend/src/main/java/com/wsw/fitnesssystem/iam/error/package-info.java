/**
 * iam 限界上下文 —— 对外的错误码契约。
 *
 * <p><b>本包定位：</b>iam 模块对外的「错误语义字典」，集中管理认证、会话、
 * 风控、授权等所有子域对外的错误码。本包是 iam 模块<b>公开 API</b> 的一部分，
 * 被接口层 {@code GlobalExceptionHandler} 与前端错误码字典共同消费。</p>
 *
 * <p><b>为什么不是子域各自维护 error 包？</b></p>
 * <ul>
 *   <li>错误码是<b>上下文级契约</b>，不是子域内部实现——外界只关心
 *       「iam 会抛哪些错误」，不关心 iam 内部怎么划分子域</li>
 *   <li>{@code GlobalExceptionHandler} 需要在一处引用 iam 的所有错误码，
 *       集中管理避免跨子域的循环依赖</li>
 *   <li>未来 iam 若独立成 Maven module 或微服务，本包整体跟随迁移，
 *       无需修改外部依赖</li>
 * </ul>
 *
 * <p><b>放置原则：</b></p>
 * <ul>
 *   <li><b>只放 iam 专属错误码</b>：通用错误（参数、系统、接口）放
 *       {@link com.wsw.fitnesssystem.shared.kernel.error.CommonErrorCode}</li>
 *   <li><b>不依赖任何子域</b>：本包被 authentication / session / risk /
 *       authorization 子域依赖，不反向依赖它们</li>
 *   <li><b>不承载业务逻辑</b>：本包只有枚举，无 Bean、无工具方法</li>
 * </ul>
 *
 * <p><b>错误码枚举清单：</b></p>
 * <ul>
 *   <li>{@link com.wsw.fitnesssystem.iam.error.IamAuthNErrorCode} —— 认证 / Token / RefreshToken</li>
 *   <li>{@link com.wsw.fitnesssystem.iam.error.IamSessionErrorCode} —— 会话管理</li>
 *   <li>{@link com.wsw.fitnesssystem.iam.error.IamAuthZErrorCode} —— 授权 / 角色</li>
 *   <li>{@link com.wsw.fitnesssystem.iam.error.IamRiskErrorCode} —— 登录风控</li>
 * </ul>
 *
 * <p><b>命名规范：</b>{@code iam.{子域}.{错误}}</p>
 * <ul>
 *   <li>{@code iam.authn.*} —— 认证（Authentication）</li>
 *   <li>{@code iam.token.*} —— 访问令牌</li>
 *   <li>{@code iam.refresh_token.*} —— 刷新令牌</li>
 *   <li>{@code iam.session.*} —— 会话</li>
 *   <li>{@code iam.authz.*} —— 授权（Authorization）</li>
 *   <li>{@code iam.risk.*} —— 风控</li>
 * </ul>
 *
 * <p><b>依赖方向（严格遵守）：</b></p>
 * <ul>
 *   <li>本包可依赖 {@code shared.kernel.error}</li>
 *   <li>本包<b>不依赖</b>任何 iam 子域（authentication / session / risk / authorization / audit）</li>
 *   <li>本包<b>不依赖</b> {@code interfaces} 包（HTTP 语义由接口层处理）</li>
 * </ul>
 *
 * <p><b>判断标准：</b>如果一个问题只与 iam 有关、与其它上下文无关，
 * 就属于本包。凡是「跨上下文通用」的错误，一律放 {@code shared.kernel.error}。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/21
 * @since 1.0
 */
package com.wsw.fitnesssystem.iam.error;