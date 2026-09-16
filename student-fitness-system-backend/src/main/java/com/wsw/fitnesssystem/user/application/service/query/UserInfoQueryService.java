package com.wsw.fitnesssystem.user.application.service.query;

import com.wsw.fitnesssystem.shared.domain.valueobject.Operator;
import com.wsw.fitnesssystem.shared.exception.BizException;
import com.wsw.fitnesssystem.shared.response.ResultCode;
import com.wsw.fitnesssystem.user.application.dto.result.UserAccountResult;
import com.wsw.fitnesssystem.user.application.dto.result.UserAuthorizationResult;
import com.wsw.fitnesssystem.user.application.dto.result.UserInfoResult;
import com.wsw.fitnesssystem.user.application.port.output.UserAccountQueryPort;
import com.wsw.fitnesssystem.user.application.port.output.UserAuthorizationQueryPort;
import com.wsw.fitnesssystem.user.domain.model.UserProfile;
import com.wsw.fitnesssystem.user.domain.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 用户信息查询服务（Application 层读操作）
 *
 * <p><b>职责：</b>
 * <ul>
 *   <li>聚合认证账号（authentication 提供的 Port）与用户档案（本模块 Repository）</li>
 *   <li>查询用户角色与权限（authorization 提供的 Port）</li>
 * </ul>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/26 16:04
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserInfoQueryService {

    private final UserAccountQueryPort userAccountQueryPort;
    private final UserProfileRepository userProfileRepository;
    private final UserAuthorizationQueryPort userAuthorizationQueryPort;

    /**
     * 获取当前操作用户的个人信息
     *
     * @param operator 当前登录用户身份
     * @return 用户信息结果对象
     * @throws BizException 当用户不存在时抛出
     */
    public UserInfoResult getCurrentUserInfo(Operator operator) {
        Long userId = operator.userId();
        Long campusId = operator.campusId();

        // 1. 认证账号（来自 authentication）
        UserAccountResult account = userAccountQueryPort
            .findByUserIdAndCampusId(userId, campusId)
            .orElseThrow(() -> new BizException(ResultCode.USER_NOT_FOUND));

        // 2. 用户档案（本模块）
        UserProfile profile = userProfileRepository.findByUserIdAndCampusId(userId, campusId)
            .orElseThrow(() -> new BizException(ResultCode.USER_NOT_FOUND));

        // 3. 授权（来自 authorization）
        UserAuthorizationResult authorizations = userAuthorizationQueryPort.findByUserIdAndCampusId(userId, campusId);

        return UserInfoResult.builder()
            .userId(account.getUserId())
            .campusId(account.getCampusId())
            .username(account.getUsername())
            .nickname(profile.getNickname())
            .phoneNumber(profile.getPhoneNumber())
            .email(profile.getEmail())
            .remark(profile.getRemark())
            .userType(account.getUserType())
            .roles(authorizations.getRoles())
            .permissions(authorizations.getPermissions())
            .build();
    }

}
