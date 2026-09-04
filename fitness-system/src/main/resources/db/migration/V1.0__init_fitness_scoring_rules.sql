-- 2. 规则集（年级分组）
/*复旦标准的关键特征：
1）按年级分：大一和大二、大三和大四
2）按性别分：男、女
3）按项目分：7 个项目
4）分段计分：每个项目有多个分数段（如肺活量：100分、95分、90分……）
 */
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

-- 年级分组：大一大二（合并）、大三大四（合并）
INSERT INTO fitness_score_rule_set (rule_set_id, rule_set_code, rule_set_name, grade_min, grade_max, status)
VALUES (1, 'FRESHMAN_SOPHOMORE', '大一大二标准', 1, 2, 1),
       (2, 'JUNIOR_SENIOR', '大三大四标准', 3, 4, 1);

-- 3. 评分规则（按年级/性别分）

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
-- 规则集：大一大二 (rule_set_id = 1)
-- ============================================================

-- 1.1 大一大二男生 肺活量（值越大越好，正向）
INSERT INTO fitness_score_rule (rule_set_id, item_id, item_code, gender, min_value, max_value, score, status)
VALUES (1, 2, 'VITAL_CAPACITY', 1, 5040, 999999, 100, 1),
       (1, 2, 'VITAL_CAPACITY', 1, 4920, 5040, 95, 1),
       (1, 2, 'VITAL_CAPACITY', 1, 4800, 4920, 90, 1),
       (1, 2, 'VITAL_CAPACITY', 1, 4550, 4800, 85, 1),
       (1, 2, 'VITAL_CAPACITY', 1, 4300, 4550, 80, 1),
       (1, 2, 'VITAL_CAPACITY', 1, 4180, 4300, 78, 1),
       (1, 2, 'VITAL_CAPACITY', 1, 4060, 4180, 76, 1),
       (1, 2, 'VITAL_CAPACITY', 1, 3940, 4060, 74, 1),
       (1, 2, 'VITAL_CAPACITY', 1, 3820, 3940, 72, 1),
       (1, 2, 'VITAL_CAPACITY', 1, 3700, 3820, 70, 1),
       (1, 2, 'VITAL_CAPACITY', 1, 3580, 3700, 68, 1),
       (1, 2, 'VITAL_CAPACITY', 1, 3460, 3580, 66, 1),
       (1, 2, 'VITAL_CAPACITY', 1, 3340, 3460, 64, 1),
       (1, 2, 'VITAL_CAPACITY', 1, 3220, 3340, 62, 1),
       (1, 2, 'VITAL_CAPACITY', 1, 3100, 3220, 60, 1),
       (1, 2, 'VITAL_CAPACITY', 1, 2940, 3100, 50, 1),
       (1, 2, 'VITAL_CAPACITY', 1, 2780, 2940, 40, 1),
       (1, 2, 'VITAL_CAPACITY', 1, 2620, 2780, 30, 1),
       (1, 2, 'VITAL_CAPACITY', 1, 2460, 2620, 20, 1),
       (1, 2, 'VITAL_CAPACITY', 1, 2300, 2460, 10, 1),
       (1, 2, 'VITAL_CAPACITY', 1, 0, 2300, 0, 1);

-- 1.2 大一大二男生 50米跑（值越小越好，反向；偏移 +0.01）
INSERT INTO fitness_score_rule (rule_set_id, item_id, item_code, gender, min_value, max_value, score, status)
VALUES (1, 3, '50M', 1, 0, 6.71, 100, 1),
       (1, 3, '50M', 1, 6.71, 6.81, 95, 1),
       (1, 3, '50M', 1, 6.81, 6.91, 90, 1),
       (1, 3, '50M', 1, 6.91, 7.01, 85, 1),
       (1, 3, '50M', 1, 7.01, 7.11, 80, 1),
       (1, 3, '50M', 1, 7.11, 7.31, 78, 1),
       (1, 3, '50M', 1, 7.31, 7.51, 76, 1),
       (1, 3, '50M', 1, 7.51, 7.71, 74, 1),
       (1, 3, '50M', 1, 7.71, 7.91, 72, 1),
       (1, 3, '50M', 1, 7.91, 8.11, 70, 1),
       (1, 3, '50M', 1, 8.11, 8.31, 68, 1),
       (1, 3, '50M', 1, 8.31, 8.51, 66, 1),
       (1, 3, '50M', 1, 8.51, 8.71, 64, 1),
       (1, 3, '50M', 1, 8.71, 8.91, 62, 1),
       (1, 3, '50M', 1, 8.91, 9.11, 60, 1),
       (1, 3, '50M', 1, 9.11, 9.31, 50, 1),
       (1, 3, '50M', 1, 9.31, 9.51, 40, 1),
       (1, 3, '50M', 1, 9.51, 9.71, 30, 1),
       (1, 3, '50M', 1, 9.71, 9.91, 20, 1),
       (1, 3, '50M', 1, 9.91, 10.11, 10, 1),
       (1, 3, '50M', 1, 10.11, 999999, 0, 1);

-- 1.3 大一大二男生 坐位体前屈（正向）
INSERT INTO fitness_score_rule (rule_set_id, item_id, item_code, gender, min_value, max_value, score, status)
VALUES (1, 4, 'SIT_AND_REACH', 1, 24.9, 999999, 100, 1),
       (1, 4, 'SIT_AND_REACH', 1, 23.1, 24.9, 95, 1),
       (1, 4, 'SIT_AND_REACH', 1, 21.3, 23.1, 90, 1),
       (1, 4, 'SIT_AND_REACH', 1, 19.5, 21.3, 85, 1),
       (1, 4, 'SIT_AND_REACH', 1, 17.7, 19.5, 80, 1),
       (1, 4, 'SIT_AND_REACH', 1, 16.3, 17.7, 78, 1),
       (1, 4, 'SIT_AND_REACH', 1, 14.9, 16.3, 76, 1),
       (1, 4, 'SIT_AND_REACH', 1, 13.5, 14.9, 74, 1),
       (1, 4, 'SIT_AND_REACH', 1, 12.1, 13.5, 72, 1),
       (1, 4, 'SIT_AND_REACH', 1, 10.7, 12.1, 70, 1),
       (1, 4, 'SIT_AND_REACH', 1, 9.3, 10.7, 68, 1),
       (1, 4, 'SIT_AND_REACH', 1, 7.9, 9.3, 66, 1),
       (1, 4, 'SIT_AND_REACH', 1, 6.5, 7.9, 64, 1),
       (1, 4, 'SIT_AND_REACH', 1, 5.1, 6.5, 62, 1),
       (1, 4, 'SIT_AND_REACH', 1, 3.7, 5.1, 60, 1),
       (1, 4, 'SIT_AND_REACH', 1, 2.7, 3.7, 50, 1),
       (1, 4, 'SIT_AND_REACH', 1, 1.7, 2.7, 40, 1),
       (1, 4, 'SIT_AND_REACH', 1, 0.7, 1.7, 30, 1),
       (1, 4, 'SIT_AND_REACH', 1, -0.3, 0.7, 20, 1),
       (1, 4, 'SIT_AND_REACH', 1, -1.3, -0.3, 10, 1),
       (1, 4, 'SIT_AND_REACH', 1, -999999, -1.3, 0, 1);

