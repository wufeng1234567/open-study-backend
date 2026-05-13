package com.openstudy.questionError.service;

import com.openstudy.questionError.domain.UserQuestionError;

import java.util.List;

/**
 * 用户错题记录（支持复习与掌握跟踪）Service接口
 * 
 * @author liu
 * @date 2025-12-16
 */
public interface IUserQuestionErrorService 
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
     * 批量删除用户错题记录（支持复习与掌握跟踪）
     * 
     * @param errorIds 需要删除的用户错题记录（支持复习与掌握跟踪）主键集合
     * @return 结果
     */
    public int deleteUserQuestionErrorByErrorIds(Long[] errorIds);

    /**
     * 删除用户错题记录（支持复习与掌握跟踪）信息
     * 
     * @param errorId 用户错题记录（支持复习与掌握跟踪）主键
     * @return 结果
     */
    public int deleteUserQuestionErrorByErrorId(Long errorId);


    /**
     * 检查用户对某题的错题记录是否存在
     *
     * @param userId 用户ID
     * @param questionId 题目ID
     * @return true-存在错题记录，false-不存在
     */
    boolean checkUserQuestionErrorExists(Long userId, Long questionId);

    /**
     * 增加错误次数（如果记录已存在）
     *
     * @param userId 用户ID
     * @param questionId 题目ID
     * @return 是否成功
     */
    boolean incrementErrorCount(Long userId, Long questionId);

    /**
     * 记录错题（智能方法：存在则增加次数，不存在则新增）
     *
     * @param errorRecord 错题记录
     * @return 是否成功
     */
    boolean recordOrUpdateQuestionError(UserQuestionError errorRecord);
}
