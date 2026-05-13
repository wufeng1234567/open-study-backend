package com.openstudy.questionBank.domain;

import com.openstudy.common.annotation.Excel;
import com.openstudy.common.core.domain.BaseEntity;
import lombok.Data;

@Data
public class QuestionBank extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @Excel(name = "题库ID")
    private Long id;

    @Excel(name = "题库名称")
    private String bankName;

    @Excel(name = "科目")
    private String subject;

    @Excel(name = "题库描述")
    private String description;

    @Excel(name = "封面图")
    private String coverImage;

    @Excel(name = "题目总数")
    private Long totalQuestions;

    @Excel(name = "状态")
    private String status;

    /** 创建用户ID */
    private Long userId;

    /** ✅ 新增：是否公开 0=公有 1=私有 */
    @Excel(name = "是否公开", readConverterExp = "0=公有,1=私有")
    private Integer isPublic;

    // getter/setter 由 Lombok 自动生成
}