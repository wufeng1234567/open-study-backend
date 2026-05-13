# OpenStudy - 开放学习平台

<p align="center">
  <img alt="logo" src="https://oscimg.oschina.net/oscnet/up-d3d0a9303e11d522a06cd263f3079027715.png">
</p>

<h4 align="center">基于 Spring AI 的智能学习与知识分享平台</h4>

<p align="center">
  <a href="https://spring.io/projects/spring-boot"><img src="https://img.shields.io/badge/Spring%20Boot-3.5.11-brightgreen.svg"></a>
  <a href="https://spring.io/projects/spring-ai"><img src="https://img.shields.io/badge/Spring%20AI-1.0.0--M5-blue.svg"></a>
  <a href="https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html"><img src="https://img.shields.io/badge/JDK-17-orange.svg"></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-MIT-blue.svg"></a>
</p>

---

## 项目简介

**OpenStudy** 是一个功能丰富的在线学习平台，基于 Spring AI 框架开发，融合了智能刷题、知识分享、社区互动等核心功能。平台支持 AI 智能出题、OCR 拍照识别、RAG 知识库问答等前沿技术，为用户提供全新的智能化学习体验。

---

## 技术栈

### 后端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.5.11 | 核心框架 |
| Spring AI | 1.0.0-M5 | AI 智能服务 |
| Spring Security | 6.x | 安全认证 |
| MyBatis | 3.0.5 | ORM 持久层 |
| PageHelper | 分页插件 | 数据分页 |
| Redis | - | 缓存与会话 |
| JWT | 0.9.1 | Token 认证 |
| Druid | 1.2.28 | 数据库连接池 |

### 前端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.x | 渐进式框架 |
| Vite | - | 构建工具 |
| Element Plus | - | UI 组件库 |
| Pinia | - | 状态管理 |
| Vue Router | - | 路由管理 |

### AI 能力

| 服务 | 模型 | 用途 |
|------|------|------|
| 智谱 AI | GLM-4-Plus | 主聊天模型、AI 出题 |
| DeepSeek | deepseek-chat | 备用聊天模型 |
| SiliconFlow | PaddleOCR-VL | OCR 文字识别 |

---

## 核心功能

### 1. 智能刷题系统

- **多模式练习**：支持顺序练习、随机练习、自定义练习、模拟考试四种模式
- **AI 智能出题**：根据知识点、难度、题型自动生成题目，支持单选题、多选题、判断题、填空题、阅读理解等多种题型
- **个人学习追踪**：错题本自动收录、斩题标记已掌握、收藏重点题目
- **刷题设置**：自定义每次练习题目数量、是否包含错题等参数

### 2. 题库管理系统

- **题库创建**：可视化创建题库，支持按章节组织题目
- **题库广场**：浏览所有公开题库，发现优质学习资源
- **收藏功能**：收藏喜欢的题库，便于下次学习
- **题库详情**：查看题库统计信息、章节分布、收藏人数等

### 3. 学习笔记系统

- **富文本编辑**：基于 TipTap 的富文本编辑器，支持插入图片、代码块等
- **公开分享**：优质笔记可发布到笔记广场，供其他用户学习
- **笔记关联题库**：笔记可关联相关题库，实现知识点的强化练习
- **互动评论**：对笔记进行评论和点赞

### 4. AI 智能助手

- **智能问答**：基于 Spring AI 的智能聊天，支持多 AI 提供商切换
- **上下文记忆**：多轮对话保持上下文连贯
- **RAG 知识库**：上传文档构建个人知识库，基于检索增强生成精准回答
- **AI 出题**：根据知识点描述自动生成高质量题目

### 5. 英语学习模块

- **拍照识词**：OCR 技术识别图片中的英文单词
- **词汇管理**：创建个人词库，分类管理词汇
- **听力练习**：支持听力材料的学习和复习
- **阅读练习**：提供阅读理解训练

### 6. 学习社区

- **笔记广场**：浏览和搜索其他用户分享的学习笔记
- **排行榜**：按周榜、月榜查看热门笔记
- **留言互动**：在笔记下留言交流
- **消息中心**：通知、@我、私信功能

### 7. 实用工具箱

- **图片水印**：为图片添加文字或图片水印
- **文档转换**：支持 PDF、Word、图片等格式互转
- **图片处理**：压缩、裁剪、加边框等操作
- **文本工具**：文本格式化、加密解密等

### 8. 后台管理系统

- **用户管理**：用户账号的增删改查、角色分配
- **角色权限**：基于 RBAC 的权限控制模型
- **菜单管理**：动态配置系统菜单
- **系统监控**：在线用户、日志审计、性能监控
- **定时任务**：可视化配置定时任务
- **代码生成**：根据数据库表自动生成前后端代码

---

## 项目结构

```
OpenStudy/
├── openstudy-server-springboot3/          # 后端项目
│   ├── openstudy-admin/                    # 后台管理模块
│   ├── openstudy-framework/               # 框架核心
│   ├── openstudy-system/                   # 系统功能模块
│   ├── openstudy-common/                  # 通用工具模块
│   ├── openstudy-generator/               # 代码生成器
│   ├── openstudy-quartz/                  # 定时任务模块
│   └── sql/                               # 数据库脚本
│
└── openstudy-vue3/                        # 前端项目
    └── src/
        ├── api/                           # API 接口封装
        ├── assets/                        # 静态资源
        ├── components/                    # 公共组件
        ├── composables/                   # 组合式函数
        ├── router/                       # 路由配置
        ├── store/                        # Pinia 状态管理
        ├── utils/                        # 工具函数
        └── views/                        # 页面组件
            ├── admin/                    # 后台管理页面
            ├── front/                    # 前台用户页面
            └── system/                   # 系统配置页面
```

---

## 快速开始

### 环境要求

- JDK 17+
- Node.js 16+
- MySQL 8.0+
- Redis 6.0+

### 后端启动

```bash
cd openstudy-server-springboot3
mvn clean package
java -jar openstudy-admin/target/openstudy-admin.jar
```

### 前端启动

```bash
cd openstudy-vue3
pnpm install
pnpm dev
```

### 访问地址

- 前台首页：http://localhost
- 后台管理：http://localhost/#/login
- 默认账号：admin / admin123

---

## API 接口

项目提供完整的 RESTful API，支持 Swagger 文档在线查看：

```
http://localhost:8086/swagger-ui.html
```

---

## 特色亮点

- **AI 赋能学习**：深度集成 Spring AI，实现智能出题、问答、OCR 识别
- **前后端分离**：基于 Vue 3 + Spring Boot 3 的现代化架构
- **响应式设计**：适配桌面端和移动端多种设备
- **组件化开发**：丰富的可复用组件，提升开发效率
- **权限精细控制**：基于 RBAC 模型的细粒度权限管理

---

## 致谢

本项目基于 **若依前后端分离版本 Spring Boot 3 分支** 二次开发，感谢若依框架提供的优秀架构和丰富功能。

> 若依官网：https://ruoyi.vip
>
> 若依文档：http://doc.ruoyi.vip

---

## 许可证

本项目采用 MIT 许可证开源。
