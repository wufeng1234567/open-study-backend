package com.openstudy.ai.mapper;

import com.openstudy.ai.domain.RagQaRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * RAG 问答记录 Mapper
 */
@Mapper
public interface RagQaRecordMapper {
    int insert(RagQaRecord record);

    List<RagQaRecord> selectByKnowledgeBaseId(@Param("knowledgeBaseId") Long knowledgeBaseId);

    /**
     * 删除指定知识库的所有问答记录
     *
     * @param knowledgeBaseId 知识库ID
     * @return 删除的记录数
     */
    int deleteByKnowledgeBaseId(@Param("knowledgeBaseId") Long knowledgeBaseId);
}
