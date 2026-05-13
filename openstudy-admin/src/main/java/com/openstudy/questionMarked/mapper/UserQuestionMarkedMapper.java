package com.openstudy.questionMarked.mapper;

import com.openstudy.questionMarked.domain.UserQuestionMarked;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户斩题（重点攻克题目）Mapper接口
 * 
 * @author ruoyi
 * @date 2025-12-17
 */
public interface UserQuestionMarkedMapper 
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
     * 删除用户斩题（重点攻克题目）
     * 
     * @param markedId 用户斩题（重点攻克题目）主键
     * @return 结果
     */
    public int deleteUserQuestionMarkedByMarkedId(Long markedId);

    /**
     * 批量删除用户斩题（重点攻克题目）
     * 
     * @param markedIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteUserQuestionMarkedByMarkedIds(Long[] markedIds);



    /**
     * 检查题目是否存在
     * @param questionId 题目ID
     * @return 是否存在
     */
    int checkQuestionExists(@Param("questionId") Long questionId);

    /**
     * 检查是否已斩题
     * @param userId 用户ID
     * @param questionId 题目ID
     * @param excludeMarkedId 排除的斩题ID（用于修改时）
     * @return 是否已斩题
     */
    int checkMarkedExists(@Param("userId") Long userId,
                          @Param("questionId") Long questionId,
                          @Param("excludeMarkedId") Long excludeMarkedId);


}
