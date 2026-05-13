package com.openstudy.favoriteQuestion.mapper;

import com.openstudy.favoriteQuestion.domain.UserQuestionFavorite;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户题目收藏（支持复习功能）Mapper接口
 *
 * @author liu
 * @date 2025-12-10
 */
public interface UserQuestionFavoriteMapper
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
     * @return 用户题目收藏（支持复习功能）集合
     */
    public List<UserQuestionFavorite> selectUserQuestionFavoriteList(UserQuestionFavorite userQuestionFavorite);

    /**
     * 检查题目是否存在
     *
     * @param questionId 题目ID
     * @return 是否存在（int类型，0表示不存在，>0表示存在）
     */
    public int checkQuestionExists(Long questionId);

    /**
     * 检查是否已收藏题目
     *
     * @param userId 用户ID
     * @param questionId 题目ID
     * @param excludeFavoriteId 排除的收藏ID（用于修改时）
     * @return 是否已收藏
     */
    public int checkFavoriteExists(@Param("userId") Long userId,
                                   @Param("questionId") Long questionId,
                                   @Param("excludeFavoriteId") Long excludeFavoriteId);

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
     * 删除用户题目收藏（支持复习功能）
     *
     * @param favoriteId 用户题目收藏（支持复习功能）主键
     * @return 结果
     */
    public int deleteUserQuestionFavoriteByFavoriteId(Long favoriteId);

    /**
     * 批量删除用户题目收藏（支持复习功能）
     *
     * @param favoriteIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteUserQuestionFavoriteByFavoriteIds(Long[] favoriteIds);
    /**
     * 根据题目ID获取题库ID
     * @param questionId 题目ID
     * @return 题库ID
     */
    Long getQuestionBankIdByQuestionId(@Param("questionId") Long questionId);

    // long getQuestionBankId(Long questionId);
}