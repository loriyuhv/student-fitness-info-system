package com.wsw.fitnesssystem.handle_excel.domain.service;

import com.wsw.fitnesssystem.handle_excel.application.dto.UserExcelDTO;
import com.wsw.fitnesssystem.handle_excel.domain.model.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author loriyuhv
 * @version 1.0 2026/3/26 15:56
 * @since 1.0
 */
@Service
public class UserImportDomainService {
    public List<User> validateAndConvert(List<UserExcelDTO> batch) {

        List<User> result = new ArrayList<>();

        for (UserExcelDTO dto : batch) {

            if (dto.getUsername() == null) {
                throw new RuntimeException("用户名不能为空");
            }

            User user = new User();
            user.setUsername(dto.getUsername());
            user.setPassword(dto.getPassword());
            user.setNickName(dto.getNickName());
            result.add(user);
        }

        return result;
    }
}
