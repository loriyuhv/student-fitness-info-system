-- ============================================================
-- 初始化系统默认用户（开箱即用测试账号）
-- 目的：提供开发/测试环境的基础登录账号，便于功能演示与联调
-- ⚠️ 安全警告：所有初始账号的密码均为默认值（明文：123456，对应下方的 Bcrypt 密文），
--    部署到生产环境前，必须禁用这些默认账号或强制用户在首次登录时修改密码！
-- 校区策略：所有初始化用户统一归属校区 ID=1101（示例校区）。
--    注：生产环境中，系统级管理员通常 campus_id=0 以跨校区管理，
--    此处绑定具体校区仅为了在多校区演示中直观区分数据范围。
-- ============================================================

-- 1. 系统管理员账号（角色：ADMIN）
-- 说明：拥有系统全部权限，用于系统配置、角色分配及运维管理。
-- 用户名：admin（固定保留名称，不可删除）
-- 来源：source=0（IMPORT 导入），create_by=1（系统自举创建）
INSERT INTO sys_user (`campus_id`, `username`, `password`, nickname, remark, user_type, source, create_by)
VALUES (1101,
        'admin',
        '$2a$10$2529vU4WTji.qS6i3LfEYu6s2NUHzeWOnwl.so9CtSJUm7O3QnHp6',
        '系统管理员',
        '系统初始化管理员',
        0, -- user_type: 0-管理员
        0, -- source: 0-导入
        1 -- create_by: 1 代表系统自身
       );

-- 2. 示例教师账号（角色：TEACHER）
-- 说明：体育教师“张建国”，工号 12018007，具备体测数据录入、查看本班汇总等教学权限。
-- 用户名即工号，便于与教务系统数据对齐。
INSERT INTO sys_user (`campus_id`, `username`, `password`, nickname, remark, user_type, source, create_by)
VALUES (1101,
        '12018007',
        '$2a$10$2529vU4WTji.qS6i3LfEYu6s2NUHzeWOnwl.so9CtSJUm7O3QnHp6',
        '张建国',
        '体育老师',
        1, -- user_type: 1-教师
        0, -- source: 0-导入
        1 -- create_by: 1 代表系统自身
       );

-- 3. 示例学生账号（角色：STUDENT）
-- 说明：学生“王子轩”，学号 412251401，仅可登录查看本人体测成绩与历史记录。
-- 用户名即学号，符合校园信息化场景中的统一身份认证习惯。
INSERT INTO sys_user (`campus_id`, `username`, `password`, nickname, remark, user_type, source, create_by)
VALUES (1101,
        '412251401',
        '$2a$10$2529vU4WTji.qS6i3LfEYu6s2NUHzeWOnwl.so9CtSJUm7O3QnHp6',
        '王子轩',
        '学生',
        2, -- user_type: 2-学生
        0, -- source: 0-导入
        1 -- create_by: 1 代表系统自身
       );

-- ============================================================
-- 初始化系统内置角色（RBAC 核心数据）
-- 目的：定义系统运行必需的三个基础角色，用于后续用户授权。
-- 关联说明：角色创建后，需在 sys_role_permission 中为每个角色挂载对应的权限编码，
--          在 sys_user_role 中将用户与角色绑定，三者共同构成完整的权限体系。
-- 校区策略：所有内置角色统一归属校区 ID=1101（示例校区），
--          实际部署时可根据需要调整 campus_id 或创建校区自定义角色。
-- 数据权限提示：这里显式指定 data_scope，避免使用默认值 0（全部数据）带来的越权风险。
-- ============================================================

-- 1. 系统管理员角色（ADMIN）
-- 用途：系统最高权限，可管理用户、角色、权限配置，并查看所有数据。
-- 数据范围：0-全部数据（跨校区、跨班级），用于运维与系统配置。
INSERT INTO sys_role (campus_id, role_code, role_name, data_scope, remark)
VALUES (1101,
        'ADMIN',
        '系统管理员',
        0, -- data_scope: 0-全部数据
        '系统最高权限，负责系统配置与用户管理，可跨校区操作');

-- 2. 教师角色（TEACHER）
-- 用途：负责学生体测数据的录入、修改及查看，仅限任教班级。
-- 数据范围：2-本班（通过教师-班级关联表确定任教班级，数据查询时自动过滤）
INSERT INTO sys_role (campus_id, role_code, role_name, data_scope, remark)
VALUES (1101,
        'TEACHER',
        '教师',
        2, -- data_scope: 2-本班
        '负责学生体测数据的录入与管理，仅可操作自己任教班级的学生数据');

