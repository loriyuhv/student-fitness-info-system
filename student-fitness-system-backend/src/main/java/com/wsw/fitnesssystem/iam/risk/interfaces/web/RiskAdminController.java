package com.wsw.fitnesssystem.iam.risk.interfaces.web;

import com.wsw.fitnesssystem.iam.risk.application.port.input.RiskControlUseCase;
import com.wsw.fitnesssystem.iam.risk.domain.vb.RiskSubject;
import com.wsw.fitnesssystem.shared.application.context.RequestContextHolder;
import com.wsw.fitnesssystem.shared.domain.vb.Operator;
import com.wsw.fitnesssystem.shared.interfaces.web.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 风控管理 - Web 入站适配器
 *
 * <p><b>职责边界：</b>
 * <ul>
 *   <li>接收管理员对风控状态的管理请求</li>
 *   <li>将 HTTP 参数转换为领域值对象 {@link RiskSubject}</li>
 *   <li>调用入站端口 {@link RiskControlUseCase}，不直接访问仓储</li>
 * </ul>
 * 本类<b>不承载业务规则</b>，仅做参数转换与用例编排。</p>
 *
 * <p><b>为什么放在 risk 子域？</b></p>
 * 解锁的本质是「修改风控状态」，风控状态的所有者是 risk 子域。
 * authentication 子域只在认证流程中编排调用本用例，不拥有这份状态。
 * 若放到 authentication，会引入跨子域的反向依赖与语义错位。</p>
 *
 * <p><b>依赖方向：</b>
 * {@code interfaces → application → domain ← infrastructure}，
 * 本类只依赖应用层入站端口 {@link RiskControlUseCase}，
 * 不触碰领域仓储、Redis 等内部实现。</p>
 *
 * <p><b>权限：</b>管理端接口，需具备 {@code ROLE_ADMIN} 角色。
 * 与认证接口（登录/刷新）的 {@code permitAll} 策略不同，
 * 由 {@code @PreAuthorize} + RBAC 拦截，未授权时由
 * {@code JwtAccessDeniedHandler} 返回 403。</p>
 *
 * <p><b>审计：</b>解锁动作需要留痕，本类负责记录「谁在何时解了谁」的
 * 操作日志；若需严格审计（入库、可追溯），可在
 * {@code RiskControlAppService.unlock} 中扩展审计写入。</p>
 *
 * <p><b>HTTP 方法选择：</b>采用 {@code DELETE}，理由是解锁的本质是
 * <b>删除锁定记录</b>（{@code fail} + {@code lock} 两个 Redis Key），
 * 符合 HTTP DELETE 的幂等语义——重复调用结果一致，无副作用。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/26 12:01
 * @since 1.0
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/iam/risk")
public class RiskAdminController {

    private final RiskControlUseCase riskControlUseCase;

    /**
     * 管理员手动解封账号风控状态。
     *
     * <p><b>幂等：</b>目标状态为「未锁定」。
     * 若账号当前无风控记录（从未失败、已被自动解锁或已手动解锁），
     * 删除操作无副作用，接口仍返回成功。</p>
     *
     * <p><b>参数校验：</b>{@code username} 由
     * {@link UnlockUserRequest} 的 {@code @NotBlank} 校验，
     * 空或空白时抛出 {@code MethodArgumentNotValidException}，
     * 由全局异常处理器转换为 400 响应。</p>
     *
     * <p><b>权限校验：</b>{@code @PreAuthorize} 在方法调用前拦截，
     * 无 {@code ROLE_ADMIN} 角色时抛 {@code AccessDeniedException}，
     * 由 {@code JwtAccessDeniedHandler} 返回 403。</p>
     *
     * <p><b>路径设计：</b>
     * <ul>
     *   <li>当前：{@code DELETE /iam/risk/users/unlock}</li>
     *   <li>未来支持 IP / 设备维度时可扩展为
     *       {@code DELETE /iam/risk/{dimension}/unlock}，
     *       请求体增加 {@code dimension} 字段</li>
     * </ul></p>
     *
     * @param request 解锁用户请求体，{@code username} 非空白
     * @return 统一响应包装，成功时 data 为空
     */
    @DeleteMapping("/users/unlock")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ApiResponse<Void> unlockUser(
        @RequestBody @Valid UnlockUserRequest request) {
        String username = request.username();
        log.info("Admin unlock invoked: username={}, operator={}", username, currentOperatorName());
        riskControlUseCase.unlock(RiskSubject.user(username));
        return ApiResponse.success();
    }

    /**
     * 获取当前操作者用户名，用于审计日志。
     *
     * @return 操作者用户名，无法获取时返回 {@code "unknown"}
     */
    private String currentOperatorName() {
        Operator operator = RequestContextHolder.getOperator();
        if (operator == null) {
            return "unknown";
        }
        return operator.username();
    }

}
