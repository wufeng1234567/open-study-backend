package com.openstudy.ai.service;

import com.openstudy.ai.model.GenerateRecord;
import com.openstudy.ai.util.AiRedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenerateHistoryService {

    private final AiRedisUtil redisUtil;

    /**
     * 记录出题行为（暂存到 Redis）
     */
    public void recordGenerate(GenerateRecord record) {
        record.setTimestamp(System.currentTimeMillis());
        redisUtil.saveGenerateRecord(record.getUserId(), record);
        log.debug("记录出题历史: userId={}, type={}, count={}", 
                record.getUserId(), record.getRequestType(), record.getGeneratedCount());
    }

    /**
     * 获取待同步的记录（用于定时任务）
     */
    public List<Object> fetchPendingRecords(Long userId) {
        return redisUtil.fetchPendingGenerateRecords(userId);
    }
}