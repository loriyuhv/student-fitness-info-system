package com.wsw.fitnesssystem.user.infrastructure.adapter;

import com.wsw.fitnesssystem.shared.data_permission.TeacherClassQueryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 教师班级查询适配器。
 * <p>实现 shared 定义的 {@link TeacherClassQueryPort}。</p>
 *
 * <p><b>数据来源：</b>{@code teacher_profile} JOIN {@code teacher_class}。</p>
 * <p><b>建议：</b>结果可缓存在 Redis，教师班级分配变更时失效。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/11 12:32
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TeacherClassQueryAdapter implements TeacherClassQueryPort {

    @Override
    public Set<Long> queryClassIdsByUserIdAndCampusId(Long userId, Long campusId) {
        return Set.of();
    }

}
