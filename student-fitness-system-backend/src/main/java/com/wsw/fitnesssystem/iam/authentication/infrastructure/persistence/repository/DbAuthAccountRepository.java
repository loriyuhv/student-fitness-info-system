package com.wsw.fitnesssystem.iam.authentication.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wsw.fitnesssystem.iam.authentication.domain.model.AuthAccount;
import com.wsw.fitnesssystem.iam.authentication.domain.repository.AuthAccountRepository;
import com.wsw.fitnesssystem.iam.authentication.infrastructure.persistence.converter.AuthAccountConverter;
import com.wsw.fitnesssystem.iam.authentication.infrastructure.persistence.entity.SysUserPo;
import com.wsw.fitnesssystem.iam.authentication.infrastructure.persistence.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * AuthAccountRepository 的 MySQL 实现
 *
 * @author loriyuhv
 * @version 1.0 2026/9/16 15:17
 * @since 1.0
 */
@Repository
@RequiredArgsConstructor
public class DbAuthAccountRepository implements AuthAccountRepository {

    private final SysUserMapper mapper;

    @Override
    public AuthAccount save(AuthAccount account) {
        SysUserPo po = AuthAccountConverter.toPo(account);
        if (po.getUserId() == null) {
            mapper.insert(po);
        } else {
            /*
            * 注意：@TableLogic 会让 updateById 自动追加 `AND deleted = 0`，因此本方法无法更新 deleted=1 的账号。
            * 若将来要实现「恢复已删除账号」，必须走自定义 SQL（绕过 @TableLogic），不能依赖本方法。
             */
            mapper.updateById(po);
        }
        return AuthAccountConverter.toDomain(po);
    }

    @Override
    public Optional<AuthAccount> findByUserIdAndCampusId(Long userId, Long campusId) {
        LambdaQueryWrapper<SysUserPo> wrapper = new LambdaQueryWrapper<SysUserPo>()
            .eq(SysUserPo::getUserId, userId)
            .eq(SysUserPo::getCampusId, campusId);
        return Optional.ofNullable(mapper.selectOne(wrapper)).map(AuthAccountConverter::toDomain);
    }

    @Override
    public Optional<AuthAccount> findByUsername(String username) {
        LambdaQueryWrapper<SysUserPo> wrapper = new LambdaQueryWrapper<SysUserPo>()
            .eq(SysUserPo::getUsername, username);
        return Optional.ofNullable(mapper.selectOne(wrapper)).map(AuthAccountConverter::toDomain);
    }

    /**
     * 根据一批用户名，查询数据库中已经存在的用户名集合
     * @param usernames 待校验的用户名集合
     * @return 数据库真实存在的用户名集合（返回子集）
     */
    @Override
    public Set<String> findExistingUsernames(Collection<String> usernames) {
        // 防御分支：空集合直接返回空Set，避免数据库无效查询
        if (CollectionUtils.isEmpty(usernames)) {
            return Collections.emptySet();
        }
        LambdaQueryWrapper<SysUserPo> wrapper = new LambdaQueryWrapper<SysUserPo>()
            .in(SysUserPo::getUsername, usernames) // where username IN (?, ?, ?)
            .select(SysUserPo::getUsername); // 只查询username这一列，不要查整行PO，性能优化
        return mapper.selectList(wrapper).stream()
            .map(SysUserPo::getUsername) // 从PO对象提取username字符串
            .collect(Collectors.toSet()); // 转Set，自动去重
    }

    @Override
    public boolean existsByUsername(String username) {
        LambdaQueryWrapper<SysUserPo> wrapper = new LambdaQueryWrapper<SysUserPo>()
            .eq(SysUserPo::getUsername, username);
        return mapper.selectCount(wrapper) > 0;
    }

}
