-- 创建 excel_template_config 表
DROP TABLE IF EXISTS excel_template_config;
CREATE TABLE `excel_template_config`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `biz_type`    VARCHAR(64)  NOT NULL COMMENT '业务类型：USER_IMPORT',
    `file_name`   VARCHAR(128) NOT NULL COMMENT '模板文件名，如：user_import_template.xlsx',
    `sheet_name`  VARCHAR(64) DEFAULT '模板' COMMENT 'Excel Sheet名称',
    `headers`     JSON         NOT NULL COMMENT '列头，JSON数组，如：["校区","用户账号","密码","昵称"]',
    `rules`       JSON         NOT NULL COMMENT '列规则说明，JSON数组，与headers一一对应',
    `examples`    JSON COMMENT '示例数据，二维JSON数组',
    `version`     INT         DEFAULT 0 COMMENT '乐观锁版本号',
    `status`      TINYINT(1)  DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
    `deleted`     TINYINT     DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    `create_by`   VARCHAR(50) COMMENT '创建人',
    `create_time` DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`   VARCHAR(50) COMMENT '更新人',
    `update_time` DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_biz_type` (`biz_type`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='Excel导入模板配置表';
