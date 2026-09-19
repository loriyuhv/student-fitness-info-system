package com.wsw.fitnesssystem.user.application.service.query;

import com.wsw.fitnesssystem.shared.domain.vb.Operator;
import com.wsw.fitnesssystem.shared.application.exception.BizException;
import com.wsw.fitnesssystem.shared.interfaces.web.response.ResultCode;
import com.wsw.fitnesssystem.user.application.dto.result.AdminUserDetailResult;
import com.wsw.fitnesssystem.user.application.dto.result.UserAccountResult;
import com.wsw.fitnesssystem.user.application.dto.result.UserAuthorizationResult;
import com.wsw.fitnesssystem.user.application.dto.result.UserInfoResult;
import com.wsw.fitnesssystem.user.application.port.output.UserAccountQueryPort;
import com.wsw.fitnesssystem.user.application.port.output.UserAuthorizationQueryPort;
import com.wsw.fitnesssystem.user.domain.model.StudentProfile;
import com.wsw.fitnesssystem.user.domain.model.TeacherProfile;
import com.wsw.fitnesssystem.user.domain.model.UserProfile;
import com.wsw.fitnesssystem.user.domain.repository.StudentProfileRepository;
import com.wsw.fitnesssystem.user.domain.repository.TeacherProfileRepository;
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
    private final StudentProfileRepository studentProfileRepository;
    private final TeacherProfileRepository teacherProfileRepository;
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

    /**
     * C2：管理员查询用户详情。
     *
     * <p><b>数据范围：</b>由拦截器自动收敛；
     * 校区管理员看不到其他校区的用户（返回 404）。</p>
     * <p><b>多次查询的必要性：</b>sys_user / user_profile / student_profile(或 teacher_profile)
     * 各自独立过拦截器，任一失败都不会泄漏数据。</p>
     */
    public AdminUserDetailResult getUserDetailForAdmin(Long userId) {
        // 1. 认证账号（IAM 提供）
        UserAccountResult account = userAccountQueryPort.findByUserId(userId)
            .orElseThrow(() -> new BizException(ResultCode.USER_NOT_FOUND));

        // 2. 通用画像（可能为空）
        UserProfile profile = userProfileRepository.findByUserId(userId).orElse(null);

        // 3. 扩展表（按 userType 二选一）
        AdminUserDetailResult.StudentExtension studentExt = null;
        AdminUserDetailResult.TeacherExtension teacherExt = null;

        Integer userType = account.getUserType();
        if (userType != null && userType == 2) {
            StudentProfile student = studentProfileRepository.findByUserId(userId).orElse(null);
            if (student != null) {
                studentExt = AdminUserDetailResult.StudentExtension.builder()
                    .studentNo(student.getStudentNo())
                    .classId(student.getClassId())
                    .enrollYear(student.getEnrollYear())
                    .major(student.getMajor())
                    .idCardMasked(maskIdCard(student.getIdCard()))
                    .familyAddress(student.getFamilyAddress())
                    .build();
            }
        } else if (userType != null && userType == 1) {
            TeacherProfile teacher = teacherProfileRepository.findByUserId(userId).orElse(null);
            if (teacher != null) {
                teacherExt = AdminUserDetailResult.TeacherExtension.builder()
                    .teacherNo(teacher.getTeacherNo())
                    .remark(teacher.getRemark())
                    .build();
            }
        }

        return AdminUserDetailResult.builder()
            .userId(account.getUserId())
            .campusId(account.getCampusId())
            .username(account.getUsername())
            .userType(account.getUserType())
            .source(account.getSource())
            .status(account.getStatus())
            .nickname(profile != null ? profile.getNickname() : null)
            .phoneNumber(profile != null ? profile.getPhoneNumber() : null)
            .email(profile != null ? profile.getEmail() : null)
            .gender(profile != null && profile.getGender() != null
                ? profile.getGender().getCode() : null)
            .birthDate(profile != null ? profile.getBirthDate() : null)
            .avatarUrl(profile != null ? profile.getAvatarUrl() : null)
            .address(profile != null ? profile.getAddress() : null)
            .remark(profile != null ? profile.getRemark() : null)
            .student(studentExt)
            .teacher(teacherExt)
            .build();
    }

    /**
     * 身份证脱敏：前 6 位 + 后 4 位，中间 8 位用 * 填充。
     * 长度不足 10 位时返回原值（异常数据兜底）。
     */
    private String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 10) {
            return idCard;
        }
        return idCard.substring(0, 6) + "********" + idCard.substring(idCard.length() - 4);
    }

}
