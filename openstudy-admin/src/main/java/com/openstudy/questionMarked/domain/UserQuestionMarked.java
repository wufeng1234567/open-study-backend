package com.openstudy.questionMarked.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.openstudy.common.annotation.Excel;
import com.openstudy.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * 用户斩题（重点攻克题目）对象 user_question_marked
 * 
 * @author ruoyi
 * @date 2025-12-17
 */
public class UserQuestionMarked extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 斩题ID */
    private Long markedId;

    /** 用户ID（关联sys_user.user_id） */
    @Excel(name = "用户ID", readConverterExp = "关=联sys_user.user_id")
    private Long userId;

    /** 题目ID（关联question_main.id） */
    @Excel(name = "题目ID", readConverterExp = "关=联question_main.id")
    private Long questionId;

    /** 所属题库ID（冗余字段，方便查询） */
    @Excel(name = "所属题库ID", readConverterExp = "冗=余字段，方便查询")
    private Long bankId;

    /** 斩题备注（如：解题思路,技巧总结,易错点） */
    @Excel(name = "斩题备注", readConverterExp = "如=：解题思路,技巧总结,易错点")
    private String notes;

    /** 标签（逗号分隔，如：难题,重点,易错,技巧） */
    @Excel(name = "标签", readConverterExp = "逗=号分隔，如：难题,重点,易错,技巧")
    private String tags;

    /** 斩题类型：1错题 2难题 3重点 4易错 5技巧 */
    @Excel(name = "斩题类型：1错题 2难题 3重点 4易错 5技巧")
    private Long markedType;

    /** 难度等级：1简单 2中等 3困难 4极难 */
    @Excel(name = "难度等级：1简单 2中等 3困难 4极难")
    private Long difficultyLevel;

    /** 斩题前的错误次数 */
    @Excel(name = "斩题前的错误次数")
    private Long errorTimesBeforeMark;

    /** 斩题前的正确次数 */
    @Excel(name = "斩题前的正确次数")
    private Long correctTimesBeforeMark;

    /** 复习次数 */
    @Excel(name = "复习次数")
    private Long reviewCount;

    /** 最后复习时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "最后复习时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date lastReviewTime;

    /** 下次复习时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "下次复习时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date nextReviewTime;

    /** 是否掌握：0未掌握 1已掌握 */
    @Excel(name = "是否掌握：0未掌握 1已掌握")
    private Long isMastered;

    /** 掌握日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "掌握日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date masteryDate;

    /** 状态：1正常 0删除 */
    @Excel(name = "状态：1正常 0删除")
    private Long markedStatus;

    public void setMarkedId(Long markedId) 
    {
        this.markedId = markedId;
    }

    public Long getMarkedId() 
    {
        return markedId;
    }

    public void setUserId(Long userId) 
    {
        this.userId = userId;
    }

    public Long getUserId() 
    {
        return userId;
    }

    public void setQuestionId(Long questionId) 
    {
        this.questionId = questionId;
    }

    public Long getQuestionId() 
    {
        return questionId;
    }

    public void setBankId(Long bankId) 
    {
        this.bankId = bankId;
    }

    public Long getBankId() 
    {
        return bankId;
    }

    public void setNotes(String notes) 
    {
        this.notes = notes;
    }

    public String getNotes() 
    {
        return notes;
    }

    public void setTags(String tags) 
    {
        this.tags = tags;
    }

    public String getTags() 
    {
        return tags;
    }

    public void setMarkedType(Long markedType) 
    {
        this.markedType = markedType;
    }

    public Long getMarkedType() 
    {
        return markedType;
    }

    public void setDifficultyLevel(Long difficultyLevel) 
    {
        this.difficultyLevel = difficultyLevel;
    }

    public Long getDifficultyLevel() 
    {
        return difficultyLevel;
    }

    public void setErrorTimesBeforeMark(Long errorTimesBeforeMark) 
    {
        this.errorTimesBeforeMark = errorTimesBeforeMark;
    }

    public Long getErrorTimesBeforeMark() 
    {
        return errorTimesBeforeMark;
    }

    public void setCorrectTimesBeforeMark(Long correctTimesBeforeMark) 
    {
        this.correctTimesBeforeMark = correctTimesBeforeMark;
    }

    public Long getCorrectTimesBeforeMark() 
    {
        return correctTimesBeforeMark;
    }

    public void setReviewCount(Long reviewCount) 
    {
        this.reviewCount = reviewCount;
    }

    public Long getReviewCount() 
    {
        return reviewCount;
    }

    public void setLastReviewTime(Date lastReviewTime) 
    {
        this.lastReviewTime = lastReviewTime;
    }

    public Date getLastReviewTime() 
    {
        return lastReviewTime;
    }

    public void setNextReviewTime(Date nextReviewTime) 
    {
        this.nextReviewTime = nextReviewTime;
    }

    public Date getNextReviewTime() 
    {
        return nextReviewTime;
    }

    public void setIsMastered(Long isMastered) 
    {
        this.isMastered = isMastered;
    }

    public Long getIsMastered() 
    {
        return isMastered;
    }

    public void setMasteryDate(Date masteryDate) 
    {
        this.masteryDate = masteryDate;
    }

    public Date getMasteryDate() 
    {
        return masteryDate;
    }

    public void setMarkedStatus(Long markedStatus) 
    {
        this.markedStatus = markedStatus;
    }

    public Long getMarkedStatus() 
    {
        return markedStatus;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("markedId", getMarkedId())
            .append("userId", getUserId())
            .append("questionId", getQuestionId())
            .append("bankId", getBankId())
            .append("notes", getNotes())
            .append("tags", getTags())
            .append("markedType", getMarkedType())
            .append("difficultyLevel", getDifficultyLevel())
            .append("errorTimesBeforeMark", getErrorTimesBeforeMark())
            .append("correctTimesBeforeMark", getCorrectTimesBeforeMark())
            .append("reviewCount", getReviewCount())
            .append("lastReviewTime", getLastReviewTime())
            .append("nextReviewTime", getNextReviewTime())
            .append("isMastered", getIsMastered())
            .append("masteryDate", getMasteryDate())
            .append("markedStatus", getMarkedStatus())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
