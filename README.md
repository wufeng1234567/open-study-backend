# OpenStudy - AI 智能刷题系统

<p align="center">
  <img alt="logo" src="https://oscimg.oschina.net/oscnet/up-d3d0a9303e11d522a06cd263f3079027715.png">
</p>

<h4 align="center">基于 Spring AI 的智能刷题与出题平台</h4>

<p align="center">
  <a href="https://spring.io/projects/spring-boot"><img src="https://img.shields.io/badge/Spring%20Boot-3.5.11-brightgreen.svg"></a>
  <a href="https://spring.io/projects/spring-ai"><img src="https://img.shields.io/badge/Spring%20AI-1.0.0--M5-blue.svg"></a>
  <a href="https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html"><img src="https://img.shields.io/badge/JDK-17-orange.svg"></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-MIT-blue.svg"></a>
</p>

---

## 📖 项目简介

**OpenStudy** 是一个基于 **Spring AI** 框架开发的智能化刷题平台，核心能力包括：

- 🤖 **AI 智能出题** - 根据知识点、难度、题型自动生成题目
- 📝 **AI 辅助刷题** - 智能解析答案、错题分析、个性化推荐
- 🔍 **OCR 文字识别** - 图片题目识别、手写答案识别
- 🧠 **RAG 知识库** - 基于检索增强的智能问答
- 🎯 **多 AI 提供商** - 智谱 AI、DeepSeek、SiliconFlow 无缝切换
- ⚡ **流式响应** - SSE 实时输出，提升用户体验

> 本项目聚焦于 **AI 教育场景落地**，提供完整的 AI 基础设施集成方案，便于快速构建智能学习应用。

---

## 🛠️ 技术栈

### AI 核心框架

| 技术 | 版本 | 说明 |
|------|------|------|
| **Spring AI** | 1.0.0-M5 | Spring 官方 AI 抽象层 |
| Spring AI ZhiPuAI | - | 智谱 AI 适配器 |
| Spring AI OpenAI | - | OpenAI 兼容接口（DeepSeek/SiliconFlow） |
| Reactor | - | 响应式编程（流式输出） |

### AI 提供商

| 提供商 | 模型 | 用途 | 状态 |
|--------|------|------|------|
| **智谱 AI** | GLM-4-Plus | 主聊天模型、题目生成 | ✅ 已集成 |
| **DeepSeek** | deepseek-chat | 备用聊天模型 | ✅ 已集成 |
| **SiliconFlow** | PaddleOCR-VL | OCR 文字识别 | ✅ 已集成 |
| OpenAI | gpt-3.5-turbo | 可扩展支持 | ⚙️ 配置中 |

### OCR 识别

| 技术 | 版本 | 说明 |
|------|------|------|
| **Tess4J** | 5.4.0 | 本地 OCR 引擎（Tesseract） |
| SiliconFlow OCR | - | 云端 OCR API |

### 基础框架

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.5.11 | 核心框架 |
| JDK | 17 | Java 运行环境 |
| MyBatis | 3.0.5 | ORM 持久层 |
| Druid | 1.2.28 | 数据库连接池 |
| Redis | - | 缓存与会话管理 |
| Spring Security | - | 安全认证 |
| JWT | 0.9.1 | Token 认证 |

---

## 🏗️ 项目结构（AI 核心模块）

```
openstudy-admin/src/main/java/com/openstudy/
│
├── ai/                              # AI 核心模块
│   ├── controller/
│   │   └── AiController.java        # AI 接口控制器（聊天、出题、刷题）
│   │
│   ├── service/                     # AI 业务服务层
│   │   ├── AiService.java           # 通用聊天服务（多提供商切换）
│   │   ├── AiQuestionService.java   # AI 出题服务
│   │   ├── BankContextService.java  # 题库上下文管理
│   │   ├── UserPreferenceService.java  # 用户偏好学习
│   │   ├── ConversationHistoryService.java  # 对话历史管理
│   │   ├── GenerateHistoryService.java      # 生成历史记录
│   │   │
│   │   ├── core/                    # 核心生成服务
│   │   │   ├── QuestionGenService.java      # 普通题目生成
│   │   │   └── CompositeGenService.java     # 组合题生成（阅读理解）
│   │   │
│   │   ├── infra/                   # 基础设施
│   │   │   ├── AiProviderManager.java       # AI 提供商管理器
│   │   │   ├── AiProvider.java              # AI 提供商接口
│   │   │   ├── ZhipuAiProvider.java         # 智谱 AI 实现
│   │   │   ├── DeepSeekProvider.java        # DeepSeek 实现
│   │   │   └── ...
│   │   │
│   │   └── rag/                     # RAG 检索增强生成
│   │       ├── RagChatService.java          # RAG 聊天服务
│   │       └── RagDocumentService.java      # RAG 文档管理
│   │
│   ├── model/                       # 数据模型
│   │   ├── ConversationMessage.java # 对话消息
│   │   └── GenerateRecord.java      # 生成记录
│   │
│   ├── template/                    # Prompt 模板
│   │   └── PromptTemplate.java      # 提示词模板管理
│   │
│   ├── parser/                      # 响应解析器
│   │   └── QuestionParser.java      # AI 响应解析为题目对象
│   │
│   └── util/                        # AI 工具类
│       └── ...
│
├── ocr/                             # OCR 识别模块
│   ├── controller/
│   │   └── OcrController.java       # OCR 接口
│   ├── service/
│   │   ├── OcrService.java          # OCR 服务接口
│   │   └── impl/
│   │       ├── Tess4jOcrService.java    # 本地 Tesseract 实现
│   │       └── SiliconFlowOcrService.java  # 云端 OCR 实现
│   └── domain/
│       └── OcrResult.java           # OCR 结果模型
│
├── questionBank/                    # 题库管理
├── questionMain/                    # 题目主体
├── questionError/                   # 错题本
├── questionMarked/                  # 标记题目
├── favoriteQuestion/                # 收藏题目
└── ...
```

