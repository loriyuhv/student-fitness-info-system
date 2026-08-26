package com.wsw.fitnesssystem.auth.authentication.application.dto.command;

import lombok.Builder;
import lombok.Getter;

/**
 * 登录业务指令（Application 层输入模型）
 *
 * <p><b>职责：</b>
 * <ul>
 *   <li>承载应用层登录鉴权所需的全部输入数据，包括前端传递的凭证和后端推导的元数据</li>
 *   <li>作为 Application Service 登录方法的入参，表达“一次登录请求的业务语义”</li>
 *   <li><b>严禁</b>添加任何 Web/JSON 序列化注解（如 @JsonProperty、@Valid 等），
 *       保持 POJO 的纯净性，确保其在非 Web 环境（如单元测试、MQ 消费）中可独立构建</li>
 * </ul>
 *
 * <p><b>与 LoginRequest 的关系（关键解耦点）：</b>
 * <ul>
 *   <li>{@code LoginRequest} 是 Web 层的“协议载体”，属于 {@code interfaces.web.dto} 包，
 *       包含前端直接传递的原始字段（username, password, deviceType）以及校验注解</li>
 *   <li>{@code LoginCommand} 是 Application 层的“业务指令”，属于 {@code application.dto.command} 包，
 *       包含登录业务所需的所有数据，其中 {@code ip} 和 {@code userAgent} 由 Web 层从
 *       {@code HttpServletRequest} 中提取并组装进来</li>
 *   <li>Controller 负责将两者组合转换，实现“协议层”与“业务层”的彻底隔离</li>
 * </ul>
 *
 * <p><b>设计原则：</b>
 * <ul>
 *   <li>只包含登录业务关心的数据，不包含任何 Web 容器相关对象（如 HttpServletRequest、HttpSession）</li>
 *   <li>所有字段均为不可变（通过 {@code @Builder} 和 {@code @Getter} 实现），保证线程安全</li>
 *   <li>设备类型（deviceType）用于后续的多端登录互踢、安全审计等策略</li>
 *   <li>IP 和 User-Agent 用于风控、日志记录、异常登录检测等安全增强功能</li>
 * </ul>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>用户通过用户名密码登录</li>
 *   <li>管理后台代登录（内部调用，无需 Web 层，可直接构造 Command）</li>
 *   <li>单元测试中模拟登录行为</li>
 * </ul>
 *
 * @author loriyuhv
 * @version 1.0 2026/1/16 12:59
 * @since 1.0
 */
@Getter
@Builder
public class LoginCommand {

    /** 用户账号 */
    private String username;

    /** 账号密码 */
    private String password;

    /** 设备类型：WEB / APP / MINI_PROGRAM */
    private String deviceType;

    /** 设备ID */
    private String deviceId;

    /** 客户端 IP */
    private String ip;

    /** User-Agent */
    private String userAgent;

}
