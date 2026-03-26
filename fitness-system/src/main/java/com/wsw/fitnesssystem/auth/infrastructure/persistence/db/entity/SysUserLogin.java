package com.wsw.fitnesssystem.auth.infrastructure.persistence.db.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户登录审计表实体
 * 用途：
 * 1. 记录登录成功 / 失败行为
 * 2. 支撑安全审计、风控分析
 * 3. 支撑失败次数统计、账号/IP 锁定
 * 表：sys_user_login
 *
 * @author loriyuhv
 * @version 1.0 2026/1/11 16:53
 * @since 1.0
 */
@Data
@TableName("sys_user_login")
public class SysUserLogin implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 登录记录ID（主键）
     */
    @TableId(type = IdType.AUTO)
    private Long loginId;

    /**
     * 用户ID
     * - 登录成功：一定有值
     * - 登录失败（用户不存在）：可为空
     */
    private Long userId;

    /**
     * 登录账号（冗余字段）
     * - 即使 userId 为空也必须有
     * - 用于失败统计、审计查询
     */
    private String username;

    /**
     * 登录类型
     * 1 - 登录成功
     * 0 - 登录失败
     */
    private Integer loginType;

    /**
     * 登录失败原因
     * 取值来自 LoginFailReason 枚举
     * 例：
     * - PASSWORD_ERROR
     * - USER_NOT_FOUND
     * - ACCOUNT_LOCKED
     */
    private String failReason;

    /**
     * 当前失败次数（发生本次失败后的累计次数）
     * - 便于直接审计查看
     * - 与 Redis 中的失败计数保持一致
     */
    private Integer failCount;

    /**
     * 是否触发锁定
     * 1 - 已锁定
     * 0 - 未锁定
     */
    private Integer locked;

    /**
     * JWT 中的 tokenId
     * - 登录成功时必填
     * - 登录失败时为空
     * - 用于踢人 / 登出 / 失效追踪
     */
    private String tokenId;

    /**
     * 设备类型
     * 例如：
     * - PC
     * - MOBILE
     * - PAD
     */
    private String deviceType;

    /**
     * 客户端信息（User-Agent）
     * - 浏览器类型
     * - APP 信息
     */
    private String clientInfo;

    /**
     * 登录IP地址
     */
    private String loginIp;

    /**
     * 登录地（可异步解析）
     * 例如：浙江省杭州市
     */
    private String loginLocation;

    /**
     * 登录时间
     * - 成功 / 失败都会记录
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime loginTime;

    /**
     * token 过期时间
     * - 仅登录成功时有值
     */
    private LocalDateTime expireTime;

    /**
     * 登出时间
     * - 主动退出
     * - 被踢下线
     * - token 失效
     */
    private LocalDateTime logoutTime;

    /**
     * 登出原因
     * - 主动退出
     * - 被踢下线
     * - token 失效
     */
    private String logoutReason;

    /**
     * 在线状态
     * 1 - 在线
     * 0 - 已下线
     * 仅对登录成功记录有意义
     */
    private Integer status;

    /**
     * 逻辑删除标识
     * 0 - 未删除
     * 1 - 已删除
     */
    @TableLogic(value = "0", delval = "1")
    private Integer deleted;
}
