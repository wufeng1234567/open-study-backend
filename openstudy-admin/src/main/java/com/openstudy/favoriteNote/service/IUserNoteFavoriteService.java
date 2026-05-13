package com.openstudy.favoriteNote.service;

import com.openstudy.favoriteNote.domain.UserNoteFavorite;
import com.openstudy.notes.domain.Note;

import java.util.List;

/**
 * 用户笔记收藏Service接口
 *
 * @author liu
 * @date 2026-05-04
 */
public interface IUserNoteFavoriteService
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
     * @return 用户笔记收藏
     */
    public List<UserNoteFavorite> selectUserNoteFavoriteList(UserNoteFavorite userNoteFavorite);

    /**
     * 检查笔记是否存在
     *
     * @param noteId 笔记ID
     * @return 是否存在
     */
    public boolean checkNoteExists(Long noteId);

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
    public int deleteByUserAndNote(Long userId, Long noteId);

    /**
     * 检查用户是否已收藏笔记
     *
     * @param userId 用户ID
     * @param noteId 笔记ID
     * @return 是否已收藏
     */
    public boolean checkNoteFavoriteExists(Long userId, Long noteId);

    /**
     * 获取用户收藏的笔记详情列表
     *
     * @param userId 用户ID
     * @return 笔记列表
     */
    public List<Note> getFavoriteNoteDetails(Long userId);

    /**
     * 批量删除用户笔记收藏
     *
     * @param favoriteIds 需要删除的收藏ID
     * @return 结果
     */
    public int deleteUserNoteFavoriteByFavoriteIds(Long[] favoriteIds);
}
