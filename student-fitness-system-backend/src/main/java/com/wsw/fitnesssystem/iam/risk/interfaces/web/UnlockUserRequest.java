package com.wsw.fitnesssystem.iam.risk.interfaces.web;

import jakarta.validation.constraints.NotBlank;

/**
 * 解锁用户请求体 - Web 入站 DTO
 *
 * <p><b>职责：</b>承载管理员解封账号风控状态的入参，
 * 由 {@link RiskAdminController#unlockUser} 消费。</p>
 *
 * <p><b>为什么用 RequestBody 而不是 Path Variable？</b></p>
 * <ul>
 *   <li>username 允许出现特殊字符（中文、{@code @}、{@code .} 等），
 *       放在路径中需要 URL 编码，容易踩坑</li>
 *   <li>Path Variable 天然无法表达空字符串（{@code /users//unlock}
 *       不会匹配 {@code /users/{username}/unlock}），
 *       导致「空用户名」这类非法输入无法走校验分支</li>
 *   <li>未来若支持批量解锁，body 结构扩展更自然</li>
 * </ul>
 *
 * <p><b>校验：</b>{@code @NotBlank} 由 {@code @Valid} 触发，
 * 校验失败抛出 {@code MethodArgumentNotValidException}，
 * 由全局异常处理器统一转换为 400 响应。</p>
 *
 * <p><b>与领域校验的关系：</b>
 * <ul>
 *   <li>本 DTO：<b>接口层</b>防御，拦截「请求格式非法」</li>
 *   <li>{@code RiskSubject}：<b>领域层</b>兜底，防止任何非法值进入领域</li>
 * </ul>
 * 双层防御，任一层校验不通过都会拒绝请求。</p>
 *
 * @param username 用户登录名，非空白
 * @author loriyuhv
 * @version 1.0 2026/9/26 12:51
 * @since 1.0
 */
public record UnlockUserRequest(@NotBlank(message = "用户名不能为空") String username) {
}
