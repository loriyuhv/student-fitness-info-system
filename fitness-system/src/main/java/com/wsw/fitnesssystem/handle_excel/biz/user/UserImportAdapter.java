package com.wsw.fitnesssystem.handle_excel.biz.user;

import com.wsw.fitnesssystem.handle_excel.application.dto.UserExcelDTO;
import com.wsw.fitnesssystem.handle_excel.core.adapter.ImportAdapter;
import com.wsw.fitnesssystem.handle_excel.infrastructure.config.ExcelConstants;
import com.wsw.fitnesssystem.handle_excel.infrastructure.persistence.db.entity.SysUser;
import com.wsw.fitnesssystem.handle_excel.infrastructure.persistence.db.mapper.ExcelSysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户导入适配器 — 业务接入示例
 * 只需实现 ImportAdapter 接口，Spring 自动扫描注册到中台
 * @author loriyuhv
 * @version 1.0 2026/8/21 15:48
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserImportAdapter implements ImportAdapter<UserExcelDTO, SysUser> {
    private final ExcelSysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public String getBizType() {
        return "USER_IMPORT";
    }

    @Override
    public Class<UserExcelDTO> getDtoClass() {
        return UserExcelDTO.class;
    }

    @Override
    public List<UserExcelDTO> validate(List<UserExcelDTO> batch) {
        // 1. 过滤空用户名
        List<UserExcelDTO> filtered = batch.stream()
                .filter(dto -> StringUtils.isNotBlank(dto.getUsername()))
                .collect(Collectors.toList());

        if (filtered.isEmpty()) {
            return filtered;
        }

        // 2. 批量查重：查询数据库中已存在的用户名
        List<String> usernames = filtered.stream()
                .map(UserExcelDTO::getUsername)
                .toList();

        List<String> existing = userMapper.selectExistingUsernames(usernames);
        Set<String> existingSet = new HashSet<>(existing);

        // 3. 过滤已存在的用户名
        return filtered.stream()
                .filter(dto -> !existingSet.contains(dto.getUsername()))
                .collect(Collectors.toList());
    }

    @Override
    public List<SysUser> convert(List<UserExcelDTO> dtoList) {
        List<SysUser> result = new ArrayList<>();
        for (UserExcelDTO dto : dtoList) {
            SysUser entity = new SysUser();
            entity.setCampusId(ExcelConstants.DEFAULT_CAMPUS_ID);
            entity.setUsername(dto.getUsername().trim());

            // 空密码生成随机密码
            String rawPassword = StringUtils.isBlank(dto.getPassword())
                    ? ExcelConstants.DEFAULT_PASSWORD
                    : dto.getPassword();
            entity.setPassword(passwordEncoder.encode(rawPassword));

            entity.setNickname(StringUtils.isBlank(dto.getNickname())
                    ? dto.getUsername()
                    : dto.getNickname().trim());

            entity.setPhoneNumber(null);
            entity.setEmail(null);
            entity.setUserType(ExcelConstants.DEFAULT_USER_TYPE);   // 学生
            entity.setStatus(ExcelConstants.DEFAULT_STATUS);     // 启用
            entity.setDeleted(ExcelConstants.DEFAULT_DELETED);    // 未删除

            result.add(entity);
        }
        return result;
    }

    @Override
    public void persist(List<SysUser> entities) {
        if (entities == null || entities.isEmpty()) return;

        // 内部再分片，防止 SQL 过长
        int batchSize = 500;
        for (int i = 0; i < entities.size(); i += batchSize) {
            List<SysUser> batch = entities.subList(i, Math.min(i + batchSize, entities.size()));
            userMapper.batchInsert(batch);
        }
        log.debug("用户批量插入完成，共 {} 条", entities.size());
    }
}
