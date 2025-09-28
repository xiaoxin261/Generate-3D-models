# Generate3D API 设计文档 - 灵感库和我的模型

## 概述

本文档详细描述了Generate3D平台中**灵感库**和**我的模型**两个核心功能模块的API接口设计。基于现有的代码结构分析，设计了完整的RESTful API接口，支持模型浏览、收藏、管理等功能。

## 基础信息

- **API版本**: v1.0
- **基础URL**: `http://localhost:8090/api`
- **认证方式**: JWT Bearer Token（部分接口可选）
- **数据格式**: JSON
- **字符编码**: UTF-8

## 通用响应格式

```json
{
  "success": true,
  "message": "操作成功",
  "data": {},
  "timestamp": "2024-01-01T12:00:00Z"
}
```

## 错误响应格式

```json
{
  "success": false,
  "message": "错误描述",
  "code": "ERROR_CODE",
  "timestamp": "2024-01-01T12:00:00Z"
}
```

---

## 1. 灵感库 API

灵感库提供公共的3D模型展示和浏览功能，用户可以查看、搜索和收藏优质的3D模型作品。

### 1.1 获取灵感库模型列表

**接口描述**: 分页获取灵感库中的公共模型列表

```http
GET /api/v1/inspiration/models
```

**请求参数**:
```json
{
  "page": 1,           // 页码，默认1
  "size": 20,          // 每页数量，默认20，最大100
  "category": "all",   // 分类筛选：all, furniture, decoration, architecture, character, vehicle, other
  "style": "all",      // 风格筛选：all, realistic, cartoon, minimalist, futuristic
  "sort": "latest",    // 排序方式：latest(最新), popular(热门), favorite(收藏最多)
  "keyword": ""        // 搜索关键词（可选）
}
```

**响应示例**:
```json
{
  "success": true,
  "message": "获取成功",
  "data": {
    "total": 150,
    "page": 1,
    "size": 20,
    "totalPages": 8,
    "models": [
      {
        "modelId": "inspiration_001",
        "name": "现代简约沙发",
        "description": "一款现代简约风格的三人沙发，适合客厅装饰",
        "category": "furniture",
        "style": "minimalist",
        "thumbnailPath": "/thumbnails/inspiration_001.png",
        "previewUrl": "/preview/inspiration_001",
        "fileFormat": "gltf",
        "fileSize": 2048000,
        "verticesCount": 15420,
        "facesCount": 8960,
        "materialType": "fabric",
        "primaryColor": "#8B4513",
        "tags": ["沙发", "家具", "现代", "简约"],
        "favoriteCount": 128,
        "viewCount": 1520,
        "downloadCount": 89,
        "isFavorited": false,
        "author": {
          "username": "designer_001",
          "nickname": "设计师小王",
          "avatar": "/avatars/designer_001.png"
        },
        "createdAt": "2024-01-15T10:30:00Z",
        "updatedAt": "2024-01-15T10:30:00Z"
      }
    ]
  }
}
```

### 1.2 获取灵感库模型详情

**接口描述**: 获取指定模型的详细信息

```http
GET /api/v1/inspiration/models/{modelId}
```

**路径参数**:
- `modelId`: 模型ID

