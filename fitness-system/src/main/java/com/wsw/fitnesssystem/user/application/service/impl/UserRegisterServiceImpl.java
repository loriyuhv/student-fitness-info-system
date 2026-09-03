package com.wsw.fitnesssystem.user.application.service.impl;

import com.wsw.fitnesssystem.handle_excel.core.model.UserImportData;
import com.wsw.fitnesssystem.handle_excel.core.model.UserImportResult;
import com.wsw.fitnesssystem.user.application.service.UserRegisterService;
import com.wsw.fitnesssystem.user.domain.model.StudentProfile;
import com.wsw.fitnesssystem.user.domain.model.TeacherProfile;
import com.wsw.fitnesssystem.user.domain.model.User;
import com.wsw.fitnesssystem.user.domain.model.UserProfile;
import com.wsw.fitnesssystem.user.domain.port.StudentProfileRepository;
import com.wsw.fitnesssystem.user.domain.port.TeacherProfileRepository;
import com.wsw.fitnesssystem.user.domain.port.UserProfileRepository;
import com.wsw.fitnesssystem.user.domain.port.UserRepository;
import com.wsw.fitnesssystem.user.domain.valueobject.Gender;
import com.wsw.fitnesssystem.user.domain.valueobject.Status;
import com.wsw.fitnesssystem.user.domain.valueobject.UserType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/4 07:20
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserRegisterServiceImpl implements UserRegisterService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final TeacherProfileRepository teacherProfileRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long registerSingle(UserImportData data) {
        return doRegisterSingleUser(data);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<UserImportResult> registerBatch(
        List<UserImportData> dataList, Set<String> duplicateInFile, Set<String> existingInDb) {

        List<UserImportResult> results = new ArrayList<>(dataList.size());

        for (UserImportData data : dataList) {
            String username = data.getUsername();

            // 1. 文件中重复 → 直接失败，不查库
            if (duplicateInFile.contains(username)) {
                results.add(failResult(data, "用户名在文件中重复"));
                continue;
            }

            // 2. 数据库中已存在 → 失败
            if (existingInDb.contains(username)) {
                results.add(failResult(data, "用户名已存在"));
                continue;
            }

            // 3. 校验通过 → 创建 User 并保存
            try {
                Long userId = doRegisterSingleUser(data);
                results.add(successResult(data, userId));
                // log.debug("用户注册成功: username={}, userId={}", username, user.getUserId());
            } catch (DuplicateKeyException e) {
                // 精确捕获重复键异常
                results.add(failResult(data, "数据重复: 该记录已存在（用户名或唯一键冲突）"));
                log.error("用户注册失败: username={}, row={}", username, data.getRowIndex(), e);
            } catch (DataIntegrityViolationException e) {
                // int row = rowExtractor != null ? rowExtractor.apply(entity) : -1;
                // String cause = singleEx.getMostSpecificCause().getMessage();
                // 截断过长消息，只保留前50个字符
                String cause = e.getMostSpecificCause().getMessage();
                String friendlyMsg = cause.length() > 30 ? cause.substring(0, 30) + "..." : cause;
                // collector.addError(row, "数据格式异常: " + friendlyMsg);
                results.add(failResult(data, "数据格式异常: " + friendlyMsg));
                log.error("用户注册失败: username={}, row={}", username, data.getRowIndex(), e);
            } catch (Exception e) {
                results.add(failResult(data, "系统异常: " + e.getMessage()));
                log.error("用户注册失败: username={}, row={}", username, data.getRowIndex(), e);
            }
        }

        log.info("批量注册完成: 总数={}, 成功={}, 失败={}",
            dataList.size(),
            results.stream().filter(UserImportResult::isSuccess).count(),
            results.stream().filter(r -> !r.isSuccess()).count());

        return results;
    }


    /**
     * 保存单行用户数据（抽取为独立方法，便于事务管理）
     * @param data 数据
     * @return userId
     */
    private Long doRegisterSingleUser(UserImportData data) {
        // 1. 保存 User
        User user = User.builder()
            .campusId(data.getCampusId())
            .username(data.getUsername())
            .password(data.getPassword()) // 已加密
            .nickname(data.getNickname())
            .phoneNumber(data.getPhoneNumber())
            .email(data.getEmail())
            .userType(UserType.of(data.getUserType()))
            .status(Status.ENABLED)
            .build();

        userRepository.save(user);
        Long userId = user.getUserId();

        // 2. 创建并保存 UserProfile（所有用户类型都需要）
        UserProfile userProfile = UserProfile.builder()
            .userId(userId)
            .campusId(data.getCampusId())
            .gender(Gender.of(data.getGenderOrDefault()))
            .birthDate(parseDate(data.getBirthDate()))
            .avatarUrl(data.getAvatarUrl())
            .address(data.getAddress())
            .build();
        userProfileRepository.save(userProfile);

        // 3. 根据用户类型插入扩展表
        if (data.isStudent()) {
            StudentProfile student = StudentProfile.builder()
                .campusId(data.getCampusId())
                .userId(userId)
                .studentNo(data.getStudentNoOrDefault())
                .classId(data.getClassId())
                .enrollYear(data.getEnrollYear())
                .major(data.getMajor())
                .idCard(data.getIdCard())
                .gender(Gender.of(data.getGenderOrDefault()))
                .familyAddress(data.getFamilyAddress())
                .status(Status.ENABLED)
                .build();
            studentProfileRepository.save(student);
        } else if (data.isTeacher()) {
            TeacherProfile teacher = TeacherProfile.builder()
                .campusId(data.getCampusId())
                .userId(userId)
                .teacherNo(data.getTeacherNoOrDefault())
                .gender(Gender.of(data.getGenderOrDefault()))
                .status(Status.ENABLED)
                .build();
            teacherProfileRepository.save(teacher);
        }

        return userId;
    }


    // ==================== 辅助方法 ====================

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr.trim(), DATE_FORMATTER);
        } catch (Exception e) {
            log.warn("日期格式解析失败: {}", dateStr);
            return null;
        }
    }

    /**
     * 构建成功结果
     */
    private UserImportResult successResult(UserImportData data, Long userId) {
        return UserImportResult.builder()
            .rowIndex(data.getRowIndexOrDefault())
            .username(data.getUsername())
            .success(true)
            .userId(userId)
            .build();
    }

    /**
     * 构建失败结果
     */
    private UserImportResult failResult(UserImportData data, String reason) {
        return UserImportResult.builder()
            .rowIndex(data.getRowIndexOrDefault())
            .username(data.getUsername())
            .success(false)
            .errorMessage(reason)
            .rowData(buildRowData(data))
            .build();
    }

    private List<String> buildRowData(UserImportData data) {
        return List.of(
            Objects.toString(data.getCampusId(), ""),
            Objects.toString(data.getUsername(), ""),
            "******",  // 密码脱敏
            Objects.toString(data.getNickname(), ""),
            Objects.toString(data.getPhoneNumber(), ""),
            Objects.toString(data.getEmail(), ""),
            Objects.toString(data.getUserType(), ""),
            Objects.toString(data.getGender(), ""),
            Objects.toString(data.getBirthDate(), ""),
            Objects.toString(data.getAvatarUrl(), ""),
            Objects.toString(data.getAddress(), ""),
            Objects.toString(data.getStudentNo(), ""),
            Objects.toString(data.getClassId(), ""),
            Objects.toString(data.getEnrollYear(), ""),
            Objects.toString(data.getMajor(), ""),
            Objects.toString(data.getIdCard(), ""),
            Objects.toString(data.getFamilyAddress(), ""),
            Objects.toString(data.getTeacherNo(), "")
        );
    }

}
