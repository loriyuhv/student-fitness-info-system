-- 用户扩展信息表（存储所有用户的可选扩展信息，与认证表 sys_user 解耦）
-- 说明：本表与 sys_user 表是 1:1 关系，存储用户的基本资料、头像、最后登录信息等，
--       sys_user 表专注于认证（账号/密码/状态），本表专注于用户画像（资料/头像/地址）
--       sys_user 与 user_profile 的区分，遵循“认证与个人信息分离”的设计原则
DROP TABLE IF EXISTS user_profile;
CREATE TABLE user_profile
(
    profile_id      BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户扩展信息唯一标识（自增主键）',
    user_id         BIGINT           NOT NULL COMMENT '关联用户ID（逻辑外键：sys_user.user_id，1:1 关系）',
    campus_id       BIGINT           NOT NULL DEFAULT 1 COMMENT '所属校区ID（0表示系统级，非0关联校区表，便于多校区数据隔离）',
    gender          TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女（默认未知）',
    birth_date      DATE COMMENT '出生日期（格式：YYYY-MM-DD，用于年龄计算和统计）',
    avatar_url      VARCHAR(500) COMMENT '头像图片URL（支持 CDN 地址，最长 500 字符）',
    address         VARCHAR(255) COMMENT '联系地址（用户填写的常用地址，用于寄送通知或证明材料）',
    last_login_ip   VARCHAR(45) COMMENT '最后登录IP地址（支持 IPv4 和 IPv6，冗余字段，用于安全审计和风控）',
    last_login_time DATETIME COMMENT '最后登录时间（冗余字段，避免关联查询 sys_user_login 表，提高列表展示性能）',
    deleted         TINYINT                   DEFAULT 0 COMMENT '逻辑删除标记：0-未删除，1-已删除（软删除，保留历史数据用于审计）',
    create_by       VARCHAR(50) COMMENT '创建人（记录操作人用户名，便于追溯数据来源）',
    update_by       VARCHAR(50) COMMENT '最后更新人（记录操作人用户名，便于追踪变更责任人）',
    create_time     DATETIME                  DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间（自动生成）',
    update_time     DATETIME                  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录最后更新时间（自动更新）',
    -- 唯一索引：确保一个用户只有一条扩展信息记录
    UNIQUE KEY uk_user_id (user_id),
    -- 普通索引：加速按删除标记查询有效用户扩展信息
    KEY idx_deleted (deleted),
    -- 普通索引：加速按创建时间排序查询（如最近注册用户列表）
    KEY idx_create_time (create_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户扩展信息表（存储用户基本资料、头像、地址及最后登录记录，与 sys_user 表 1:1 关联）';

-- 班级信息表（存储学校所有班级的基本信息，被学生档案和教师-班级关系引用）
-- 说明：本表存储班级的基础信息，包括班级编码、名称、年级等。
--       student_profile.class_id 引用本表，标识学生所属班级；
--       teacher_class.class_id 引用本表，标识教师的任教班级。
--       班级是系统中最核心的组织单元，用于数据权限过滤（教师仅见本班学生）。
DROP TABLE IF EXISTS class_info;
CREATE TABLE class_info
(
    class_id      BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '班级唯一标识（自增主键）',
    campus_id     BIGINT      NOT NULL DEFAULT 1 COMMENT '所属校区ID（0表示系统级，非0关联校区表，支持多校区数据隔离）',
    class_code    VARCHAR(30) NOT NULL COMMENT '班级编码（业务唯一标识，格式：年级+专业代码+班序号，如 20247300101 表示2024级计算机科学技术1班，建议统一编码规范）',
    class_name    VARCHAR(50) NOT NULL COMMENT '班级显示名称（如“2024级体育教育1班”，用于界面展示和导出报表）',
    grade         YEAR        NOT NULL COMMENT '年级（入学年份，格式：YYYY，如 2024，用于按年级筛选和统计）',
    student_count INT                  DEFAULT 0 COMMENT '学生人数（冗余统计字段，避免每次查询都 COUNT，用于列表展示和概览，业务代码需在增删学生时同步更新）',
    status        TINYINT              DEFAULT 1 COMMENT '业务启用状态：0-停用（该班级不可再分配学生，已有数据保留），1-启用（正常使用）',
    deleted       TINYINT              DEFAULT 0 COMMENT '逻辑删除标记：0-未删除，1-已删除（参与唯一索引，允许删除后重建同名班级）',
    remark        VARCHAR(200) COMMENT '备注信息（运营或管理备注，如“本班为体育教育专业示范班”）',
    create_by     VARCHAR(50) COMMENT '创建人（记录操作人用户名，便于追溯数据来源）',
    create_time   DATETIME             DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间（自动生成）',
    update_by     VARCHAR(50) COMMENT '最后更新人（记录操作人用户名，便于追踪变更责任人）',
    update_time   DATETIME             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录最后更新时间（自动更新）',
    -- 唯一索引：确保同一班级编码在系统中唯一（含逻辑删除），删除后可重建同名班级
    UNIQUE KEY uk_class_code (class_code, deleted)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='班级信息表（存储学校班级基础信息，被 student_profile 和 teacher_class 引用，用于数据权限过滤和业务统计）';

-- 学生信息扩展表（存储学生的学籍档案信息，与认证表 sys_user 解耦）
-- 说明：
--   1. 本表与 sys_user 表是 1:1 关系，存储学生的学籍特有信息
--   2. sys_user 表专注于认证（账号/密码/状态），本表专注于学籍档案（学号/班级/身份证号等）
--   3. 仅用户类型为“学生”的用户在本表有记录，教师和管理员无记录
--   4. 与 user_profile 表互为补充：user_profile 存储通用信息（头像/地址/最后登录），
--      本表存储学生特有信息（学号/班级/专业/身份证号等）
--   5. 敏感字段（学号、身份证号）在业务代码中需严格控制读写权限，仅 ADMIN 可修改
DROP TABLE IF EXISTS student_profile;
CREATE TABLE student_profile
(
    student_id     BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '学生档案唯一标识（自增主键）',
    campus_id      BIGINT      NOT NULL DEFAULT 1 COMMENT '所属校区ID（0表示系统级，非0关联校区表，支持多校区数据隔离）',
    user_id        BIGINT      NOT NULL COMMENT '关联用户ID（逻辑外键：sys_user.user_id，1:1 关系，仅 user_type=2 的用户在此有记录）',
    student_no     VARCHAR(20) NOT NULL COMMENT '学号（业务唯一标识，通常与 sys_user.username 保持一致，便于关联查询和统一认证）',
    class_id       BIGINT COMMENT '所属班级ID（逻辑外键：class_info.class_id，可为空，学生可能尚未分班）',
    enroll_year    INT COMMENT '入学年份（格式：YYYY，如 2024，用于按入学年份筛选和统计，可能与班级 grade 不一致，如学生留级或转班）',
    major          VARCHAR(100) COMMENT '专业名称（如“计算机科学与技术”“体育教育”，冗余字段，便于列表展示时避免关联查询）',
    id_card        VARCHAR(18) COMMENT '身份证号（18位，中国大陆居民身份证号码，敏感字段，业务代码中需严格脱敏处理）',
    gender         TINYINT UNSIGNED     DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女（冗余字段，与 user_profile.gender 同步，便于学籍报表单独查询）',
    birth_date     DATE COMMENT '出生日期（格式：YYYY-MM-DD，冗余字段，与 user_profile.birth_date 同步，便于学籍报表单独查询）',
    family_address VARCHAR(255) COMMENT '家庭详细地址（用于寄送通知书、成绩单等材料）',
    avatar_url     VARCHAR(500) COMMENT '学生照片URL（头像链接，支持 CDN 地址，最长 500 字符）',
    remark         VARCHAR(200) COMMENT '备注信息（如“体测免测”“转专业学生”等特殊标记）',
    status         TINYINT              DEFAULT 1 COMMENT '学籍状态：0-禁用（如休学、退学），1-正常（在校在籍）',
    deleted        TINYINT              DEFAULT 0 COMMENT '逻辑删除标记：0-未删除，1-已删除（参与唯一索引，允许删除后重建）',
    create_by      VARCHAR(50) COMMENT '创建人（记录操作人用户名，便于追溯数据来源）',
    create_time    DATETIME             DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间（自动生成）',
    update_by      VARCHAR(50) COMMENT '最后更新人（记录操作人用户名，便于追踪变更责任人）',
    update_time    DATETIME             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录最后更新时间（自动更新）',
    -- 唯一索引：确保一个用户只有一条学生档案记录，且删除后可重建
    UNIQUE KEY uk_user_deleted (user_id, deleted),
    -- 唯一索引：确保学号在系统中唯一，且删除后可重建同名学号
    UNIQUE KEY uk_student_no_deleted (student_no, deleted),
    -- 唯一索引：确保身份证号在系统中唯一，且删除后可重建
    UNIQUE KEY uk_id_card_deleted (id_card, deleted),
    -- 普通索引：加速按状态和删除标记查询有效学生列表
    KEY idx_status_deleted (status, deleted)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='学生信息扩展表（存储学生学籍档案：学号/班级/专业/身份证号等，与 sys_user 表 1:1 关联，仅学生类型用户有记录）';

-- 教师扩展信息表（存储教师的档案信息，与认证表 sys_user 解耦）
-- 说明：
--   1. 本表与 sys_user 表是 1:1 关系，存储教师的特有档案信息
--   2. sys_user 表专注于认证（账号/密码/状态），本表专注于教师档案（工号/性别/备注等）
--   3. 仅用户类型为“教师”的用户在本表有记录，管理员和学生无记录
--   4. 与 user_profile 表互为补充：user_profile 存储通用信息（头像/地址/最后登录），
--      本表存储教师特有信息（工号等）
--   5. 教师通过 teacher_class 表关联班级，实现 data_scope=2（本班）的数据权限过滤
--   6. 教师可查看/修改自己的档案信息（受 user:teacher:view 和 user:teacher:update 权限控制）
DROP TABLE IF EXISTS teacher_profile;
CREATE TABLE teacher_profile
(
    teacher_id  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '教师档案唯一标识（自增主键）',
    campus_id   BIGINT      NOT NULL DEFAULT 1 COMMENT '所属校区ID（0表示系统级，非0关联校区表，支持多校区数据隔离）',
    user_id     BIGINT      NOT NULL COMMENT '关联用户ID（逻辑外键：sys_user.user_id，1:1 关系，仅 user_type=1 的用户在此有记录）',
    teacher_no  VARCHAR(20) NOT NULL COMMENT '教师工号（业务唯一标识，通常与 sys_user.username 保持一致，便于关联查询和统一认证）',
    gender      TINYINT UNSIGNED     DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女（冗余字段，与 user_profile.gender 同步，便于教师报表单独查询）',
    remark      VARCHAR(200) COMMENT '备注信息（如“高级职称”“教研组长”等身份标记或补充说明）',
    status      TINYINT              DEFAULT 1 COMMENT '在职状态：0-禁用（如离职、停职），1-正常（在职在岗）',
    deleted     TINYINT              DEFAULT 0 COMMENT '逻辑删除标记：0-未删除，1-已删除（参与唯一索引，允许删除后重建）',
    create_by   VARCHAR(50) COMMENT '创建人（记录操作人用户名，便于追溯数据来源）',
    create_time DATETIME             DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间（自动生成）',
    update_by   VARCHAR(50) COMMENT '最后更新人（记录操作人用户名，便于追踪变更责任人）',
    update_time DATETIME             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录最后更新时间（自动更新）',
    -- 唯一索引：确保一个用户只有一条教师档案记录，且删除后可重建
    UNIQUE KEY uk_user_deleted (user_id, deleted),
    -- 唯一索引：确保工号在系统中唯一，且删除后可重建同名工号
    UNIQUE KEY uk_teacher_no_deleted (teacher_no, deleted),
    -- 普通索引：加速按状态和删除标记查询有效教师列表
    KEY idx_status_deleted (status, deleted)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='教师信息扩展表（存储教师档案：工号/性别等，与 sys_user 表 1:1 关联，仅教师类型用户有记录，通过 teacher_class 关联班级实现数据权限过滤）';

-- 教师-班级关系表（建立教师与班级的多对多关联，用于数据权限过滤和教学任务分配）
-- 说明：
--   1. 本表建立教师与班级的多对多关系：一个教师可管理多个班级，一个班级可被多个教师管理
--   2. 核心用途：配合教师角色的 data_scope=2（本班）实现数据权限过滤，
--      查询时通过本表获取当前教师的任教班级列表，在 SQL 中追加 class_id IN (...) 条件
--   3. 与 teacher_profile 的区别：teacher_profile 存储教师档案信息（工号/性别等），
--      本表存储教师与班级的关联关系（任教分配）
--   4. 典型的业务场景：
--      - 体测成绩录入：教师仅可录入任教班级学生的体测数据
--      - 数据查看：教师查看体测记录时，仅返回任教班级学生的数据
--      - 学生管理：教师管理本班学生的档案信息
DROP TABLE IF EXISTS teacher_class;
CREATE TABLE teacher_class
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联记录唯一标识（自增主键）',
    campus_id   BIGINT NOT NULL DEFAULT 1 COMMENT '所属校区ID（0表示系统级，非0关联校区表，支持多校区数据隔离）',
    teacher_id  BIGINT NOT NULL COMMENT '教师ID（逻辑外键：teacher_profile.teacher_id，标识被分配教学任务的教师）',
    class_id    BIGINT NOT NULL COMMENT '班级ID（逻辑外键：class_info.class_id，标识分配给该教师的班级）',
    status      TINYINT         DEFAULT 1 COMMENT '关联状态：0-无效（该教师不再负责该班级，但不删除历史记录），1-有效（当前正在任教）',
    deleted     TINYINT         DEFAULT 0 COMMENT '逻辑删除标记：0-未删除，1-已删除（参与唯一索引，允许删除后重新建立相同关联）',
    remark      VARCHAR(200) COMMENT '备注信息（如“临时代课”“2024秋季学期”等教学任务说明）',
    create_by   VARCHAR(50) COMMENT '创建人（记录操作人用户名，便于追溯数据来源）',
    create_time DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间（自动生成）',
    update_by   VARCHAR(50) COMMENT '最后更新人（记录操作人用户名，便于追踪变更责任人）',
    update_time DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录最后更新时间（自动更新）',
    -- 唯一索引：确保同一教师在同一班级下只有一条有效/已删除记录，防止重复分配
    -- 设计意图：当教师不再负责某班级时，执行逻辑删除（deleted=1），
    --         如需重新分配，需先物理删除或更新原记录（恢复 deleted=0）
    -- 业务约束：同一教师 + 同一班级 只能存在一条有效关联记录
    UNIQUE KEY uk_teacher_class (teacher_id, class_id, deleted)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='教师-班级关系表（建立教师与班级的多对多关联，用于 data_scope=2 本班数据权限过滤和教学任务分配）';
