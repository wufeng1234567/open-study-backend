package com.openstudy.questionError.mapper;

import com.openstudy.questionError.domain.UserQuestionError;

import java.util.List;
import java.util.Map;

/**
 * 用户错题记录（支持复习与掌握跟踪）Mapper接口
 * 
 * @author liu
 * @date 2025-12-16
 */
public interface UserQuestionErrorMapper 
{
    /**
     * 查询用户错题记录（支持复习与掌握跟踪）
     * 
     * @param errorId 用户错题记录（支持复习与掌握跟踪）主键
     * @return 用户错题记录（支持复习与掌握跟踪）
     */
    public UserQuestionError selectUserQuestionErrorByErrorId(Long errorId);

    /**
     * 查询用户错题记录（支持复习与掌握跟踪）列表
     * 
     * @param userQuestionError 用户错题记录（支持复习与掌握跟踪）
     * @return 用户错题记录（支持复习与掌握跟踪）集合
     */
    public List<UserQuestionError> selectUserQuestionErrorList(UserQuestionError userQuestionError);

    /**
     * 新增用户错题记录（支持复习与掌握跟踪）
     * 
     * @param userQuestionError 用户错题记录（支持复习与掌握跟踪）
     * @return 结果
     */
    public int insertUserQuestionError(UserQuestionError userQuestionError);

    /**
     * 修改用户错题记录（支持复习与掌握跟踪）
     * 
     * @param userQuestionError 用户错题记录（支持复习与掌握跟踪）
     * @return 结果
     */
    public int updateUserQuestionError(UserQuestionError userQuestionError);

    /**
     * 删除用户错题记录（支持复习与掌握跟踪）
     * 
     * @param errorId 用户错题记录（支持复习与掌握跟踪）主键
     * @return 结果
     */
    public int deleteUserQuestionErrorByErrorId(Long errorId);

    /**
     * 批量删除用户错题记录（支持复习与掌握跟踪）
     * 
     * @param errorIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteUserQuestionErrorByErrorIds(Long[] errorIds);

    /**
     * 检查用户对某题的错题记录是否存在
     *
     * @param params 包含userId和questionId的Map
     * @return 是否存在（1存在，0不存在）
     */
    int checkUserQuestionErrorExists(Map<String, Object> params);

    /**
     * 增加错误次数
     *
     * @param params 包含userId和questionId的Map
     * @return 影响行数
     */
    int incrementErrorCount(Map<String, Object> params);



}
