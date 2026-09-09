package com.wsw.fitnesssystem.data_exchange.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 导入模块基础设施层配置。
 * <p>
 * 包含文件限制、缓存策略、锁策略、限流策略等技术配置项。
 * 配置前缀：{@code data-exchange.infrastructure}
 * </p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/9 12:43
 * @since 1.0
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "data-exchange.infrastructure")
public class ImportInfrastructureProperties {

    /** Redis 缓存配置 */
    private RedisConfig redis = new RedisConfig();

    /** 限流配置 */
    private RateLimitConfig rateLimit = new RateLimitConfig();

    /** 临时文件配置 */
    private TempFileConfig tempFile = new TempFileConfig();

    @Getter
    @Setter
    public static class RedisConfig {
        /** 导入任务进度 TTL（小时） */
        private long taskTtlHours = 24;

        /** 文件锁 TTL（分钟） */
        private long lockTtlMinutes = 60;

        /** 错误信息最大长度（存入 Redis 时截断） */
        private int errorMsgMaxLength = 500;

        /** 错误摘要保留条数 */
        private int errorMsgMaxCount = 5;
    }

    @Getter
    @Setter
    public static class RateLimitConfig {
        /** 限流时间窗口（秒） */
        private int windowSeconds = 60;

        /** 窗口内最大提交次数 */
        private int maxCount = 5;
    }

    @Getter
    @Setter
    public static class TempFileConfig {
        /** 临时文件根目录（相对于 java.io.tmpdir） */
        private String rootDir = "import";

        /** 临时文件名 */
        private String fileName = "data.xlsx";
    }

}
