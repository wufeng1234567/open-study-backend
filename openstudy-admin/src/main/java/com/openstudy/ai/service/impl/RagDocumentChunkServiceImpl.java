package com.openstudy.ai.service.impl;

import com.openstudy.ai.domain.RagDocumentChunk;
import com.openstudy.ai.mapper.RagDocumentChunkMapper;
import com.openstudy.ai.service.RagDocumentChunkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * RAG 文档分片服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagDocumentChunkServiceImpl implements RagDocumentChunkService {
    
    private final RagDocumentChunkMapper chunkMapper;
    
    @Override
    public int saveBatch(List<RagDocumentChunk> chunks) {
        if (chunks == null || chunks.isEmpty()) {
            log.warn("分片列表为空，跳过保存");
            return 0;
        }
        
        log.info("批量保存文档分片，数量: {}", chunks.size());
        int count = 0;
        for (RagDocumentChunk chunk : chunks) {
            chunk.setCreateTime(new Date());
            chunk.setContentLength(chunk.getContent() != null ? chunk.getContent().length() : 0);
            count += chunkMapper.insert(chunk);
        }
        log.info("分片批量保存成功，共 {} 条", count);
        return count;
    }
    
    @Override
    public int deleteByDocumentId(Long documentId) {
        log.info("删除文档分片: {}", documentId);
        int result = chunkMapper.deleteByDocumentId(documentId);
        log.info("删除了 {} 个分片", result);
        return result;
    }
    
    @Override
    public List<RagDocumentChunk> listByDocumentId(Long documentId) {
        log.info("查询文档分片列表: {}", documentId);
        List<RagDocumentChunk> list = chunkMapper.selectByDocumentId(documentId);
        log.info("查询到 {} 个分片", list.size());
        return list;
    }
}
