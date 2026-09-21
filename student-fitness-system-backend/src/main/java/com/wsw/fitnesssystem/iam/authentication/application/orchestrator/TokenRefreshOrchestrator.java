package com.wsw.fitnesssystem.iam.authentication.application.orchestrator;

import com.wsw.fitnesssystem.iam.authentication.application.dto.command.RefreshCommand;
import com.wsw.fitnesssystem.iam.authentication.application.dto.result.RefreshResult;
import com.wsw.fitnesssystem.iam.authentication.application.event.TokenRefreshedEvent;
import com.wsw.fitnesssystem.iam.authentication.application.port.output.SessionPort;
import com.wsw.fitnesssystem.iam.authentication.application.port.output.TokenPort;
import com.wsw.fitnesssystem.iam.authentication.application.port.output.dto.RefreshTokenClaims;
import com.wsw.fitnesssystem.iam.authentication.application.port.output.dto.TokenPair;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 令牌刷新流程编排器
 *
 * <p><b>职责：</b>
 * <ul>
 *   <li>串联刷新流程：解析 RefreshToken → 获取旧 AccessTokenId → 生成新 Token → 原子轮换 → 发布事件</li>
 *   <li>负责新旧令牌 ID 的传递和事件组装</li>
 * </ul>
 *
 * <p><b>编排步骤：</b>
 * <ol>
 *   <li>解析 RefreshToken 得到 claims</li>
 *   <li>通过 refreshTokenId 反查旧 accessTokenId（在轮换前记录，用于事件与审计）</li>
 *   <li>生成新的 Token 对（新 accessTokenId / refreshTokenId）</li>
 *   <li>会话原子轮换（旧 RefreshToken 校验、旧会话失效、新会话写入由适配器保证原子性）</li>
 *   <li>发布 {@link TokenRefreshedEvent} 供异步审计使用</li>
 * </ol>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/21 10:53
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenRefreshOrchestrator {

    private final TokenPort tokenPort;
    private final SessionPort sessionPort;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 执行令牌刷新编排
     *
     * @param command 刷新业务指令
     * @return 新的令牌对
     */
    public RefreshResult execute(RefreshCommand command) {
        // 1. 解析 RefreshToken
        RefreshTokenClaims claims = tokenPort.parseRefreshToken(command.refreshToken());

        long campusId = claims.getCampusId();
        long userId = claims.getUserId();
        String oldRefreshTokenId = claims.getJti();

        // 2. 反查旧 AccessTokenId（在轮换前记录，轮换后旧索引会被清除）
        String oldAccessTokenId = sessionPort
            .getAccessTokenIdByRefreshTokenId(campusId, userId, oldRefreshTokenId);

        // 3. 生成新的 Token 对
        String newAccessTokenId = UUID.randomUUID().toString();
        String newRefreshTokenId = UUID.randomUUID().toString();
        long tokenVersion = sessionPort.getTokenVersion(campusId, userId);

        TokenPair tokenPair = tokenPort.generate(
            campusId, userId, claims.getUsername(), claims.getUserType(),
            claims.getDeviceId(), tokenVersion, newAccessTokenId, newRefreshTokenId
        );

        // 4. 会话原子轮换（适配器内部校验旧 RefreshToken 是否仍存在）
        sessionPort.rotateRefreshToken(
            campusId, userId,
            oldRefreshTokenId, oldAccessTokenId,
            newRefreshTokenId, newAccessTokenId
        );

        // 5. 发布刷新事件（异步审计）
        eventPublisher.publishEvent(new TokenRefreshedEvent(
            this, userId, campusId, oldAccessTokenId, newAccessTokenId,
            newRefreshTokenId, tokenPair.getAccessTokenExpiresIn(),
            command.deviceType(), command.userAgent(), command.ip()
        ));

        log.info("Token refreshed: userId={}, campusId={}, oldAccessTokenId={}, newAccessTokenId={}",
            userId, campusId, oldAccessTokenId, newAccessTokenId);

        return new RefreshResult(
            tokenPair.getAccessToken(),
            tokenPair.getRefreshToken(),
            tokenPair.getAccessTokenExpiresIn()
        );
    }

}