---

## 🚀 快速开始

### 环境要求

- **JDK**: 17+
- **Maven**: 3.6+
- **MySQL**: 8.0+
- **Redis**: 6.0+

### 安装步骤

#### 1. 克隆项目

```bash
git clone https://gitee.com/your-repo/openstudy-server-springboot3.git
cd openstudy-server-springboot3
```

#### 2. 创建数据库

```sql
source sql/ry_20260330.sql
```

#### 3. 配置 AI API Key

编辑 `openstudy-admin/src/main/resources/application.yml`：

```yaml
spring:
  ai:
    # 智谱 AI 配置
    zhipuai:
      api-key: your_zhipuai_api_key
      base-url: https://open.bigmodel.cn/api/paas
      chat:
        options:
          model: glm-4-plus
          temperature: 0.7
    
    # DeepSeek 配置（通过 OpenAI 兼容接口）
    openai:
      api-key: your_deepseek_api_key
      base-url: https://api.deepseek.com
      chat:
        options:
          model: deepseek-chat
    
    # SiliconFlow OCR 配置
    siliconflow:
      api-key: your_siliconflow_api_key
      base-url: https://api.siliconflow.cn
      chat:
        options:
          model: PaddlePaddle/PaddleOCR-VL-1.5
```

#### 4. 编译启动

```bash
mvn clean package
java -jar openstudy-admin/target/openstudy-admin.jar
```

#### 5. 访问服务

- **API 文档**: http://localhost:8086/swagger-ui.html
- **后端端口**: 8086

---

## 🎯 核心功能

### 1️⃣ AI 智能出题

根据知识点、题型、难度自动生成题目，支持多种题型和组合题。

#### API 接口

```java
POST /ai/generate/questions
{
  "knowledgePoint": "勾股定理",
  "questionType": "单选题",
  "count": 5,
  "provider": "zhipuai"
}
```

#### 核心服务

- **QuestionGenService** - 普通题目生成（单选、多选、判断、填空）
- **CompositeGenService** - 组合题生成（阅读理解、完形填空）
- **PromptTemplate** - 提示词模板管理
- **QuestionParser** - AI 响应解析为结构化题目

#### 使用示例

```java
@Autowired
private AiQuestionService aiQuestionService;

// 生成 5 道勾股定理单选题
String questions = aiQuestionService.generateQuestions(
    "勾股定理",      // 知识点
    "单选题",        // 题型
    5,              // 数量
    "zhipuai"       // AI 提供商
);

// 生成阅读理解题（组合题）
String passage = "阅读以下文章...";
String compositeQuestions = aiQuestionService.generateReadingComprehension(
    passage,
    3,              // 题目数量
    "zhipuai"
);
```

---

### 2️⃣ AI 辅助刷题

智能解析答案、错题分析、个性化学习推荐。

#### API 接口

```java
// 智能聊天（带上下文）
POST /ai/chat
{
  "message": "这道题为什么选A？",
  "sessionId": "session_123",
  "provider": "zhipuai"
}

// 答案分析
POST /ai/analyze
{
  "question": "1+1=?",
  "userAnswer": "2",
  "correctAnswer": "2",
  "provider": "zhipuai"
}
```

#### 核心服务

- **AiService** - 通用聊天服务，支持多提供商切换
- **ConversationHistoryService** - 对话历史管理（上下文记忆）
- **BankContextService** - 题库上下文注入
- **UserPreferenceService** - 用户偏好学习（个性化推荐）

#### 流式输出（SSE）

```java
@GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<String> chatStream(@RequestBody ChatRequest request) {
    return aiService.chatStream(request.getMessage(), request.getProvider());
}
```

---

