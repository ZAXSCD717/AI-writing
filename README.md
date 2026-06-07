# AI 小说转剧本工具

---

## 项目简介

本项目是一款 AI 辅助剧本创作工具，帮助小说作者将自己的作品低成本改编为结构化剧本。
用户只需上传小说文本（3 章以上），AI 自动分析角色、场景、情节，生成符合标准 Schema 的剧本初稿（YAML 格式），
作者可在线预览、编辑、导出，大幅降低小说到剧本的改编门槛。

### 核心能力

| 功能 | 说明 |
|------|------|
| 📖 小说上传 | 粘贴小说正文，自动检测章节数 |
| 🤖 AI 自动转换 | 调用 DeepSeek API 分析角色/场景/对白，生成结构化 YAML 剧本 |
| 📝 在线编辑 | 在浏览器中直接编辑 YAML 剧本内容 |
| 📥 YAML 导出 | 下载标准 YAML 格式剧本文件 |
| 📋 剧本管理 | 多剧本列表管理，支持查看/编辑/导出 |

---

## 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 后端框架 | Spring Boot | 3.4.3 |
| AI 集成 | Spring AI (OpenAI-compatible → DeepSeek) | 1.0.4 |
| ORM | MyBatis-Plus | 3.5.9 |
| 数据库 | MySQL | 8.0 |
| 前端框架 | Vue 3 + Vite | 8.x |
| UI 组件 | Element Plus | 最新 |
| 构建工具 | Maven | 3.9+ |

---

## 快速开始

### 前置依赖

- JDK 17+
- Maven 3.9+
- Node.js 18+
- MySQL 8.0

### 1. 克隆项目

```bash
git clone git@github.com:ZAXSCD717/AI-writing.git
cd AI-writing
```

### 2. 配置数据库

```bash
# 登录 MySQL 并创建数据库
mysql -u root -p
# 执行以下 SQL
CREATE DATABASE IF NOT EXISTS ainovel DEFAULT CHARACTER SET utf8mb4;
```

数据库表会在首次启动时自动创建（已提供建表 SQL，或由 MyBatis-Plus 自动建表）。

### 3. 配置 API Key

编辑 `backend/src/main/resources/application.yml`，将 DeepSeek API Key 配置到环境变量或直接填入：

```yaml
spring:
  ai:
    openai:
      api-key: your-deepseek-api-key-here
```

### 4. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端启动在 `http://localhost:8888`

### 5. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端启动在 `http://localhost:5173`，Vite 已配置代理，API 请求自动转发到后端。

---

## 使用方法

### 第一步：上传小说

1. 打开浏览器访问 `http://localhost:5173`
2. 点击「上传小说」卡片
3. 填写小说标题、作者（选填）
4. 粘贴小说正文（至少 3 章）
5. 点击「上传并转换」

### 第二步：AI 自动转换

- 系统自动检测章节数
- 调用 DeepSeek API 分析小说内容
- 生成结构化 YAML 剧本（包含元信息、角色表、幕/场结构、对白等）

### 第三步：编辑剧本

- 在剧本详情页查看生成的 YAML 内容
- 可切换到编辑模式手动修改
- 点击「保存」更新剧本

### 第四步：导出 YAML

- 在剧本详情页或列表页点击「导出 YAML」
- 下载 `.yaml` 格式的剧本文件

---

## YAML Schema

本项目定义了完整的剧本 YAML Schema，详见 [docs/YAML_SCHEMA.md](docs/YAML_SCHEMA.md)。

Schema 核心结构：

```
script
├── metadata          # 剧本元信息（标题、来源、版本等）
├── characters        # 角色表（ID/名称/性别/性格/描述）
└── acts              # 幕
    ├── scenes        # 场
        ├── setting   # 场景设置（地点/时间/氛围/道具）
        └── elements  # 剧本元素
            ├── narration   # 旁白/场景描写
            ├── dialogue    # 对白（引用角色 ID）
            ├── action      # 动作描述
            └── transition  # 转场效果
```

---

## 项目结构

```
AI Novel to Script Tool/
├── backend/                    # Spring Boot 后端
│   ├── src/main/java/com/ainovel/backend/
│   │   ├── common/             # 统一响应 + 全局异常处理
│   │   ├── config/             # Spring AI 配置、CORS、自动填充
│   │   ├── controller/         # REST API 控制器
│   │   ├── mapper/             # MyBatis-Plus Mapper
│   │   ├── model/
│   │   │   ├── dto/            # 请求/响应 DTO
│   │   │   └── entity/         # 数据库实体
│   │   └── service/            # 业务逻辑
│   ├── src/main/resources/
│   │   └── application.yml     # 配置文件
│   └── pom.xml
├── frontend/                   # Vue 3 前端
│   ├── src/
│   │   ├── api/                # Axios API 封装
│   │   ├── router/             # Vue Router 路由
│   │   └── views/              # 页面组件
│   ├── vite.config.js          # Vite 配置（含 API 代理）
│   └── package.json
├── docs/
│   └── YAML_SCHEMA.md          # YAML Schema 设计文档
├── README.md
└── .gitignore
```

---

## API 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/novels` | 上传小说 |
| GET | `/api/novels/{id}` | 获取小说详情 |
| GET | `/api/novels` | 小说列表（分页） |
| POST | `/api/novels/{id}/convert` | AI 转换小说为剧本 |
| GET | `/api/scripts/{id}` | 获取剧本详情 |
| GET | `/api/scripts` | 剧本列表（分页） |
| PUT | `/api/scripts/{id}` | 更新剧本（编辑） |
| GET | `/api/scripts/{id}/export` | 导出 YAML 文件 |

---

## Demo 视频

<!-- TODO: 替换为你的 Demo 视频链接 -->
> Demo 视频链接：通过网盘分享的文件：6月7日.mp4
> 链接: https://pan.baidu.com/s/1Tr-HKb47WXzUhaDhDG9hjg?pwd=f28e 提取码: f28e

---

## 许可证

MIT License © 2024
