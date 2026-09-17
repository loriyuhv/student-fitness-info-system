package com.wsw.fitnesssystem.shared.data_permission.domain;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/17 16:36
 * @since 1.0
 */
class DataPermissionRuleTest {

    @Test
    void selfAndCampus_shouldSupportSelfCollegeAll() {
        var rule = DataPermissionRule.selfAndCampus("user_id", "campus_id");
        assertThat(rule.supports(DataScope.SELF)).isTrue();
        assertThat(rule.supports(DataScope.COLLEGE)).isTrue();
        assertThat(rule.supports(DataScope.ALL)).isTrue();
        assertThat(rule.supports(DataScope.CLASS)).isFalse();   // ★ user 模块的关键
    }

    @Test
    void withClass_shouldSupportAllFour() {
        var rule = DataPermissionRule.withClass("user_id", "class_id", "campus_id");
        assertThat(rule.supports(DataScope.SELF)).isTrue();
        assertThat(rule.supports(DataScope.COLLEGE)).isTrue();
        assertThat(rule.supports(DataScope.CLASS)).isTrue();
        assertThat(rule.supports(DataScope.ALL)).isTrue();
    }

    @Test
    void ofExempt_shouldBeExempt() {
        var rule = DataPermissionRule.ofExempt();
        assertThat(rule.exempt()).isTrue();
        assertThat(rule.selfColumn()).isNull();
    }

    @Test
    void shouldFailWhenClassSupportedButNoColumn() {
        assertThatThrownBy(
            () -> new DataPermissionRule(
                EnumSet.of(DataScope.CLASS),
                "user_id", null, "campus_id", false)
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("CLASS supported but classColumn is null");
    }

}