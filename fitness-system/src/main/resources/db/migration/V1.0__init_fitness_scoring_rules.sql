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

-- 1.2 大一大二男生 50米跑（值越小越好，反向）
INSERT INTO fitness_score_rule (rule_set_id, item_id, item_code, gender, min_value, max_value, score, status)
VALUES (1, 3, '50M', 1, 0, 6.7, 100, 1),
       (1, 3, '50M', 1, 6.7, 6.8, 95, 1),
       (1, 3, '50M', 1, 6.8, 6.9, 90, 1),
       (1, 3, '50M', 1, 6.9, 7.0, 85, 1),
       (1, 3, '50M', 1, 7.0, 7.1, 80, 1),
       (1, 3, '50M', 1, 7.1, 7.3, 78, 1),
       (1, 3, '50M', 1, 7.3, 7.5, 76, 1),
       (1, 3, '50M', 1, 7.5, 7.7, 74, 1),
       (1, 3, '50M', 1, 7.7, 7.9, 72, 1),
       (1, 3, '50M', 1, 7.9, 8.1, 70, 1),
       (1, 3, '50M', 1, 8.1, 8.3, 68, 1),
       (1, 3, '50M', 1, 8.3, 8.5, 66, 1),
       (1, 3, '50M', 1, 8.5, 8.7, 64, 1),
       (1, 3, '50M', 1, 8.7, 8.9, 62, 1),
       (1, 3, '50M', 1, 8.9, 9.1, 60, 1),
       (1, 3, '50M', 1, 9.1, 9.3, 50, 1),
       (1, 3, '50M', 1, 9.3, 9.5, 40, 1),
       (1, 3, '50M', 1, 9.5, 9.7, 30, 1),
       (1, 3, '50M', 1, 9.7, 9.9, 20, 1),
       (1, 3, '50M', 1, 9.9, 10.1, 10, 1),
       (1, 3, '50M', 1, 10.1, 999999, 0, 1);

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

-- 1.6 大一大二男生 1000米跑（反向，时间越短越好）
INSERT INTO fitness_score_rule (rule_set_id, item_id, item_code, gender, min_value, max_value, score, status)
VALUES (1, 7, 'RUN_1000_800', 1, 0, 197, 100, 1),  -- 3'17" = 197秒
       (1, 7, 'RUN_1000_800', 1, 197, 202, 95, 1), -- 3'22" = 202秒
       (1, 7, 'RUN_1000_800', 1, 202, 207, 90, 1), -- 3'27" = 207秒
       (1, 7, 'RUN_1000_800', 1, 207, 214, 85, 1), -- 3'34" = 214秒
       (1, 7, 'RUN_1000_800', 1, 214, 222, 80, 1), -- 3'42" = 222秒
       (1, 7, 'RUN_1000_800', 1, 222, 227, 78, 1), -- 3'47" = 227秒
       (1, 7, 'RUN_1000_800', 1, 227, 232, 76, 1), -- 3'52" = 232秒
       (1, 7, 'RUN_1000_800', 1, 232, 237, 74, 1), -- 3'57" = 237秒
       (1, 7, 'RUN_1000_800', 1, 237, 242, 72, 1), -- 4'02" = 242秒
       (1, 7, 'RUN_1000_800', 1, 242, 247, 70, 1), -- 4'07" = 247秒
       (1, 7, 'RUN_1000_800', 1, 247, 252, 68, 1), -- 4'12" = 252秒
       (1, 7, 'RUN_1000_800', 1, 252, 257, 66, 1), -- 4'17" = 257秒
       (1, 7, 'RUN_1000_800', 1, 257, 262, 64, 1), -- 4'22" = 262秒
       (1, 7, 'RUN_1000_800', 1, 262, 267, 62, 1), -- 4'27" = 267秒
       (1, 7, 'RUN_1000_800', 1, 267, 272, 60, 1), -- 4'32" = 272秒
       (1, 7, 'RUN_1000_800', 1, 272, 292, 50, 1), -- 4'52" = 292秒
       (1, 7, 'RUN_1000_800', 1, 292, 312, 40, 1), -- 5'12" = 312秒
       (1, 7, 'RUN_1000_800', 1, 312, 332, 30, 1), -- 5'32" = 332秒
       (1, 7, 'RUN_1000_800', 1, 332, 352, 20, 1), -- 5'52" = 352秒
       (1, 7, 'RUN_1000_800', 1, 352, 372, 10, 1), -- 6'12" = 372秒
       (1, 7, 'RUN_1000_800', 1, 372, 999999, 0, 1);

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

