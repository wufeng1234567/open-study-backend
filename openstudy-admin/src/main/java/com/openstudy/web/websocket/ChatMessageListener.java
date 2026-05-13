package com.openstudy.web.websocket;

import com.alibaba.fastjson.JSON;
import com.openstudy.system.event.ChatMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ChatMessageListener {
    private static final Logger log = LoggerFactory.getLogger(ChatMessageListener.class);

    @Autowired
    private ChatWebSocketHandler chatWebSocketHandler;

    @EventListener
    public void handleChatMessageEvent(ChatMessageEvent event) {
        try {
            String jsonMsg = JSON.toJSONString(event.getMessage());
            log.info("ChatMessageListener 收到事件: senderId={}, receiverId={}, content={}",
                    event.getMessage().getSenderId(), event.getMessage().getReceiverId(), event.getMessage().getContent());
            chatWebSocketHandler.sendMessageToUser(event.getMessage().getReceiverId(), jsonMsg);
            log.debug("推送聊天消息到用户: receiverId={}", event.getMessage().getReceiverId());
        } catch (Exception e) {
            log.error("推送聊天消息失败", e);
        }
    }
}
