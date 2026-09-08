-- 系统用户表（存储所有用户认证及基本信息，包含管理员、教师、学生）
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user
(
    `user_id`      BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户唯一标识（自增主键）',
    `campus_id`    BIGINT       NOT NULL DEFAULT 0 COMMENT '所属校区ID（0表示系统级管理员，非0关联校区表）',
    `username`     VARCHAR(32)  NOT NULL COMMENT '登录账号（学生用学号，教师用工号，管理员自定义）',
    `password`     VARCHAR(255) NOT NULL COMMENT '登录密码（使用Bcrypt加密存储）',
    `nickname`     VARCHAR(50)  NOT NULL COMMENT '显示昵称（可重复，用于界面展示）',
    `phone_number` VARCHAR(20) COMMENT '手机号码（可用于登录或找回密码）',
    `email`        VARCHAR(128) COMMENT '电子邮箱（可用于登录或找回密码）',
    `remark`       VARCHAR(200) COMMENT '备注信息（运营或管理备注，无业务逻辑）',
    `user_type`    TINYINT               DEFAULT 2 NOT NULL COMMENT '用户类型：0-管理员 1-教师 2-学生',
    `source`       TINYINT               DEFAULT 0 COMMENT '用户来源：0-导入（IMPORT），1-同步（SYNC），2-手动录入（MANUAL）',
    `status`       TINYINT               DEFAULT 1 COMMENT '业务启用状态：0-禁用（无法登录/分配权限），1-启用（正常）业务可见性',
    `deleted`      TINYINT               DEFAULT 0 COMMENT '逻辑删除标记：0-未删除，1-已删除（参与唯一索引，允许同名恢复）数据可见性',
    `create_by`    BIGINT COMMENT '创建人用户ID（关联sys_user.user_id）',
    `update_by`    BIGINT COMMENT '最后更新人用户ID（关联sys_user.user_id）',
    `create_time`  DATETIME              DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间（自动生成）',
    `update_time`  DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录最后更新时间（自动更新）',
    UNIQUE KEY uk_username_deleted (`username`, `deleted`),
    UNIQUE KEY uk_phone_deleted (`phone_number`, `deleted`),
    UNIQUE KEY uk_email_deleted (`email`, `deleted`),
    KEY idx_campus_username_deleted (`campus_id`, `username`, `deleted`),
    KEY idx_campus_status_deleted (`campus_id`, `status`, `deleted`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='系统用户表（存储所有用户认证及基本信息，包含管理员、教师、学生）';

-- 用户登录审计表（记录每次登录请求的详细上下文、JWT 令牌生命周期及在线状态，用于安全审计与活跃会话管理）
DROP TABLE IF EXISTS sys_user_login;
CREATE TABLE sys_user_login
(
    `login_id`       BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '登录记录唯一标识（自增主键）',
    `user_id`        BIGINT COMMENT '关联的用户ID（失败登录或因未认证用户可能为NULL，关联sys_user.user_id）',
    `username`       VARCHAR(50) NOT NULL COMMENT '登录账号（冗余存储，便于快速检索，即使user_id为空也可定位）',
    `login_type`     TINYINT     NOT NULL COMMENT '登录结果类型：1-成功，0-失败（用于统计成功率）',
    `fail_reason`    VARCHAR(100) COMMENT '登录失败原因（仅在login_type=0时有值，如“密码错误”“账号锁定”）',
    `token_id`       VARCHAR(64) COMMENT 'JWT令牌唯一标识（仅在登录成功时生成，用于后续踢人/续期操作）',
    `device_type`    VARCHAR(30) COMMENT '设备类型（PC / MOBILE / PAD，从User-Agent解析）',
    `client_info`    VARCHAR(255) COMMENT '客户端详细信息（如浏览器版本、操作系统、APP名称等）',
    `login_ip`       VARCHAR(50) COMMENT '登录来源IP地址（支持IPv4/IPv6）',
    `login_location` VARCHAR(100) COMMENT '登录地理位置（基于IP解析，如“广东省广州市”）',
    `login_time`     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '登录请求时间（自动生成，记录实际登录发生时刻）',
    `expire_time`    DATETIME COMMENT 'JWT令牌过期时间（仅成功登录时有值，用于主动清理过期会话）',
    `logout_time`    DATETIME COMMENT '用户主动登出或系统强制下线的时间（NULL表示仍在线或未登出）',
    `logout_reason`  VARCHAR(50) COMMENT '登出原因枚举：LOGOUT（主动登出）、KICK（被踢下线）、EXPIRE（令牌到期自动下线）',
    `status`         TINYINT  DEFAULT 1 COMMENT '会话在线状态：1-在线（令牌有效），0-已下线（令牌已失效或被注销）',
    `deleted`        TINYINT  DEFAULT 0 COMMENT '逻辑删除标记：0-未删除，1-已删除（用于数据清理归档，默认保留）',
    -- 索引设计：覆盖常用查询场景（账号检索、用户历史、时间范围、IP审计）
    KEY idx_username (`username`),
    KEY idx_user_id (`user_id`),
    KEY idx_login_type (`login_type`),
    KEY idx_login_ip (`login_ip`),
    KEY idx_login_time (`login_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户登录审计表（记录每次登录请求、令牌生命周期及在线状态，用于安全分析和会话管理）';

-- 系统角色表（定义权限集合的载体，通过角色关联权限编码，实现用户权限的批量授予）
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role
(
    `role_id`     BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色唯一标识（自增主键）',
    `campus_id`   BIGINT      NOT NULL DEFAULT 0 COMMENT '所属校区ID（0表示系统级角色，可在所有校区生效；非0表示校区自定义角色，仅本校区可用）',
    `role_code`   VARCHAR(30) NOT NULL COMMENT '角色编码（系统内部唯一标识，通常用于代码硬编码判断，如 ADMIN/TEACHER/STUDENT，不可随意变更）',
    `role_name`   VARCHAR(50) NOT NULL COMMENT '角色显示名称（用于界面展示，业务上唯一，管理员可根据业务调整名称，不影响系统逻辑）',
    `data_scope`  TINYINT              DEFAULT 0 COMMENT '数据行级权限范围（配合权限编码中的按钮权限共同控制数据可见性）：0-全部数据（跨校区/跨班级），1-仅本人（仅查看自己创建的数据），2-本班（仅查看本班学生数据），3-本学院（仅查看本学院数据），4-自定义（预留扩展，需额外配置数据规则）',
    `remark`      VARCHAR(200) COMMENT '角色备注说明（描述该角色的适用场景或权限边界，如“负责体测数据录入的教师”）',
    `status`      TINYINT              DEFAULT 1 COMMENT '业务启用状态：0-禁用（该角色不可被分配，已有用户该角色权限失效），1-启用（可正常分配与使用）',
    `deleted`     TINYINT              DEFAULT 0 COMMENT '逻辑删除标记：0-未删除，1-已删除（参与唯一索引，允许回收后重建同名角色）',
    `create_time` DATETIME             DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间（自动生成）',
    `update_time` DATETIME             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录最后更新时间（自动更新，仅记录角色基础信息变更，不因权限挂载变化而更新）',
    -- 唯一索引：确保同一校区下角色编码与角色名称的唯一性，且删除后可重新创建同名角色
    UNIQUE KEY uk_role_code_deleted (`campus_id`, `role_code`, `deleted`),
    UNIQUE KEY uk_role_name_deleted (`campus_id`, `role_name`, `deleted`),
    -- 普通索引：加速按状态和删除标记的查询（如查询所有启用角色列表）
    KEY idx_status_deleted (`status`, `deleted`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='系统角色表（RBAC 权限模型核心表，定义角色及其数据范围，用户通过关联角色获得权限）';

-- 系统权限表（定义系统中所有可操作的权限点，即“按钮/接口级别的操作权限”，与角色关联实现功能权限控制）
DROP TABLE IF EXISTS sys_permission;
CREATE TABLE sys_permission
(
    `perm_id`     BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '权限唯一标识（自增主键）',
    `perm_code`   VARCHAR(100) NOT NULL COMMENT '权限编码（系统内部唯一标识，格式：模块:资源:操作，如 system:user:view 表示“查看用户”）。该编码在程序中被 @PreAuthorize 注解引用，不可随意变更',
    `perm_name`   VARCHAR(100) NOT NULL COMMENT '权限名称（用于界面展示，如“查看用户”“新增角色”，便于管理员理解权限含义）',
    `remark`      VARCHAR(200) COMMENT '权限说明（描述该权限允许执行的具体操作，如“允许查询用户列表，支持按条件筛选”）',
    `status`      TINYINT  DEFAULT 1 COMMENT '业务启用状态：0-禁用（该权限不可被授予角色，已有授权失效），1-启用（可正常分配）',
    `deleted`     TINYINT  DEFAULT 0 COMMENT '逻辑删除标记：0-未删除，1-已删除（参与唯一索引，允许删除后重新创建同名权限）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间（自动生成）',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录最后更新时间（自动更新）',
    -- 唯一索引：确保权限编码和名称在系统中唯一，且删除后可以重建同名记录
    UNIQUE KEY uk_perm_code_deleted (`perm_code`, `deleted`),
    UNIQUE KEY uk_perm_name_deleted (`perm_name`, `deleted`),
    -- 普通索引：加速按状态和删除标记的查询
    KEY idx_status_deleted (`status`, `deleted`),
    -- 单独索引：权限校验场景中经常通过 perm_code 快速查询权限记录，提升性能
    KEY idx_perm_code (perm_code)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='系统权限表（定义所有细粒度的操作权限点，如“查看用户”“新增体测记录”，通过角色授权控制用户可执行的操作）';

-- 用户角色关联表（建立用户与角色的多对多关系，实现用户权限的授予）
-- 说明：一个用户可以拥有多个角色，一个角色可以被多个用户拥有
--      用户通过关联的角色获得对应的操作权限（sys_role_permission）和数据权限（sys_role.data_scope）
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role
(
    `id`          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联记录唯一标识（自增主键）',
    `user_id`     BIGINT NOT NULL COMMENT '用户ID（关联 sys_user.user_id，标识被授权的用户）',
    `role_id`     BIGINT NOT NULL COMMENT '角色ID（关联 sys_role.role_id，标识授予用户的角色）',
    `status`      TINYINT  DEFAULT 1 COMMENT '关联状态：0-禁用（该用户的该角色权限立即失效，但关联关系保留），1-启用（正常授予，权限生效）',
    `deleted`     TINYINT  DEFAULT 0 COMMENT '逻辑删除标记：0-未删除，1-已删除（参与唯一索引，允许删除后重新绑定相同角色）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间（自动生成，记录授权时间）',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录最后更新时间（自动更新，记录状态变更或重新授权时间）',
    -- 唯一索引：确保同一用户在同一角色下只有一条有效/已删除记录，防止重复授权
    -- 设计意图：当需要回收用户角色时，执行逻辑删除（deleted=1），如需重新授予，需先物理删除或更新原记录
    UNIQUE KEY uk_user_role_deleted (`user_id`, `role_id`, `deleted`),
    -- 普通索引：加速按用户查询其所有角色的场景（如登录时加载用户权限）
    KEY idx_user_id (`user_id`),
    -- 普通索引：加速按角色查询其所有用户的场景（如查看某个角色下有哪些用户）
    KEY idx_role_id (`role_id`),
    -- 复合索引：加速查询用户的有效角色列表（最常用场景：登录鉴权时查询 status=1 且 deleted=0 的角色）
    KEY idx_user_deleted (`user_id`, `deleted`),
    -- 复合索引：加速按关联状态和删除标记的查询（如统计所有有效的用户-角色绑定关系）
    KEY idx_status_deleted (`status`, `deleted`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户角色关联表（RBAC 权限模型核心关联表，记录用户与角色的多对多绑定关系，实现权限授予）';

-- 角色权限关联表（建立角色与权限点的多对多关系，实现功能权限的授予）
-- 说明：一个角色可以拥有多个权限点，一个权限点可以被多个角色拥有
--      角色通过关联的权限点获得对应的操作权限（按钮/接口级别），
--      配合 sys_role.data_scope 字段共同构成完整的访问控制矩阵
DROP TABLE IF EXISTS sys_role_permission;
CREATE TABLE sys_role_permission
(
    `id`          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联记录唯一标识（自增主键）',
    `role_id`     BIGINT NOT NULL COMMENT '角色ID（关联 sys_role.role_id，标识被授予权限的角色）',
    `perm_id`     BIGINT NOT NULL COMMENT '权限ID（关联 sys_permission.perm_id，标识授予角色的操作权限点）',
    `status`      TINYINT  DEFAULT 1 COMMENT '关联状态：0-禁用（该角色的该权限立即失效，但关联关系保留），1-启用（正常授予，权限生效）',
    `deleted`     TINYINT  DEFAULT 0 COMMENT '逻辑删除标记：0-未删除，1-已删除（参与唯一索引，允许删除后重新绑定相同权限）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间（自动生成，记录授权时间）',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录最后更新时间（自动更新，记录状态变更或重新授权时间）',
    -- 唯一索引：确保同一角色在同一权限点下只有一条有效/已删除记录，防止重复授权
    -- 设计意图：当需要回收角色权限时，执行逻辑删除（deleted=1），如需重新授予，需先物理删除或更新原记录
    UNIQUE KEY uk_role_perm_deleted (`role_id`, `perm_id`, `deleted`),
    -- 普通索引：加速按角色查询其所有权限的场景（最常用：登录后加载角色权限列表）
    KEY idx_role_id (`role_id`),
    -- 普通索引：加速按权限点反查其被哪些角色拥有的场景（如查看某个权限被分配给了哪些角色）
    KEY idx_perm_id (`perm_id`),
    -- 复合索引：加速查询角色的有效权限列表（最常用场景：权限校验时查询 status=1 且 deleted=0 的权限）
    KEY idx_role_deleted (`role_id`, `deleted`),
    -- 复合索引：加速按关联状态和删除标记的查询（如统计所有有效的角色-权限绑定关系）
    KEY idx_status_deleted (`status`, `deleted`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='角色权限关联表（RBAC 权限模型核心关联表，记录角色与权限点的多对多绑定关系，实现功能权限授予）';
