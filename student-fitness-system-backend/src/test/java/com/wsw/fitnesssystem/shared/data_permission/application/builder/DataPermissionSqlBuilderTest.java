package com.wsw.fitnesssystem.shared.data_permission.application.builder;

import com.wsw.fitnesssystem.shared.data_permission.application.registry.DataPermissionRuleRegistry;
import com.wsw.fitnesssystem.shared.data_permission.domain.DataPermissionContext;
import com.wsw.fitnesssystem.shared.data_permission.domain.DataPermissionRule;
import com.wsw.fitnesssystem.shared.data_permission.domain.DataScope;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/17 19:49
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
class DataPermissionSqlBuilderTest {

    @Mock
    DataPermissionRuleRegistry ruleRegistry;

    @InjectMocks
    DataPermissionSqlBuilder builder;

    @Test
    void ctxNull_shouldReturnOriginal() {
        Expression where = new Column("id");
        assertThat(builder.build(where, null, "user_profile")).isSameAs(where);
    }

    @Test
    void allScope_shouldReturnOriginal() {
        var ctx = new DataPermissionContext(DataScope.ALL, 1L, 1001L, Set.of());
        Expression where = new Column("id");
        assertThat(builder.build(where, ctx, "user_profile")).isSameAs(where);
    }

    @Test
    void unregisteredTable_shouldDeny() {
        when(ruleRegistry.find("unknown_table")).thenReturn(null);
        var ctx = new DataPermissionContext(DataScope.SELF, 1L, 1001L, Set.of());
        Expression result = builder.build(null, ctx, "unknown_table");
        assertThat(result.toString()).isEqualTo("1 = 0");
    }

    @Test
    void exemptTable_shouldReturnOriginal() {
        when(ruleRegistry.find("class_info")).thenReturn(DataPermissionRule.ofExempt());
        var ctx = new DataPermissionContext(DataScope.SELF, 1L, 1001L, Set.of());
        Expression where = new Column("id");
        assertThat(builder.build(where, ctx, "class_info")).isSameAs(where);
    }

    @Test
    void contractViolation_userProfileWithClass_shouldDeny() {
        when(ruleRegistry.find("user_profile"))
            .thenReturn(DataPermissionRule.selfAndCampus("user_id", "campus_id"));
        var ctx = new DataPermissionContext(
            DataScope.CLASS, 1L, 1001L, Set.of(10L));
        Expression result = builder.build(null, ctx, "user_profile");
        assertThat(result.toString()).isEqualTo("1 = 0");   // ★ 关键
    }

    @Test
    void selfScope_shouldAppendUserCondition() {
        when(ruleRegistry.find("user_profile"))
            .thenReturn(DataPermissionRule.selfAndCampus("user_id", "campus_id"));
        var ctx = new DataPermissionContext(
            DataScope.SELF, 100L, 1001L, Set.of());
        Expression result = builder.build(null, ctx, "user_profile");
        assertThat(result.toString()).isEqualTo("user_id = 100");
    }

    @Test
    void collegeScope_shouldAppendCampusCondition() {
        when(ruleRegistry.find("user_profile"))
            .thenReturn(DataPermissionRule.selfAndCampus("user_id", "campus_id"));
        var ctx = new DataPermissionContext(DataScope.COLLEGE, 1L, 1001L, Set.of());
        Expression result = builder.build(null, ctx, "user_profile");
        assertThat(result.toString()).isEqualTo("campus_id = 1001");
    }

    @Test
    void classScopeWithEmptyClasses_shouldDeny() {
        when(ruleRegistry.find("fitness_record"))
            .thenReturn(DataPermissionRule.withClass(
                "student_user_id", "class_id", "campus_id"));
        var ctx = new DataPermissionContext(DataScope.CLASS, 1L, 1001L, Set.of());
        Expression result = builder.build(null, ctx, "fitness_record");
        assertThat(result.toString()).isEqualTo("1 = 0");
    }

    @Test
    void classScope_shouldAppendInCondition() {
        when(ruleRegistry.find("fitness_record"))
            .thenReturn(DataPermissionRule.withClass(
                "student_user_id", "class_id", "campus_id"));
        // Set<Long> classSet = Set.of(10L, 20L); // 注意：这个是无序的，不能保证顺序
        // LinkedHashSet可以保证顺序
        Set<Long> classIds = Stream.of(10L, 20L)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        var ctx = new DataPermissionContext(
            DataScope.CLASS, 1L, 1001L, classIds);
        Expression result = builder.build(null, ctx, "fitness_record");
        assertThat(result.toString()).contains("class_id IN (10, 20)");
    }

    @Test
    void whereNotNull_shouldAndCombine() {
        when(ruleRegistry.find("user_profile"))
            .thenReturn(DataPermissionRule.selfAndCampus("user_id", "campus_id"));
        var ctx = new DataPermissionContext(
            DataScope.SELF, 100L, 1001L, Set.of());
        Expression where = new Column("deleted");
        Expression result = builder.build(where, ctx, "user_profile");
        assertThat(result.toString()).contains("deleted").contains("user_id = 100");
    }

}