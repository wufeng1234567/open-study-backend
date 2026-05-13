package com.openstudy.questionMarked.service;

import com.openstudy.questionMarked.domain.UserQuestionMarked;

import java.util.List;

/**
 * 用户斩题（重点攻克题目）Service接口
 * 
 * @author ruoyi
 * @date 2025-12-17
 */
public interface IUserQuestionMarkedService 
{
    /**
     * 查询用户斩题（重点攻克题目）
     * 
     * @param markedId 用户斩题（重点攻克题目）主键
     * @return 用户斩题（重点攻克题目）
     */
    public UserQuestionMarked selectUserQuestionMarkedByMarkedId(Long markedId);

    /**
     * 查询用户斩题（重点攻克题目）列表
     * 
     * @param userQuestionMarked 用户斩题（重点攻克题目）
     * @return 用户斩题（重点攻克题目）集合
     */
    public List<UserQuestionMarked> selectUserQuestionMarkedList(UserQuestionMarked userQuestionMarked);

    /**
     * 新增用户斩题（重点攻克题目）
     * 
     * @param userQuestionMarked 用户斩题（重点攻克题目）
     * @return 结果
     */
    public int insertUserQuestionMarked(UserQuestionMarked userQuestionMarked);

    /**
     * 修改用户斩题（重点攻克题目）
     * 
     * @param userQuestionMarked 用户斩题（重点攻克题目）
     * @return 结果
     */
    public int updateUserQuestionMarked(UserQuestionMarked userQuestionMarked);

    /**
     * 批量删除用户斩题（重点攻克题目）
     * 
     * @param markedIds 需要删除的用户斩题（重点攻克题目）主键集合
     * @return 结果
     */
    public int deleteUserQuestionMarkedByMarkedIds(Long[] markedIds);

    /**
     * 删除用户斩题（重点攻克题目）信息
     * 
     * @param markedId 用户斩题（重点攻克题目）主键
     * @return 结果
     */
    public int deleteUserQuestionMarkedByMarkedId(Long markedId);

    /**
     * 检查用户是否已斩题
     * @param userId 用户ID
     * @param questionId 题目ID
     * @return 是否已斩题
     */
    boolean checkQuestionMarkedExists(Long userId, Long questionId);

    /**
     * 根据用户ID和题目ID查询斩题记录
     * @param userId 用户ID
     * @param questionId 题目ID
     * @return 斩题记录
     */
    UserQuestionMarked selectUserQuestionMarkedByUserIdAndQuestionId(Long userId, Long questionId);

    Long getQuestionBankId(Long questionId);
}
