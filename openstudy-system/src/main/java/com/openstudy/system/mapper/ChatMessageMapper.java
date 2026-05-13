package com.openstudy.system.mapper;

import com.openstudy.system.domain.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface ChatMessageMapper {

        int insertChatMessage(ChatMessage message);

        int batchInsertChatMessage(@Param("messages") List<ChatMessage> messages);

        List<ChatMessage> selectChatMessagesByConversation(
                        @Param("userId1") Long userId1,
                        @Param("userId2") Long userId2,
                        @Param("currentUserId") Long currentUserId,
                        @Param("limit") int limit,
                        @Param("offset") int offset);

        List<ChatMessage> selectRecentConversations(@Param("userId") Long userId, @Param("limit") int limit);

        List<ChatMessage> selectLastMessageBetweenUsers(
                        @Param("otherUserId") Long otherUserId,
                        @Param("currentUserId") Long currentUserId,
                        @Param("limit") int limit);

        int markMessagesAsRead(@Param("receiverId") Long receiverId, @Param("senderId") Long senderId);

        int deleteMessage(@Param("id") Long id, @Param("userId") Long userId);

        int getUnreadCount(@Param("receiverId") Long receiverId);

        int getUnreadCountFromUser(@Param("receiverId") Long receiverId, @Param("senderId") Long senderId);

        ChatMessage findMessageByUniqueKey(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId,
            @Param("content") String content);

        int deleteConversation(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

        int hideConversationFromUser(@Param("userId") Long userId, @Param("otherUserId") Long otherUserId);

        int hideMessagesForUser(@Param("userId") Long userId, @Param("otherUserId") Long otherUserId);
}
