package com.wsw.fitnesssystem.auth.audit.infrastructure.repository;

import com.wsw.fitnesssystem.auth.audit.AbstractAuditIntegrationTest;
import com.wsw.fitnesssystem.auth.audit.domain.model.LoginAudit;
import com.wsw.fitnesssystem.auth.audit.domain.port.LoginAuditRepository;
import com.wsw.fitnesssystem.auth.audit.domain.valueobject.DeviceInfo;
import com.wsw.fitnesssystem.auth.audit.domain.valueobject.IpAddress;
import com.wsw.fitnesssystem.auth.audit.domain.valueobject.LogoutReason;
import com.wsw.fitnesssystem.auth.audit.domain.valueobject.OnlineStatus;
import com.wsw.fitnesssystem.auth.audit.infrastructure.persistence.db.converter.LoginAuditConverter;
import com.wsw.fitnesssystem.auth.audit.infrastructure.persistence.db.entity.SysUserLogin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

/**
 * {@link LoginAuditRepositoryImpl} 集成测试
 *
 * <p>DisplayName：JUnit 5（Jupiter）专属注解，**给测试类 / 测试方法起一个可读性强的展示名称**，
 * 只会影响报告、控制台输出，**不改变任何测试逻辑、执行流程**。</p>
 * <p>使用：类或方法上</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/28 15:08
 * @since 1.0
 */
@DisplayName("登录审计仓储集成测试")
public class LoginAuditRepositoryImplTest extends AbstractAuditIntegrationTest {

    @Autowired
    private LoginAuditRepository auditRepository;

    private static final String TEST_USER = "repo-test-user";
    private static final String TOKEN_ID = "repo-token-123";
    private static final LocalDateTime EXPIRE_TIME = LocalDateTime.now().plusHours(2);

    @BeforeEach
    @AfterEach
    void cleanUp() {
        cleanTestData(TEST_USER);
    }

    @Test
    @DisplayName("1. 保存登录成功记录 → 数据库字段正确")
    void save_shouldInsertSuccessAudit() {
        // Given
        LoginAudit audit = createSuccessAudit();

        // When
        auditRepository.save(audit);

        // Then
        SysUserLogin entity = findLatestByUsername(TEST_USER);
        assertThat(entity).isNotNull();
        assertThat(entity.getUserId()).isEqualTo(100L);
        assertThat(entity.getUsername()).isEqualTo(TEST_USER);
        assertThat(entity.getLoginType()).isEqualTo(1);
        assertThat(entity.getTokenId()).isEqualTo(TOKEN_ID);
        assertThat(entity.getLoginIp()).isEqualTo("192.168.1.1");
        assertThat(entity.getStatus()).isEqualTo(1);
        assertThat(entity.getLoginId()).isNotNull();
    }

    @Test
    @DisplayName("2. 保存登录失败记录 → 数据库字段正确")
    void save_shouldInsertFailureAudit() {
        // Given
        LoginAudit audit = LoginAudit.recordFailure(
            TEST_USER,
            new IpAddress("192.168.1.1"),
            new DeviceInfo("WEB", "Mozilla/5.0"),
            "Password error"
        );

        // When
        auditRepository.save(audit);

        // Then
        SysUserLogin entity = findLatestByUsername(TEST_USER);
        assertThat(entity).isNotNull();
        assertThat(entity.getLoginType()).isEqualTo(0);
        assertThat(entity.getFailReason()).isEqualTo("Password error");
        assertThat(entity.getTokenId()).isNull();
        assertThat(entity.getStatus()).isEqualTo(0);
    }

    @Test
    @DisplayName("3. 通过 TokenId 查询在线记录 → 返回 LoginAudit")
    void findByTokenId_shouldReturnAuditWhenOnline() {
        // Given
        LoginAudit audit = createSuccessAudit();
        auditRepository.save(audit);

        // When
        Optional<LoginAudit> found = auditRepository.findByTokenId(TOKEN_ID);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getTokenId()).isEqualTo(TOKEN_ID);
        assertThat(found.get().isOnline()).isTrue();
        assertThat(found.get().getUserId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("4. 通过 TokenId 查询已下线记录 → 返回空")
    void findByTokenId_shouldReturnEmptyWhenOffline() {
        // Given：保存并下线
        LoginAudit audit = createSuccessAudit();
        auditRepository.save(audit);
        audit.terminate(LogoutReason.LOGOUT);
        auditRepository.update(audit);

        // When
        Optional<LoginAudit> found = auditRepository.findByTokenId(TOKEN_ID);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("5. 通过 TokenId 查询不存在记录 → 返回空")
    void findByTokenId_shouldReturnEmptyWhenNotExists() {
        // When
        Optional<LoginAudit> found = auditRepository.findByTokenId("non-existent");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("6. 更新在线记录 → 使用主键更新成功")
    void update_shouldUpdateByIdWhenIdExists() {
        // Given：先保存
        LoginAudit audit = createSuccessAudit();
        auditRepository.save(audit);

        Optional<LoginAudit> saved = auditRepository.findByTokenId(TOKEN_ID);
        assertThat(saved).isPresent();
        LoginAudit savedAudit = saved.get();

        // When：修改并更新
        savedAudit.terminate(LogoutReason.KICK);
        auditRepository.update(savedAudit);

        // Then
        SysUserLogin entity = findLatestByUsername(TEST_USER);
        assertThat(entity.getStatus()).isEqualTo(0);
        assertThat(entity.getLogoutReason()).isEqualTo(LogoutReason.KICK.name());
        assertThat(entity.getLogoutTime()).isNotNull();
    }

    @Test
    @DisplayName("7. Converter 恢复已下线记录 → 不抛异常")
    void converter_shouldRestoreOfflineRecordWithoutException() {
        // 1. 保存并下线
        LoginAudit audit = createSuccessAudit();
        auditRepository.save(audit);
        audit.terminate(LogoutReason.KICK);
        auditRepository.update(audit);

        // 2. 直接从 Mapper 查询
        SysUserLogin entity = sysUserLoginMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUserLogin>()
                .eq(SysUserLogin::getTokenId, TOKEN_ID)
        );
        assertThat(entity).isNotNull();

        // 3. Converter 转换
        assertThatNoException()
            .describedAs("已下线记录恢复不应抛异常")
            .isThrownBy(() -> {
                LoginAudit restored = LoginAuditConverter.toDomain(entity);
                assertThat(restored).isNotNull();
                assertThat(restored.getStatus()).isEqualTo(OnlineStatus.OFFLINE);
                assertThat(restored.getLogoutReason()).isEqualTo(LogoutReason.KICK);
            });
    }

    private LoginAudit createSuccessAudit() {
        return LoginAudit.recordSuccess(
            100L,
            TEST_USER,
            TOKEN_ID,
            EXPIRE_TIME,
            new DeviceInfo("WEB", "Mozilla/5.0"),
            new IpAddress("192.168.1.1")
        );
    }

}
