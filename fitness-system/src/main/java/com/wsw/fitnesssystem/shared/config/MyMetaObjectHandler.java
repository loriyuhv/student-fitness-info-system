package com.wsw.fitnesssystem.shared.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.wsw.fitnesssystem.shared.context.RequestContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 元数据自动填充处理器
 * <p>自动填充 createBy / createTime / updateBy / updateTime</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/1 03:27
 * @since 1.0
 */
@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入时填充
     *
     * @param metaObject 元对象
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        Long currentUserId = getCurrentUserId();
        LocalDateTime now = LocalDateTime.now();

        // 创建时间
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        // 更新时间
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
        // 创建人
        this.strictInsertFill(metaObject, "createBy", Long.class, currentUserId);
        // 更新人
        this.strictInsertFill(metaObject, "updateBy", Long.class, currentUserId);
    }

    /**
     * 更新时填充
     * @param metaObject 元对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        Long currentUserId = getCurrentUserId();
        LocalDateTime now = LocalDateTime.now();

        // 更新时间
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, now);
        // 更新人
        this.strictUpdateFill(metaObject, "updateBy", Long.class, currentUserId);
    }

    /**
     * 获取当前登录用户 ID
     * <p>如果无法获取（如系统初始化、定时任务等），返回 null</p>
     */
    private Long getCurrentUserId() {
        try {
            return RequestContextHolder.getRequiredOperator().userId();
        } catch (Exception e) {
            // 系统启动、定时任务、无请求上下文时返回 null
            // 不会影响插入操作（数据库字段允许 NULL）
            log.debug("Unable to get current user ID for audit fill, context may not be available");
            return null;
        }
    }

}
