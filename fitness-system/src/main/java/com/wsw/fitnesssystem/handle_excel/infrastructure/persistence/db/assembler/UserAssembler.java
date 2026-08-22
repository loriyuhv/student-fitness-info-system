package com.wsw.fitnesssystem.handle_excel.infrastructure.persistence.db.assembler;

import com.wsw.fitnesssystem.handle_excel.domain.model.User;
import com.wsw.fitnesssystem.handle_excel.infrastructure.config.ExcelConstants;
import com.wsw.fitnesssystem.handle_excel.infrastructure.persistence.db.entity.SysUser;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 用户领域模型 → 持久化实体 组装器
 * <p>负责 DTO/Domain → Entity 的转换，包含技术细节（如密码加密、默认值填充）</p>
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
     * @param user 领域模型
     * @return 持久化实体
     */
    public SysUser toEntity(User user) {

        SysUser entity = new SysUser();

        entity.setCampusId(ExcelConstants.DEFAULT_CAMPUS_ID);
        entity.setUsername(user.getUsername());
        // 密码加密：如果 DomainService 已生成随机密码，这里直接加密
        String password = StringUtils.isEmpty(user.getPassword())
                ? ExcelConstants.DEFAULT_PASSWORD
                : user.getPassword();
        entity.setPassword(passwordEncoder.encode(password));
        entity.setNickname(user.getNickname());
        entity.setPhoneNumber(null);
        entity.setEmail(null);
        entity.setUserType(ExcelConstants.DEFAULT_USER_TYPE); // 学生
        entity.setStatus(ExcelConstants.DEFAULT_STATUS); // 启用
        entity.setDeleted(ExcelConstants.DEFAULT_DELETED); // 未删除

        return entity;
    }

    public List<SysUser> toEntityList(List<User> users) {
        return users.stream()
            .map(this::toEntity)
            .toList();
    }
}
