create table export_tasks
(
    id            bigint auto_increment
        primary key,
    task_id       varchar(64)                           not null comment '任务ID',
    user_id       bigint                                not null comment '用户ID',
    model_id      varchar(64)                           not null comment '模型ID',
    export_format varchar(20)                           not null comment '导出格式',
    quality       varchar(20) default 'medium'          null comment '质量等级',
    status        varchar(20) default 'processing'      null comment '状态',
    progress      int         default 0                 null comment '进度百分比',
    file_path     varchar(500)                          null comment '导出文件路径',
    file_size     bigint                                null comment '文件大小',
    download_url  varchar(500)                          null comment '下载链接',
    expires_at    datetime                              null comment '过期时间',
    created_at    datetime    default CURRENT_TIMESTAMP null,
    completed_at  datetime                              null comment '完成时间',
    constraint task_id
        unique (task_id)
);

create index idx_model_id
    on export_tasks (model_id);

create index idx_user_id
    on export_tasks (user_id);

create table inspiration_models
(
    id             bigint auto_increment
        primary key,
    model_id       varchar(64)                           not null comment '模型ID',
    name           varchar(200)                          not null comment '模型名称',
    description    text                                  null comment '模型描述',
    category       varchar(50)                           not null comment '分类',
    style          varchar(50)                           not null comment '风格',
    file_path      varchar(500)                          not null comment '文件路径',
    thumbnail_path varchar(500)                          null comment '缩略图路径',
    file_format    varchar(20) default 'gltf'            null comment '文件格式',
    file_size      bigint      default 0                 null comment '文件大小',
    vertices_count int         default 0                 null comment '顶点数',
    faces_count    int         default 0                 null comment '面数',
    material_type  varchar(50)                           null comment '材质类型',
    primary_color  varchar(20)                           null comment '主色调',
    favorite_count int         default 0                 null comment '收藏数',
    view_count     int         default 0                 null comment '浏览数',
    download_count int         default 0                 null comment '下载数',
    author_id      bigint                                null comment '作者ID',
    status         tinyint     default 1                 null comment '状态：1-正常，0-下架',
    created_at     datetime    default CURRENT_TIMESTAMP null,
    updated_at     datetime    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint model_id
        unique (model_id)
);

create index idx_author
    on inspiration_models (author_id);

create index idx_category
    on inspiration_models (category);

create index idx_style
    on inspiration_models (style);

create table login_logs
(
    id             bigint auto_increment
        primary key,
    user_id        bigint                              null,
    username       varchar(50)                         null comment '登录用户名',
    login_type     tinyint   default 1                 null comment '登录类型：1-密码，2-验证码，3-第三方',
    login_status   tinyint   default 1                 null comment '登录状态：0-失败，1-成功',
    ip_address     varchar(45)                         null comment 'IP地址',
    user_agent     text                                null comment '用户代理',
    device_info    varchar(200)                        null comment '设备信息',
    location       varchar(100)                        null comment '登录地点',
    failure_reason varchar(200)                        null comment '失败原因',
    created_at     timestamp default CURRENT_TIMESTAMP null
);

create index idx_created_at
    on login_logs (created_at);

create index idx_login_status
    on login_logs (login_status);

create index idx_user_id
    on login_logs (user_id);

create table model_statistics
(
    id             bigint auto_increment
        primary key,
    model_id       varchar(64)                        not null comment '模型ID',
    source_type    varchar(20)                        not null comment '来源：user_generated, inspiration',
    view_count     int      default 0                 null comment '浏览次数',
    download_count int      default 0                 null comment '下载次数',
    favorite_count int      default 0                 null comment '收藏次数',
    share_count    int      default 0                 null comment '分享次数',
    last_viewed_at datetime                           null comment '最后浏览时间',
    updated_at     datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint uk_model_source
        unique (model_id, source_type)
);

