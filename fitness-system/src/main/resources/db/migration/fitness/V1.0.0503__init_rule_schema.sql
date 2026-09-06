-- 规则配置表建表：

-- ============================================================
-- 1. 评分规则（按年级/性别分）
-- ============================================================
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

-- ============================================================
-- 2.加分规则（fitness_bonus_rule）
-- ============================================================

-- 体测加分评分规则表
DROP TABLE IF EXISTS fitness_bonus_rule;
CREATE TABLE fitness_bonus_rule
(
    bonus_id    BIGINT PRIMARY KEY AUTO_INCREMENT,
    rule_set_id BIGINT           NOT NULL,
    item_code   VARCHAR(50)      NOT NULL,
    gender      TINYINT UNSIGNED NOT NULL,
    status      TINYINT  DEFAULT 1,
    deleted     TINYINT  DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_bonus_rule (rule_set_id, item_code, gender, deleted)
);

-- 体测加分明细表
DROP TABLE IF EXISTS fitness_bonus_detail;
CREATE TABLE fitness_bonus_detail
(
    detail_id     BIGINT PRIMARY KEY AUTO_INCREMENT,
    bonus_rule_id BIGINT  NOT NULL COMMENT '关联 fitness_bonus_rule.bonus_id',
    bonus_value   TINYINT NOT NULL COMMENT '加分数值 1-10',
    min_count     INT     NOT NULL COMMENT '达到此次数/成绩获得对应加分',
    status        TINYINT  DEFAULT 1,
    deleted       TINYINT  DEFAULT 0,
    create_time   DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_rule_bonus (bonus_rule_id, bonus_value, deleted)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='体测加分明细表';

-- ============================================================
-- 3. BMI 等级标准（单独处理）
-- BMI 不按年级细分，只按 性别 分（男生 BMI 标准略高）：
-- ============================================================

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

-- ============================================================
-- 4. 等级评定标准（总分 → 等级）
-- ============================================================

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


-- ============================================================
-- 5. 项目性别适用表
-- ============================================================

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

-- ============================================================
-- 6. K值规则
-- ============================================================

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
