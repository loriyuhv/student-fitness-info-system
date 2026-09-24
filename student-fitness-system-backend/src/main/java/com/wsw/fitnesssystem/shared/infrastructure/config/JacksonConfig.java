package com.wsw.fitnesssystem.shared.infrastructure.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * <p>全局 Jackson 配置</p>
 *
 * <p>
 * 设计目标：
 * <li>为整个项目提供统一、跨语言友好的 ObjectMapper</li>
 * <li>供 {@link com.wsw.fitnesssystem.shared.infrastructure.json.JsonSerializer} 使用</li>
 * <li>也可作为 Spring MVC 的 JSON 序列化基础</li>
 * </p>
 *
 * <p>
 * 关键约定：
 * <li>不启用 default typing：JSON 中不写 @class，保证跨语言契约干净</li>
 * <li>时间统一为 ISO-8601 字符串，便于 Python / Go 解析</li>
 * <li>忽略未知字段：其他语言写入新字段时，Java 旧版本也能读</li>
 * <li>null 字段不写入：节省空间、契约更干净</li>
 * </p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/24 08:00
 * @since 1.0
 */
@Configuration
public class JacksonConfig {

    /**
     * <p>全局 ObjectMapper</p>
     *
     * <p>
     * 说明：
     * <li>{@code @Primary} 保证项目中其他组件默认注入到这个实例</li>
     * <li>新建的 ObjectMapper 默认没有启用 default typing，这里显式调用
     * {@code deactivateDefaultTyping()} 是为了表达"禁用 @class"的意图，
     * 避免后来者不小心开启
     * </li>
     * </p>
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // 1. null 字段不参与序列化，跨语言下数据更干净
        mapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);

        // 2. 支持 Java 8 时间类型（LocalDateTime / LocalDate / LocalTime 等）
        mapper.registerModule(new JavaTimeModule());

        // 3. 时间不使用时间戳数组，改用 ISO-8601 字符串，跨语言友好
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 4. 忽略 JSON 中的未知字段，便于跨语言契约演进
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // 5. 显式禁用多态类型信息，保证 JSON 中不出现 "@class"
        mapper.deactivateDefaultTyping();

        return mapper;
    }

}
