/**
 * 共享内核 —— 跨层异常契约。
 *
 * <p><b>本包定位：</b>被 Application / Infrastructure / Interfaces 三层
 * 共同引用的异常契约。这些异常是「跨层载体」，不是任何一层的私有物。</p>
 *
 * <p><b>与其它异常包的区别：</b></p>
 * <ul>
 *   <li>{@code shared.kernel.error}：错误码契约</li>
 *   <li>{@code shared.domain.exception}：领域异常（只被领域层抛）</li>
 *   <li>{@code {module}.application.exception}：模块应用层专属异常（如控制流信号）</li>
 * </ul>
 *
 * <p><b>异常类型：</b></p>
 * <ul>
 *   <li>{@link com.wsw.fitnesssystem.shared.kernel.exception.BizException}
 *       —— 业务规则不满足（4xx）</li>
 *   <li>{@link com.wsw.fitnesssystem.shared.kernel.exception.SystemException}
 *       —— 技术系统故障（5xx）</li>
 * </ul>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/21
 * @since 1.0
 */
package com.wsw.fitnesssystem.shared.kernel.exception;