# 🤖 智能健身问答助手

基于 **Spring Boot 3.4.1 + Vue 3** 的全栈健身 AI 应用，集成 Spring AI 大模型，提供智能问答、减脂餐食谱、健身训练、健康资讯、补剂指南等功能。

## ✨ 功能特性

| 模块 | 功能 |
|------|------|
| 💬 AI 智能问答 | 基于 Spring AI 的健身知识对话，支持 SSE 流式输出 |
| 🥗 减脂餐食谱 | 105 道减脂餐，支持搜索、详情查看、收藏、每日记录 |
| 🏋️ 健身训练 | 20 套训练计划，按类别/难度筛选，收藏和记录 |
| 📰 健康资讯 | 8 篇科普文章，涵盖营养知识、训练技巧、减脂误区、心理健康 |
| 💊 补剂指南 | 10 种运动补剂详解，包含功效、剂量、服用时机、注意事项 |
| ❤️ 收藏功能 | 支持收藏减脂餐、训练、文章、补剂，4 个独立 Tab |
| 📊 数据看板 | ECharts 可视化：7 天卡路里趋势、训练类别分布、每日记录 |
| 👤 个人中心 | 头像上传、BMI 指数可视化、身体数据管理 |
| 🌙 暗黑模式 | CSS 变量主题切换，localStorage 持久化 |
| ⚙️ 后台管理 | 管理员 CRUD 全部内容（减脂餐/训练/文章/补剂） |

## 🛠️ 技术栈

### 后端
- **Java 17** + **Spring Boot 3.4.1**
- **Spring AI** (ChatClient + SSE 流式输出)
- **Spring Security** + **JWT** 认证
- **MyBatis-Plus** ORM
- **MySQL 8** 数据库（10 张表）

### 前端
- **Vue 3** (Composition API + `<script setup>`)
- **Vite** 构建工具
- **Pinia** 状态管理
- **Vue Router** 路由守卫
- **Element Plus** UI 组件库
- **ECharts** 数据可视化
- **Axios** HTTP 请求

## 📁 项目结构

```
Project_AIchat/
├── src/main/java/com/example/project_aichat/
│   ├── config/          # Spring Security 配置
│   ├── controller/      # REST API 控制器（10个）
│   ├── dto/response/    # 统一响应封装
│   ├── entity/          # 数据库实体（10个）
│   ├── mapper/          # MyBatis-Plus Mapper
│   ├── security/        # JWT 认证过滤器
│   ├── service/         # 业务逻辑层
│   └── ProjectAIchatApplication.java
├── src/main/resources/
│   ├── db/              # SQL 建表 + 种子数据
│   └── application.yml  # 应用配置
├── frontend/
│   ├── src/
│   │   ├── api/         # Axios API 封装（8个）
│   │   ├── router/      # Vue Router 路由
│   │   ├── stores/      # Pinia 状态管理
│   │   ├── styles/      # 全局样式 + CSS 变量
│   │   ├── utils/       # Markdown 渲染工具
│   │   └── views/       # Vue 页面组件（14个）
│   └── package.json
└── pom.xml
```

## 🗄️ 数据库表结构

| 表名 | 说明 |
|------|------|
| `user` | 用户表（含身体数据） |
| `conversation` | 对话会话表 |
| `message` | 消息记录表 |
| `meal` | 减脂餐食谱表（105条） |
| `workout` | 健身训练表（20条） |
| `article` | 健康资讯表（8条） |
| `supplement` | 补剂指南表（10条） |
| `user_favorite` | 用户收藏表 |
| `daily_record` | 每日饮食训练记录表 |

## 🚀 快速开始

### 环境要求
- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Maven 3.8+

### 1. 数据库初始化

```bash
mysql -u root -p < src/main/resources/db/schema.sql
mysql -u root -p < src/main/resources/db/seed_meals.sql
mysql -u root -p < src/main/resources/db/seed_articles_supplements.sql
```

### 2. 后端配置

编辑 `src/main/resources/application.yml`，修改数据库连接和 AI API Key：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/aichat
    username: root
    password: your_password
  ai:
    openai:
      api-key: your-api-key
```

### 3. 启动后端

```bash
mvn spring-boot:run
```

后端运行在 `http://localhost:8080`

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端运行在 `http://localhost:5173`

## 📱 页面路由

| 路径 | 页面 | 权限 |
|------|------|------|
| `/login` | 登录 | 公开 |
| `/register` | 注册 | 公开 |
| `/` | AI 聊天主页 | 需登录 |
| `/meals` | 减脂餐列表 | 公开 |
| `/meals/:id` | 减脂餐详情 | 公开 |
| `/workouts` | 训练列表 | 公开 |
| `/workouts/:id` | 训练详情 | 公开 |
| `/articles` | 健康资讯列表 | 公开 |
| `/articles/:id` | 资讯详情 | 公开 |
| `/supplements` | 补剂列表 | 公开 |
| `/supplements/:id` | 补剂详情 | 公开 |
| `/favorites` | 我的收藏 | 需登录 |
| `/dashboard` | 数据看板 | 需登录 |
| `/profile` | 个人中心 | 需登录 |
| `/admin` | 后台管理 | 管理员 |

## 🔐 默认管理员账号

注册后在数据库中修改用户 role 为 `admin`：

```sql
UPDATE user SET role = 'admin' WHERE phone = 'your_phone';
```

## 📄 License

MIT
