package com.wsw.fitnesssystem.iam.risk.infrastructure.cache;

import com.wsw.fitnesssystem.iam.risk.domain.model.RiskProfile;
import com.wsw.fitnesssystem.iam.risk.domain.repository.RiskRepository;
import com.wsw.fitnesssystem.iam.risk.domain.vb.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 风控仓储 - Redis 实现
 *
 * <p><b>Key 设计：</b>
 * <ul>
 *   <li>{@code iam:risk:fail:{dimension}:{value}} → 失败次数（String）</li>
 *   <li>{@code iam:risk:lock:{dimension}:{value}} → 锁定标记（String）</li>
 * </ul></p>
 *
 * <p><b>并发安全：</b>使用 Lua 脚本实现原子「递增 + 锁定判断」，
 * 全程在 Redis 服务端执行，Java 层无竞态。</p>
 *
 * <p><b>维度无关：</b>本实现不感知具体维度，
 * Key 由 {@link RiskSubject#dimension()} 驱动，扩展新维度无需修改本类。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/10 21:27
 * @since 1.0
 */
@Slf4j
@Repository
public class RedisRiskRepository implements RiskRepository {

    private final StringRedisTemplate redisTemplate;

    // ==================== Lua 脚本 ====================

    /**
     * 原子「失败计数 +1 + 阈值判断 + 锁定」脚本。
     *
     * <p><b>KEYS：</b>
     * <ol>
     *   <li>[1] failKey - 失败计数 Key</li>
     *   <li>[2] lockKey - 锁定标记 Key</li>
     * </ol></p>
     *
     * <p><b>ARGV：</b>
     * <ol>
     *   <li>[1] maxFail - 阈值</li>
     *   <li>[2] lockTtl - 锁定时长（秒）</li>
     *   <li>[3] failTtl - 计数窗口（秒）</li>
     * </ol></p>
     *
     * <p><b>返回：</b>{@code {当前失败次数, 是否锁定(1/0)}}</p>
     */
    private static final String FAIL_LUA_SCRIPT = """
        local failKey = KEYS[1]
        local lockKey = KEYS[2]
        local maxFail = tonumber(ARGV[1])
        local lockTtl = tonumber(ARGV[2])
        local failTtl = tonumber(ARGV[3])
        
        -- 1. 先检查是否已锁定
        local alreadyLocked = redis.call('EXISTS', lockKey)
        if alreadyLocked == 1 then
            -- 已锁定，直接返回当前失败次数（不递增）
            local currentFail = redis.call('GET', failKey)
            currentFail = currentFail or 0
            return {tonumber(currentFail), 1}
        end
        
        -- 2. 未锁定，正常递增
        -- 2.1 原子递增失败次数
        local currentFail = redis.call('INCR', failKey)
        -- 2.2 设置失败计数过期时间（滑动窗口）
        redis.call('EXPIRE', failKey, failTtl)
        
        local locked = 0
        if currentFail >= maxFail then
            redis.call('SET', lockKey, '1', 'EX', lockTtl)
            locked = 1
        end
        
        -- 4. 返回当前失败次数和是否锁定
        return {currentFail, locked}
        """;

    private final RedisScript<List> failScript;

    public RedisRiskRepository(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.failScript = new DefaultRedisScript<>(FAIL_LUA_SCRIPT, List.class);
    }

    // ==================== 端口实现 ====================

    @Override
    public Optional<RiskProfile> find(RiskSubject subject) {
        String failKey = RiskRedisKeys.failKey(subject);
        String lockKey = RiskRedisKeys.lockKey(subject);

        String failCountStr = redisTemplate.opsForValue().get(failKey);
        Boolean locked = redisTemplate.hasKey(lockKey);

        int failCount = (failCountStr == null) ? 0 : Integer.parseInt(failCountStr);
        LockState lock = locked ? LockState.locked() : LockState.unlocked();

        // 无任何记录，返回 empty
        if (failCount == 0 && !lock.status()) {
            return Optional.empty();
        }

        return Optional.of(RiskProfile.restore(subject, failCount, lock));
    }

    @Override
    public RiskFailResult incrementFailAndGet(RiskSubject subject, RiskPolicy policy) {
        String failKey = RiskRedisKeys.failKey(subject);
        String lockKey = RiskRedisKeys.lockKey(subject);

        List<?> result = redisTemplate.execute(
            failScript,
            List.of(failKey, lockKey),
            String.valueOf(policy.maxFailCount()),
            String.valueOf(policy.lockDurationSeconds()),
            String.valueOf(policy.countWindowSeconds())
        );

        if (result.size() < 2) {
            throw new IllegalStateException("Unexpected Lua result for subject: " + subject.value());
        }

        long currentFail = ((Number) result.get(0)).longValue();
        boolean locked = ((Number) result.get(1)).intValue() == 1;
        int remaining = (int) Math.max(0, policy.maxFailCount() - currentFail);

        log.debug("Risk fail recorded: dimension={}, value={}, failCount={}, locked={}, remaining={}",
            subject.dimension(), subject.value(), currentFail, locked, remaining);

        return new RiskFailResult((int) currentFail, locked, remaining);
    }

    @Override
    public void delete(RiskSubject subject) {
        redisTemplate.delete(RiskRedisKeys.failKey(subject));
        redisTemplate.delete(RiskRedisKeys.lockKey(subject));
        log.debug("Deleted risk state: dimension={}, value={}", subject.dimension(), subject.value());
    }

}
