-- CMS分类表
CREATE TABLE IF NOT EXISTS `cms_category` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(100) NOT NULL COMMENT '分类名称',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '分类描述',
  `parent_id` BIGINT(20) DEFAULT 0 COMMENT '父分类ID',
  `sort` INT(11) DEFAULT 0 COMMENT '排序',
  `status` TINYINT(1) DEFAULT 1 COMMENT '状态(0:禁用,1:启用)',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '删除标记(0:未删除,1:已删除)',
  `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
  `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_status` (`status`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='CMS分类表';

-- CMS类型表（分类下的类型）
CREATE TABLE IF NOT EXISTS `cms_type` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `category_id` BIGINT(20) NOT NULL COMMENT '分类ID',
  `name` VARCHAR(100) NOT NULL COMMENT '类型名称',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '类型描述',
  `sort` INT(11) DEFAULT 0 COMMENT '排序',
  `status` TINYINT(1) DEFAULT 1 COMMENT '状态(0:禁用,1:启用)',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '删除标记(0:未删除,1:已删除)',
  `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
  `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='CMS类型表（分类下的子类型）';

-- CMS文章表
CREATE TABLE IF NOT EXISTS `cms_article` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` VARCHAR(200) NOT NULL COMMENT '文章标题',
  `category_id` BIGINT(20) DEFAULT NULL COMMENT '分类ID',
  `type_id` BIGINT(20) DEFAULT NULL COMMENT '类型ID（分类下的类型）',
  `summary` VARCHAR(500) DEFAULT NULL COMMENT '文章摘要',
  `content` LONGTEXT COMMENT '文章内容',
  `cover` VARCHAR(500) DEFAULT NULL COMMENT '封面图URL',
  `status` INT(11) DEFAULT 0 COMMENT '状态(0:草稿,1:已发布)',
  `sort` INT(11) DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '删除标记(0:未删除,1:已删除)',
  `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
  `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_type_id` (`type_id`),
  KEY `idx_status` (`status`),
  KEY `idx_deleted` (`deleted`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='CMS文章表';

-- CMS文章-封面图片表（一对多）
CREATE TABLE IF NOT EXISTS `cms_article_cover` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `article_id` BIGINT(20) NOT NULL COMMENT '文章ID',
  `object_name` VARCHAR(500) NOT NULL COMMENT 'MinIO对象路径（objectName）',
  `sort` INT(11) DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_article_id` (`article_id`),
  KEY `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='CMS文章-封面图片表';