-- 1.4 大一大二男生 立定跳远（正向）
INSERT INTO fitness_score_rule (rule_set_id, item_id, item_code, gender, min_value, max_value, score, status)
VALUES (1, 5, 'STANDING_LONG_JUMP', 1, 273, 999999, 100, 1),
       (1, 5, 'STANDING_LONG_JUMP', 1, 268, 273, 95, 1),
       (1, 5, 'STANDING_LONG_JUMP', 1, 263, 268, 90, 1),
       (1, 5, 'STANDING_LONG_JUMP', 1, 256, 263, 85, 1),
       (1, 5, 'STANDING_LONG_JUMP', 1, 248, 256, 80, 1),
       (1, 5, 'STANDING_LONG_JUMP', 1, 244, 248, 78, 1),
       (1, 5, 'STANDING_LONG_JUMP', 1, 240, 244, 76, 1),
       (1, 5, 'STANDING_LONG_JUMP', 1, 236, 240, 74, 1),
       (1, 5, 'STANDING_LONG_JUMP', 1, 232, 236, 72, 1),
       (1, 5, 'STANDING_LONG_JUMP', 1, 228, 232, 70, 1),
       (1, 5, 'STANDING_LONG_JUMP', 1, 224, 228, 68, 1),
       (1, 5, 'STANDING_LONG_JUMP', 1, 220, 224, 66, 1),
       (1, 5, 'STANDING_LONG_JUMP', 1, 216, 220, 64, 1),
       (1, 5, 'STANDING_LONG_JUMP', 1, 212, 216, 62, 1),
       (1, 5, 'STANDING_LONG_JUMP', 1, 208, 212, 60, 1),
       (1, 5, 'STANDING_LONG_JUMP', 1, 203, 208, 50, 1),
       (1, 5, 'STANDING_LONG_JUMP', 1, 198, 203, 40, 1),
       (1, 5, 'STANDING_LONG_JUMP', 1, 193, 198, 30, 1),
       (1, 5, 'STANDING_LONG_JUMP', 1, 188, 193, 20, 1),
       (1, 5, 'STANDING_LONG_JUMP', 1, 183, 188, 10, 1),
       (1, 5, 'STANDING_LONG_JUMP', 1, 0, 183, 0, 1);

-- 1.5 大一大二男生 引体向上（正向，中间有空档）
INSERT INTO fitness_score_rule (rule_set_id, item_id, item_code, gender, min_value, max_value, score, status)
VALUES (1, 6, 'PULL_UP', 1, 19, 999999, 100, 1),
       (1, 6, 'PULL_UP', 1, 18, 19, 95, 1),
       (1, 6, 'PULL_UP', 1, 17, 18, 90, 1),
       (1, 6, 'PULL_UP', 1, 16, 17, 85, 1),
       (1, 6, 'PULL_UP', 1, 15, 16, 80, 1),
       (1, 6, 'PULL_UP', 1, 14, 15, 76, 1),
       (1, 6, 'PULL_UP', 1, 13, 14, 72, 1),
       (1, 6, 'PULL_UP', 1, 12, 13, 68, 1),
       (1, 6, 'PULL_UP', 1, 11, 12, 64, 1),
       (1, 6, 'PULL_UP', 1, 10, 11, 60, 1),
       (1, 6, 'PULL_UP', 1, 9, 10, 50, 1),
       (1, 6, 'PULL_UP', 1, 8, 9, 40, 1),
       (1, 6, 'PULL_UP', 1, 7, 8, 30, 1),
       (1, 6, 'PULL_UP', 1, 6, 7, 20, 1),
       (1, 6, 'PULL_UP', 1, 5, 6, 10, 1),
       (1, 6, 'PULL_UP', 1, 0, 5, 0, 1);

-- 1.6 大一大二男生 1000米跑（反向，时间越短越好；偏移 +1秒）
INSERT INTO fitness_score_rule (rule_set_id, item_id, item_code, gender, min_value, max_value, score, status)
VALUES (1, 7, 'RUN_1000_800', 1, 0, 198, 100, 1),
       (1, 7, 'RUN_1000_800', 1, 198, 203, 95, 1),
       (1, 7, 'RUN_1000_800', 1, 203, 208, 90, 1),
       (1, 7, 'RUN_1000_800', 1, 208, 215, 85, 1),
       (1, 7, 'RUN_1000_800', 1, 215, 223, 80, 1),
       (1, 7, 'RUN_1000_800', 1, 223, 228, 78, 1),
       (1, 7, 'RUN_1000_800', 1, 228, 233, 76, 1),
       (1, 7, 'RUN_1000_800', 1, 233, 238, 74, 1),
       (1, 7, 'RUN_1000_800', 1, 238, 243, 72, 1),
       (1, 7, 'RUN_1000_800', 1, 243, 248, 70, 1),
       (1, 7, 'RUN_1000_800', 1, 248, 253, 68, 1),
       (1, 7, 'RUN_1000_800', 1, 253, 258, 66, 1),
       (1, 7, 'RUN_1000_800', 1, 258, 263, 64, 1),
       (1, 7, 'RUN_1000_800', 1, 263, 268, 62, 1),
       (1, 7, 'RUN_1000_800', 1, 268, 273, 60, 1),
       (1, 7, 'RUN_1000_800', 1, 273, 293, 50, 1),
       (1, 7, 'RUN_1000_800', 1, 293, 313, 40, 1),
       (1, 7, 'RUN_1000_800', 1, 313, 333, 30, 1),
       (1, 7, 'RUN_1000_800', 1, 333, 353, 20, 1),
       (1, 7, 'RUN_1000_800', 1, 353, 373, 10, 1),
       (1, 7, 'RUN_1000_800', 1, 373, 999999, 0, 1);

-- ============================================================
-- 1.7 大一大二女生 肺活量（正向）
INSERT INTO fitness_score_rule (rule_set_id, item_id, item_code, gender, min_value, max_value, score, status)
VALUES (1, 2, 'VITAL_CAPACITY', 2, 3400, 999999, 100, 1),
       (1, 2, 'VITAL_CAPACITY', 2, 3250, 3400, 95, 1),
       (1, 2, 'VITAL_CAPACITY', 2, 3100, 3250, 90, 1),
       (1, 2, 'VITAL_CAPACITY', 2, 2950, 3100, 85, 1),
       (1, 2, 'VITAL_CAPACITY', 2, 2800, 2950, 80, 1),
       (1, 2, 'VITAL_CAPACITY', 2, 2700, 2800, 78, 1),
       (1, 2, 'VITAL_CAPACITY', 2, 2600, 2700, 76, 1),
       (1, 2, 'VITAL_CAPACITY', 2, 2500, 2600, 74, 1),
       (1, 2, 'VITAL_CAPACITY', 2, 2400, 2500, 72, 1),
       (1, 2, 'VITAL_CAPACITY', 2, 2300, 2400, 70, 1),
       (1, 2, 'VITAL_CAPACITY', 2, 2200, 2300, 68, 1),
       (1, 2, 'VITAL_CAPACITY', 2, 2100, 2200, 66, 1),
       (1, 2, 'VITAL_CAPACITY', 2, 2000, 2100, 64, 1),
       (1, 2, 'VITAL_CAPACITY', 2, 1900, 2000, 62, 1),
       (1, 2, 'VITAL_CAPACITY', 2, 1800, 1900, 60, 1),
       (1, 2, 'VITAL_CAPACITY', 2, 1680, 1800, 50, 1),
       (1, 2, 'VITAL_CAPACITY', 2, 1580, 1680, 40, 1),
       (1, 2, 'VITAL_CAPACITY', 2, 1480, 1580, 30, 1),
       (1, 2, 'VITAL_CAPACITY', 2, 1380, 1480, 20, 1),
       (1, 2, 'VITAL_CAPACITY', 2, 1280, 1380, 10, 1),
       (1, 2, 'VITAL_CAPACITY', 2, 0, 1280, 0, 1);