**响应示例**:
```json
{
  "success": true,
  "message": "获取成功",
  "data": {
    "modelId": "inspiration_001",
    "name": "现代简约沙发",
    "description": "一款现代简约风格的三人沙发，采用高品质面料制作，线条简洁流畅，适合现代家居环境",
    "originalText": "设计一个现代简约风格的三人沙发",
    "category": "furniture",
    "style": "minimalist",
    "filePath": "/models/inspiration_001.gltf",
    "thumbnailPath": "/thumbnails/inspiration_001.png",
    "previewUrl": "/preview/inspiration_001",
    "fileFormat": "gltf",
    "fileSize": 2048000,
    "verticesCount": 15420,
    "facesCount": 8960,
    "boundingBox": {
      "min": [-1.2, 0, -0.8],
      "max": [1.2, 0.9, 0.8]
    },
    "materialType": "fabric",
    "primaryColor": "#8B4513",
    "dimensions": {
      "length": 2.4,
      "width": 1.6,
      "height": 0.9
    },
    "tags": ["沙发", "家具", "现代", "简约"],
    "favoriteCount": 128,
    "viewCount": 1520,
    "downloadCount": 89,
    "isFavorited": false,
    "viewerConfig": {
      "camera": {
        "position": [2, 2, 2],
        "target": [0, 0.5, 0]
      },
      "lighting": {
        "ambient": 0.4,
        "directional": 0.8
      }
    },
    "author": {
      "username": "designer_001",
      "nickname": "设计师小王",
      "avatar": "/avatars/designer_001.png",
      "bio": "专注现代家具设计5年"
    },
    "relatedModels": [
      {
        "modelId": "inspiration_002",
        "name": "配套茶几",
        "thumbnailPath": "/thumbnails/inspiration_002.png"
      }
    ],
    "createdAt": "2024-01-15T10:30:00Z",
    "updatedAt": "2024-01-15T10:30:00Z"
  }
}
```

### 1.3 收藏/取消收藏灵感库模型

**接口描述**: 收藏或取消收藏指定的灵感库模型

```http
POST /api/v1/inspiration/models/{modelId}/favorite
```

**请求头**:
```
Authorization: Bearer {jwt_token}
```

**请求体**:
```json
{
  "favorite": true  // true: 收藏, false: 取消收藏
}
```

**响应示例**:
```json
{
  "success": true,
  "message": "收藏成功",
  "data": {
    "modelId": "inspiration_001",
    "isFavorited": true,
    "favoriteCount": 129
  }
}
```

### 1.4 获取灵感库分类列表

**接口描述**: 获取所有可用的模型分类

```http
GET /api/v1/inspiration/categories
```

**响应示例**:
```json
{
  "success": true,
  "message": "获取成功",
  "data": [
    {
      "key": "furniture",
      "name": "家具",
      "icon": "/icons/furniture.svg",
      "count": 45,
      "description": "各类家具模型"
    },
    {
      "key": "decoration",
      "name": "装饰品",
      "icon": "/icons/decoration.svg",
      "count": 32,
      "description": "装饰用品和摆件"
    },
    {
      "key": "architecture",
      "name": "建筑",
      "icon": "/icons/architecture.svg",
      "count": 28,
      "description": "建筑和结构模型"
    }
  ]
}
```

### 1.5 获取热门标签

**接口描述**: 获取灵感库中的热门标签

```http
GET /api/v1/inspiration/tags/popular
```

**请求参数**:
```json
{
  "limit": 20  // 返回数量限制，默认20
}
```

**响应示例**:
```json
{
  "success": true,
  "message": "获取成功",
  "data": [
    {
      "tag": "现代",
      "count": 89,
      "category": "style"
    },
    {
      "tag": "家具",
      "count": 76,
      "category": "type"
    },
    {
      "tag": "简约",
      "count": 65,
      "category": "style"
    }
  ]
}
```

---

## 2. 我的模型 API

我的模型功能提供用户个人模型的管理，包括查看生成的模型、收藏的模型、模型编辑和删除等功能。

### 2.1 获取我的模型列表

**接口描述**: 分页获取当前用户的模型列表

```http
GET /api/v1/user/models
```

**请求头**:
```
Authorization: Bearer {jwt_token}
```

**请求参数**:
```json
{
  "page": 1,           // 页码，默认1
  "size": 20,          // 每页数量，默认20
  "type": "all",       // 类型筛选：all(全部), generated(生成的), favorited(收藏的)
  "category": "all",   // 分类筛选
  "status": "all",     // 状态筛选：all, active(正常), deleted(已删除)
  "sort": "latest",    // 排序：latest(最新), oldest(最早), name(名称)
  "keyword": ""        // 搜索关键词
}
```

