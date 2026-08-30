package com.wsw.fitnesssystem.handle_excel.infrastructure.persistence.db.assembler;

import com.wsw.fitnesssystem.handle_excel.domain.model.User;
import com.wsw.fitnesssystem.handle_excel.infrastructure.config.ExcelConstants;
import com.wsw.fitnesssystem.handle_excel.infrastructure.persistence.db.entity.SysUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 用户领域模型 → 持久化实体 组装器
 * <p>职责：纯技术转换，包含密码加密、默认值填充等基础设施细节</p>
 * <p>不处理业务规则（如"昵称为空时默认用户名"），这些由领域层 {@link User} 保证</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/3/26 16:36
 * @since 1.0
 */
@Component
@RequiredArgsConstructor
public class UserAssembler {

    private final PasswordEncoder passwordEncoder;

    /**
     * 领域模型 → 持久化实体
     * <p>注意：User 中的 password 此时仍是明文（或默认密码），此处负责加密</p>
     *
     * @param user 领域模型
     * @return 持久化实体
     */
    public SysUser toEntity(User user) {
        SysUser entity = new SysUser();

        entity.setRowIndex(user.getRowIndex());
        entity.setCampusId(ExcelConstants.DEFAULT_CAMPUS_ID);
        entity.setUsername(user.getUsername());
        entity.setPassword(passwordEncoder.encode(user.getPassword()));
        entity.setNickname(user.getNickname());
        entity.setPhoneNumber(null);
        entity.setEmail(null);
        entity.setUserType(ExcelConstants.DEFAULT_USER_TYPE); // 学生
        entity.setStatus(ExcelConstants.DEFAULT_STATUS); // 启用
        entity.setDeleted(ExcelConstants.DEFAULT_DELETED); // 未删除
        // 如需保留行号到实体，可增加 @Transient 字段

        return entity;
    }

}
