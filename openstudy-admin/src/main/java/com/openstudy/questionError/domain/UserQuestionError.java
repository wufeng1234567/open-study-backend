package com.openstudy.questionError.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.openstudy.common.annotation.Excel;
import com.openstudy.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.ToString;

import java.util.Date;

/**
 * 用户错题记录（支持复习与掌握跟踪）对象 user_question_error
 * 
 * @author liu
 * @date 2025-12-16
 */
@Data
@ToString
public class UserQuestionError extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 错题记录ID */
    private Long errorId;

    /** 用户ID（关联sys_user.user_id） */
    @Excel(name = "用户ID", readConverterExp = "关=联sys_user.user_id")
    private Long userId;

    /** 题目ID（关联question_main.id） */
    @Excel(name = "题目ID", readConverterExp = "关=联question_main.id")
    private Long questionId;

    /** 所属题库ID（冗余字段，提升查询性能） */
    @Excel(name = "所属题库ID", readConverterExp = "冗=余字段，提升查询性能")
    private Long bankId;

    /** 首次做错时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "首次做错时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date errorTime;

    /** 最近一次做错时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "最近一次做错时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date lastErrorTime;

    /** 累计错误次数 */
    @Excel(name = "累计错误次数")
    private Long errorCount;

    /** 错因备注（如：概念不清、粗心、不会） */
    @Excel(name = "错因备注", readConverterExp = "如=：概念不清、粗心、不会")
    private String notes;

    /** 标签（逗号分隔，如：易错,公式记错,审题不清） */
    @Excel(name = "标签", readConverterExp = "逗=号分隔，如：易错,公式记错,审题不清")
    private String tags;

    /** 用户自评难度：1-5星 */
    @Excel(name = "用户自评难度：1-5星")
    private Long difficultyRating;

    /** 已复习次数 */
    @Excel(name = "已复习次数")
    private Long reviewCount;

    /** 最后复习时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "最后复习时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date lastReviewTime;

    /** 下次复习时间（基于记忆曲线） */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "下次复习时间", readConverterExp = "基=于记忆曲线")
    private Date nextReviewTime;

    /** 是否已掌握：0未掌握 1已掌握 */
    @Excel(name = "是否已掌握：0未掌握 1已掌握")
    private Long isMastered;

    /** 记录状态：1活跃（错题中） 2已掌握归档 0无效/删除 */
    @Excel(name = "记录状态：1活跃", readConverterExp = "错=题中")
    private Long status;


}
