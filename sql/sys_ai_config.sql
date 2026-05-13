-- AI模型配置表
-- 用于存储用户自定义的AI模型配置（API Key等）

CREATE TABLE `sys_ai_config` (
  `config_id` bigint NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID（null表示系统级配置）',
  `provider` varchar(50) NOT NULL COMMENT '服务商：zhipuai/deepseek/siliconflow/openai',
  `provider_name` varchar(100) DEFAULT NULL COMMENT '服务商显示名称',
  `model` varchar(100) NOT NULL COMMENT '模型名称',
  `api_key` varchar(500) DEFAULT NULL COMMENT 'API Key',
  `base_url` varchar(500) DEFAULT NULL COMMENT 'API地址（用户自定义，优先使用）',
  `is_default` tinyint(1) DEFAULT 0 COMMENT '是否为用户默认模型（0否 1是）',
  `is_enabled` tinyint(1) DEFAULT 1 COMMENT '是否启用（0否 1是）',
  `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `thinking_mode` varchar(20) DEFAULT 'auto' COMMENT '思考模式: auto(自动), enabled(启用), disabled(禁用)',
  `reasoning_effort` varchar(20) DEFAULT 'high' COMMENT '推理强度: low, medium, high, max',
  `context_length` varchar(20) DEFAULT '32k' COMMENT '上下文长度: 32k, 128k, 1m',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  PRIMARY KEY (`config_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_provider` (`provider`),
  KEY `idx_user_default` (`user_id`,`is_default`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='AI模型配置表';

-- 插入系统默认配置（glm-4-plus）
INSERT INTO `sys_ai_config` (`config_id`, `user_id`, `provider`, `provider_name`, `model`, `api_key`, `base_url`, `is_default`, `is_enabled`, `status`, `remark`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES
(1, NULL, 'zhipuai', '智谱AI', 'glm-4-plus', NULL, NULL, 1, 1, '0', '系统默认配置', NOW(), NOW(), '', '');
