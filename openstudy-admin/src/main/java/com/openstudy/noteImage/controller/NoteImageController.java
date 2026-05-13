package com.openstudy.noteImage.controller;

import com.openstudy.common.annotation.Log;
import com.openstudy.common.core.controller.BaseController;
import com.openstudy.common.core.domain.AjaxResult;
import com.openstudy.common.core.page.TableDataInfo;
import com.openstudy.common.enums.BusinessType;
import com.openstudy.common.utils.poi.ExcelUtil;
import com.openstudy.noteImage.domain.NoteImage;
import com.openstudy.noteImage.service.INoteImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 笔记关联的图片资源Controller
 * 
 * @author ruoyi
 * @date 2025-12-02
 */
@RestController
@RequestMapping("/noteImage/noteImage")
@PreAuthorize("@ss.hasRole('common')")

public class NoteImageController extends BaseController
{
    @Autowired
    private INoteImageService noteImageService;

    /**
     * 查询笔记关联的图片资源列表
     */
    // @PreAuthorize("@ss.hasPermi('noteImage:noteImage:list')")
    @GetMapping("/list")
    public TableDataInfo list(NoteImage noteImage)
    {
        startPage();
        List<NoteImage> list = noteImageService.selectNoteImageList(noteImage);
        return getDataTable(list);
    }

    /**
     * 导出笔记关联的图片资源列表
     */
    // @PreAuthorize("@ss.hasPermi('noteImage:noteImage:export')")
    @Log(title = "笔记关联的图片资源", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, NoteImage noteImage)
    {
        List<NoteImage> list = noteImageService.selectNoteImageList(noteImage);
        ExcelUtil<NoteImage> util = new ExcelUtil<NoteImage>(NoteImage.class);
        util.exportExcel(response, list, "笔记关联的图片资源数据");
    }

    /**
     * 获取笔记关联的图片资源详细信息
     */
    // @PreAuthorize("@ss.hasPermi('noteImage:noteImage:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(noteImageService.selectNoteImageById(id));
    }

    /**
     * 新增笔记关联的图片资源
     */
    // @PreAuthorize("@ss.hasPermi('noteImage:noteImage:add')")
    @Log(title = "笔记关联的图片资源", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody NoteImage noteImage)
    {
        return toAjax(noteImageService.insertNoteImage(noteImage));
    }

    /**
     * 修改笔记关联的图片资源
     */
    // @PreAuthorize("@ss.hasPermi('noteImage:noteImage:edit')")
    @Log(title = "笔记关联的图片资源", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody NoteImage noteImage)
    {
        return toAjax(noteImageService.updateNoteImage(noteImage));
    }

    /**
     * 删除笔记关联的图片资源
     */
    // @PreAuthorize("@ss.hasPermi('noteImage:noteImage:remove')")
    @Log(title = "笔记关联的图片资源", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(noteImageService.deleteNoteImageByIds(ids));
    }
}
