package com.openstudy.words.controller;

import com.openstudy.common.annotation.Log;
import com.openstudy.common.core.controller.BaseController;
import com.openstudy.common.core.domain.AjaxResult;
import com.openstudy.common.core.page.TableDataInfo;
import com.openstudy.common.enums.BusinessType;
import com.openstudy.common.utils.poi.ExcelUtil;
import com.openstudy.words.domain.Words;
import com.openstudy.words.service.IWordsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 单词Controller
 *
 * @author liu
 * @date 2025-10-24
 */
@RestController
@RequestMapping("/words/words")
@PreAuthorize("@ss.hasRole('common')")
public class WordsController extends BaseController
{
    @Autowired
    private IWordsService wordsService;

    /**
     * 查询单词列表
     */
    @GetMapping("/list")
    public TableDataInfo list(Words words)
    {
        startPage();
        List<Words> list = wordsService.selectWordsList(words);
        return getDataTable(list);
    }

    /**
     * 导出单词列表
     */
    @Log(title = "单词", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Words words)
    {
        List<Words> list = wordsService.selectWordsList(words);
        ExcelUtil<Words> util = new ExcelUtil<Words>(Words.class);
        util.exportExcel(response, list, "单词数据");
    }

    /**
     * 获取单词详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(wordsService.selectWordsById(id));
    }

    /**
     * 新增单词
     */
    @Log(title = "单词", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Words words)
    {
        return toAjax(wordsService.insertWords(words));
    }

    /**
     * 批量新增单词
     */
    @Log(title = "单词", businessType = BusinessType.INSERT)
    @PostMapping("/batch")
    public AjaxResult batchAdd(@RequestBody List<Words> wordsList)
    {
        int successCount = 0;
        for (Words words : wordsList) {
            try {
                if (wordsService.insertWords(words) > 0) {
                    successCount++;
                }
            } catch (Exception e) {
                // 记录日志，继续处理其他单词
                logger.error("批量添加单词失败: " + e.getMessage(), e);
            }
        }
        return success("成功添加 " + successCount + " 个单词，失败 " + (wordsList.size() - successCount) + " 个");
    }

    /**
     * 修改单词
     */
    @Log(title = "单词", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Words words)
    {
        return toAjax(wordsService.updateWords(words));
    }

    /**
     * 删除单词
     */
    @Log(title = "单词", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(wordsService.deleteWordsByIds(ids));
    }
}