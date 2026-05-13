-- 留言板表
DROP TABLE IF EXISTS `leave_message`;
CREATE TABLE `leave_message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '留言ID',
  `user_id` BIGINT NOT NULL COMMENT '留言用户ID',
  `user_name` VARCHAR(100) NOT NULL COMMENT '留言用户名',
  `content` TEXT NOT NULL COMMENT '留言内容',
  `ip` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-已删除，1-正常',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='留言板表';

-- 插入测试数据
INSERT INTO `leave_message` (`id`, `user_id`, `user_name`, `content`, `ip`, `status`, `create_time`) VALUES
(1, 1, 'admin', '这是管理员的测试留言', '127.0.0.1', 1, NOW()),
(2, 2, 'common', '这是普通用户的测试留言', '127.0.0.1', 1, NOW());
