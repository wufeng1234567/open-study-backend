package com.openstudy.favoriteBank.service;

import com.openstudy.favoriteBank.domain.UserBankFavorite;

import java.util.List;

/**
 * 用户题库收藏Service接口
 * 
 * @author liu
 * @date 2025-12-09
 */
public interface IUserBankFavoriteService 
{
    /**
     * 查询用户题库收藏
     * 
     * @param favoriteId 用户题库收藏主键
     * @return 用户题库收藏
     */
    public UserBankFavorite selectUserBankFavoriteByFavoriteId(Long favoriteId);

    /**
     * 查询用户题库收藏列表
     * 
     * @param userBankFavorite 用户题库收藏
     * @return 用户题库收藏集合
     */
    public List<UserBankFavorite> selectUserBankFavoriteList(UserBankFavorite userBankFavorite);

    /**
     * 新增用户题库收藏
     * 
     * @param userBankFavorite 用户题库收藏
     * @return 结果
     */
    public int insertUserBankFavorite(UserBankFavorite userBankFavorite);

    /**
     * 修改用户题库收藏
     * 
     * @param userBankFavorite 用户题库收藏
     * @return 结果
     */
    public int updateUserBankFavorite(UserBankFavorite userBankFavorite);

    /**
     * 批量删除用户题库收藏
     * 
     * @param favoriteIds 需要删除的用户题库收藏主键集合
     * @return 结果
     */
    public int deleteUserBankFavoriteByFavoriteIds(Long[] favoriteIds);

    /**
     * 删除用户题库收藏信息
     * 
     * @param favoriteId 用户题库收藏主键
     * @return 结果
     */
    public int deleteUserBankFavoriteByFavoriteId(Long favoriteId);

    public boolean checkBankExists(Long bankId);

    boolean checkBankFavoriteExists(Long userId, Long bankId);

    UserBankFavorite selectUserBankFavoriteByUserIdAndBankId(Long userId, Long bankId);

    /**
     * 删除用户题库收藏
     *
     * @param userId 用户ID
     * @param bankId 题库ID
     * @return 结果
     */
    int deleteUserBankFavoriteByUserIdAndBankId(Long userId, Long bankId);


}
