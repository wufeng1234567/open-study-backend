package com.openstudy.ai.mapper;

import com.openstudy.ai.domain.RagKnowledgeBase;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface RagKnowledgeBaseMapper {
    int insert(RagKnowledgeBase kb);
    int deleteById(Long id);
    int update(RagKnowledgeBase kb);
    RagKnowledgeBase selectById(Long id);
    List<RagKnowledgeBase> selectByUserId(Long userId);
    
    /**
     * 增量更新文档数量
     * @param id 知识库ID
     * @param delta 变化量（正数增加，负数减少）
     */
    void incrementDocumentCount(@Param("id") Long id, @Param("delta") int delta);
}