-- 1.8 大一大二女生 50米跑（反向，偏移+0.01）
INSERT INTO fitness_score_rule (rule_set_id, item_id, item_code, gender, min_value, max_value, score, status)
VALUES (1, 3, '50M', 2, 0, 7.51, 100, 1),
       (1, 3, '50M', 2, 7.51, 7.71, 95, 1),
       (1, 3, '50M', 2, 7.71, 7.91, 90, 1),
       (1, 3, '50M', 2, 7.91, 8.11, 85, 1),
       (1, 3, '50M', 2, 8.11, 8.31, 80, 1),
       (1, 3, '50M', 2, 8.31, 8.51, 78, 1),
       (1, 3, '50M', 2, 8.51, 8.71, 76, 1),
       (1, 3, '50M', 2, 8.71, 8.91, 74, 1),
       (1, 3, '50M', 2, 8.91, 9.11, 72, 1),
       (1, 3, '50M', 2, 9.11, 9.31, 70, 1),
       (1, 3, '50M', 2, 9.31, 9.51, 68, 1),
       (1, 3, '50M', 2, 9.51, 9.71, 66, 1),
       (1, 3, '50M', 2, 9.71, 9.91, 64, 1),
       (1, 3, '50M', 2, 9.91, 10.11, 62, 1),
       (1, 3, '50M', 2, 10.11, 10.31, 60, 1),
       (1, 3, '50M', 2, 10.31, 10.51, 50, 1),
       (1, 3, '50M', 2, 10.51, 10.71, 40, 1),
       (1, 3, '50M', 2, 10.71, 10.91, 30, 1),
       (1, 3, '50M', 2, 10.91, 11.11, 20, 1),
       (1, 3, '50M', 2, 11.11, 11.31, 10, 1),
       (1, 3, '50M', 2, 11.31, 999999, 0, 1);

-- 1.9 大一大二女生 坐位体前屈（正向）
INSERT INTO fitness_score_rule (rule_set_id, item_id, item_code, gender, min_value, max_value, score, status)
VALUES (1, 4, 'SIT_AND_REACH', 2, 25.8, 999999, 100, 1),
       (1, 4, 'SIT_AND_REACH', 2, 23.5, 25.8, 95, 1),
       (1, 4, 'SIT_AND_REACH', 2, 21.0, 23.5, 90, 1),
       (1, 4, 'SIT_AND_REACH', 2, 18.5, 21.0, 85, 1),
       (1, 4, 'SIT_AND_REACH', 2, 16.0, 18.5, 80, 1),
       (1, 4, 'SIT_AND_REACH', 2, 14.5, 16.0, 78, 1),
       (1, 4, 'SIT_AND_REACH', 2, 13.0, 14.5, 76, 1),
       (1, 4, 'SIT_AND_REACH', 2, 11.5, 13.0, 74, 1),
       (1, 4, 'SIT_AND_REACH', 2, 10.0, 11.5, 72, 1),
       (1, 4, 'SIT_AND_REACH', 2, 8.5, 10.0, 70, 1),
       (1, 4, 'SIT_AND_REACH', 2, 7.0, 8.5, 68, 1),
       (1, 4, 'SIT_AND_REACH', 2, 5.5, 7.0, 66, 1),
       (1, 4, 'SIT_AND_REACH', 2, 4.0, 5.5, 64, 1),
       (1, 4, 'SIT_AND_REACH', 2, 2.5, 4.0, 62, 1),
       (1, 4, 'SIT_AND_REACH', 2, 1.0, 2.5, 60, 1),
       (1, 4, 'SIT_AND_REACH', 2, 0.0, 1.0, 50, 1),
       (1, 4, 'SIT_AND_REACH', 2, -1.0, 0.0, 40, 1),
       (1, 4, 'SIT_AND_REACH', 2, -2.0, -1.0, 30, 1),
       (1, 4, 'SIT_AND_REACH', 2, -3.0, -2.0, 20, 1),
       (1, 4, 'SIT_AND_REACH', 2, -4.0, -3.0, 10, 1),
       (1, 4, 'SIT_AND_REACH', 2, -999999, -4.0, 0, 1);

-- 1.10 大一大二女生 立定跳远（正向）
INSERT INTO fitness_score_rule (rule_set_id, item_id, item_code, gender, min_value, max_value, score, status)
VALUES (1, 5, 'STANDING_LONG_JUMP', 2, 207, 999999, 100, 1),
       (1, 5, 'STANDING_LONG_JUMP', 2, 201, 207, 95, 1),
       (1, 5, 'STANDING_LONG_JUMP', 2, 195, 201, 90, 1),
       (1, 5, 'STANDING_LONG_JUMP', 2, 189, 195, 85, 1),
       (1, 5, 'STANDING_LONG_JUMP', 2, 183, 189, 80, 1),
       (1, 5, 'STANDING_LONG_JUMP', 2, 179, 183, 78, 1),
       (1, 5, 'STANDING_LONG_JUMP', 2, 175, 179, 76, 1),
       (1, 5, 'STANDING_LONG_JUMP', 2, 171, 175, 74, 1),
       (1, 5, 'STANDING_LONG_JUMP', 2, 167, 171, 72, 1),
       (1, 5, 'STANDING_LONG_JUMP', 2, 163, 167, 70, 1),
       (1, 5, 'STANDING_LONG_JUMP', 2, 159, 163, 68, 1),
       (1, 5, 'STANDING_LONG_JUMP', 2, 155, 159, 66, 1),
       (1, 5, 'STANDING_LONG_JUMP', 2, 151, 155, 64, 1),
       (1, 5, 'STANDING_LONG_JUMP', 2, 147, 151, 62, 1),
       (1, 5, 'STANDING_LONG_JUMP', 2, 143, 147, 60, 1),
       (1, 5, 'STANDING_LONG_JUMP', 2, 138, 143, 50, 1),
       (1, 5, 'STANDING_LONG_JUMP', 2, 133, 138, 40, 1),
       (1, 5, 'STANDING_LONG_JUMP', 2, 128, 133, 30, 1),
       (1, 5, 'STANDING_LONG_JUMP', 2, 123, 128, 20, 1),
       (1, 5, 'STANDING_LONG_JUMP', 2, 118, 123, 10, 1),
       (1, 5, 'STANDING_LONG_JUMP', 2, 0, 118, 0, 1);

