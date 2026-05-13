package com.openstudy.ai.service.impl;

import com.openstudy.ai.domain.RagDocument;
import com.openstudy.ai.mapper.RagDocumentMapper;
import com.openstudy.ai.mapper.RagKnowledgeBaseMapper;
import com.openstudy.ai.service.RagDocumentChunkService;
import com.openstudy.ai.service.RagDocumentService;
import com.openstudy.ai.service.rag.RagVectorService;
import com.openstudy.notes.domain.Note;
import com.openstudy.notes.mapper.NoteMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

/**
 * RAG 文档服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagDocumentServiceImpl implements RagDocumentService {
    
    private final RagDocumentMapper documentMapper;
    private final RagKnowledgeBaseMapper ragKnowledgeBaseMapper;
    private final RagDocumentChunkService chunkService;
    private final RagVectorService vectorService;
    private final NoteMapper noteMapper;
    private final com.openstudy.ai.service.rag.RagDocumentSyncService syncService;
    private final JdbcTemplate jdbcTemplate;
    
    @Override
    public RagDocument save(RagDocument doc) {
        log.info("保存文档: {}", doc.getFileName());
        doc.setStatus(0); // 0-处理中
        doc.setChunkCount(0);
        doc.setCreateTime(new Date());
        doc.setUpdateTime(new Date());
        documentMapper.insert(doc);
        log.info("文档保存成功，ID: {}", doc.getId());
        return doc;
    }
    
    @Override
    public int update(RagDocument doc) {
        log.info("更新文档: {}", doc.getId());
        doc.setUpdateTime(new Date());
        int result = documentMapper.update(doc);
        log.info("文档更新成功");
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(Long id) {
        log.info("删除文档: {}", id);
        
        // 先查询文档信息，获取知识库ID
        RagDocument document = documentMapper.selectById(id);
        if (document == null) {
            log.warn("文档不存在: {}", id);
            return 0;
        }
        
        Long knowledgeBaseId = document.getKnowledgeBaseId();
        
        // 1. 先删除向量库中的数据（必须在删除分块之前，因为需要查询分块的 vector_id）
        try {
            vectorService.deleteByDocumentId(id);
            log.info("已删除文档向量: documentId={}", id);
        } catch (Exception e) {
            log.error("删除向量失败，但不影响文档删除: documentId={}", id, e);
        }
        
        // 2. 删除分块
        chunkService.deleteByDocumentId(id);
        log.info("已删除文档分块: documentId={}", id);
        
        // 3. 删除文档记录
        int result = documentMapper.deleteById(id);
        
        // 4. 更新知识库文档数量（减少1）
        if (result > 0 && knowledgeBaseId != null) {
            ragKnowledgeBaseMapper.incrementDocumentCount(knowledgeBaseId, -1);
            log.info("知识库文档数量已更新，kbId: {}, delta: -1", knowledgeBaseId);
        }
        
        log.info("文档删除成功");
        return result;
    }
    
    @Override
    public RagDocument getById(Long id) {
        log.info("查询文档: {}", id);
        RagDocument doc = documentMapper.selectById(id);
        if (doc == null) {
            log.warn("文档不存在: {}", id);
        }
        return doc;
    }
    
    @Override
    public List<RagDocument> listByKnowledgeBaseId(Long kbId) {
        log.info("查询知识库文档列表: {}", kbId);
        List<RagDocument> list = documentMapper.selectByKnowledgeBaseId(kbId);
        log.info("查询到 {} 个文档", list.size());
        return list;
    }
    
    @Override
    public int updateStatus(Long id, Integer status, String errorMsg) {
        log.info("更新文档状态: {}, 状态: {}", id, status);
        RagDocument doc = new RagDocument();
        doc.setId(id);
        doc.setStatus(status);
        doc.setErrorMsg(errorMsg);
        doc.setUpdateTime(new Date());
        int result = documentMapper.update(doc);
        log.info("文档状态更新成功");
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long importFromNote(Long noteId, Long knowledgeBaseId) {
        log.info("开始从笔记导入知识库: noteId={}, kbId={}", noteId, knowledgeBaseId);
        
        try {
            // 1. 查询笔记
            Note note = noteMapper.selectNoteById(noteId);
            if (note == null) {
                throw new RuntimeException("笔记不存在: " + noteId);
            }
            
            String title = note.getTitle();
            String markdownContent = note.getMarkdownContent();
            
            if (markdownContent == null || markdownContent.isEmpty()) {
                throw new RuntimeException("笔记内容为空");
            }
            
            // 2. 先创建临时文件，获取路径
            // 注意：这里先不插入数据库，因为需要 documentId 来命名文件
            // 所以先用一个临时ID，插入后再重命名文件
            String tempDir = "D:/ruoyi/uploadPath/rag/temp/";
            Files.createDirectories(Paths.get(tempDir));
            
            // 3. 先插入文档记录获取ID（filePath 先设为占位符）
            RagDocument document = new RagDocument();
            document.setKnowledgeBaseId(knowledgeBaseId);
            document.setUserId(note.getUserId());
            document.setFileName(title + ".md");
            document.setFileType("md");
            document.setFileSize((long) markdownContent.getBytes().length);
            document.setStatus(1); // 1-待处理
            document.setChunkCount(0);
            document.setErrorMsg("");
            document.setCreateTime(new Date());
            document.setUpdateTime(new Date());
            
            // 先插入获取ID
            documentMapper.insert(document);
            Long documentId = document.getId();
            log.info("文档记录创建成功，ID: {}", documentId);
            
            // 4. 将 markdown 内容保存到临时文件（使用真实的 documentId）
            String tempFilePath = tempDir + "doc_" + documentId + ".md";
            Files.writeString(Paths.get(tempFilePath), markdownContent);
            log.info("Markdown 内容已保存到临时文件: {}", tempFilePath);
            
            // 5. 将 tempFilePath 更新到数据库（mapper 的 update 不含 filePath）
            jdbcTemplate.update("UPDATE rag_document SET file_path = ? WHERE id = ?", tempFilePath, documentId);
            log.info("filePath 已更新到数据库: {}", tempFilePath);
            
            // 6. 更新知识库文档数量
            ragKnowledgeBaseMapper.incrementDocumentCount(knowledgeBaseId, 1);
            log.info("知识库文档数量已更新，kbId: {}", knowledgeBaseId);
            
            // 7. 同步处理（解析 + 分块 + 向量化）
            log.info("开始同步处理文档: documentId={}", documentId);
            syncService.syncFullProcess(documentId);
            
            log.info("笔记导入知识库成功: documentId={}", documentId);
            return documentId;
            
        } catch (Exception e) {
            log.error("笔记导入知识库失败", e);
            throw new RuntimeException("导入失败: " + e.getMessage(), e);
        }
    }
}
