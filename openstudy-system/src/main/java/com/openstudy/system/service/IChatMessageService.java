package com.openstudy.system.service;

import com.openstudy.system.domain.ChatMessage;
import java.util.List;

public interface IChatMessageService {

    ChatMessage sendMessage(ChatMessage message);

    List<ChatMessage> getConversation(Long userId1, Long userId2, int page, int size);

    List<ChatMessage> getRecentConversations(Long userId, int limit);

    void markAsRead(Long receiverId, Long senderId);

    void deleteMessage(Long messageId, Long userId);

    void deleteConversation(Long userId1, Long userId2);

    void hideChatHistoryForUser(Long userId, Long otherUserId);

    int getUnreadCount(Long userId);

    int getUnreadCountFromUser(Long receiverId, Long senderId);

    void syncToDatabase(Long userId);
}