-- 1.11 大一大二女生 仰卧起坐（正向，用PULL_UP字段）
INSERT INTO fitness_score_rule (rule_set_id, item_id, item_code, gender, min_value, max_value, score, status)
VALUES (1, 6, 'PULL_UP', 2, 56, 999999, 100, 1),
       (1, 6, 'PULL_UP', 2, 52, 56, 95, 1),
       (1, 6, 'PULL_UP', 2, 48, 52, 90, 1),
       (1, 6, 'PULL_UP', 2, 44, 48, 85, 1),
       (1, 6, 'PULL_UP', 2, 40, 44, 80, 1),
       (1, 6, 'PULL_UP', 2, 38, 40, 78, 1),
       (1, 6, 'PULL_UP', 2, 36, 38, 76, 1),
       (1, 6, 'PULL_UP', 2, 34, 36, 74, 1),
       (1, 6, 'PULL_UP', 2, 32, 34, 72, 1),
       (1, 6, 'PULL_UP', 2, 30, 32, 70, 1),
       (1, 6, 'PULL_UP', 2, 28, 30, 68, 1),
       (1, 6, 'PULL_UP', 2, 26, 28, 66, 1),
       (1, 6, 'PULL_UP', 2, 24, 26, 64, 1),
       (1, 6, 'PULL_UP', 2, 22, 24, 62, 1),
       (1, 6, 'PULL_UP', 2, 20, 22, 60, 1),
       (1, 6, 'PULL_UP', 2, 18, 20, 50, 1),
       (1, 6, 'PULL_UP', 2, 16, 18, 40, 1),
       (1, 6, 'PULL_UP', 2, 14, 16, 30, 1),
       (1, 6, 'PULL_UP', 2, 12, 14, 20, 1),
       (1, 6, 'PULL_UP', 2, 10, 12, 10, 1),
       (1, 6, 'PULL_UP', 2, 0, 10, 0, 1);

-- 1.12 大一大二女生 800米跑（反向，偏移+0.01秒）
INSERT INTO fitness_score_rule (rule_set_id, item_id, item_code, gender, min_value, max_value, score, status)
VALUES (1, 7, 'RUN_1000_800', 2, 0, 199, 100, 1),
       (1, 7, 'RUN_1000_800', 2, 199, 205, 95, 1),
       (1, 7, 'RUN_1000_800', 2, 205, 211, 90, 1),
       (1, 7, 'RUN_1000_800', 2, 211, 218, 85, 1),
       (1, 7, 'RUN_1000_800', 2, 218, 225, 80, 1),
       (1, 7, 'RUN_1000_800', 2, 225, 230, 78, 1),
       (1, 7, 'RUN_1000_800', 2, 230, 235, 76, 1),
       (1, 7, 'RUN_1000_800', 2, 235, 240, 74, 1),
       (1, 7, 'RUN_1000_800', 2, 240, 245, 72, 1),
       (1, 7, 'RUN_1000_800', 2, 245, 250, 70, 1),
       (1, 7, 'RUN_1000_800', 2, 250, 255, 68, 1),
       (1, 7, 'RUN_1000_800', 2, 255, 260, 66, 1),
       (1, 7, 'RUN_1000_800', 2, 260, 265, 64, 1),
       (1, 7, 'RUN_1000_800', 2, 265, 270, 62, 1),
       (1, 7, 'RUN_1000_800', 2, 270, 275, 60, 1),
       (1, 7, 'RUN_1000_800', 2, 275, 295, 50, 1),
       (1, 7, 'RUN_1000_800', 2, 295, 315, 40, 1),
       (1, 7, 'RUN_1000_800', 2, 315, 335, 30, 1),
       (1, 7, 'RUN_1000_800', 2, 335, 355, 20, 1),
       (1, 7, 'RUN_1000_800', 2, 355, 375, 10, 1),
       (1, 7, 'RUN_1000_800', 2, 375, 999999, 0, 1);

-- ============================================================
-- 规则集 2：大三大四 (rule_set_id = 2)
-- ============================================================

-- 2.1 大三大四男生 肺活量（值越大越好，正向）
INSERT INTO fitness_score_rule (rule_set_id, item_id, item_code, gender, min_value, max_value, score, status)
VALUES (2, 2, 'VITAL_CAPACITY', 1, 5140, 999999, 100, 1),
       (2, 2, 'VITAL_CAPACITY', 1, 5020, 5140, 95, 1),
       (2, 2, 'VITAL_CAPACITY', 1, 4900, 5020, 90, 1),
       (2, 2, 'VITAL_CAPACITY', 1, 4650, 4900, 85, 1),
       (2, 2, 'VITAL_CAPACITY', 1, 4400, 4650, 80, 1),
       (2, 2, 'VITAL_CAPACITY', 1, 4280, 4400, 78, 1),
       (2, 2, 'VITAL_CAPACITY', 1, 4160, 4280, 76, 1),
       (2, 2, 'VITAL_CAPACITY', 1, 4040, 4160, 74, 1),
       (2, 2, 'VITAL_CAPACITY', 1, 3920, 4040, 72, 1),
       (2, 2, 'VITAL_CAPACITY', 1, 3800, 3920, 70, 1),
       (2, 2, 'VITAL_CAPACITY', 1, 3680, 3800, 68, 1),
       (2, 2, 'VITAL_CAPACITY', 1, 3560, 3680, 66, 1),
       (2, 2, 'VITAL_CAPACITY', 1, 3440, 3560, 64, 1),
       (2, 2, 'VITAL_CAPACITY', 1, 3320, 3440, 62, 1),
       (2, 2, 'VITAL_CAPACITY', 1, 3200, 3320, 60, 1),
       (2, 2, 'VITAL_CAPACITY', 1, 3030, 3200, 50, 1),
       (2, 2, 'VITAL_CAPACITY', 1, 2860, 3030, 40, 1),
       (2, 2, 'VITAL_CAPACITY', 1, 2690, 2860, 30, 1),
       (2, 2, 'VITAL_CAPACITY', 1, 2520, 2690, 20, 1),
       (2, 2, 'VITAL_CAPACITY', 1, 2350, 2520, 10, 1),
       (2, 2, 'VITAL_CAPACITY', 1, 0, 2350, 0, 1);