create table model_tags
(
    id           bigint auto_increment
        primary key,
    model_id     varchar(64)                           not null comment '模型ID',
    tag_name     varchar(50)                           not null comment '标签名称',
    tag_category varchar(20) default 'custom'          null comment '标签分类：style, type, custom',
    created_at   datetime    default CURRENT_TIMESTAMP null
);

create index idx_model_id
    on model_tags (model_id);

create index idx_tag_name
    on model_tags (tag_name);

create table model_user_ratings
(
    id              bigint auto_increment
        primary key,
    model_id        varchar(64)                        not null,
    user_id         bigint                             not null,
    overall_rating  decimal(3, 2)                      not null comment '总体评分(1-5)',
    quality_rating  decimal(3, 2)                      null comment '质量评分(1-5)',
    accuracy_rating decimal(3, 2)                      null comment '准确性评分(1-5)',
    visual_rating   decimal(3, 2)                      null comment '视觉效果评分(1-5)',
    feedback_text   text                               null comment '文字反馈',
    created_at      datetime default CURRENT_TIMESTAMP null,
    constraint uk_model_user
        unique (model_id, user_id)
);

create index idx_model_id
    on model_user_ratings (model_id);

create index idx_overall_rating
    on model_user_ratings (overall_rating);

create table operation_logs
(
    id              bigint auto_increment
        primary key,
    user_id         bigint                              null,
    operation_type  varchar(50)                         not null comment '操作类型',
    operation_desc  varchar(200)                        null comment '操作描述',
    resource_type   varchar(50)                         null comment '资源类型',
    resource_id     varchar(100)                        null comment '资源ID',
    request_method  varchar(10)                         null comment '请求方法',
    request_url     varchar(500)                        null comment '请求URL',
    request_params  text                                null comment '请求参数',
    response_status int                                 null comment '响应状态码',
    ip_address      varchar(45)                         null comment 'IP地址',
    user_agent      text                                null comment '用户代理',
    execution_time  int                                 null comment '执行时间(ms)',
    created_at      timestamp default CURRENT_TIMESTAMP null
);

create index idx_created_at
    on operation_logs (created_at);

create index idx_operation_type
    on operation_logs (operation_type);

create index idx_user_id
    on operation_logs (user_id);

create table t_evaluation_config
(
    id          bigint auto_increment
        primary key,
    config_name varchar(100)                          not null comment '配置名称',
    config_type varchar(50)                           not null comment '配置类型：geometry/perceptual/semantic/general',
    config_data json                                  null comment '配置数据',
    is_default  tinyint(1)  default 0                 null comment '是否为默认配置',
    is_active   tinyint(1)  default 1                 null comment '是否启用',
    version     varchar(20) default '1.0'             null comment '配置版本',
    description text                                  null comment '配置描述',
    created_at  timestamp   default CURRENT_TIMESTAMP null,
    updated_at  timestamp   default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP
)
    comment '评估配置表' collate = utf8mb4_unicode_ci;

create index idx_config_type
    on t_evaluation_config (config_type);

create index idx_is_default
    on t_evaluation_config (is_default);

create table t_export_task
(
    id                bigint auto_increment comment '导出任务ID'
        primary key,
    export_id         varchar(64)                           not null comment '导出任务唯一标识',
    model_ids         json                                  not null comment '模型ID列表',
    export_format     varchar(20)                           not null comment '导出格式',
    include_textures  tinyint     default 1                 null comment '包含纹理',
    include_materials tinyint     default 1                 null comment '包含材质',
    status            varchar(20) default 'pending'         null comment '状态: pending/processing/completed/failed',
    progress          int         default 0                 null comment '进度百分比',
    output_path       varchar(500)                          null comment '输出文件路径',
    file_size         bigint                                null comment '文件大小',
    download_url      varchar(500)                          null comment '下载链接',
    expires_at        timestamp                             null comment '过期时间',
    created_at        timestamp   default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at        timestamp   default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint export_id
        unique (export_id)
)
    comment '导出任务表';

