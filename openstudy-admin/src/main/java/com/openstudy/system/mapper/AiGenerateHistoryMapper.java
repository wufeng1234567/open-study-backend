package com.openstudy.system.mapper;

import com.openstudy.system.domain.AiGenerateHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface AiGenerateHistoryMapper {
    
    int insert(AiGenerateHistory record);
    
    int batchInsert(@Param("list") List<AiGenerateHistory> list);
    
    List<AiGenerateHistory> selectByUserId(@Param("userId") Long userId);
}