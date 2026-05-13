package com.openstudy.favoriteNote.service.impl;

import com.openstudy.common.exception.ServiceException;
import com.openstudy.common.utils.DateUtils;
import com.openstudy.favoriteNote.domain.UserNoteFavorite;
import com.openstudy.favoriteNote.mapper.UserNoteFavoriteMapper;
import com.openstudy.favoriteNote.service.IUserNoteFavoriteService;
import com.openstudy.notes.domain.Note;
import com.openstudy.notes.mapper.NoteMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户笔记收藏Service业务层处理
 *
 * @author liu
 * @date 2026-05-04
 */
@Service
public class UserNoteFavoriteServiceImpl implements IUserNoteFavoriteService
{
    @Autowired
    private UserNoteFavoriteMapper userNoteFavoriteMapper;

    @Autowired
    private NoteMapper noteMapper;

    /**
     * 查询用户笔记收藏
     *
     * @param favoriteId 收藏ID
     * @return 用户笔记收藏
     */
    @Override
    public UserNoteFavorite selectUserNoteFavoriteByFavoriteId(Long favoriteId)
    {
        return userNoteFavoriteMapper.selectUserNoteFavoriteByFavoriteId(favoriteId);
    }

    /**
     * 查询用户笔记收藏列表
     *
     * @param userNoteFavorite 用户笔记收藏
     * @return 用户笔记收藏
     */
    @Override
    public List<UserNoteFavorite> selectUserNoteFavoriteList(UserNoteFavorite userNoteFavorite)
    {
        return userNoteFavoriteMapper.selectUserNoteFavoriteList(userNoteFavorite);
    }

    /**
     * 检查笔记是否存在
     *
     * @param noteId 笔记ID
     * @return 是否存在
     */
    @Override
    public boolean checkNoteExists(Long noteId)
    {
        return userNoteFavoriteMapper.checkNoteExists(noteId) > 0;
    }

    /**
     * 新增用户笔记收藏
     *
     * @param userNoteFavorite 用户笔记收藏
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertUserNoteFavorite(UserNoteFavorite userNoteFavorite)
    {
        if (userNoteFavorite.getUserId() == null)
        {
            throw new ServiceException("用户ID不能为空");
        }
        if (userNoteFavorite.getNoteId() == null)
        {
            throw new ServiceException("笔记ID不能为空");
        }
        if (!checkNoteExists(userNoteFavorite.getNoteId()))
        {
            throw new ServiceException("笔记不存在");
        }
        if (checkNoteFavoriteExists(userNoteFavorite.getUserId(), userNoteFavorite.getNoteId()))
        {
            throw new ServiceException("已收藏该笔记，不能重复收藏");
        }
        userNoteFavorite.setCreateTime(DateUtils.getNowDate());
        return userNoteFavoriteMapper.insertUserNoteFavorite(userNoteFavorite);
    }

    /**
     * 删除用户笔记收藏
     *
     * @param favoriteId 收藏ID
     * @return 结果
     */
    @Override
    public int deleteUserNoteFavoriteByFavoriteId(Long favoriteId)
    {
        return userNoteFavoriteMapper.deleteUserNoteFavoriteByFavoriteId(favoriteId);
    }

    /**
     * 根据用户ID和笔记ID删除收藏
     *
     * @param userId 用户ID
     * @param noteId 笔记ID
     * @return 结果
     */
    @Override
    public int deleteByUserAndNote(Long userId, Long noteId)
    {
        return userNoteFavoriteMapper.deleteByUserAndNote(userId, noteId);
    }

    /**
     * 检查用户是否已收藏笔记
     *
     * @param userId 用户ID
     * @param noteId 笔记ID
     * @return 是否已收藏
     */
    @Override
    public boolean checkNoteFavoriteExists(Long userId, Long noteId)
    {
        return userNoteFavoriteMapper.checkFavoriteExists(userId, noteId) > 0;
    }

    /**
     * 获取用户收藏的笔记详情列表
     *
     * @param userId 用户ID
     * @return 笔记列表
     */
    @Override
    public List<Note> getFavoriteNoteDetails(Long userId)
    {
        List<Long> noteIds = userNoteFavoriteMapper.selectFavoriteNoteIdsByUserId(userId);
        if (noteIds == null || noteIds.isEmpty())
        {
            return new ArrayList<>();
        }
        List<Note> notes = new ArrayList<>();
        for (Long noteId : noteIds)
        {
            Note note = noteMapper.selectNoteById(noteId);
            if (note != null)
            {
                notes.add(note);
            }
        }
        return notes;
    }

    /**
     * 批量删除用户笔记收藏
     *
     * @param favoriteIds 需要删除的收藏ID
     * @return 结果
     */
    @Override
    public int deleteUserNoteFavoriteByFavoriteIds(Long[] favoriteIds)
    {
        return userNoteFavoriteMapper.deleteUserNoteFavoriteByFavoriteIds(favoriteIds);
    }
}
