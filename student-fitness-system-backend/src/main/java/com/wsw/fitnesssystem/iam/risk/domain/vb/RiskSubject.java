package com.wsw.fitnesssystem.iam.risk.domain.vb;

import com.wsw.fitnesssystem.iam.risk.domain.enums.RiskDimension;
import com.wsw.fitnesssystem.shared.domain.exception.DomainValidationException;
import org.apache.commons.lang3.StringUtils;

/**
 * 风控主体 - 值对象
 *
 * <p><b>职责：</b>唯一标识「被风控的对象」。</p>
 *
 * <p><b>为什么不用 {@code AccountIdentifier}？</b></p>
 * <ul>
 *   <li>账号只是风控的一个维度。IP、设备、行为维度都有各自的主体标识</li>
 *   <li>若把主体锁死为 username，则所有下游接口（仓储、Key、用例）
 *       都会被隐式绑在账号维度上，扩展时需大面积改动</li>
 *   <li>{@code RiskSubject} = (维度, 标识值) 二元组，正交表达所有维度</li>
 * </ul>
 *
 * <p><b>不变量：</b>维度非 null；标识值非空白。</p>
 *
 * <p><b>示例：</b>
 * <pre>{@code
 * RiskSubject.user("zhangsan");           // 账号维度
 * RiskSubject.of(RiskDimension.IP, "1.2.3.4");  // IP 维度
 * }</pre></p>
 *
 * @param dimension 风控维度，决定 Key 前缀与策略来源
 * @param value     主体标识值（username / IP / deviceId / 行为指纹）
 * @author loriyuhv
 * @version 1.0 2026/9/26 07:48
 * @since 1.0
 */
public record RiskSubject(RiskDimension dimension, String value) {

    public RiskSubject {
        if (dimension == null) {
            throw new DomainValidationException("风控维度不能为空");
        }
        if (StringUtils.isBlank(value)) {
            throw new DomainValidationException("风控主体标识不能为空");
        }
    }

    /** 通用工厂：任意维度。 */
    public static RiskSubject of(RiskDimension dimension, String value) {
        return new RiskSubject(dimension, value);
    }

    // ==================== 便捷工厂 ====================

    /** 账号维度主体 */
    public static RiskSubject user(String username) {
        return new RiskSubject(RiskDimension.USER, username);
    }

}