create index idx_expires_at
    on t_export_task (expires_at);

create index idx_export_id
    on t_export_task (export_id);

create index idx_status
    on t_export_task (status);

create table t_generation_task
(
    id                bigint auto_increment comment '任务ID'
        primary key,
    task_id           varchar(64)                           not null comment '任务唯一标识',
    model_id          varchar(64)                           null comment '关联模型ID',
    input_text        text                                  not null comment '输入文本',
    generation_params json                                  null comment '生成参数',
    status            varchar(20) default 'pending'         null comment '任务状态: pending/processing/completed/failed',
    progress          int         default 0                 null comment '进度百分比',
    error_message     text                                  null comment '错误信息',
    estimated_time    int                                   null comment '预估耗时(秒)',
    actual_time       int                                   null comment '实际耗时(秒)',
    started_at        timestamp                             null comment '开始时间',
    completed_at      timestamp                             null comment '完成时间',
    created_at        timestamp   default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at        timestamp   default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint task_id
        unique (task_id)
)
    comment '模型生成任务表';

create index idx_created_at
    on t_generation_task (created_at);

create index idx_model_id
    on t_generation_task (model_id);

create index idx_status
    on t_generation_task (status);

create index idx_task_id
    on t_generation_task (task_id);

create table t_model
(
    id                    bigint auto_increment comment '模型ID'
        primary key,
    model_id              varchar(64)                           not null comment '模型唯一标识',
    name                  varchar(255)                          not null comment '模型名称',
    description           text                                  null comment '模型描述',
    original_text         text                                  null,
    category              varchar(50)                           null comment '模型分类',
    file_path             varchar(500)                          null comment '模型文件路径',
    file_size             bigint                                null comment '文件大小(字节)',
    file_format           varchar(20) default 'gltf'            null comment '文件格式',
    thumbnail_path        varchar(500)                          null comment '缩略图路径',
    vertices_count        int                                   null comment '顶点数',
    faces_count           int                                   null comment '面数',
    bounding_box_min      varchar(100)                          null comment '包围盒最小值',
    bounding_box_max      varchar(100)                          null comment '包围盒最大值',
    generation_params     json                                  null comment '生成参数',
    material_type         varchar(50)                           null comment '材质类型',
    primary_color         varchar(20)                           null comment '主色调',
    model_size            varchar(20)                           null comment '模型尺寸',
    status                tinyint     default 1                 null comment '状态: 1-正常, 0-删除',
    favorite              tinyint     default 0                 null comment '是否收藏: 1-是, 0-否',
    generation_time       int                                   null comment '生成耗时(秒)',
    created_at            timestamp   default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at            timestamp   default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    last_evaluation_id    varchar(64)                           null comment '最近评估ID',
    last_evaluation_score decimal(4, 2)                         null comment '最近评估分数',
    last_evaluation_grade varchar(10)                           null comment '最近评估等级',
    evaluation_count      int         default 0                 null comment '评估次数',
    last_evaluated_at     timestamp                             null comment '最近评估时间',
    user_id               bigint                                null comment '用户ID',
    style                 varchar(50)                           null comment '风格',
    is_public             tinyint     default 0                 null comment '是否公开：1-是，0-否',
    view_count            int         default 0                 null comment '浏览次数',
    download_count        int         default 0                 null comment '下载次数',
    task_id               varchar(64)                           null comment '生成任务ID',
    constraint model_id
        unique (model_id)
)
    comment '3D模型表';

create index idx_category
    on t_model (category);

create index idx_created_at
    on t_model (created_at);

create index idx_favorite
    on t_model (favorite);

create index idx_last_evaluation_grade
    on t_model (last_evaluation_grade);

create index idx_last_evaluation_score
    on t_model (last_evaluation_score);

create index idx_model_id
    on t_model (model_id);

create index idx_status
    on t_model (status);

