-- 参考：https://fdty.fudan.edu.cn/b0/31/c29222a307249/page.htm

-- 体测记录主表：某学生的一次体测
DROP TABLE IF EXISTS student_fitness_record;
CREATE TABLE student_fitness_record
(
    record_id             BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '体测记录ID',
    -- 关联信息
    student_user_id       BIGINT   NOT NULL COMMENT '学生用户ID（逻辑外键：sys_user.user_id）',
    operator_user_id      BIGINT   NOT NULL COMMENT '操作人用户ID（逻辑外键：sys_user.user_id）',
    -- 体测基本信息
    test_time             DATETIME NOT NULL COMMENT '体测时间',
    test_round            TINYINT UNSIGNED COMMENT '第几次体测（展示用）',
    test_type             TINYINT  DEFAULT 1 COMMENT '体测类型：1-正式 2-补测 3-重测',
    -- 汇总结果（当次体测结论）
    total_score           DECIMAL(5, 2) COMMENT '体测总分',
    total_level           VARCHAR(20) COMMENT '体测等级',
    model_id              BIGINT COMMENT '使用的体质模型ID',
    k_value               TINYINT COMMENT 'K值：K-means聚类值',
    physique_type         VARCHAR(20) COMMENT '体质类型',
    sport_prescription    VARCHAR(50) COMMENT '运动处方',
    -- 规则上下文
    score_rule_version    VARCHAR(20) COMMENT '评分规则版本',
    physique_rule_version VARCHAR(20) COMMENT '体质判定规则版本',
    -- 业务状态
    status                TINYINT  DEFAULT 1 COMMENT '状态：0-作废 1-正常 2-已汇总',
    confirm_status        TINYINT  DEFAULT 0 COMMENT '确认状态：0-未确认 1-已确认',
    confirm_time          DATETIME COMMENT '确认时间',
    -- 通用字段
    deleted               TINYINT  DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    remark                VARCHAR(200) COMMENT '备注',
    create_by             VARCHAR(50) COMMENT '创建人',
    create_time           DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by             VARCHAR(50) COMMENT '更新人',
    update_time           DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    -- 索引
    KEY idx_student_time (student_user_id, test_time),
    KEY idx_student_status_deleted (student_user_id, status, deleted),
    KEY idx_operator (operator_user_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='学生体测记录表';

-- 项目-性别适用表：男测什么、女测什么
DROP TABLE IF EXISTS fitness_item_gender;
CREATE TABLE fitness_item_gender
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    item_id     BIGINT           NOT NULL COMMENT '体测项目ID',
    gender      TINYINT UNSIGNED NOT NULL COMMENT '性别：1-男 2-女',
    deleted     TINYINT  DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_item_gender (item_id, gender, deleted)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='体测项目性别适用表';

-- 体测项目明细表：这次体测，具体每个项目的值
DROP TABLE IF EXISTS student_fitness_record_item;
CREATE TABLE student_fitness_record_item
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录明细ID',
    record_id   BIGINT NOT NULL COMMENT '体测记录ID',
    item_id     BIGINT NOT NULL COMMENT '体测项目ID',
    item_value  DECIMAL(10, 2) COMMENT '项目值',
    score       DECIMAL(5, 2) COMMENT '项目得分',
    deleted     TINYINT  DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_record_item (record_id, item_id, deleted)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='体测记录项目明细表';

-- K 值规则表
DROP TABLE IF EXISTS fitness_k_rule;
CREATE TABLE fitness_k_rule
(
    k_value            TINYINT     NOT NULL COMMENT 'K值（聚类编号）',
    model_id           BIGINT      NOT NULL COMMENT '模型ID（逻辑外键）',

    physique_type      VARCHAR(20) NOT NULL COMMENT '体质类型',
    sport_prescription VARCHAR(50) NOT NULL COMMENT '运动处方',

    description        VARCHAR(200) COMMENT '规则说明',

    status             TINYINT  DEFAULT 1 COMMENT '状态：0-停用 1-启用',
    deleted            TINYINT  DEFAULT 0 COMMENT '逻辑删除',
    create_time        DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time        DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (model_id, k_value)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='K值-体质规则表';

-- 聚类模型表
DROP TABLE IF EXISTS fitness_cluster_model;
CREATE TABLE fitness_cluster_model
(
    model_id        BIGINT PRIMARY KEY AUTO_INCREMENT,
    gender          TINYINT NOT NULL COMMENT '性别：1-男 2-女',
    grade_group     TINYINT NOT NULL COMMENT '年级分组：1-低年级 2-高年级',

    feature_desc    VARCHAR(200) COMMENT '特征说明',
    cluster_count   TINYINT NOT NULL,

    scaler_params   JSON COMMENT '标准化参数',
    cluster_centers JSON COMMENT '簇中心',

    status          TINYINT  DEFAULT 1,
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='体质聚类模型表';

-- 学生体测汇总表
DROP TABLE IF EXISTS student_fitness_summary;
CREATE TABLE student_fitness_summary
(
    summary_id         BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '体测汇总ID',
    user_id            BIGINT NOT NULL COMMENT '学生用户ID（逻辑外键：sys_user.user_id）',
    record_id          BIGINT NOT NULL COMMENT '最近一次体测记录ID（逻辑外键：student_fitness_record.record_id）',
    total_score        DECIMAL(5, 2) COMMENT '体测总分',
    total_level        VARCHAR(20) COMMENT '总体等级',
    k_value            TINYINT COMMENT 'K值',
    physique_type      VARCHAR(20) COMMENT '体质类型',
    sport_prescription VARCHAR(50) COMMENT '运动处方',
    latest_test_time   DATETIME COMMENT '最近体测时间',
    status             TINYINT  DEFAULT 1 COMMENT '状态：0-无效，1-正常，2-冻结',
    deleted            TINYINT  DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    remark             VARCHAR(200) COMMENT '备注',
    create_by          VARCHAR(50) COMMENT '创建人',
    create_time        DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by          VARCHAR(50) COMMENT '更新人',
    update_time        DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    -- 逻辑一对一约束
    UNIQUE KEY uk_user_deleted (user_id, deleted),
    -- 高频查询索引
    KEY idx_user_status_deleted (user_id, status, deleted)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='学生体测汇总表';