package com.wsw.fitnesssystem.iam.session.application.event;

import com.wsw.fitnesssystem.iam.audit.domain.valueobject.LogoutReason;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 会话终止事件
 *
 * <p><b>职责：</b>当某个登录会话终止时发布，用于通知审计、授权、风控等模块做后续处理。</p>
 *
 * <p><b>终止来源（reason）：</b>
 * <ul>
 *   <li>主动登出：{@code LogoutCommandService}（authentication 侧），reason=LOGOUT</li>
 *   <li>管理员撤销：{@code RevokeSessionCommandService}（session 侧），reason=KICK</li>
 * </ul></p>
 *
 * <p><b>订阅者：</b>
 * <ul>
 *   <li>audit 子域：写入登出 / 撤销审计记录</li>
 *   <li>authorization 子域（后续）：清理授权缓存</li>
 *   <li>风控子域（可选）：异常登出模式识别</li>
 * </ul></p>
 *
 * <p><b>设计说明：</b>
 * 事件同时携带 {@code campusId} 和 {@code userId}，是为了让订阅方无需再解析
 * {@code tokenId} 即可完成上下文相关的处理（如按用户清理缓存）。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/28 16:31
 * @since 1.0
 */
@Getter
public class SessionTerminatedEvent extends ApplicationEvent {

    /** 用户ID */
    private final long userId;

    /** 校区ID */
    private final long campusId;

    /** 被终止的 AccessToken ID */
    private final String tokenId;

    /** 终止原因（登出 / 踢人 / 其他） */
    private final LogoutReason reason;

    public SessionTerminatedEvent(
        Object source,
        long campusId,
        long userId,
        String tokenId,
        LogoutReason reason
    ) {
        super(source);
        this.campusId = campusId;
        this.userId = userId;
        this.tokenId = tokenId;
        this.reason = reason;
    }

}
