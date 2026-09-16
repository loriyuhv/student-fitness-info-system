package com.wsw.fitnesssystem.user.application.service.command;

import com.wsw.fitnesssystem.data_exchange.application.dto.command.UserImportCommand;
import com.wsw.fitnesssystem.user.application.config.UserApplicationProperties;
import com.wsw.fitnesssystem.user.application.dto.command.UserAccountProvisionCommand;
import com.wsw.fitnesssystem.user.application.dto.result.UserAccountProvisionResult;
import com.wsw.fitnesssystem.user.application.port.output.UserAccountProvisioningPort;
import com.wsw.fitnesssystem.user.domain.model.StudentProfile;
import com.wsw.fitnesssystem.user.domain.model.TeacherProfile;
import com.wsw.fitnesssystem.user.domain.model.UserProfile;
import com.wsw.fitnesssystem.user.domain.repository.StudentProfileRepository;
import com.wsw.fitnesssystem.user.domain.repository.TeacherProfileRepository;
import com.wsw.fitnesssystem.user.domain.repository.UserProfileRepository;
import com.wsw.fitnesssystem.user.domain.vb.Gender;
import com.wsw.fitnesssystem.user.domain.vb.Status;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 单行用户注册器（独立事务）。
 *
 * <p><b>职责：</b>把"一行导入数据"落成三张表：sys_user（通过 Port）、user_profile、
 * student_profile 或 teacher_profile。
 *
 * <p><b>事务：</b>本类方法自带 {@code @Transactional}，每次调用是独立事务。
 * 调用方 {@code UserRegisterServiceImpl} 不得加 {@code @Transactional}，
 * 以保证"一行失败不影响其他行"。
 *
 * <p><b>为什么独立成类：</b>Spring AOP 自调用不生效。若把 {@code registerOne} 和
 * {@code registerBatch} 放同一个类，内部调用不会走事务代理。
 *
 * @author loriyuhv
 * @version 1.0 2026/9/16 21:32
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserSingleRegistrar {

    private final UserAccountProvisioningPort userAccountProvisioningPort;
    private final UserProfileRepository userProfileRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private final UserApplicationProperties appProperties;

    /** 日期解析器：由 {@link #initDateFormatter()} 在 Bean 初始化时构建 */
    private DateTimeFormatter dateFormatter;

    @PostConstruct
    void initDateFormatter() {
        this.dateFormatter = DateTimeFormatter.ofPattern(
            appProperties.getDataImport().getDateFormat());
        log.debug("Date formatter initialized: pattern={}",
            appProperties.getDataImport().getDateFormat());
    }

    /**
     * 注册单行用户。
     *
     * @param data 导入数据（密码已加密）
     * @return 新生成的 userId
     */
    @Transactional(rollbackFor = Exception.class)
    public Long registerOne(UserImportCommand data) {
        // 1. 创建认证账号（sys_user）—— 通过 authentication 提供的 Port
        UserAccountProvisionResult account = userAccountProvisioningPort.createAccount(
            UserAccountProvisionCommand.builder()
                .campusId(data.getCampusId())
                .username(data.getUsername())
                .passwordHash(data.getPassword())
                .userType(data.getUserType())
                .source(0)           // 0=IMPORT
                .operatorId(null)    // 由 MetaObjectHandler 自动填充
                .build()
        );
        Long userId = account.getUserId();

        // 2. 创建用户档案（user_profile）
        saveUserProfile(userId, data);

        // 3. 按用户类型落扩展表
        if (data.isStudent()) {
            saveStudentProfile(userId, data);
        } else if (data.isTeacher()) {
            saveTeacherProfile(userId, data);
        }

        return userId;
    }

    private void saveUserProfile(Long userId, UserImportCommand data) {
        // 2.1 创建基础档案（nickname 必填）
        UserProfile profile = UserProfile.create(
            userId, data.getCampusId(), data.getNickname(), null);

        // 2.2 按需填充画像字段
        profile.updateGender(Gender.of(data.getGenderOrDefault()), null);

        if (data.getBirthDate() != null && !data.getBirthDate().isBlank()) {
            profile.updateBirthDate(parseDate(data.getBirthDate()), null);
        }
        if (data.getPhoneNumber() != null && !data.getPhoneNumber().isBlank()) {
            profile.updatePhoneNumber(data.getPhoneNumber(), null);
        }
        if (data.getEmail() != null && !data.getEmail().isBlank()) {
            profile.updateEmail(data.getEmail(), null);
        }
        if (data.getAvatarUrl() != null && !data.getAvatarUrl().isBlank()) {
            profile.updateAvatarUrl(data.getAvatarUrl(), null);
        }
        if (data.getAddress() != null && !data.getAddress().isBlank()) {
            profile.updateAddress(data.getAddress(), null);
        }

        userProfileRepository.save(profile);
    }

    private void saveStudentProfile(Long userId, UserImportCommand data) {
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
    }

    private void saveTeacherProfile(Long userId, UserImportCommand data) {
        TeacherProfile teacher = TeacherProfile.builder()
            .campusId(data.getCampusId())
            .userId(userId)
            .teacherNo(data.getTeacherNoOrDefault())
            .gender(Gender.of(data.getGenderOrDefault()))
            .status(Status.ENABLED)
            .build();
        teacherProfileRepository.save(teacher);
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr.trim(), dateFormatter);
        } catch (Exception e) {
            log.warn("Date parse failed: value={}", dateStr);
            return null;
        }
    }

}
