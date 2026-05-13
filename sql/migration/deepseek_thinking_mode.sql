-- DeepSeek 思考模式支持 - 数据库字段迁移
-- 为 sys_ai_config 表添加思考模式相关字段

ALTER TABLE `sys_ai_config`
ADD COLUMN `thinking_mode` varchar(20) DEFAULT 'auto' COMMENT '思考模式: auto(自动), enabled(启用), disabled(禁用)' AFTER `remark`,
ADD COLUMN `reasoning_effort` varchar(20) DEFAULT 'high' COMMENT '推理强度: low, medium, high, max' AFTER `thinking_mode`,
ADD COLUMN `context_length` varchar(20) DEFAULT '32k' COMMENT '上下文长度: 32k, 128k, 1m' AFTER `reasoning_effort`;