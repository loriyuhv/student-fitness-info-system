package com.wsw.fitnesssystem.user.infrastructure.adapter;

import com.wsw.fitnesssystem.handle_excel.core.model.UserImportData;
import com.wsw.fitnesssystem.handle_excel.core.model.UserImportResult;
import com.wsw.fitnesssystem.handle_excel.core.port.UserImportPort;
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
public class LocalUserImportAdapter implements UserImportPort {

    private final UserRegistrationAppService userRegistrationAppService;

    @Override
    public List<UserImportResult> batchRegister(List<UserImportData> userDataList) {
        log.info("Batch registering {} users via local adapter", userDataList.size());
        try {
            return userRegistrationAppService.batchRegister(userDataList);
        } catch (BizException e) {
            log.error("Batch registration failed", e);
            throw e;
        }
    }

}
