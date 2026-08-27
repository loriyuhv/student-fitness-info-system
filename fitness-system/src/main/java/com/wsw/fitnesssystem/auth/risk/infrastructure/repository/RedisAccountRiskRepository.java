package com.wsw.fitnesssystem.auth.risk.infrastructure.repository;

import com.wsw.fitnesssystem.auth.risk.domain.model.AccountRiskProfile;
import com.wsw.fitnesssystem.auth.risk.domain.port.AccountRiskRepository;
import com.wsw.fitnesssystem.auth.risk.domain.valueobject.AccountIdentifier;
import com.wsw.fitnesssystem.auth.risk.domain.valueobject.AccountLock;
import com.wsw.fitnesssystem.auth.risk.domain.valueobject.RiskFailResult;
import com.wsw.fitnesssystem.auth.risk.domain.valueobject.RiskPolicy;
import com.wsw.fitnesssystem.auth.shared.infrastructure.redis.AuthRedisKeys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 账号风控仓储 - Redis 实现
 *
 * <p>Key 设计:
 * <li>auth:risk:fail:{user}:{username}  → 失败次数（String）</li>
 * <li>auth:risk:lock:{user}:{username}  → 锁定标记（String）</li>
 *
 * <p>并发安全：使用 Lua 脚本实现原子递增 + 锁定判断。
 *
 * @author loriyuhv
 * @version 1.0 2026/8/10 21:27
 * @since 1.0
 */
@Slf4j
@Repository
public class RedisAccountRiskRepository implements AccountRiskRepository {

    private final StringRedisTemplate redisTemplate;

    // ==================== Lua 脚本 ====================

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

    public RedisAccountRiskRepository(
        StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.failScript = new DefaultRedisScript<>(FAIL_LUA_SCRIPT, List.class);
    }

    @Override
    public Optional<AccountRiskProfile> findByIdentifier(AccountIdentifier identifier) {
        String username = identifier.username();
        String failKey = AuthRedisKeys.riskUserFailKey(username);
        String lockKey = AuthRedisKeys.riskUserLockKey(username);

        String failCountStr = redisTemplate.opsForValue().get(failKey);
        Boolean locked = redisTemplate.hasKey(lockKey);

        int failCount = (failCountStr == null) ? 0 : Integer.parseInt(failCountStr);
        AccountLock lock = locked ? AccountLock.locked() : AccountLock.unlocked();

        // 无任何记录，返回 empty
        if (failCount == 0 && !lock.status()) {
            return Optional.empty();
        }

        return Optional.of(AccountRiskProfile.restore(identifier, failCount, lock));
    }

    @Override
    public RiskFailResult incrementFailAndGet(AccountIdentifier identifier, RiskPolicy policy) {
        String username = identifier.username();
        String failKey = AuthRedisKeys.riskUserFailKey(username);
        String lockKey = AuthRedisKeys.riskUserLockKey(username);

        long lockTtl = policy.lockDurationSeconds();
        long failTtl = policy.countWindowSeconds();

        List<Long> result = redisTemplate.execute(
            failScript,
            List.of(failKey, lockKey),
            String.valueOf(policy.maxFailCount()),
            String.valueOf(lockTtl),
            String.valueOf(failTtl)
        );

        long currentFail = result.get(0);
        boolean locked = result.get(1) == 1;
        int remaining = (int) Math.max(0, policy.maxFailCount() - currentFail);

        log.debug("Risk fail record: user={}, failCount={}, locked={}, remaining={}",
            username, currentFail, locked, remaining);

        return new RiskFailResult((int) currentFail, locked, remaining);
    }

    @Override
    public void delete(AccountIdentifier identifier) {
        String username = identifier.username();
        redisTemplate.delete(AuthRedisKeys.riskUserFailKey(username));
        redisTemplate.delete(AuthRedisKeys.riskUserLockKey(username));
        log.debug("Deleted risk state for user: {}", username);
    }

}
