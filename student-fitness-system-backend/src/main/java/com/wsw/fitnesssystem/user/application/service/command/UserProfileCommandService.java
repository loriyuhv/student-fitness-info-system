package com.wsw.fitnesssystem.user.application.service.command;

import com.wsw.fitnesssystem.shared.data_permission.DataPermissionScope;
import com.wsw.fitnesssystem.shared.data_permission.domain.DataScope;
import com.wsw.fitnesssystem.shared.domain.exception.DomainConflictException;
import com.wsw.fitnesssystem.shared.domain.exception.DomainValidationException;
import com.wsw.fitnesssystem.shared.domain.vb.Operator;
import com.wsw.fitnesssystem.shared.application.exception.BizException;
import com.wsw.fitnesssystem.shared.interfaces.web.response.ResultCode;
import com.wsw.fitnesssystem.user.application.dto.command.UpdateMyProfileCommand;
import com.wsw.fitnesssystem.user.domain.model.UserProfile;
import com.wsw.fitnesssystem.user.domain.repository.UserProfileRepository;
import com.wsw.fitnesssystem.user.domain.vb.Gender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户画像命令服务。
 *
 * <p><b>职责：</b>处理"修改个人画像"等写操作。</p>
 * <p><b>数据范围：</b>通过 {@code operator.userId()} + {@code operator.campusId()}
 * 双重限定，只可能改到自己。</p>
 * <p><b>字段白名单：</b>由 domain 方法逐个控制，任何非法字段都会被拒绝。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/18 08:56
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileCommandService {

    private final UserProfileRepository userProfileRepository;

    /**
     * A2：修改本人画像。
     *
     * <p><b>部分更新语义：</b>请求中为 null 的字段保持不变，非 null 的字段按
     * {@link UserProfile} 的领域方法逐字段更新。</p>
     *
     * <p><b>唯一约束：</b>手机号、邮箱有全局唯一索引；如果新值与他人冲突，
     * 由数据库抛出 {@code DuplicateKeyException}，由全局异常处理器统一转换。</p>
     */
    @DataPermissionScope(DataScope.SELF)
    @Transactional(rollbackFor = Exception.class)
    public void updateMyProfile(Operator operator, UpdateMyProfileCommand command) {
        UserProfile profile = userProfileRepository
            .findByUserIdAndCampusId(operator.userId(), operator.campusId())
            .orElseThrow(() -> new BizException(ResultCode.USER_NOT_FOUND));

        try {
            // 逐字段更新，null 表示"不修改"
            if (command.getNickname() != null) {
                profile.updateNickname(command.getNickname(), operator.userId());
            }
            if (command.getPhoneNumber() != null) {
                profile.updatePhoneNumber(command.getPhoneNumber(), operator.userId());
            }
            if (command.getEmail() != null) {
                profile.updateEmail(command.getEmail(), operator.userId());
            }
            if (command.getGender() != null) {
                profile.updateGender(Gender.of(command.getGender()), operator.userId());
            }
            if (command.getBirthDate() != null) {
                profile.updateBirthDate(command.getBirthDate(), operator.userId());
            }
            if (command.getAddress() != null) {
                profile.updateAddress(command.getAddress(), operator.userId());
            }
            if (command.getAvatarUrl() != null) {
                profile.updateAvatarUrl(command.getAvatarUrl(), operator.userId());
            }

            userProfileRepository.save(profile);
        } catch (DomainValidationException e) {
            throw new BizException(ResultCode.PARAM_INVALID, e.getMessage(), e);
        } catch (DomainConflictException e) {
            throw new BizException(ResultCode.DATA_ALREADY_EXISTS, e.getMessage(), e);
        }
        log.info("[A2] Profile updated: userId={}", operator.userId());
    }

}
