package com.wsw.fitnesssystem.user.infrastructure.adapter;

import com.wsw.fitnesssystem.data_exchange.application.dto.command.UserImportCommand;
import com.wsw.fitnesssystem.data_exchange.application.dto.result.UserImportResult;
import com.wsw.fitnesssystem.data_exchange.application.port.output.UserProvisioningPort;
import com.wsw.fitnesssystem.shared.exception.BizException;
import com.wsw.fitnesssystem.user.application.service.impl.UserRegistrationAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 本地适配器（单体/模块化阶段使用）
 * <p>通过直接调用 User 模块的 {@link UserRegistrationAppService} 进行批量注册，
 * 将 Excel 导入数据转换为 User 模块的领域对象并持久化。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/1 12:04
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LocalUserProvisioningAdapter implements UserProvisioningPort {

    private final UserRegistrationAppService userRegistrationAppService;

    @Override
    public List<UserImportResult> batchRegister(List<UserImportCommand> userDataList) {
        log.info("Batch registering {} users via local adapter", userDataList.size());
        try {
            return userRegistrationAppService.batchRegister(userDataList);
        } catch (BizException e) {
            log.error("Batch registration failed", e);
            throw e;
        }
    }

}