-- 1.8 大一大二女生 50米跑（反向）
INSERT INTO fitness_score_rule (rule_set_id, item_id, item_code, gender, min_value, max_value, score, status)
VALUES (1, 3, '50M', 2, 0, 7.5, 100, 1),
       (1, 3, '50M', 2, 7.5, 7.7, 95, 1),
       (1, 3, '50M', 2, 7.7, 7.9, 90, 1),
       (1, 3, '50M', 2, 7.9, 8.1, 85, 1),
       (1, 3, '50M', 2, 8.1, 8.3, 80, 1),
       (1, 3, '50M', 2, 8.3, 8.5, 78, 1),
       (1, 3, '50M', 2, 8.5, 8.7, 76, 1),
       (1, 3, '50M', 2, 8.7, 8.9, 74, 1),
       (1, 3, '50M', 2, 8.9, 9.1, 72, 1),
       (1, 3, '50M', 2, 9.1, 9.3, 70, 1),
       (1, 3, '50M', 2, 9.3, 9.5, 68, 1),
       (1, 3, '50M', 2, 9.5, 9.7, 66, 1),
       (1, 3, '50M', 2, 9.7, 9.9, 64, 1),
       (1, 3, '50M', 2, 9.9, 10.1, 62, 1),
       (1, 3, '50M', 2, 10.1, 10.3, 60, 1),
       (1, 3, '50M', 2, 10.3, 10.5, 50, 1),
       (1, 3, '50M', 2, 10.5, 10.7, 40, 1),
       (1, 3, '50M', 2, 10.7, 10.9, 30, 1),
       (1, 3, '50M', 2, 10.9, 11.1, 20, 1),
       (1, 3, '50M', 2, 11.1, 11.3, 10, 1),
       (1, 3, '50M', 2, 11.3, 999999, 0, 1);

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

-- 1.12 大一大二女生 800米跑
INSERT INTO fitness_score_rule (rule_set_id, item_id, item_code, gender, min_value, max_value, score, status)
VALUES (1, 7, 'RUN_1000_800', 2, 0, 198, 100, 1),  -- 3'18" = 198秒
       (1, 7, 'RUN_1000_800', 2, 198, 204, 95, 1), -- 3'24" = 204秒
       (1, 7, 'RUN_1000_800', 2, 204, 210, 90, 1), -- 3'30" = 210秒
       (1, 7, 'RUN_1000_800', 2, 210, 217, 85, 1), -- 3'37" = 217秒
       (1, 7, 'RUN_1000_800', 2, 217, 224, 80, 1), -- 3'44" = 224秒
       (1, 7, 'RUN_1000_800', 2, 224, 229, 78, 1), -- 3'49" = 229秒
       (1, 7, 'RUN_1000_800', 2, 229, 234, 76, 1), -- 3'54" = 234秒
       (1, 7, 'RUN_1000_800', 2, 234, 239, 74, 1), -- 3'59" = 239秒
       (1, 7, 'RUN_1000_800', 2, 239, 244, 72, 1), -- 4'04" = 244秒
       (1, 7, 'RUN_1000_800', 2, 244, 249, 70, 1), -- 4'09" = 249秒
       (1, 7, 'RUN_1000_800', 2, 249, 254, 68, 1), -- 4'14" = 254秒
       (1, 7, 'RUN_1000_800', 2, 254, 259, 66, 1), -- 4'19" = 259秒
       (1, 7, 'RUN_1000_800', 2, 259, 264, 64, 1), -- 4'24" = 264秒
       (1, 7, 'RUN_1000_800', 2, 264, 269, 62, 1), -- 4'29" = 269秒
       (1, 7, 'RUN_1000_800', 2, 269, 274, 60, 1), -- 4'34" = 274秒
       (1, 7, 'RUN_1000_800', 2, 274, 294, 50, 1), -- 4'54" = 294秒
       (1, 7, 'RUN_1000_800', 2, 294, 314, 40, 1), -- 5'14" = 314秒
       (1, 7, 'RUN_1000_800', 2, 314, 334, 30, 1), -- 5'34" = 334秒
       (1, 7, 'RUN_1000_800', 2, 334, 354, 20, 1), -- 5'54" = 354秒
       (1, 7, 'RUN_1000_800', 2, 354, 374, 10, 1), -- 6'14" = 374秒
       (1, 7, 'RUN_1000_800', 2, 374, 999999, 0, 1);

-- ============================================================
-- 规则集 2：大三大四 (rule_set_id = 2)
-- ============================================================

-- 2.1 大三大四男生 肺活量
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

