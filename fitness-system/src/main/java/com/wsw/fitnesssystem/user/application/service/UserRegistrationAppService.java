package com.wsw.fitnesssystem.user.application.service;

import com.wsw.fitnesssystem.handle_excel.core.model.UserImportData;
import com.wsw.fitnesssystem.handle_excel.core.model.UserImportResult;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户注册应用服务
 * <p>处理用户注册相关的业务用例，包括 Excel 批量导入注册</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/1 13:13
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserRegistrationAppService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 批量注册用户（用于 Excel 导入）
     * <p>处理流程：</p>
     * <ol>
     *   <li>按用户名分组，标记文件中重复的行（只保留第一条，其余直接失败）</li>
     *   <li>批量查数据库，获取已存在的用户名</li>
     *   <li>逐行处理：文件中重复 → 失败；数据库中已存在 → 失败；通过 → 创建并保存</li>
     * </ol>
     *
     * @param dataList 用户导入数据列表
     * @return 每条数据的处理结果（含行号、成功/失败状态、错误原因）
     */
    @Transactional(rollbackFor = Exception.class)
    public List<UserImportResult> batchRegister(List<UserImportData> dataList) {

        if (dataList == null || dataList.isEmpty()) {
            return List.of();
        }

        List<UserImportResult> results = new ArrayList<>(dataList.size());

        // ==================== 第一步：检测文件中重复的用户名 ====================
        // 按用户名分组，用于识别重复
        Map<String, List<UserImportData>> groupedByUsername = dataList.stream()
            .collect(Collectors.groupingBy(UserImportData::getUsername));

        // 收集每个用户名第一次出现的数据（用于后续处理）
        List<UserImportData> firstOccurrenceList = new ArrayList<>();
        // 记录文件中重复的用户名（仅保留除第一条外的其余行）
        Set<String> duplicateInFile = new HashSet<>();

        for (Map.Entry<String, List<UserImportData>> entry : groupedByUsername.entrySet()) {
            List<UserImportData> list = entry.getValue();
            // 第一条保留（用于后续处理）
            firstOccurrenceList.add(list.get(0));
            // 其余行标记为文件中重复
            if (list.size() > 1) {
                for (int i = 1; i < list.size(); i++) {
                    duplicateInFile.add(list.get(i).getUsername());
                }
            }
        }

        // ==================== 第二步：批量查数据库（只查每个用户名第一次出现的） ====================
        List<String> usernamesToCheck = firstOccurrenceList.stream()
            .map(UserImportData::getUsername)
            .toList();
        Set<String> existingInDb = userRepository.findExistingUsernames(usernamesToCheck);

        // ==================== 第三步：逐行处理 ====================
        for (UserImportData data : dataList) {
            String username = data.getUsername();

            // 3.1 文件中重复 → 直接失败，不查库
            if (duplicateInFile.contains(username)) {
                results.add(failResult(data, "用户名在文件中重复"));
                continue;
            }

            // 3.2 数据库中已存在 → 失败
            if (existingInDb.contains(username)) {
                results.add(failResult(data, "用户名已存在"));
                continue;
            }

            // 3.3 校验通过 → 创建 User 并保存
            try {
                User user = User.builder()
                    .campusId(data.getCampusId())
                    .username(username)
                    .password(data.getPassword())
                    .nickname(data.getNickname())
                    .phoneNumber(data.getPhoneNumber())
                    .email(data.getEmail())
                    .userType(UserType.of(data.getUserType()))
                    .status(Status.ENABLED)
                    .build();

                userRepository.save(user);
                Long userId = user.getUserId();

                // 3.3.2 创建并保存 UserProfile（所有用户类型都需要）
                UserProfile userProfile = UserProfile.builder()
                    .userId(userId)
                    .campusId(data.getCampusId())
                    .gender(Gender.of(data.getGenderOrDefault()))
                    .birthDate(parseDate(data.getBirthDate()))
                    .avatarUrl(data.getAvatarUrl())
                    .address(data.getAddress())
                    .build();
                userProfileRepository.save(userProfile);

                // 3.3.3 根据用户类型插入扩展表
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

                results.add(successResult(data, user.getUserId()));
                log.debug("用户注册成功: username={}, userId={}", username, user.getUserId());

            } catch (Exception e) {
                log.error("用户注册失败: username={}, row={}", username, data.getRowIndex(), e);
                results.add(failResult(data, "系统异常: " + e.getMessage()));
            }
        }

        log.info("批量注册完成: 总数={}, 成功={}, 失败={}",
            dataList.size(),
            results.stream().filter(UserImportResult::isSuccess).count(),
            results.stream().filter(r -> !r.isSuccess()).count());

        return results;
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
            .build();
    }

}