-- 3. 学生角色（STUDENT）
-- 用途：登录后仅可查看本人体测成绩与历史记录，无录入/修改权限。
-- 数据范围：1-仅本人（通过当前登录用户 user_id 过滤数据）
INSERT INTO sys_role (campus_id, role_code, role_name, data_scope, remark)
VALUES (1101,
        'STUDENT',
        '学生',
        1, -- data_scope: 1-仅本人
        '仅可查看本人体测数据，无法查看他人数据或执行变更操作');

-- ============================================================
-- 系统权限初始化数据（V1.0）
-- 说明：
--   1. 权限编码格式：{模块}:{资源}:{操作}（如 system:user:view）
--      对应代码中的 @PreAuthorize("hasAuthority('system:user:view')") 注解
--   2. 权限按模块分组：系统管理（system:*）/ 体测管理（fitness:*）
--   3. 删除权限（逻辑删除）后，若需重建同名权限，需先物理删除已逻辑删除的记录
--   4. 新增权限后，需同步更新 ADMIN 角色的全量绑定（建议使用 CROSS JOIN）
--   5. 权限名称（perm_name）用于界面展示，权限编码（perm_code）用于程序判断，两者不可混淆
-- ============================================================

INSERT INTO sys_permission (perm_code, perm_name, remark)
VALUES

-- ============================================================
-- 一、系统管理模块（system:*）
--    权限说明：管理后台核心功能，仅 ADMIN 角色拥有
--    包含：用户管理、角色管理、权限管理三个子模块的完整 CRUD 操作
--    适用对象：系统运维人员、超级管理员
-- ============================================================

-- 1.1 用户管理（system:user:*）
--    功能：管理所有系统用户（管理员/教师/学生）的生命周期
--    安全约束：密码修改需走独立的密码重置流程，不通过此接口直接写入明文
('system:user:view', '查看用户', '查询用户列表，支持按用户名/校区/用户类型/状态筛选，用于用户管理页面的数据展示'),
('system:user:add', '新增用户',
 '创建系统用户，需指定用户类型（0-管理员/1-教师/2-学生）并分配初始角色，创建后用户可使用初始密码登录'),
('system:user:update', '修改用户',
 '编辑用户基本信息（昵称/手机号/邮箱/状态）及重新分配角色，不可直接修改密码，密码重置需走独立流程'),
('system:user:delete', '删除用户',
 '逻辑删除用户（deleted=1），删除后用户无法登录系统，但历史数据（如体测记录）保留用于审计追溯'),

-- 1.2 角色管理（system:role:*）
--    功能：定义和管理系统中的所有角色，角色是权限的集合
--    注意：role_code 在代码中可能被硬编码引用（如判断是否为 ADMIN），变更需谨慎
('system:role:view', '查看角色', '查询角色列表，支持按角色编码/名称/状态筛选，用于角色管理页面的数据展示'),
('system:role:add', '新增角色',
 '创建自定义角色，需配置角色编码、名称及数据权限范围（data_scope），编码一旦确定不可随意变更'),
('system:role:update', '修改角色',
 '编辑角色基础信息（名称/状态/备注）及数据权限范围，注意 role_code 不可变更，否则会影响代码中的硬编码判断'),
('system:role:delete', '删除角色',
 '逻辑删除角色（deleted=1），删除后该角色不可再分配给用户，已有授权关系保留但权限失效，可用于角色废弃或合并'),

-- 1.3 权限管理（system:permission:*）
--    功能：定义系统中的所有操作权限点，权限是系统中最细粒度的控制单元
--    编码规范：{模块}:{资源}:{操作}，如 system:user:view 表示“系统模块-用户资源-查看操作”
('system:permission:view', '查看权限',
 '查询权限列表，支持按权限编码/名称/状态筛选，用于权限管理页面的数据展示，编码格式需遵循命名规范'),
('system:permission:add', '新增权限',
 '创建新权限点，需遵循 {模块}:{资源}:{操作} 命名规范确保唯一性，新增后需同步更新代码中的 @PreAuthorize 注解引用'),
('system:permission:update', '修改权限',
 '编辑权限基础信息（名称/状态/备注），注意 perm_code 不可变更，变更等同于新建权限，需同步修改代码引用'),
('system:permission:delete', '删除权限',
 '逻辑删除权限（deleted=1），删除后该权限不可再被授予角色，已授予的权限立即失效，重建同名权限需先物理删除旧记录'),

-- ============================================================
-- 二、体测管理模块（fitness:*）
--    权限说明：体测业务的核心操作功能，教师和学生分别拥有不同粒度的权限
--    包含：体测记录管理、体测汇总分析两大子模块
--    数据权限配合：所有查询类操作均受 data_scope 限制（教师仅见本班，学生仅见本人）
-- ============================================================

