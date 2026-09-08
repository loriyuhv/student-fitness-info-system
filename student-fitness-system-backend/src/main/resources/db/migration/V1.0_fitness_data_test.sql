-- ============================================================
-- 体测数据初始化验证 SQL
-- 执行前确保已运行 V1.0__init_fitness_scoring_rules.sql
-- ============================================================


-- ============================================================
-- 一、基础数据验证
-- ============================================================

-- 1.1 检查项目字典
SELECT item_id, item_code, item_name, item_type
FROM fitness_item
ORDER BY sort_order;

-- 1.2 检查规则集
SELECT rule_set_id, rule_set_code, rule_set_name, grade_min, grade_max
FROM fitness_score_rule_set;

-- 1.3 统计各表数据量
SELECT 'fitness_item' AS table_name, COUNT(*) AS row_count
FROM fitness_item
UNION ALL
SELECT 'fitness_score_rule_set', COUNT(*)
FROM fitness_score_rule_set
UNION ALL
SELECT 'fitness_score_rule', COUNT(*)
FROM fitness_score_rule
UNION ALL
SELECT 'fitness_weight_level_rule', COUNT(*)
FROM fitness_weight_level_rule
UNION ALL
SELECT 'fitness_score_level_rule', COUNT(*)
FROM fitness_score_level_rule
UNION ALL
SELECT 'fitness_bonus_rule', COUNT(*)
FROM fitness_bonus_rule
UNION ALL
SELECT 'fitness_bonus_detail', COUNT(*)
FROM fitness_bonus_detail;


-- ============================================================
-- 二、评分规则查询测试
-- ============================================================

-- 2.1 男生 肺活量（正向，值越大越好）
-- 大一男生 5040 → 100分（刚好达到100分档）
SELECT '2.1.1 大一男生 肺活量 5040ml' AS test_case, score AS expected_100
FROM fitness_score_rule
WHERE rule_set_id = 1
  AND item_code = 'VITAL_CAPACITY'
  AND gender = 1
  AND 5040 >= min_value
  AND 5040 < max_value;

-- 大一男生 5039 → 95分（差1ml掉档）
SELECT '2.1.2 大一男生 肺活量 5039ml' AS test_case, score AS expected_95
FROM fitness_score_rule
WHERE rule_set_id = 1
  AND item_code = 'VITAL_CAPACITY'
  AND gender = 1
  AND 5039 >= min_value
  AND 5039 < max_value;

-- 大四男生 5140 → 100分（大三大四标准刚好100分档）
SELECT '2.1.3 大四男生 肺活量 5140ml' AS test_case, score AS expected_100
FROM fitness_score_rule
WHERE rule_set_id = 2
  AND item_code = 'VITAL_CAPACITY'
  AND gender = 1
  AND 5140 >= min_value
  AND 5140 < max_value;

-- 大一女生 3400 → 100分
SELECT '2.1.4 大一女生 肺活量 3400ml' AS test_case, score AS expected_100
FROM fitness_score_rule
WHERE rule_set_id = 1
  AND item_code = 'VITAL_CAPACITY'
  AND gender = 2
  AND 3400 > min_value
  AND 3400 <= max_value;

-- 2.2 50米跑（反向，值越小越好）
-- 大一男生 6.7s → 100分
SELECT '2.2.1 大一男生 50米跑 6.7s' AS test_case, score AS expected_100
FROM fitness_score_rule
WHERE rule_set_id = 1
  AND item_code = '50M'
  AND gender = 1
  AND 6.7 > min_value
  AND 6.7 <= max_value;

-- 大一男生 6.8s → 95分
SELECT '2.2.2 大一男生 50米跑 6.8s' AS test_case, score AS expected_95
FROM fitness_score_rule
WHERE rule_set_id = 1
  AND item_code = '50M'
  AND gender = 1
  AND 6.8 > min_value
  AND 6.8 <= max_value;

-- 大一女生 7.5s → 100分
SELECT '2.2.3 大一女生 50米跑 7.5s' AS test_case, score AS expected_100
FROM fitness_score_rule
WHERE rule_set_id = 1
  AND item_code = '50M'
  AND gender = 2
  AND 7.5 > min_value
  AND 7.5 <= max_value;

