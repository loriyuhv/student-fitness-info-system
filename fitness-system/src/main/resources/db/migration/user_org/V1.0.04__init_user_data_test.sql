# 通过用户账号查看用户扩展信息 sys_user 与 user_profile是一一对应的关系，通过user_id逻辑外键链接
SELECT su.*, up.*
FROM sys_user su
         LEFT JOIN user_profile up
                   ON su.user_id = up.user_id AND su.campus_id = up.campus_id AND up.deleted = 0
WHERE su.username = 'admin'
  AND su.campus_id = 1101
  AND su.status = 1
  AND su.deleted = 0;

SELECT su.*, up.*
FROM sys_user su
         LEFT JOIN user_profile up
                   ON su.user_id = up.user_id AND su.campus_id = up.campus_id AND up.deleted = 0
WHERE su.username = '12018007'
  AND su.campus_id = 1101
  AND su.status = 1
  AND su.deleted = 0;

# 通过用户账号查看学生扩展信息，例如班级ID，通过班级ID获取该学生所在班级的班级编码和班级名称
SELECT ci.class_name as '班级名称', ci.class_code as '班级编码'
FROM sys_user su
         LEFT JOIN student_profile sp
                   ON su.user_id = sp.user_id AND su.campus_id = sp.campus_id AND sp.status = 1 AND sp.deleted = 0
         LEFT JOIN class_info ci
                   ON sp.class_id = ci.class_id AND sp.campus_id = ci.campus_id AND ci.status = 1 AND ci.deleted = 0
WHERE su.username = '412251401'
  AND su.campus_id = 1101
  AND su.status = 1
  AND su.deleted = 0;

# 通过用户账号查看教师扩展信息，例如班级ID，通过班级ID获取该教师所负责班级的班级编码和班级名称
SELECT su.username as '教师名称', ci.class_code as '班级编码', ci.class_name as '班级名称'
FROM sys_user su
         LEFT JOIN teacher_profile tp
                   ON su.user_id = tp.user_id AND su.campus_id = tp.campus_id AND tp.status = 1 AND tp.deleted = 0
         LEFT JOIN teacher_class tc
                   ON tp.teacher_id = tc.teacher_id AND tp.campus_id = tc.campus_id AND tc.status = 1 AND tc.deleted = 0
         LEFT JOIN class_info ci
                   ON tc.class_id = ci.class_id AND tc.campus_id = ci.campus_id AND ci.status = 1 AND ci.deleted = 0
WHERE su.username = '12018007'
  AND su.campus_id = 1101
  AND su.status = 1
  AND su.deleted = 0;

-- ============================================================
-- 六、数据一致性验证（此部分为注释，不执行）
-- ============================================================
-- 【验证 1】查询所有用户及其扩展信息：
SELECT u.username,
       u.nickname,
       u.user_type,
       p.gender,
       p.birth_date,
       p.address,
       p.last_login_time
FROM sys_user u
         LEFT JOIN user_profile p ON u.user_id = p.user_id
WHERE u.deleted = 0
  AND u.username IN ('admin', '12018007', '412251401');

-- 【验证 2】查询张建国的完整信息（含任教班级）：
SELECT u.username,
       u.nickname,
       tp.teacher_no,
       tp.gender AS teacher_gender,
       ci.class_code,
       ci.class_name,
       ci.grade
FROM sys_user u
         JOIN teacher_profile tp ON u.user_id = tp.user_id AND tp.deleted = 0
         JOIN teacher_class tc ON tp.teacher_id = tc.teacher_id AND tc.deleted = 0 AND tc.status = 1
         JOIN class_info ci ON tc.class_id = ci.class_id AND ci.deleted = 0 AND ci.status = 1
WHERE u.username = '12018007'
  AND u.deleted = 0
  AND u.status = 1;

-- 【验证 3】查询王子轩的完整学籍信息：
SELECT u.username,
       u.nickname,
       sp.student_no,
       sp.major,
       sp.id_card,
       ci.class_code,
       ci.class_name,
       ci.grade
FROM sys_user u
         JOIN student_profile sp ON u.user_id = sp.user_id AND sp.deleted = 0
         LEFT JOIN class_info ci ON sp.class_id = ci.class_id AND ci.deleted = 0
WHERE u.username = '412251401'
  AND u.deleted = 0
  AND u.status = 1;

-- 【验证 4】验证教师的数据权限范围（应返回 3 个班级）：
SELECT COUNT(*) AS class_count
FROM teacher_class
WHERE teacher_id = (SELECT teacher_id FROM teacher_profile WHERE teacher_no = '12018007')
  AND deleted = 0
  AND status = 1;
--   -- 预期结果：3
