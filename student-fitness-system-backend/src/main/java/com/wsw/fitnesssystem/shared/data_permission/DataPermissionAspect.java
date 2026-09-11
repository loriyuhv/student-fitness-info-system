package com.wsw.fitnesssystem.shared.data_permission;

import com.wsw.fitnesssystem.shared.context.RequestContextHolder;
import com.wsw.fitnesssystem.shared.domain.valueobject.Operator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 数据权限上下文装配切面。
 * <p>请求进入 Service 层方法时，组装 {@link DataPermissionContext} 并放入 ThreadLocal。</p>
 * <p>请求结束后清理 ThreadLocal，避免线程池复用导致脏数据。</p>
 *
 * <p><b>装配时机：</b>所有标注了 {@code @DataPermission} 注解的方法，
 * 或统一对所有 Service 方法生效（推荐）。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/10 12:56
 * @since 1.0
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class DataPermissionAspect {

    private final DataScopeQueryPort dataScopeQueryPort;
    private final TeacherClassQueryPort teacherClassQueryPort;

    @Around("@annotation(org.springframework.transaction.annotation.Transactional) " +
        "|| execution(* com.wsw.fitnesssystem..application.service.query..*(..))")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        Operator operator = RequestContextHolder.getOperator();
        if (operator == null) {
            // 未登录场景（如系统任务），不组装 Context
            return pjp.proceed();
        }

        try {
            DataPermissionContext ctx = buildContext(operator);
            DataPermissionContextHolder.setContext(ctx);
            return pjp.proceed();
        } finally {
            DataPermissionContextHolder.clear();
        }
    }

    private DataPermissionContext buildContext(Operator operator) {
        DataScope scope = dataScopeQueryPort.queryMaxDataScope(
            operator.userId(), operator.campusId());

        Set<Long> allowedClassIds = (scope == DataScope.CLASS)
            ? teacherClassQueryPort.queryClassIdsByUserIdAndCampusId(operator.userId(), operator.campusId())
            : Set.of();

        return new DataPermissionContext(
            scope, operator.userId(), operator.campusId(), allowedClassIds
        );
    }

}
