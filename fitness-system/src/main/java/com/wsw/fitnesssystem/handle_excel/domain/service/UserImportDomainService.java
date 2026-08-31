package com.wsw.fitnesssystem.handle_excel.domain.service;

import com.wsw.fitnesssystem.handle_excel.application.dto.UserExcelDTO;
import com.wsw.fitnesssystem.handle_excel.core.collector.ErrorCollector;
import com.wsw.fitnesssystem.handle_excel.core.collector.ErrorCollectorHolder;
import com.wsw.fitnesssystem.handle_excel.domain.model.User;
import com.wsw.fitnesssystem.handle_excel.infrastructure.persistence.db.UserBatchRepository;
import com.wsw.fitnesssystem.shared.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
     * 业务批量校验并转换为领域对象
     *
     * <p>前提：传入的 DTO 已经通过格式校验（非空、长度等）</p>
     * <p>职责：查重、业务规则校验、创建领域对象</p>
     *
     * @param dtoList dtoList 格式校验通过的 DTO 列表（保证非空用户名）
     * @return 合法的领域对象列表
     */
    public List<User> validateAndConvert(List<UserExcelDTO> dtoList) {

        ErrorCollector collector = ErrorCollectorHolder.get();
        List<User> result = new ArrayList<>();

        if (dtoList == null || dtoList.isEmpty()) return List.of();

        // 1. 批量查重（只查库，不做格式校验）
        List<String> usernames = dtoList.stream().map(UserExcelDTO::getUsername).distinct().toList();

        Set<String> existingUsernames = userBatchRepository.findExistingUsernames(usernames);

        // 2. 业务校验 + 转换
        for (UserExcelDTO dto : dtoList) {
            String username = dto.getUsername();
            int rowIndex = dto.getRowIndex() != null ? dto.getRowIndex() : -1;
            List<String> rowData = List.of(
                username,
                Objects.toString(dto.getPassword(), ""),
                Objects.toString(dto.getNickname(), "")
            );

            // 2.1 查重校验
            if (existingUsernames.contains(username)) {
                collector.addError(rowIndex, rowData, "用户名已存在: " + username);
                continue;
            }

            // 2.2 业务规则校验（如：用户名是否包含特殊字符）
            if (!isValidUsernameFormat(username)) {
                collector.addError(rowIndex, rowData, "用户名包含非法字符: " + username);
                continue;
            }

            // 2.3 创建领域对象
            try {
                User user = User.create(username, dto.getPassword(), dto.getNickname(), rowIndex);
                result.add(user);
            } catch (BizException e) {
                // 理论上不会触发（因为格式校验已做），但保留防御性
                collector.addError(rowIndex, rowData, e.getMessage());
            }
        }

        return result;
    }

    /**
     * 用户名格式校验（示例：只允许字母、数字、下划线）
     *
     * @param username 代校验的用户名称
     * @return 校验后的用户名称
     */
    private boolean isValidUsernameFormat(String username) {
        return username != null && username.matches("^[a-zA-Z0-9_]+$");
    }

}
