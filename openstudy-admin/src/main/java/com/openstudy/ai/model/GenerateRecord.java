package com.openstudy.ai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateRecord {
    
    private Long userId;                // 用户ID
    private Long bankId;                // 题库ID（可为空）
    private String sessionId;           // 会话ID
    private String requestType;         // question / composite
    private String questionType;        // single/multiple/judge/fill/essay
    private String knowledgePoint;      // 知识点/出题要求
    private Integer requestCount;       // 请求生成数量
    private Integer generatedCount;     // 实际生成数量
    private String provider;            // AI模型
    private Long timestamp;             // 时间戳
}