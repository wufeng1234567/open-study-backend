package com.openstudy.ai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * RAG 问答响应对象（支持越界检测）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RagAnswerResponse {
    
    /**
     * AI 回答内容
     */
    private String answer;
    
    /**
     * 用户问题
     */
    private String question;
    
    /**
     * 回答来源：knowledge_base（知识库）或 ai_general（通用AI）
     */
    private String source;
    
    /**
     * 置信度/相似度得分（0-1之间）
     */
    private Double confidence;
    
    /**
     * 是否在知识库范围内
     */
    private Boolean isInScope;
    
    /**
     * 用户是否有其他知识库
     */
    private Boolean hasOtherKB;
    
    /**
     * 其他知识库列表（只包含 id 和 name）
     */
    private List<Map<String, Object>> otherKBs;
    
    /**
     * 提示语（用于前端显示）
     */
    private String tip;
}