-- 2.2 大三大四男生 50米跑（反向，偏移+0.01）
INSERT INTO fitness_score_rule (rule_set_id, item_id, item_code, gender, min_value, max_value, score, status)
VALUES (2, 3, '50M', 1, 0, 6.61, 100, 1),
       (2, 3, '50M', 1, 6.61, 6.71, 95, 1),
       (2, 3, '50M', 1, 6.71, 6.81, 90, 1),
       (2, 3, '50M', 1, 6.81, 6.91, 85, 1),
       (2, 3, '50M', 1, 6.91, 7.01, 80, 1),
       (2, 3, '50M', 1, 7.01, 7.21, 78, 1),
       (2, 3, '50M', 1, 7.21, 7.41, 76, 1),
       (2, 3, '50M', 1, 7.41, 7.61, 74, 1),
       (2, 3, '50M', 1, 7.61, 7.81, 72, 1),
       (2, 3, '50M', 1, 7.81, 8.01, 70, 1),
       (2, 3, '50M', 1, 8.01, 8.21, 68, 1),
       (2, 3, '50M', 1, 8.21, 8.41, 66, 1),
       (2, 3, '50M', 1, 8.41, 8.61, 64, 1),
       (2, 3, '50M', 1, 8.61, 8.81, 62, 1),
       (2, 3, '50M', 1, 8.81, 9.01, 60, 1),
       (2, 3, '50M', 1, 9.01, 9.21, 50, 1),
       (2, 3, '50M', 1, 9.21, 9.41, 40, 1),
       (2, 3, '50M', 1, 9.41, 9.61, 30, 1),
       (2, 3, '50M', 1, 9.61, 9.81, 20, 1),
       (2, 3, '50M', 1, 9.81, 10.01, 10, 1),
       (2, 3, '50M', 1, 10.01, 999999, 0, 1);

-- 2.3 大三大四男生 坐位体前屈
INSERT INTO fitness_score_rule (rule_set_id, item_id, item_code, gender, min_value, max_value, score, status)
VALUES (2, 4, 'SIT_AND_REACH', 1, 25.1, 999999, 100, 1),
       (2, 4, 'SIT_AND_REACH', 1, 23.3, 25.1, 95, 1),
       (2, 4, 'SIT_AND_REACH', 1, 21.5, 23.3, 90, 1),
       (2, 4, 'SIT_AND_REACH', 1, 19.9, 21.5, 85, 1),
       (2, 4, 'SIT_AND_REACH', 1, 18.2, 19.9, 80, 1),
       (2, 4, 'SIT_AND_REACH', 1, 16.8, 18.2, 78, 1),
       (2, 4, 'SIT_AND_REACH', 1, 15.4, 16.8, 76, 1),
       (2, 4, 'SIT_AND_REACH', 1, 14.0, 15.4, 74, 1),
       (2, 4, 'SIT_AND_REACH', 1, 12.6, 14.0, 72, 1),
       (2, 4, 'SIT_AND_REACH', 1, 11.2, 12.6, 70, 1),
       (2, 4, 'SIT_AND_REACH', 1, 9.8, 11.2, 68, 1),
       (2, 4, 'SIT_AND_REACH', 1, 8.4, 9.8, 66, 1),
       (2, 4, 'SIT_AND_REACH', 1, 7.0, 8.4, 64, 1),
       (2, 4, 'SIT_AND_REACH', 1, 5.6, 7.0, 62, 1),
       (2, 4, 'SIT_AND_REACH', 1, 4.2, 5.6, 60, 1),
       (2, 4, 'SIT_AND_REACH', 1, 3.2, 4.2, 50, 1),
       (2, 4, 'SIT_AND_REACH', 1, 2.2, 3.2, 40, 1),
       (2, 4, 'SIT_AND_REACH', 1, 1.2, 2.2, 30, 1),
       (2, 4, 'SIT_AND_REACH', 1, 0.2, 1.2, 20, 1),
       (2, 4, 'SIT_AND_REACH', 1, -0.8, 0.2, 10, 1),
       (2, 4, 'SIT_AND_REACH', 1, -999999, -0.8, 0, 1);

-- 2.4 大三大四男生 立定跳远
INSERT INTO fitness_score_rule (rule_set_id, item_id, item_code, gender, min_value, max_value, score, status)
VALUES (2, 5, 'STANDING_LONG_JUMP', 1, 275, 999999, 100, 1),
       (2, 5, 'STANDING_LONG_JUMP', 1, 270, 275, 95, 1),
       (2, 5, 'STANDING_LONG_JUMP', 1, 265, 270, 90, 1),
       (2, 5, 'STANDING_LONG_JUMP', 1, 258, 265, 85, 1),
       (2, 5, 'STANDING_LONG_JUMP', 1, 250, 258, 80, 1),
       (2, 5, 'STANDING_LONG_JUMP', 1, 246, 250, 78, 1),
       (2, 5, 'STANDING_LONG_JUMP', 1, 242, 246, 76, 1),
       (2, 5, 'STANDING_LONG_JUMP', 1, 238, 242, 74, 1),
       (2, 5, 'STANDING_LONG_JUMP', 1, 234, 238, 72, 1),
       (2, 5, 'STANDING_LONG_JUMP', 1, 230, 234, 70, 1),
       (2, 5, 'STANDING_LONG_JUMP', 1, 226, 230, 68, 1),
       (2, 5, 'STANDING_LONG_JUMP', 1, 222, 226, 66, 1),
       (2, 5, 'STANDING_LONG_JUMP', 1, 218, 222, 64, 1),
       (2, 5, 'STANDING_LONG_JUMP', 1, 214, 218, 62, 1),
       (2, 5, 'STANDING_LONG_JUMP', 1, 210, 214, 60, 1),
       (2, 5, 'STANDING_LONG_JUMP', 1, 205, 210, 50, 1),
       (2, 5, 'STANDING_LONG_JUMP', 1, 200, 205, 40, 1),
       (2, 5, 'STANDING_LONG_JUMP', 1, 195, 200, 30, 1),
       (2, 5, 'STANDING_LONG_JUMP', 1, 190, 195, 20, 1),
       (2, 5, 'STANDING_LONG_JUMP', 1, 185, 190, 10, 1),
       (2, 5, 'STANDING_LONG_JUMP', 1, 0, 185, 0, 1);

-- 2.5 大三大四男生 引体向上
INSERT INTO fitness_score_rule (rule_set_id, item_id, item_code, gender, min_value, max_value, score, status)
VALUES (2, 6, 'PULL_UP', 1, 20, 999999, 100, 1),
       (2, 6, 'PULL_UP', 1, 19, 20, 95, 1),
       (2, 6, 'PULL_UP', 1, 18, 19, 90, 1),
       (2, 6, 'PULL_UP', 1, 17, 18, 85, 1),
       (2, 6, 'PULL_UP', 1, 16, 17, 80, 1),
       (2, 6, 'PULL_UP', 1, 15, 16, 76, 1),
       (2, 6, 'PULL_UP', 1, 14, 15, 72, 1),
       (2, 6, 'PULL_UP', 1, 13, 14, 68, 1),
       (2, 6, 'PULL_UP', 1, 12, 13, 64, 1),
       (2, 6, 'PULL_UP', 1, 11, 12, 60, 1),
       (2, 6, 'PULL_UP', 1, 10, 11, 50, 1),
       (2, 6, 'PULL_UP', 1, 9, 10, 40, 1),
       (2, 6, 'PULL_UP', 1, 8, 9, 30, 1),
       (2, 6, 'PULL_UP', 1, 7, 8, 20, 1),
       (2, 6, 'PULL_UP', 1, 6, 7, 10, 1),
       (2, 6, 'PULL_UP', 1, 0, 6, 0, 1);

