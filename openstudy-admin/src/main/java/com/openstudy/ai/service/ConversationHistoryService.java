package com.openstudy.ai.service;

import com.openstudy.ai.model.ConversationMessage;
import com.openstudy.ai.util.AiRedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationHistoryService {

    private final AiRedisUtil redisUtil;

    private static final String KEY_PREFIX = "ai:conv:";
    private static final int MAX_HISTORY_SIZE = 20;      // 最多保留20条
    private static final int EXPIRE_MINUTES = 30;        // 30分钟过期

    /**
     * 保存一条对话消息
     */
    public void saveMessage(Long userId, String sessionId, ConversationMessage message) {
        String key = buildKey(userId, sessionId);
        message.setTimestamp(System.currentTimeMillis());
        
        redisUtil.listRightPush(key, message);
        redisUtil.listTrim(key, -MAX_HISTORY_SIZE, -1);  // 只保留最近20条
        redisUtil.expire(key, EXPIRE_MINUTES, TimeUnit.MINUTES);
        
        log.debug("保存对话消息: userId={}, role={}", userId, message.getRole());
    }

    /**
     * 获取最近N条对话历史
     */
    public List<ConversationMessage> getRecentMessages(Long userId, String sessionId, int count) {
        String key = buildKey(userId, sessionId);
        List<Object> objects = redisUtil.listRange(key, -count, -1);
        
        List<ConversationMessage> messages = new ArrayList<>();
        for (Object obj : objects) {
            if (obj instanceof ConversationMessage) {
                messages.add((ConversationMessage) obj);
            }
        }
        return messages;
    }

    /**
     * 构建上下文提示词（把历史对话拼接成字符串）
     */
    public String buildContextPrompt(Long userId, String sessionId) {
        List<ConversationMessage> history = getRecentMessages(userId, sessionId, 10);
        
        if (history.isEmpty()) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("【对话历史】\n");
        for (ConversationMessage msg : history) {
            String roleName = "assistant".equals(msg.getRole()) ? "AI" : "用户";
            sb.append(roleName).append("：").append(msg.getContent()).append("\n");
        }
        sb.append("---\n");
        return sb.toString();
    }

    /**
     * 清空会话历史
     */
    public void clearHistory(Long userId, String sessionId) {
        redisUtil.delete(buildKey(userId, sessionId));
    }

    private String buildKey(Long userId, String sessionId) {
        return KEY_PREFIX + "user:" + userId + ":session:" + sessionId;
    }

    /**
     * 构建出题用的历史上下文（精简版）
     */
    public String buildQuestionContext(Long userId, Long bankId) {
        if (bankId == null) return "";

        String sessionId = "bank_" + bankId;
        List<ConversationMessage> history = getRecentMessages(userId, sessionId, 10);

        if (history.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        sb.append("【出题历史上下文】\n");
        for (ConversationMessage msg : history) {
            String roleName = "assistant".equals(msg.getRole()) ? "AI" : "用户";
            sb.append(roleName).append("：").append(msg.getContent()).append("\n");
        }
        sb.append("---\n");
        return sb.toString();
    }
}