-- 2.1 体测记录管理（fitness:record:*）
--    功能：管理学生体测成绩的全生命周期（录入/修改/删除/查询/导出）
--    适用对象：教师（本班数据） + 学生（仅本人数据，通过 self:view 实现）
('fitness:record:view', '查看体测记录',
 '查询体测记录列表，支持按班级/学号/测试时间/项目筛选，受 data_scope 限制：教师仅见本班，学生仅见本人'),
('fitness:record:add', '新增体测记录',
 '录入学生体测数据，包括身高、体重、肺活量、50米跑、800/1000米跑等项目成绩，一次可录入多条记录，支持批量导入'),
('fitness:record:update', '修改体测记录',
 '编辑已有的体测记录，用于数据纠错或补录，修改后自动记录更新时间和操作人，操作日志留痕便于审计'),
('fitness:record:delete', '删除体测记录',
 '删除体测记录（通常保留不删，仅在数据严重错误时由管理员操作），建议使用逻辑删除或状态标记替代物理删除'),
('fitness:record:export', '导出体测记录',
 '将体测数据导出为 Excel 或 PDF 格式，支持按班级/年级/项目范围导出，用于归档、打印或向上级报送纸质报告'),

-- 2.2 体测汇总分析（fitness:summary:*）
--    功能：提供体测数据的统计分析能力，支持多维度汇总与可视化展示
--    适用对象：教师（本班汇总） + 学生（本人汇总，通过 data_scope 限制）
('fitness:summary:view', '查看体测汇总',
 '查看体测统计分析结果，包括班级/年级/全校各项目的平均分、及格率、优秀率等汇总指标，支持图表可视化展示'),
('fitness:summary:export', '导出体测汇总',
 '导出体测分析报告，包含统计图表与汇总数据，支持多种格式（Excel/PDF/Word），用于教学评估与向上级汇报'),
-- ============================================================
-- 三、用户管理模块（user:*）
--    权限说明：管理用户扩展信息（user_profile / student_profile / teacher_profile）
--              与 system:user:*（系统用户管理）不同，本模块针对特定用户类型的扩展信息
--    包含：学生档案管理（student 子资源）+ 教师档案管理（teacher 子资源）
--    设计原则：
--       1. 学生核心隐私字段（学号、身份证号）仅 ADMIN 可修改，教师仅可查看及修改辅助字段
--       2. 教师档案信息（工号等）ADMIN 可全量管理，教师本人仅可查看/修改非敏感字段
-- ============================================================

-- 3.1 学生档案管理（user:student:*）
--    功能：管理学生扩展信息（student_profile 表）
--    适用对象：教师（本班学生）+ ADMIN（全部学生）
--    与 system:user:* 的区别：
--       system:user:* 控制的是 sys_user 表（登录账号/密码/状态）
--       user:student:* 控制的是 student_profile 表（学号/班级/身份证号/家庭地址等扩展信息）
--    安全约束：update 操作在 Service 层需做字段白名单校验，禁止修改学号和身份证号
('user:student:view', '查看学生档案',
 '查看学生详细档案信息（含学号、班级、身份证号、家庭地址、联系电话等），受 data_scope=2（本班）限制，教师仅可见本班学生'),
('user:student:update', '修改学生档案',
 '修改学生非核心信息（如家庭地址、联系电话、紧急联系人、备注），不可修改学号和身份证号，该类字段变更需 ADMIN 通过 system:user:update 操作'),

-- 3.2 教师档案管理（user:teacher:*）
--    功能：管理教师扩展信息（teacher_profile 表）
--    适用对象：教师本人（仅查看/修改自己的信息）+ ADMIN（全部教师）
--    与 system:user:* 的区别：
--       system:user:* 控制的是 sys_user 表（登录账号/密码/状态）
--       user:teacher:* 控制的是 teacher_profile 表（工号/性别等扩展信息）
--    安全约束：update 操作需校验操作人是否为本人或 ADMIN，禁止教师修改他人的档案
('user:teacher:view', '查看教师档案',
 '查看教师档案信息（含工号、性别、所属校区等），教师本人可查看自己的档案，ADMIN 可查看全部'),
('user:teacher:update', '修改教师档案',
 '修改教师档案信息（如性别、备注等），教师本人仅可修改自己的非敏感字段，ADMIN 可修改全部');

