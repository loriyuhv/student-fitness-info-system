package com.wsw.fitnesssystem.user.application.service;

import com.wsw.fitnesssystem.user.application.dto.port.UserAuthData;
import com.wsw.fitnesssystem.user.domain.model.User;
import com.wsw.fitnesssystem.user.domain.port.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author loriyuhv
 * @version 1.0 2026/8/26 14:20
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class UserAuthQueryService {

    private final UserRepository userRepository;

    public UserAuthData getAuthUserData(String username) {
        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null) {
            return null;
        }

        return UserAuthData.builder()
            .userId(user.getUserId())
            .campusId(user.getCampusId())
            .username(user.getUsername())
            .password(user.getPassword())
            .userType(user.getUserType().getCode())
            .status(user.getStatus().getCode())
            .build();
    }

    public UserAuthData getAuthUserData(long campusId, long userId) {
        User user = userRepository.findByCampusIdAndUserId(campusId, userId).orElse(null);

        if (user == null) {
            return null;
        }

        return UserAuthData.builder()
            .userId(user.getUserId())
            .campusId(user.getCampusId())
            .username(user.getUsername())
            .password(user.getPassword())
            .userType(user.getUserType().getCode())
            .status(user.getStatus().getCode())
            .build();
    }

}
