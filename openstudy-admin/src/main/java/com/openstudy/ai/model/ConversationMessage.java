package com.openstudy.ai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationMessage {
    
    private String role;        // user / assistant / system
    private String content;     // 消息内容
    private Long timestamp;     // 时间戳
}