**响应示例**:
```json
{
  "success": true,
  "message": "获取成功",
  "data": {
    "total": 25,
    "page": 1,
    "size": 20,
    "totalPages": 2,
    "models": [
      {
        "modelId": "user_model_001",
        "name": "我的小房子",
        "description": "一个温馨的小房子模型",
        "originalText": "设计一个温馨的小房子",
        "category": "architecture",
        "style": "cartoon",
        "thumbnailPath": "/thumbnails/user_model_001.png",
        "filePath": "/models/user_model_001.gltf",
        "fileFormat": "gltf",
        "fileSize": 1536000,
        "verticesCount": 8420,
        "facesCount": 4960,
        "materialType": "wood",
        "primaryColor": "#DEB887",
        "status": "completed",
        "isGenerated": true,
        "isFavorited": false,
        "isPublic": false,
        "generationParams": {
          "length": 3.0,
          "width": 2.5,
          "height": 2.8,
          "style": "cartoon"
        },
        "taskId": "task_12345",
        "createdAt": "2024-01-20T14:30:00Z",
        "updatedAt": "2024-01-20T14:35:00Z"
      }
    ]
  }
}
```

### 2.2 获取我的模型详情

**接口描述**: 获取指定用户模型的详细信息

```http
GET /api/v1/user/models/{modelId}
```

**请求头**:
```
Authorization: Bearer {jwt_token}
```

**响应示例**:
```json
{
  "success": true,
  "message": "获取成功",
  "data": {
    "modelId": "user_model_001",
    "name": "我的小房子",
    "description": "一个温馨的小房子模型，包含门窗和烟囱等细节",
    "originalText": "设计一个温馨的小房子，要有门窗和烟囱",
    "category": "architecture",
    "style": "cartoon",
    "filePath": "/models/user_model_001.gltf",
    "thumbnailPath": "/thumbnails/user_model_001.png",
    "fileFormat": "gltf",
    "fileSize": 1536000,
    "verticesCount": 8420,
    "facesCount": 4960,
    "boundingBox": {
      "min": [-1.5, 0, -1.25],
      "max": [1.5, 2.8, 1.25]
    },
    "materialType": "wood",
    "primaryColor": "#DEB887",
    "dimensions": {
      "length": 3.0,
      "width": 2.5,
      "height": 2.8
    },
    "status": "completed",
    "isGenerated": true,
    "isFavorited": false,
    "isPublic": false,
    "downloadCount": 0,
    "viewCount": 12,
    "generationParams": {
      "length": 3.0,
      "width": 2.5,
      "height": 2.8,
      "style": "cartoon",
      "color": "#DEB887",
      "materialType": "wood"
    },
    "taskId": "task_12345",
    "generationTime": 45,
    "viewerConfig": {
      "camera": {
        "position": [3, 3, 3],
        "target": [0, 1.4, 0]
      }
    },
    "exportFormats": ["gltf", "obj", "stl"],
    "createdAt": "2024-01-20T14:30:00Z",
    "updatedAt": "2024-01-20T14:35:00Z"
  }
}
```

### 2.3 更新模型信息

**接口描述**: 更新用户模型的基本信息

```http
PUT /api/v1/user/models/{modelId}
```

**请求头**:
```
Authorization: Bearer {jwt_token}
```

**请求体**:
```json
{
  "name": "我的温馨小屋",
  "description": "更新后的描述信息",
  "category": "architecture",
  "isPublic": false,
  "tags": ["房子", "建筑", "卡通"]
}
```

**响应示例**:
```json
{
  "success": true,
  "message": "更新成功",
  "data": {
    "modelId": "user_model_001",
    "name": "我的温馨小屋",
    "description": "更新后的描述信息",
    "updatedAt": "2024-01-20T15:00:00Z"
  }
}
```

### 2.4 删除模型

**接口描述**: 删除指定的用户模型（软删除）

```http
DELETE /api/v1/user/models/{modelId}
```

**请求头**:
```
Authorization: Bearer {jwt_token}
```

**响应示例**:
```json
{
  "success": true,
  "message": "删除成功",
  "data": {
    "modelId": "user_model_001",
    "deletedAt": "2024-01-20T15:30:00Z"
  }
}
```

### 2.5 批量操作模型

**接口描述**: 批量操作多个模型（删除、设置公开状态等）

```http
POST /api/v1/user/models/batch
```

