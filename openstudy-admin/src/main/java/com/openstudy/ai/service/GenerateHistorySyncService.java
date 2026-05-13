package com.openstudy.ai.service;

import com.openstudy.ai.model.GenerateRecord;
import com.openstudy.ai.util.AiRedisUtil;
import com.openstudy.system.domain.AiGenerateHistory;
import com.openstudy.system.mapper.AiGenerateHistoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenerateHistorySyncService {

    private final AiRedisUtil redisUtil;
    private final AiGenerateHistoryMapper historyMapper;

    /**
     * 每5分钟自动同步一次
     */
    @Scheduled(fixedDelay = 300000)
    public void syncGenerateHistory() {
        log.debug("开始同步出题历史...");
        // 这里需要遍历所有在线用户，简化版先空着
    }

    /**
     * 手动同步指定用户的出题历史
     */
    public int syncUserGenerateHistory(Long userId) {
        List<Object> records = redisUtil.fetchPendingGenerateRecords(userId);
        if (records.isEmpty()) return 0;
        
        List<AiGenerateHistory> histories = new ArrayList<>();
        for (Object obj : records) {
            if (obj instanceof GenerateRecord) {
                histories.add(convertToEntity((GenerateRecord) obj));
            }
        }
        
        if (!histories.isEmpty()) {
            historyMapper.batchInsert(histories);
            log.info("同步用户 {} 的出题历史 {} 条", userId, histories.size());
        }
        return histories.size();
    }

    private AiGenerateHistory convertToEntity(GenerateRecord record) {
        AiGenerateHistory entity = new AiGenerateHistory();
        entity.setUserId(record.getUserId());
        entity.setBankId(record.getBankId());
        entity.setSessionId(record.getSessionId());
        entity.setRequestType(record.getRequestType());
        entity.setQuestionType(record.getQuestionType());
        entity.setKnowledgePoint(record.getKnowledgePoint());
        entity.setRequestCount(record.getRequestCount());
        entity.setGeneratedCount(record.getGeneratedCount());
        entity.setProvider(record.getProvider());
        return entity;
    }
}