-- 2.6 大三大四男生 1000米跑（反向，偏移+1秒）
INSERT INTO fitness_score_rule (rule_set_id, item_id, item_code, gender, min_value, max_value, score, status)
VALUES (2, 7, 'RUN_1000_800', 1, 0, 196, 100, 1),
       (2, 7, 'RUN_1000_800', 1, 196, 201, 95, 1),
       (2, 7, 'RUN_1000_800', 1, 201, 206, 90, 1),
       (2, 7, 'RUN_1000_800', 1, 206, 213, 85, 1),
       (2, 7, 'RUN_1000_800', 1, 213, 221, 80, 1),
       (2, 7, 'RUN_1000_800', 1, 221, 226, 78, 1),
       (2, 7, 'RUN_1000_800', 1, 226, 231, 76, 1),
       (2, 7, 'RUN_1000_800', 1, 231, 236, 74, 1),
       (2, 7, 'RUN_1000_800', 1, 236, 241, 72, 1),
       (2, 7, 'RUN_1000_800', 1, 241, 246, 70, 1),
       (2, 7, 'RUN_1000_800', 1, 246, 251, 68, 1),
       (2, 7, 'RUN_1000_800', 1, 251, 256, 66, 1),
       (2, 7, 'RUN_1000_800', 1, 256, 261, 64, 1),
       (2, 7, 'RUN_1000_800', 1, 261, 266, 62, 1),
       (2, 7, 'RUN_1000_800', 1, 266, 271, 60, 1),
       (2, 7, 'RUN_1000_800', 1, 271, 291, 50, 1),
       (2, 7, 'RUN_1000_800', 1, 291, 311, 40, 1),
       (2, 7, 'RUN_1000_800', 1, 311, 331, 30, 1),
       (2, 7, 'RUN_1000_800', 1, 331, 351, 20, 1),
       (2, 7, 'RUN_1000_800', 1, 351, 371, 10, 1),
       (2, 7, 'RUN_1000_800', 1, 371, 999999, 0, 1);

-- 2.7 大三大四女生 肺活量
INSERT INTO fitness_score_rule (rule_set_id, item_id, item_code, gender, min_value, max_value, score, status)
VALUES (2, 2, 'VITAL_CAPACITY', 2, 3450, 999999, 100, 1),
       (2, 2, 'VITAL_CAPACITY', 2, 3300, 3450, 95, 1),
       (2, 2, 'VITAL_CAPACITY', 2, 3150, 3300, 90, 1),
       (2, 2, 'VITAL_CAPACITY', 2, 3000, 3150, 85, 1),
       (2, 2, 'VITAL_CAPACITY', 2, 2850, 3000, 80, 1),
       (2, 2, 'VITAL_CAPACITY', 2, 2750, 2850, 78, 1),
       (2, 2, 'VITAL_CAPACITY', 2, 2650, 2750, 76, 1),
       (2, 2, 'VITAL_CAPACITY', 2, 2550, 2650, 74, 1),
       (2, 2, 'VITAL_CAPACITY', 2, 2450, 2550, 72, 1),
       (2, 2, 'VITAL_CAPACITY', 2, 2350, 2450, 70, 1),
       (2, 2, 'VITAL_CAPACITY', 2, 2250, 2350, 68, 1),
       (2, 2, 'VITAL_CAPACITY', 2, 2150, 2250, 66, 1),
       (2, 2, 'VITAL_CAPACITY', 2, 2050, 2150, 64, 1),
       (2, 2, 'VITAL_CAPACITY', 2, 1950, 2050, 62, 1),
       (2, 2, 'VITAL_CAPACITY', 2, 1850, 1950, 60, 1),
       (2, 2, 'VITAL_CAPACITY', 2, 1730, 1850, 50, 1),
       (2, 2, 'VITAL_CAPACITY', 2, 1630, 1730, 40, 1),
       (2, 2, 'VITAL_CAPACITY', 2, 1530, 1630, 30, 1),
       (2, 2, 'VITAL_CAPACITY', 2, 1430, 1530, 20, 1),
       (2, 2, 'VITAL_CAPACITY', 2, 1330, 1430, 10, 1),
       (2, 2, 'VITAL_CAPACITY', 2, 0, 1330, 0, 1);

-- 2.8 大三大四女生 50米跑
INSERT INTO fitness_score_rule (rule_set_id, item_id, item_code, gender, min_value, max_value, score, status)
VALUES (2, 3, '50M', 2, 0, 7.41, 100, 1),
       (2, 3, '50M', 2, 7.41, 7.61, 95, 1),
       (2, 3, '50M', 2, 7.61, 7.81, 90, 1),
       (2, 3, '50M', 2, 7.81, 8.01, 85, 1),
       (2, 3, '50M', 2, 8.01, 8.21, 80, 1),
       (2, 3, '50M', 2, 8.21, 8.41, 78, 1),
       (2, 3, '50M', 2, 8.41, 8.61, 76, 1),
       (2, 3, '50M', 2, 8.61, 8.81, 74, 1),
       (2, 3, '50M', 2, 8.81, 9.01, 72, 1),
       (2, 3, '50M', 2, 9.01, 9.21, 70, 1),
       (2, 3, '50M', 2, 9.21, 9.41, 68, 1),
       (2, 3, '50M', 2, 9.41, 9.61, 66, 1),
       (2, 3, '50M', 2, 9.61, 9.81, 64, 1),
       (2, 3, '50M', 2, 9.81, 10.01, 62, 1),
       (2, 3, '50M', 2, 10.01, 10.21, 60, 1),
       (2, 3, '50M', 2, 10.21, 10.41, 50, 1),
       (2, 3, '50M', 2, 10.41, 10.61, 40, 1),
       (2, 3, '50M', 2, 10.61, 10.81, 30, 1),
       (2, 3, '50M', 2, 10.81, 11.01, 20, 1),
       (2, 3, '50M', 2, 11.01, 11.21, 10, 1),
       (2, 3, '50M', 2, 11.21, 999999, 0, 1);

