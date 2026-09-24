package com.wsw.fitnesssystem.iam.authentication.application.service.command;

import com.wsw.fitnesssystem.iam.authentication.application.dto.command.LoginCommand;
import com.wsw.fitnesssystem.iam.authentication.application.dto.result.LoginResult;
import com.wsw.fitnesssystem.iam.authentication.application.orchestrator.LoginOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 登录用例入口（Application 层写服务）
 *
 * <p><b>职责：</b>
 * <ul>
 *   <li>作为登录场景的 Application 层入口，接收 {@link LoginCommand}，返回 {@link LoginResult}</li>
 *   <li>仅做用例入口转发，不包含具体编排逻辑，编排细节委托给 {@link LoginOrchestrator}</li>
 *   <li>为 Web 层 / MQ 消费者 / 单元测试提供统一的登录调用入口</li>
 * </ul>
 *
 * <p><b>设计边界：</b>
 * <ul>
 *   <li>不直接依赖任何端口（Port），端口交互全部由 Orchestrator 承担</li>
 *   <li>不直接依赖仓储，不涉及领域规则</li>
 *   <li>不做 DTO 转换（协议转换在 Controller 层，Command → Result 在 Orchestrator 内完成）</li>
 * </ul>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/21 10:51
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginCommandService {

    private final LoginOrchestrator loginOrchestrator;

    /**
     * 用户登录
     *
     * @param command 登录业务指令
     * @return 登录结果（Token 对）
     */
    public LoginResult login(LoginCommand command) {
        log.debug("Login use case invoked: username={}, deviceType={}",
            command.username(), command.deviceType());
        return loginOrchestrator.execute(command);
    }

}
