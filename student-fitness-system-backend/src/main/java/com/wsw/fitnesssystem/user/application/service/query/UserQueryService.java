package com.wsw.fitnesssystem.user.application.service.query;

import com.wsw.fitnesssystem.shared.domain.pagination.PageSlice;
import com.wsw.fitnesssystem.user.application.dto.query.UserListQuery;
import com.wsw.fitnesssystem.user.application.dto.result.UserAccountResult;
import com.wsw.fitnesssystem.user.application.dto.result.UserListItemResult;
import com.wsw.fitnesssystem.user.application.port.output.UserAccountQueryPort;
import com.wsw.fitnesssystem.user.domain.model.StudentProfile;
import com.wsw.fitnesssystem.user.domain.model.TeacherProfile;
import com.wsw.fitnesssystem.user.domain.model.UserProfile;
import com.wsw.fitnesssystem.user.domain.repository.StudentProfileRepository;
import com.wsw.fitnesssystem.user.domain.repository.TeacherProfileRepository;
import com.wsw.fitnesssystem.user.domain.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用户列表查询服务（C1）。
 *
 * <p><b>数据来源：</b>以 {@code sys_user} 为主表分页，按 userType 分组批量补扩展表。</p>
 * <p><b>数据权限：</b>由拦截器自动收敛（COLLEGE / ALL）。本方法<b>不显式</b>过滤 campusId。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/14 18:22
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserQueryService {

    private static final int USER_TYPE_TEACHER = 1;
    private static final int USER_TYPE_STUDENT = 2;

    private final UserAccountQueryPort userAccountQueryPort;
    private final UserProfileRepository userProfileRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final TeacherProfileRepository teacherProfileRepository;

    /**
     * C1：分页查询用户列表。
     *
     * <p><b>三步查询：</b></p>
     * <ol>
     *   <li>从 sys_user 分页（拦截器自动追加 campus_id 条件）</li>
     *   <li>按 userType 分组，批量补 student_profile / teacher_profile</li>
     *   <li>批量补 user_profile（画像）</li>
     * </ol>
     *
     * @param query 查询条件
     * @return 分页结果（{@link PageSlice}，与 domain 层共用的分页结果原语）
     */
    public PageSlice<UserListItemResult> listUsers(UserListQuery query) {
        // 1. 主表分页
        PageSlice<UserAccountResult> accountPage = userAccountQueryPort.page(query);
        if (accountPage.isEmpty()) {
            return PageSlice.empty(query.pageNum(), query.pageSize());
        }

        List<UserAccountResult> accounts = accountPage.items();

        // 2. 按 userType 分组收集 userId
        Set<Long> allUserIds = accounts.stream()
            .map(UserAccountResult::getUserId)
            .collect(Collectors.toSet());

        Set<Long> studentUserIds = accounts.stream()
            .filter(a -> USER_TYPE_STUDENT == a.getUserType())
            .map(UserAccountResult::getUserId)
            .collect(Collectors.toSet());

        Set<Long> teacherUserIds = accounts.stream()
            .filter(a -> USER_TYPE_TEACHER == a.getUserType())
            .map(UserAccountResult::getUserId)
            .collect(Collectors.toSet());

        // 3. 批量补三张表（每个查询独立走拦截器）
        Map<Long, UserProfile> profileMap = toMap(
            userProfileRepository.findByUserIds(allUserIds),
            UserProfile::getUserId);

        Map<Long, StudentProfile> studentMap = studentUserIds.isEmpty()
            ? Collections.emptyMap()
            : toMap(studentProfileRepository.findByUserIds(studentUserIds),
            StudentProfile::getUserId);

        Map<Long, TeacherProfile> teacherMap = teacherUserIds.isEmpty()
            ? Collections.emptyMap()
            : toMap(teacherProfileRepository.findByUserIds(teacherUserIds),
            TeacherProfile::getUserId);

        // 4. 组装
        List<UserListItemResult> items = accounts.stream()
            .map(acc -> assemble(acc, profileMap, studentMap, teacherMap))
            .toList();

        return PageSlice.of(items, accountPage.total(),
            query.pageNum(), query.pageSize());
    }

    private UserListItemResult assemble(
        UserAccountResult acc,
        Map<Long, UserProfile> profileMap,
        Map<Long, StudentProfile> studentMap,
        Map<Long, TeacherProfile> teacherMap) {

        UserListItemResult.UserListItemResultBuilder b = UserListItemResult.builder()
            .userId(acc.getUserId())
            .campusId(acc.getCampusId())
            .username(acc.getUsername())
            .userType(acc.getUserType())
            .status(acc.getStatus());

        // 通用画像
        UserProfile profile = profileMap.get(acc.getUserId());
        if (profile != null) {
            b.nickname(profile.getNickname())
                .phoneNumber(profile.getPhoneNumber())
                .email(profile.getEmail());
        }

        // 学生扩展（仅 userType=2）
        if (acc.getUserType() != null && acc.getUserType() == USER_TYPE_STUDENT) {
            StudentProfile sp = studentMap.get(acc.getUserId());
            if (sp != null) {
                b.studentNo(sp.getStudentNo())
                    .classId(sp.getClassId())
                    .major(sp.getMajor());
            }
        }

        // 教师扩展（仅 userType=1）
        if (acc.getUserType() != null && acc.getUserType() == USER_TYPE_TEACHER) {
            TeacherProfile tp = teacherMap.get(acc.getUserId());
            if (tp != null) {
                b.teacherNo(tp.getTeacherNo());
            }
        }

        return b.build();
    }

    private <T> Map<Long, T> toMap(List<T> list, Function<T, Long> keyFn) {
        return list.stream().collect(Collectors.toMap(keyFn, Function.identity()));
    }

}