-- 2.2 大三大四男生 50米跑
INSERT INTO fitness_score_rule (rule_set_id, item_id, item_code, gender, min_value, max_value, score, status)
VALUES (2, 3, '50M', 1, 0, 6.6, 100, 1),
       (2, 3, '50M', 1, 6.6, 6.7, 95, 1),
       (2, 3, '50M', 1, 6.7, 6.8, 90, 1),
       (2, 3, '50M', 1, 6.8, 6.9, 85, 1),
       (2, 3, '50M', 1, 6.9, 7.0, 80, 1),
       (2, 3, '50M', 1, 7.0, 7.2, 78, 1),
       (2, 3, '50M', 1, 7.2, 7.4, 76, 1),
       (2, 3, '50M', 1, 7.4, 7.6, 74, 1),
       (2, 3, '50M', 1, 7.6, 7.8, 72, 1),
       (2, 3, '50M', 1, 7.8, 8.0, 70, 1),
       (2, 3, '50M', 1, 8.0, 8.2, 68, 1),
       (2, 3, '50M', 1, 8.2, 8.4, 66, 1),
       (2, 3, '50M', 1, 8.4, 8.6, 64, 1),
       (2, 3, '50M', 1, 8.6, 8.8, 62, 1),
       (2, 3, '50M', 1, 8.8, 9.0, 60, 1),
       (2, 3, '50M', 1, 9.0, 9.2, 50, 1),
       (2, 3, '50M', 1, 9.2, 9.4, 40, 1),
       (2, 3, '50M', 1, 9.4, 9.6, 30, 1),
       (2, 3, '50M', 1, 9.6, 9.8, 20, 1),
       (2, 3, '50M', 1, 9.8, 10.0, 10, 1),
       (2, 3, '50M', 1, 10.0, 999999, 0, 1);

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

