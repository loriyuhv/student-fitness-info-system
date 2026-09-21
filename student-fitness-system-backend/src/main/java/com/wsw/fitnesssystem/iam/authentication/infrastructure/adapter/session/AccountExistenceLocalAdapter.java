package com.wsw.fitnesssystem.iam.authentication.infrastructure.adapter.session;

import com.wsw.fitnesssystem.iam.authentication.domain.repository.AuthAccountRepository;
import com.wsw.fitnesssystem.iam.session.application.port.output.AccountExistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * {@link AccountExistencePort} 本地实现
 *
 * <p><b>职责：</b>
 * <ul>
 *   <li>由 authentication 实现，供 session 模块校验账号存在性</li>
 *   <li>把"用户是否存在"的领域查询能力以端口形式对外暴露</li>
 * </ul>
 *
 * <p><b>依赖方向：</b>
 * <pre>
 *   session.application.port.output.AccountExistencePort   ←  定义契约
 *   authentication.infrastructure.adapter.session          ←  实现
 * </pre>
 * 符合依赖反转：session 只依赖自身定义的端口，authentication 负责实现。</p>
 *
 * <p><b>包路径说明：</b>
 * 本类位于 {@code adapter.session} 下，语义为"authentication 为 session 子域提供的适配器"，
 * 与 {@code adapter.user} 下"为 user 子域提供的适配器"区分开。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/21 15:52
 * @since 1.0
 */
@Component
@RequiredArgsConstructor
public class AccountExistenceLocalAdapter implements AccountExistencePort {

    private final AuthAccountRepository authAccountRepository;

    @Override
    public boolean exists(long campusId, long userId) {
        return authAccountRepository.findByUserIdAndCampusId(userId, campusId).isPresent();
    }

}
