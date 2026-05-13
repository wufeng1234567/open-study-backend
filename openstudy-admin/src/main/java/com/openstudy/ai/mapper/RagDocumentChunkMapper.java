package com.openstudy.ai.mapper;

import com.openstudy.ai.domain.RagDocumentChunk;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface RagDocumentChunkMapper {
    int insert(RagDocumentChunk chunk);

    /**
     * 批量插入分块
     */
    int batchInsert(List<RagDocumentChunk> chunks);

    int deleteByDocumentId(Long documentId);

    List<RagDocumentChunk> selectByDocumentId(Long documentId);

    /**
     * 通过向量ID查询分块
     */
    RagDocumentChunk selectByVectorId(@Param("vectorId") String vectorId);

    /**
     * 批量通过向量ID查询分块
     */
    List<RagDocumentChunk> selectByVectorIds(@Param("vectorIds") List<String> vectorIds);
}