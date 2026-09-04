-- 1. 体测项目字典初始化
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

-- 4. BMI 等级标准（单独处理）
-- BMI 不按年级细分，只按 性别 分（男生 BMI 标准略高）：

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

-- 男生 BMI（来自复旦官网）
INSERT INTO fitness_weight_level_rule (rule_set_id, gender, min_bmi, max_bmi, level_code, level_name, score, status)
VALUES (1, 1, 17.9, 23.9, 'NORMAL', '正常', 100, 1),
       (1, 1, 15.9, 17.9, 'LOW', '低体重', 80, 1),
       (1, 1, 23.9, 27.9, 'OVERWEIGHT', '超重', 80, 1),
       (1, 1, 10, 15.9, 'UNDERWEIGHT', '体重过低', 60, 1),
       (1, 1, 27.9, 50, 'OBESE', '肥胖', 60, 1);

-- 女生 BMI
INSERT INTO fitness_weight_level_rule (rule_set_id, gender, min_bmi, max_bmi, level_code, level_name, score, status)
VALUES (1, 2, 17.2, 23.9, 'NORMAL', '正常', 100, 1),
       (1, 2, 15.7, 17.2, 'LOW', '低体重', 80, 1),
       (1, 2, 23.9, 27.9, 'OVERWEIGHT', '超重', 80, 1),
       (1, 2, 10, 15.7, 'UNDERWEIGHT', '体重过低', 60, 1),
       (1, 2, 27.9, 50, 'OBESE', '肥胖', 60, 1);

-- 5. 等级评定标准（总分 → 等级）

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

-- 所有年级、性别通用（官网标准）
INSERT INTO fitness_score_level_rule (rule_set_id, gender, min_score, max_score, level_code, level_name, status)
VALUES (1, 0, 90, 100, 'EXCELLENT', '优秀', 1),
       (1, 0, 80, 90, 'GOOD', '良好', 1),
       (1, 0, 60, 80, 'PASS', '及格', 1),
       (1, 0, 0, 60, 'FAIL', '不及格', 1);
