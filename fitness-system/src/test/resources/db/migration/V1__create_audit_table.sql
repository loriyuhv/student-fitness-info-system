DROP TABLE IF EXISTS sys_user_login;
CREATE TABLE sys_user_login (
    `login_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '登录记录ID',
    `user_id` BIGINT COMMENT '用户ID（失败时可能为空）',
    `username` VARCHAR(50) NOT NULL COMMENT '登录账号（冗余）',
    `login_type` TINYINT NOT NULL COMMENT '登录类型：1-成功 0-失败',
    `fail_reason` VARCHAR(100) COMMENT '失败原因（仅失败时）',
    `token_id` VARCHAR(64) COMMENT 'JWT tokenId（仅成功时）',
    `device_type` VARCHAR(30) COMMENT '设备类型（PC / MOBILE / PAD）',
    `client_info` VARCHAR(255) COMMENT '客户端信息（浏览器/APP）',
    `login_ip` VARCHAR(50) COMMENT '登录IP',
    `login_location` VARCHAR(100) COMMENT '登录地',
    `login_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    `expire_time` DATETIME COMMENT 'token过期时间',
    `logout_time` DATETIME COMMENT '登出时间',
    `logout_reason` VARCHAR(50) COMMENT '登出原因（LOGOUT / KICK / EXPIRE）',
    `status` TINYINT DEFAULT 1 COMMENT '在线状态：1-在线 0-下线',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    KEY idx_username (`username`),
    KEY idx_user_id (`user_id`),
    KEY idx_login_type (`login_type`),
    KEY idx_login_ip (`login_ip`),
    KEY idx_login_time (`login_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户登录审计表';