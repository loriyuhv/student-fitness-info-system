package com.wsw.fitnesssystem.shared.data_permission.application.registry;

import com.wsw.fitnesssystem.shared.data_permission.domain.DataScope;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/17 20:39
 * @since 1.0
 */
@SpringBootTest
@ActiveProfiles({"dev", "local"})
class DataPermissionRuleRegistryTest {

    @Autowired
    DataPermissionRuleRegistry registry;

    @Test
    void userProfile_shouldNotSupportClass() {
        var rule = registry.find("user_profile");
        assertThat(rule).isNotNull();
        assertThat(rule.supports(DataScope.CLASS)).isFalse();
    }

    @Test
    void fitnessRecord_shouldSupportClass() {
        var rule = registry.find("fitness_record");
        assertThat(rule.supports(DataScope.CLASS)).isTrue();
    }

    @Test
    void classInfo_shouldBeExempt() {
        var rule = registry.find("class_info");
        assertThat(rule.exempt()).isTrue();
    }

    @Test
    void unknownTable_shouldReturnNull() {
        assertThat(registry.find("unknown_table")).isNull();
    }

}