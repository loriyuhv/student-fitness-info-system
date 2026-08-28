package com.wsw.fitnesssystem.auth.audit;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wsw.fitnesssystem.auth.audit.infrastructure.persistence.db.entity.SysUserLogin;
import com.wsw.fitnesssystem.auth.audit.infrastructure.persistence.db.mapper.SysUserLoginMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Audit 模块集成测试基类
 *
 * <p>使用 Testcontainers 2.0.5 + MySQL 8.4
 * <p>ActiveProfiles：指定当前测试类，激活 `test` 这个 Spring 配置环境</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/28 14:55
 * @since 1.0
 */
@Slf4j
@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class AbstractAuditIntegrationTest {

    // ==================== Testcontainers 配置 ====================

    /**
     * Container: Testcontainers注解
     * 1. 标记该字段是一个Docker容器
     * 2. static: 容器只会启动【一次】，所有测试用例共用同一个MySQL容器，速度更快
     * 3. final: 容器引用不可修改
     */
    @Container
    static final MySQLContainer MYSQL = new MySQLContainer(DockerImageName
        .parse("mysql:8.4") // 指定拉取的docker镜像：mysql 8.4版本
        .asCompatibleSubstituteFor("mysql") // 声明这个镜像等价于官方mysql镜像，解决版本兼容校验警告
    )
        .withDatabaseName("fitness_test") // 创建出来的测试库名称：fitness_test
        .withUsername("test") // 数据库用户名
        .withPassword("test") // 数据库密码
        .withReuse(true);  // 开启容器重用：测试跑完不销毁容器，下次运行直接复用，省去反复启动mysql耗时

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");

        // MyBatis-Plus 逻辑删除配置（确保测试与生产一致）
        registry.add("mybatis-plus.global-config.db-config.logic-delete-field", () -> "deleted");
        registry.add("mybatis-plus.global-config.db-config.logic-delete-value", () -> "1");
        registry.add("mybatis-plus.global-config.db-config.logic-not-delete-value", () -> "0");

        // 禁用 Flyway
        registry.add("spring.flyway.enabled", () -> "false");

        // 减少日志噪音
        registry.add("logging.level.org.testcontainers", () -> "WARN");
    }

    @Autowired
    protected SysUserLoginMapper sysUserLoginMapper;

    /**
     * 清理指定用户的测试数据
     */
    protected void cleanTestData(String username) {
        sysUserLoginMapper.delete(
            new LambdaQueryWrapper<SysUserLogin>().eq(SysUserLogin::getUsername, username)
        );
    }

    /**
     * 获取最新的一条记录
     */
    protected SysUserLogin findLatestByUsername(String username) {

        SysUserLogin result = sysUserLoginMapper.selectOne(
            new LambdaQueryWrapper<SysUserLogin>()
                .eq(SysUserLogin::getUsername, username)
                .orderByDesc(SysUserLogin::getLoginId)
                .last("LIMIT 1")
        );

        log.info("user result: {}", result);
        return result;
    }

}