-- 2.3 坐位体前屈（正向）
-- 大一男生 24.9cm → 100分
SELECT '2.3.1 大一男生 坐位体前屈 24.9cm' AS test_case, score AS expected_100
FROM fitness_score_rule
WHERE rule_set_id = 1
  AND item_code = 'SIT_AND_REACH'
  AND gender = 1
  AND 24.9 >= min_value
  AND 24.9 < max_value;

-- 2.4 立定跳远（正向）
-- 大一男生 273cm → 100分
SELECT '2.4.1 大一男生 立定跳远 273cm' AS test_case, score AS expected_100
FROM fitness_score_rule
WHERE rule_set_id = 1
  AND item_code = 'STANDING_LONG_JUMP'
  AND gender = 1
  AND 273 >= min_value
  AND 273 < max_value;

-- 2.5 引体向上/仰卧起坐（正向，有空档）
-- 大一男生 19次 → 100分
SELECT '2.5.1 大一男生 引体向上 19次' AS test_case, score AS expected_100
FROM fitness_score_rule
WHERE rule_set_id = 1
  AND item_code = 'PULL_UP'
  AND gender = 1
  AND 19 >= min_value
  AND 19 < max_value;

-- 大一男生 18次 → 95分
SELECT '2.5.2 大一男生 引体向上 18次' AS test_case, score AS expected_95
FROM fitness_score_rule
WHERE rule_set_id = 1
  AND item_code = 'PULL_UP'
  AND gender = 1
  AND 18 >= min_value
  AND 18 < max_value;

-- 大一男生 14.5次 → 76分（验证空档：15次80分，14次76分）
SELECT '2.5.3 大一男生 引体向上 14.5次' AS test_case, score AS expected_76
FROM fitness_score_rule
WHERE rule_set_id = 1
  AND item_code = 'PULL_UP'
  AND gender = 1
  AND 14.5 >= min_value
  AND 14.5 < max_value;

