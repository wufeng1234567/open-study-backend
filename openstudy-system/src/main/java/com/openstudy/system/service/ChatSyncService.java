package com.openstudy.system.service;

import com.openstudy.common.core.redis.RedisCache;
import com.openstudy.system.domain.ChatMessage;
import com.openstudy.system.mapper.ChatMessageMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class ChatSyncService {
    private static final Logger log = LoggerFactory.getLogger(ChatSyncService.class);

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    private static final String CHAT_PENDING_KEY = "chat:pending:";
    private static final String CHAT_MESSAGES_KEY = "chat:messages:";

    @Scheduled(fixedDelay = 300000)
    public void scheduledSync() {
        log.debug("开始定时同步聊天消息...");
        syncAllPendingMessages();
    }

    public void syncAllPendingMessages() {
        Collection<String> keys = redisCache.keys(CHAT_PENDING_KEY + "*");
        if (keys == null || keys.isEmpty()) {
            return;
        }

        for (String key : keys) {
            try {
                Long userId = extractUserIdFromKey(key);
                if (userId != null) {
                    syncUserMessages(userId);
                }
            } catch (Exception e) {
                log.error("同步用户消息失败: key={}", key, e);
            }
        }
    }

    public void syncUserMessages(Long userId) {
        String pendingKey = CHAT_PENDING_KEY + userId;

        try {
            List<ChatMessage> pending = redisCache.getCacheList(pendingKey);

            if (pending == null || pending.isEmpty()) {
                return;
            }

            List<ChatMessage> validMessages = new ArrayList<>();
            for (ChatMessage msg : pending) {
                if (msg != null && msg.getId() != null && msg.getContent() != null) {
                    validMessages.add(msg);
                }
            }

            if (!validMessages.isEmpty()) {
                chatMessageMapper.batchInsertChatMessage(validMessages);
                log.info("同步用户 {} 的 {} 条聊天消息到数据库", userId, validMessages.size());
            }

            redisCache.deleteObject(pendingKey);
        } catch (Exception e) {
            log.warn("同步用户 {} 消息失败或无需同步", userId, e);
            redisCache.deleteObject(pendingKey);
        }
    }

    public void syncOnShutdown() {
        log.info("系统关闭，开始同步所有聊天消息到数据库...");
        syncAllPendingMessages();
    }

    private Long extractUserIdFromKey(String key) {
        if (key == null || !key.startsWith(CHAT_PENDING_KEY)) {
            return null;
        }
        try {
            return Long.parseLong(key.substring(CHAT_PENDING_KEY.length()));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