### 3️⃣ OCR 文字识别

支持图片题目识别、手写答案识别，集成本地和云端两种方案。

#### API 接口

```java
POST /ocr/recognize
Content-Type: multipart/form-data

file: [图片文件]
type: question  // question: 题目识别, answer: 答案识别
```

#### 核心服务

- **Tess4jOcrService** - 本地 Tesseract OCR 引擎
- **SiliconFlowOcrService** - 云端 PaddleOCR-VL 模型
- **OcrController** - OCR 统一接口

#### 使用示例

```java
@Autowired
private OcrService ocrService;

// 识别图片中的题目
MultipartFile imageFile = ...;
String recognizedText = ocrService.recognize(imageFile, "question");

// 自动切换到云端 OCR（更高精度）
String highQualityText = siliconFlowOcrService.recognize(imageFile);
```

---

### 4️⃣ RAG 知识库问答系统

基于检索增强生成（RAG）技术，构建完整的知识库管理和智能问答系统。

#### 🎯 核心功能

**1. 知识库管理**
- ✅ 创建/更新/删除知识库
- ✅ 文档数量自动统计
- ✅ 级联删除（知识库 → 文档 → 分块 → 向量）

**2. 文档管理**
- ✅ 多格式支持（PDF、DOCX、TXT、MD）
- ✅ 文件上传与校验（大小限制 3MB）
- ✅ 异步解析与分块处理
- ✅ 向量化存储（Spring AI VectorStore）
- ✅ 文档状态追踪（待解析 → 解析中 → 已分块 → 已向量化）

**3. 智能问答**
- ✅ 基于知识库的上下文检索
- ✅ 相似度搜索（Top-K）
- ✅ 问答记录保存与历史查询
- ✅ 用户反馈收集（有用/无用）

**4. 数据处理流程**
```
用户上传文档 → 文件校验 → 文本提取 → 智能分块 → 向量化 → 存入向量库
     ↓
用户提问 → 向量检索相关片段 → 构建增强Prompt → AI生成答案 → 保存记录
```

#### 📁 项目结构

```
openstudy-admin/src/main/java/com/openstudy/ai/
│
├── controller/                      # RAG 控制器层
│   ├── RagKnowledgeBaseController.java    # 知识库管理接口
│   ├── RagDocumentController.java         # 文档管理接口
│   ├── RagDocumentParserController.java   # 文档解析接口
│   ├── RagDocumentStatusController.java   # 文档状态查询
│   ├── RagQuestionController.java         # RAG 出题接口
│   ├── RagQaRecordController.java         # 问答记录接口
│   └── RagTestController.java             # 测试接口
│
├── domain/                          # 数据模型
│   ├── RagKnowledgeBase.java              # 知识库实体
│   ├── RagDocument.java                   # 文档实体
│   ├── RagDocumentChunk.java              # 文档分块实体
│   └── RagQaRecord.java                   # 问答记录实体
│
├── mapper/                          # MyBatis Mapper
│   ├── RagKnowledgeBaseMapper.java        # 知识库 Mapper
│   ├── RagDocumentMapper.java             # 文档 Mapper
│   ├── RagDocumentChunkMapper.java        # 分块 Mapper
│   └── RagQaRecordMapper.java             # 问答记录 Mapper
│
├── service/rag/                     # RAG 业务服务层
│   ├── RagDocumentUploadService.java      # 文档上传服务
│   ├── RagDocumentParserService.java      # 文档解析服务
│   ├── RagDocumentChunkService.java       # 文档分块服务
│   ├── RagVectorService.java              # 向量化服务
│   ├── RagDocumentAsyncService.java       # 异步处理服务
│   ├── RagDocumentSyncService.java        # 同步处理服务（调试用）
│   ├── RagChatService.java                # RAG 聊天服务
│   └── RagQuestionService.java            # RAG 出题服务
│
└── service/impl/                    # 服务实现层
    ├── RagKnowledgeBaseServiceImpl.java   # 知识库服务实现
    ├── RagDocumentServiceImpl.java        # 文档服务实现
    ├── RagDocumentChunkServiceImpl.java   # 分块服务实现
    └── RagQaRecordServiceImpl.java        # 问答记录服务实现

resources/mapper/rag/                # MyBatis XML 映射文件
├── RagKnowledgeBaseMapper.xml
├── RagDocumentMapper.xml
├── RagDocumentChunkMapper.xml
└── RagQaRecordMapper.xml
```

#### 🔌 API 接口文档

**1. 知识库管理**

```java
// 创建知识库
POST /rag/knowledgeBase
{
  "userId": 1,
  "name": "数学题库",
  "description": "初中数学知识点",
  "isPublic": 0
}

// 查询用户知识库列表
GET /rag/knowledgeBase/list?userId=1

// 查询知识库详情
GET /rag/knowledgeBase/{id}

// 更新知识库
PUT /rag/knowledgeBase/{id}
{
  "name": "新知识库名称",
  "description": "新的描述"
}

// 删除知识库（级联删除所有文档、分块、向量数据）
DELETE /rag/knowledgeBase/{id}
```

