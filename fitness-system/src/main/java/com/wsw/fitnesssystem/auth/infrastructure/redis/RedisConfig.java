package com.wsw.fitnesssystem.auth.infrastructure.redis;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wsw.fitnesssystem.auth.application.authorization.dto.UserAuthorization;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @author loriyuhv
 * @version 1.0 2026/1/15 16:00
 * @since 1.0
 */
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        // 设置 key 的序列化器
        template.setKeySerializer(new StringRedisSerializer());
        // 设置 value 的序列化器
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        // 设置 hash key 的序列化器
        template.setHashKeySerializer(new StringRedisSerializer());
        // 设置 hash value 的序列化器
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }

    /**
     * <p>【新增】专门用于 UserAuthorization 的 RedisTemplate</p>
     *
     * <p>
     *     设计目的：
     *     <li>为权限缓存提供专用的 Redis 操作模板</li>
     *     <li>使用纯 JSON 格式存储，不写入 Java 类名（@class），节省空间且跨语言兼容</li>
     *     <li>与通用的 RedisTemplate 隔离，避免配置冲突</li>
     * </p>
     *
     * <p>
     *     存储格式对比：
     *     <li>GenericJackson2JsonRedisSerializer: {"@class":"com.xxx.UserAuthorization","userId":2,...}</li>
     *     <li>本配置（Jackson2JsonRedisSerializer）: {"userId":2,"roles":["TEACHER"],...}  ← 更简洁</li>
     * </p>
     * @param factory Redis 连接工厂，由 Spring Boot 自动配置注入
     * @return 配置好的专用 RedisTemplate
     */
    @Bean(name = "userAuthRedisTemplate")
    public RedisTemplate<String, UserAuthorization> userAuthRedisTemplate(
        RedisConnectionFactory factory) {

        // 1. 创建模板实例
        /* 泛型 <String, UserAuthorization> 表示：
        * - Key 的类型是 String（Redis 的键，如 "auth:user:1:2"）
        * - Value 的类型是 UserAuthorization（Redis 的值，权限对象）
        * */
        RedisTemplate<String, UserAuthorization> template = new RedisTemplate<>();

        // 2. 设置连接工厂：连接工厂负责创建与 Redis 服务器的连接（包括连接池管理）
        template.setConnectionFactory(factory);

        // ========================================
        // 3. 配置 Key 的序列化（String → 字节数组）
        // ========================================

        /*
        * StringRedisSerializer: 将 Java String 按 UTF-8 编码为字节
        * 结果：Redis 中的 Key 是可读的字符串，如 "auth:user:1:2"
        * 对比：如果用默认的 JdkSerializationRedisSerializer，Key 会变成二进制乱码
         */
        template.setKeySerializer(new StringRedisSerializer());

        // ========================================
        // 4. 配置 Value 的序列化（UserAuthorization → JSON 字符串 → 字节数组）
        // ========================================

        // 4.1 创建 Jackson 的 ObjectMapper，控制 JSON 的生成规则
        ObjectMapper mapper = new ObjectMapper();

        // 4.2 忽略值为 null 的字段，节省存储空间
        // 例如：permissions 为 null 时，JSON 中不包含该字段，而不是 "permissions":null
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        // 4.3 【关键】禁用 Jackson 的类型信息功能
        // 默认情况下，Jackson 会在 JSON 中写入 "@class": "完整类名"，用于反序列化时自动推断类型
        // 这里禁用后，JSON 更简洁，且不与具体 Java 类绑定（跨语言、重构类名安全）
        // 代价：反序列化时必须明确指定目标类型（通过 Jackson2JsonRedisSerializer 的构造参数）
        mapper.deactivateDefaultTyping();

        // 4.4 创建 Jackson2JsonRedisSerializer，专门处理 UserAuthorization 类型
        // 构造参数 将配置好的 ObjectMapper 应用到序列化器, 实现"存时无类型信息，读时知道目标类型"
        // 构造参数 mapper UserAuthorization.class 告诉 Jackson：反序列化时创建这个类的实例
        Jackson2JsonRedisSerializer<UserAuthorization> serializer =
            new Jackson2JsonRedisSerializer<>(mapper, UserAuthorization.class);

        // 4.4 将序列化器设置到模板
        // 此后，所有通过此模板存入的 UserAuthorization 都会转为纯 JSON
        template.setValueSerializer(serializer);

        // ========================================
        // 5. 配置 Hash 结构的序列化（备用）
        // ========================================

        // Hash 是 Redis 的一种数据结构：{ "field1": "value1", "field2": "value2" }
        // 虽然当前主要用 String 结构（opsForValue），但统一配置以防万一

        // Hash 的 Key（field）也用可读字符串
        template.setHashKeySerializer(new StringRedisSerializer());

        // Hash 的 Value 同样用 JSON 格式
        template.setHashValueSerializer(serializer);

        // ========================================
        // 6. 初始化模板
        // ========================================

        // afterPropertiesSet() 是 Spring 的初始化方法
        // 执行必要的校验和准备工作，必须调用，否则序列化器可能不生效

        template.afterPropertiesSet();return template;
    }
}
