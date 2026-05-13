package com.openstudy.questionBank.controller;

import com.openstudy.common.annotation.Log;
import com.openstudy.common.core.controller.BaseController;
import com.openstudy.common.core.domain.AjaxResult;
import com.openstudy.common.core.page.TableDataInfo;
import com.openstudy.common.enums.BusinessType;
import com.openstudy.common.utils.SecurityUtils;
import com.openstudy.common.utils.poi.ExcelUtil;
import com.openstudy.questionBank.domain.QuestionBank;
import com.openstudy.questionBank.service.IQuestionBankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

@PreAuthorize("@ss.hasRole('common')")
@RestController
@RequestMapping("/questionBank/questionBank")
public class QuestionBankController extends BaseController
{
    @Autowired
    private IQuestionBankService questionBankService;

    @GetMapping("/list")
    public TableDataInfo list(QuestionBank questionBank)
    {
        startPage();
        List<QuestionBank> list = questionBankService.selectQuestionBankList(questionBank);
        return getDataTable(list);
    }

    @GetMapping("/all")
    public AjaxResult listAll(QuestionBank questionBank)
    {
        List<QuestionBank> list = questionBankService.selectQuestionBankList(questionBank);
        return success(list);
    }

    // ✅ 新增：获取当前用户的题库列表
    @GetMapping("/my")
    public AjaxResult myBanks()
    {
        Long userId = SecurityUtils.getUserId();
        List<QuestionBank> list = questionBankService.selectQuestionBankListByUserId(userId);
        return success(list);
    }

    @Log(title = "题库主", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, QuestionBank questionBank)
    {
        List<QuestionBank> list = questionBankService.selectQuestionBankList(questionBank);
        ExcelUtil<QuestionBank> util = new ExcelUtil<QuestionBank>(QuestionBank.class);
        util.exportExcel(response, list, "题库主数据");
    }

    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(questionBankService.selectQuestionBankById(id));
    }

    @Log(title = "题库主", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody QuestionBank questionBank)
    {
        // 自动填充当前用户ID
        questionBank.setUserId(SecurityUtils.getUserId());
        return toAjax(questionBankService.insertQuestionBank(questionBank));
    }

    @Log(title = "题库主", businessType = BusinessType.INSERT)
    @PostMapping("/createWithReturn")
    public AjaxResult createWithReturn(@RequestBody QuestionBank questionBank)
    {
        // 自动填充当前用户ID
        questionBank.setUserId(SecurityUtils.getUserId());
        questionBankService.insertQuestionBank(questionBank);
        return success(questionBank);
    }

    @Log(title = "题库主", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody QuestionBank questionBank)
    {
        // 可添加权限校验：只有创建者或管理员能修改
        return toAjax(questionBankService.updateQuestionBank(questionBank));
    }

    @Log(title = "题库主", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(questionBankService.deleteQuestionBankByIds(ids));
    }
}