**2. 文档管理**

```java
// 上传文档（自动触发解析、分块、向量化）
POST /rag/document/upload
Content-Type: multipart/form-data

file: [PDF/DOCX/TXT/MD 文件]
knowledgeBaseId: 1
userId: 1

// 查询知识库下的文档列表
GET /rag/document/list/{knowledgeBaseId}

// 删除文档（同时删除分块和向量数据，更新知识库计数）
DELETE /rag/document/{documentId}

// 向量化文档
POST /rag/document/vectorize/{documentId}
```

**3. 问答记录**

```java
// 保存问答记录
POST /rag/qa/save
{
  "userId": 1,
  "knowledgeBaseId": 1,
  "question": "什么是勾股定理？",
  "answer": "勾股定理是...",
  "durationMs": 1500,
  "feedback": null  // 1-有用, 0-无用, null-未反馈
}

// 查询知识库的问答历史（最近 50 条）
GET /rag/qa/list/{knowledgeBaseId}
```

**4. RAG 智能问答**

```java
// 基于知识库的智能问答
POST /ai/rag/chat
{
  "message": "解释一下勾股定理",
  "knowledgeBaseId": 1,
  "topK": 5  // 检索最相关的 5 个片段
}

// 流式输出
GET /ai/rag/chat/stream?message=xxx&knowledgeBaseId=1
```

#### 💡 技术实现详解

**1. 文档上传与处理流程**

```java
@Service
public class RagDocumentUploadService {
    
    public Long uploadDocument(MultipartFile file, Long kbId, Long userId) {
        // 1. 文件校验（类型、大小）
        validateFile(file);  // 支持 pdf, docx, txt, md，最大 3MB
        
        // 2. 上传文件到服务器
        String filePath = FileUploadUtils.upload(baseDir, file);
        
        // 3. 创建文档记录
        RagDocument doc = createDocumentRecord(file, kbId, userId, filePath);
        documentMapper.insert(doc);
        
        // 4. 更新知识库文档数量（原子性操作）
        ragKnowledgeBaseMapper.incrementDocumentCount(kbId, 1);
        
        // 5. 异步触发解析、分块、向量化
        asyncService.asyncFullProcess(doc.getId());
        
        return doc.getId();
    }
}
```

**2. 智能分块算法**

```java
@Service
public class RagDocumentChunkService {
    
    private static final int CHUNK_SIZE = 800;      // 每块最大字符数
    private static final int OVERLAP_SIZE = 0;      // 无重叠
    private static final int MAX_CHUNKS = 100;      // 最大分块数
    
    public void chunkDocument(Long documentId) {
        // 1. 读取文档内容
        String content = readContentFromTempFile(documentId);
        
        // 2. 按段落分割
        String[] paragraphs = content.split("\n\n");
        
        // 3. 对长段落进行二次切分
        List<String> chunks = new ArrayList<>();
        for (String para : paragraphs) {
            if (para.length() <= CHUNK_SIZE) {
                chunks.add(para);
            } else {
                chunks.addAll(splitLongText(para));
            }
        }
        
        // 4. 限制最大分块数
        if (chunks.size() > MAX_CHUNKS) {
            chunks = chunks.subList(0, MAX_CHUNKS);
        }
        
        // 5. 批量保存分块
        chunkMapper.batchInsert(chunkList);
    }
}
```

**3. 向量化存储**

```java
@Service
public class RagVectorService {
    
    @Autowired
    private VectorStore vectorStore;  // Spring AI 向量存储抽象
    
    public void vectorizeDocument(Long documentId) {
        // 1. 查询所有分块
        List<RagDocumentChunk> chunks = chunkMapper.selectByDocumentId(documentId);
        
        // 2. 分批向量化（每批 3 个，避免内存溢出）
        int batchSize = 3;
        for (int i = 0; i < chunks.size(); i += batchSize) {
            List<RagDocumentChunk> batch = chunks.subList(i, Math.min(i + batchSize, chunks.size()));
            
            // 3. 转换为 Spring AI Document
            List<Document> documents = batch.stream()
                .map(chunk -> new Document(
                    chunk.getContent(),
                    Map.of(
                        "documentId", documentId,
                        "chunkIndex", chunk.getChunkIndex(),
                        "knowledgeBaseId", kbId
                    )
                ))
                .collect(Collectors.toList());
            
            // 4. 存储到向量数据库
            vectorStore.add(documents);
            
            // 5. 清理内存
            System.gc();
        }
        
        // 6. 更新文档状态为已向量化
        doc.setStatus(5);
        documentMapper.update(doc);
    }
}
```

