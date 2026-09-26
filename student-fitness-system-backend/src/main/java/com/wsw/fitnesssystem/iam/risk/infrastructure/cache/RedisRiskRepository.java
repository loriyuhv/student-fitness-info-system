package com.wsw.fitnesssystem.iam.risk.infrastructure.cache;

import com.wsw.fitnesssystem.iam.risk.domain.model.RiskProfile;
import com.wsw.fitnesssystem.iam.risk.domain.repository.RiskRepository;
import com.wsw.fitnesssystem.iam.risk.domain.vb.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

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
            local currentFail = redis.call('GET', failKey) or 0
            return {tonumber(currentFail), 1, 0}
        end
        
        -- 2. 未锁定，正常递增
        -- 2.1 原子递增失败次数
        local currentFail = redis.call('INCR', failKey)
        -- 2.2 设置失败计数过期时间（滑动窗口）
        redis.call('EXPIRE', failKey, failTtl)
        
        local locked = 0
        local newlyLocked = 0
        if currentFail >= maxFail then
            redis.call('SET', lockKey, '1', 'EX', lockTtl)
            redis.call('EXPIRE', failKey, lockTtl)
            locked = 1
            newlyLocked = 1
        end
        
        -- 4. 返回当前失败次数和是否锁定
        return {currentFail, locked, newlyLocked}
        """;

    private static final RedisScript<List<Object>> FAIL_SCRIPT = buildFailScript();

    @SuppressWarnings({"unchecked"})
    private static RedisScript<List<Object>> buildFailScript() {
        Class<List<Object>> listClass = (Class<List<Object>>) (Class<?>) List.class;
        return new DefaultRedisScript<>(FAIL_LUA_SCRIPT, listClass);
    }


    public RedisRiskRepository(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // ==================== 端口实现 ====================

    @Override
    public Optional<RiskProfile> find(RiskSubject subject) {
        String failKey = RiskRedisKeys.failKey(subject);
        String lockKey = RiskRedisKeys.lockKey(subject);

        // 原则5：使用 multiGet 保证一次网络往返，读取同一时刻的快照
        List<String> results = redisTemplate.opsForValue().multiGet(List.of(failKey, lockKey));

        if (results == null || results.size() < 2) {
            return Optional.empty();
        }

        String failCountStr  = results.get(0);
        String lockStr  = results.get(1);
        int failCount = (failCountStr == null) ? 0 : Integer.parseInt(failCountStr);
        LockState lock = (lockStr != null) ? LockState.locked() : LockState.unlocked();

        // 无任何记录，返回 empty
        if (failCount == 0 && !lock.status()) {
            return Optional.empty();
        }

        return Optional.of(RiskProfile.restore(subject, failCount, lock));
    }

    @Override
    public RiskFailResult recordFailure(RiskSubject subject, RiskPolicy policy) {
        String failKey = RiskRedisKeys.failKey(subject);
        String lockKey = RiskRedisKeys.lockKey(subject);

        List<Object> result = redisTemplate.execute(
            FAIL_SCRIPT,
            List.of(failKey, lockKey),
            String.valueOf(policy.maxFailCount()),
            String.valueOf(policy.lockDurationSeconds()),
            String.valueOf(policy.countWindowSeconds())
        );

        // 防御性校验
        if (CollectionUtils.isEmpty(result) || result.size() < 3) {
            throw new IllegalStateException("Unexpected Lua result for subject: " + subject.value());
        }

        int currentFail = ((Number) result.get(0)).intValue();
        boolean locked = ((Number) result.get(1)).intValue() == 1;
        boolean newlyLocked = ((Number) result.get(2)).intValue() == 1;

        // 原则1：调用领域工厂计算 remainingAttempts，Infra 层不再裸算
        RiskFailResult failResult = RiskFailResult.from(currentFail, locked, newlyLocked, policy);

        log.debug("Risk fail recorded: dimension={}, value={}, result={}",
            subject.dimension(), subject.value(), failResult);

        return failResult;
    }

    @Override
    public void delete(RiskSubject subject) {
        String failKey = RiskRedisKeys.failKey(subject);
        String lockKey = RiskRedisKeys.lockKey(subject);
        // 原则6：使用多 key DEL，Redis 保证原子性，一次网络往返
        redisTemplate.delete(List.of(failKey, lockKey));
        log.debug("Deleted risk state: dimension={}, value={}", subject.dimension(), subject.value());
    }

}
