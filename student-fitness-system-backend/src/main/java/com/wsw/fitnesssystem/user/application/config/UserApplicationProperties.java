package com.wsw.fitnesssystem.user.application.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * User 模块应用层配置。
 *
 * <p><b>职责：</b>承载本模块的业务策略参数（与具体技术实现无关）。</p>
 * <p><b>不承载：</b>文件限制、Redis TTL 等技术参数（应归入基础设施层配置）。</p>
 *
 * <p><b>配置前缀：</b>{@code user.application}</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/14 17:36
 * @since 1.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "user.application")
public class UserApplicationProperties {

    /** 用户导入相关业务策略 */
    private DataImport dataImport = new DataImport();

    @Data
    public static class DataImport {

        /**
         * 导入数据中日期字段的解析格式。
         * <p>用于 {@code UserRegisterServiceImpl#parseDate(String)}。</p>
         */
        private String dateFormat = "yyyy-MM-dd";
    }

}