**4. RAG 检索增强问答**

```java
@Service
public class RagChatService {
    
    @Autowired
    private VectorStore vectorStore;
    
    @Autowired
    private ChatClient chatClient;
    
    public String chatWithRag(String question, Long kbId, int topK) {
        // 1. 向量检索相关片段
        SearchRequest searchRequest = SearchRequest.builder()
            .query(question)
            .topK(topK)
            .filterExpression("knowledgeBaseId == " + kbId)
            .build();
        
        List<Document> results = vectorStore.similaritySearch(searchRequest);
        
        // 2. 构建增强 Prompt
        String context = results.stream()
            .map(Document::getContent)
            .collect(Collectors.joining("\n\n"));
        
        String prompt = String.format("""
            基于以下背景知识回答问题：
            
            %s
            
            问题：%s
            
            请给出详细、准确的回答。
            """, context, question);
        
        // 3. 调用 AI 生成答案
        return chatClient.prompt(prompt).call().content();
    }
}
```

**5. 级联删除机制**

```java
@Service
@Transactional(rollbackFor = Exception.class)
public class RagKnowledgeBaseServiceImpl {
    
    public int deleteById(Long id) {
        // 1. 查询知识库下所有文档
        List<RagDocument> documents = documentMapper.selectByKnowledgeBaseId(id);
        
        // 2. 逐个删除文档及其关联数据
        for (RagDocument doc : documents) {
            // 删除分块
            chunkService.deleteByDocumentId(doc.getId());
            
            // TODO: 删除向量库中的数据
            // vectorService.deleteByDocumentId(doc.getId());
            
            // 删除文档记录
            documentMapper.deleteById(doc.getId());
        }
        
        // 3. 删除知识库本身
        return knowledgeBaseMapper.deleteById(id);
    }
}
```

**6. 文档数量自动统计**

```xml
<!-- RagKnowledgeBaseMapper.xml -->
<update id="incrementDocumentCount">
    UPDATE rag_knowledge_base 
    SET document_count = document_count + #{delta}, 
        update_time = NOW()
    WHERE id = #{id}
</update>
```

```java
// 上传文档时 +1
ragKnowledgeBaseMapper.incrementDocumentCount(kbId, 1);

// 删除文档时 -1
ragKnowledgeBaseMapper.incrementDocumentCount(kbId, -1);
```

#### 🗄️ 数据库表结构

**1. rag_knowledge_base（知识库表）**

```sql
CREATE TABLE `rag_knowledge_base` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `name` VARCHAR(255) NOT NULL COMMENT '知识库名称',
  `description` TEXT COMMENT '描述',
  `icon` VARCHAR(500) COMMENT '图标URL',
  `is_public` TINYINT DEFAULT 0 COMMENT '是否公开：0-私有，1-公开',
  `document_count` INT DEFAULT 0 COMMENT '文档数量',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='RAG知识库表';
```

**2. rag_document（文档表）**

