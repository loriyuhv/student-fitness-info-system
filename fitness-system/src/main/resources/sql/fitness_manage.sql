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

-- 体测项目字典表：测什么、怎么测、单位是什么
DROP TABLE IF EXISTS fitness_item;
CREATE TABLE fitness_item
(
    item_id         BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '体测项目ID',
    item_code       VARCHAR(50) NOT NULL COMMENT '项目编码',
    item_name       VARCHAR(50) NOT NULL COMMENT '项目名称',
    min_valid_value DECIMAL(10, 2) COMMENT '物理最小合理值',
    max_valid_value DECIMAL(10, 2) COMMENT '物理最大合理值',
    item_type       TINYINT     NOT NULL COMMENT '项目类型：1-基础体征 2-体能项目',
    unit            VARCHAR(20) COMMENT '计量单位',
    value_type      TINYINT     NOT NULL COMMENT '值类型：1-数值型 2-计数型',
    precision_scale TINYINT COMMENT '小数位数',
    is_score_item   TINYINT  DEFAULT 1 COMMENT '是否计入总分',
    sort_order      INT      DEFAULT 0 COMMENT '显示顺序',
    status          TINYINT  DEFAULT 1 COMMENT '状态：0-停用 1-启用',
    deleted         TINYINT  DEFAULT 0 COMMENT '逻辑删除',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_item_code (item_code)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='体测项目字典表';

-- 先确认项目编码，后续评分规则引用这些编码
INSERT INTO fitness_item (item_code, item_name, min_valid_value, max_valid_value, item_type, unit, value_type,
                          is_score_item, sort_order)
VALUES ('BMI', '体重指数', 10, 50, 1, 'kg/m²', 1, 1, 1),
       ('VITAL_CAPACITY', '肺活量', 500, 9999, 2, 'ml', 1, 1, 2),
       ('50M', '50米跑', 4, 15, 2, 's', 1, 1, 3),
       ('SIT_AND_REACH', '坐位体前屈', -20, 40, 2, 'cm', 1, 1, 4),
       ('STANDING_LONG_JUMP', '立定跳远', 50, 350, 2, 'cm', 1, 1, 5),
       ('PULL_UP', '引体向上（男）/ 仰卧起坐（女）', 0, 100, 2, '次', 1, 1, 6),
       ('RUN_1000_800', '1000米跑（男）/ 800米跑（女）', 120, 600, 2, 's', 1, 1, 7);

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

-- 评分规则集表
DROP TABLE IF EXISTS fitness_score_rule_set;
CREATE TABLE fitness_score_rule_set
(
    rule_set_id   BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评分规则集ID',
    rule_set_code VARCHAR(50)  NOT NULL COMMENT '规则集编码',
    rule_set_name VARCHAR(100) NOT NULL COMMENT '规则集名称',
    grade_min     TINYINT      NOT NULL COMMENT '最小年级',
    grade_max     TINYINT      NOT NULL COMMENT '最大年级',
    status        TINYINT DEFAULT 1 COMMENT '状态：0-停用 1-启用',
    deleted       TINYINT DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_rule_set_code (rule_set_code, deleted)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='体测评分规则集表';

-- 体测评分规则表
DROP TABLE IF EXISTS fitness_score_rule;
CREATE TABLE fitness_score_rule
(
    rule_id     BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评分规则ID',
    rule_set_id BIGINT           NOT NULL COMMENT '评分规则集ID（逻辑外键：fitness_score_rule_set.rule_set_id）',
    item_id     BIGINT           NOT NULL COMMENT '体测项目ID（逻辑外键：fitness_item.item_id）',
    item_code   VARCHAR(50)      NOT NULL COMMENT '项目编码',
    gender      TINYINT UNSIGNED NOT NULL COMMENT '性别：1-男 2-女',
    min_value   DECIMAL(10, 2) COMMENT '最小值（含）',
    max_value   DECIMAL(10, 2) COMMENT '最大值（不含）',
    score       DECIMAL(5, 2)    NOT NULL COMMENT '该区间对应得分',
    status      TINYINT  DEFAULT 1 COMMENT '状态：0-停用 1-启用',
    deleted     TINYINT  DEFAULT 0 COMMENT '逻辑删除',
    sort_order  INT      DEFAULT 0 COMMENT '规则排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_rule_set_item_gender_range (rule_set_id, item_code, gender, min_value, max_value),
    KEY idx_rule_lookup (rule_set_id, item_code, gender)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='体测评分规则表';

-- 体测总分评分等级规则表
DROP TABLE IF EXISTS fitness_score_level_rule;
CREATE TABLE fitness_score_level_rule
(
    level_rule_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评分等级规则ID',
    rule_set_id   BIGINT           NOT NULL COMMENT '评分规则集ID（如：大一大二 / 大三大四）',
    gender        TINYINT UNSIGNED NOT NULL COMMENT '性别：1-男 2-女',
    min_score     DECIMAL(5, 2)    NOT NULL COMMENT '最低分（含）',
    max_score     DECIMAL(5, 2)    NOT NULL COMMENT '最高分（不含）',
    level_code    VARCHAR(20)      NOT NULL COMMENT '等级编码：EXCELLENT / GOOD / PASS / FAIL',
    level_name    VARCHAR(20)      NOT NULL COMMENT '等级名称：优秀 / 良好 / 及格 / 不及格',
    status        TINYINT  DEFAULT 1 COMMENT '状态：0-停用 1-启用',
    deleted       TINYINT  DEFAULT 0 COMMENT '逻辑删除',
    sort_order    INT      DEFAULT 0 COMMENT '排序',
    create_time   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_rule (
                        rule_set_id, gender, min_score, max_score, deleted
        ),
    KEY idx_lookup (rule_set_id, gender)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='体测总分评分等级规则表';

-- 体重等级规则表
DROP TABLE IF EXISTS fitness_weight_level_rule;
CREATE TABLE fitness_weight_level_rule
(
    weight_level_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '体重等级规则ID',
    rule_set_id     BIGINT        NOT NULL COMMENT '规则集ID（如：大学生标准）',
    gender          TINYINT  DEFAULT 0 COMMENT '性别：0-通用 1-男 2-女',
    min_bmi         DECIMAL(4, 2) NOT NULL COMMENT 'BMI下限（含）',
    max_bmi         DECIMAL(4, 2) NOT NULL COMMENT 'BMI上限（不含）',
    level_code      VARCHAR(20)   NOT NULL COMMENT '等级编码',
    level_name      VARCHAR(20)   NOT NULL COMMENT '等级名称',
    score           TINYINT       NOT NULL COMMENT '体重等级得分',
    status          TINYINT  DEFAULT 1 COMMENT '状态：0-停用 1-启用',
    deleted         TINYINT  DEFAULT 0 COMMENT '逻辑删除',
    sort_order      INT      DEFAULT 0 COMMENT '排序',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_rule (
                        rule_set_id, gender, min_bmi, max_bmi, deleted
        ),
    KEY idx_lookup (rule_set_id, gender)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='体重等级规则表（BMI）';

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