-- 基础字典表初始化数据

-- 1. 项目编码初始化数据，后续评分规则引用这些编码
INSERT INTO fitness_item (item_code, item_name, min_valid_value, max_valid_value, item_type, unit, value_type,
                          is_score_item, sort_order)
VALUES ('BMI', '体重指数', 10, 50, 1, 'kg/m²', 1, 1, 1),
       ('VITAL_CAPACITY', '肺活量', 500, 9999, 2, 'ml', 1, 1, 2),
       ('50M', '50米跑', 4, 15, 2, 's', 1, 1, 3),
       ('SIT_AND_REACH', '坐位体前屈', -20, 40, 2, 'cm', 1, 1, 4),
       ('STANDING_LONG_JUMP', '立定跳远', 50, 350, 2, 'cm', 1, 1, 5),
       ('PULL_UP', '引体向上（男）/ 仰卧起坐（女）', 0, 100, 2, '次', 1, 1, 6),
       ('RUN_1000_800', '1000米跑（男）/ 800米跑（女）', 120, 600, 2, 's', 1, 1, 7);

-- 2. 体测评分规则集表初始化数据：年级分组：大一大二（合并）、大三大四（合并）
INSERT INTO fitness_score_rule_set (rule_set_id, rule_set_code, rule_set_name, grade_min, grade_max, status)
VALUES (1, 'FRESHMAN_SOPHOMORE', '大一大二标准', 1, 2, 1),
       (2, 'JUNIOR_SENIOR', '大三大四标准', 3, 4, 1);


-- 3. 聚类模型表初始化数据
-- 男生 低年级 模型
INSERT INTO fitness_cluster_model
    (model_id, gender, grade_group, cluster_count, feature_desc)
VALUES (1, 1, 1, 4, '男生低年级体测聚类模型');

-- 女生 低年级 模型
INSERT INTO fitness_cluster_model
    (model_id, gender, grade_group, cluster_count, feature_desc)
VALUES (2, 2, 1, 4, '女生低年级体测聚类模型');
