package com.wsw.fitnesssystem.handle_excel.infrastructure.persistence.db.assembler;

import com.wsw.fitnesssystem.handle_excel.domain.model.User;
import com.wsw.fitnesssystem.handle_excel.infrastructure.persistence.db.entity.SysUser;
import lombok.RequiredArgsConstructor;
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
        entity.setPassword(passwordEncoder.encode(user.getPassword()));
        entity.setNickName(user.getNickName());
        entity.setPhoneNumber(null);
        entity.setEmail(null);
        entity.setUserType(2);
        entity.setStatus(1);
        entity.setDeleted(0);

        return entity;
    }

    public List<SysUser> toEntityList(List<User> users) {
        return users.stream()
            .map(this::toEntity)
            .toList();
    }
}
