package com.wsw.fitnesssystem.user.infrastructure.adapter;

import com.wsw.fitnesssystem.data_exchange.application.dto.command.UserImportCommand;
import com.wsw.fitnesssystem.data_exchange.application.dto.result.UserImportResult;
import com.wsw.fitnesssystem.data_exchange.application.port.output.UserProvisioningPort;
import com.wsw.fitnesssystem.shared.exception.BizException;
import com.wsw.fitnesssystem.user.application.service.impl.UserRegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 用户导入端口本地适配器（单体/模块化阶段）。
 * <p>
 * 实现 {@link UserProvisioningPort} 端口，通过本地调用 {@link UserRegistrationService}
 * 完成用户批量导入。
 * </p>
 * <p>
 * <b>演进说明：</b>
 * <ul>
 *   <li>当前阶段：单体架构，直接本地调用 User 模块的 Application Service</li>
 *   <li>微服务阶段：替换为 {@code UserProvisioningFeignAdapter}，通过 HTTP 远程调用</li>
 * </ul>
 * </p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/1 12:04
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserProvisioningLocalAdapter implements UserProvisioningPort {

    private final UserRegistrationService userRegistrationService;

    @Override
    public List<UserImportResult> importUsers(List<UserImportCommand> commands) {
        log.info("Batch registering {} users via local adapter", commands.size());
        try {
            return userRegistrationService.batchRegister(commands);
        } catch (BizException e) {
            log.error("Batch registration failed", e);
            throw e;
        }
    }

}