-- 2.6 1000米/800米跑（反向）
-- 大一男生 197s(3'17") → 100分
SELECT '2.6.1 大一男生 1000米跑 197s' AS test_case, score AS expected_100
FROM fitness_score_rule
WHERE rule_set_id = 1
  AND item_code = 'RUN_1000_800'
  AND gender = 1
  AND 197 >= min_value
  AND 197 < max_value;


-- 大一男生 198s(3'18") → 95分
SELECT '2.6.2 大一男生 1000米跑 198s' AS test_case, score AS expected_95
FROM fitness_score_rule
WHERE rule_set_id = 1
  AND item_code = 'RUN_1000_800'
  AND gender = 1
  AND 198 > min_value
  AND 198 <= max_value;

-- 大一女生 210s(3'30") → 100分
SELECT '2.6.3 大一女生 800米跑 210s' AS test_case, score AS expected_100
FROM fitness_score_rule
WHERE rule_set_id = 1
  AND item_code = 'RUN_1000_800'
  AND gender = 2
  AND 210 > min_value
  AND 210 <= max_value;


-- ============================================================
-- 三、BMI 等级查询测试
-- ============================================================

-- 3.1 男生 BMI 22.0 → 正常 100分
SELECT '3.1 男生 BMI 22.0' AS test_case, level_name, score
FROM fitness_weight_level_rule
WHERE gender = 1
  AND 22.0 >= min_bmi
  AND 22.0 < max_bmi;

-- 3.2 男生 BMI 16.5 → 低体重 80分
SELECT '3.2 男生 BMI 16.5' AS test_case, level_name, score
FROM fitness_weight_level_rule
WHERE gender = 1
  AND 16.5 >= min_bmi
  AND 16.5 < max_bmi;

-- 3.3 男生 BMI 28.0 → 肥胖 60分
SELECT '3.3 男生 BMI 28.0' AS test_case, level_name, score
FROM fitness_weight_level_rule
WHERE gender = 1
  AND 28.0 >= min_bmi
  AND 28.0 < max_bmi;

-- 3.4 女生 BMI 18.0 → 正常 100分
SELECT '3.4 女生 BMI 18.0' AS test_case, level_name, score
FROM fitness_weight_level_rule
WHERE gender = 2
  AND 18.0 >= min_bmi
  AND 18.0 < max_bmi;

-- 3.5 女生 BMI 16.0 → 低体重 80分
SELECT '3.5 女生 BMI 16.0' AS test_case, level_name, score
FROM fitness_weight_level_rule
WHERE gender = 2
  AND 16.0 >= min_bmi
  AND 16.0 < max_bmi;


-- ============================================================
-- 四、总分等级查询测试
-- ============================================================

-- 4.1 总分 95 → 优秀
SELECT '4.1 总分 95' AS test_case, level_code, level_name
FROM fitness_score_level_rule
WHERE gender = 0
  AND 95 >= min_score
  AND 95 < max_score;

-- 4.2 总分 85 → 良好
SELECT '4.2 总分 85' AS test_case, level_code, level_name
FROM fitness_score_level_rule
WHERE gender = 0
  AND 85 >= min_score
  AND 85 < max_score;

-- 4.3 总分 70 → 及格
SELECT '4.3 总分 70' AS test_case, level_code, level_name
FROM fitness_score_level_rule
WHERE gender = 0
  AND 70 >= min_score
  AND 70 < max_score;

-- 4.4 总分 50 → 不及格
SELECT '4.4 总分 50' AS test_case, level_code, level_name
FROM fitness_score_level_rule
WHERE gender = 0
  AND 50 >= min_score
  AND 50 < max_score;


-- ============================================================
-- 五、加分规则查询测试
-- ============================================================

-- 5.1 大一大二男生引体向上 22次 → 基础分100 + 加分3 = 103 → 最终103
WITH base AS (SELECT score AS base_score
              FROM fitness_score_rule
              WHERE rule_set_id = 1
                AND item_code = 'PULL_UP'
                AND gender = 1
                AND 22 >= min_value
                AND 22 < max_value -- 12次对应10分
),
     bonus AS (SELECT bonus_value as bv
               FROM fitness_bonus_detail
               WHERE bonus_rule_id = 1
                 AND min_count <= 22 -- 无加分记录
               ORDER BY bonus_value DESC
               LIMIT 1)
SELECT '大一男生引体向上12次'                                              AS test_case,
       (SELECT base_score FROM base)                                       AS base_score,
       COALESCE((SELECT bv FROM bonus), 0)                                 AS bonus,
       (SELECT base_score FROM base) + COALESCE((SELECT bv FROM bonus), 0) AS final_score;

-- 5.2 大一大二女生仰卧起坐 65次 → 基础分100 + 加分6 = 106 → 最终106
WITH base AS (SELECT score AS base_score
              FROM fitness_score_rule
              WHERE rule_set_id = 1
                AND item_code = 'PULL_UP'
                AND gender = 2
                AND 65 >= min_value
                AND 65 < max_value),
     bonus AS (SELECT bonus_value
               FROM fitness_bonus_detail
               WHERE bonus_rule_id = 5
                 AND min_count <= 65
               ORDER BY bonus_value DESC
               LIMIT 1)
SELECT '5.2 大一女生仰卧起坐65次'                                                   AS test_case,
       (SELECT base_score FROM base)                                                AS base_score,
       COALESCE((SELECT bonus_value FROM bonus), 0)                                 AS bonus,
       (SELECT base_score FROM base) + COALESCE((SELECT bonus_value FROM bonus), 0) AS final_score;

-- 5.3 大一大二女生仰卧起坐 69次 → 基础分100 + 加分10 = 110 → 最终110（验证满分）
WITH base AS (SELECT score AS base_score
              FROM fitness_score_rule
              WHERE rule_set_id = 1
                AND item_code = 'PULL_UP'
                AND gender = 2
                AND 69 >= min_value
                AND 69 < max_value),
     bonus AS (SELECT bonus_value
               FROM fitness_bonus_detail
               WHERE bonus_rule_id = 5
                 AND min_count <= 69
               ORDER BY bonus_value DESC
               LIMIT 1)
SELECT '5.3 大一女生仰卧起坐69次'                                                   AS test_case,
       (SELECT base_score FROM base)                                                AS base_score,
       COALESCE((SELECT bonus_value FROM bonus), 0)                                 AS bonus,
       (SELECT base_score FROM base) + COALESCE((SELECT bonus_value FROM bonus), 0) AS final_score;

-- 5.4 大一大二男生1000米跑 190s → 基础分100 + 加分2 = 102 → 最终102
WITH base AS (SELECT score AS base_score
              FROM fitness_score_rule
              WHERE rule_set_id = 1
                AND item_code = 'RUN_1000_800'
                AND gender = 1
                AND 190 >= min_value
                AND 190 < max_value),
     bonus AS (SELECT bonus_value
               FROM fitness_bonus_detail
               WHERE bonus_rule_id = 3
                 AND min_count >= 190
               ORDER BY bonus_value DESC
               LIMIT 1)
SELECT '5.4 大一男生1000米跑190s'                                                   AS test_case,
       (SELECT base_score FROM base)                                                AS base_score,
       COALESCE((SELECT bonus_value FROM bonus), 0)                                 AS bonus,
       (SELECT base_score FROM base) + COALESCE((SELECT bonus_value FROM bonus), 0) AS final_score;

-- 5.5 大一大二女生800米跑 200s → 基础分100 + 加分2 = 102 → 最终102
WITH base AS (SELECT score AS base_score
              FROM fitness_score_rule
              WHERE rule_set_id = 1
                AND item_code = 'RUN_1000_800'
                AND gender = 2
                AND 200 >= min_value
                AND 200 < max_value),
     bonus AS (SELECT bonus_value
               FROM fitness_bonus_detail
               WHERE bonus_rule_id = 7
                 AND min_count >= 200
               ORDER BY bonus_value DESC
               LIMIT 1)
SELECT '5.5 大一女生800米跑200s'                                                   AS test_case,
       (SELECT base_score FROM base)                                               AS base_score,
       COALESCE((SELECT bonus_value FROM bonus), 0)                                             AS bonus,
       (SELECT base_score FROM base) + COALESCE((SELECT bonus_value FROM bonus), 0) AS final_score;

-- 5.6 大三大四男生引体向上 22次 → 大三大四标准，基础100分 + 加分2 = 102
WITH base AS (SELECT score AS base_score
              FROM fitness_score_rule
              WHERE rule_set_id = 2
                AND item_code = 'PULL_UP'
                AND gender = 1
                AND 22 >= min_value
                AND 22 < max_value),
     bonus AS (SELECT bonus_value
               FROM fitness_bonus_detail
               WHERE bonus_rule_id = 2
                 AND min_count <= 22
               ORDER BY bonus_value DESC
               LIMIT 1)
SELECT '5.6 大四男生引体向上22次'                                                  AS test_case,
       (SELECT base_score FROM base)                                               AS base_score,
       COALESCE((SELECT bonus_value FROM bonus), 0)                                             AS bonus,
       (SELECT base_score FROM base) + COALESCE((SELECT bonus_value FROM bonus), 0) AS final_score;


-- ============================================================
-- 六、边界值测试
-- ============================================================

-- 6.1 大一男生肺活量 5139 → 大一大二标准 100分
SELECT '6.1 大一男生肺活量5140ml' AS test_case,
       (SELECT score
        FROM fitness_score_rule
        WHERE rule_set_id = 1
          AND item_code = 'VITAL_CAPACITY'
          AND gender = 1
          AND 5139 >= min_value
          AND 5139 < max_value)   AS freshman_score,
       (SELECT score
        FROM fitness_score_rule
        WHERE rule_set_id = 2
          AND item_code = 'VITAL_CAPACITY'
          AND gender = 1
          AND 5139 >= min_value
          AND 5139 < max_value)   AS senior_score;
-- 预期：大一100分，大三大四95分（验证年级差异化）

-- 6.2 大一男生50米跑 10.1s → 刚好10分档的边界
SELECT '6.2 大一男生50米跑10.1s' AS test_case, score
FROM fitness_score_rule
WHERE rule_set_id = 1
  AND item_code = '50M'
  AND gender = 1
  AND 10.1 > min_value
  AND 10.1 <= max_value;
-- 预期：10分（反向项目边界）

-- 6.3 大一男生50米跑 10.2s → 下一档0分
SELECT '6.3 大一男生50米跑10.2s' AS test_case, score
FROM fitness_score_rule
WHERE rule_set_id = 1
  AND item_code = '50M'
  AND gender = 1
  AND 10.2 > min_value
  AND 10.2 <= max_value;
-- 预期：0分


-- ============================================================
-- 七、总结
-- ============================================================

SELECT '✅ 测试执行完成，请逐条检查预期结果是否正确。' AS summary;