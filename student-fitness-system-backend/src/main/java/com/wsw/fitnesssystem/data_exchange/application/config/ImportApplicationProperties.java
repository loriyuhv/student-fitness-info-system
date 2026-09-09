package com.wsw.fitnesssystem.data_exchange.application.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 导入模块应用层配置。
 * <p>
 * 包含业务策略、校验规则、展示策略等应用层配置项。
 * 配置前缀：{@code data-exchange.application}
 * </p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/9 12:41
 * @since 1.0
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "data-exchange.application")
public class ImportApplicationProperties {

    /** 文件限制配置 */
    private FileConfig file = new FileConfig();

    /** 批量处理策略 */
    private BatchConfig batch = new BatchConfig();

    /** 校验规则 */
    private ValidationConfig validation = new ValidationConfig();

    /** 展示策略 */
    private DisplayConfig display = new DisplayConfig();


    @Getter
    @Setter
    public static class FileConfig {
        /** 单文件最大大小（字符串格式，如 200MB） */
        private String maxSize = "200MB";

        /** 支持的文件扩展名 */
        private List<String> allowedExtensions = new ArrayList<>(List.of(".xlsx", ".xls"));

        /**
         * 获取最大文件大小（字节）
         */
        public long getMaxSizeInBytes() {
            return parseSize(maxSize);
        }

        private long parseSize(String size) {
            if (size == null || size.isBlank()) {
                return 200 * 1024 * 1024L;
            }
            size = size.trim().toUpperCase();
            long multiplier = 1;
            String numStr = size;
            String substring = size.substring(0, size.length() - 2);
            if (size.endsWith("KB")) {
                multiplier = 1024;
                numStr = substring;
            } else if (size.endsWith("MB")) {
                multiplier = 1024 * 1024;
                numStr = substring;
            } else if (size.endsWith("GB")) {
                multiplier = 1024 * 1024 * 1024;
                numStr = substring;
            }
            try {
                return Long.parseLong(numStr.trim()) * multiplier;
            } catch (NumberFormatException e) {
                return 200 * 1024 * 1024L;
            }
        }
    }

    @Getter
    @Setter
    public static class BatchConfig {
        /** 默认每批处理条数 */
        private int defaultSize = 500;

        /** 小文件阈值：< 此值走全量解析，≥ 此值走流式解析 */
        private int streamThreshold = 10000;
    }

    @Getter
    @Setter
    public static class ValidationConfig {
        /** 用户名最大长度 */
        private int usernameMaxLength = 50;

        /** 密码最小长度 */
        private int passwordMinLength = 6;

        /** 昵称最大长度 */
        private int nicknameMaxLength = 20;
    }

    @Getter
    @Setter
    public static class DisplayConfig {
        /** 错误摘要保留条数（前端展示） */
        private int errorMsgMaxCount = 5;
    }

}
