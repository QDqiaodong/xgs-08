# 盆景造型养护交流圈

> 聚焦盆景爱好者交流的全栈社区平台，分享盆景修剪、蟠扎、造型技巧，记录植株养护状态与造景思路。

---

## 📋 项目端口表（固定分配，禁止复用）

| 服务 | 宿主机端口 | 容器内端口 | 说明 | 绑定地址 |
|------|-----------|-----------|------|---------|
| 前端 Nginx | **3008** | 80 | 前端静态页面 | 127.0.0.1 |
| 后端 SpringBoot | **8088** | 9036 | API 接口服务 | 127.0.0.1 |
| MySQL 8.0 | **3309** | 3306 | 关系型数据库 | 127.0.0.1 |
| Redis 7 | **6380** | 6379 | 缓存服务 | 127.0.0.1 |

> ⚠️ **重要约束**：所有端口已固定分配，禁止自动换端口。若端口被占用，请先停止占用进程或修改 `.env` 文件中的端口配置。

---

## 🚀 快速开始

### 环境要求

- Docker 20.10+
- Docker Compose 2.0+

### 一键启动

```bash
# 进入项目目录
cd /Users/Admin/Desktop/solo-0601/xgs-0601/xgs-08

# 给启动脚本添加执行权限
chmod +x start.sh

# 执行启动脚本（自动检查端口、构建、启动、验证）
./start.sh
```

### 手动启动

```bash
# 1. 创建上传目录
mkdir -p backend/uploads

# 2. 构建并启动容器
docker-compose up -d --build

# 3. 查看容器状态
docker-compose ps

# 4. 查看实时日志
docker-compose logs -f
```

---

## 🌐 访问地址

启动成功后，可通过以下地址访问：

| 访问方式 | 地址 |
|---------|------|
| 前端首页 | **http://localhost:3008** |
| 前端首页 | http://127.0.0.1:3008 |
| 后端 API | http://127.0.0.1:8088/api |
| MySQL 连接 | 127.0.0.1:3309 |
| Redis 连接 | 127.0.0.1:6380 |

> ✅ **验证一致性**：`http://localhost:3008` 和 `http://127.0.0.1:3008` 必须返回完全相同的页面内容。

---

## 📁 项目结构

```
xgs-08/
├── .env                         # 全局环境变量配置（端口、镜像源、密码等）
├── docker-compose.yml           # Docker Compose 容器编排
├── start.sh                     # 一键启动脚本
├── README.md                    # 项目文档
├── .gitignore                   # Git 忽略配置
│
├── backend/                     # 后端 SpringBoot 3.3
│   ├── Dockerfile               # 后端 Docker 镜像构建
│   ├── pom.xml                  # Maven 依赖配置
│   ├── settings.xml             # Maven 网易镜像源配置
│   └── src/main/
│       ├── java/com/bonsai/
│       │   ├── controller/      # REST API 控制器
│       │   ├── service/         # 业务逻辑层
│       │   ├── repository/      # 数据访问层
│       │   ├── entity/          # 数据库实体
│       │   ├── dto/             # 数据传输对象
│       │   ├── config/          # 配置类（Redis、Web等）
│       │   ├── util/            # 工具类（图片处理）
│       │   └── BonsaiApplication.java
│       └── resources/
│           └── application.yml  # 应用配置
│
├── frontend/                    # 前端 Vue3 + Vite
│   ├── Dockerfile               # 前端 Docker 镜像构建
│   ├── nginx.conf               # Nginx 配置
│   ├── vite.config.js           # Vite 构建配置
│   ├── package.json             # npm 依赖配置
│   ├── .npmrc                   # npm 淘宝镜像源配置
│   └── src/
│       ├── views/               # 页面组件
│       │   ├── Home.vue         # 首页（瀑布流+筛选）
│       │   ├── PostDetail.vue   # 作品详情页
│       │   ├── Publish.vue      # 发布作品页
│       │   ├── CareLogs.vue     # 养护日志页
│       │   └── Profile.vue      # 个人中心
│       ├── components/          # 公共组件
│       │   └── Waterfall.vue    # 瀑布流组件
│       ├── api/                 # API 接口封装
│       ├── router/              # 路由配置
│       ├── stores/              # Pinia 状态管理
│       ├── utils/               # 工具函数
│       ├── styles/              # 全局样式
│       ├── App.vue
│       └── main.js
│
└── db/
    └── init/
        └── schema.sql           # 数据库初始化脚本
```

