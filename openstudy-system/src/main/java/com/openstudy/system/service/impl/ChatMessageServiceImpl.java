package com.openstudy.system.service.impl;

import com.openstudy.common.core.redis.RedisCache;
import com.openstudy.system.domain.ChatConversation;
import com.openstudy.system.domain.ChatMessage;
import com.openstudy.system.event.ChatMessageEvent;
import com.openstudy.system.mapper.ChatConversationMapper;
import com.openstudy.system.mapper.ChatMessageMapper;
import com.openstudy.system.service.IChatConversationService;
import com.openstudy.system.service.IChatMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ChatMessageServiceImpl implements IChatMessageService {
    private static final Logger log = LoggerFactory.getLogger(ChatMessageServiceImpl.class);

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private ChatConversationMapper chatConversationMapper;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    private static final String CHAT_MESSAGES_KEY = "chat:messages:";
    private static final String CHAT_CONVERSATIONS_KEY = "chat:conversations:";
    private static final String CHAT_PENDING_KEY = "chat:pending:";
    private static final String CHAT_UNREAD_KEY = "chat:unread:";
    private static final int MAX_CACHED_MESSAGES = 100;
    private static final int EXPIRE_DAYS = 30;

    @Override
    public ChatMessage sendMessage(ChatMessage message) {
        message.setIsRead(false);
        message.setIsDeleted(0);
        message.setCreateTime(new Date());

        String cacheKey = buildMessagesCacheKey(message.getSenderId(), message.getReceiverId());
        long tempId = System.currentTimeMillis();
        long oldTempId = -tempId;

        try {
            ChatConversation senderConv = new ChatConversation();
            senderConv.setUserId(message.getSenderId());
            senderConv.setOtherUserId(message.getReceiverId());
            chatConversationMapper.insert(senderConv);

            ChatConversation receiverConv = new ChatConversation();
            receiverConv.setUserId(message.getReceiverId());
            receiverConv.setOtherUserId(message.getSenderId());
            chatConversationMapper.insert(receiverConv);
        } catch (Exception e) {
            log.debug("会话记录已存在或创建失败: {}", e.getMessage());
        }

        try {
            message.setId(null);
            message.setCreateTime(new Date());
            chatMessageMapper.insertChatMessage(message);

            ChatMessage savedMsg = chatMessageMapper.findMessageByUniqueKey(
                    message.getSenderId(), message.getReceiverId(),
                    message.getContent());

            if (savedMsg != null && savedMsg.getId() != null) {
                message.setId(savedMsg.getId());
                log.info("消息已保存到数据库: id={}, sender={}, receiver={}", message.getId(), message.getSenderId(),
                        message.getReceiverId());
            } else {
                message.setId(oldTempId);
                log.warn("无法获取消息ID，使用临时ID: {}", oldTempId);
            }
            redisCache.setCacheObject(cacheKey + message.getId(), message);

            String senderConvKey = CHAT_CONVERSATIONS_KEY + message.getSenderId();
            redisCache.deleteObject(senderConvKey);
            String receiverConvKey = CHAT_CONVERSATIONS_KEY + message.getReceiverId();
            redisCache.deleteObject(receiverConvKey);
        } catch (Exception e) {
            log.error("保存消息到数据库失败: {}", e.getMessage(), e);
            message.setId(oldTempId);
            redisCache.setCacheObject(cacheKey + message.getId(), message);
        }

        String pendingKey = CHAT_PENDING_KEY + message.getSenderId();
        try {
            List<ChatMessage> pending = redisCache.getCacheList(pendingKey);
            if (pending == null) {
                pending = new ArrayList<>();
            }
            pending.add(message);
            redisCache.deleteObject(pendingKey);
            if (!pending.isEmpty()) {
                redisCache.setCacheList(pendingKey, pending);
            }
        } catch (Exception e) {
            log.warn("缓存pending消息失败，使用直接发送", e);
        }

        updateUnreadCount(message.getReceiverId());

        applicationEventPublisher.publishEvent(new ChatMessageEvent(message));

        log.info("发送消息: from={} to={}", message.getSenderId(), message.getReceiverId());
        return message;
    }

    @Override
    public List<ChatMessage> getConversation(Long userId1, Long userId2, int page, int size) {
        String cacheKey = buildMessagesCacheKey(userId1, userId2);

        Collection<String> keys = redisCache.keys(cacheKey + "*");
        List<ChatMessage> cachedMessages = new ArrayList<>();
        if (keys != null && !keys.isEmpty()) {
            for (String key : keys) {
                ChatMessage m = redisCache.getCacheObject(key);
                if (m != null && m instanceof ChatMessage) {
                    cachedMessages.add(m);
                }
            }
            if (!cachedMessages.isEmpty()) {
                cachedMessages.sort((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()));
                cachedMessages = cachedMessages.stream()
                        .filter(m -> m.getIsDeleted() == null || m.getIsDeleted() == 0)
                        .collect(Collectors.toList());
                int start = (page - 1) * size;
                int end = Math.min(start + size, cachedMessages.size());
                if (start < cachedMessages.size()) {
                    return cachedMessages.subList(start, end);
                }
            }
        }

        List<ChatMessage> messages = chatMessageMapper.selectChatMessagesByConversation(
                userId1, userId2, userId1, size, (page - 1) * size);

        messages.forEach(m -> redisCache.setCacheObject(cacheKey + m.getId(), m));
        redisCache.expire(cacheKey, EXPIRE_DAYS, TimeUnit.DAYS);

        return messages;
    }

    @Override
    public List<ChatMessage> getRecentConversations(Long userId, int limit) {
        String cacheKey = CHAT_CONVERSATIONS_KEY + userId;

        redisCache.deleteObject(cacheKey);

        List<ChatConversation> userConvs = chatConversationMapper.selectByUserId(userId);

        List<ChatMessage> conversations = new ArrayList<>();
        for (ChatConversation conv : userConvs) {
            List<ChatMessage> msgs = chatMessageMapper.selectLastMessageBetweenUsers(conv.getOtherUserId(), userId, 1);
            if (!msgs.isEmpty()) {
                conversations.add(msgs.get(0));
            }
        }

        conversations.sort((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()));

        if (conversations.size() > limit) {
            conversations = conversations.subList(0, limit);
        }

        if (!conversations.isEmpty()) {
            redisCache.setCacheObject(cacheKey, conversations);
            redisCache.expire(cacheKey, EXPIRE_DAYS, TimeUnit.DAYS);
        }

        return conversations;
    }

    @Override
    public void markAsRead(Long receiverId, Long senderId) {
        String cacheKey = buildMessagesCacheKey(senderId, receiverId);

        Collection<String> keys = redisCache.keys(cacheKey + "*");
        if (keys != null) {
            for (String key : keys) {
                ChatMessage m = redisCache.getCacheObject(key);
                if (m != null && m instanceof ChatMessage && !m.isRead() && m.getReceiverId().equals(receiverId)) {
                    m.setIsRead(true);
                    redisCache.setCacheObject(key, m);
                }
            }
        }

        chatMessageMapper.markMessagesAsRead(receiverId, senderId);

        updateUnreadCount(receiverId);
    }

    @Override
    public void deleteMessage(Long messageId, Long userId) {
        String pattern = CHAT_MESSAGES_KEY + "*";
        Collection<String> keys = redisCache.keys(pattern);
        if (keys != null) {
            for (String key : keys) {
                Object msg = redisCache.getCacheObject(key);
                if (msg instanceof ChatMessage) {
                    ChatMessage m = (ChatMessage) msg;
                    if (m.getId() != null && m.getId().equals(messageId) &&
                            (m.getSenderId().equals(userId) || m.getReceiverId().equals(userId))) {
                        m.setIsDeleted(1);
                        redisCache.setCacheObject(key, m);
                        break;
                    }
                }
            }
        }

        if (messageId != null) {
            try {
                chatMessageMapper.deleteMessage(messageId, userId);
            } catch (Exception e) {
                log.warn("删除消息失败: messageId={}, userId={}, error={}", messageId, userId, e.getMessage());
            }
        }
    }

    @Override
    public void deleteConversation(Long userId1, Long userId2) {
        String cacheKey1 = buildMessagesCacheKey(userId1, userId2);
        String cacheKey2 = buildMessagesCacheKey(userId2, userId1);

        redisCache.deleteObject(cacheKey1);
        redisCache.deleteObject(cacheKey2);

        String convKey1 = CHAT_CONVERSATIONS_KEY + userId1;
        String convKey2 = CHAT_CONVERSATIONS_KEY + userId2;
        redisCache.deleteObject(convKey1);
        redisCache.deleteObject(convKey2);

        chatConversationMapper.deleteByUserIdAndOtherId(userId1, userId2);
    }

    @Override
    public void hideChatHistoryForUser(Long userId, Long otherUserId) {
        String cacheKey1 = buildMessagesCacheKey(userId, otherUserId);
        String cacheKey2 = buildMessagesCacheKey(otherUserId, userId);

        redisCache.deleteObject(cacheKey1);
        redisCache.deleteObject(cacheKey2);

        String convKey = CHAT_CONVERSATIONS_KEY + userId;
        redisCache.deleteObject(convKey);

        chatMessageMapper.hideMessagesForUser(userId, otherUserId);
    }

    @Override
    public int getUnreadCount(Long userId) {
        String key = CHAT_UNREAD_KEY + userId;
        Integer count = redisCache.getCacheObject(key);
        if (count != null) {
            return count;
        }

        int dbCount = chatMessageMapper.getUnreadCount(userId);
        redisCache.setCacheObject(key, dbCount);
        return dbCount;
    }

    @Override
    public int getUnreadCountFromUser(Long receiverId, Long senderId) {
        return chatMessageMapper.getUnreadCountFromUser(receiverId, senderId);
    }

    @Override
    @Transactional
    public void syncToDatabase(Long userId) {
        String pendingKey = CHAT_PENDING_KEY + userId;
        List<ChatMessage> pending = redisCache.getCacheList(pendingKey);

        if (pending == null || pending.isEmpty()) {
            return;
        }

        List<ChatMessage> toSave = pending.stream()
                .filter(m -> m instanceof ChatMessage)
                .map(m -> (ChatMessage) m)
                .collect(Collectors.toList());

        if (!toSave.isEmpty()) {
            chatMessageMapper.batchInsertChatMessage(toSave);
            log.info("同步用户 {} 的 {} 条聊天消息到数据库", userId, toSave.size());
        }

        redisCache.deleteObject(pendingKey);
    }

    private void updateUnreadCount(Long userId) {
        String key = CHAT_UNREAD_KEY + userId;
        int count = chatMessageMapper.getUnreadCount(userId);
        redisCache.setCacheObject(key, count);
    }

    private String buildMessagesCacheKey(Long userId1, Long userId2) {
        long minId = Math.min(userId1, userId2);
        long maxId = Math.max(userId1, userId2);
        return CHAT_MESSAGES_KEY + minId + ":" + maxId + ":";
    }
}
