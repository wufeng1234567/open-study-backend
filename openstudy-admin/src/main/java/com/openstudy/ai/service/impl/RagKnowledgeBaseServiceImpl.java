package com.openstudy.ai.service.impl;

import com.openstudy.ai.domain.RagDocument;
import com.openstudy.ai.domain.RagKnowledgeBase;
import com.openstudy.ai.mapper.RagDocumentMapper;
import com.openstudy.ai.mapper.RagKnowledgeBaseMapper;
import com.openstudy.ai.mapper.RagQaRecordMapper;
import com.openstudy.ai.service.RagDocumentChunkService;
import com.openstudy.ai.service.RagKnowledgeBaseService;
import com.openstudy.ai.service.rag.RagVectorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * RAG 知识库服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagKnowledgeBaseServiceImpl implements RagKnowledgeBaseService {
    
    private final RagKnowledgeBaseMapper knowledgeBaseMapper;
    private final RagDocumentMapper documentMapper;
    private final RagDocumentChunkService chunkService;
    private final RagVectorService vectorService;
    private final RagQaRecordMapper qaRecordMapper;
    
    @Override
    public RagKnowledgeBase create(RagKnowledgeBase kb) {
        log.info("创建知识库: {}", kb.getName());
        kb.setDocumentCount(0);
        kb.setCreateTime(new Date());
        kb.setUpdateTime(new Date());
        knowledgeBaseMapper.insert(kb);
        log.info("知识库创建成功，ID: {}", kb.getId());
        return kb;
    }
    
    @Override
    public int update(RagKnowledgeBase kb) {
        log.info("更新知识库: {}", kb.getId());
        kb.setUpdateTime(new Date());
        int result = knowledgeBaseMapper.update(kb);
        log.info("知识库更新成功");
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(Long id) {
        log.info("删除知识库: {}", id);
        
        // 1. 先删除该知识库的所有问答记录
        int deletedQaCount = qaRecordMapper.deleteByKnowledgeBaseId(id);
        log.info("已删除知识库的问答记录: knowledgeBaseId={}, count={}", id, deletedQaCount);
        
        // 2. 查询知识库下的所有文档
        List<RagDocument> documents = documentMapper.selectByKnowledgeBaseId(id);
        
        // 3. 删除每个文档的分块和向量数据
        if (documents != null && !documents.isEmpty()) {
            log.info("知识库下有 {} 个文档，开始清理", documents.size());
            for (RagDocument doc : documents) {
                try {
                    // 1. 先删除向量库中的数据（必须在删除分块之前，因为需要查询分块的 vector_id）
                    vectorService.deleteByDocumentId(doc.getId());
                    log.info("已删除文档向量: documentId={}", doc.getId());
                    
                    // 2. 删除分块
                    chunkService.deleteByDocumentId(doc.getId());
                    log.info("已删除文档分块: documentId={}", doc.getId());
                    
                    // 3. 删除文档记录
                    documentMapper.deleteById(doc.getId());
                    log.info("已删除文档: documentId={}", doc.getId());
                } catch (Exception e) {
                    log.error("删除文档失败: documentId={}", doc.getId(), e);
                    throw new RuntimeException("删除文档失败: " + e.getMessage(), e);
                }
            }
        }
        
        // 4. 删除知识库本身
        int result = knowledgeBaseMapper.deleteById(id);
        log.info("知识库删除成功");
        return result;
    }
    
    @Override
    public RagKnowledgeBase getById(Long id) {
        log.info("查询知识库: {}", id);
        RagKnowledgeBase kb = knowledgeBaseMapper.selectById(id);
        if (kb == null) {
            log.warn("知识库不存在: {}", id);
        }
        return kb;
    }
    
    @Override
    public List<RagKnowledgeBase> listByUserId(Long userId) {
        log.info("查询用户知识库列表: {}", userId);
        List<RagKnowledgeBase> list = knowledgeBaseMapper.selectByUserId(userId);
        log.info("查询到 {} 个知识库", list.size());
        return list;
    }
}