-- 2.9 大三大四女生 坐位体前屈
INSERT INTO fitness_score_rule (rule_set_id, item_id, item_code, gender, min_value, max_value, score, status)
VALUES (2, 4, 'SIT_AND_REACH', 2, 26.3, 999999, 100, 1),
       (2, 4, 'SIT_AND_REACH', 2, 23.8, 26.3, 95, 1),
       (2, 4, 'SIT_AND_REACH', 2, 21.3, 23.8, 90, 1),
       (2, 4, 'SIT_AND_REACH', 2, 18.8, 21.3, 85, 1),
       (2, 4, 'SIT_AND_REACH', 2, 16.3, 18.8, 80, 1),
       (2, 4, 'SIT_AND_REACH', 2, 14.8, 16.3, 78, 1),
       (2, 4, 'SIT_AND_REACH', 2, 13.3, 14.8, 76, 1),
       (2, 4, 'SIT_AND_REACH', 2, 11.8, 13.3, 74, 1),
       (2, 4, 'SIT_AND_REACH', 2, 10.3, 11.8, 72, 1),
       (2, 4, 'SIT_AND_REACH', 2, 8.8, 10.3, 70, 1),
       (2, 4, 'SIT_AND_REACH', 2, 7.3, 8.8, 68, 1),
       (2, 4, 'SIT_AND_REACH', 2, 5.8, 7.3, 66, 1),
       (2, 4, 'SIT_AND_REACH', 2, 4.3, 5.8, 64, 1),
       (2, 4, 'SIT_AND_REACH', 2, 2.8, 4.3, 62, 1),
       (2, 4, 'SIT_AND_REACH', 2, 1.3, 2.8, 60, 1),
       (2, 4, 'SIT_AND_REACH', 2, 0.3, 1.3, 50, 1),
       (2, 4, 'SIT_AND_REACH', 2, -0.7, 0.3, 40, 1),
       (2, 4, 'SIT_AND_REACH', 2, -1.7, -0.7, 30, 1),
       (2, 4, 'SIT_AND_REACH', 2, -2.7, -1.7, 20, 1),
       (2, 4, 'SIT_AND_REACH', 2, -3.7, -2.7, 10, 1),
       (2, 4, 'SIT_AND_REACH', 2, -999999, -3.7, 0, 1);

-- 2.10 大三大四女生 立定跳远
INSERT INTO fitness_score_rule (rule_set_id, item_id, item_code, gender, min_value, max_value, score, status)
VALUES (2, 5, 'STANDING_LONG_JUMP', 2, 208, 999999, 100, 1), -- 原 209
       (2, 5, 'STANDING_LONG_JUMP', 2, 202, 208, 95, 1),
       (2, 5, 'STANDING_LONG_JUMP', 2, 196, 202, 90, 1),
       (2, 5, 'STANDING_LONG_JUMP', 2, 189, 196, 85, 1),
       (2, 5, 'STANDING_LONG_JUMP', 2, 182, 189, 80, 1),
       (2, 5, 'STANDING_LONG_JUMP', 2, 179, 182, 78, 1),
       (2, 5, 'STANDING_LONG_JUMP', 2, 176, 179, 76, 1),
       (2, 5, 'STANDING_LONG_JUMP', 2, 173, 176, 74, 1),
       (2, 5, 'STANDING_LONG_JUMP', 2, 170, 173, 72, 1),
       (2, 5, 'STANDING_LONG_JUMP', 2, 167, 170, 70, 1),
       (2, 5, 'STANDING_LONG_JUMP', 2, 164, 167, 68, 1),
       (2, 5, 'STANDING_LONG_JUMP', 2, 161, 164, 66, 1),
       (2, 5, 'STANDING_LONG_JUMP', 2, 158, 161, 64, 1),
       (2, 5, 'STANDING_LONG_JUMP', 2, 155, 158, 62, 1),
       (2, 5, 'STANDING_LONG_JUMP', 2, 152, 155, 60, 1),
       (2, 5, 'STANDING_LONG_JUMP', 2, 147, 152, 50, 1),
       (2, 5, 'STANDING_LONG_JUMP', 2, 142, 147, 40, 1),
       (2, 5, 'STANDING_LONG_JUMP', 2, 137, 142, 30, 1),
       (2, 5, 'STANDING_LONG_JUMP', 2, 132, 137, 20, 1),
       (2, 5, 'STANDING_LONG_JUMP', 2, 127, 132, 10, 1),
       (2, 5, 'STANDING_LONG_JUMP', 2, 0, 127, 0, 1);

-- 2.11 大三大四女生 仰卧起坐
INSERT INTO fitness_score_rule (rule_set_id, item_id, item_code, gender, min_value, max_value, score, status)
VALUES (2, 6, 'PULL_UP', 2, 57, 999999, 100, 1),-- 100分：57次（原 58）
       (2, 6, 'PULL_UP', 2, 55, 57, 95, 1),     -- 54-56 → 95分
       (2, 6, 'PULL_UP', 2, 53, 55, 90, 1),     -- 52-54 → 90分
       (2, 6, 'PULL_UP', 2, 50, 53, 85, 1),     -- 49-52 → 85分（注意：实际是49次85分）
       (2, 6, 'PULL_UP', 2, 47, 50, 80, 1),     -- 46-49 → 80分
       (2, 6, 'PULL_UP', 2, 45, 47, 78, 1),     -- 44-46 → 78分
       (2, 6, 'PULL_UP', 2, 43, 45, 76, 1),     -- 42-44 → 76分
       (2, 6, 'PULL_UP', 2, 41, 43, 74, 1),     -- 40-42 → 74分
       (2, 6, 'PULL_UP', 2, 39, 41, 72, 1),     -- 38-40 → 72分
       (2, 6, 'PULL_UP', 2, 37, 39, 70, 1),     -- 36-38 → 70分
       (2, 6, 'PULL_UP', 2, 35, 37, 68, 1),     -- 34-36 → 68分
       (2, 6, 'PULL_UP', 2, 33, 35, 66, 1),     -- 32-34 → 66分
       (2, 6, 'PULL_UP', 2, 31, 33, 64, 1),     -- 30-32 → 64分
       (2, 6, 'PULL_UP', 2, 29, 31, 62, 1),     -- 28-30 → 62分
       (2, 6, 'PULL_UP', 2, 27, 29, 60, 1),     -- 26-28 → 60分
       (2, 6, 'PULL_UP', 2, 25, 27, 50, 1),     -- 24-26 → 50分
       (2, 6, 'PULL_UP', 2, 23, 25, 40, 1),     -- 22-24 → 40分
       (2, 6, 'PULL_UP', 2, 21, 23, 30, 1),     -- 20-22 → 30分
       (2, 6, 'PULL_UP', 2, 19, 21, 20, 1),     -- 18-20 → 20分
       (2, 6, 'PULL_UP', 2, 17, 19, 10, 1),     -- 16-18 → 10分
       (2, 6, 'PULL_UP', 2, 0, 17, 0, 1);
-- 0-16 → 0分

