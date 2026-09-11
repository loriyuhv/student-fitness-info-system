package com.wsw.fitnesssystem.shared.data_permission;

import java.util.Set;

/**
 * 教师班级查询端口。
 * <p>由 user 模块实现，返回教师关联的班级 ID 列表。</p>
 * <p>仅在 {@code data_scope = CLASS} 时被调用。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/10 12:49
 * @since 1.0
 */
public interface TeacherClassQueryPort {

    /**
     * 查询教师任教的所有班级 ID。
     *
     * @param userId 用户 ID（对应 sys_user.user_id）
     * @param campusId 校区 ID（对应 sys_user.campus_id）
     * @return 班级 ID 集合（若用户非教师或无任教班级，返回空集合）
     */
    Set<Long> queryClassIdsByUserIdAndCampusId(Long userId, Long campusId);

}
