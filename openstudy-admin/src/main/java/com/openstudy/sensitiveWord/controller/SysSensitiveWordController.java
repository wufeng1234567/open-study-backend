package com.openstudy.sensitiveWord.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.openstudy.common.annotation.Log;
import com.openstudy.common.core.controller.BaseController;
import com.openstudy.common.core.domain.AjaxResult;
import com.openstudy.common.enums.BusinessType;
import com.openstudy.sensitiveWord.domain.SysSensitiveWord;
import com.openstudy.sensitiveWord.service.ISysSensitiveWordService;
import com.openstudy.common.utils.poi.ExcelUtil;
import com.openstudy.common.core.page.TableDataInfo;

/**
 * 敏感词管理Controller
 * 
 * @author liu
 * @date 2026-04-17
 */
@RestController
@RequestMapping("/sensitiveWord/sensitiveWord")
public class SysSensitiveWordController extends BaseController
{
    @Autowired
    private ISysSensitiveWordService sysSensitiveWordService;

    /**
     * 查询敏感词管理列表
     */
    @PreAuthorize("@ss.hasPermi('sensitiveWord:sensitiveWord:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysSensitiveWord sysSensitiveWord)
    {
        startPage();
        List<SysSensitiveWord> list = sysSensitiveWordService.selectSysSensitiveWordList(sysSensitiveWord);
        return getDataTable(list);
    }

    /**
     * 导出敏感词管理列表
     */
    @PreAuthorize("@ss.hasPermi('sensitiveWord:sensitiveWord:export')")
    @Log(title = "敏感词管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysSensitiveWord sysSensitiveWord)
    {
        List<SysSensitiveWord> list = sysSensitiveWordService.selectSysSensitiveWordList(sysSensitiveWord);
        ExcelUtil<SysSensitiveWord> util = new ExcelUtil<SysSensitiveWord>(SysSensitiveWord.class);
        util.exportExcel(response, list, "敏感词管理数据");
    }

    /**
     * 获取敏感词管理详细信息
     */
    @PreAuthorize("@ss.hasPermi('sensitiveWord:sensitiveWord:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(sysSensitiveWordService.selectSysSensitiveWordById(id));
    }

    /**
     * 新增敏感词管理
     */
    @PreAuthorize("@ss.hasPermi('sensitiveWord:sensitiveWord:add')")
    @Log(title = "敏感词管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SysSensitiveWord sysSensitiveWord)
    {
        return toAjax(sysSensitiveWordService.insertSysSensitiveWord(sysSensitiveWord));
    }

    /**
     * 修改敏感词管理
     */
    @PreAuthorize("@ss.hasPermi('sensitiveWord:sensitiveWord:edit')")
    @Log(title = "敏感词管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SysSensitiveWord sysSensitiveWord)
    {
        return toAjax(sysSensitiveWordService.updateSysSensitiveWord(sysSensitiveWord));
    }

    /**
     * 删除敏感词管理
     */
    @PreAuthorize("@ss.hasPermi('sensitiveWord:sensitiveWord:remove')")
    @Log(title = "敏感词管理", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(sysSensitiveWordService.deleteSysSensitiveWordByIds(ids));
    }

    /**
     * AI生成敏感词
     */
    @PreAuthorize("@ss.hasPermi('sensitiveWord:sensitiveWord:add')")
    @Log(title = "AI生成敏感词", businessType = BusinessType.INSERT)
    @PostMapping("/aiGenerate")
    public AjaxResult aiGenerate(@RequestBody AiGenerateRequest request) {
        if (request.getTopic() == null || request.getTopic().trim().isEmpty()) {
            return error("主题不能为空");
        }
        if (request.getCategory() == null || request.getCategory().trim().isEmpty()) {
            return error("分类不能为空");
        }

        int count = request.getCount() != null ? request.getCount() : 20;
        if (count < 5) count = 20;
        if (count > 50) count = 50;

        // 调用 Service 时传递 3 个参数
        int addedCount = sysSensitiveWordService.aiGenerateWords(
                request.getTopic().trim(),
                request.getCategory().trim(),
                count
        );

        return success("成功生成 " + addedCount + " 个敏感词");
    }

    // 请求内部类
    @lombok.Data
    public static class AiGenerateRequest {
        @jakarta.validation.constraints.NotBlank(message = "主题不能为空")
        private String topic;
        @jakarta.validation.constraints.NotBlank(message = "分类不能为空")
        private String category;
        private Integer count = 20;
    }
}
