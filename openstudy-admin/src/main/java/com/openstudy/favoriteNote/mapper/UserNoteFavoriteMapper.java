package com.openstudy.favoriteNote.mapper;

import com.openstudy.favoriteNote.domain.UserNoteFavorite;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户笔记收藏Mapper接口
 *
 * @author liu
 * @date 2026-05-04
 */
public interface UserNoteFavoriteMapper
{
    /**
     * 查询用户笔记收藏
     *
     * @param favoriteId 收藏ID
     * @return 用户笔记收藏
     */
    public UserNoteFavorite selectUserNoteFavoriteByFavoriteId(Long favoriteId);

    /**
     * 查询用户笔记收藏列表
     *
     * @param userNoteFavorite 用户笔记收藏
     * @return 用户笔记收藏集合
     */
    public List<UserNoteFavorite> selectUserNoteFavoriteList(UserNoteFavorite userNoteFavorite);

    /**
     * 检查笔记是否存在
     *
     * @param noteId 笔记ID
     * @return 是否存在（0表示不存在，>0表示存在）
     */
    public int checkNoteExists(@Param("noteId") Long noteId);

    /**
     * 检查是否已收藏笔记
     *
     * @param userId 用户ID
     * @param noteId 笔记ID
     * @return 是否已收藏
     */
    public int checkFavoriteExists(@Param("userId") Long userId, @Param("noteId") Long noteId);

    /**
     * 新增用户笔记收藏
     *
     * @param userNoteFavorite 用户笔记收藏
     * @return 结果
     */
    public int insertUserNoteFavorite(UserNoteFavorite userNoteFavorite);

    /**
     * 删除用户笔记收藏
     *
     * @param favoriteId 收藏ID
     * @return 结果
     */
    public int deleteUserNoteFavoriteByFavoriteId(Long favoriteId);

    /**
     * 根据用户ID和笔记ID删除收藏
     *
     * @param userId 用户ID
     * @param noteId 笔记ID
     * @return 结果
     */
    public int deleteByUserAndNote(@Param("userId") Long userId, @Param("noteId") Long noteId);

    /**
     * 批量删除用户笔记收藏
     *
     * @param favoriteIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteUserNoteFavoriteByFavoriteIds(Long[] favoriteIds);

    /**
     * 获取用户收藏的笔记ID列表
     *
     * @param userId 用户ID
     * @return 笔记ID列表
     */
    public List<Long> selectFavoriteNoteIdsByUserId(@Param("userId") Long userId);
}
