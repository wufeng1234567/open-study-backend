-- 聊天消息表
DROP TABLE IF EXISTS `chat_message`;
CREATE TABLE `chat_message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID，主键',
  `sender_id` BIGINT NOT NULL COMMENT '发送者用户ID',
  `sender_name` VARCHAR(100) NOT NULL COMMENT '发送者用户名',
  `sender_nickname` VARCHAR(100) NOT NULL COMMENT '发送者昵称',
  `sender_avatar` VARCHAR(255) DEFAULT NULL COMMENT '发送者头像',
  `receiver_id` BIGINT NOT NULL COMMENT '接收者用户ID',
  `receiver_name` VARCHAR(100) NOT NULL COMMENT '接收者用户名',
  `receiver_nickname` VARCHAR(100) NOT NULL COMMENT '接收者昵称',
  `receiver_avatar` VARCHAR(255) DEFAULT NULL COMMENT '接收者头像',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_sender_id` (`sender_id`) COMMENT '发送者ID索引',
  KEY `idx_receiver_id` (`receiver_id`) COMMENT '接收者ID索引',
  KEY `idx_create_time` (`create_time`) COMMENT '创建时间索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';

-- 插入测试数据
INSERT INTO `chat_message` (`id`, `sender_id`, `sender_name`, `sender_nickname`, `sender_avatar`, `receiver_id`, `receiver_name`, `receiver_nickname`, `receiver_avatar`, `content`, `is_read`, `is_deleted`, `create_time`) VALUES
(1, 1, 'admin', '若依', NULL, 2, 'common', '普通用户', NULL, '你好，这是一条测试消息', 0, 0, NOW()),
(2, 2, 'common', '普通用户', NULL, 1, 'admin', '若依', NULL, '收到，你好！', 0, 0, NOW());
