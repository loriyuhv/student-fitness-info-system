package com.wsw.fitnesssystem.shared.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.DataPermissionInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.wsw.fitnesssystem.shared.data_permission.CustomDataPermissionHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置类
 * <p>全局基础设施，所有模块共用</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/1 03:27
 * @since 1.0
 */
@Configuration
@RequiredArgsConstructor
public class MybatisPlusConfig {

    private final CustomDataPermissionHandler dataPermissionHandler;

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 乐观锁插件（支持 @Version 注解）
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

        // 数据权限拦截器（注意顺序：放在最后，确保其他插件先生效）
        interceptor.addInnerInterceptor(new DataPermissionInterceptor(dataPermissionHandler));

        return interceptor;
    }

}
