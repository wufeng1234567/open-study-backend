package com.openstudy.notes.domain;

import com.openstudy.common.annotation.Excel;
import com.openstudy.common.core.domain.BaseEntity;
import lombok.Data;

@Data
public class NoteQuestionBank extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @Excel(name = "主键ID")
    private Long id;

    @Excel(name = "笔记ID")
    private Long noteId;

    @Excel(name = "题库ID")
    private Long questionBankId;

    @Excel(name = "用户ID")
    private Long userId;

    @Excel(name = "题库名称")
    private String bankName;

    @Excel(name = "笔记标题")
    private String noteTitle;
}
