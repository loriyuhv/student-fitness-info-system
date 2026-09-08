package com.wsw.fitnesssystem.user.infrastructure.persistence.repository;

import com.wsw.fitnesssystem.user.domain.model.TeacherProfile;
import com.wsw.fitnesssystem.user.domain.port.TeacherProfileRepository;
import com.wsw.fitnesssystem.user.domain.valueobject.Gender;
import com.wsw.fitnesssystem.user.domain.valueobject.Status;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/2 12:19
 * @since 1.0
 */
@SpringBootTest
@Transactional
@DisplayName("TeacherProfileRepository 单元测试")
class TeacherProfileRepositoryImplTest {

    @Autowired
    private TeacherProfileRepository repository;

    @Test
    @DisplayName("保存教师信息成功 - 新增")
    void shouldInsertTeacherProfile_whenNoId() {
        // Given
        TeacherProfile teacher = TeacherProfile.builder()
            .campusId(1001L)
            .userId(1L)
            .teacherNo("T2021001")
            .gender(Gender.MALE)
            .status(Status.ENABLED)
            .remark("高级教师")
            .build();

        // When
        repository.save(teacher);

        // Then
        assertThat(teacher.getTeacherId()).isNotNull();
        Optional<TeacherProfile> found = repository.findByUserIdAndCampusId(1L, 1001L);
        assertThat(found).isPresent();
        assertThat(found.get().getTeacherNo()).isEqualTo("T2021001");
        assertThat(found.get().getRemark()).isEqualTo("高级教师");
        assertThat(found.get().getCampusId()).isEqualTo(1001L);
    }

    @Test
    @DisplayName("保存教师信息成功 - 更新")
    void shouldUpdateTeacherProfile_whenIdExists() {
        // Given
        TeacherProfile teacher = TeacherProfile.builder()
            .campusId(1002L)
            .userId(2L)
            .teacherNo("T2021002")
            .gender(Gender.MALE)
            .status(Status.ENABLED)
            .remark("中级教师")
            .build();
        repository.save(teacher);
        Long teacherId = teacher.getTeacherId();

        // When
        teacher.setRemark("高级教师");
        repository.save(teacher);

        // Then
        Optional<TeacherProfile> found = repository.findByUserIdAndCampusId(2L, 1002L);
        assertThat(found).isPresent();
        assertThat(found.get().getTeacherId()).isEqualTo(teacherId);
        assertThat(found.get().getRemark()).isEqualTo("高级教师");
    }

    @Test
    @DisplayName("根据用户ID和校区ID查询教师信息 - 存在")
    void shouldFindByUserIdAndCampusId_whenExists() {
        // Given
        TeacherProfile teacher = TeacherProfile.builder()
            .campusId(1003L)
            .userId(3L)
            .teacherNo("T2021003")
            .gender(Gender.MALE)
            .status(Status.ENABLED)
            .build();
        repository.save(teacher);

        // When
        Optional<TeacherProfile> found = repository.findByUserIdAndCampusId(3L, 1003L);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(3L);
        assertThat(found.get().getCampusId()).isEqualTo(1003L);
        assertThat(found.get().getTeacherNo()).isEqualTo("T2021003");
    }

    @Test
    @DisplayName("根据工号查询教师信息 - 存在")
    void shouldFindByTeacherNo_whenExists() {
        // Given
        TeacherProfile teacher = TeacherProfile.builder()
            .campusId(1004L)
            .userId(4L)
            .teacherNo("T2021004")
            .gender(Gender.MALE)
            .status(Status.ENABLED)
            .build();
        repository.save(teacher);

        // When
        Optional<TeacherProfile> found = repository.findByTeacherNo("T2021004");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getTeacherNo()).isEqualTo("T2021004");
    }

    @Test
    @DisplayName("根据用户ID和校区ID查询教师信息 - 不存在")
    void shouldReturnEmpty_whenUserIdAndCampusIdNotFound() {
        // When
        Optional<TeacherProfile> found = repository.findByUserIdAndCampusId(999L, 1001L);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("根据工号查询教师信息 - 不存在")
    void shouldReturnEmpty_whenTeacherNoNotFound() {
        // When
        Optional<TeacherProfile> found = repository.findByTeacherNo("NOT_EXIST");

        // Then
        assertThat(found).isEmpty();
    }

}