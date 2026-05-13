package com.openstudy.notes.controller;

import com.openstudy.common.annotation.Log;
import com.openstudy.common.core.controller.BaseController;
import com.openstudy.common.core.domain.AjaxResult;
import com.openstudy.common.core.page.TableDataInfo;
import com.openstudy.common.enums.BusinessType;
import com.openstudy.common.utils.poi.ExcelUtil;
import com.openstudy.notes.domain.Note;
import com.openstudy.notes.service.INoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 用户笔记主，每篇笔记必须归属于一个用户自定义的分类Controller
 * 
 * @author liu
 * @date 2025-12-02
 */
@RestController
@RequestMapping("/notes/note")
@PreAuthorize("@ss.hasRole('common')")
public class NoteController extends BaseController {
    @Autowired
    private INoteService noteService;

    /**
     * 查询用户笔记主，每篇笔记必须归属于一个用户自定义的分类列表
     */
    // @PreAuthorize("@ss.hasPermi('notes:note:list')")
    @GetMapping("/list")
    public TableDataInfo list(Note note) {
        startPage();
        List<Note> list = noteService.selectNoteList(note);
        return getDataTable(list);
    }

    /**
     * 导出用户笔记主，每篇笔记必须归属于一个用户自定义的分类列表
     */
    // @PreAuthorize("@ss.hasPermi('notes:note:export')")
    @Log(title = "用户笔记主，每篇笔记必须归属于一个用户自定义的分类", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Note note) {
        List<Note> list = noteService.selectNoteList(note);
        ExcelUtil<Note> util = new ExcelUtil<Note>(Note.class);
        util.exportExcel(response, list, "用户笔记主，每篇笔记必须归属于一个用户自定义的分类数据");
    }

    /**
     * 获取用户笔记主，每篇笔记必须归属于一个用户自定义的分类详细信息
     */
    // @PreAuthorize("@ss.hasPermi('notes:note:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(noteService.selectNoteById(id));
    }

    /**
     * 新增用户笔记主，每篇笔记必须归属于一个用户自定义的分类
     */
    // @PreAuthorize("@ss.hasPermi('notes:note:add')")
    @Log(title = "用户笔记主，每篇笔记必须归属于一个用户自定义的分类", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Note note) {
        noteService.insertNote(note);
        return success(note.getId());
    }

    /**
     * 修改用户笔记主，每篇笔记必须归属于一个用户自定义的分类
     */
    // @PreAuthorize("@ss.hasPermi('notes:note:edit')")
    @Log(title = "用户笔记主，每篇笔记必须归属于一个用户自定义的分类", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Note note) {
        return toAjax(noteService.updateNote(note));
    }

    /**
     * 删除用户笔记主，每篇笔记必须归属于一个用户自定义的分类
     */
    // @PreAuthorize("@ss.hasPermi('notes:note:remove')")
    @Log(title = "用户笔记主，每篇笔记必须归属于一个用户自定义的分类", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(noteService.deleteNoteByIds(ids));
    }
}
