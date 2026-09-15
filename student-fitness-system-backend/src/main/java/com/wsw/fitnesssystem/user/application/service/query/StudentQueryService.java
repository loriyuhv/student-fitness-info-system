package com.wsw.fitnesssystem.user.application.service.query;

import com.wsw.fitnesssystem.shared.response.PageResult;
import com.wsw.fitnesssystem.user.application.dto.query.StudentListQuery;
import com.wsw.fitnesssystem.user.application.dto.result.StudentListItemResult;
import com.wsw.fitnesssystem.user.domain.model.StudentProfile;
import com.wsw.fitnesssystem.user.domain.model.User;
import com.wsw.fitnesssystem.user.domain.repository.StudentProfileRepository;
import com.wsw.fitnesssystem.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 学生列表查询服务。
 *
 * <p><b>当前阶段：</b>仅支持管理员查询全校区学生，不做身份分流。</p>
 *
 * <p><b>⚠️ 数据权限说明：</b>本服务当前未接入数据权限拦截器，
 * 通过显式传递 {@code campusId} 限定范围。
 * 待 {@code DataPermissionSqlBuilder} 补全后，移除 {@code campusId} 入参，
 * 由切面自动装配并拦截 SQL。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/14 18:22
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudentQueryService {

    private final StudentProfileRepository studentProfileRepository;
    private final UserRepository userRepository;

    /**
     * 分页查询学生列表（当前：管理员视角，全校区）。
     */
    public PageResult<StudentListItemResult> listStudents(StudentListQuery query) {
        PageResult<StudentProfile> profilePage = studentProfileRepository.page(
            query.getPageNum(), query.getPageSize()
        );

        if (profilePage.isEmpty()) {
            return PageResult.empty(query.getPageNum(), query.getPageSize());
        }

        // 2. 批量补全用户信息（避免 N+1）
        Set<Long> userIds = profilePage.getItems().stream()
            .map(StudentProfile::getUserId)
            .collect(Collectors.toSet());

        Map<Long, User> userMap = userRepository.findByIds(userIds).stream()
            .collect(Collectors.toMap(User::getUserId, Function.identity()));

        // 3. 组装结果
        List<StudentListItemResult> items = profilePage.getItems().stream()
            .map(sp -> assemble(sp, userMap.get(sp.getUserId())))
            .toList();

        return PageResult.of(items, profilePage.getTotal(),
            query.getPageNum(), query.getPageSize());
    }

    private StudentListItemResult assemble(StudentProfile profile, User user) {
        StudentListItemResult.StudentListItemResultBuilder builder = StudentListItemResult.builder()
            .studentId(profile.getStudentId())
            .userId(profile.getUserId())
            .studentNo(profile.getStudentNo())
            .classId(profile.getClassId())
            .enrollYear(profile.getEnrollYear())
            .major(profile.getMajor())
            .gender(profile.getGender() == null ? null : profile.getGender().getCode())
            .familyAddress(profile.getFamilyAddress());

        if (user != null) {
            builder.nickname(user.getNickname())
                .phoneNumber(user.getPhoneNumber())
                .email(user.getEmail());
        }
        return builder.build();
    }

}
