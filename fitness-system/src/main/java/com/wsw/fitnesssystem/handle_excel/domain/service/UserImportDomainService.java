package com.wsw.fitnesssystem.handle_excel.domain.service;

import com.wsw.fitnesssystem.handle_excel.application.dto.UserExcelDTO;
import com.wsw.fitnesssystem.handle_excel.domain.model.User;
import com.wsw.fitnesssystem.handle_excel.infrastructure.persistence.db.UserBatchRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author loriyuhv
 * @version 1.0 2026/3/26 15:56
 * @since 1.0
 */
@Slf4j
@Service
public class UserImportDomainService {

    private final UserBatchRepository userBatchRepository;

    public UserImportDomainService(UserBatchRepository userBatchRepository) {
        this.userBatchRepository = userBatchRepository;
    }

    /**
     * 校验并转换：包含格式校验 + 批量查重 + 空密码处理
     * @param batch 批次
     * @return 校验过的用户信息
     */
    public List<User> validateAndConvert(List<UserExcelDTO> batch) {

        List<User> result = new ArrayList<>();

        // 1. 格式校验（收集所有错误，不立即抛异常，让上层决定）
        for (UserExcelDTO dto : batch) {
            if (StringUtils.isBlank(dto.getUsername())) {
                log.warn("用户名不能为空，跳过: {}", dto);
                continue;
            }
            if (dto.getUsername().length() > 50) {
                log.warn("用户名过长，跳过: {}", dto.getUsername());
            }
        }

        // 2. 批量查重：查询数据库中已存在的用户名
        List<String> usernames = batch.stream().map(UserExcelDTO::getUsername)
                .filter(StringUtils::isNotBlank)
                .toList();
        if (usernames.isEmpty()) {
            return result;
        }

        Set<String> existingUsernames = userBatchRepository.findExistingUsernames(usernames);

        // 3. 转换 + 过滤重复
        for (UserExcelDTO dto : batch) {
            String username = dto.getUsername();
            if (StringUtils.isBlank(username) || existingUsernames.contains(username)) {
                continue; // 跳过空用户名或已存在的
            }
            User user = new User();
            user.setUsername(dto.getUsername().trim());
            // 空密码生成密码12456
            String password = StringUtils.isBlank(dto.getPassword()) ? "12345" : dto.getPassword();
            user.setPassword(password);
            user.setNickname(StringUtils.isBlank(dto.getNickname()) ? username : dto.getNickname().trim());
            result.add(user);
        }
        return result;
    }
}
