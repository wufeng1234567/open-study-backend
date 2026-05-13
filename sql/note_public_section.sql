-- 创建公开笔记分区表
CREATE TABLE IF NOT EXISTS `note_public_section` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分区ID，主键',
  `name` varchar(100) NOT NULL COMMENT '分区名称',
  `sort_order` int DEFAULT '0' COMMENT '排序序号',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公开笔记分区表';

-- 为 note 表添加 public_section_id 字段
ALTER TABLE `note` ADD COLUMN `public_section_id` bigint NULL COMMENT '公开分区ID' AFTER `sort_order`;

-- 添加索引
ALTER TABLE `note` ADD INDEX `idx_public_section_id` (`public_section_id`);
