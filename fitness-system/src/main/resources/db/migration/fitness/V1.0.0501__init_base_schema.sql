-- 基础字典表建表

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

-- 3. 聚类模型表建表
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
