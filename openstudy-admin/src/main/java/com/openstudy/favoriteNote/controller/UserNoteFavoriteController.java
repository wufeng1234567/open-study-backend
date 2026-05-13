package com.openstudy.favoriteNote.controller;

import com.openstudy.common.annotation.Log;
import com.openstudy.common.core.controller.BaseController;
import com.openstudy.common.core.domain.AjaxResult;
import com.openstudy.common.core.page.TableDataInfo;
import com.openstudy.common.enums.BusinessType;
import com.openstudy.common.exception.ServiceException;
import com.openstudy.favoriteNote.domain.UserNoteFavorite;
import com.openstudy.favoriteNote.service.IUserNoteFavoriteService;
import com.openstudy.notes.domain.Note;
import com.openstudy.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户笔记收藏Controller
 *
 * @author liu
 * @date 2026-05-04
 */
@RestController
@RequestMapping("/favoriteNote/favoriteNote")
@PreAuthorize("@ss.hasRole('common')")
public class UserNoteFavoriteController extends BaseController
{
    @Autowired
    private IUserNoteFavoriteService userNoteFavoriteService;

    @Autowired
    private ISysUserService userService;

    /**
     * 查询用户笔记收藏列表
     */
    @GetMapping("/list")
    public TableDataInfo list(UserNoteFavorite userNoteFavorite)
    {
        startPage();
        List<UserNoteFavorite> list = userNoteFavoriteService.selectUserNoteFavoriteList(userNoteFavorite);
        return getDataTable(list);
    }

    /**
     * 获取用户收藏的笔记详情列表
     */
    @GetMapping("/details")
    public AjaxResult details()
    {
        Long userId = getUserId();
        List<Note> notes = userNoteFavoriteService.getFavoriteNoteDetails(userId);
        return success(notes);
    }

    /**
     * 检查笔记是否存在
     */
    @GetMapping("/checkNote/{noteId}")
    public AjaxResult checkNoteExists(@PathVariable Long noteId)
    {
        boolean exists = userNoteFavoriteService.checkNoteExists(noteId);
        Map<String, Object> result = new HashMap<>();
        result.put("exists", exists);
        result.put("noteId", noteId);
        return success(result);
    }

    /**
     * 获取用户笔记收藏详细信息
     */
    @GetMapping(value = "/{favoriteId}")
    public AjaxResult getInfo(@PathVariable("favoriteId") Long favoriteId)
    {
        return success(userNoteFavoriteService.selectUserNoteFavoriteByFavoriteId(favoriteId));
    }

    /**
     * 新增用户笔记收藏
     */
    @Log(title = "用户笔记收藏", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody UserNoteFavorite userNoteFavorite)
    {
        try
        {
            if (userNoteFavorite.getUserId() == null)
            {
                return error("用户ID不能为空");
            }
            if (userNoteFavorite.getNoteId() == null)
            {
                return error("笔记ID不能为空");
            }
            int result = userNoteFavoriteService.insertUserNoteFavorite(userNoteFavorite);
            return toAjax(result);
        }
        catch (ServiceException e)
        {
            return error(e.getMessage());
        }
        catch (Exception e)
        {
            logger.error("新增笔记收藏失败", e);
            return error("新增失败：" + e.getMessage());
        }
    }

    /**
     * 删除用户笔记收藏
     */
    @Log(title = "用户笔记收藏", businessType = BusinessType.DELETE)
    @DeleteMapping("/{favoriteIds}")
    public AjaxResult remove(@PathVariable Long[] favoriteIds)
    {
        try
        {
            return toAjax(userNoteFavoriteService.deleteUserNoteFavoriteByFavoriteIds(favoriteIds));
        }
        catch (Exception e)
        {
            logger.error("删除笔记收藏失败", e);
            return error("删除失败：" + e.getMessage());
        }
    }

    /**
     * 检查用户是否已收藏笔记
     */
    @GetMapping("/checkFavorite/{userId}/{noteId}")
    public AjaxResult checkNoteFavoriteExists(@PathVariable Long userId, @PathVariable Long noteId)
    {
        try
        {
            boolean isFavorited = userNoteFavoriteService.checkNoteFavoriteExists(userId, noteId);
            Map<String, Object> result = new HashMap<>();
            result.put("userId", userId);
            result.put("noteId", noteId);
            result.put("isFavorited", isFavorited);
            return success(result);
        }
        catch (Exception e)
        {
            logger.error("检查笔记收藏状态失败", e);
            return error("检查失败：" + e.getMessage());
        }
    }

    /**
     * 根据用户ID和笔记ID删除收藏
     */
    @DeleteMapping("/deleteByUserAndNote/{userId}/{noteId}")
    public AjaxResult deleteByUserAndNote(@PathVariable Long userId, @PathVariable Long noteId)
    {
        try
        {
            boolean isFavorited = userNoteFavoriteService.checkNoteFavoriteExists(userId, noteId);
            if (!isFavorited)
            {
                return error("该用户未收藏此笔记");
            }
            int result = userNoteFavoriteService.deleteByUserAndNote(userId, noteId);
            return toAjax(result);
        }
        catch (Exception e)
        {
            logger.error("删除笔记收藏失败", e);
            return error("删除失败：" + e.getMessage());
        }
    }
}
