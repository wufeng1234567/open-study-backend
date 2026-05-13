package com.openstudy.ai.service;

import com.openstudy.ai.domain.RagKnowledgeBase;

import java.util.List;

/**
 * RAG 知识库服务接口
 */
public interface RagKnowledgeBaseService {
    
    /**
     * 创建知识库
     */
    RagKnowledgeBase create(RagKnowledgeBase kb);
    
    /**
     * 更新知识库
     */
    int update(RagKnowledgeBase kb);
    
    /**
     * 删除知识库
     */
    int deleteById(Long id);
    
    /**
     * 根据ID查询知识库
     */
    RagKnowledgeBase getById(Long id);
    
    /**
     * 查询用户的知识库列表
     */
    List<RagKnowledgeBase> listByUserId(Long userId);
}
