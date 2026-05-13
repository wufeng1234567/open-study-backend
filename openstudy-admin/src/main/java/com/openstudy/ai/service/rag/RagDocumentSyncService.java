package com.openstudy.ai.service.rag;

import com.openstudy.ai.domain.RagDocument;
import com.openstudy.ai.mapper.RagDocumentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * RAG 文档同步处理服务（临时方案，用于调试）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagDocumentSyncService {

    private final RagDocumentParserService parserService;
    private final RagDocumentChunkService chunkService;
    private final RagVectorService vectorService;
    private final RagDocumentMapper documentMapper;

    /**
     * 同步完整流程（解析 + 分块 + 向量化）
     * 注意：这会阻塞调用线程，仅用于调试！
     */
    public void syncFullProcess(Long documentId) {
        log.info("=== 开始同步处理文档 === documentId: {}", documentId);
        
        try {
            // 1. 更新状态为处理中
            RagDocument doc = documentMapper.selectById(documentId);
            if (doc != null) {
                doc.setStatus(2); // 解析中
                documentMapper.update(doc);
            }
            
            // 2. 解析
            log.info("步骤1/3: 解析文档");
            parserService.parseDocument(documentId);
            log.info("步骤1/3 完成");
            
            // 3. 分块
            log.info("步骤2/3: 分块文档");
            chunkService.chunkDocument(documentId);
            log.info("步骤2/3 完成");
            
            // 4. 向量化
            log.info("步骤3/3: 向量化文档");
            vectorService.vectorizeDocument(documentId);
            log.info("步骤3/3 完成");
            
            log.info("=== 文档同步处理完成 === documentId: {}", documentId);
            
        } catch (Exception e) {
            log.error("=== 同步处理失败 === documentId: {}", documentId, e);
            
            // 更新错误状态
            try {
                RagDocument doc = documentMapper.selectById(documentId);
                if (doc != null) {
                    doc.setStatus(4);
                    doc.setErrorMsg("处理失败: " + e.getMessage());
                    documentMapper.update(doc);
                }
            } catch (Exception ex) {
                log.error("更新错误状态失败", ex);
            }
            
            throw new RuntimeException("文档处理失败: " + e.getMessage(), e);
        }
    }
}
