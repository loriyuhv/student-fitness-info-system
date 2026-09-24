package com.wsw.fitnesssystem.shared.infrastructure.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * <p>纯 JSON 序列化工具</p>
 *
 * <p>
 *     职责边界（重要）：
 *     <li>只负责 Java 对象 ⇄ JSON 字符串的转换</li>
 *     <li>不依赖 Redis / Caffeine / HTTP / MQ 等任何具体存储或传输技术</li>
 *     <li>可以被任意基础设施适配器复用</li>
 * </p>
 *
 * <p>
 *     设计初衷：
 *     <li>把"序列化"从"存储"中剥离出来，避免出现"名字像通用缓存、实际绑 Redis"的工具类</li>
 *     <li>序列化规则由全局 {@link com.fasterxml.jackson.databind.ObjectMapper} 统一决定</li>
 *     <li>跨语言场景下，生成的 JSON 不带 @class、不含 Java 特有字段</li>
 *     <li>统一异常出口，避免 JsonProcessingException 散落到各个适配器</li>
 * </p>
 *
 * <p>
 *     不做的事：
 *     <li>不负责 key 规范、TTL、缓存穿透等业务/存储语义</li>
 *     <li>不负责异常重试、降级、埋点，这些应由调用方按场景决定</li>
 *     <li>不感知"缓存"这个概念，它只是一个 JSON 工具</li>
 * </p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/24 07:46
 * @since 1.0
 */
@Component
@RequiredArgsConstructor
public class JsonSerializer {

    /**
     * <p>全局 ObjectMapper</p>
     *
     * <p>
     *     由 {@link com.wsw.fitnesssystem.shared.infrastructure.config.JacksonConfig} 提供，
     *     配置已包含 JavaTimeModule、ISO-8601 时间、忽略未知字段等跨语言友好设置。
     * </p>
     */
    private final ObjectMapper objectMapper;

    /**
     * <p>对象 → JSON 字符串</p>
     *
     * <p>
     *     失败时抛出 {@link JsonSerializationException}，由调用方决定如何处理：
     *     <li>缓存写入场景：通常应直接抛出，因为写不进缓存说明契约有问题</li>
     *     <li>日志输出场景：通常应降级为 toString，避免日志失败影响主流程</li>
     *     <li>MQ 发送场景：通常应抛出，让消息重试或进死信</li>
     *     </p>
     *
     * @param value 待序列化对象，不应为 null
     * @return JSON 字符串
     * @throws JsonSerializationException 序列化失败时抛出
     */
    public String toJson(Object value) {
        if (value == null) {
            throw new JsonSerializationException("待序列化对象为 null");
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new JsonSerializationException(
                "JSON 序列化失败, type=" + value.getClass().getName(), e);
        }
    }

    /**
     * <p>JSON 字符串 → 对象</p>
     *
     * <p>
     *     为什么必须显式传 Class：
     *     <li>因为 JSON 中不存 @class，Jackson 无法自动推断目标类型</li>
     *     <li>这是跨语言共享的代价：类型信息由调用方保证，而不是写在数据里</li>
     *     </p>
     *
     * @param json  JSON 字符串，不应为 null
     * @param clazz 目标类型
     * @param <T>   值类型
     * @return 反序列化后的对象
     * @throws JsonSerializationException 反序列化失败时抛出
     */
    public <T> T fromJson(String json, Class<T> clazz) {
        if (json == null) {
            throw new JsonSerializationException("待反序列化 JSON 为 null");
        }
        if (clazz == null) {
            throw new JsonSerializationException("目标类型为 null");
        }
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new JsonSerializationException(
                "JSON 反序列化失败, target=" + clazz.getName() + ", raw=" + json, e);
        }
    }

}
