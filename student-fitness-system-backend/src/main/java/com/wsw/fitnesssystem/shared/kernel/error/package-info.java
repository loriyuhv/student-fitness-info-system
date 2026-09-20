/**
 * 共享内核 —— 跨限界上下文共享的错误码契约。
 *
 * <p><b>本包定位：</b>所有限界上下文（iam / user / data_exchange / fitness 等）
 * 共同依赖的错误码契约与通用错误码。它是六边形架构「共享内核（Shared Kernel）」
 * 模式的落地，只承载规范与最小共识，不承载任何业务实现。</p>
 *
 * <p><b>放置原则：</b></p>
 * <ul>
 *   <li><b>契约接口</b>：{@link com.wsw.fitnesssystem.shared.kernel.error.ErrorCode}
 *       与 {@link com.wsw.fitnesssystem.shared.kernel.error.DomainErrorCode}，
 *       前者供应用层 / 接口层使用，后者供领域层使用</li>
 *   <li><b>通用错误码</b>：{@link com.wsw.fitnesssystem.shared.kernel.error.CommonErrorCode}，
 *       只放所有上下文都可能抛出的错误（参数校验、系统异常、文件操作、第三方接口等）</li>
 *   <li><b>不放模块专属错误码</b>：iam / user / fitness 等专属错误码各自放在
 *       {@code {module}.error} 包下</li>
 *   <li><b>不放业务逻辑</b>：本包不含任何业务实现、Spring Bean、工具方法</li>
 * </ul>
 *
 * <p><b>依赖方向（严格遵守）：</b></p>
 * <ul>
 *   <li>本包<b>不依赖</b>任何 {@code domain} / {@code application} /
 *       {@code infrastructure} / {@code interfaces} 包</li>
 *   <li>本包<b>不依赖</b>任何具体框架（Spring Security、MyBatis 等）</li>
 *   <li>本包<b>可被</b>所有上层包依赖</li>
 * </ul>
 *
 * <p><b>判断标准：</b>如果一个问题「任何上下文都可能遇到」，就属于本包；
 * 如果只属于某个模块，就放到那个模块的 {@code error} 包。</p>
 *
 * <p><b>示例：</b></p>
 * <ul>
 *   <li>✅ {@code common.param.invalid} —— 跨上下文的参数校验错误</li>
 *   <li>✅ {@code common.system.database_error} —— 跨上下文的系统错误</li>
 *   <li>❌ {@code iam.token.expired} —— iam 专属，应放 {@code iam.error}</li>
 *   <li>❌ {@code user.phone.already_exists} —— user 专属，应放 {@code user.error}</li>
 * </ul>
 *
 * <p><b>命名规范：</b>{@code common.{子域}.{错误}}，例如
 * {@code common.param.invalid}、{@code common.system.error}。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/21
 * @since 1.0
 */
package com.wsw.fitnesssystem.shared.kernel.error;