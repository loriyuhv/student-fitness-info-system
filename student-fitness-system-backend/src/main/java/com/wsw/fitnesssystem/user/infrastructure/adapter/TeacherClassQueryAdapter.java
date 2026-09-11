package com.wsw.fitnesssystem.user.infrastructure.adapter;

import com.wsw.fitnesssystem.shared.data_permission.TeacherClassQueryPort;
import com.wsw.fitnesssystem.user.domain.repository.TeacherClassRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 教师班级查询适配器（Infrastructure 层）。
 * <p>
 * 实现 shared 定义的 {@link TeacherClassQueryPort}，
 * 供数据权限拦截器在 {@code data_scope = CLASS} 场景下查询教师任教班级。
 * </p>
 *
 * <p><b>数据来源：</b>{@code teacher_profile} JOIN {@code teacher_class}，
 * 通过 Domain 层的 {@link TeacherClassRepository} 间接访问，
 * 不直接依赖 MyBatis Mapper。</p>
 *
 * <p><b>缓存建议：</b>本方法在每次数据权限过滤时被调用，属于热路径。
 * 后续可引入 Redis 缓存（Key: {@code user:teacher:classes:{campusId}:{userId}}），
 * 失效时机：教师班级分配变更、教师档案停用。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/11 12:32
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TeacherClassQueryAdapter implements TeacherClassQueryPort {

    private final TeacherClassRepository teacherClassRepository;

    @Override
    public Set<Long> queryClassIdsByUserIdAndCampusId(Long userId, Long campusId) {
        if (userId == null || campusId == null) {
            log.debug("Skip teacher class query for null userId or campusId");
            return Set.of();
        }

        Set<Long> classIds = teacherClassRepository.findClassIdsByUserIdAndCampusId(userId, campusId);
        log.debug("Teacher {} resolved {} class(es): {}", userId, classIds.size(), classIds);

        return classIds;
    }

}
