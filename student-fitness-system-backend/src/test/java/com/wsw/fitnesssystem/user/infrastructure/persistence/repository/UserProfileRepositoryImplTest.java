package com.wsw.fitnesssystem.user.infrastructure.persistence.repository;

import com.wsw.fitnesssystem.user.domain.model.UserProfile;
import com.wsw.fitnesssystem.user.domain.port.UserProfileRepository;
import com.wsw.fitnesssystem.user.domain.valueobject.Gender;
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
 * @version 1.0 2026/9/2 09:21
 * @since 1.0
 */
@SpringBootTest
@Transactional
@DisplayName("UserProfileRepository 单元测试")
class UserProfileRepositoryImplTest {

    @Autowired
    private UserProfileRepository repository;

    @Test
    @DisplayName("保存用户档案成功 - 新增")
    void shouldInsertUserProfile_whenNoId() {
        // Given
        UserProfile profile = UserProfile.builder()
            .userId(1L)
            .campusId(1001L)
            .gender(Gender.MALE)
            .birthDate(LocalDate.of(2000, 1, 1))
            .avatarUrl("https://example.com/avatar.jpg")
            .address("北京市海淀区")
            .build();

        // When
        repository.save(profile);

        // Then
        assertThat(profile.getProfileId()).isNotNull();
        Optional<UserProfile> found = repository.findByUserIdAndCampusId(1L, 1001L);
        assertThat(found).isPresent();
        assertThat(found.get().getGender()).isEqualTo(Gender.MALE);
        assertThat(found.get().getAddress()).isEqualTo("北京市海淀区");
        assertThat(found.get().getCampusId()).isEqualTo(1001L);
    }

    @Test
    @DisplayName("保存用户档案成功 - 更新")
    void shouldUpdateUserProfile_whenIdExists() {
        // Given
        UserProfile profile = UserProfile.builder()
            .userId(2L)
            .campusId(1002L)
            .gender(Gender.MALE)
            .address("上海市浦东新区")
            .build();
        repository.save(profile);
        Long profileId = profile.getProfileId();

        // When
        profile.setAddress("上海市静安区");
        repository.save(profile);

        // Then
        Optional<UserProfile> found = repository.findByUserIdAndCampusId(2L, 1002L);
        assertThat(found).isPresent();
        assertThat(found.get().getProfileId()).isEqualTo(profileId);
        assertThat(found.get().getAddress()).isEqualTo("上海市静安区");
    }

    @Test
    @DisplayName("根据用户ID和校区ID查询用户档案 - 存在")
    void shouldFindByUserIdAndCampusId_whenExists() {
        // Given
        UserProfile profile = UserProfile.builder()
            .userId(3L)
            .campusId(1003L)
            .gender(Gender.MALE)
            .birthDate(LocalDate.of(1999, 5, 15))
            .build();
        repository.save(profile);

        // When
        Optional<UserProfile> found = repository.findByUserIdAndCampusId(3L, 1003L);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(3L);
        assertThat(found.get().getCampusId()).isEqualTo(1003L);
        assertThat(found.get().getGender()).isEqualTo(Gender.MALE);
    }

    @Test
    @DisplayName("根据用户ID和校区ID查询用户档案 - 不存在")
    void shouldReturnEmpty_whenUserIdAndCampusIdNotFound() {
        // When
        Optional<UserProfile> found = repository.findByUserIdAndCampusId(999L, 1001L);

        // Then
        assertThat(found).isEmpty();
    }

}