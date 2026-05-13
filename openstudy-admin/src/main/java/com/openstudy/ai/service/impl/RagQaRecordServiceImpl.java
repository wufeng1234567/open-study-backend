package com.openstudy.ai.service.impl;

import com.openstudy.ai.domain.RagQaRecord;
import com.openstudy.ai.mapper.RagQaRecordMapper;
import com.openstudy.ai.service.RagQaRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * RAG 问答记录服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagQaRecordServiceImpl implements RagQaRecordService {
    
    private final RagQaRecordMapper qaRecordMapper;
    
    @Override
    public RagQaRecord save(RagQaRecord record) {
        log.info("保存问答记录: knowledgeBaseId={}", record.getKnowledgeBaseId());
        record.setCreateTime(new Date());
        qaRecordMapper.insert(record);
        log.info("问答记录保存成功，ID: {}", record.getId());
        return record;
    }
    
    @Override
    public List<RagQaRecord> listByKnowledgeBaseId(Long knowledgeBaseId) {
        log.info("查询知识库问答历史: knowledgeBaseId={}", knowledgeBaseId);
        List<RagQaRecord> list = qaRecordMapper.selectByKnowledgeBaseId(knowledgeBaseId);
        log.info("查询到 {} 条问答记录", list.size());
        return list;
    }
}
