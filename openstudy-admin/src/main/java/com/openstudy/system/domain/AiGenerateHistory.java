package com.openstudy.system.domain;

import lombok.Data;
import java.util.Date;

@Data
public class AiGenerateHistory {
    private Long id;
    private Long userId;
    private Long bankId;
    private String sessionId;
    private String requestType;
    private String questionType;
    private String knowledgePoint;
    private Integer requestCount;
    private Integer generatedCount;
    private String provider;
    private Date createTime;
}