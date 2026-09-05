DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user
(
    `user_id`      BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    `campus_id`    BIGINT       NOT NULL DEFAULT 0 COMMENT '校区ID，管理员为0',
    `username`     VARCHAR(32)  NOT NULL COMMENT '登录账号（学号/工号）',
    `password`     VARCHAR(255) NOT NULL COMMENT '密码（BCrypt）',
    `nickname`     VARCHAR(50)  NOT NULL COMMENT '用户昵称',
    `phone_number` VARCHAR(20) COMMENT '手机号',
    `email`        VARCHAR(128) COMMENT '邮箱',
    `remark`       VARCHAR(200) COMMENT '备注',
    `user_type`    TINYINT               DEFAULT 2 NOT NULL COMMENT '0-管理员 1-教师 2-学生',
    `source`       TINYINT               DEFAULT 0 COMMENT '用户来源（0：IMPORT / 1：SYNC / 2：MANUAL）',
    `status`       TINYINT               DEFAULT 1 COMMENT '状态（0-禁用，1-启用），业务可见性',
    `deleted`      TINYINT               DEFAULT 0 COMMENT '逻辑删除（0-未删除 1-已删除），数据可见性',
    `create_by`    BIGINT COMMENT '创建用户ID',
    `update_by`    BIGINT COMMENT '更新用户ID',
    `create_time`  DATETIME              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_username_deleted (`username`, `deleted`),
    UNIQUE KEY uk_phone_deleted (`phone_number`, `deleted`),
    UNIQUE KEY uk_email_deleted (`email`, `deleted`),
    KEY idx_campus_username_deleted (`campus_id`, `username`, `deleted`),
    KEY idx_campus_status_deleted (`campus_id`, `status`, `deleted`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='系统用户表';

-- 初始化管理员账号
INSERT INTO sys_user (`campus_id`, `username`, `password`, nickname, remark, user_type, source, create_by)
VALUES (1101,
        'admin',
        '$2a$10$2529vU4WTji.qS6i3LfEYu6s2NUHzeWOnwl.so9CtSJUm7O3QnHp6',
        '系统管理员',
        '系统初始化管理员',
        0,
        0,
        1);

-- 初始化示例教师账号
INSERT INTO sys_user (`campus_id`, `username`, `password`, nickname, remark, user_type, source, create_by)
VALUES (1101,
        '12018007',
        '$2a$10$2529vU4WTji.qS6i3LfEYu6s2NUHzeWOnwl.so9CtSJUm7O3QnHp6',
        '张建国',
        '体育老师',
        1,
        0,
        1);

-- 初始化示例学生账号
INSERT INTO sys_user (`campus_id`, `username`, `password`, nickname, remark, user_type, source, create_by)
VALUES (1101,
        '412251401',
        '$2a$10$2529vU4WTji.qS6i3LfEYu6s2NUHzeWOnwl.so9CtSJUm7O3QnHp6',
        '肖成',
        '学生',
        1,
        0,
        1);

DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role
(
    `role_id`     BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    `campus_id`   BIGINT      NOT NULL DEFAULT 0 COMMENT '校区ID',
    `role_code`   VARCHAR(30) NOT NULL COMMENT '角色编码（系统唯一）',
    `role_name`   VARCHAR(50) NOT NULL COMMENT '角色名称（业务唯一）',
    `data_scope`  TINYINT              DEFAULT 0 COMMENT '数据权限范围（0-全部 1-本人 2-本班 3-本学院 4-自定义）',
    `remark`      VARCHAR(200) COMMENT '备注',
    `status`      TINYINT              DEFAULT 1 COMMENT '状态（0-禁用，1-启用），业务可见性',
    `deleted`     TINYINT              DEFAULT 0 COMMENT '逻辑删除（0-未删除 1-已删除），数据可见性',
    `create_time` DATETIME             DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_role_code_deleted (`campus_id`, `role_code`, `deleted`),
    UNIQUE KEY uk_role_name_deleted (`campus_id`, `role_name`, `deleted`),
    KEY idx_status_deleted (`status`, `deleted`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='系统角色表';

INSERT INTO sys_role (campus_id, role_code, role_name, remark)
VALUES (1101, 'ADMIN', '系统管理员', '系统最高权限，负责系统配置与用户管理'),
       (1101, 'TEACHER', '教师', '负责学生体测数据的录入与管理'),
       (1101, 'STUDENT', '学生', '仅可查看本人体测数据');

DROP TABLE IF EXISTS sys_permission;
CREATE TABLE sys_permission
(
    `perm_id`     BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '权限ID',
    `perm_code`   VARCHAR(100) NOT NULL COMMENT '权限编码（系统唯一）',
    `perm_name`   VARCHAR(100) NOT NULL COMMENT '权限名称（业务唯一）',
    `remark`      VARCHAR(200) COMMENT '备注',
    `status`      TINYINT  DEFAULT 1 COMMENT '状态（0-禁用，1-启用），业务可见性',
    `deleted`     TINYINT  DEFAULT 0 COMMENT '逻辑删除（0-未删除 1-已删除），数据可见性',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_perm_code_deleted (`perm_code`, `deleted`),
    UNIQUE KEY uk_perm_name_deleted (`perm_name`, `deleted`),
    KEY idx_status_deleted (`status`, `deleted`),
    KEY idx_perm_code (perm_code)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='系统权限表';
INSERT INTO sys_permission (perm_code, perm_name, remark)
VALUES
-- 系统管理
('system:user:view', '查看用户', '用户列表查询'),
('system:user:add', '新增用户', '创建系统用户'),
('system:user:update', '修改用户', '修改用户信息'),
('system:user:delete', '删除用户', '删除系统用户'),

('system:role:view', '查看角色', '角色列表查询'),
('system:role:add', '新增角色', '创建角色'),
('system:role:update', '修改角色', '修改角色信息'),
('system:role:delete', '删除角色', '删除角色'),

('system:permission:view', '查看权限', '权限列表查询'),
('system:permission:add', '新增权限', '创建权限'),
('system:permission:update', '修改权限', '修改权限'),
('system:permission:delete', '删除权限', '删除权限'),

-- 体测管理
('fitness:record:view', '查看体测记录', '查询体测记录'),
('fitness:record:add', '新增体测记录', '录入体测数据'),
('fitness:record:update', '修改体测记录', '修改体测数据'),
('fitness:record:delete', '删除体测记录', '删除体测数据'),
('fitness:record:export', '导出体测记录', '导出体测数据'),
('fitness:record:self:view', '学生查看本人体测记录', '学生查询本人体测记录'),

('fitness:summary:view', '查看体测汇总', '查看体测分析结果'),
('fitness:summary:export', '导出体测汇总', '导出体测分析结果');

DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role
(
    `id`          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `user_id`     BIGINT NOT NULL COMMENT '用户ID（逻辑外键：sys_user.user_id）',
    `role_id`     BIGINT NOT NULL COMMENT '角色ID（逻辑外键：sys_role.role_id）',
    `status`      TINYINT  DEFAULT 1 COMMENT '状态（0-禁用，1-启用），业务可见性',
    `deleted`     TINYINT  DEFAULT 0 COMMENT '逻辑删除（0-未删除 1-已删除），数据可见性',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_role_deleted (`user_id`, `role_id`, `deleted`),
    KEY idx_user_id (`user_id`),
    KEY idx_role_id (`role_id`),
    KEY idx_user_deleted (`user_id`, `deleted`),
    KEY idx_status_deleted (`status`, `deleted`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户角色关联表';

-- 管理员 → ADMIN 角色绑定
-- “ADMIN 角色默认全量权限，仅用于系统初始化”
INSERT INTO sys_user_role (user_id, role_id)
SELECT u.user_id, r.role_id
FROM sys_user u
         JOIN sys_role r
              ON r.role_code = 'ADMIN'
WHERE u.username = 'admin';

-- 教师→ TEACHER 角色绑定
INSERT INTO sys_user_role (user_id, role_id)
SELECT u.user_id, r.role_id
FROM sys_user u,
     sys_role r
WHERE u.`username` = '12018007'
  AND r.role_code = 'TEACHER';

-- 学生→ STUDENT 角色绑定
INSERT INTO sys_user_role (user_id, role_id)
SELECT u.user_id, r.role_id
FROM sys_user u,
     sys_role r
WHERE u.`username` = '412251401'
  AND r.role_code = 'STUDENT';

DROP TABLE IF EXISTS sys_role_permission;
CREATE TABLE sys_role_permission
(
    `id`          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `role_id`     BIGINT NOT NULL COMMENT '角色ID（逻辑外键：sys_role.role_id）',
    `perm_id`     BIGINT NOT NULL COMMENT '权限ID（逻辑外键：sys_permission.perm_id）',
    `status`      TINYINT  DEFAULT 1 COMMENT '状态（0-禁用，1-启用），业务可见性',
    `deleted`     TINYINT  DEFAULT 0 COMMENT '逻辑删除（0-未删除 1-已删除），数据可见性',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_role_perm_deleted (`role_id`, `perm_id`, `deleted`),
    KEY idx_role_id (`role_id`),
    KEY idx_perm_id (`perm_id`),
    KEY idx_role_deleted (`role_id`, `deleted`),
    KEY idx_status_deleted (`status`, `deleted`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='角色权限关联表';

-- 管理员：角色-权限绑定
INSERT INTO sys_role_permission (role_id, perm_id)
SELECT r.role_id, p.perm_id
FROM sys_role r
         JOIN sys_permission p
WHERE r.role_code = 'ADMIN';

-- 教师：角色-权限绑定
INSERT INTO sys_role_permission (role_id, perm_id)
SELECT r.role_id, p.perm_id
FROM sys_role r
         JOIN sys_permission p
              ON p.perm_code IN (
                                 'fitness:record:view',
                                 'fitness:record:add',
                                 'fitness:record:update',
                                 'fitness:summary:view'
                  )
WHERE r.role_code = 'TEACHER';

-- 学生：角色-权限绑定
INSERT INTO sys_role_permission (role_id, perm_id)
SELECT r.role_id, p.perm_id
FROM sys_role r
         JOIN sys_permission p
              ON p.perm_code IN (
                                 'fitness:record:self:view',
                                 'fitness:record:view',
                                 'fitness:summary:view'
                  )
WHERE r.role_code = 'STUDENT';

DROP TABLE IF EXISTS sys_user_login;
CREATE TABLE sys_user_login
(
    `login_id`       BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '登录记录ID',

    `user_id`        BIGINT COMMENT '用户ID（失败时可能为空）',
    `username`       VARCHAR(50) NOT NULL COMMENT '登录账号（冗余）',

    `login_type`     TINYINT     NOT NULL COMMENT '登录类型：1-成功 0-失败',
    `fail_reason`    VARCHAR(100) COMMENT '失败原因（仅失败时）',

    `token_id`       VARCHAR(64) COMMENT 'JWT tokenId（仅成功时）',

    `device_type`    VARCHAR(30) COMMENT '设备类型（PC / MOBILE / PAD）',
    `client_info`    VARCHAR(255) COMMENT '客户端信息（浏览器/APP）',

    `login_ip`       VARCHAR(50) COMMENT '登录IP',
    `login_location` VARCHAR(100) COMMENT '登录地',

    `login_time`     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    `expire_time`    DATETIME COMMENT 'token过期时间',
    `logout_time`    DATETIME COMMENT '登出时间',
    logout_reason    VARCHAR(50) COMMENT '登出原因（LOGOUT / KICK / EXPIRE）',

    `status`         TINYINT  DEFAULT 1 COMMENT '在线状态：1-在线 0-下线',
    `deleted`        TINYINT  DEFAULT 0 COMMENT '逻辑删除',

    KEY idx_username (`username`),
    KEY idx_user_id (`user_id`),
    KEY idx_login_type (`login_type`),
    KEY idx_login_ip (`login_ip`),
    KEY idx_login_time (`login_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户登录审计表';