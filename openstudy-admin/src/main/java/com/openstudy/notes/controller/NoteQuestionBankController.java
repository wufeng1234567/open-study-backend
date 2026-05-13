package com.openstudy.notes.controller;

import com.openstudy.common.annotation.Log;
import com.openstudy.common.core.controller.BaseController;
import com.openstudy.common.core.domain.AjaxResult;
import com.openstudy.common.enums.BusinessType;
import com.openstudy.common.utils.SecurityUtils;
import com.openstudy.notes.domain.NoteQuestionBank;
import com.openstudy.notes.service.INoteQuestionBankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/noteQuestionBank/noteQuestionBank")
public class NoteQuestionBankController extends BaseController {
    @Autowired
    private INoteQuestionBankService noteQuestionBankService;

    @PreAuthorize("@ss.hasRole('common')")
    @GetMapping("/list")
    public AjaxResult list(NoteQuestionBank noteQuestionBank) {
        List<NoteQuestionBank> list = noteQuestionBankService.selectNoteQuestionBankList(noteQuestionBank);
        return success(list);
    }

    @PreAuthorize("@ss.hasRole('common')")
    @GetMapping("/byNote/{noteId}")
    public AjaxResult getByNoteId(@PathVariable Long noteId) {
        List<NoteQuestionBank> list = noteQuestionBankService.selectByNoteId(noteId);
        return success(list);
    }

    @PreAuthorize("@ss.hasRole('common')")
    @GetMapping("/byBank/{bankId}")
    public AjaxResult getByBankId(@PathVariable Long bankId) {
        List<NoteQuestionBank> list = noteQuestionBankService.selectByBankId(bankId);
        return success(list);
    }

    @PreAuthorize("@ss.hasRole('common')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(noteQuestionBankService.selectNoteQuestionBankById(id));
    }

    @PreAuthorize("@ss.hasRole('common')")
    @Log(title = "笔记题库关联", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody NoteQuestionBank noteQuestionBank) {
        noteQuestionBank.setUserId(SecurityUtils.getUserId());
        return toAjax(noteQuestionBankService.insertNoteQuestionBank(noteQuestionBank));
    }

    @PreAuthorize("@ss.hasRole('common')")
    @Log(title = "笔记题库关联", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody NoteQuestionBank noteQuestionBank) {
        return toAjax(noteQuestionBankService.updateNoteQuestionBank(noteQuestionBank));
    }

    @PreAuthorize("@ss.hasRole('common')")
    @Log(title = "笔记题库关联", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(noteQuestionBankService.deleteNoteQuestionBankByIds(ids));
    }

    @PreAuthorize("@ss.hasRole('common')")
    @DeleteMapping("/note/{noteId}/bank/{bankId}")
    public AjaxResult removeByNoteAndBank(@PathVariable Long noteId, @PathVariable Long bankId) {
        return toAjax(noteQuestionBankService.deleteByNoteIdAndBankId(noteId, bankId));
    }
}