create table t_model_evaluation
(
    id                bigint auto_increment
        primary key,
    evaluation_id     varchar(64)                           not null comment '评估唯一标识',
    model_id          varchar(64)                           not null comment '关联模型ID',
    user_id           bigint                                null comment '用户ID',
    evaluation_type   tinyint     default 1                 null comment '评估类型：1-完整评估，2-几何检查，3-感知评分，4-语义验证',
    evaluation_config json                                  null comment '评估配置参数',
    status            varchar(20) default 'pending'         null comment '评估状态：pending/processing/completed/failed',
    progress          tinyint     default 0                 null comment '评估进度(0-100)',
    error_message     text                                  null comment '错误信息',
    started_at        timestamp                             null comment '开始时间',
    completed_at      timestamp                             null comment '完成时间',
    execution_time    int                                   null comment '执行耗时(秒)',
    created_at        timestamp   default CURRENT_TIMESTAMP null,
    updated_at        timestamp   default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint evaluation_id
        unique (evaluation_id)
)
    comment '模型评估记录表' collate = utf8mb4_unicode_ci;

create table t_evaluation_report
(
    id                   bigint auto_increment
        primary key,
    evaluation_id        varchar(64)                             not null comment '评估ID',
    final_score          decimal(4, 2)                           null comment '最终综合评分(0-10)',
    geometry_weight      decimal(3, 2) default 0.30              null comment '几何权重',
    perceptual_weight    decimal(3, 2) default 0.40              null comment '感知权重',
    semantic_weight      decimal(3, 2) default 0.30              null comment '语义权重',
    quality_grade        varchar(10)                             null comment '质量等级：A+/A/B+/B/C+/C/D',
    pass_threshold       decimal(4, 2) default 6.00              null comment '通过阈值',
    is_passed            tinyint(1)                              null comment '是否通过评估',
    failed_items         json                                    null comment '失败项目列表',
    critical_issues      json                                    null comment '严重问题列表',
    warning_issues       json                                    null comment '警告问题列表',
    full_report          json                                    null comment '完整评估报告(JSON格式)',
    report_summary       text                                    null comment '报告摘要',
    improvement_priority json                                    null comment '改进优先级列表',
    next_steps           text                                    null comment '下一步建议',
    created_at           timestamp     default CURRENT_TIMESTAMP null,
    constraint t_evaluation_report_ibfk_1
        foreign key (evaluation_id) references t_model_evaluation (evaluation_id)
            on delete cascade
)
    comment '评估报告表' collate = utf8mb4_unicode_ci;

create index idx_evaluation_id
    on t_evaluation_report (evaluation_id);

create index idx_is_passed
    on t_evaluation_report (is_passed);

create index idx_quality_grade
    on t_evaluation_report (quality_grade);

create table t_geometry_health_result
(
    id                   bigint auto_increment
        primary key,
    evaluation_id        varchar(64)                         not null comment '评估ID',
    mesh_integrity_score decimal(5, 3)                       null comment '网格完整性评分(0-1)',
    vertex_count         int                                 null comment '顶点数量',
    face_count           int                                 null comment '面数量',
    edge_count           int                                 null comment '边数量',
    topology_score       decimal(5, 3)                       null comment '拓扑结构评分(0-1)',
    is_manifold          tinyint(1)                          null comment '是否为流形',
    boundary_edges       int                                 null comment '边界边数量',
    non_manifold_edges   int                                 null comment '非流形边数量',
    face_quality_score   decimal(5, 3)                       null comment '面片质量评分(0-1)',
    min_face_area        decimal(10, 6)                      null comment '最小面积',
    max_face_area        decimal(10, 6)                      null comment '最大面积',
    avg_face_area        decimal(10, 6)                      null comment '平均面积',
    holes_count          int       default 0                 null comment '破洞数量',
    duplicate_vertices   int       default 0                 null comment '重复顶点数量',
    degenerate_faces     int       default 0                 null comment '退化面数量',
    issues_detail        json                                null comment '问题详情列表',
    repair_suggestions   json                                null comment '修复建议',
    created_at           timestamp default CURRENT_TIMESTAMP null,
    constraint t_geometry_health_result_ibfk_1
        foreign key (evaluation_id) references t_model_evaluation (evaluation_id)
            on delete cascade
)
    comment '几何健康检查结果表' collate = utf8mb4_unicode_ci;

