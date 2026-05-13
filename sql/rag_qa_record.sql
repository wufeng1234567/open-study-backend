-- RAG 问答记录表
CREATE TABLE IF NOT EXISTS `rag_qa_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `knowledge_base_id` BIGINT NOT NULL COMMENT '知识库ID',
  `question` TEXT NOT NULL COMMENT '问题',
  `answer` TEXT COMMENT '答案',
  `duration_ms` INT DEFAULT 0 COMMENT '耗时（毫秒）',
  `feedback` TINYINT DEFAULT NULL COMMENT '用户反馈：1-有用，0-无用，NULL-未反馈',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_knowledge_base_id` (`knowledge_base_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='RAG问答记录表';
