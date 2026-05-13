package com.openstudy.ai.service;

import com.openstudy.ai.util.AiRedisUtil;
import com.openstudy.system.domain.UserAiPreference;
import com.openstudy.system.mapper.UserAiPreferenceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserPreferenceService {

    private final AiRedisUtil redisUtil;
    private final UserAiPreferenceMapper preferenceMapper;

    /**
     * 用户登录时加载偏好到 Redis
     */
    public void loadPreferenceOnLogin(Long userId) {
        UserAiPreference pref = preferenceMapper.selectByUserId(userId);
        if (pref != null) {
            Map<String, Object> map = new HashMap<>();
            map.put("preferSubject", pref.getPreferSubject());
            map.put("preferQuestionTypes", pref.getPreferQuestionTypes());
            map.put("preferDifficulty", pref.getPreferDifficulty());
            map.put("preferOptionsCount", pref.getPreferOptionsCount());
            map.put("preferWithAnalysis", pref.getPreferWithAnalysis());
            redisUtil.saveUserPreference(userId, map);
            log.debug("用户 {} 偏好已加载到 Redis", userId);
        }
    }

    /**
     * 获取用户偏好（优先从 Redis 取）
     */
    public Map<Object, Object> getUserPreference(Long userId) {
        Map<Object, Object> cached = redisUtil.getUserPreference(userId);
        if (cached == null || cached.isEmpty()) {
            loadPreferenceOnLogin(userId);  // 自动从MySQL加载
            cached = redisUtil.getUserPreference(userId);
        }
        return cached;
    }
}