```sql
CREATE TABLE `rag_document` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `knowledge_base_id` BIGINT NOT NULL COMMENT '知识库ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `file_name` VARCHAR(255) NOT NULL COMMENT '文件名',
  `file_type` VARCHAR(20) NOT NULL COMMENT '文件类型：pdf, docx, txt, md',
  `file_size` BIGINT COMMENT '文件大小（字节）',
  `file_path` VARCHAR(500) COMMENT '文件存储路径',
  `raw_content` TEXT COMMENT '原始内容（预览）',
  `chunk_count` INT DEFAULT 0 COMMENT '分块数量',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1-待解析，2-解析中，3-已分块，4-失败，5-已向量化',
  `error_msg` TEXT COMMENT '错误信息',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_kb_id` (`knowledge_base_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='RAG文档表';
```

**3. rag_document_chunk（文档分块表）**

```sql
CREATE TABLE `rag_document_chunk` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `document_id` BIGINT NOT NULL COMMENT '文档ID',
  `chunk_index` INT NOT NULL COMMENT '分块索引',
  `content` TEXT NOT NULL COMMENT '分块内容',
  `content_length` INT COMMENT '内容长度',
  `vector_id` VARCHAR(255) COMMENT '向量ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_document_id` (`document_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='RAG文档分块表';
```

**4. rag_qa_record（问答记录表）**

```sql
CREATE TABLE `rag_qa_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `knowledge_base_id` BIGINT NOT NULL COMMENT '知识库ID',
  `question` TEXT NOT NULL COMMENT '问题',
  `answer` TEXT COMMENT '答案',
  `duration_ms` INT DEFAULT 0 COMMENT '耗时（毫秒）',
  `feedback` TINYINT DEFAULT NULL COMMENT '用户反馈：1-有用，0-无用，NULL-未反馈',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_kb_id` (`knowledge_base_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='RAG问答记录表';
```

---

### 5️⃣ 多 AI 提供商管理

统一的 AI 提供商抽象层，支持动态切换和故障转移。

#### 架构设计

```java
// AI 提供商接口
public interface AiProvider {
    String chat(String message);
    String getName();
    boolean isAvailable();
}

// 提供商管理器
@Service
public class AiProviderManager {
    private Map<String, AiProvider> providers;
    
    public String chat(String message, String providerName) {
        AiProvider provider = getProvider(providerName);
        return provider.chat(message);
    }
}
```

#### 已集成的提供商

| 提供商 | 实现类 | 特点 |
|--------|--------|------|
| 智谱 AI | ZhipuAiProvider | 中文理解强，成本低 |
| DeepSeek | DeepSeekProvider | 推理能力强 |
| SiliconFlow | SiliconFlowProvider | OCR 专用 |

#### 配置切换

```yaml
ai:
  default-provider: zhipuai      # 默认提供商
  fallback-provider: deepseek    # 故障转移
```

```java
// 运行时切换
GET /ai/switch-model?provider=deepseek
```

---

## 🧠 AI 基础设施详解

### Spring AI 集成架构

本项目基于 **Spring AI 1.0.0-M5** 构建，提供统一的 AI 抽象层：

```
┌─────────────────────────────────────┐
│      Application Layer (业务层)       │
│  AiController, AiQuestionService     │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│   Spring AI Abstraction (抽象层)     │
│  ChatClient, ChatModel, Prompt       │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│   Provider Adapters (适配器层)       │
│  ZhiPuAiChatModel, OpenAiChatModel   │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│   AI Providers (AI 提供商)           │
│  智谱AI, DeepSeek, SiliconFlow       │
└─────────────────────────────────────┘
```

### 核心组件

#### 1. ChatClient 统一调用

```java
@Autowired
private ChatClient chatClient;

// 简单调用
String response = chatClient.prompt()
    .user("什么是勾股定理？")
    .call()
    .content();

// 带系统提示词
String response = chatClient.prompt()
    .system("你是一位数学老师")
    .user("解释勾股定理")
    .call()
    .content();

// 流式输出
Flux<String> stream = chatClient.prompt()
    .user("生成长文")
    .stream()
    .content();
```

#### 2. Prompt 模板管理

```java
@Component
public class PromptTemplate {
    
    public static final String SYSTEM_PROMPT = """
        你是一位专业的教育助手，擅长出题和解答问题。
        请按照以下格式输出题目：
        
        题目1：[题干]
        A. [选项A]
        B. [选项B]
        C. [选项C]
        D. [选项D]
        答案：[正确答案]
        解析：[详细解析]
        """;
    
    public String buildQuestionPrompt(String knowledgePoint, String type, int count) {
        return String.format("""
            请生成 %d 道关于「%s」的%s。
            要求：
            1. 难度适中，适合中学生
            2. 题目清晰，无歧义
            3. 解析详细，易于理解
            """, count, knowledgePoint, type);
    }
}
```

#### 3. 对话历史管理

```java
@Service
public class ConversationHistoryService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    // 保存消息
    public void saveMessage(Long userId, String sessionId, ConversationMessage message) {
        String key = String.format("conversation:%d:%s", userId, sessionId);
        redisTemplate.opsForList().rightPush(key, message);
        redisTemplate.expire(key, 24, TimeUnit.HOURS);
    }
    
    // 构建上下文 Prompt
    public String buildContextPrompt(Long userId, String sessionId) {
        List<ConversationMessage> history = getHistory(userId, sessionId, 10);
        return history.stream()
            .map(msg -> msg.getRole() + ": " + msg.getContent())
            .collect(Collectors.joining("\n"));
    }
}
```

#### 4. RAG 检索增强

```java
@Service
public class RagChatService {
    
    @Autowired
    private VectorStore vectorStore;  // 向量数据库
    
    @Autowired
    private ChatClient chatClient;
    
    public String chatWithRag(String question) {
        // 1. 向量检索相关文档
        List<Document> docs = vectorStore.similaritySearch(question);
        
        // 2. 构建增强 Prompt
        String context = docs.stream()
            .map(Document::getContent)
            .collect(Collectors.joining("\n"));
        
        String prompt = String.format("""
            基于以下背景知识回答问题：
            
            %s
            
            问题：%s
            """, context, question);
        
        // 3. 调用 AI 生成答案
        return chatClient.prompt(prompt).call().content();
    }
}
```

---

## 📊 API 文档

项目集成了 **SpringDoc OpenAPI**，启动后访问：

- **Swagger UI**: http://localhost:8086/swagger-ui.html
- **API Docs**: http://localhost:8086/v3/api-docs

### 主要接口分组

| 分组 | 路径 | 说明 |
|------|------|------|
| AI 聊天 | `/ai/chat*` | 智能对话、上下文记忆 |
| AI 出题 | `/ai/generate/*` | 题目生成、试卷生成 |
| AI 分析 | `/ai/analyze` | 答案解析、错题分析 |
| OCR 识别 | `/ocr/*` | 图片文字识别 |
| 模型管理 | `/ai/models*` | 模型列表、切换 |
| RAG 问答 | `/ai/rag/*` | 知识库问答 |

---

## 🔧 开发指南

### 扩展新的 AI 提供商

#### 步骤 1: 实现 AiProvider 接口

```java
@Component
public class YourAiProvider implements AiProvider {
    
    @Value("${your.ai.api-key}")
    private String apiKey;
    
    @Override
    public String chat(String message) {
        // 调用你的 AI API
        return callYourApi(message);
    }
    
    @Override
    public String getName() {
        return "your-ai";
    }
    
    @Override
    public boolean isAvailable() {
        return StringUtils.hasText(apiKey);
    }
}
```

#### 步骤 2: 注册到 ProviderManager

```java
@Configuration
public class AiProviderConfig {
    
    @Bean
    public AiProviderManager providerManager(
            List<AiProvider> providers) {
        return new AiProviderManager(providers);
    }
}
```

#### 步骤 3: 配置文件添加

```yaml
spring:
  ai:
    your-ai:
      api-key: your_api_key
      base-url: https://api.your-ai.com
```

---

### 自定义 Prompt 模板

```java
@Component
public class CustomPromptTemplate {
    
    public String buildMathProblemPrompt(String topic, int difficulty) {
        return switch (difficulty) {
            case 1 -> String.format("生成一道简单的%s题目", topic);
            case 2 -> String.format("生成一道中等的%s题目，需要多步推理", topic);
            case 3 -> String.format("生成一道困难的%s题目，考察综合能力", topic);
            default -> throw new IllegalArgumentException("无效难度");
        };
    }
}
```

---

### 集成向量数据库（RAG）

本项目支持多种向量数据库：

```yaml
spring:
  ai:
    vectorstore:
      # 选项 1: Chroma
      chroma:
        client:
          host: localhost
          port: 8000
      
      # 选项 2: Milvus
      milvus:
        client:
          host: localhost
          port: 19530
      
      # 选项 3: Redis
      redis:
        index-name: documents
```

```java
@Service
public class DocumentIngestionService {
    
    @Autowired
    private VectorStore vectorStore;
    
    public void ingestDocument(String text, Map<String, Object> metadata) {
        Document doc = Document.builder()
            .content(text)
            .metadata(metadata)
            .build();
        
        vectorStore.add(List.of(doc));
    }
}
```

---

## 🐛 常见问题

### 1. Spring AI 客户端未初始化

**问题**: `ChatClient` 或 `ZhiPuAiChatModel` 为 null

**解决**:
```yaml
# 检查配置是否正确
spring:
  ai:
    zhipuai:
      api-key: your_api_key  # 确保填写了正确的 API Key
      chat:
        enabled: true         # 确保启用
```

### 2. AI 接口调用超时

**解决**:
```yaml
spring:
  ai:
    zhipuai:
      chat:
        options:
          timeout: 30s  # 增加超时时间
```

### 3. OCR 识别精度低

**解决**:
- 使用云端 SiliconFlow OCR（PaddleOCR-VL 模型）
- 提高图片分辨率和清晰度
- 预处理图片（二值化、去噪）

```java
// 切换到云端 OCR
@Autowired
private SiliconFlowOcrService siliconFlowOcr;

String result = siliconFlowOcr.recognize(imageFile);
```

### 4. 对话上下文丢失

**解决**:
- 检查 Redis 连接是否正常
- 确认 sessionId 是否正确传递
- 调整历史消息数量（默认保留 10 条）

```java
// 增加上下文长度
historyService.getHistory(userId, sessionId, 20);  // 改为 20 条
```

### 5. 流式输出不工作

**解决**:
```java
// 确保返回类型为 Flux
@GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<String> stream() {
    return aiService.chatStream(message, provider);
}
```

前端需要使用 EventSource 接收：
```javascript
const eventSource = new EventSource('/ai/chat/stream');
eventSource.onmessage = (event) => {
    console.log(event.data);
};
```

---

## 📝 更新日志

### v3.9.2 (2026-04-24)

**RAG 知识库系统增强**
- ✨ 完整的知识库管理功能（创建/更新/删除/查询）
- ✨ 文档上传与多格式支持（PDF、DOCX、TXT、MD）
- ✨ 智能分块算法（按段落分割，最大 800 字符/块）
- ✨ 向量化存储（Spring AI VectorStore 集成）
- ✨ 文档数量自动统计（原子性增量更新）
- ✨ 级联删除机制（知识库 → 文档 → 分块 → 向量）
- ✨ 问答记录保存与历史查询
- ✨ 用户反馈收集（有用/无用）
- ✨ 异步处理支持（解析、分块、向量化）
- ✨ 文档状态追踪（5 种状态流转）

**API 接口新增**
- ✨ `POST /rag/knowledgeBase` - 创建知识库
- ✨ `PUT /rag/knowledgeBase/{id}` - 更新知识库
- ✨ `DELETE /rag/knowledgeBase/{id}` - 删除知识库（级联）
- ✨ `GET /rag/document/list/{kbId}` - 查询文档列表
- ✨ `DELETE /rag/document/{id}` - 删除文档
- ✨ `POST /rag/qa/save` - 保存问答记录
- ✨ `GET /rag/qa/list/{kbId}` - 查询问答历史

**技术优化**
- ✨ 文件上传校验（类型、大小限制 3MB）
- ✨ 批量插入分块（提高性能）
- ✨ 分批向量化（每批 3 个，避免内存溢出）
- ✨ 事务管理（保证数据一致性）
- ✨ GC 优化（向量化后主动清理内存）

### v3.9.2 (2026-04-22)

**AI 核心功能**
- ✨ 集成 Spring AI 1.0.0-M5 框架
- ✨ 实现多 AI 提供商管理（智谱、DeepSeek、SiliconFlow）
- ✨ AI 智能出题服务（普通题 + 组合题）
- ✨ AI 辅助刷题（答案分析、错题解析）
- ✨ OCR 文字识别（本地 Tess4J + 云端 PaddleOCR）
- ✨ RAG 知识库问答系统
- ✨ 对话历史管理（Redis 持久化）
- ✨ 流式输出支持（SSE）
- ✨ Prompt 模板管理系统

**基础设施**
- ✨ AI 提供商抽象层设计
- ✨ 故障转移机制（主备切换）
- ✨ 用户偏好学习服务
- ✨ 生成历史记录追踪

---

## 🎯 路线图

### 短期计划（v3.9.3）

- [ ] 支持更多题型（作文题、编程题）
- [ ] 题目质量评分系统
- [ ] 个性化学习路径推荐
- [ ] 向量数据库集成（Milvus/Chroma）

### 中期计划（v3.10.0）

- [ ] 语音识别与合成（STT/TTS）
- [ ] 手写公式识别
- [ ] 知识图谱构建
- [ ] 学习数据分析看板

### 长期愿景

- [ ] 多模态学习（图文音视）
- [ ] 自适应学习系统
- [ ] 智能辅导老师 Agent
- [ ] 开放 API 平台

---




---

## 二、Python 增强服务功能清单

### 2.1 功能总览

| 序号 | 功能模块 | 优先级 | 状态 | 说明 |
|------|---------|--------|------|------|
| 1 | 题库采集（爬虫） | P0 | 待开发 | 从公开网站爬取题目，批量导入 |
| 2 | 复杂文档解析 | P0 | 待开发 | PDF/Word/PPT 解析为纯文本 |
| 3 | OCR 图片预处理 | P1 | 待开发 | 提升图片识别准确率 |
| 4 | 题目数据清洗 | P1 | 待开发 | 标准化题目格式 |
| 5 | 学习数据分析 | P2 | 待开发 | 生成学习报告和可视化图表 |

### 2.2 功能一：题库采集（爬虫）

#### 业务场景
- 老师需要快速导入大量公开题目
- 系统需要定期更新题库资源

#### 技术实现

```python
# 爬虫服务核心接口
POST /crawler/fetch
{
    "source": "baidu_wenku",     # 数据来源
    "url": "https://xxx.com",    # 目标地址
    "config": {
        "max_pages": 10,
        "delay": 1,
        "use_proxy": false
    }
}

# 响应
{
    "code": 200,
    "data": {
        "task_id": "crawler_001",
        "total_fetched": 50,
        "valid_count": 48,
        "saved_count": 45
    }
}

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

---

## 📄 开源协议

本项目基于 [MIT License](LICENSE) 开源协议。

---

## 🙏 致谢

- [Spring AI](https://spring.io/projects/spring-ai) - Spring 官方 AI 抽象层
- [智谱 AI](https://open.bigmodel.cn/) - GLM 大模型提供商
- [DeepSeek](https://www.deepseek.com/) - 深度求索大模型
- [SiliconFlow](https://siliconflow.cn/) - AI 模型推理平台
- [Tesseract OCR](https://github.com/tesseract-ocr/tesseract) - 开源 OCR 引擎
- [RuoYi-Vue](https://gitee.com/y_project/RuoYi-Vue) - 快速开发框架基础

---

## 📞 联系方式

- **项目地址**: [Gitee](https://gitee.com/your-repo/openstudy-server-springboot3)
- **问题反馈**: [Issues](https://gitee.com/your-repo/openstudy-server-springboot3/issues)

---

<p align="center">Made with ❤️ by OpenStudy Team</p>





