package com.wsw.fitnesssystem.iam.session.application.service.command;

import com.wsw.fitnesssystem.iam.audit.domain.valueobject.LogoutReason;
import com.wsw.fitnesssystem.iam.error.IamAuthNErrorCode;
import com.wsw.fitnesssystem.iam.session.application.dto.command.RevokeSessionCommand;
import com.wsw.fitnesssystem.iam.session.application.dto.result.RevokeSessionResult;
import com.wsw.fitnesssystem.iam.session.application.event.SessionTerminatedEvent;
import com.wsw.fitnesssystem.iam.session.application.port.output.AccountExistencePort;
import com.wsw.fitnesssystem.iam.session.domain.repository.SessionRepository;
import com.wsw.fitnesssystem.shared.kernel.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * 撤销会话用例入口（Application 层写服务）
 *
 * <p><b>职责：</b>
 * <ul>
 *   <li>处理管理员撤销指定用户全部在线会话的用例</li>
 *   <li>属于会话生命周期管理用例，归属 session 子域</li>
 * </ul>
 *
 * <p><b>流程：</b>
 * <ol>
 *   <li>校验目标用户是否存在（通过 {@link AccountExistencePort}，避免直接依赖 authentication 领域）</li>
 *   <li>移除目标用户所有在线会话，返回被移除的 AccessTokenId 集合</li>
 *   <li>为每个被撤销的 Token 发布 {@link SessionTerminatedEvent}（reason=KICK）</li>
 * </ol>
 *
 * <p><b>设计边界：</b>
 * <ul>
 *   <li>不做授权缓存清理，该副作用由订阅 {@link SessionTerminatedEvent} 的 authorization 子域处理</li>
 *   <li>不做调用者身份校验（谁在撤销），由接口层 {@code @PreAuthorize} 保证调用者权限</li>
 * </ul>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/21 15:45
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RevokeSessionCommandService {

    private final SessionRepository sessionRepository;
    private final AccountExistencePort accountExistencePort;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 撤销指定用户的所有在线会话
     *
     * @param command 撤销会话业务指令
     * @return 撤销结果
     * @throws BizException 目标用户不存在
     */
    public RevokeSessionResult revoke(RevokeSessionCommand command) {
        long campusId = command.campusId();
        long userId = command.userId();

        // 1. 校验目标用户存在性
        if (!accountExistencePort.exists(campusId, userId)) {
            throw new BizException(IamAuthNErrorCode.KICK_TARGET_NOT_FOUND);
        }

        // 2. 移除所有在线会话
        Set<String> revokedTokenIds = sessionRepository.removeAllSessions(campusId, userId);

        // 3. 发布会话终止事件（异步审计）
        if (!revokedTokenIds.isEmpty()) {
            for (String tokenId : revokedTokenIds) {
                eventPublisher.publishEvent(
                    new SessionTerminatedEvent(this, campusId, userId, tokenId, LogoutReason.KICK)
                );
            }
            log.info("Revoked {} sessions: campusId={}, userId={}",
                revokedTokenIds.size(), campusId, userId);
        } else {
            log.info("Revoke sessions but no online session found: campusId={}, userId={}",
                campusId, userId);
        }

        return new RevokeSessionResult(revokedTokenIds);
    }

}