**请求头**:
```
Authorization: Bearer {jwt_token}
```

**请求体**:
```json
{
  "action": "delete",  // 操作类型：delete, setPublic, setPrivate, favorite, unfavorite
  "modelIds": ["user_model_001", "user_model_002", "user_model_003"]
}
```

**响应示例**:
```json
{
  "success": true,
  "message": "批量操作成功",
  "data": {
    "action": "delete",
    "successCount": 3,
    "failedCount": 0,
    "results": [
      {
        "modelId": "user_model_001",
        "success": true
      },
      {
        "modelId": "user_model_002",
        "success": true
      },
      {
        "modelId": "user_model_003",
        "success": true
      }
    ]
  }
}
```

### 2.6 获取收藏的模型

**接口描述**: 获取用户收藏的所有模型（包括自己生成的和灵感库的）

```http
GET /api/v1/user/favorites
```

**请求头**:
```
Authorization: Bearer {jwt_token}
```

**请求参数**:
```json
{
  "page": 1,
  "size": 20,
  "source": "all",     // 来源筛选：all(全部), generated(自己生成), inspiration(灵感库)
  "category": "all",
  "sort": "latest"
}
```

**响应示例**:
```json
{
  "success": true,
  "message": "获取成功",
  "data": {
    "total": 15,
    "page": 1,
    "size": 20,
    "totalPages": 1,
    "models": [
      {
        "modelId": "inspiration_001",
        "name": "现代简约沙发",
        "thumbnailPath": "/thumbnails/inspiration_001.png",
        "source": "inspiration",
        "favoritedAt": "2024-01-18T10:00:00Z"
      },
      {
        "modelId": "user_model_001",
        "name": "我的小房子",
        "thumbnailPath": "/thumbnails/user_model_001.png",
        "source": "generated",
        "favoritedAt": "2024-01-20T14:35:00Z"
      }
    ]
  }
}
```

### 2.7 导出模型

**接口描述**: 导出指定格式的模型文件

```http
POST /api/v1/user/models/{modelId}/export
```

**请求头**:
```
Authorization: Bearer {jwt_token}
```

**请求体**:
```json
{
  "format": "obj",     // 导出格式：gltf, obj, stl, fbx
  "quality": "high",   // 质量：low, medium, high
  "includeTextures": true,
  "includeAnimations": false
}
```

**响应示例**:
```json
{
  "success": true,
  "message": "导出任务已创建",
  "data": {
    "taskId": "export_task_001",
    "modelId": "user_model_001",
    "format": "obj",
    "status": "processing",
    "estimatedTime": 30,
    "createdAt": "2024-01-20T16:00:00Z"
  }
}
```

### 2.8 获取导出任务状态

**接口描述**: 查询模型导出任务的进度和状态

```http
GET /api/v1/user/export-tasks/{taskId}
```

**请求头**:
```
Authorization: Bearer {jwt_token}
```

**响应示例**:
```json
{
  "success": true,
  "message": "获取成功",
  "data": {
    "taskId": "export_task_001",
    "modelId": "user_model_001",
    "format": "obj",
    "status": "completed",
    "progress": 100,
    "downloadUrl": "/downloads/export_task_001.zip",
    "fileSize": 2048000,
    "expiresAt": "2024-01-27T16:00:00Z",
    "createdAt": "2024-01-20T16:00:00Z",
    "completedAt": "2024-01-20T16:00:30Z"
  }
}
```

### 2.9 获取模型统计信息

**接口描述**: 获取用户模型的统计数据

```http
GET /api/v1/user/models/statistics
```

**请求头**:
```
Authorization: Bearer {jwt_token}
```

**响应示例**:
```json
{
  "success": true,
  "message": "获取成功",
  "data": {
    "totalModels": 25,
    "generatedModels": 20,
    "favoritedModels": 5,
    "publicModels": 3,
    "totalViews": 156,
    "totalDownloads": 23,
    "storageUsed": 52428800,  // 字节
    "storageLimit": 1073741824,  // 字节
    "categoryStats": [
      {
        "category": "furniture",
        "count": 8,
        "percentage": 32
      },
      {
        "category": "decoration",
        "count": 6,
        "percentage": 24
      }
    ],
    "monthlyStats": [
      {
        "month": "2024-01",
        "generated": 12,
        "favorited": 3
      }
    ]
  }
}
```

