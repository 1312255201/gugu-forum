# 咕咕论坛 (GuGu Forum)

<div align="center">

![Java](https://img.shields.io/badge/Java-21+-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)
![License](https://img.shields.io/badge/License-MIT-blue.svg)
![Version](https://img.shields.io/badge/Version-1.0.0-red.svg)

**一个现代化的校园论坛系统**

🌟 功能丰富 | 🚀 高性能 | 🔒 安全可靠 | 📱 响应式设计

[项目演示](https://www.gugufish.cn/) • [API文档](https://www.gugufish.cn/swagger-ui/index.html) • [问题反馈](https://github.com/1312255201/gugu-forum/issues)

</div>

---

## 📋 项目简介

咕咕论坛是一个专为校园环境设计的现代化论坛系统，提供了丰富的社区功能和完善的用户体验。系统采用前后端分离架构，后端基于Spring Boot生态构建，具有高性能、高可用、易扩展的特点。

### ✨ 核心特性

- 🏛️ **论坛系统**：支持多分类主题发布、评论互动、点赞收藏
- 🔍 **失物招领**：校园失物发布与查找，支持多条件筛选
- 🎯 **校园活动**：活动信息发布与管理，状态追踪
- 👤 **用户系统**：完整的用户注册、登录、资料管理
- 🔐 **安全认证**：JWT令牌认证，Spring Security权限控制
- 📧 **邮件服务**：邮箱验证码、通知推送
- 🌤️ **天气服务**：基于地理位置的实时天气查询
- 📷 **文件上传**：头像上传、图片管理，MinIO对象存储
- 📱 **响应式设计**：适配PC、平板、手机多端
- 📖 **API文档**：Swagger自动生成接口文档

---

## 🛠️ 技术栈

### 后端技术

| 技术栈 | 版本   | 描述 |
|--------|------|------|
| Java | 21   | 核心开发语言 |
| Spring Boot | 3.x  | 主框架 |
| Spring Security | 6.x  | 安全框架 |
| Spring Data JPA | 3.x  | 数据访问层 |
| MyBatis-Plus | 3.x  | ORM框架 |
| M.SQL | 5.7+ | 关系型数据库 |
| Redis | 5.x  | 缓存数据库 |
| RabbitMQ | 3.x  | 消息队列 |
| MinIO | 最新版  | 对象存储 |
| JWT | -    | 身份认证 |
| Swagger | 3.x  | API文档 |
| Lombok | -    | 简化开发 |
| SLF4J + Logback | -    | 日志框架 |

### 开发工具

- **IDE**: IntelliJ IDEA / Eclipse
- **构建工具**: Maven 3.x
- **版本控制**: Git
- **接口测试**: Postman / Swagger UI
- **数据库管理**: Navicat / DataGrip

---

## 🏗️ 系统架构

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│                 │    │                 │    │                 │
│   前端应用      │◄──►│   Nginx代理     │◄──►│   Spring Boot   │
│                 │    │                 │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                                       │
                       ┌─────────────────┐            │
                       │                 │            │
                       │     Redis       │◄───────────┤
                       │     缓存        │            │
                       └─────────────────┘            │
                                                       │
                       ┌─────────────────┐            │
                       │                 │            │
                       │     MySQL       │◄───────────┤
                       │     数据库      │            │
                       └─────────────────┘            │
                                                       │
                       ┌─────────────────┐            │
                       │                 │            │
                       │     MinIO       │◄───────────┤
                       │    对象存储     │            │
                       └─────────────────┘            │
                                                       │
                       ┌─────────────────┐            │
                       │                 │            │
                       │    RabbitMQ     │◄───────────┘
                       │    消息队列     │
                       └─────────────────┘
```

---

## 📦 功能模块

### 🏛️ 论坛核心
- **主题管理**: 创建、编辑、删除主题
- **分类浏览**: 按主题分类筛选内容
- **评论系统**: 发表、查看、删除评论
- **互动功能**: 点赞、收藏主题
- **置顶机制**: 管理员设置重要主题置顶
- **权限控制**: 禁言用户限制发言

### 👤 用户系统
- **账户管理**: 注册、登录、个人信息管理
- **安全设置**: 密码修改、邮箱绑定
- **隐私配置**: 个人隐私偏好设置
- **头像上传**: 个人头像自定义

### 🔍 失物招领
- **信息发布**: 发布失物/招领信息
- **智能搜索**: 地点、时间、状态多条件筛选
- **状态管理**: 物品找到后状态更新
- **权限控制**: 仅发布者可管理自己的信息

### 🎯 校园活动
- **活动展示**: 校园活动信息浏览
- **状态筛选**: 按活动状态查看
- **详情查看**: 活动完整信息展示

### 🛠️ 系统管理
- **通知系统**: 系统消息、互动通知
- **文件管理**: 图片上传、存储管理
- **日志监控**: 系统操作日志记录
- **API文档**: 自动生成接口文档

---

## 🚀 快速开始

### 环境要求

- **Java**: 21+
- **Maven**: 3.6+
- **MySQL**: 5.7+
- **Redis**: 5.x
- **RabbitMQ**: 3.x
- **MinIO**: 最新版

### 克隆项目

```bash
git clone https://github.com/1312255201/gugu-forum.git
cd gugu-forum
```

### 数据库配置

1. 创建MySQL数据库：
```sql
CREATE DATABASE gugu_forum CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 导入数据库脚本：
```bash
mysql -u root -p gugu_forum < forum.sql
```

### 配置文件

复制配置模板并修改数据库连接信息：

```bash
cp src/main/resources/application.yml.template src/main/resources/application.yml
```

修改 `application.yml` 中的配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/gugu_forum?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password
    
  data:
    redis:
      host: localhost
      port: 6379
      password: your_redis_password
      
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    
  minio:
    endpoint: http://localhost:9000
    username: your_minio_access_key
    password: your_minio_secret_key
```

### 启动应用

```bash
# 安装依赖
mvn clean install

# 启动应用
mvn spring-boot:run
```

应用启动后访问：
- **应用首页**: http://localhost:8080
- **API文档**: http://localhost:8080/swagger-ui/index.html

---

## 🌐 生产环境部署

### Nginx配置

```nginx
server {
    listen 80;
    server_name your-domain.com;
    
    # 静态资源
    location /images/ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        
        # 缓存设置
        expires 30d;
        add_header Cache-Control "public, immutable";
    }
    
    # API请求
    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
    
    # 前端应用
    location / {
        try_files $uri $uri/ /index.html;
        root /var/www/gugu-forum;
        index index.html;
    }
}
```

### SSL证书配置

```nginx
server {
    listen 443 ssl http2;
    server_name your-domain.com;
    
    ssl_certificate /path/to/your/cert.pem;
    ssl_certificate_key /path/to/your/key.pem;
    
    # ... 其他配置
}

# HTTP重定向到HTTPS
server {
    listen 80;
    server_name your-domain.com;
    return 301 https://$server_name$request_uri;
}
```

---

## 📊 性能优化

### 数据库优化
- 添加适当的索引
- 配置连接池参数
- 启用查询缓存

### 缓存策略
- Redis缓存热点数据
- 静态资源CDN加速
- 浏览器缓存配置

### JVM调优
```bash
java -Xms512m -Xmx2g -XX:+UseG1GC -jar gugu-forum.jar
```

---

## 🔧 开发指南

### 项目结构

```
template-backend/
├── src/main/java/cn/gugufish/
│   ├── config/          # 配置类
│   ├── controller/      # 控制器层
│   ├── service/         # 服务层
│   ├── entity/          # 实体类
│   ├── utils/           # 工具类
│   ├── filter/          # 过滤器
│   └── GuguForumApplication.java
├── src/main/resources/
│   ├── application.yml  # 配置文件
│   └── logback-spring.xml
├── pom.xml
└── README.md
```

### 开发规范

1. **代码规范**: 遵循阿里巴巴Java开发手册
2. **注释规范**: 所有公共方法必须添加详细注释
3. **异常处理**: 统一异常处理机制
4. **日志规范**: 合理使用日志级别
5. **数据库规范**: 统一命名规范和字段类型

### API设计规范

- RESTful API设计
- 统一响应格式
- 合理的HTTP状态码
- 完善的错误信息

---

## 🧪 测试


### 接口测试

访问 Swagger UI 进行接口测试：
http://localhost:8080/swagger-ui/index.html

---

## 🔍 故障排查

### 常见问题

1. **启动失败**
   - 检查端口是否被占用
   - 验证数据库连接配置
   - 查看启动日志

2. **数据库连接失败**
   - 确认数据库服务状态
   - 检查用户名密码
   - 验证网络连通性

3. **文件上传失败**
   - 检查MinIO服务状态
   - 验证访问密钥配置
   - 确认存储桶权限

### 日志查看

```bash
# 查看应用日志
tail -f logs/gugu-forum.log

# 查看错误日志
grep ERROR logs/gugu-forum.log
```

---

## 🤝 贡献指南

我们欢迎所有形式的贡献！

### 如何贡献

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/amazing-feature`)
3. 提交更改 (`git commit -m 'Add some amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 开启 Pull Request

### 开发流程

1. 在Issue中讨论新特性或bug修复
2. 遵循代码规范进行开发
3. 添加必要的测试用例
4. 更新相关文档
5. 提交PR并等待review

---

## 📄 开源协议

本项目基于 [MIT License](LICENSE) 开源协议。

---

## 🙏 致谢

感谢以下开源项目：

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Spring Security](https://spring.io/projects/spring-security)
- [MyBatis-Plus](https://baomidou.com/)
- [MinIO](https://min.io/)
- [RabbitMQ](https://www.rabbitmq.com/)
- [Swagger](https://swagger.io/)

---

## 📞 联系我们

- **作者**: GuguFish
- **邮箱**: [1312255201@qq.com]
- **网站**: [https://www.gugufish.cn/](https://www.gugufish.cn/)
- **GitHub**: [https://github.com/1312255201/gugu-forum](https://github.com/1312255201/gugu-forum)

---

<div align="center">

**⭐ 如果这个项目对你有帮助，请给它一个星标！**

Made with ❤️ by GuguFish

</div>
