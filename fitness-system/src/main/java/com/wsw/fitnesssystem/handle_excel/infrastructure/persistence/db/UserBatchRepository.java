package com.wsw.fitnesssystem.handle_excel.infrastructure.persistence.db;

import com.wsw.fitnesssystem.handle_excel.infrastructure.persistence.db.entity.SysUser;
import com.wsw.fitnesssystem.handle_excel.infrastructure.persistence.db.mapper.ExcelSysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author loriyuhv
 * @version 1.0 2026/3/26 15:53
 * @since 1.0
 */
@Repository
@RequiredArgsConstructor
public class UserBatchRepository {

    private final ExcelSysUserMapper userMapper;

    private static final int BATCH_SIZE = 500;

    public void batchInsert(List<SysUser> list) {

        for (int i = 0; i < list.size(); i += BATCH_SIZE) {
            List<SysUser> batch = list.subList(i, Math.min(i + BATCH_SIZE, list.size()));
            userMapper.batchInsert(batch);
        }
    }

    public Set<String> findExistingUsernames(List<String> usernames) {
        return new HashSet<>(userMapper.selectExistingUsernames(usernames));
    }

    public Set<String> findExistingPhones(List<String> phones) {
        return new HashSet<>(userMapper.selectExistingPhones(phones));
    }
}
