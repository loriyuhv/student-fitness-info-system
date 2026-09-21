package com.wsw.fitnesssystem.iam.session.application.dto.command;

/**
 * 撤销会话业务指令（Application 层输入模型）
 *
 * <p><b>职责：</b>
 * <ul>
 *   <li>承载"管理员撤销某用户全部在线会话"这一用例的输入数据</li>
 *   <li>作为 {@code RevokeSessionCommandService#revoke} 的入参</li>
 *   <li><b>严禁</b>添加任何 Web/JSON 序列化注解，保持 POJO 纯净性</li>
 * </ul>
 *
 * <p><b>与 Web 层协议对象的关系：</b>
 * <ul>
 *   <li>Web 层路径变量由 Controller 通过 Assembler 转换为本 Command</li>
 *   <li>Command 中不出现 {@code HttpServletRequest}、{@code PathVariable} 等 Web 概念</li>
 * </ul>
 *
 * <p><b>设计原则：</b>
 * <ul>
 *   <li>不可变 record，保证线程安全</li>
 *   <li>只承载用例需要的字段，不冗余</li>
 * </ul>
 *
 * @param campusId 校区ID
 * @param userId   目标用户ID
 * @author loriyuhv
 * @version 1.0 2026/9/21 15:37
 * @since 1.0
 */
public record RevokeSessionCommand(
    Long campusId,
    Long userId
) {
}
