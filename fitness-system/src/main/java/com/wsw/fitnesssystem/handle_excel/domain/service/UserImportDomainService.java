package com.wsw.fitnesssystem.handle_excel.domain.service;

import com.wsw.fitnesssystem.handle_excel.application.dto.UserExcelDTO;
import com.wsw.fitnesssystem.handle_excel.domain.model.User;
import com.wsw.fitnesssystem.handle_excel.infrastructure.config.ExcelConstants;
import com.wsw.fitnesssystem.handle_excel.infrastructure.persistence.db.UserBatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户导入领域服务
 * <p>负责用户导入的业务校验和领域转换，属于领域层核心逻辑</p>
 * <p>原则：只处理业务规则，不处理技术细节（如 Redis、线程池）</p>
 * @author loriyuhv
 * @version 1.0 2026/3/26 15:56
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserImportDomainService {

    private final UserBatchRepository userBatchRepository;

    /**
     * 校验并转换：包含格式校验 + 批量查重 + 空密码处理
     * @param batch 批次
     * @return 校验通过并转换后的领域模型列表
     */
    public List<User> validateAndConvert(List<UserExcelDTO> batch) {

        List<User> result = new ArrayList<>();

        // 1. 格式校验 + 过滤
        List<UserExcelDTO> filtered = batch.stream().filter(this::isValid).collect(Collectors.toList());

        if (filtered.isEmpty()) {
            return result;
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
            String password = StringUtils.isBlank(dto.getPassword())
                    ? ExcelConstants.DEFAULT_PASSWORD
                    : dto.getPassword();
            user.setPassword(password);
            user.setNickname(StringUtils.isBlank(dto.getNickname())
                    ? username
                    : dto.getNickname().trim());
            result.add(user);
        }
        return result;
    }

    /**
     * 单条格式校验
     *
     * @param dto Excel DTO
     * @return true=合法, false=非法
     */
    private boolean isValid(UserExcelDTO dto) {
        if (dto == null) {
            return false;
        }
        if (StringUtils.isBlank(dto.getUsername())) {
            log.warn("用户名不能为空，跳过");
            return false;
        }
        if (dto.getUsername().length() > ExcelConstants.USERNAME_MAX_LENGTH) {
            log.warn("用户名过长，跳过: {}", dto.getUsername());
            return false;
        }
        return true;
    }
}
