package com.openstudy.favoriteBank.mapper;

import com.openstudy.favoriteBank.domain.UserBankFavorite;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户题库收藏Mapper接口
 * 
 * @author liu
 * @date 2025-12-09
 */
public interface UserBankFavoriteMapper 
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
     * 删除用户题库收藏
     * 
     * @param favoriteId 用户题库收藏主键
     * @return 结果
     */
    public int deleteUserBankFavoriteByFavoriteId(Long favoriteId);

    /**
     * 批量删除用户题库收藏
     * 
     * @param favoriteIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteUserBankFavoriteByFavoriteIds(Long[] favoriteIds);

    int checkBankExists(Long bankId);


    int checkFavoriteExists(@Param("userId") Long userId,
                            @Param("bankId") Long bankId,
                            @Param("excludeFavoriteId") Long excludeFavoriteId);

    /**
     * 根据用户ID和题库ID删除收藏
     *
     * @param userId 用户ID
     * @param bankId 题库ID
     * @return 结果
     */
    int deleteUserBankFavoriteByUserIdAndBankId(@Param("userId") Long userId,
                                                @Param("bankId") Long bankId);



}

