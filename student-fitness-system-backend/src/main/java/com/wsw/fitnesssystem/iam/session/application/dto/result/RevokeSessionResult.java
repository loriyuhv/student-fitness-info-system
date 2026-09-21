package com.wsw.fitnesssystem.iam.session.application.dto.result;

import java.util.Collections;
import java.util.Set;

/**
 * 撤销会话业务结果（Application 层输出模型）
 *
 * <p><b>职责：</b>
 * <ul>
 *   <li>承载撤销操作的结果数据</li>
 *   <li>作为 {@code RevokeSessionCommandService#revoke} 的返回值</li>
 *   <li><b>严禁</b>添加任何 Web/JSON 序列化注解</li>
 * </ul>
 *
 * <p><b>空集合语义：</b>
 * "该用户当前无在线会话"用空集合表达，不用 {@code null}，
 * 避免调用方做无意义的 null 判断。</p>
 *
 * @param revokedTokenIds 被撤销的 AccessTokenId 集合；无会话时为空集合（非 null）
 * @author loriyuhv
 * @version 1.0 2026/9/21 15:37
 * @since 1.0
 */
public record RevokeSessionResult(
    Set<String> revokedTokenIds
) {

    /**
     * 紧凑构造器：保证集合永不为 null，语义上"无会话"用空集合表达。
     */
    public RevokeSessionResult {
        revokedTokenIds = (revokedTokenIds == null)
            ? Collections.emptySet()
            : Set.copyOf(revokedTokenIds);
    }

    /**
     * 被撤销的会话数量
     */
    public int count() {
        return revokedTokenIds.size();
    }

}
