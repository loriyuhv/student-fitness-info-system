DROP TABLE user_profile;
CREATE TABLE user_profile (
  profile_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户信息ID',
  user_id BIGINT NOT NULL COMMENT '关联用户ID',
  campus_id BIGINT NOT NULL DEFAULT 1 COMMENT '校区ID',
  gender TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
  birth_date DATE COMMENT '出生日期',
  avatar_url VARCHAR(500) COMMENT '头像URL',
  address VARCHAR(255) COMMENT '联系地址',
  last_login_ip VARCHAR(45) COMMENT '最后登录IP',
  last_login_time DATETIME COMMENT '最后登录时间',
  deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  create_by VARCHAR(50) COMMENT '创建人',
  update_by VARCHAR(50) COMMENT '更新人',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_id (user_id),
  KEY idx_deleted (deleted),
  KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户扩展信息表';

DROP TABLE class_info;
CREATE TABLE class_info (
  class_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '班级ID',
  campus_id BIGINT NOT NULL DEFAULT 1 COMMENT '校区ID',
  class_code VARCHAR(30) NOT NULL COMMENT '班级编码（如：4122124）',
  class_name VARCHAR(50) NOT NULL COMMENT '班级名称',
  grade YEAR NOT NULL COMMENT '年级（入学年份）',
  student_count INT DEFAULT 0 COMMENT '学生人数',
  status TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-停用',
  deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  remark VARCHAR(200) COMMENT '备注',
  create_by VARCHAR(50) COMMENT '创建人',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_by VARCHAR(50) COMMENT '更新人',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_class_code (class_code, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级信息表';

DROP TABLE student_profile;
CREATE TABLE student_profile (
  student_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '学生信息ID',
  campus_id BIGINT NOT NULL DEFAULT 1 COMMENT '校区ID',
  user_id BIGINT NOT NULL COMMENT '关联sys_user.user_id（逻辑外键）',
  student_no VARCHAR(20) NOT NULL COMMENT '学号',
  class_id BIGINT COMMENT '班级ID（逻辑外键）',
  enroll_year INT COMMENT '入学年份',
  major VARCHAR(100) COMMENT '专业',
  id_card VARCHAR(18) COMMENT '身份证号',
  gender TINYINT UNSIGNED DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
  birth_date DATE COMMENT '出生日期',
  family_address VARCHAR(255) COMMENT '家庭地址',
  avatar_url VARCHAR(500) COMMENT '学生照片URL',
  remark VARCHAR(200) COMMENT '备注',
  status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
  deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  create_by VARCHAR(50) COMMENT '创建人',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_by VARCHAR(50) COMMENT '更新人',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_user_deleted (user_id, deleted),
  UNIQUE KEY uk_student_no_deleted (student_no, deleted),
  UNIQUE KEY uk_id_card_deleted (id_card, deleted),
  KEY idx_status_deleted (status, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生信息扩展表';

-- 教师扩展信息表
DROP TABLE teacher_profile;
CREATE TABLE teacher_profile (
  teacher_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '教师信息ID',
  campus_id BIGINT NOT NULL DEFAULT 1 COMMENT '校区ID',
  user_id BIGINT NOT NULL COMMENT '关联sys_user.user_id（逻辑外键）',
  teacher_no VARCHAR(20) NOT NULL COMMENT '教师工号',
  gender TINYINT UNSIGNED DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
  remark VARCHAR(200) COMMENT '备注',
  status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
  deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  create_by VARCHAR(50) COMMENT '创建人',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_by VARCHAR(50) COMMENT '更新人',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_user_deleted (user_id, deleted),
  UNIQUE KEY uk_teacher_no_deleted (teacher_no, deleted),
  KEY idx_status_deleted (status, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师信息表';

-- 教师-班级关系表
CREATE TABLE teacher_class (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  campus_id BIGINT NOT NULL DEFAULT 1 COMMENT '校区ID',
  teacher_id BIGINT NOT NULL COMMENT '教师ID（关联teacher_info.teacher_id）',
  class_id BIGINT NOT NULL COMMENT '班级ID（关联class_info.class_id）',
  status TINYINT DEFAULT 1 COMMENT '关系状态：1-有效，0-无效',
  deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  remark VARCHAR(200) COMMENT '备注',
  create_by VARCHAR(50) COMMENT '创建人',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_by VARCHAR(50) COMMENT '更新人',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_teacher_class (teacher_id, class_id, deleted)  -- 防止重复分配
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师-班级关系表';
