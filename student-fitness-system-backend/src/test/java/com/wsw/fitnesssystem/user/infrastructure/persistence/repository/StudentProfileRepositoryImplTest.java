package com.wsw.fitnesssystem.user.infrastructure.persistence.repository;

import com.wsw.fitnesssystem.user.domain.model.StudentProfile;
import com.wsw.fitnesssystem.user.domain.port.StudentProfileRepository;
import com.wsw.fitnesssystem.user.domain.valueobject.Gender;
import com.wsw.fitnesssystem.user.domain.valueobject.Status;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author loriyuhv
 * @version 1.0 2026/9/2 10:04
 * @since 1.0
 */
@SpringBootTest
@Transactional
@DisplayName("StudentProfileRepository 单元测试")
class StudentProfileRepositoryImplTest {

    @Autowired
    private StudentProfileRepository repository;

    @Test
    @DisplayName("保存学生信息成功 - 新增")
    void shouldInsertStudentProfile_whenNoId() {
        // Given
        StudentProfile student = StudentProfile.builder()
            .campusId(1001L)
            .userId(1L)
            .studentNo("2021001")
            .classId(100L)
            .enrollYear(2021)
            .major("计算机科学与技术")
            .idCard("110101199001011234")
            .gender(Gender.MALE)
            .birthDate(LocalDate.of(2000, 1, 1))
            .familyAddress("北京市朝阳区")
            .status(Status.ENABLED)
            .build();

        // When
        repository.save(student);

        // Then
        assertThat(student.getStudentId()).isNotNull();
        Optional<StudentProfile> found = repository.findByUserIdAndCampusId(1L, 1001L);
        assertThat(found).isPresent();
        assertThat(found.get().getStudentNo()).isEqualTo("2021001");
        assertThat(found.get().getMajor()).isEqualTo("计算机科学与技术");
        assertThat(found.get().getCampusId()).isEqualTo(1001L);
    }

    @Test
    @DisplayName("保存学生信息成功 - 更新")
    void shouldUpdateStudentProfile_whenIdExists() {
        // Given
        StudentProfile student = StudentProfile.builder()
            .campusId(1002L)
            .userId(2L)
            .studentNo("2021002")
            .major("软件工程")
            .gender(Gender.MALE)
            .status(Status.ENABLED)
            .build();
        repository.save(student);
        Long studentId = student.getStudentId();

        // When
        student.setMajor("人工智能");
        repository.save(student);

        // Then
        Optional<StudentProfile> found = repository.findByUserIdAndCampusId(2L, 1002L);
        assertThat(found).isPresent();
        assertThat(found.get().getStudentId()).isEqualTo(studentId);
        assertThat(found.get().getMajor()).isEqualTo("人工智能");
    }

    @Test
    @DisplayName("根据用户ID和校区ID查询学生信息 - 存在")
    void shouldFindByUserIdAndCampusId_whenExists() {
        // Given
        StudentProfile student = StudentProfile.builder()
            .campusId(1003L)
            .userId(3L)
            .studentNo("2021003")
            .major("网络安全")
            .gender(Gender.MALE)
            .status(Status.ENABLED)
            .build();
        repository.save(student);

        // When
        Optional<StudentProfile> found = repository.findByUserIdAndCampusId(3L, 1003L);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(3L);
        assertThat(found.get().getCampusId()).isEqualTo(1003L);
        assertThat(found.get().getStudentNo()).isEqualTo("2021003");
    }

    @Test
    @DisplayName("根据学号查询学生信息 - 存在")
    void shouldFindByStudentNo_whenExists() {
        // Given
        StudentProfile student = StudentProfile.builder()
            .campusId(1004L)
            .userId(4L)
            .studentNo("2021004")
            .major("数据科学")
            .gender(Gender.MALE)
            .status(Status.ENABLED)
            .build();
        repository.save(student);

        // When
        Optional<StudentProfile> found = repository.findByStudentNo("2021004");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getStudentNo()).isEqualTo("2021004");
        assertThat(found.get().getMajor()).isEqualTo("数据科学");
    }

    @Test
    @DisplayName("根据用户ID和校区ID查询学生信息 - 不存在")
    void shouldReturnEmpty_whenUserIdAndCampusIdNotFound() {
        // When
        Optional<StudentProfile> found = repository.findByUserIdAndCampusId(999L, 1001L);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("根据学号查询学生信息 - 不存在")
    void shouldReturnEmpty_whenStudentNoNotFound() {
        // When
        Optional<StudentProfile> found = repository.findByStudentNo("NOT_EXIST");

        // Then
        assertThat(found).isEmpty();
    }

}