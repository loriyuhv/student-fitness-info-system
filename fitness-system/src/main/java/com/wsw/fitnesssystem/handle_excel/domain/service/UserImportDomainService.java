package com.wsw.fitnesssystem.handle_excel.domain.service;

import com.wsw.fitnesssystem.handle_excel.application.dto.UserExcelDTO;
import com.wsw.fitnesssystem.handle_excel.core.collector.ErrorCollector;
import com.wsw.fitnesssystem.handle_excel.core.collector.ErrorCollectorHolder;
import com.wsw.fitnesssystem.handle_excel.domain.model.User;
import com.wsw.fitnesssystem.handle_excel.infrastructure.persistence.db.UserBatchRepository;
import com.wsw.fitnesssystem.shared.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 用户导入领域服务
 * <p>负责用户导入的核心业务规则：格式校验、批量查重、领域转换</p>
 * <p>原则：只处理业务规则，不处理技术细节（如 Redis、线程池、密码加密）</p>
 *
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
     * 批量校验并转换为领域对象
     * <p>流程：</p>
     * <li>1. 过滤格式非法的 DTO（空用户名等）</li>
     * <li>2. 批量查询数据库已存在的用户名</li>
     * <li>3. 使用充血模型 {@link User#create} 构造合法领域对象</li>
     * @param batch Excel DTO 批次
     * @return 校验通过并转换后的领域模型列表（已去重）
     */
    public List<User> validateAndConvert(List<UserExcelDTO> batch) {
        ErrorCollector collector = ErrorCollectorHolder.get();
        List<User> result = new ArrayList<>();

        // 1. 基础格式过滤
        List<UserExcelDTO> validDtoList = batch.stream()
                .filter(dto ->
                        dto != null && StringUtils.isNotBlank(dto.getUsername()))
                .toList();

        if (validDtoList.isEmpty()) {
            return List.of();
        }

        // 2. 批量查重：查询数据库中已存在的用户名
        List<String> usernames = validDtoList.stream()
                .map(UserExcelDTO::getUsername)
                .map(String::trim)
                .distinct()
                .toList();

        if (usernames.isEmpty()) {
            return List.of();
        }

        Set<String> existingUsernames = userBatchRepository.findExistingUsernames(usernames);

        // 3. 领域转换（通过充血模型的工厂方法保证合法性）
        for (UserExcelDTO dto : validDtoList) {
            String username = dto.getUsername().trim();
            if (existingUsernames.contains(username)) {
                collector.addError(
                    dto.getRowIndex(),
                    List.of(username, dto.getPassword(), dto.getNickname()),
                    "用户名已存在: " + username
                );
                // log.warn("用户名已存在，跳过: {}", username);
                continue; // 跳过空用户名或已存在的
            }
            try {
                User user = User.create(username, dto.getPassword(), dto.getNickname(), dto.getRowIndex());
                result.add(user);
            } catch (BizException e) {
                collector.addError(
                    dto.getRowIndex(),
                    List.of(username, dto.getPassword(), dto.getNickname()),
                    e.getMessage()
                );
                log.warn("用户创建失败: {}", e.getMessage());
            }
        }
        return result;
    }

}