---

## 3. 数据模型定义

### 3.1 Model 实体

```json
{
  "modelId": "string",           // 模型唯一标识
  "name": "string",              // 模型名称
  "description": "string",       // 模型描述
  "originalText": "string",      // 原始输入文本
  "category": "string",          // 分类
  "style": "string",             // 风格
  "filePath": "string",          // 文件路径
  "thumbnailPath": "string",     // 缩略图路径
  "fileFormat": "string",        // 文件格式
  "fileSize": "number",          // 文件大小（字节）
  "verticesCount": "number",     // 顶点数
  "facesCount": "number",        // 面数
  "boundingBox": {               // 包围盒
    "min": [x, y, z],
    "max": [x, y, z]
  },
  "materialType": "string",      // 材质类型
  "primaryColor": "string",      // 主要颜色
  "dimensions": {                // 尺寸
    "length": "number",
    "width": "number",
    "height": "number"
  },
  "status": "string",            // 状态
  "tags": ["string"],           // 标签
  "createdAt": "string",        // 创建时间
  "updatedAt": "string"         // 更新时间
}
```

### 3.2 分页响应格式

```json
{
  "total": "number",        // 总数量
  "page": "number",         // 当前页码
  "size": "number",         // 每页数量
  "totalPages": "number",   // 总页数
  "items": []               // 数据列表
}
```

---

## 4. 错误码定义

| 错误码 | HTTP状态码 | 描述 |
|--------|------------|------|
| SUCCESS | 200 | 操作成功 |
| INVALID_PARAMS | 400 | 参数错误 |
| UNAUTHORIZED | 401 | 未授权 |
| FORBIDDEN | 403 | 禁止访问 |
| NOT_FOUND | 404 | 资源不存在 |
| MODEL_NOT_FOUND | 404 | 模型不存在 |
| ALREADY_FAVORITED | 409 | 已收藏 |
| STORAGE_LIMIT_EXCEEDED | 413 | 存储空间不足 |
| INTERNAL_ERROR | 500 | 服务器内部错误 |

---

## 5. 接口权限说明

### 5.1 公开接口（无需认证）
- 获取灵感库模型列表
- 获取灵感库模型详情
- 获取分类列表
- 获取热门标签

### 5.2 需要认证的接口
- 收藏/取消收藏操作
- 我的模型相关的所有接口
- 模型导出功能

### 5.3 权限级别
- **普通用户**: 基础的查看、收藏、生成功能
- **VIP用户**: 更多存储空间、高级导出格式
- **管理员**: 管理灵感库内容、用户模型审核

---

## 6. 性能和限制

### 6.1 请求限制
- 普通用户：100 请求/分钟
- VIP用户：500 请求/分钟
- 管理员：1000 请求/分钟

### 6.2 存储限制
- 普通用户：1GB 存储空间
- VIP用户：10GB 存储空间
- 单个模型文件：最大100MB

### 6.3 导出限制
- 普通用户：每天5次导出
- VIP用户：每天50次导出
- 导出文件保存7天

---

## 7. 版本更新记录

| 版本 | 日期 | 更新内容 |
|------|------|----------|
| v1.0 | 2024-01-20 | 初始版本，包含灵感库和我的模型基础功能 |

---

## 8. 数据库设计方案

为了完全支持灵感库和我的模型功能，需要对现有数据库进行扩展和优化。

### 8.1 现有数据库表分析

当前系统已有以下核心表：

| 表名 | 用途 | 状态 |
|------|------|------|
| `users` | 用户基础信息 | ✅ 完整 |
| `t_model` | 3D模型数据 | ⚠️ 需要扩展 |
| `user_settings` | 用户设置 | ✅ 完整 |
| `user_roles` | 用户角色 | ✅ 完整 |
| `login_logs` | 登录日志 | ✅ 完整 |
| `operation_logs` | 操作日志 | ✅ 完整 |
| `t_system_config` | 系统配置 | ✅ 完整 |
| `export_tasks` | 导出任务 | ⚠️ 需要完善 |

