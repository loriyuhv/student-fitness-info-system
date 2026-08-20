package com.wsw.fitnesssystem.handle_excel.infrastructure.persistence.db;

import com.wsw.fitnesssystem.handle_excel.infrastructure.persistence.db.entity.SysUser;
import com.wsw.fitnesssystem.handle_excel.infrastructure.persistence.db.mapper.ExcelSysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author loriyuhv
 * @version 1.0 2026/3/26 15:53
 * @since 1.0
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class UserBatchRepository {

    private final ExcelSysUserMapper userMapper;

    private static final int BATCH_SIZE = 500;

    /**
     * 批量插入（内部再分片，防止SQL过长）
     * @param list 用户对象列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchInsert(List<SysUser> list) {
        if (list == null || list.isEmpty()) return;

        for (int i = 0; i < list.size(); i += BATCH_SIZE) {
            List<SysUser> batch = list.subList(i, Math.min(i + BATCH_SIZE, list.size()));
            userMapper.batchInsert(batch);
            log.debug("批量插入 {} 条", batch.size());
        }
    }

    public Set<String> findExistingUsernames(List<String> usernames) {
        return new HashSet<>(userMapper.selectExistingUsernames(usernames));
    }

    public Set<String> findExistingPhones(List<String> phones) {
        return new HashSet<>(userMapper.selectExistingPhones(phones));
    }
}
