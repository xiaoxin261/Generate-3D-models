-- ========================================
-- 3D模型质量评估系统数据库表结构
-- ========================================

-- 1. 模型评估结果表
CREATE TABLE `model_evaluations` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
  `model_id` VARCHAR(64) NOT NULL COMMENT '模型ID',
  `evaluation_version` VARCHAR(20) DEFAULT '1.0' COMMENT '评估算法版本',
  
  -- 评估分数
  `geometric_score` DECIMAL(5,2) DEFAULT 0 COMMENT '几何质量评分(0-100)',
  `visual_score` DECIMAL(5,2) DEFAULT 0 COMMENT '视觉效果评分(0-100)',
  `technical_score` DECIMAL(5,2) DEFAULT 0 COMMENT '技术指标评分(0-100)',
  `final_score` DECIMAL(5,2) DEFAULT 0 COMMENT '最终综合评分(0-100)',
  `grade` VARCHAR(5) COMMENT '评分等级(A+, A, B, C, D)',
  
  -- 详细评估数据
  `geometric_details` JSON COMMENT '几何质量详细数据',
  `visual_details` JSON COMMENT '视觉效果详细数据',
  `technical_details` JSON COMMENT '技术指标详细数据',
  
  -- 评估状态
  `status` VARCHAR(20) DEFAULT 'pending' COMMENT '评估状态: pending, processing, completed, failed',
  `error_message` TEXT COMMENT '错误信息',
  `evaluation_time` INT DEFAULT 0 COMMENT '评估耗时(秒)',
  
  -- 时间戳
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  UNIQUE KEY `uk_model_id` (`model_id`),
  INDEX `idx_final_score` (`final_score`),
  INDEX `idx_grade` (`grade`),
  INDEX `idx_status` (`status`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模型评估结果表';

-- 2. 用户评分表
CREATE TABLE `model_user_ratings` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
  `model_id` VARCHAR(64) NOT NULL COMMENT '模型ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  
  -- 评分数据
  `overall_rating` DECIMAL(3,2) NOT NULL COMMENT '总体评分(1-5)',
  `quality_rating` DECIMAL(3,2) COMMENT '质量评分(1-5)',
  `accuracy_rating` DECIMAL(3,2) COMMENT '准确性评分(1-5)',
  `visual_rating` DECIMAL(3,2) COMMENT '视觉效果评分(1-5)',
  
  -- 反馈内容
  `feedback_text` TEXT COMMENT '文字反馈',
  `feedback_tags` JSON COMMENT '反馈标签',
  
  -- 状态管理
  `is_anonymous` TINYINT DEFAULT 0 COMMENT '是否匿名评分',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 1-有效, 0-无效',
  
  -- 时间戳
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  UNIQUE KEY `uk_model_user` (`model_id`, `user_id`),
  INDEX `idx_model_id` (`model_id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_overall_rating` (`overall_rating`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户评分表';

-- 3. 评估任务表
CREATE TABLE `evaluation_tasks` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
  `task_id` VARCHAR(64) UNIQUE NOT NULL COMMENT '任务唯一标识',
  `model_id` VARCHAR(64) NOT NULL COMMENT '模型ID',
  
  -- 任务配置
  `evaluation_type` VARCHAR(20) DEFAULT 'full' COMMENT '评估类型: full, quick, custom',
  `priority` TINYINT DEFAULT 5 COMMENT '优先级(1-10)',
  `retry_count` TINYINT DEFAULT 0 COMMENT '重试次数',
  `max_retries` TINYINT DEFAULT 3 COMMENT '最大重试次数',
  
  -- 任务状态
  `status` VARCHAR(20) DEFAULT 'pending' COMMENT '状态: pending, processing, completed, failed, cancelled',
  `progress` TINYINT DEFAULT 0 COMMENT '进度百分比(0-100)',
  `current_step` VARCHAR(50) COMMENT '当前执行步骤',
  `error_message` TEXT COMMENT '错误信息',
  
  -- 执行信息
  `worker_id` VARCHAR(50) COMMENT '执行节点ID',
  `started_at` DATETIME COMMENT '开始时间',
  `completed_at` DATETIME COMMENT '完成时间',
  `execution_time` INT COMMENT '执行时间(秒)',
  
  -- 时间戳
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  INDEX `idx_model_id` (`model_id`),
  INDEX `idx_status` (`status`),
  INDEX `idx_priority` (`priority`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评估任务表';

-- 4. 评估指标配置表
CREATE TABLE `evaluation_metrics_config` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
  `metric_name` VARCHAR(50) NOT NULL COMMENT '指标名称',
  `metric_category` VARCHAR(20) NOT NULL COMMENT '指标分类: geometric, visual, technical',
  `weight` DECIMAL(4,3) NOT NULL COMMENT '权重(0-1)',
  `threshold_excellent` DECIMAL(5,2) COMMENT '优秀阈值',
  `threshold_good` DECIMAL(5,2) COMMENT '良好阈值',
  `threshold_fair` DECIMAL(5,2) COMMENT '一般阈值',
  `is_enabled` TINYINT DEFAULT 1 COMMENT '是否启用',
  `description` TEXT COMMENT '指标描述',
  
  -- 时间戳
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  UNIQUE KEY `uk_metric_name` (`metric_name`),
  INDEX `idx_category` (`metric_category`),
  INDEX `idx_enabled` (`is_enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评估指标配置表';

-- 5. 评估历史记录表
CREATE TABLE `evaluation_history` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
  `model_id` VARCHAR(64) NOT NULL COMMENT '模型ID',
  `evaluation_id` BIGINT NOT NULL COMMENT '评估结果ID',
  `version` INT DEFAULT 1 COMMENT '评估版本号',
  
  -- 评估快照
  `score_snapshot` JSON COMMENT '评分快照',
  `config_snapshot` JSON COMMENT '配置快照',
  
  -- 变更信息
  `change_reason` VARCHAR(100) COMMENT '变更原因',
  `changed_by` BIGINT COMMENT '变更人ID',
  
  -- 时间戳
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  
  INDEX `idx_model_id` (`model_id`),
  INDEX `idx_evaluation_id` (`evaluation_id`),
  INDEX `idx_version` (`version`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评估历史记录表';

-- ========================================
-- 初始化评估指标配置数据
-- ========================================

INSERT INTO `evaluation_metrics_config` (`metric_name`, `metric_category`, `weight`, `threshold_excellent`, `threshold_good`, `threshold_fair`, `description`) VALUES
-- 几何质量指标
('mesh_integrity', 'geometric', 0.150, 95.00, 85.00, 70.00, '网格完整性检测'),
('topology_validation', 'geometric', 0.100, 90.00, 80.00, 65.00, '拓扑结构验证'),
('geometric_accuracy', 'geometric', 0.150, 92.00, 82.00, 68.00, '几何精度评估'),

-- 视觉效果指标
('render_quality', 'visual', 0.150, 88.00, 78.00, 65.00, '渲染质量评估'),
('color_accuracy', 'visual', 0.100, 85.00, 75.00, 60.00, '色彩准确性评估'),
('style_consistency', 'visual', 0.100, 90.00, 80.00, 65.00, '风格一致性评估'),

-- 技术指标
('file_size_optimization', 'technical', 0.080, 85.00, 75.00, 60.00, '文件大小优化'),
('polygon_efficiency', 'technical', 0.080, 88.00, 78.00, 65.00, '多边形效率'),
('generation_performance', 'technical', 0.090, 90.00, 80.00, 70.00, '生成性能评估');

-- ========================================
-- 创建评估统计视图
-- ========================================

CREATE VIEW `evaluation_stats_view` AS
SELECT 
    DATE(me.created_at) as evaluation_date,
    COUNT(*) as total_evaluations,
    AVG(me.final_score) as avg_final_score,
    AVG(me.geometric_score) as avg_geometric_score,
    AVG(me.visual_score) as avg_visual_score,
    AVG(me.technical_score) as avg_technical_score,
    COUNT(CASE WHEN me.grade = 'A+' THEN 1 END) as grade_a_plus_count,
    COUNT(CASE WHEN me.grade = 'A' THEN 1 END) as grade_a_count,
    COUNT(CASE WHEN me.grade = 'B' THEN 1 END) as grade_b_count,
    COUNT(CASE WHEN me.grade = 'C' THEN 1 END) as grade_c_count,
    COUNT(CASE WHEN me.grade = 'D' THEN 1 END) as grade_d_count,
    AVG(me.evaluation_time) as avg_evaluation_time
FROM model_evaluations me
WHERE me.status = 'completed'
GROUP BY DATE(me.created_at)
ORDER BY evaluation_date DESC;

-- ========================================
-- 创建用户评分统计视图
-- ========================================

CREATE VIEW `user_rating_stats_view` AS
SELECT 
    mur.model_id,
    COUNT(*) as rating_count,
    AVG(mur.overall_rating) as avg_overall_rating,
    AVG(mur.quality_rating) as avg_quality_rating,
    AVG(mur.accuracy_rating) as avg_accuracy_rating,
    AVG(mur.visual_rating) as avg_visual_rating,
    COUNT(CASE WHEN mur.overall_rating >= 4.0 THEN 1 END) as positive_ratings,
    COUNT(CASE WHEN mur.overall_rating <= 2.0 THEN 1 END) as negative_ratings
FROM model_user_ratings mur
WHERE mur.status = 1
GROUP BY mur.model_id;

-- ========================================
-- 创建索引优化
-- ========================================

-- 复合索引优化查询性能
ALTER TABLE `model_evaluations` ADD INDEX `idx_score_grade_created` (`final_score`, `grade`, `created_at` DESC);
ALTER TABLE `model_user_ratings` ADD INDEX `idx_model_rating_created` (`model_id`, `overall_rating`, `created_at` DESC);
ALTER TABLE `evaluation_tasks` ADD INDEX `idx_status_priority_created` (`status`, `priority`, `created_at`);

-- ========================================
-- 创建触发器
-- ========================================

-- 自动更新模型评估等级
DELIMITER $$
CREATE TRIGGER `update_evaluation_grade` 
BEFORE UPDATE ON `model_evaluations`
FOR EACH ROW
BEGIN
    IF NEW.final_score >= 90 THEN
        SET NEW.grade = 'A+';
    ELSEIF NEW.final_score >= 80 THEN
        SET NEW.grade = 'A';
    ELSEIF NEW.final_score >= 70 THEN
        SET NEW.grade = 'B';
    ELSEIF NEW.final_score >= 60 THEN
        SET NEW.grade = 'C';
    ELSE
        SET NEW.grade = 'D';
    END IF;
END$$
DELIMITER ;

-- 自动记录评估历史
DELIMITER $$
CREATE TRIGGER `record_evaluation_history` 
AFTER UPDATE ON `model_evaluations`
FOR EACH ROW
BEGIN
    IF OLD.final_score != NEW.final_score OR OLD.status != NEW.status THEN
        INSERT INTO `evaluation_history` (
            `model_id`, `evaluation_id`, `version`, 
            `score_snapshot`, `change_reason`
        ) VALUES (
            NEW.model_id, NEW.id, 
            (SELECT COALESCE(MAX(version), 0) + 1 FROM evaluation_history WHERE model_id = NEW.model_id),
            JSON_OBJECT(
                'final_score', NEW.final_score,
                'geometric_score', NEW.geometric_score,
                'visual_score', NEW.visual_score,
                'technical_score', NEW.technical_score,
                'grade', NEW.grade,
                'status', NEW.status
            ),
            'Auto update'
        );
    END IF;
END$$
DELIMITER ;