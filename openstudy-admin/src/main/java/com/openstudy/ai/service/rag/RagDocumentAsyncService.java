package com.openstudy.ai.service.rag;

import com.openstudy.ai.domain.RagDocument;
import com.openstudy.ai.mapper.RagDocumentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * RAG 文档异步处理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagDocumentAsyncService {

    private final RagDocumentParserService parserService;
    private final RagDocumentChunkService chunkService;
    private final RagVectorService vectorService;
    private final RagDocumentMapper documentMapper;

    /**
     * 异步解析文档（解析 + 分块）
     */
    @Async("taskExecutor")
    public void asyncParseAndChunk(Long documentId) {
        log.info("开始异步解析文档，documentId: {}", documentId);
        
        try {
            // 1. 更新状态为处理中
            RagDocument doc = documentMapper.selectById(documentId);
            if (doc != null) {
                doc.setStatus(2); // 解析中
                documentMapper.update(doc);
            }
            
            // 2. 解析文档
            parserService.parseDocument(documentId);
            
            // 3. 分块
            chunkService.chunkDocument(documentId);
            
            log.info("文档解析和分块完成，documentId: {}", documentId);
            
        } catch (Exception e) {
            log.error("异步解析文档失败，documentId: {}", documentId, e);
            
            // 更新错误状态
            try {
                RagDocument doc = documentMapper.selectById(documentId);
                if (doc != null) {
                    doc.setStatus(4); // 失败
                    doc.setErrorMsg("异步解析失败: " + e.getMessage());
                    documentMapper.update(doc);
                }
            } catch (Exception ex) {
                log.error("更新错误状态失败", ex);
            }
        }
    }
    
    /**
     * 异步向量化文档
     */
    @Async("taskExecutor")
    public void asyncVectorize(Long documentId) {
        log.info("开始异步向量化文档，documentId: {}", documentId);
        
        try {
            vectorService.vectorizeDocument(documentId);
            log.info("文档向量化完成，documentId: {}", documentId);
            
        } catch (Exception e) {
            log.error("异步向量化失败，documentId: {}", documentId, e);
            
            // 更新错误状态
            try {
                RagDocument doc = documentMapper.selectById(documentId);
                if (doc != null) {
                    doc.setStatus(4); // 失败
                    doc.setErrorMsg("异步向量化失败: " + e.getMessage());
                    documentMapper.update(doc);
                }
            } catch (Exception ex) {
                log.error("更新错误状态失败", ex);
            }
        }
    }
    
    /**
     * 异步完整流程（解析 + 分块 + 向量化）
     */
    @Async("taskExecutor")
    public void asyncFullProcess(Long documentId) {
        log.info("=== 异步任务开始 === documentId: {}, 线程: {}", documentId, Thread.currentThread().getName());
        
        try {
            // 1. 解析
            log.info("步骤1/3: 解析文档, documentId: {}", documentId);
            parserService.parseDocument(documentId);
            log.info("步骤1/3 完成: 解析文档成功, documentId: {}", documentId);
            
            // 2. 分块
            log.info("步骤2/3: 分块文档, documentId: {}", documentId);
            chunkService.chunkDocument(documentId);
            log.info("步骤2/3 完成: 分块文档成功, documentId: {}", documentId);
            
            // 3. 向量化
            log.info("步骤3/3: 向量化文档, documentId: {}", documentId);
            vectorService.vectorizeDocument(documentId);
            log.info("步骤3/3 完成: 向量化文档成功, documentId: {}", documentId);
            
            log.info("=== 文档完整处理完成 === documentId: {}", documentId);
            
        } catch (Exception e) {
            log.error("=== 异步完整处理失败 === documentId: {}, 错误类型: {}, 错误信息: {}", 
                    documentId, e.getClass().getName(), e.getMessage(), e);
            
            // 更新错误状态
            try {
                RagDocument doc = documentMapper.selectById(documentId);
                if (doc != null) {
                    doc.setStatus(4); // 失败
                    doc.setErrorMsg("处理失败: " + e.getMessage());
                    documentMapper.update(doc);
                    log.info("已更新文档错误状态, documentId: {}", documentId);
                }
            } catch (Exception ex) {
                log.error("更新错误状态失败, documentId: {}", documentId, ex);
            }
        }
    }
}
