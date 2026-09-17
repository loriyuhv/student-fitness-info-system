package com.wsw.fitnesssystem.shared.data_permission.application.registry;

import com.wsw.fitnesssystem.shared.data_permission.domain.DataPermissionRule;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 数据权限契约注册表。
 *
 * <p><b>契约按模块差异声明：</b></p>
 * <ul>
 *   <li><b>user 模块表</b>（{@code user_profile} / {@code teacher_profile} / {@code sys_user}）：
 *       仅支持 SELF / COLLEGE / ALL。<b>教师无权查看他人档案</b>，CLASS 出现即拒绝。</li>
 *   <li><b>fitness 模块表</b>（{@code student_profile} / {@code fitness_record}）：
 *       额外支持 CLASS，允许教师查看本班学生数据。</li>
 *   <li><b>公共表</b>（{@code class_info} / {@code teacher_class}）：
 *       显式 {@code exempt}，所有角色可读。</li>
 * </ul>
 *
 * <p><b>Fail-safe 策略：</b>未注册的表 → 拦截器拒绝，不再静默放行。
 * 如需放行，必须显式声明 {@code exempt()}。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/17 13:08
 * @since 1.0
 */
@Component
public class DataPermissionRuleRegistry {

    private static final Map<String, DataPermissionRule> TABLE_RULES = Map.ofEntries(

        /* ==================== user 模块（不支持 CLASS） ==================== */

        // 用户画像：本人 / 本校区管理员 / 超级管理员
        Map.entry("user_profile",
            DataPermissionRule.selfAndCampus("user_id", "campus_id")),

        // 教师档案：教师只能看自己，管理员看本校区
        Map.entry("teacher_profile",
            DataPermissionRule.selfAndCampus("user_id", "campus_id")),

        // 系统用户：同上
        Map.entry("sys_user",
            DataPermissionRule.selfAndCampus("user_id", "campus_id")),

        /* ==================== fitness 模块（支持 CLASS） ==================== */

        // 学生档案：教师可看本班，管理员可看本校区，学生只看自己
        Map.entry("student_profile",
            DataPermissionRule.withClass("user_id", "class_id", "campus_id")),

        // 体测记录：SELF 维度按 student_user_id（被体测的学生），非 create_by
        Map.entry("fitness_record",
            DataPermissionRule
                .withClass("student_user_id", "class_id", "campus_id")
        ),

        /* ==================== 公共表（显式豁免） ==================== */

        // 班级信息：公共字典，所有角色可读（写操作由功能权限控制）
        Map.entry("class_info", DataPermissionRule.ofExempt()),

        // 教师-班级关联：公共关联表
        Map.entry("teacher_class", DataPermissionRule.ofExempt())
    );

    /**
     * 查询表的数据权限契约。
     *
     * @param tableName 表名（不区分大小写）
     * @return 契约；未注册的表返回 {@code null}，调用方按 fail-safe 策略拒绝
     */
    public DataPermissionRule find(String tableName) {
        if (tableName == null) return null;
        return TABLE_RULES.get(tableName.toLowerCase());
    }

}
