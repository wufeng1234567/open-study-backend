package com.openstudy.ai.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

/**
 * RAG 问答记录
 */
@Data
public class RagQaRecord {
    private Long id;
    private Long userId;
    private Long knowledgeBaseId;
    private String question;
    private String answer;
    private Integer durationMs;
    private Integer feedback; // 用户反馈：1-有用，0-无用，null-未反馈
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
