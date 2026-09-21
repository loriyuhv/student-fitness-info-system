package com.wsw.fitnesssystem.iam.authentication.infrastructure.adapter.user;

import com.wsw.fitnesssystem.iam.authentication.domain.model.AuthAccount;
import com.wsw.fitnesssystem.iam.authentication.domain.repository.AuthAccountRepository;
import com.wsw.fitnesssystem.iam.authentication.domain.enums.UserSource;
import com.wsw.fitnesssystem.iam.authentication.domain.enums.UserType;
import com.wsw.fitnesssystem.user.application.dto.command.UserAccountProvisionCommand;
import com.wsw.fitnesssystem.user.application.dto.result.UserAccountProvisionResult;
import com.wsw.fitnesssystem.user.application.port.output.UserAccountProvisioningPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * UserAccountProvisioningPort 本地实现
 *
 * <p>由 authentication 实现，供 user 模块创建认证账号（导入/注册场景）。
 * <p>使用 MANDATORY 事务传播：强制调用方已开启事务，保证 sys_user + user_profile 同事务。
 *
 * @author loriyuhv
 * @version 1.0 2026/9/16 15:56
 * @since 1.0
 */
@Component
@RequiredArgsConstructor
public class UserAccountProvisioningLocalAdapter implements UserAccountProvisioningPort {

    private final AuthAccountRepository repository;

    @Override
    @Transactional(propagation = Propagation.MANDATORY, rollbackFor = Exception.class)
    public UserAccountProvisionResult createAccount(UserAccountProvisionCommand command) {
        AuthAccount account = AuthAccount.create(
            command.getCampusId(),
            command.getUsername(),
            command.getPasswordHash(),
            UserType.of(command.getUserType()),
            UserSource.of(command.getSource()),
            command.getOperatorId()
        );
        AuthAccount saved = repository.save(account);
        return UserAccountProvisionResult.builder()
            .userId(saved.getUserId())
            .username(saved.getUsername())
            .status(saved.getStatus().getCode())
            .build();
    }

}