-- ============================================================
-- 管理员 → ADMIN 角色绑定
-- 说明：将系统初始化管理员账号（admin）与 ADMIN 角色关联，授予系统最高权限
-- 适用场景：仅用于系统初始化阶段，确保系统启动后存在一个拥有全部权限的管理员
-- 安全警告：此绑定赋予用户全部权限（包括系统配置、用户管理、权限分配等），
--          生产环境部署后，应立即创建新的管理员账号并禁用此初始账号（status=0），
--          或强制要求 admin 在首次登录时修改密码。
-- 权限来源：ADMIN 角色的权限通过在 sys_role_permission 中使用 CROSS JOIN
--          与 sys_permission 全量关联实现（参见角色-权限绑定脚本）
-- ============================================================
INSERT INTO sys_user_role (user_id, role_id)
SELECT u.user_id, r.role_id
FROM sys_user u
         JOIN sys_role r
              ON r.role_code = 'ADMIN'
WHERE u.username = 'admin';

-- ============================================================
-- 教师 → TEACHER 角色绑定
-- 说明：将示例教师账号（12018007 张建国）与 TEACHER 角色关联，
--       授予体测数据录入、修改及本班数据查看等教学管理权限
-- 适用场景：系统初始化阶段创建示例教师账号，用于开发测试和功能演示
-- 权限范围：通过 TEACHER 角色获得以下权限：
--           - fitness:record:view / add / update（体测记录管理）
--           - fitness:summary:view（体测汇总查看）
--           - student:profile:view / update（学生档案查看与有限修改）
-- 数据权限：配合 TEACHER 角色的 data_scope=2（本班），
--           所有查询操作仅返回该教师任教班级的学生数据
-- 前置条件：执行此绑定前，需确保：
--           1. sys_user 表中已存在 username='12018007' 的用户记录
--           2. sys_role 表中已存在 role_code='TEACHER' 的角色记录
--           3. 教师-班级关联表（teacher_class）中已配置该教师的任教班级
-- 安全提示：初始密码为默认值（123456），部署生产环境前应强制教师修改密码
-- ============================================================
INSERT INTO sys_user_role (user_id, role_id)
SELECT u.user_id, r.role_id
FROM sys_user u
         JOIN sys_role r ON r.role_code = 'TEACHER'
WHERE u.username = '12018007';

-- ============================================================
-- 学生 → STUDENT 角色绑定
-- 说明：将示例学生账号（412251401 肖成）与 STUDENT 角色关联，
--       授予查看本人体测数据及个人汇总分析等权限
-- 适用场景：系统初始化阶段创建示例学生账号，用于开发测试和功能演示
-- 权限范围：通过 STUDENT 角色获得以下权限：
--           - fitness:record:self:view（仅查看本人体测记录）
--           - fitness:record:view（配合 data_scope=1，实际仅返回本人数据）
--           - fitness:summary:view（配合 data_scope=1，仅返回本人的汇总分析）
-- 数据权限：配合 STUDENT 角色的 data_scope=1（仅本人），
--           所有查询操作强制过滤当前登录用户的 user_id，
--           确保学生无法查看或操作任何他人的数据
-- 前置条件：执行此绑定前，需确保：
--           1. sys_user 表中已存在 username='412251401' 的用户记录
--           2. sys_role 表中已存在 role_code='STUDENT' 的角色记录
-- 安全提示：初始密码为默认值（123456），部署生产环境前应强制学生修改密码；
--           学生仅拥有只读权限，无任何录入、修改或删除操作能力
-- ============================================================
INSERT INTO sys_user_role (user_id, role_id)
SELECT u.user_id, r.role_id
FROM sys_user u
         JOIN sys_role r ON r.role_code = 'STUDENT'
WHERE u.username = '412251401';

-- ============================================================
-- ADMIN 角色 → 全部权限绑定
-- 说明：使用 CROSS JOIN（笛卡尔积）将 ADMIN 角色与系统中所有权限点全量关联，
--       授予系统管理员全部操作权限（包括系统管理 + 体测管理 + 学生档案管理）
-- 适用场景：系统初始化阶段，确保 ADMIN 角色拥有系统全部功能权限
-- 权限来源：ADMIN 角色的数据权限由 sys_role.data_scope=0（全部数据）控制，
--           功能权限由本语句授予的全部权限点控制，
--           两者共同构成 ADMIN 的完整权限矩阵
-- 执行机制：SELECT 子查询动态获取 role_id 和所有 perm_id，
--           避免硬编码 ID，确保与已有数据保持一致
-- 安全警告：此绑定赋予 ADMIN 角色全部权限（包括删除用户/角色/权限等危险操作），
--           生产环境中应严格控制 ADMIN 角色的授予范围，仅限运维负责人使用
-- 后续维护：新增权限点后，需重新执行此脚本（或使用 CROSS JOIN 覆盖），
--           否则新权限不会自动授予 ADMIN 角色
-- ============================================================
INSERT INTO sys_role_permission (role_id, perm_id)
SELECT r.role_id, p.perm_id
FROM sys_role r
         CROSS JOIN sys_permission p -- CROSS JOIN = 笛卡尔积，将 ADMIN 与每个权限点逐一配对
