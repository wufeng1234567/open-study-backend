package com.openstudy.words.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.openstudy.common.annotation.Excel;
import com.openstudy.common.core.domain.BaseEntity;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * 单词对象 words
 * 
 * @author liu
 * @date 2025-10-24
 */

@Data
public class Words extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 英文单词 */
    @Excel(name = "英文单词")
    private String english;

    /** 中文意思 */
    @Excel(name = "中文意思")
    private String chinese;

    /** 音标 */
    @Excel(name = "音标")
    private String phonetic;

    /** 所属单词本ID */
    @Excel(name = "所属单词本ID")
    private Long wordBookId;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "创建时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date createdTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "更新时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date updatedTime;

    /** 逻辑删除标识: 0-未删除, 1-已删除 */
    @Excel(name = "逻辑删除标识: 0-未删除, 1-已删除")
    private Long isDeleted;

    /** 删除时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "删除时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date deletedTime;

    /** 单词本名字*/
    private String wordBookName;

    /** 类型: 1-单词, 2-词组, 3-句子 */
    private Integer wordType;

    /** 是否掌握: 0-未掌握, 1-已掌握 */
    private Integer isMastered;

    /** 复习次数 */
    private Integer reviewCount;

    /** 最后复习时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastReviewTime;

    // 添加无参构造函数
    public Words() {
    }

    public Words(String wordBookName) {
        this.wordBookName = wordBookName;
    }

    public String getWordBookName() {
        return wordBookName;
    }

    public void setWordBookName(String wordsBookName) {
        this.wordBookName = wordsBookName;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setEnglish(String english) 
    {
        this.english = english;
    }

    public String getEnglish() 
    {
        return english;
    }

    public void setChinese(String chinese) 
    {
        this.chinese = chinese;
    }

    public String getChinese() 
    {
        return chinese;
    }

    public void setPhonetic(String phonetic) 
    {
        this.phonetic = phonetic;
    }

    public String getPhonetic() 
    {
        return phonetic;
    }

    public void setWordBookId(Long wordBookId) 
    {
        this.wordBookId = wordBookId;
    }

    public Long getWordBookId() 
    {
        return wordBookId;
    }

    public void setCreatedTime(Date createdTime) 
    {
        this.createdTime = createdTime;
    }

    public Date getCreatedTime() 
    {
        return createdTime;
    }

    public void setUpdatedTime(Date updatedTime) 
    {
        this.updatedTime = updatedTime;
    }

    public Date getUpdatedTime() 
    {
        return updatedTime;
    }

    public void setIsDeleted(Long isDeleted) 
    {
        this.isDeleted = isDeleted;
    }

    public Long getIsDeleted() 
    {
        return isDeleted;
    }

    public void setDeletedTime(Date deletedTime) 
    {
        this.deletedTime = deletedTime;
    }

    public Date getDeletedTime() 
    {
        return deletedTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("english", getEnglish())
            .append("chinese", getChinese())
            .append("phonetic", getPhonetic())
            .append("wordBookId", getWordBookId())
            .append("createdTime", getCreatedTime())
            .append("updatedTime", getUpdatedTime())
            .append("isDeleted", getIsDeleted())
            .append("deletedTime", getDeletedTime())
            .toString();
    }
}
