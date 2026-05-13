package com.openstudy.ai.util;

import com.openstudy.ai.model.GenerateRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class AiRedisUtil {

    private final RedisTemplate<Object, Object> redisTemplate;

    private static final String GENERATE_PENDING_KEY = "ai:pending:generate:user:";

    // ========== List 操作（对话历史、出题暂存） ==========
    
    public void listRightPush(String key, Object value) {
        redisTemplate.opsForList().rightPush(key, value);
    }

    public List<Object> listRange(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    public void listTrim(String key, long start, long end) {
        redisTemplate.opsForList().trim(key, start, end);
    }

    // ========== Hash 操作（用户偏好、题库上下文） ==========
    
    public void hashPutAll(String key, Map<String, Object> map) {
        redisTemplate.opsForHash().putAll(key, map);
    }

    public Map<Object, Object> hashGetAll(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    // ========== 通用操作 ==========
    
    public void expire(String key, long timeout, TimeUnit unit) {
        redisTemplate.expire(key, timeout, unit);
    }

    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 暂存出题记录
     */
    public void saveGenerateRecord(Long userId, GenerateRecord record) {
        String key = GENERATE_PENDING_KEY + userId;
        listRightPush(key, record);
    }

    /**
     * 获取并清空待同步的出题记录
     */
    public List<Object> fetchPendingGenerateRecords(Long userId) {
        String key = GENERATE_PENDING_KEY + userId;
        List<Object> records = listRange(key, 0, -1);
        delete(key);
        return records;
    }


    // ========== 用户偏好缓存 ==========

    private static final String PREFERENCE_KEY = "ai:pref:user:";

    public void saveUserPreference(Long userId, Map<String, Object> preference) {
        hashPutAll(PREFERENCE_KEY + userId, preference);
        expire(PREFERENCE_KEY + userId, 1, TimeUnit.HOURS);
    }

    public Map<Object, Object> getUserPreference(Long userId) {
        return hashGetAll(PREFERENCE_KEY + userId);
    }
}