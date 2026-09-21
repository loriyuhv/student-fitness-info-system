package com.wsw.fitnesssystem.iam.authentication.application.service.command;

import com.wsw.fitnesssystem.iam.audit.domain.valueobject.LogoutReason;
import com.wsw.fitnesssystem.iam.authentication.application.event.SessionTerminatedEvent;
import com.wsw.fitnesssystem.iam.authentication.application.port.output.AuthorizationPort;
import com.wsw.fitnesssystem.iam.authentication.application.port.output.SessionPort;
import com.wsw.fitnesssystem.iam.authentication.domain.repository.AuthAccountRepository;
import com.wsw.fitnesssystem.iam.error.IamAuthNErrorCode;
import com.wsw.fitnesssystem.shared.kernel.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Set;

/**
 * 强制踢人用例入口（Application 层写服务）
 *
 * <p><b>职责：</b>
 * <ul>
 *   <li>处理管理员强制下线指定用户的所有会话</li>
 *   <li>与主动登出的区别：需要校验目标用户是否存在，需要清理权限缓存</li>
 * </ul>
 *
 * <p><b>流程：</b>
 * <ol>
 *   <li>校验目标用户是否存在（防止对不存在用户执行踢人）</li>
 *   <li>清除目标用户的权限缓存（避免后续请求命中旧权限）</li>
 *   <li>移除目标用户所有在线会话，返回被移除的 AccessTokenId 集合</li>
 *   <li>为每个被踢出的 Token 发布 {@link SessionTerminatedEvent}（reason=KICK）</li>
 * </ol>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/21 10:51
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionKickCommandService {

    private final SessionPort sessionPort;
    private final AuthorizationPort authorizationPort;
    private final AuthAccountRepository authAccountRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 管理员强制踢人
     *
     * @param campusId 校区ID
     * @param userId   目标用户ID
     * @return 被踢出的所有 AccessTokenId 集合
     */
    public Set<String> kick(long campusId, long userId) {
        // 1. 校验目标用户是否存在
        authAccountRepository.findByUserIdAndCampusId(userId, campusId)
            .orElseThrow(() -> new BizException(IamAuthNErrorCode.KICK_TARGET_NOT_FOUND));

        // 2. 清除权限缓存
        authorizationPort.removeAuthorization(userId, campusId);

        // 3. 移除所有在线会话
        Set<String> sessions = sessionPort.removeAllSessions(campusId, userId);

        // 4. 发布踢人事件
        if (!CollectionUtils.isEmpty(sessions)) {
            for (String tokenId : sessions) {
                eventPublisher.publishEvent(
                    new SessionTerminatedEvent(this, tokenId, LogoutReason.KICK));
            }
            log.info("Kicked {} sessions for user {} campus {}",
                sessions.size(), userId, campusId);
        } else {
            log.info("Kick user {}, campus {}: no online sessions", userId, campusId);
        }

        return sessions;
    }

}
