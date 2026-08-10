package com.wsw.fitnesssystem.auth.infrastructure.risk;

import com.wsw.fitnesssystem.auth.domain.risk.model.AccountRiskProfile;
import com.wsw.fitnesssystem.auth.domain.risk.port.AccountRiskRepository;
import com.wsw.fitnesssystem.auth.domain.risk.valueobject.AccountIdentifier;
import com.wsw.fitnesssystem.auth.domain.risk.valueobject.AccountLock;
import com.wsw.fitnesssystem.auth.infrastructure.config.RiskPolicyProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

/**
 * 账号风控仓储 - Redis 实现
 *
 * <p>Key 设计（v2 前缀，与旧版隔离）：
 * <li>auth:risk:fail:{username}  → 失败次数（String）</li>
 * <li>auth:risk:lock:{username}  → 锁定标记（String）</li>
 * @author loriyuhv
 * @version 1.0 2026/8/10 21:27
 * @since 1.0
 */
@Repository
@RequiredArgsConstructor
public class RedisAccountRiskRepository implements AccountRiskRepository {
    private final StringRedisTemplate redisTemplate;
    private final RiskPolicyProperties policyProperties;

    private static final String FAIL_PREFIX = "auth:risk:fail:";
    private static final String LOCK_PREFIX = "auth:risk:lock:";

    @Override
    public Optional<AccountRiskProfile> findByIdentifier(AccountIdentifier identifier) {
        String username = identifier.username();
        String failKey = FAIL_PREFIX + username;
        String lockKey = LOCK_PREFIX + username;

        String failCountStr = redisTemplate.opsForValue().get(failKey);
        Boolean locked = redisTemplate.hasKey(lockKey);

        int failCount = (failCountStr == null) ? 0 : Integer.parseInt(failCountStr);
        AccountLock lock = locked
                ? AccountLock.locked()
                : AccountLock.unlocked();

        // 无任何记录，返回 empty
        if (failCount == 0 && !lock.isLocked()) {
            return Optional.empty();
        }

        return Optional.of(new AccountRiskProfile(identifier, failCount, lock));
    }

    @Override
    public void save(AccountRiskProfile profile) {
        String username = profile.getIdentifier().username();
        String failKey = FAIL_PREFIX + username;
        String lockKey = LOCK_PREFIX + username;

        Duration failTtl = Duration.ofMinutes(policyProperties.getCountWindowMinutes());
        Duration lockTtl = Duration.ofMinutes(policyProperties.getLockDurationMinutes());

        // 保存失败次数（每次保存都刷新 TTL）
        if (profile.getConsecutiveFailCount() > 0) {
            redisTemplate.opsForValue().set(
                    failKey,
                    String.valueOf(profile.getConsecutiveFailCount()),
                    failTtl
            );
        } else {
            redisTemplate.delete(failKey);
        }

        // 保存锁定状态
        if (profile.getLock().isLocked()) {
            redisTemplate.opsForValue().set(lockKey, "1", lockTtl);
        } else {
            redisTemplate.delete(lockKey);
        }
    }

    @Override
    public void delete(AccountIdentifier identifier) {
        String username = identifier.username();
        redisTemplate.delete(FAIL_PREFIX + username);
        redisTemplate.delete(LOCK_PREFIX + username);
    }
}
