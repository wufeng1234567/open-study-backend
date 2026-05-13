package com.openstudy.ai.service;

import com.openstudy.ai.domain.RagQaRecord;
import java.util.List;

/**
 * RAG 问答记录服务接口
 */
public interface RagQaRecordService {
    
    /**
     * 保存问答记录
     */
    RagQaRecord save(RagQaRecord record);
    
    /**
     * 查询知识库的问答历史
     */
    List<RagQaRecord> listByKnowledgeBaseId(Long knowledgeBaseId);
}