WHERE r.role_code = 'ADMIN';

-- ============================================================
-- TEACHER 角色 → 权限绑定
-- 说明：为教师角色授予体测管理及学生档案查看等相关权限，
--       支持教师完成日常教学管理工作（录入体测成绩、查看本班数据、管理学生档案）
-- 适用场景：系统初始化阶段，为 TEACHER 角色配置固定的功能权限集合
-- 权限范围：通过本语句授予以下权限点：
--           1. fitness:record:view     - 查看体测记录（受 data_scope=2 限制，仅本班）
--           2. fitness:record:add      - 新增体测记录（录入本班学生体测成绩）
--           3. fitness:record:update   - 修改体测记录（纠错或补录本班数据）
--           4. fitness:summary:view    - 查看体测汇总（仅本班统计汇总数据）
--           5. user:student:view       - 查看学生档案（本班学生基本信息）
--           6. user:student:update     - 修改学生档案（仅限非敏感字段：地址/电话等）
-- 数据权限配合：所有查询/修改操作配合 TEACHER 角色的 data_scope=2（本班），
--              自动过滤仅返回该教师任教班级的学生数据，无需在业务代码中额外编写班级过滤逻辑
-- 安全约束：student:profile:update 在 Service 层需做字段白名单校验，
--           禁止教师修改学号（student_no）、身份证号（id_card）等核心不可变字段
-- 后续维护：如需为教师角色新增权限（如导出功能、跨班级查看等），
--           需在此语句的 p.perm_code IN (...) 列表中追加对应的权限编码，并重新执行
-- ============================================================
INSERT INTO sys_role_permission (role_id, perm_id)
SELECT r.role_id, p.perm_id
FROM sys_role r
JOIN sys_permission p ON p.perm_code IN (
    -- 体测记录管理（本班范围）
    'fitness:record:view',
    'fitness:record:add',
    'fitness:record:update',
    -- 体测汇总查看（本班范围）
    'fitness:summary:view',
    -- 学生档案管理（本班学生，仅查看和有限修改）
    'user:student:view',
    'user:student:update',
    -- 教师档案管理（仅查看和有限修改）
    'user:teacher:view',
    'user:teacher:update'
)
WHERE r.role_code = 'TEACHER';

-- ============================================================
-- STUDENT 角色 → 权限绑定
-- 说明：为学生角色授予仅查看本人数据的权限，
--       支持学生登录后查看自己的体测记录和个人汇总分析
-- 适用场景：系统初始化阶段，为 STUDENT 角色配置固定的功能权限集合
-- 权限范围：通过本语句授予以下权限点：
--           1. fitness:record:self:view - 学生专用查看权限（仅本人体测记录）
--           2. fitness:record:view      - 通用查看权限（配合 data_scope=1，实际仅返回本人数据）
--           3. fitness:summary:view     - 查看体测汇总（配合 data_scope=1，仅返回本人的汇总分析）
-- 数据权限配合：所有查询操作配合 STUDENT 角色的 data_scope=1（仅本人），
--              系统在 Service 层强制追加当前登录用户的 user_id 过滤条件，
--              确保学生无法通过修改请求参数或 SQL 注入等方式查看他人数据
-- 安全约束：学生仅拥有只读权限，无任何录入（add）、修改（update）、删除（delete）或导出（export）权限，
--           这是系统中最严格的权限集，遵循最小权限原则
-- 前置条件：执行此绑定前，需确保：
--           1. sys_role 表中已存在 role_code='STUDENT' 的角色记录
--           2. sys_permission 表中已存在上述三个权限编码的记录
-- 后续维护：如需为学生角色新增权限（如查看班级排名等），
--           需在此语句的 p.perm_code IN (...) 列表中追加对应的权限编码，并重新执行
-- ============================================================
INSERT INTO sys_role_permission (role_id, perm_id)
SELECT r.role_id, p.perm_id
FROM sys_role r
JOIN sys_permission p ON p.perm_code IN (
    'fitness:record:view',        -- 配合 data_scope=1，仅返回本人数据
    'fitness:summary:view'        -- 配合 data_scope=1，仅返回本人的汇总分析
)
WHERE r.role_code = 'STUDENT';
