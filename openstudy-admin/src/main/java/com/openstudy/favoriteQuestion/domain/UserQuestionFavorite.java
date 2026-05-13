package com.openstudy.favoriteQuestion.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.openstudy.common.annotation.Excel;
import com.openstudy.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * 用户题目收藏（支持复习功能）对象 user_question_favorite
 * 
 * @author liu
 * @date 2025-12-10
 */
public class UserQuestionFavorite extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 收藏ID */
    private Long favoriteId;

    /** 用户ID（关联sys_user.user_id） */
    @Excel(name = "用户ID", readConverterExp = "关=联sys_user.user_id")
    private Long userId;

    /** 题目ID（关联question_main.id） */
    @Excel(name = "题目ID", readConverterExp = "关=联question_main.id")
    private Long questionId;

    /** 所属题库ID（冗余字段，方便查询） */
    @Excel(name = "所属题库ID", readConverterExp = "冗=余字段，方便查询")
    private Long bankId;

    /** 收藏备注（如：易错点,重要考点） */
    @Excel(name = "收藏备注", readConverterExp = "如=：易错点,重要考点")
    private String notes;

    /** 标签（逗号分隔，如：易错,高频,难点） */
    @Excel(name = "标签", readConverterExp = "逗=号分隔，如：易错,高频,难点")
    private String tags;

    /** 难度评级：1-5星 */
    @Excel(name = "难度评级：1-5星")
    private Long difficultyRating;

    /** 错误次数 */
    @Excel(name = "错误次数")
    private Long errorTimes;

    /** 正确次数 */
    @Excel(name = "正确次数")
    private Long correctTimes;

    /** 最后复习时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "最后复习时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date lastReviewTime;

    /** 下次复习时间（记忆曲线） */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "下次复习时间", readConverterExp = "记=忆曲线")
    private Date nextReviewTime;

    /** 复习次数 */
    @Excel(name = "复习次数")
    private Long reviewCount;

    /** 是否标星：0否 1是（重点题目） */
    @Excel(name = "是否标星：0否 1是", readConverterExp = "重=点题目")
    private Long isStarred;

    /** 状态：1正常收藏 2已掌握 3待复习 0取消收藏 */
    @Excel(name = "状态：1正常收藏 2已掌握 3待复习 0取消收藏")
    private Long favoriteStatus;

    public void setFavoriteId(Long favoriteId) 
    {
        this.favoriteId = favoriteId;
    }

    public Long getFavoriteId() 
    {
        return favoriteId;
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

    public void setDifficultyRating(Long difficultyRating) 
    {
        this.difficultyRating = difficultyRating;
    }

    public Long getDifficultyRating() 
    {
        return difficultyRating;
    }

    public void setErrorTimes(Long errorTimes) 
    {
        this.errorTimes = errorTimes;
    }

    public Long getErrorTimes() 
    {
        return errorTimes;
    }

    public void setCorrectTimes(Long correctTimes) 
    {
        this.correctTimes = correctTimes;
    }

    public Long getCorrectTimes() 
    {
        return correctTimes;
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

    public void setReviewCount(Long reviewCount) 
    {
        this.reviewCount = reviewCount;
    }

    public Long getReviewCount() 
    {
        return reviewCount;
    }

    public void setIsStarred(Long isStarred) 
    {
        this.isStarred = isStarred;
    }

    public Long getIsStarred() 
    {
        return isStarred;
    }

    public void setFavoriteStatus(Long favoriteStatus) 
    {
        this.favoriteStatus = favoriteStatus;
    }

    public Long getFavoriteStatus() 
    {
        return favoriteStatus;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("favoriteId", getFavoriteId())
            .append("userId", getUserId())
            .append("questionId", getQuestionId())
            .append("bankId", getBankId())
            .append("notes", getNotes())
            .append("tags", getTags())
            .append("difficultyRating", getDifficultyRating())
            .append("errorTimes", getErrorTimes())
            .append("correctTimes", getCorrectTimes())
            .append("lastReviewTime", getLastReviewTime())
            .append("nextReviewTime", getNextReviewTime())
            .append("reviewCount", getReviewCount())
            .append("isStarred", getIsStarred())
            .append("favoriteStatus", getFavoriteStatus())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
