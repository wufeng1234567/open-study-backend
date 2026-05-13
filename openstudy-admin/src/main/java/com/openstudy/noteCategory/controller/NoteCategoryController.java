package com.openstudy.noteCategory.controller;

import com.openstudy.common.annotation.Log;
import com.openstudy.common.core.controller.BaseController;
import com.openstudy.common.core.domain.AjaxResult;
import com.openstudy.common.core.page.TableDataInfo;
import com.openstudy.common.enums.BusinessType;
import com.openstudy.common.utils.poi.ExcelUtil;
import com.openstudy.noteCategory.domain.NoteCategory;
import com.openstudy.noteCategory.service.INoteCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 笔记分类，支持用户自定义单层分类，每个分类下可包含多篇笔记Controller
 * 
 * @author ruoyi
 * @date 2025-12-02
 */
@RestController
@RequestMapping("/noteCategory/noteCategory")
@PreAuthorize("@ss.hasRole('common')")
public class NoteCategoryController extends BaseController
{
    @Autowired
    private INoteCategoryService noteCategoryService;

    /**
     * 查询笔记分类，支持用户自定义单层分类，每个分类下可包含多篇笔记列表
     */
    // @PreAuthorize("@ss.hasPermi('noteCategory:noteCategory:list')")
    @GetMapping("/list")
    public TableDataInfo list(NoteCategory noteCategory)
    {
        startPage();
        List<NoteCategory> list = noteCategoryService.selectNoteCategoryList(noteCategory);
        return getDataTable(list);
    }
    @GetMapping("/all")
    public AjaxResult getAll(NoteCategory noteCategory) {
        // 不调用 startPage() → 不分页
        List<NoteCategory> list = noteCategoryService.selectNoteCategoryList(noteCategory);
        return success(list);
    }

    /**
     * 导出笔记分类，支持用户自定义单层分类，每个分类下可包含多篇笔记列表
     */
    // @PreAuthorize("@ss.hasPermi('noteCategory:noteCategory:export')")
    @Log(title = "笔记分类，支持用户自定义单层分类，每个分类下可包含多篇笔记", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, NoteCategory noteCategory)
    {
        List<NoteCategory> list = noteCategoryService.selectNoteCategoryList(noteCategory);
        ExcelUtil<NoteCategory> util = new ExcelUtil<NoteCategory>(NoteCategory.class);
        util.exportExcel(response, list, "笔记分类，支持用户自定义单层分类，每个分类下可包含多篇笔记数据");
    }

    /**
     * 获取笔记分类，支持用户自定义单层分类，每个分类下可包含多篇笔记详细信息
     */
    // @PreAuthorize("@ss.hasPermi('noteCategory:noteCategory:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(noteCategoryService.selectNoteCategoryById(id));
    }

    /**
     * 新增笔记分类，支持用户自定义单层分类，每个分类下可包含多篇笔记
     */
    // @PreAuthorize("@ss.hasPermi('noteCategory:noteCategory:add')")
    @Log(title = "笔记分类，支持用户自定义单层分类，每个分类下可包含多篇笔记", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody NoteCategory noteCategory) {

        int rows = noteCategoryService.insertNoteCategory(noteCategory);
        if (rows > 0 && noteCategory.getId() != null) {
            return AjaxResult.success(noteCategory.getId()); // ✅ 安全返回 ID
        }
        return AjaxResult.error("新增失败");
    }

    /**
     * 修改笔记分类，支持用户自定义单层分类，每个分类下可包含多篇笔记
     */
    // @PreAuthorize("@ss.hasPermi('noteCategory:noteCategory:edit')")
    @Log(title = "笔记分类，支持用户自定义单层分类，每个分类下可包含多篇笔记", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody NoteCategory noteCategory)
    {
        return toAjax(noteCategoryService.updateNoteCategory(noteCategory));
    }

    /**
     * 删除笔记分类，支持用户自定义单层分类，每个分类下可包含多篇笔记
     */
    // @PreAuthorize("@ss.hasPermi('noteCategory:noteCategory:remove')")
    @Log(title = "笔记分类，支持用户自定义单层分类，每个分类下可包含多篇笔记", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(noteCategoryService.deleteNoteCategoryByIds(ids));
    }
}
