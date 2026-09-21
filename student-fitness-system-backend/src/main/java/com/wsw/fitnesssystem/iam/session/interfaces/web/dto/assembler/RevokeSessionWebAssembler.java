package com.wsw.fitnesssystem.iam.session.interfaces.web.dto.assembler;

import com.wsw.fitnesssystem.iam.session.application.dto.command.RevokeSessionCommand;
import com.wsw.fitnesssystem.iam.session.application.dto.result.RevokeSessionResult;
import com.wsw.fitnesssystem.iam.session.interfaces.web.dto.response.RevokeSessionResponse;
import org.springframework.stereotype.Component;

/**
 * 撤销会话场景 Web 层组装器
 *
 * <p><b>职责：</b>
 * <ul>
 *   <li>Web 层协议对象 ⇄ Application 层 DTO 的双向转换</li>
 *   <li>承载协议适配逻辑（字段命名、默认值、展示文案等）</li>
 *   <li>防腐层（ACL）的具体实现入口，隔离 Web 与 Application</li>
 * </ul>
 *
 * <p><b>设计约定：</b>
 * <ul>
 *   <li>Assembler 无状态，使用 {@code @Component} 注册为单例</li>
 *   <li>不在 Assembler 中做业务判断，只做机械转换与文案适配</li>
 * </ul>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/21 15:50
 * @since 1.0
 */
@Component
public class RevokeSessionWebAssembler {

    /**
     * Web 数据 → Application 层 Command
     *
     * @param campusId 校区ID
     * @param userId   目标用户ID
     * @return 撤销会话业务指令
     */
    public RevokeSessionCommand toCommand(Long campusId, Long userId) {
        return new RevokeSessionCommand(campusId, userId);
    }

    /**
     * Application 层 Result → Web 层 Response
     *
     * @param result 应用层结果
     * @return 协议响应对象
     */
    public RevokeSessionResponse toResponse(RevokeSessionResult result) {
        return new RevokeSessionResponse(
            result.count(),
            result.revokedTokenIds(),
            buildMessage(result)
        );
    }

    /**
     * 根据结果构造展示消息
     * <p>属于展示层文案适配，不涉及业务规则判断。</p>
     *
     * @param result 应用层结果
     * @return 面向用户的展示文案
     */
    private String buildMessage(RevokeSessionResult result) {
        if (result.count() == 0) {
            return "该用户当前无在线会话";
        }
        return "已撤销 " + result.count() + " 个会话";
    }

}
