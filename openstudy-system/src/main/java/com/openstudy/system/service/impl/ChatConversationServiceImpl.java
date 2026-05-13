package com.openstudy.system.service.impl;

import com.openstudy.system.domain.ChatConversation;
import com.openstudy.system.mapper.ChatConversationMapper;
import com.openstudy.system.service.IChatConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ChatConversationServiceImpl implements IChatConversationService {

    @Autowired
    private ChatConversationMapper chatConversationMapper;

    @Override
    public List<ChatConversation> getUserConversations(Long userId) {
        return chatConversationMapper.selectByUserId(userId);
    }

    @Override
    public void createConversation(Long userId, Long otherUserId) {
        ChatConversation existing = chatConversationMapper.selectByUserIdAndOtherId(userId, otherUserId);
        if (existing == null) {
            ChatConversation conversation = new ChatConversation();
            conversation.setUserId(userId);
            conversation.setOtherUserId(otherUserId);
            chatConversationMapper.insert(conversation);
        }
    }

    @Override
    public void deleteConversation(Long userId, Long otherUserId) {
        chatConversationMapper.deleteByUserIdAndOtherId(userId, otherUserId);
    }

    @Override
    public boolean hasConversation(Long userId, Long otherUserId) {
        return chatConversationMapper.selectByUserIdAndOtherId(userId, otherUserId) != null;
    }
}
