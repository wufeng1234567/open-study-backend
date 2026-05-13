# RAG 性能优化说明

## 🔴 已修复的性能问题

### 1. PDF 解析内存泄漏 ✅
**问题**: PDFBox 默认会加载整个 PDF 到内存，大文件会导致内存爆炸
**修复**: 
- 限制 PDFBox 内存使用为 100MB
- 限制最大处理页数为 100 页
- 使用 try-with-resources 确保资源释放

### 2. 向量化内存占用过高 ✅
**问题**: 一次性将所有分块加载到内存并向量化
**修复**: 
- 改为分批处理，每批 10 个分块
- 每批处理后立即清理内存
- 添加进度日志

### 3. 数据库插入效率低 ✅
**问题**: 逐条插入分块，导致大量数据库连接和事务开销
**修复**: 
- 使用 MyBatis 批量插入（batchInsert）
- 一次性插入所有分块，减少数据库交互

---

## ⚠️ 仍需优化的问题

### 4. rawContent 存储过大（重要！）
**问题**: 
- 解析后的完整文本存储在 `rag_document.raw_content` 字段
- 如果 PDF 有 100 页，文本可能达到几 MB
- 每次查询文档都会加载这个大字段到内存
- 多个用户同时解析会导致内存飙升

**建议解决方案**:

#### 方案 A：不存储 rawContent（推荐）
```java
// 解析后直接分块，不保存完整文本
doc.setRawContent(null);  // 或者只保存前1000字符作为预览
doc.setStatus(3);
ragDocumentMapper.update(doc);
```

#### 方案 B：存储到文件系统
```java
// 将文本保存到文件
String textFilePath = filePath.replace(".pdf", ".txt");
Files.writeString(Paths.get(textFilePath), content);
doc.setRawContent(null);  // 数据库中不存储
doc.setTextFilePath(textFilePath);  // 只存文件路径
```

#### 方案 C：使用 TEXT 字段并延迟加载
在 Mapper XML 中，查询时排除 rawContent 字段：
```xml
<select id="selectById" resultType="...">
    SELECT id, knowledge_base_id, user_id, file_name, ...
    -- 不包含 raw_content
    FROM rag_document WHERE id = #{id}
</select>
```

---

## 📊 性能监控建议

### 1. 添加 JVM 参数
在启动脚本中添加：
```bash
java -Xms512m -Xmx2g -XX:+UseG1GC -jar openstudy-admin.jar
```

### 2. 监控关键指标
- 堆内存使用量
- GC 频率和时间
- 数据库连接池使用情况

### 3. 日志级别调整
生产环境将日志级别改为 WARN：
```yaml
logging:
  level:
    com.openstudy.ai: warn
```

---

## 🚀 进一步优化建议

### 1. 异步处理
将解析、分块、向量化改为异步任务：
```java
@Async
public void processDocumentAsync(Long documentId) {
    parserService.parseDocument(documentId);
    chunkService.chunkDocument(documentId);
    vectorService.vectorizeDocument(documentId);
}
```

### 2. 限流控制
防止同时处理太多文档：
```java
@SemaphoreLimit(permits = 3)  // 最多同时处理3个文档
public void parseDocument(Long documentId) {
    // ...
}
```

### 3. 缓存优化
- 缓存常用的向量检索结果
- 使用 Redis 缓存文档元数据

### 4. 分页查询
查询分块时使用分页，避免一次性加载所有分块。

---

## 📝 测试建议

### 测试场景
1. **小文件测试**: 1MB 以内的 PDF
2. **中等文件**: 5-10MB 的 PDF（50-100页）
3. **大文件测试**: 20MB+ 的 PDF（200页+）
4. **并发测试**: 同时上传 3-5 个文档

### 监控指标
- 内存峰值不超过 2GB
- CPU 使用率不超过 70%
- 单个文档处理时间 < 2分钟

---

## 🔧 紧急处理

如果已经出现内存飙升：

1. **重启服务**
```bash
# 停止服务
taskkill /F /PID <java进程ID>

# 重新启动
java -Xms512m -Xmx2g -jar openstudy-admin.jar
```

2. **清理大数据**
```sql
-- 删除大文件的 raw_content
UPDATE rag_document 
SET raw_content = NULL 
WHERE LENGTH(raw_content) > 100000;
```

3. **检查正在进行的任务**
```sql
-- 查看状态为处理中的文档
SELECT * FROM rag_document WHERE status IN (0, 1, 2);
```
