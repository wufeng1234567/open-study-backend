package com.openstudy.wordBooks.controller;

import com.openstudy.common.annotation.Log;
import com.openstudy.common.core.controller.BaseController;
import com.openstudy.common.core.domain.AjaxResult;
import com.openstudy.common.core.page.TableDataInfo;
import com.openstudy.common.enums.BusinessType;
import com.openstudy.common.utils.poi.ExcelUtil;
import com.openstudy.wordBooks.domain.WordBooks;
import com.openstudy.wordBooks.service.IWordBooksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 单词本Controller
 * 
 * @author liu
 * @date 2025-10-24
 */
@RestController
@RequestMapping("/wordBooks/wordBooks")
@PreAuthorize("@ss.hasRole('common')")
public class WordBooksController extends BaseController
{
    @Autowired
    private IWordBooksService wordBooksService;

    /**
     * 查询单词本列表
     */
    @GetMapping("/list")
    public TableDataInfo list(WordBooks wordBooks)
    {
        startPage();
        List<WordBooks> list = wordBooksService.selectWordBooksList(wordBooks);
        return getDataTable(list);
    }

    /**
     * 导出单词本列表
     */
    @Log(title = "单词本", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, WordBooks wordBooks)
    {
        List<WordBooks> list = wordBooksService.selectWordBooksList(wordBooks);
        ExcelUtil<WordBooks> util = new ExcelUtil<WordBooks>(WordBooks.class);
        util.exportExcel(response, list, "单词本数据");
    }

    /**
     * 获取单词本详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(wordBooksService.selectWordBooksById(id));
    }

    /**
     * 新增单词本
     */
    @Log(title = "单词本", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody WordBooks wordBooks)
    {
        return toAjax(wordBooksService.insertWordBooks(wordBooks));
    }

    /**
     * 修改单词本
     */
    @Log(title = "单词本", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody WordBooks wordBooks)
    {
        return toAjax(wordBooksService.updateWordBooks(wordBooks));
    }

    /**
     * 删除单词本
     */
    @Log(title = "单词本", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(wordBooksService.deleteWordBooksByIds(ids));
    }


    /**
     * 前台查询用户可见的单词本列表
     * 用户可以看到：自己创建的 + 默认单词本
     */
    @GetMapping("/front/list")
    public TableDataInfo frontList(WordBooks wordBooks)
    {
        startPage();
        List<WordBooks> list = wordBooksService.selectFrontWordBooksList(wordBooks);
        return getDataTable(list);
    }

}