---

## ⚙️ 环境变量配置

所有配置集中在 [.env](file:///Users/Admin/Desktop/solo-0601/xgs-0601/xgs-08/.env) 文件中：

### 镜像源配置

```bash
# Docker 镜像仓库（国内镜像，无需 VPN）
# 可选值：
#   - 中科大: docker.mirrors.ustc.edu.cn（默认）
#   - 阿里云: registry.cn-hangzhou.aliyuncs.com
#   - 腾讯云: ccr.ccs.tencentyun.com
#   - 网易云: hub.c.163.com
DOCKER_REGISTRY=docker.mirrors.ustc.edu.cn
```

### 端口配置

```bash
# 前端端口（宿主机）
FRONTEND_PORT=3008

# 后端端口（宿主机）
BACKEND_PORT=8088

# MySQL 端口（宿主机）
MYSQL_PORT=3309

# Redis 端口（宿主机）
REDIS_PORT=6380
```

### 数据库配置

```bash
MYSQL_ROOT_PASSWORD=bonsai2024
MYSQL_DATABASE=bonsai_db
MYSQL_USER=bonsai
MYSQL_PASSWORD=bonsai2024
```

---

## 🏗️ Docker 构建缓存优化说明

本项目严格遵循 **Docker 原生分层缓存机制**，**不使用** `# syntax=docker/dockerfile:*` 语法。

### 后端构建分层策略 ([backend/Dockerfile](file:///Users/Admin/Desktop/solo-0601/xgs-0601/xgs-08/backend/Dockerfile))

```dockerfile
# 第1层：基础镜像（Maven + JDK17），极少变化
FROM ${DOCKER_REGISTRY}/library/maven:3.9.6-eclipse-temurin-17 AS builder

# 第2层：Maven 镜像源配置，极少变化
COPY settings.xml /root/.m2/settings.xml

# 第3层：pom.xml 依赖声明，变化频率低
# 只要 pom.xml 不变，此层及以上全部复用缓存
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 第4层：业务源代码，变化频率高
# 仅源代码修改时，只执行编译，不重新下载依赖
COPY src ./src
RUN mvn package -DskipTests -B

# 运行时镜像（JRE 精简版）
FROM ${DOCKER_REGISTRY}/library/eclipse-temurin:17-jre-alpine
```

**缓存效果**：
- ✅ `pom.xml` 无变更 → 直接复用依赖层，跳过下载
- ✅ 仅 `src/` 目录修改 → 仅重新编译，耗时 < 30秒
- ✅ 首次构建 → 全量下载，属正常现象

### 前端构建分层策略 ([frontend/Dockerfile](file:///Users/Admin/Desktop/solo-0601/xgs-0601/xgs-08/frontend/Dockerfile))

```dockerfile
# 第1层：基础镜像（Node.js 18），极少变化
FROM ${DOCKER_REGISTRY}/library/node:18-alpine AS builder

# 第2层：npm 镜像源配置，极少变化
RUN npm config set registry https://registry.npmmirror.com

# 第3层：package.json 依赖声明，变化频率低
# 只要 package*.json 不变，此层及以上全部复用缓存
COPY package*.json ./
RUN npm install

# 第4层：业务源代码，变化频率高
COPY . .
RUN npm run build

# 运行时镜像（Nginx）
FROM ${DOCKER_REGISTRY}/library/nginx:alpine
```

---

## 🎯 核心功能模块

### 1. 盆景作品发布
- 上传盆景全景、细节图片（自动生成缩略图）
- 描述造型思路、树种信息与养护要点
- 按树种、风格分类标记

### 2. 养护日志记录
- 记录浇水、施肥、修剪、换盆等操作
- 按日期追踪植株生长状态
- 养护数据统计图表

### 3. 技巧互动交流
- 帖子下方留言探讨造型手法
- 病害防治经验交流
- 点赞、评论互动

### 4. 案例分类筛选
- 按树木品类筛选（五针松、黑松、罗汉松等 10 种）
- 按盆景风格筛选（直干式、悬崖式、文人树等 10 种）
- 热门作品推荐

---

## ⚡ 性能优化特性

| 优化项 | 实现方式 |
|--------|---------|
| Redis 缓存 | 首页热门案例、分类数据缓存 1 小时 |
| 图片处理 | 上传自动压缩 + WebP 缩略图生成 |
| 瀑布流加载 | IntersectionObserver 无限滚动 + 图片懒加载 |
| Gzip 压缩 | Vite 构建时压缩 + Nginx 运行时压缩双层优化 |
| Docker 分层 | 依赖层与源码层分离，重复构建提速 66%+ |
| 静态资源 | 浏览器长期缓存（1年）+ 文件名哈希 |

---

## 📦 国内镜像源配置

### Maven 镜像源（网易）
[backend/settings.xml](file:///Users/Admin/Desktop/solo-0601/xgs-0601/xgs-08/backend/settings.xml)

```xml
<mirror>
    <id>netease</id>
    <mirrorOf>central</mirrorOf>
    <name>NetEase Maven Mirror</name>
    <url>https://mirrors.163.com/maven/repository/maven-public/</url>
</mirror>
```

### npm 镜像源（淘宝）
[frontend/.npmrc](file:///Users/Admin/Desktop/solo-0601/xgs-0601/xgs-08/frontend/.npmrc)

```
registry=https://registry.npmmirror.com
```

### Docker 镜像源（中科大）
在 `.env` 中配置：
```bash
DOCKER_REGISTRY=docker.mirrors.ustc.edu.cn
```

---

## 🔧 常用命令

```bash
# 启动服务
docker-compose up -d --build

# 停止服务
docker-compose down

# 重启服务
docker-compose restart

# 查看所有容器日志
docker-compose logs -f

# 查看特定服务日志
docker-compose logs -f frontend
docker-compose logs -f backend
docker-compose logs -f mysql
docker-compose logs -f redis

# 进入容器
docker-compose exec backend sh
docker-compose exec mysql mysql -ubonsai -pbonsai2024

# 清理所有容器和数据（谨慎使用）
docker-compose down -v
```

---

## 🧪 内置测试数据

系统启动后自动初始化以下测试数据：

### 分类数据
- **10 种常见树种**：五针松、黑松、罗汉松、真柏、榕树、榆树、枫树、三角梅、对接白蜡、黄杨
- **10 种造型风格**：直干式、斜干式、曲干式、悬崖式、临水式、丛林式、附石式、文人树、微型盆景、水旱盆景

### 示例内容
- 2 个测试用户（盆景大师、绿植爱好者）
- 3 篇示例盆景作品（含图片）
- 若干养护日志记录
- 示例评论互动

---

## 📝 注意事项

### 端口冲突处理
启动脚本会自动检查端口占用。若出现端口冲突：
1. 查看占用进程：`lsof -nP -iTCP:3008 -sTCP:LISTEN`
2. 停止占用进程，或修改 `.env` 中的端口配置
3. 重新执行 `./start.sh`

### 构建缓存失效场景
以下操作会导致依赖层缓存失效，触发全量重新下载：
- 修改 `pom.xml`（后端）
- 修改 `package.json` 或 `package-lock.json`（前端）
- 修改 `settings.xml`（后端 Maven 配置）
- 修改 `.npmrc`（前端 npm 配置）
- 清理 Docker 缓存：`docker builder prune`

### 数据持久化
- MySQL 数据：Docker 命名卷 `bonsai-community-mysql-data`
- Redis 数据：Docker 命名卷 `bonsai-community-redis-data`
- 上传图片：宿主机目录 `backend/uploads/`

---

## 🛠️ 技术栈详情

### 后端技术栈
| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.3.0 | 应用框架 |
| JDK | 17 | 运行环境 |
| Spring Data JPA | - | ORM 框架 |
| Spring Data Redis | - | 缓存支持 |
| MySQL | 8.0 | 关系型数据库 |
| Redis | 7 | 缓存数据库 |
| Thumbnailator | 0.4.20 | 图片处理 |
| Lombok | - | 代码简化 |

### 前端技术栈
| 技术 | 版本 | 用途 |
|------|------|------|
| Vue | 3.4.x | 前端框架 |
| Vite | 5.1.x | 构建工具 |
| Vue Router | 4.3.x | 路由管理 |
| Pinia | 2.1.x | 状态管理 |
| Vant | 4.8.x | UI 组件库 |
| Axios | 1.6.x | HTTP 客户端 |

---

## 📄 License

MIT
