package com.wsw.fitnesssystem.shared.data_permission.domain;

import org.springframework.util.CollectionUtils;

import java.util.EnumSet;
import java.util.Set;

/**
 * 表的数据权限契约。
 *
 * <p><b>职责：</b>声明一张表允许哪些数据范围，以及每个范围对应的过滤字段。
 * 本类"支持哪些 scope"的显式声明，使拦截器能够区分以下三种情形：</p>
 * <ul>
 *   <li>契约支持当前 scope → 生成对应过滤条件；</li>
 *   <li>契约不支持当前 scope（如 {@code user_profile} 遇到 CLASS）→ <b>拒绝</b>；</li>
 *   <li>表显式豁免数据权限（如公共字典表）→ <b>放行</b>。</li>
 * </ul>
 *
 * <p><b>契约自洽性校验（构造期）：</b>声明支持某 scope 却未提供对应列时，构造器抛异常，
 * 使配置错误在 <b>应用启动期</b>暴露，而非等某次查询才崩。</p>
 *
 * @param supportedScopes 该表支持的 scope 集合，不可为空
 * @param selfColumn      SELF 过滤字段名（支持 SELF 时必非 null）
 * @param classColumn     CLASS 过滤字段名（支持 CLASS 时必非 null）
 * @param campusColumn    COLLEGE 过滤字段名（支持 COLLEGE 时必非 null）
 * @param exempt          true 表示豁免数据权限（不追加任何条件）
 * @author loriyuhv
 * @version 1.0 2026/9/17 12:54
 * @since 1.0
 */
public record DataPermissionRule(
    Set<DataScope> supportedScopes,
    String selfColumn,
    String classColumn,
    String campusColumn,
    boolean exempt
) {

    /**
     * 紧凑构造器：契约自洽性校验。
     * <p>任何"声明支持但字段缺失"或"豁免却声明字段"的矛盾都在此处暴露。</p>
     */
    public DataPermissionRule {
        if (CollectionUtils.isEmpty(supportedScopes)) {
            throw new IllegalArgumentException("supportedScopes must not be empty");
        }
        if (exempt && (selfColumn != null || classColumn != null || campusColumn != null)) {
            throw new IllegalArgumentException("exempt rule must not declare columns");
        }
        if (!exempt) {
            if (supportedScopes.contains(DataScope.SELF) && selfColumn == null) {
                throw new IllegalArgumentException("SELF supported but selfColumn is null");
            }
            if (supportedScopes.contains(DataScope.CLASS) && classColumn == null) {
                throw new IllegalArgumentException("CLASS supported but classColumn is null");
            }
            if (supportedScopes.contains(DataScope.COLLEGE) && campusColumn == null) {
                throw new IllegalArgumentException("COLLEGE supported but campusColumn is null");
            }
        }
    }

    /** 该表是否支持指定 scope。 */
    public boolean supports(DataScope scope) {
        return supportedScopes.contains(scope);
    }

    // ==================== 工厂方法 ====================

    /**
     * 显式豁免：不追加任何数据权限条件。
     * <p>典型场景：公共字典表、关联表等所有角色均可读的表。</p>
     */
    public static DataPermissionRule ofExempt() {
        return new DataPermissionRule(
            EnumSet.allOf(DataScope.class),
            null, null, null, true
        );
    }

    /**
     * 个人中心类表：支持 SELF / COLLEGE / ALL，<b>不支持 CLASS</b>。
     * <p>典型场景：{@code user_profile} / {@code teacher_profile} / {@code sys_user}。</p>
     */
    public static DataPermissionRule selfAndCampus(String selfColumn, String campusColumn) {
        return new DataPermissionRule(
            EnumSet.of(DataScope.SELF, DataScope.COLLEGE, DataScope.ALL),
            selfColumn, null, campusColumn, false
        );
    }

    /**
     * 业务数据表：支持 SELF / CLASS / COLLEGE / ALL 全维度。
     * <p>典型场景：{@code student_profile} / {@code fitness_record}。</p>
     */
    public static DataPermissionRule withClass(
        String selfColumn, String classColumn, String campusColumn) {
        return new DataPermissionRule(
            EnumSet.of(DataScope.SELF, DataScope.CLASS, DataScope.COLLEGE, DataScope.ALL),
            selfColumn, classColumn, campusColumn, false
        );
    }

}
