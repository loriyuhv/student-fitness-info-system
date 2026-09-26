package com.wsw.fitnesssystem.iam.risk.domain.vb;

/**
 * 风控策略 - 值对象
 *
 * <p><b>职责：</b>封装一次风控判定所需的全部参数，与具体维度无关。
 * 由应用层通过 {@code RiskPolicyProvider} 按维度获取后传入领域/仓储层。</p>
 *
 * <p><b>为什么用 {@link IllegalArgumentException} 而不是领域异常？</b></p>
 * <ul>
 *   <li>本值对象的字段<b>全部来自配置</b>（见 {@code RiskPolicyProperties}），
 *       不是用户输入，不属于「领域不变量被业务行为违反」</li>
 *   <li>校验失败意味着<b>配置或代码错误</b>，属于开发期问题，
 *       应该尽早暴露（fail-fast）</li>
 *   <li>配置的<b>主防线</b>在 {@code RiskPolicyProperties} 的
 *       {@code @Validated} 上（启动期校验），本类校验是<b>兜底</b>——
 *       防止任何非法值绕过配置层进入领域</li>
 * </ul>
 *
 * <p><b>使用约束：</b>本值对象只在启动期 / 装配期构造，
 * <b>不应在请求处理路径中构造</b>。若发现运行时抛出本异常，
 * 说明配置层校验被绕过，需要排查。</p>
 *
 * @param maxFailCount        触发锁定的失败次数阈值
 * @param lockDurationSeconds 锁定时长（秒）
 * @param countWindowSeconds  失败计数统计窗口（秒）
 * @author loriyuhv
 * @version 1.0 2026/8/10 21:08
 * @since 1.0
 */
public record RiskPolicy(
    int maxFailCount, long lockDurationSeconds, long countWindowSeconds) {

    public RiskPolicy {
        if (maxFailCount <= 0) {
            throw new IllegalArgumentException("maxFailCount must > 0, got: " + maxFailCount);
        }
        if (lockDurationSeconds <= 0) {
            throw new IllegalArgumentException("lockDurationSeconds must > 0, got: " + lockDurationSeconds);
        }
        if (countWindowSeconds <= 0) {
            throw new IllegalArgumentException("countWindowSeconds must > 0, got: " + countWindowSeconds);
        }
    }

    /** 纯领域判断：是否达到锁定阈值 */
    public boolean shouldLock(int failCount) {
        return failCount >= maxFailCount;
    }

    /** 纯领域计算：剩余尝试次数 */
    public int remainingAttempts(int failCount, boolean locked) {
        // 已锁定则剩余为 0，否则计算剩余
        return locked ? 0 : Math.max(0, maxFailCount - failCount);
    }

}
