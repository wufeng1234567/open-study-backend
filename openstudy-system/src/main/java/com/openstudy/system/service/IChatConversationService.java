package com.openstudy.system.service;

import com.openstudy.system.domain.ChatConversation;
import java.util.List;

public interface IChatConversationService {

    List<ChatConversation> getUserConversations(Long userId);

    void createConversation(Long userId, Long otherUserId);

    void deleteConversation(Long userId, Long otherUserId);

    boolean hasConversation(Long userId, Long otherUserId);
}