### 8.2 新增数据库表

#### 8.2.1 用户收藏表 (`user_favorites`)

**用途**: 管理用户收藏的模型（包括自己生成的和灵感库的）

```sql
CREATE TABLE `user_favorites` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `model_id` VARCHAR(64) NOT NULL COMMENT '模型ID',
  `source_type` VARCHAR(20) NOT NULL COMMENT '来源类型：generated(用户生成), inspiration(灵感库)',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  UNIQUE KEY `uk_user_model` (`user_id`, `model_id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_model_id` (`model_id`),
  INDEX `idx_source_type` (`source_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收藏表';
```

#### 8.2.2 模型标签表 (`model_tags`)

**用途**: 管理模型的标签系统，支持分类和搜索

```sql
CREATE TABLE `model_tags` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
  `model_id` VARCHAR(64) NOT NULL COMMENT '模型ID',
  `tag_name` VARCHAR(50) NOT NULL COMMENT '标签名称',
  `tag_category` VARCHAR(20) DEFAULT 'custom' COMMENT '标签分类：style(风格), type(类型), custom(自定义)',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  
  INDEX `idx_model_id` (`model_id`),
  INDEX `idx_tag_name` (`tag_name`),
  INDEX `idx_tag_category` (`tag_category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模型标签表';
```

#### 8.2.3 灵感库模型表 (`inspiration_models`)

**用途**: 存储公共的灵感库模型，与用户生成的模型分离

```sql
CREATE TABLE `inspiration_models` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
  `model_id` VARCHAR(64) UNIQUE NOT NULL COMMENT '模型唯一标识',
  `name` VARCHAR(200) NOT NULL COMMENT '模型名称',
  `description` TEXT COMMENT '模型描述',
  `category` VARCHAR(50) NOT NULL COMMENT '分类：furniture, decoration, architecture, character, vehicle, other',
  `style` VARCHAR(50) NOT NULL COMMENT '风格：realistic, cartoon, minimalist, futuristic',
  
  -- 文件信息
  `file_path` VARCHAR(500) NOT NULL COMMENT '模型文件路径',
  `thumbnail_path` VARCHAR(500) COMMENT '缩略图路径',
  `file_format` VARCHAR(20) DEFAULT 'gltf' COMMENT '文件格式',
  `file_size` BIGINT DEFAULT 0 COMMENT '文件大小(字节)',
  
  -- 模型属性
  `vertices_count` INT DEFAULT 0 COMMENT '顶点数',
  `faces_count` INT DEFAULT 0 COMMENT '面数',
  `bounding_box_min` VARCHAR(100) COMMENT '包围盒最小值(JSON格式)',
  `bounding_box_max` VARCHAR(100) COMMENT '包围盒最大值(JSON格式)',
  `material_type` VARCHAR(50) COMMENT '材质类型',
  `primary_color` VARCHAR(20) COMMENT '主色调',
  `dimensions` VARCHAR(100) COMMENT '尺寸信息(JSON格式)',
  
  -- 统计信息
  `favorite_count` INT DEFAULT 0 COMMENT '收藏数',
  `view_count` INT DEFAULT 0 COMMENT '浏览数',
  `download_count` INT DEFAULT 0 COMMENT '下载数',
  
  -- 作者信息
  `author_id` BIGINT COMMENT '作者用户ID',
  `author_name` VARCHAR(100) COMMENT '作者名称',
  
  -- 状态管理
  `status` TINYINT DEFAULT 1 COMMENT '状态：1-正常，0-下架',
  `is_featured` TINYINT DEFAULT 0 COMMENT '是否精选：1-是，0-否',
  
  -- 时间戳
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  INDEX `idx_category` (`category`),
  INDEX `idx_style` (`style`),
  INDEX `idx_author` (`author_id`),
  INDEX `idx_status` (`status`),
  INDEX `idx_featured` (`is_featured`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='灵感库模型表';
```

#### 8.2.4 模型统计表 (`model_statistics`)

**用途**: 统一管理所有模型的统计数据

```sql
CREATE TABLE `model_statistics` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
  `model_id` VARCHAR(64) NOT NULL COMMENT '模型ID',
  `source_type` VARCHAR(20) NOT NULL COMMENT '来源：user_generated(用户生成), inspiration(灵感库)',
  
  -- 统计数据
  `view_count` INT DEFAULT 0 COMMENT '浏览次数',
  `download_count` INT DEFAULT 0 COMMENT '下载次数',
  `favorite_count` INT DEFAULT 0 COMMENT '收藏次数',
  `share_count` INT DEFAULT 0 COMMENT '分享次数',
  
  -- 时间记录
  `last_viewed_at` DATETIME COMMENT '最后浏览时间',
  `last_downloaded_at` DATETIME COMMENT '最后下载时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  UNIQUE KEY `uk_model_source` (`model_id`, `source_type`),
  INDEX `idx_model_id` (`model_id`),
  INDEX `idx_source_type` (`source_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模型统计表';
```

### 8.3 现有表结构修改

#### 8.3.1 用户模型表 (`t_model`) 扩展

需要为现有的 `t_model` 表添加以下字段：

```sql
-- 添加用户关联
ALTER TABLE `t_model` ADD COLUMN `user_id` BIGINT COMMENT '用户ID' AFTER `id`;

-- 添加风格字段
ALTER TABLE `t_model` ADD COLUMN `style` VARCHAR(50) COMMENT '风格：realistic, cartoon, minimalist, futuristic' AFTER `category`;

-- 添加公开设置
ALTER TABLE `t_model` ADD COLUMN `is_public` TINYINT DEFAULT 0 COMMENT '是否公开：1-是，0-否' AFTER `status`;

-- 添加统计字段
ALTER TABLE `t_model` ADD COLUMN `view_count` INT DEFAULT 0 COMMENT '浏览次数' AFTER `is_public`;
ALTER TABLE `t_model` ADD COLUMN `download_count` INT DEFAULT 0 COMMENT '下载次数' AFTER `view_count`;

-- 添加任务关联
ALTER TABLE `t_model` ADD COLUMN `task_id` VARCHAR(64) COMMENT '生成任务ID' AFTER `generationParams`;

-- 添加尺寸信息
ALTER TABLE `t_model` ADD COLUMN `dimensions` VARCHAR(100) COMMENT '尺寸信息(JSON格式)' AFTER `modelSize`;

-- 添加索引
ALTER TABLE `t_model` ADD INDEX `idx_user_id` (`user_id`);
ALTER TABLE `t_model` ADD INDEX `idx_style` (`style`);
ALTER TABLE `t_model` ADD INDEX `idx_is_public` (`is_public`);
ALTER TABLE `t_model` ADD INDEX `idx_task_id` (`task_id`);
```

#### 8.3.2 导出任务表 (`export_tasks`) 完善

```sql
CREATE TABLE IF NOT EXISTS `export_tasks` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
  `task_id` VARCHAR(64) UNIQUE NOT NULL COMMENT '任务唯一标识',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `model_id` VARCHAR(64) NOT NULL COMMENT '模型ID',
  
  -- 导出配置
  `export_format` VARCHAR(20) NOT NULL COMMENT '导出格式：gltf, obj, stl, fbx',
  `quality` VARCHAR(20) DEFAULT 'medium' COMMENT '质量等级：low, medium, high',
  `include_textures` TINYINT DEFAULT 1 COMMENT '是否包含纹理：1-是，0-否',
  `include_animations` TINYINT DEFAULT 0 COMMENT '是否包含动画：1-是，0-否',
  
  -- 任务状态
  `status` VARCHAR(20) DEFAULT 'processing' COMMENT '状态：processing, completed, failed, expired',
  `progress` INT DEFAULT 0 COMMENT '进度百分比(0-100)',
  `error_message` TEXT COMMENT '错误信息',
  
  -- 文件信息
  `file_path` VARCHAR(500) COMMENT '导出文件路径',
  `file_size` BIGINT COMMENT '文件大小(字节)',
  `download_url` VARCHAR(500) COMMENT '下载链接',
  `download_count` INT DEFAULT 0 COMMENT '下载次数',
  
  -- 时间管理
  `expires_at` DATETIME COMMENT '过期时间',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `completed_at` DATETIME COMMENT '完成时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_model_id` (`model_id`),
  INDEX `idx_status` (`status`),
  INDEX `idx_expires_at` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模型导出任务表';
```

### 8.4 数据迁移方案

#### 8.4.1 收藏数据迁移

将现有 `t_model` 表中的 `favorite` 字段数据迁移到新的 `user_favorites` 表：

```sql
-- 迁移收藏数据（假设所有收藏的模型都是用户生成的）
INSERT INTO `user_favorites` (`user_id`, `model_id`, `source_type`, `created_at`)
SELECT 
    `user_id`, 
    `model_id`, 
    'generated' as source_type,
    `created_at`
FROM `t_model` 
WHERE `favorite` = 1 AND `user_id` IS NOT NULL;

-- 迁移完成后，可以考虑删除原有的 favorite 字段
-- ALTER TABLE `t_model` DROP COLUMN `favorite`;
```

#### 8.4.2 统计数据初始化

为现有模型初始化统计数据：

```sql
-- 为用户生成的模型初始化统计数据
INSERT INTO `model_statistics` (`model_id`, `source_type`, `view_count`, `download_count`, `favorite_count`)
SELECT 
    `model_id`,
    'user_generated' as source_type,
    0 as view_count,
    0 as download_count,
    0 as favorite_count
FROM `t_model`
WHERE `status` = 1;
```

### 8.5 索引优化建议

为了提高查询性能，建议添加以下复合索引：

```sql
-- 用户收藏表复合索引
ALTER TABLE `user_favorites` ADD INDEX `idx_user_source_created` (`user_id`, `source_type`, `created_at` DESC);

-- 模型标签表复合索引
ALTER TABLE `model_tags` ADD INDEX `idx_tag_category` (`tag_name`, `tag_category`);

-- 灵感库模型表复合索引
ALTER TABLE `inspiration_models` ADD INDEX `idx_category_status_created` (`category`, `status`, `created_at` DESC);
ALTER TABLE `inspiration_models` ADD INDEX `idx_style_status_created` (`style`, `status`, `created_at` DESC);

-- 用户模型表复合索引
ALTER TABLE `t_model` ADD INDEX `idx_user_status_created` (`user_id`, `status`, `created_at` DESC);
ALTER TABLE `t_model` ADD INDEX `idx_category_status_created` (`category`, `status`, `created_at` DESC);
```

### 8.6 数据库配置建议

#### 8.6.1 字符集和排序规则

```sql
-- 确保所有表使用 utf8mb4 字符集
ALTER DATABASE `3dmodel` CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
```

#### 8.6.2 存储引擎优化

- 所有表使用 `InnoDB` 存储引擎
- 启用行级锁定和事务支持
- 配置适当的缓冲池大小

### 8.7 实施计划

#### 阶段一：核心表创建
1. 创建 `user_favorites` 表
2. 创建 `model_tags` 表
3. 扩展 `t_model` 表字段

#### 阶段二：灵感库支持
1. 创建 `inspiration_models` 表
2. 创建 `model_statistics` 表
3. 数据迁移和初始化

#### 阶段三：高级功能
1. 完善 `export_tasks` 表
2. 添加性能优化索引
3. 数据清理和维护

### 8.8 注意事项

1. **数据备份**: 在执行任何数据库变更前，务必进行完整备份
2. **渐进式迁移**: 建议分阶段实施，避免一次性大规模变更
3. **性能监控**: 密切监控新索引对写入性能的影响
4. **数据一致性**: 确保迁移过程中的数据完整性
5. **回滚方案**: 准备每个阶段的回滚脚本

---

## 9. 联系方式

- **技术支持**: support@generate3d.com
- **API文档**: http://localhost:8090/swagger-ui.html
- **开发者社区**: https://community.generate3d.com

---

*本文档基于Generate3D平台当前的代码结构和业务需求设计，如有疑问请联系开发团队。*