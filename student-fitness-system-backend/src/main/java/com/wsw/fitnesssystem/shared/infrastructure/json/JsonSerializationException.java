package com.wsw.fitnesssystem.shared.infrastructure.json;

import com.wsw.fitnesssystem.shared.kernel.error.CommonErrorCode;
import com.wsw.fitnesssystem.shared.kernel.exception.SystemException;

import java.io.Serial;

/**
 * JSON 序列化 / 反序列化异常。
 *
 * <p><b>语义：</b>Java 对象 ⇄ JSON 字符串转换失败，属于技术故障。
 * 继承 {@link SystemException}，自动被 {@code GlobalExceptionHandler.handleSystemException}
 * 处理，返回 HTTP 500 + {@code common.serialization.error}。</p>
 *
 * <p><b>为什么继承 SystemException？</b></p>
 * <ul>
 *   <li>继承 <b>精确类型</b>：需要时可用 {@code catch (JsonSerializationException)} 做特殊处理</li>
 *   <li>同时是 <b>系统异常</b>：上层适配器无需再包装，直接向上抛即可</li>
 *   <li>错误码明确：{@code common.serialization.error} 比 {@code common.system.error} 更易定位问题</li>
 * </ul>
 *
 * <p><b>设计初衷：</b>避免上层代码直接依赖 Jackson 的 {@code JsonProcessingException}，
 * 携带清晰的上下文（类型、原始 JSON），方便排查。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/24 07:59
 * @since 1.0
 */
public class JsonSerializationException extends SystemException {

    @Serial
    private static final long serialVersionUID = 1L;

    public JsonSerializationException(String message) {
        super(CommonErrorCode.SERIALIZATION_ERROR, message);
    }

    public JsonSerializationException(String message, Throwable cause) {
        super(CommonErrorCode.SERIALIZATION_ERROR, message, cause);
    }

}
