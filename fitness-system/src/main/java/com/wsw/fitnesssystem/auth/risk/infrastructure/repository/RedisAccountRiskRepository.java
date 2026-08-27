package com.wsw.fitnesssystem.auth.risk.infrastructure.repository;

import com.wsw.fitnesssystem.auth.risk.domain.model.AccountRiskProfile;
import com.wsw.fitnesssystem.auth.risk.domain.port.AccountRiskRepository;
import com.wsw.fitnesssystem.auth.risk.domain.valueobject.AccountIdentifier;
import com.wsw.fitnesssystem.auth.risk.domain.valueobject.AccountLock;
import com.wsw.fitnesssystem.auth.risk.infrastructure.config.RiskPolicyProperties;
import com.wsw.fitnesssystem.auth.shared.infrastructure.redis.AuthRedisKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

/**
 * 账号风控仓储 - Redis 实现
 *
 * <p>Key 设计:
 * <li>auth:risk:fail:{user}:{username}  → 失败次数（String）</li>
 * <li>auth:risk:lock:{user}:{username}  → 锁定标记（String）</li>
 * @author loriyuhv
 * @version 1.0 2026/8/10 21:27
 * @since 1.0
 */
@Repository
@RequiredArgsConstructor
public class RedisAccountRiskRepository implements AccountRiskRepository {

    private final StringRedisTemplate redisTemplate;
    private final RiskPolicyProperties policyProperties;

    @Override
    public Optional<AccountRiskProfile> findByIdentifier(AccountIdentifier identifier) {
        String username = identifier.username();
        String failKey = AuthRedisKeys.riskUserFailKey(username);
        String lockKey = AuthRedisKeys.riskUserLockKey(username);

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

        return Optional.of(AccountRiskProfile.restore(identifier, failCount, lock));
    }

    @Override
    public void save(AccountRiskProfile profile) {
        String username = profile.getIdentifier().username();

        // 保存失败次数（每次保存都刷新 TTL）
        if (profile.getConsecutiveFailCount() > 0) {
            saveFailCount(username, profile.getConsecutiveFailCount());
        } else {
            clearFailCount(username);
        }

        // 保存锁定状态
        if (profile.getLock().isLocked()) {
            lockAccount(username);
        } else {
            unlockAccount(username);
        }
    }

    @Override
    public void delete(AccountIdentifier identifier) {
        String username = identifier.username();
        clearFailCount(username);
        unlockAccount(username);
    }

    private void saveFailCount(String username, int count) {
        String key = AuthRedisKeys.riskUserFailKey(username);
        Duration ttl = Duration.ofMinutes(policyProperties.getCountWindowMinutes());
        redisTemplate.opsForValue().set(key, String.valueOf(count), ttl);
    }

    private void clearFailCount(String username) {
        redisTemplate.delete(AuthRedisKeys.riskUserFailKey(username));
    }

    private void lockAccount(String username) {
        String key = AuthRedisKeys.riskUserLockKey(username);
        Duration ttl = Duration.ofMinutes(policyProperties.getLockDurationMinutes());
        redisTemplate.opsForValue().set(key, "1", ttl);
    }

    private void unlockAccount(String username) {
        redisTemplate.delete(AuthRedisKeys.riskUserLockKey(username));
    }

}
