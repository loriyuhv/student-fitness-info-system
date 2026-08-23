package com.wsw.fitnesssystem.handle_excel.infrastructure.persistence.db;

import com.wsw.fitnesssystem.handle_excel.infrastructure.persistence.db.mapper.ExcelSysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

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

    public Set<String> findExistingUsernames(List<String> usernames) {
        return new HashSet<>(userMapper.selectExistingUsernames(usernames));
    }

}
