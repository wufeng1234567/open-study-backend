package com.openstudy.notes.controller;

import com.openstudy.common.core.controller.BaseController;
import com.openstudy.common.core.domain.AjaxResult;
import com.openstudy.common.core.page.TableDataInfo;
import com.openstudy.common.utils.SecurityUtils;
import com.openstudy.notes.domain.Note;
import com.openstudy.notes.domain.NoteClickRecord;
import com.openstudy.notes.mapper.NoteClickRecordMapper;
import com.openstudy.notes.service.INoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公开笔记接口 - 无需登录认证，用于前台"学习分享"页面
 */
@RestController
@RequestMapping("/notes/note/public")
public class NotePublicController extends BaseController
{
    @Autowired
    private INoteService noteService;

    @Autowired
    private NoteClickRecordMapper clickRecordMapper;

    /**
     * 分页查询所有公开笔记
     * 参数：pageNum, pageSize, userId(可选), keyword(可选), categoryId(可选), publicSectionId(可选), sortBy, sortOrder, startTime, endTime
     */
    @GetMapping("/list")
    public TableDataInfo list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long publicSectionId,
            @RequestParam(defaultValue = "create_time") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime)
    {
        startPage();
        Note note = new Note();
        note.setIsPublic(1);  // 只查公开笔记
        
        // 用户ID筛选（查看特定用户的公开笔记）
        if (userId != null) {
            note.setUserId(userId);
        }
        
        // 关键词搜索（标题、内容、作者昵称）
        if (keyword != null && !keyword.trim().isEmpty()) {
            note.setTitle(keyword.trim());
        }
        
        // 分类筛选
        if (categoryId != null) {
            note.setCategoryId(categoryId);
        }
        
        // 公开分区筛选
        if (publicSectionId != null) {
            note.setPublicSectionId(publicSectionId);
        }
        
        // 排序参数
        note.setSortBy(sortBy);
        note.setOrder(sortOrder);
        
        // 时间范围
        if (startTime != null && !startTime.trim().isEmpty()) {
            note.setStartTime(startTime.trim());
        }
        if (endTime != null && !endTime.trim().isEmpty()) {
            note.setEndTime(endTime.trim());
        }
        
        List<Note> list = noteService.selectNoteList(note);
        return getDataTable(list);
    }

    /**
     * 获取单篇公开笔记详情
     */
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        Note note = noteService.selectNoteById(id);
        if (note == null || note.getIsPublic() == null || note.getIsPublic() != 1) {
            return error("笔记不存在或未公开");
        }
        return success(note);
    }

    /**
     * 公开笔记榜单（今日/本周点击榜）
     * @param type 榜单类型：today-今日，week-本周
     * @param limit 返回条数，默认10
     */
    @GetMapping("/ranking")
    public AjaxResult ranking(
            @RequestParam(defaultValue = "today") String type,
            @RequestParam(defaultValue = "10") Integer limit)
    {
        List<Note> list = noteService.selectRankingList(type, limit);
        return success(list);
    }

    /**
     * 记录笔记点击
     * @param noteId 笔记ID
     */
    @PostMapping("/click/{noteId}")
    public AjaxResult recordClick(@PathVariable Long noteId)
    {
        Long userId = null;
        try {
            userId = SecurityUtils.getUserId();
        } catch (Exception e) {
            // 未登录时 userId 为 null，仍然记录点击（用于统计）
        }
        NoteClickRecord record = new NoteClickRecord();
        record.setNoteId(noteId);
        record.setUserId(userId);
        clickRecordMapper.insert(record);
        return success();
    }
}
