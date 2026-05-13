package com.openstudy.system.mapper;

import com.openstudy.system.domain.ChatConversation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ChatConversationMapper {

    List<ChatConversation> selectByUserId(@Param("userId") Long userId);

    ChatConversation selectByUserIdAndOtherId(@Param("userId") Long userId, @Param("otherUserId") Long otherUserId);

    int insert(ChatConversation conversation);

    int deleteByUserIdAndOtherId(@Param("userId") Long userId, @Param("otherUserId") Long otherUserId);

    int deleteByUserId(@Param("userId") Long userId);
}
