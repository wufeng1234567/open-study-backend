package com.openstudy.favoriteBank.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.openstudy.common.annotation.Excel;
import com.openstudy.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * 用户题库收藏对象 user_bank_favorite
 * 
 * @author liu
 * @date 2025-12-09
 */
public class UserBankFavorite extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 收藏ID */
    private Long favoriteId;

    /** 用户ID（关联sys_user.user_id） */
    @Excel(name = "用户ID", readConverterExp = "关=联sys_user.user_id")
    private Long userId;

    /** 题库ID（关联question_bank.id） */
    @Excel(name = "题库ID", readConverterExp = "关=联question_bank.id")
    private Long bankId;

    /** 收藏备注 */
    @Excel(name = "收藏备注")
    private String notes;

    /** 标签（逗号分隔，如：高频,重点） */
    @Excel(name = "标签", readConverterExp = "逗=号分隔，如：高频,重点")
    private String tags;

    /** 排序（用户自定义） */
    @Excel(name = "排序", readConverterExp = "用=户自定义")
    private Long sortOrder;

    /** 是否标星：0否 1是 */
    @Excel(name = "是否标星：0否 1是")
    private Long isStarred;

    /** 最后学习时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "最后学习时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date lastStudyTime;

    /** 学习次数 */
    @Excel(name = "学习次数")
    private Long studyCount;

    /** 状态：1正常收藏 0取消收藏 */
    @Excel(name = "状态：1正常收藏 0取消收藏")
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

    public void setSortOrder(Long sortOrder) 
    {
        this.sortOrder = sortOrder;
    }

    public Long getSortOrder() 
    {
        return sortOrder;
    }

    public void setIsStarred(Long isStarred) 
    {
        this.isStarred = isStarred;
    }

    public Long getIsStarred() 
    {
        return isStarred;
    }

    public void setLastStudyTime(Date lastStudyTime) 
    {
        this.lastStudyTime = lastStudyTime;
    }

    public Date getLastStudyTime() 
    {
        return lastStudyTime;
    }

    public void setStudyCount(Long studyCount) 
    {
        this.studyCount = studyCount;
    }

    public Long getStudyCount() 
    {
        return studyCount;
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
            .append("bankId", getBankId())
            .append("notes", getNotes())
            .append("tags", getTags())
            .append("sortOrder", getSortOrder())
            .append("isStarred", getIsStarred())
            .append("lastStudyTime", getLastStudyTime())
            .append("studyCount", getStudyCount())
            .append("favoriteStatus", getFavoriteStatus())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
