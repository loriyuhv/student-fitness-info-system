package com.wsw.fitnesssystem.user.application.service;

import com.wsw.fitnesssystem.shared.domain.valueobject.Operator;
import com.wsw.fitnesssystem.shared.exception.BizException;
import com.wsw.fitnesssystem.shared.response.ResultCode;
import com.wsw.fitnesssystem.user.application.dto.result.UserInfoResult;
import com.wsw.fitnesssystem.user.domain.model.User;
import com.wsw.fitnesssystem.user.domain.port.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 用户信息查询服务（Application 层读操作）
 *
 * <p><b>职责：</b>
 * <ul>
 *   <li>提供用户个人资料的只读查询</li>
 *   <li>聚合用户核心信息（昵称、手机、邮箱等）</li>
 *   <li>通过 {@link UserRepository} 端口获取数据</li>
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

    private final UserRepository userRepository;

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

        User user = userRepository.findByCampusIdAndUserId(campusId, userId)
            .orElseThrow(() -> new BizException(ResultCode.USER_NOT_FOUND));

        return UserInfoResult.builder()
            .userId(user.getUserId())
            .campusId(user.getCampusId())
            .username(user.getUsername())
            .nickname(user.getNickname())
            .phoneNumber(user.getPhoneNumber())
            .email(user.getEmail())
            .remark(user.getRemark())
            .userType(user.getUserType().getCode())
            .build();
    }

}
