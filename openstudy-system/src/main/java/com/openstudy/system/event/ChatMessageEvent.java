package com.openstudy.system.event;

import com.openstudy.system.domain.ChatMessage;

public class ChatMessageEvent {
    private ChatMessage message;

    public ChatMessageEvent(ChatMessage message) {
        this.message = message;
    }

    public ChatMessage getMessage() {
        return message;
    }
}
