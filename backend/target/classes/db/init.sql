-- 创建数据库
CREATE DATABASE IF NOT EXISTS children_park DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE children_park;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    `openid` VARCHAR(100) NOT NULL COMMENT '微信openid',
    `union_id` VARCHAR(100) DEFAULT NULL COMMENT '微信unionId',
    `nick_name` VARCHAR(100) DEFAULT NULL COMMENT '昵称',
    `avatar_url` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    `gender` TINYINT DEFAULT 0 COMMENT '性别 0未知 1男 2女',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `balance` DECIMAL(10,2) DEFAULT 0.00 COMMENT '余额（币）',
    `role` VARCHAR(20) DEFAULT 'user' COMMENT '角色 user普通用户 admin管理员',
    `status` TINYINT DEFAULT 1 COMMENT '状态 0禁用 1正常',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '是否删除 0否 1是',
    UNIQUE KEY `uk_openid` (`openid`),
    KEY `idx_role` (`role`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 项目表
CREATE TABLE IF NOT EXISTS `project` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '项目ID',
    `name` VARCHAR(100) NOT NULL COMMENT '项目名称',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '项目描述',
    `price` INT NOT NULL DEFAULT 0 COMMENT '扣币价格（币/次）',
    `max_capacity` INT DEFAULT NULL COMMENT '最大容量',
    `status` TINYINT DEFAULT 1 COMMENT '状态 0暂停 1正常',
    `icon` VARCHAR(50) DEFAULT NULL COMMENT '图标',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '是否删除 0否 1是',
    KEY `idx_status` (`status`),
    KEY `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='娱乐项目表';

-- 充值订单表
CREATE TABLE IF NOT EXISTS `recharge_order` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
    `order_no` VARCHAR(50) NOT NULL COMMENT '订单号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '充值金额（元）',
    `coins_received` INT NOT NULL DEFAULT 0 COMMENT '到账币数',
    `gift_coins` INT NOT NULL DEFAULT 0 COMMENT '赠送币数',
    `payment_method` VARCHAR(20) DEFAULT 'wechat' COMMENT '支付方式',
    `prepay_id` VARCHAR(100) DEFAULT NULL COMMENT '预支付ID',
    `transaction_id` VARCHAR(100) DEFAULT NULL COMMENT '微信支付订单号',
    `status` TINYINT DEFAULT 0 COMMENT '状态 0待支付 1已支付 2已取消',
    `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '是否删除 0否 1是',
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='充值订单表';

-- 消费记录表
CREATE TABLE IF NOT EXISTS `consume_record` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    `record_no` VARCHAR(50) NOT NULL COMMENT '记录编号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `project_id` BIGINT DEFAULT NULL COMMENT '项目ID',
    `project_name` VARCHAR(100) DEFAULT NULL COMMENT '项目名称',
    `quantity` INT DEFAULT 1 COMMENT '次数',
    `amount` INT NOT NULL DEFAULT 0 COMMENT '扣币数量',
    `operator_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
    `operator_name` VARCHAR(100) DEFAULT NULL COMMENT '操作人名称',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '是否删除 0否 1是',
    UNIQUE KEY `uk_record_no` (`record_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消费记录表';

-- 二维码表
CREATE TABLE IF NOT EXISTS `qrcode` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `qrcode_data` VARCHAR(100) NOT NULL COMMENT '二维码数据',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `expire_time` DATETIME NOT NULL COMMENT '过期时间',
    `status` TINYINT DEFAULT 1 COMMENT '状态 0无效 1有效',
    `used` TINYINT DEFAULT 0 COMMENT '是否已使用 0否 1是',
    `use_time` DATETIME DEFAULT NULL COMMENT '使用时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_qrcode_data` (`qrcode_data`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_expire_time` (`expire_time`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态二维码表';

-- 系统配置表
CREATE TABLE IF NOT EXISTS `system_config` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
    `config_value` VARCHAR(500) DEFAULT NULL COMMENT '配置值',
    `config_name` VARCHAR(100) DEFAULT NULL COMMENT '配置名称',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '描述',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '是否删除 0否 1是',
    UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 初始化管理员账号（密码需要在微信端登录后手动修改角色）
-- INSERT INTO `user` (`openid`, `nick_name`, `balance`, `role`, `status`) 
-- VALUES ('mock_admin_openid', '系统管理员', 9999, 'admin', 1);

-- 初始化项目数据
INSERT INTO `project` (`name`, `description`, `price`, `max_capacity`, `status`, `icon`, `sort`) VALUES
('旋转木马', '经典旋转木马，适合所有年龄段', 5, 30, 1, '🎠', 1),
('碰碰车', '刺激有趣的碰碰车', 3, 20, 1, '🚗', 2),
('过山车', '惊险刺激的过山车', 8, 15, 1, '🎢', 3),
('海盗船', '摇摆的海盗船', 6, 25, 1, '⛵', 4),
('摩天轮', '浪漫的摩天轮', 10, 40, 1, '🎡', 5),
('小火车', '亲子小火车', 2, 50, 1, '🚂', 6),
('沙池', '儿童沙池游乐区', 4, 30, 1, '🏖️', 7),
('滑梯', '大型滑梯组合', 3, 20, 1, '🛝', 8);

-- 初始化系统配置
INSERT INTO `system_config` (`config_key`, `config_value`, `config_name`, `description`) VALUES
('qrcode_validity_minutes', '5', '二维码有效期', '用户动态二维码的有效时间（分钟）');
