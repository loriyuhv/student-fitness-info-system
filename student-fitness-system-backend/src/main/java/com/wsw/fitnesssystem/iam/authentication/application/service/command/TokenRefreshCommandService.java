package com.wsw.fitnesssystem.iam.authentication.application.service.command;

import com.wsw.fitnesssystem.iam.authentication.application.dto.command.RefreshCommand;
import com.wsw.fitnesssystem.iam.authentication.application.dto.result.RefreshResult;
import com.wsw.fitnesssystem.iam.authentication.application.orchestrator.TokenRefreshOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 令牌刷新用例入口（Application 层写服务）
 *
 * <p><b>职责：</b>
 * <ul>
 *   <li>作为刷新令牌场景的 Application 层入口，接收 {@link RefreshCommand}，返回 {@link RefreshResult}</li>
 *   <li>仅做用例入口转发，编排细节委托给 {@link TokenRefreshOrchestrator}</li>
 * </ul>
 *
 * <p><b>设计边界：</b>
 * <ul>
 *   <li>不直接依赖端口，不参与编排</li>
 *   <li>不做 DTO 转换</li>
 * </ul>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/21 10:51
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenRefreshCommandService {

    private final TokenRefreshOrchestrator tokenRefreshOrchestrator;

    /**
     * 刷新 Access Token / Refresh Token
     *
     * @param command 刷新业务指令
     * @return 新的令牌对
     */
    public RefreshResult refresh(RefreshCommand command) {
        log.info("Token refresh use case invoked: deviceType={}", command.deviceType());
        return tokenRefreshOrchestrator.execute(command);
    }

}