-- 2.6 大三大四男生 1000米跑
INSERT INTO fitness_score_rule (rule_set_id, item_id, item_code, gender, min_value, max_value, score, status)
VALUES (2, 7, 'RUN_1000_800', 1, 0, 195, 100, 1),  -- 3'15" = 195秒
       (2, 7, 'RUN_1000_800', 1, 195, 200, 95, 1), -- 3'20" = 200秒
       (2, 7, 'RUN_1000_800', 1, 200, 205, 90, 1), -- 3'25" = 205秒
       (2, 7, 'RUN_1000_800', 1, 205, 212, 85, 1), -- 3'32" = 212秒
       (2, 7, 'RUN_1000_800', 1, 212, 220, 80, 1), -- 3'40" = 220秒
       (2, 7, 'RUN_1000_800', 1, 220, 225, 78, 1), -- 3'45" = 225秒
       (2, 7, 'RUN_1000_800', 1, 225, 230, 76, 1), -- 3'50" = 230秒
       (2, 7, 'RUN_1000_800', 1, 230, 235, 74, 1), -- 3'55" = 235秒
       (2, 7, 'RUN_1000_800', 1, 235, 240, 72, 1), -- 4'00" = 240秒
       (2, 7, 'RUN_1000_800', 1, 240, 245, 70, 1), -- 4'05" = 245秒
       (2, 7, 'RUN_1000_800', 1, 245, 250, 68, 1), -- 4'10" = 250秒
       (2, 7, 'RUN_1000_800', 1, 250, 255, 66, 1), -- 4'15" = 255秒
       (2, 7, 'RUN_1000_800', 1, 255, 260, 64, 1), -- 4'20" = 260秒
       (2, 7, 'RUN_1000_800', 1, 260, 265, 62, 1), -- 4'25" = 265秒
       (2, 7, 'RUN_1000_800', 1, 265, 270, 60, 1), -- 4'30" = 270秒
       (2, 7, 'RUN_1000_800', 1, 270, 290, 50, 1), -- 4'50" = 290秒
       (2, 7, 'RUN_1000_800', 1, 290, 310, 40, 1), -- 5'10" = 310秒
       (2, 7, 'RUN_1000_800', 1, 310, 330, 30, 1), -- 5'30" = 330秒
       (2, 7, 'RUN_1000_800', 1, 330, 350, 20, 1), -- 5'50" = 350秒
       (2, 7, 'RUN_1000_800', 1, 350, 370, 10, 1), -- 6'10" = 370秒
       (2, 7, 'RUN_1000_800', 1, 370, 999999, 0, 1);

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
VALUES (2, 3, '50M', 2, 0, 7.4, 100, 1),
       (2, 3, '50M', 2, 7.4, 7.6, 95, 1),
       (2, 3, '50M', 2, 7.6, 7.8, 90, 1),
       (2, 3, '50M', 2, 7.8, 8.0, 85, 1),
       (2, 3, '50M', 2, 8.0, 8.2, 80, 1),
       (2, 3, '50M', 2, 8.2, 8.4, 78, 1),
       (2, 3, '50M', 2, 8.4, 8.6, 76, 1),
       (2, 3, '50M', 2, 8.6, 8.8, 74, 1),
       (2, 3, '50M', 2, 8.8, 9.0, 72, 1),
       (2, 3, '50M', 2, 9.0, 9.2, 70, 1),
       (2, 3, '50M', 2, 9.2, 9.4, 68, 1),
       (2, 3, '50M', 2, 9.4, 9.6, 66, 1),
       (2, 3, '50M', 2, 9.6, 9.8, 64, 1),
       (2, 3, '50M', 2, 9.8, 10.0, 62, 1),
       (2, 3, '50M', 2, 10.0, 10.2, 60, 1),
       (2, 3, '50M', 2, 10.2, 10.4, 50, 1),
       (2, 3, '50M', 2, 10.4, 10.6, 40, 1),
       (2, 3, '50M', 2, 10.6, 10.8, 30, 1),
       (2, 3, '50M', 2, 10.8, 11.0, 20, 1),
       (2, 3, '50M', 2, 11.0, 11.2, 10, 1),
       (2, 3, '50M', 2, 11.2, 999999, 0, 1);

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
VALUES (2, 7, 'RUN_1000_800', 2, 0, 196, 100, 1),  -- 3'16" = 196秒
       (2, 7, 'RUN_1000_800', 2, 196, 202, 95, 1), -- 3'22" = 202秒
       (2, 7, 'RUN_1000_800', 2, 202, 208, 90, 1), -- 3'28" = 208秒
       (2, 7, 'RUN_1000_800', 2, 208, 215, 85, 1), -- 3'35" = 215秒
       (2, 7, 'RUN_1000_800', 2, 215, 222, 80, 1), -- 3'42" = 222秒
       (2, 7, 'RUN_1000_800', 2, 222, 227, 78, 1), -- 3'47" = 227秒
       (2, 7, 'RUN_1000_800', 2, 227, 232, 76, 1), -- 3'52" = 232秒
       (2, 7, 'RUN_1000_800', 2, 232, 237, 74, 1), -- 3'57" = 237秒
       (2, 7, 'RUN_1000_800', 2, 237, 242, 72, 1), -- 4'02" = 242秒
       (2, 7, 'RUN_1000_800', 2, 242, 247, 70, 1), -- 4'07" = 247秒
       (2, 7, 'RUN_1000_800', 2, 247, 252, 68, 1), -- 4'12" = 252秒
       (2, 7, 'RUN_1000_800', 2, 252, 257, 66, 1), -- 4'17" = 257秒
       (2, 7, 'RUN_1000_800', 2, 257, 262, 64, 1), -- 4'22" = 262秒
       (2, 7, 'RUN_1000_800', 2, 262, 267, 62, 1), -- 4'27" = 267秒
       (2, 7, 'RUN_1000_800', 2, 267, 272, 60, 1), -- 4'32" = 272秒
       (2, 7, 'RUN_1000_800', 2, 272, 292, 50, 1), -- 4'52" = 292秒
       (2, 7, 'RUN_1000_800', 2, 292, 312, 40, 1), -- 5'12" = 312秒
       (2, 7, 'RUN_1000_800', 2, 312, 332, 30, 1), -- 5'32" = 332秒
       (2, 7, 'RUN_1000_800', 2, 332, 352, 20, 1), -- 5'52" = 352秒
       (2, 7, 'RUN_1000_800', 2, 352, 372, 10, 1), -- 6'12" = 372秒
       (2, 7, 'RUN_1000_800', 2, 372, 999999, 0, 1);

-- ============================================================
-- 加分规则（fitness_bonus_rule）
-- ============================================================

-- 体测加分评分规则表
DROP TABLE IF EXISTS fitness_bonus_rule;
CREATE TABLE fitness_bonus_rule
(
    bonus_id       BIGINT PRIMARY KEY AUTO_INCREMENT,
    rule_set_id    BIGINT           NOT NULL,
    item_code      VARCHAR(50)      NOT NULL,
    gender         TINYINT UNSIGNED NOT NULL,
    status         TINYINT  DEFAULT 1,
    deleted        TINYINT  DEFAULT 0,
    create_time    DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
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
