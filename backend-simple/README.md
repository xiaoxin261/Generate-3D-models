# Generate 3D Backend - 3D模型生成系统后端服务

<div align="center">

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1.5-brightgreen)
![MySQL](https://img.shields.io/badge/MySQL-8.0+-blue)
![Redis](https://img.shields.io/badge/Redis-6.0+-red)
![License](https://img.shields.io/badge/License-MIT-yellow)

**基于AI的智能3D模型生成系统后端服务**

[功能特性](#功能特性) • [技术架构](#技术架构) • [快速开始](#快速开始) • [API文档](#api文档) • [部署指南](#部署指南)

</div>

---

## 📋 项目简介

Generate 3D Backend 是一个基于Spring Boot 3.x开发的现代化3D模型生成系统后端服务。系统集成了腾讯混元AI、阿里云OSS等服务，提供从图片到3D模型的智能生成能力，支持模型质量评估、用户管理、文件存储等完整功能。

### 🎯 核心价值

- **智能生成**：基于AI技术，从2D图片智能生成高质量3D模型
- **质量评估**：完整的模型质量评估体系，确保生成效果
- **性能优化**：智能缓存、批量处理、频率限制等多重优化策略
- **企业级**：完善的用户认证、权限管理、监控告警体系

## 🚀 功能特性

### 核心功能模块

#### 🤖 AI模型生成
- **图片转3D模型**：支持多种图片格式，智能识别并生成对应3D模型
- **文本描述生成**：基于自然语言描述生成3D模型
- **批量处理**：支持批量模型生成，提高处理效率
- **异步处理**：大任务异步执行，实时进度反馈

#### 📊 质量评估系统
- **自动化评估**：几何质量、纹理质量、拓扑结构自动检测
- **用户评分**：用户反馈评分系统，持续优化模型质量
- **评估报告**：详细的质量评估报告和改进建议
- **统计分析**：评估数据统计分析，支持决策优化

#### 🔧 性能优化
- **智能缓存**：多级缓存策略，减少65-80%重复API调用
- **频率限制**：基于滑动窗口的智能频率控制
- **批量优化**：相似请求合并处理，提升40%处理效率
- **监控告警**：实时性能监控和异常告警

#### 👥 用户管理
- **JWT认证**：安全的用户认证和授权机制
- **用户画像**：完整的用户信息管理和偏好分析
- **权限控制**：细粒度的权限管理体系
- **使用统计**：用户使用情况统计和分析

#### 📁 文件管理
- **多存储支持**：本地存储、阿里云OSS等多种存储方案
- **文件处理**：图片压缩、格式转换、缩略图生成
- **安全上传**：文件类型检查、大小限制、病毒扫描
- **CDN加速**：静态资源CDN分发，提升访问速度

## 🏗️ 技术架构

### 技术栈

| 技术分类 | 技术选型 | 版本 | 说明 |
|---------|---------|------|------|
| **核心框架** | Spring Boot | 3.1.5 | 主框架 |
| **数据库** | MySQL | 8.0+ | 主数据库 |
| **缓存** | Redis | 6.0+ | 缓存和会话存储 |
| **ORM框架** | MyBatis-Plus | 3.5.7 | 数据访问层 |
| **安全框架** | Spring Security | 6.x | 认证授权 |
| **文档工具** | SpringDoc OpenAPI | 2.2.0 | API文档 |
| **工具库** | Hutool | 5.8.22 | Java工具库 |
| **JSON处理** | FastJSON2 | 2.0.43 | JSON序列化 |
| **JWT** | JJWT | 0.12.3 | JWT令牌处理 |
| **云服务** | 阿里云OSS | 3.17.0 | 对象存储 |
| **AI服务** | 腾讯混元 | 3.1.1011 | AI模型生成 |

### 系统架构图

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   前端应用层    │    │   网关层        │    │   负载均衡      │
│  React/Vue.js   │◄──►│  Spring Gateway │◄──►│   Nginx/LB     │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                ↓
┌─────────────────────────────────────────────────────────────────┐
│                        应用服务层                                │
├─────────────────┬─────────────────┬─────────────────┬───────────┤
│   用户管理服务   │   模型生成服务   │   文件管理服务   │  评估服务  │
│  UserService    │ GenerationService│  FileService   │EvalService│
└─────────────────┴─────────────────┴─────────────────┴───────────┘
                                ↓
┌─────────────────────────────────────────────────────────────────┐
│                        数据访问层                                │
├─────────────────┬─────────────────┬─────────────────┬───────────┤
│   用户数据      │   模型数据      │   文件数据      │  评估数据  │
│  UserMapper     │  ModelMapper    │  FileMapper    │EvalMapper │
└─────────────────┴─────────────────┴─────────────────┴───────────┘
                                ↓
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   MySQL数据库   │    │   Redis缓存     │    │   外部服务      │
│   主数据存储    │    │   会话/缓存     │    │  OSS/AI服务     │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### 核心模块设计

#### 1. 模型生成模块
```java
ModelGenerationService
├── 图片预处理 (ImagePreprocessor)
├── AI服务调用 (HunyuanService)
├── 模型后处理 (ModelPostprocessor)
├── 质量检测 (QualityChecker)
└── 结果存储 (ResultStorage)
```

#### 2. 优化模块
```java
AIOptimizationService
├── 智能缓存 (IntelligentCache)
├── 频率限制 (RateLimiter)
├── 批量处理 (BatchProcessor)
├── 异步调度 (AsyncScheduler)
└── 监控统计 (MonitoringService)
```

#### 3. 评估模块
```java
EvaluationService
├── 自动评估 (AutoEvaluator)
├── 用户评分 (UserRating)
├── 统计分析 (StatisticsAnalyzer)
├── 报告生成 (ReportGenerator)
└── 持续优化 (ContinuousImprovement)
```

## 🛠️ 快速开始

### 环境要求

- **Java**: 17+
- **Maven**: 3.8+
- **MySQL**: 8.0+
- **Redis**: 6.0+
- **Node.js**: 16+ (前端开发)

### 安装步骤

#### 1. 克隆项目
```bash
git clone https://github.com/your-org/generate-3d-backend.git
cd generate-3d-backend
```

#### 2. 数据库初始化
```sql
-- 创建数据库
CREATE DATABASE generate3d CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 导入初始化脚本
mysql -u root -p generate3d < docs/sql/init.sql
```

#### 3. 配置文件
复制并修改配置文件：
```bash
cp src/main/resources/application-dev.yml.example src/main/resources/application-dev.yml
```

关键配置项：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/generate3d
    username: your_username
    password: your_password
  
  data:
    redis:
      host: localhost
      port: 6379
      password: your_redis_password

app:
  ai:
    hunyuan:
      secret-id: your_hunyuan_secret_id
      secret-key: your_hunyuan_secret_key
  
  storage:
    oss:
      access-key-id: your_oss_access_key
      access-key-secret: your_oss_secret_key
      bucket-name: your_bucket_name
```

#### 4. 编译运行
```bash
# 编译项目
mvn clean compile

# 运行测试
mvn test

# 启动应用
mvn spring-boot:run

# 或者打包运行
mvn clean package
java -jar target/generate-3d-backend-1.0.0.jar
```

#### 5. 验证安装
访问以下地址验证安装：
- **健康检查**: http://localhost:8081/api/actuator/health
- **API文档**: http://localhost:8081/api/swagger-ui.html
- **应用首页**: http://localhost:8081/api/

## 📚 API文档

### 接口概览

| 模块 | 接口路径 | 描述 |
|------|---------|------|
| **认证授权** | `/auth/*` | 用户登录、注册、令牌刷新 |
| **用户管理** | `/user/*` | 用户信息、偏好设置、统计数据 |
| **模型生成** | `/api/v1/models/*` | 3D模型生成、查询、管理 |
| **任务管理** | `/api/v1/jobs/*` | 任务进度查询、状态管理 |
| **文件管理** | `/file/*` | 文件上传、下载、删除 |
| **质量评估** | `/api/v1/evaluation/*` | 模型评估、用户评分 |
| **AI优化** | `/api/ai-optimization/*` | 缓存管理、性能监控 |

### 核心接口示例

#### 1. 用户认证
```http
POST /auth/login
Content-Type: application/json

{
  "username": "user@example.com",
  "password": "password123"
}
```

#### 2. 图片生成3D模型
```http
POST /api/v1/models/generate-from-image
Authorization: Bearer {jwt_token}
Content-Type: multipart/form-data

{
  "image": "image_file",
  "style": "realistic",
  "quality": "high",
  "format": "obj"
}
```

#### 3. 查询任务进度
```http
GET /api/v1/jobs/{taskId}
Authorization: Bearer {jwt_token}
```

#### 4. 获取模型评估
```http
GET /api/v1/evaluation/model/{modelId}
Authorization: Bearer {jwt_token}
```

### 完整API文档
启动应用后访问 [Swagger UI](http://localhost:8081/api/swagger-ui.html) 查看完整的API文档。

## 🔧 配置说明

### 应用配置

#### 基础配置 (application.yml)
```yaml
server:
  port: 8081
  servlet:
    context-path: /api

spring:
  application:
    name: generate-3d-backend
  profiles:
    active: dev
```

#### 数据库配置
```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/generate3d
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:password}
    hikari:
      minimum-idle: 5
      maximum-pool-size: 15
      connection-timeout: 30000
```

#### Redis配置
```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      database: 0
      timeout: 10s
```

#### AI服务配置
```yaml
app:
  ai:
    hunyuan:
      secret-id: ${HUNYUAN_SECRET_ID}
      secret-key: ${HUNYUAN_SECRET_KEY}
      region: ap-beijing
      model: hunyuan-lite
      max-tokens: 1000
```

#### 文件存储配置
```yaml
app:
  storage:
    type: oss  # local, oss
    oss:
      endpoint: ${OSS_ENDPOINT}
      access-key-id: ${OSS_ACCESS_KEY_ID}
      access-key-secret: ${OSS_ACCESS_KEY_SECRET}
      bucket-name: ${OSS_BUCKET_NAME}
```

### 环境变量

创建 `.env` 文件：
```bash
# 数据库配置
DB_USERNAME=root
DB_PASSWORD=your_password

# Redis配置
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=your_redis_password

# AI服务配置
HUNYUAN_SECRET_ID=your_secret_id
HUNYUAN_SECRET_KEY=your_secret_key

# OSS配置
OSS_ENDPOINT=https://oss-cn-hangzhou.aliyuncs.com
OSS_ACCESS_KEY_ID=your_access_key_id
OSS_ACCESS_KEY_SECRET=your_access_key_secret
OSS_BUCKET_NAME=your_bucket_name

# JWT配置
JWT_SECRET=your_jwt_secret_key
JWT_EXPIRATION=86400000
```

## 🚀 部署指南

### Docker部署

#### 1. 构建镜像
```dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/generate-3d-backend-1.0.0.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
```

```bash
# 构建镜像
docker build -t generate-3d-backend:1.0.0 .

# 运行容器
docker run -d \
  --name generate-3d-backend \
  -p 8081:8081 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_HOST=mysql \
  -e REDIS_HOST=redis \
  generate-3d-backend:1.0.0
```

#### 2. Docker Compose部署
```yaml
version: '3.8'

services:
  app:
    build: .
    ports:
      - "8081:8081"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DB_HOST=mysql
      - REDIS_HOST=redis
    depends_on:
      - mysql
      - redis
    networks:
      - generate3d-network

  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: rootpassword
      MYSQL_DATABASE: generate3d
    volumes:
      - mysql-data:/var/lib/mysql
    networks:
      - generate3d-network

  redis:
    image: redis:6.2-alpine
    networks:
      - generate3d-network

volumes:
  mysql-data:

networks:
  generate3d-network:
    driver: bridge
```

### 生产环境部署

#### 1. 系统要求
- **CPU**: 4核心以上
- **内存**: 8GB以上
- **存储**: 100GB以上SSD
- **网络**: 100Mbps以上带宽

#### 2. 部署步骤
```bash
# 1. 创建部署目录
mkdir -p /opt/generate3d
cd /opt/generate3d

# 2. 上传应用包
scp target/generate-3d-backend-1.0.0.jar user@server:/opt/generate3d/

# 3. 创建启动脚本
cat > start.sh << 'EOF'
#!/bin/bash
export JAVA_OPTS="-Xms2g -Xmx4g -XX:+UseG1GC"
export SPRING_PROFILES_ACTIVE=prod
nohup java $JAVA_OPTS -jar generate-3d-backend-1.0.0.jar > app.log 2>&1 &
echo $! > app.pid
EOF

chmod +x start.sh

# 4. 启动应用
./start.sh
```

#### 3. Nginx配置
```nginx
upstream generate3d_backend {
    server 127.0.0.1:8081;
}

server {
    listen 80;
    server_name api.generate3d.com;
    
    location / {
        proxy_pass http://generate3d_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # 文件上传大小限制
        client_max_body_size 100M;
        
        # 超时设置
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }
    
    # 静态资源缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
}
```

## 📊 监控运维

### 健康检查
```bash
# 应用健康状态
curl http://localhost:8081/api/actuator/health

# 详细健康信息
curl http://localhost:8081/api/actuator/health/detailed

# 应用信息
curl http://localhost:8081/api/actuator/info
```

### 性能监控
```bash
# JVM内存使用
curl http://localhost:8081/api/actuator/metrics/jvm.memory.used

# HTTP请求统计
curl http://localhost:8081/api/actuator/metrics/http.server.requests

# 数据库连接池
curl http://localhost:8081/api/actuator/metrics/hikaricp.connections
```

### 日志管理
```bash
# 查看应用日志
tail -f logs/generate3d.log

# 查看错误日志
grep ERROR logs/generate3d.log

# 日志轮转配置
logrotate -d /etc/logrotate.d/generate3d
```

## 🧪 测试

### 单元测试
```bash
# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=UserServiceTest

# 生成测试报告
mvn test jacoco:report
```

### 集成测试
```bash
# 运行集成测试
mvn test -Dtest=*IntegrationTest

# 测试覆盖率报告
mvn clean test jacoco:report
open target/site/jacoco/index.html
```

### API测试
使用Postman或curl进行API测试：
```bash
# 导入Postman集合
docs/postman/Generate3D-API.postman_collection.json

# 或使用curl脚本
bash scripts/api-test.sh
```

## 🔍 故障排查

### 常见问题

#### 1. 应用启动失败
```bash
# 检查Java版本
java -version

# 检查端口占用
netstat -tlnp | grep 8081

# 查看启动日志
tail -f logs/generate3d.log
```

#### 2. 数据库连接失败
```bash
# 测试数据库连接
mysql -h localhost -u root -p generate3d

# 检查数据库配置
grep -A 10 "datasource" src/main/resources/application.yml
```

#### 3. Redis连接失败
```bash
# 测试Redis连接
redis-cli ping

# 检查Redis配置
grep -A 5 "redis" src/main/resources/application.yml
```

#### 4. AI服务调用失败
```bash
# 检查网络连接
curl -I https://hunyuan.tencentcloudapi.com

# 验证API密钥
grep "hunyuan" src/main/resources/application.yml
```

### 性能优化

#### 1. JVM调优
```bash
# 生产环境JVM参数
export JAVA_OPTS="
  -Xms4g -Xmx8g
  -XX:+UseG1GC
  -XX:MaxGCPauseMillis=200
  -XX:+HeapDumpOnOutOfMemoryError
  -XX:HeapDumpPath=/opt/generate3d/dumps/
  -Dspring.profiles.active=prod
"
```

#### 2. 数据库优化
```sql
-- 创建索引
CREATE INDEX idx_model_user_id ON models(user_id);
CREATE INDEX idx_model_created_at ON models(created_at);
CREATE INDEX idx_evaluation_model_id ON model_evaluations(model_id);

-- 查询优化
EXPLAIN SELECT * FROM models WHERE user_id = ? ORDER BY created_at DESC LIMIT 10;
```

#### 3. Redis优化
```bash
# Redis配置优化
echo "maxmemory 2gb" >> /etc/redis/redis.conf
echo "maxmemory-policy allkeys-lru" >> /etc/redis/redis.conf
systemctl restart redis
```

## 🤝 贡献指南

### 开发规范

#### 1. 代码规范
- 使用Java 17语法特性
- 遵循阿里巴巴Java开发手册
- 使用Lombok减少样板代码
- 统一使用UTF-8编码

#### 2. 提交规范
```bash
# 提交格式
git commit -m "feat: 添加用户评分功能"
git commit -m "fix: 修复文件上传bug"
git commit -m "docs: 更新API文档"

# 提交类型
feat: 新功能
fix: 修复bug
docs: 文档更新
style: 代码格式调整
refactor: 代码重构
test: 测试相关
chore: 构建过程或辅助工具的变动
```

#### 3. 分支管理
```bash
# 主分支
main: 生产环境代码
develop: 开发环境代码

# 功能分支
feature/user-rating: 用户评分功能
feature/ai-optimization: AI调用优化

# 修复分支
hotfix/security-patch: 安全补丁
hotfix/performance-fix: 性能修复
```

### 参与贡献

1. **Fork项目**到你的GitHub账号
2. **创建功能分支**: `git checkout -b feature/amazing-feature`
3. **提交更改**: `git commit -m 'feat: 添加amazing功能'`
4. **推送分支**: `git push origin feature/amazing-feature`
5. **创建Pull Request**

## 📄 许可证

本项目采用 [MIT License](LICENSE) 许可证。

## 📞 联系我们

- **项目主页**: https://github.com/your-org/generate-3d-backend
- **问题反馈**: https://github.com/your-org/generate-3d-backend/issues
- **邮箱**: support@generate3d.com
- **文档**: https://docs.generate3d.com

## 🙏 致谢

感谢以下开源项目和服务提供商：

- [Spring Boot](https://spring.io/projects/spring-boot) - 核心框架
- [MyBatis-Plus](https://baomidou.com/) - ORM框架
- [Redis](https://redis.io/) - 缓存服务
- [腾讯混元](https://cloud.tencent.com/product/hunyuan) - AI服务
- [阿里云OSS](https://www.aliyun.com/product/oss) - 对象存储
- [Swagger](https://swagger.io/) - API文档

---

<div align="center">

**⭐ 如果这个项目对你有帮助，请给我们一个Star！**

Made with ❤️ by Generate3D Team

</div>