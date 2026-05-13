package com.openstudy.system.mapper;

import com.openstudy.system.domain.UserAiPreference;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserAiPreferenceMapper {
    
    UserAiPreference selectByUserId(@Param("userId") Long userId);
    
    int insert(UserAiPreference preference);
    
    int update(UserAiPreference preference);
}