-- 2.12 大三大四女生 800米跑
INSERT INTO fitness_score_rule (rule_set_id, item_id, item_code, gender, min_value, max_value, score, status)
VALUES (2, 7, 'RUN_1000_800', 2, 0, 197, 100, 1),
       (2, 7, 'RUN_1000_800', 2, 197, 203, 95, 1),
       (2, 7, 'RUN_1000_800', 2, 203, 209, 90, 1),
       (2, 7, 'RUN_1000_800', 2, 209, 216, 85, 1),
       (2, 7, 'RUN_1000_800', 2, 216, 223, 80, 1),
       (2, 7, 'RUN_1000_800', 2, 223, 228, 78, 1),
       (2, 7, 'RUN_1000_800', 2, 228, 233, 76, 1),
       (2, 7, 'RUN_1000_800', 2, 233, 238, 74, 1),
       (2, 7, 'RUN_1000_800', 2, 238, 243, 72, 1),
       (2, 7, 'RUN_1000_800', 2, 243, 248, 70, 1),
       (2, 7, 'RUN_1000_800', 2, 248, 253, 68, 1),
       (2, 7, 'RUN_1000_800', 2, 253, 258, 66, 1),
       (2, 7, 'RUN_1000_800', 2, 258, 263, 64, 1),
       (2, 7, 'RUN_1000_800', 2, 263, 268, 62, 1),
       (2, 7, 'RUN_1000_800', 2, 268, 273, 60, 1),
       (2, 7, 'RUN_1000_800', 2, 273, 293, 50, 1),
       (2, 7, 'RUN_1000_800', 2, 293, 313, 40, 1),
       (2, 7, 'RUN_1000_800', 2, 313, 333, 30, 1),
       (2, 7, 'RUN_1000_800', 2, 333, 353, 20, 1),
       (2, 7, 'RUN_1000_800', 2, 353, 373, 10, 1),
       (2, 7, 'RUN_1000_800', 2, 373, 999999, 0, 1);

-- ============================================================
-- 加分规则（fitness_bonus_rule）
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
-- 1. 插入加分规则主表
-- ============================================================

INSERT INTO fitness_bonus_rule (rule_set_id, item_code, gender, status)
VALUES (1, 'PULL_UP', 1, 1),      -- bonus_id 预计为 1
       (2, 'PULL_UP', 1, 1),      -- bonus_id 预计为 2
       (1, 'RUN_1000_800', 1, 1), -- bonus_id 预计为 3
       (2, 'RUN_1000_800', 1, 1), -- bonus_id 预计为 4
       (1, 'PULL_UP', 2, 1),      -- bonus_id 预计为 5
       (2, 'PULL_UP', 2, 1),      -- bonus_id 预计为 6
       (1, 'RUN_1000_800', 2, 1), -- bonus_id 预计为 7
       (2, 'RUN_1000_800', 2, 1);
-- bonus_id 预计为 8

-- ============================================================
-- 2. 插入加分明细
-- ============================================================

-- 2.1 大一大二男生引体向上（bonus_id = 1）
-- 基础19次，每多1次+1分，+10次封顶
INSERT INTO fitness_bonus_detail (bonus_rule_id, bonus_value, min_count, status)
VALUES (1, 1, 20, 1),
       (1, 2, 21, 1),
       (1, 3, 22, 1),
       (1, 4, 23, 1),
       (1, 5, 24, 1),
       (1, 6, 25, 1),
       (1, 7, 26, 1),
       (1, 8, 27, 1),
       (1, 9, 28, 1),
       (1, 10, 29, 1);

-- 2.2 大三大四男生引体向上（bonus_id = 2）
-- 基础20次，每多1次+1分，+10次封顶
INSERT INTO fitness_bonus_detail (bonus_rule_id, bonus_value, min_count, status)
VALUES (2, 1, 21, 1),
       (2, 2, 22, 1),
       (2, 3, 23, 1),
       (2, 4, 24, 1),
       (2, 5, 25, 1),
       (2, 6, 26, 1),
       (2, 7, 27, 1),
       (2, 8, 28, 1),
       (2, 9, 29, 1),
       (2, 10, 30, 1);

-- 2.3 大一大二男生1000米跑（bonus_id = 3）
-- 基础197秒，时间越短加分越多
INSERT INTO fitness_bonus_detail (bonus_rule_id, bonus_value, min_count, status)
VALUES (3, 1, 193, 1),
       (3, 2, 189, 1),
       (3, 3, 185, 1),
       (3, 4, 181, 1),
       (3, 5, 177, 1),
       (3, 6, 174, 1),
       (3, 7, 171, 1),
       (3, 8, 168, 1),
       (3, 9, 165, 1),
       (3, 10, 162, 1);

-- 2.4 大三大四男生1000米跑（bonus_id = 4）
-- 基础195秒
INSERT INTO fitness_bonus_detail (bonus_rule_id, bonus_value, min_count, status)
VALUES (4, 1, 191, 1),
       (4, 2, 187, 1),
       (4, 3, 183, 1),
       (4, 4, 179, 1),
       (4, 5, 175, 1),
       (4, 6, 172, 1),
       (4, 7, 169, 1),
       (4, 8, 166, 1),
       (4, 9, 163, 1),
       (4, 10, 160, 1);

-- 2.5 大一大二女生仰卧起坐加分（bonus_id = 5）
-- 基础56次，+2次→1分，+4次→2分，+6次→3分，之后每+1次→+1分，+13次封顶
INSERT INTO fitness_bonus_detail (bonus_rule_id, bonus_value, min_count, status)
VALUES (5, 1, 58, 1),
       (5, 2, 60, 1),
       (5, 3, 62, 1),
       (5, 4, 63, 1),
       (5, 5, 64, 1),
       (5, 6, 65, 1),
       (5, 7, 66, 1),
       (5, 8, 67, 1),
       (5, 9, 68, 1),
       (5, 10, 69, 1);

-- 2.6 大三大四女生仰卧起坐加分（bonus_id = 6）
-- 基础57次，+2次→1分，+4次→2分，+6次→3分，之后每+1次→+1分，+13次封顶
INSERT INTO fitness_bonus_detail (bonus_rule_id, bonus_value, min_count, status)
VALUES (6, 1, 59, 1),
       (6, 2, 61, 1),
       (6, 3, 63, 1),
       (6, 4, 64, 1),
       (6, 5, 65, 1),
       (6, 6, 66, 1),
       (6, 7, 67, 1),
       (6, 8, 68, 1),
       (6, 9, 69, 1),
       (6, 10, 70, 1);

-- 2.7 大一大二女生800米跑（bonus_id = 7）
-- 基础198秒，每快5秒+1分，快50秒封顶
INSERT INTO fitness_bonus_detail (bonus_rule_id, bonus_value, min_count, status)
VALUES (7, 1, 193, 1), -- 3'13"
       (7, 2, 188, 1), -- 3'08"
       (7, 3, 183, 1), -- 3'03"
       (7, 4, 178, 1), -- 2'58"
       (7, 5, 173, 1), -- 2'53"
       (7, 6, 168, 1), -- 2'48"
       (7, 7, 163, 1), -- 2'43"
       (7, 8, 158, 1), -- 2'38"
       (7, 9, 153, 1), -- 2'33"
       (7, 10, 148, 1);
-- 2'28"

-- 2.8 大三大四女生800米跑（bonus_id = 8）
-- 基础196秒（3'16"）
INSERT INTO fitness_bonus_detail (bonus_rule_id, bonus_value, min_count, status)
VALUES (8, 1, 191, 1), -- 3'11"
       (8, 2, 186, 1), -- 3'06"
       (8, 3, 181, 1), -- 3'01"
       (8, 4, 176, 1), -- 2'56"
       (8, 5, 171, 1), -- 2'51"
       (8, 6, 166, 1), -- 2'46"
       (8, 7, 161, 1), -- 2'41"
       (8, 8, 156, 1), -- 2'36"
       (8, 9, 151, 1), -- 2'31"
       (8, 10, 146, 1); -- 2'26"
