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

-- 插入用户导入的模板数据
INSERT INTO `excel_template_config` (`biz_type`,
                                     `file_name`,
                                     `sheet_name`,
                                     `headers`,
                                     `rules`,
                                     `examples`,
                                     `create_by`,
                                     `update_by`)
VALUES ('USER_IMPORT',
        'user_import_template.xlsx',
        '用户导入模板',
        '[
          "校区",
          "用户账号",
          "密码",
          "昵称",
          "手机号码",
          "邮箱",
          "用户类型"
        ]',
        '[
          "必填，数字",
          "必填，长度≤50",
          "必填，≥6位",
          "选填，≤20位",
          "选填，11位数字",
          "选填，支持@163.com/@126.com/@qq.com/@gmail.com",
          "必填，0-管理员/1-教师/2-学生"
        ]',
        '[
          [
            "1001",
            "20214202",
            "123456",
            "张三",
            "13800138000",
            "zhangsan@163.com",
            "2"
          ],
          [
            "1002",
            "20214176",
            "123456",
            "李四",
            "13900139000",
            "lisi@qq.com",
            "1"
          ]
        ]',
        '1',
        '1');