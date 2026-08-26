package com.wsw.fitnesssystem.auth.authentication.interfaces.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求 DTO（Web 层面向 HTTP 协议的输入模型）
 *
 * <p><b>职责：</b>
 * <ul>
 *   <li>接收并校验 HTTP POST /login 接口的 JSON 请求体</li>
 *   <li>负责前端数据格式的初步校验（非空、长度、格式等）</li>
 *   <li><b>允许</b>使用 Jakarta Validation 注解（@NotBlank、@Size、@Pattern 等）做协议层校验</li>
 * </ul>
 *
 * <p><b>与 LoginCommand 的关系：</b>
 * <ul>
 *   <li>{@code LoginRequest} 是 Web 层的“协议载体”，包含前端直接传入的原始字段（username, password, deviceType）</li>
 *   <li>{@code LoginCommand} 是 Application 层的“业务指令”，包含登录业务所需的所有数据（包括 Web 层提取的 IP、User-Agent）</li>
 *   <li>Controller 负责将 {@code LoginRequest} 与 HttpServletRequest 中的 IP/User-Agent 组合，转换构建成 {@code LoginCommand}</li>
 * </ul>
 *
 * <p><b>设计原则：</b>
 * <ul>
 *   <li>此 DTO 只包含前端显式传递的字段，不包含后端推导的元数据（如 IP、设备指纹）</li>
 *   <li>不携带任何业务逻辑，仅作为 HTTP 层的数据载体</li>
 *   <li>字段命名、校验规则可随前端需求调整，不影响核心业务层</li>
 * </ul>
 *
 * @author loriyuhv
 * @version 1.0 2026/1/11 15:46
 * @since 1.0
 */
@Data
public class LoginRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "设备类型不能为空")
    private String deviceType;

}
