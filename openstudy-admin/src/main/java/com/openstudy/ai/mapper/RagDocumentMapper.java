package com.openstudy.ai.mapper;

import com.openstudy.ai.domain.RagDocument;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface RagDocumentMapper {
    int insert(RagDocument doc);
    int deleteById(Long id);
    int update(RagDocument doc);
    RagDocument selectById(Long id);
    List<RagDocument> selectByKnowledgeBaseId(@Param("knowledgeBaseId") Long knowledgeBaseId);
    
    /**
     * 根据状态查询文档列表
     */
    List<RagDocument> selectByStatus(@Param("status") Integer status);

    /**
     * 批量根据ID查询文档
     */
    List<RagDocument> selectByIds(@Param("ids") List<Long> ids);
}