create index idx_evaluation_id
    on t_geometry_health_result (evaluation_id);

create index idx_created_at
    on t_model_evaluation (created_at);

create index idx_model_id
    on t_model_evaluation (model_id);

create index idx_status
    on t_model_evaluation (status);

create index idx_user_id
    on t_model_evaluation (user_id);

create table t_perceptual_quality_result
(
    id                   bigint auto_increment
        primary key,
    evaluation_id        varchar(64)                         not null comment '评估ID',
    overall_score        decimal(4, 2)                       null comment '整体感知质量评分(0-10)',
    visual_clarity       decimal(4, 2)                       null comment '视觉清晰度评分',
    structural_integrity decimal(4, 2)                       null comment '结构完整性评分',
    aesthetic_score      decimal(4, 2)                       null comment '美学评分',
    render_config        json                                null comment '渲染配置参数',
    render_time          int                                 null comment '渲染耗时(秒)',
    front_view_score     decimal(4, 2)                       null comment '正面视角评分',
    back_view_score      decimal(4, 2)                       null comment '背面视角评分',
    left_view_score      decimal(4, 2)                       null comment '左侧视角评分',
    right_view_score     decimal(4, 2)                       null comment '右侧视角评分',
    top_view_score       decimal(4, 2)                       null comment '顶部视角评分',
    bottom_view_score    decimal(4, 2)                       null comment '底部视角评分',
    render_images        json                                null comment '渲染图片路径列表',
    gms_3dqa_result      json                                null comment 'GMS-3DQA详细评估结果',
    created_at           timestamp default CURRENT_TIMESTAMP null,
    constraint t_perceptual_quality_result_ibfk_1
        foreign key (evaluation_id) references t_model_evaluation (evaluation_id)
            on delete cascade
)
    comment '感知质量评分结果表' collate = utf8mb4_unicode_ci;

create index idx_evaluation_id
    on t_perceptual_quality_result (evaluation_id);

create table t_semantic_validation_result
(
    id                      bigint auto_increment
        primary key,
    evaluation_id           varchar(64)                         not null comment '评估ID',
    original_prompt         text                                null comment '原始文本描述',
    generation_params       json                                null comment '生成参数',
    overall_match           decimal(5, 3)                       null comment '整体匹配度(0-1)',
    shape_accuracy          decimal(5, 3)                       null comment '形状准确性(0-1)',
    detail_completeness     decimal(5, 3)                       null comment '细节完整性(0-1)',
    style_consistency       decimal(5, 3)                       null comment '风格一致性(0-1)',
    functional_logic        decimal(5, 3)                       null comment '功能合理性(0-1)',
    ai_model_used           varchar(50)                         null comment '使用的AI模型',
    ai_evaluation_prompt    text                                null comment 'AI评估提示词',
    ai_response_raw         text                                null comment 'AI原始响应',
    ai_confidence           decimal(5, 3)                       null comment 'AI置信度',
    feedback_summary        text                                null comment '评估反馈摘要',
    improvement_suggestions json                                null comment '改进建议列表',
    manual_review_status    tinyint   default 0                 null comment '人工复核状态：0-待复核，1-已复核，2-有争议',
    manual_reviewer_id      bigint                              null comment '复核人员ID',
    manual_review_score     decimal(5, 3)                       null comment '人工复核评分',
    manual_review_notes     text                                null comment '人工复核备注',
    manual_reviewed_at      timestamp                           null comment '人工复核时间',
    created_at              timestamp default CURRENT_TIMESTAMP null,
    constraint t_semantic_validation_result_ibfk_1
        foreign key (evaluation_id) references t_model_evaluation (evaluation_id)
            on delete cascade
)
    comment '语义验证结果表' collate = utf8mb4_unicode_ci;

