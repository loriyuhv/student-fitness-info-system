package com.wsw.fitnesssystem.auth.authentication.application.vo;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

/**
 * 获取当前登录用户信息 返回VO
 */
@Data
@Builder
public class UserInfoVO {
    private Long userId;
    private Long campusId;
    private String username;
    private String nickname;
    private Integer userType;
    private String phoneNumber;
    private String email;
    private String remark;
    private Set<String> permissions;
}
