package com.wsw.fitnesssystem.handle_excel.infrastructure.persistence.db.assembler;

import com.wsw.fitnesssystem.handle_excel.domain.model.User;
import com.wsw.fitnesssystem.handle_excel.infrastructure.persistence.db.entity.SysUser;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author loriyuhv
 * @version 1.0 2026/3/26 16:36
 * @since 1.0
 */
@Component
@RequiredArgsConstructor
public class UserAssembler {
    private final PasswordEncoder passwordEncoder;

    public SysUser toEntity(User user) {

        SysUser entity = new SysUser();

        entity.setCampusId(1L);
        entity.setUsername(user.getUsername());
        // 密码加密：如果 DomainService 已生成随机密码，这里直接加密
        String password = StringUtils.isEmpty(user.getPassword()) ? "12345" : user.getPassword();
        entity.setPassword(passwordEncoder.encode(password));
        entity.setNickname(user.getNickname());
        entity.setPhoneNumber(null);
        entity.setEmail(null);
        entity.setUserType(2); // 学生
        entity.setStatus(1); // 启用
        entity.setDeleted(0); // 未删除

        return entity;
    }

    public List<SysUser> toEntityList(List<User> users) {
        return users.stream()
            .map(this::toEntity)
            .toList();
    }
}