create index idx_evaluation_id
    on t_semantic_validation_result (evaluation_id);

create index idx_manual_review_status
    on t_semantic_validation_result (manual_review_status);

create table t_system_config
(
    id           bigint auto_increment comment '配置ID'
        primary key,
    config_key   varchar(100)                          not null comment '配置键',
    config_value text                                  null comment '配置值',
    config_type  varchar(20) default 'string'          null comment '配置类型',
    description  varchar(255)                          null comment '配置描述',
    created_at   timestamp   default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at   timestamp   default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint config_key
        unique (config_key)
)
    comment '系统配置表';

create index idx_config_key
    on t_system_config (config_key);

create table user_favorites
(
    id          bigint auto_increment
        primary key,
    user_id     bigint                             not null comment '用户ID',
    model_id    varchar(64)                        not null comment '模型ID',
    source_type varchar(20)                        not null comment '来源类型：generated, inspiration',
    created_at  datetime default CURRENT_TIMESTAMP null comment '收藏时间',
    constraint uk_user_model
        unique (user_id, model_id)
);

create index idx_model_id
    on user_favorites (model_id);

create index idx_user_id
    on user_favorites (user_id);

create table users
(
    id             bigint auto_increment
        primary key,
    username       varchar(50)                          not null comment '用户名',
    email          varchar(100)                         null comment '邮箱',
    phone          varchar(20)                          null comment '手机号',
    password_hash  varchar(255)                         not null comment '密码哈希',
    nickname       varchar(50)                          null comment '昵称',
    avatar_url     varchar(500)                         null comment '头像URL',
    gender         tinyint    default 0                 null comment '性别：0-未知，1-男，2-女',
    birthday       date                                 null comment '生日',
    region         varchar(100)                         null comment '地区',
    bio            text                                 null comment '个人简介',
    status         tinyint    default 1                 null comment '状态：0-禁用，1-正常，2-待验证',
    email_verified tinyint(1) default 0                 null comment '邮箱是否验证',
    phone_verified tinyint(1) default 0                 null comment '手机是否验证',
    last_login_at  timestamp                            null comment '最后登录时间',
    last_login_ip  varchar(45)                          null comment '最后登录IP',
    created_at     timestamp  default CURRENT_TIMESTAMP null,
    updated_at     timestamp  default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint email
        unique (email),
    constraint phone
        unique (phone),
    constraint username
        unique (username)
);

create table user_roles
(
    id         bigint auto_increment
        primary key,
    user_id    bigint                              not null,
    role_code  varchar(50)                         not null comment '角色代码',
    role_name  varchar(100)                        not null comment '角色名称',
    granted_at timestamp default CURRENT_TIMESTAMP null comment '授权时间',
    granted_by bigint                              null comment '授权人ID',
    expires_at timestamp                           null comment '过期时间',
    created_at timestamp default CURRENT_TIMESTAMP null,
    constraint uk_user_role
        unique (user_id, role_code),
    constraint user_roles_ibfk_1
        foreign key (user_id) references users (id)
            on delete cascade
);

create index idx_role_code
    on user_roles (role_code);

create index idx_user_id
    on user_roles (user_id);

create table user_settings
(
    id            bigint auto_increment
        primary key,
    user_id       bigint                              not null,
    setting_key   varchar(100)                        not null comment '设置键',
    setting_value text                                null comment '设置值',
    created_at    timestamp default CURRENT_TIMESTAMP null,
    updated_at    timestamp default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint uk_user_setting
        unique (user_id, setting_key),
    constraint user_settings_ibfk_1
        foreign key (user_id) references users (id)
            on delete cascade
);

create index idx_user_id
    on user_settings (user_id);

create index idx_email
    on users (email);

create index idx_phone
    on users (phone);

create index idx_status
    on users (status);

create index idx_username
    on users (username);

