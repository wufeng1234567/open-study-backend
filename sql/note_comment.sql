-- 笔记评论表
CREATE TABLE `note_comment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评论ID，主键',
  `note_id` BIGINT NOT NULL COMMENT '所属笔记ID',
  `parent_id` BIGINT DEFAULT NULL COMMENT '父评论ID（NULL表示一级评论）',
  `reply_to_user_id` BIGINT DEFAULT NULL COMMENT '回复目标用户ID',
  `reply_to_user_name` VARCHAR(100) DEFAULT NULL COMMENT '回复目标用户名',
  `user_id` BIGINT NOT NULL COMMENT '评论用户ID',
  `user_name` VARCHAR(100) NOT NULL COMMENT '评论用户名',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '用户头像',
  `content` TEXT NOT NULL COMMENT '评论内容',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-已删除，1-正常',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_note_id` (`note_id`) COMMENT '笔记ID索引',
  KEY `idx_parent_id` (`parent_id`) COMMENT '父评论ID索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='笔记评论表';
