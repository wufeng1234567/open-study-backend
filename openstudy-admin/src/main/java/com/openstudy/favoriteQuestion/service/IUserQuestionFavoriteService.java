package com.openstudy.favoriteQuestion.service;

import com.openstudy.favoriteQuestion.domain.UserQuestionFavorite;

import java.util.List;

/**
 * 用户题目收藏（支持复习功能）Service接口
 *
 * @author liu
 * @date 2025-12-10
 */
public interface IUserQuestionFavoriteService
{
    /**
     * 查询用户题目收藏（支持复习功能）
     *
     * @param favoriteId 用户题目收藏（支持复习功能）主键
     * @return 用户题目收藏（支持复习功能）
     */
    public UserQuestionFavorite selectUserQuestionFavoriteByFavoriteId(Long favoriteId);

    /**
     * 查询用户题目收藏（支持复习功能）列表
     *
     * @param userQuestionFavorite 用户题目收藏（支持复习功能）
     * @return 用户题目收藏（支持复习功能）
     */
    public List<UserQuestionFavorite> selectUserQuestionFavoriteList(UserQuestionFavorite userQuestionFavorite);

    /**
     * 检查题目是否存在
     *
     * @param questionId 题目ID
     * @return 是否存在
     */
    public boolean checkQuestionExists(Long questionId);

    /**
     * 新增用户题目收藏（支持复习功能）
     *
     * @param userQuestionFavorite 用户题目收藏（支持复习功能）
     * @return 结果
     */
    public int insertUserQuestionFavorite(UserQuestionFavorite userQuestionFavorite);

    /**
     * 修改用户题目收藏（支持复习功能）
     *
     * @param userQuestionFavorite 用户题目收藏（支持复习功能）
     * @return 结果
     */
    public int updateUserQuestionFavorite(UserQuestionFavorite userQuestionFavorite);

    /**
     * 批量删除用户题目收藏（支持复习功能）
     *
     * @param favoriteIds 需要删除的用户题目收藏（支持复习功能）主键
     * @return 结果
     */
    public int deleteUserQuestionFavoriteByFavoriteIds(Long[] favoriteIds);

    /**
     * 删除用户题目收藏（支持复习功能）信息
     *
     * @param favoriteId 用户题目收藏（支持复习功能）主键
     * @return 结果
     */
    public int deleteUserQuestionFavoriteByFavoriteId(Long favoriteId);

    Long getQuestionBankId(Long questionId);

    boolean checkQuestionFavoriteExists(Long userId, Long questionId);

    UserQuestionFavorite selectUserQuestionFavoriteByUserIdAndQuestionId(Long userId, Long questionId);

}