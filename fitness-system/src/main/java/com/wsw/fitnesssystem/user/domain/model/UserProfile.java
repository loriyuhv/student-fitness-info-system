package com.wsw.fitnesssystem.user.domain.model;

import com.wsw.fitnesssystem.user.domain.valueobject.Gender;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户档案（通用资料）
 *
 * @author loriyuhv
 * @version 1.0 2026/9/2 08:53
 * @since 1.0
 */
@Setter
@Getter
@Builder
public class UserProfile {

    private Long profileId;
    private Long userId;
    private Long campusId;
    private Gender gender;
    private LocalDate birthDate;
    private String avatarUrl;
    private String address;
    private String lastLoginIp;
    private LocalDateTime lastLoginTime;
    private boolean deleted;
    private Long createBy;
    private Long updateBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

}
