package com.openstudy.notes.domain;

import com.openstudy.common.annotation.Excel;
import com.openstudy.common.core.domain.BaseEntity;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.time.LocalDate;

/**
 * 用户笔记主，每篇笔记必须归属于一个用户自定义的分类对象 note
 * 
 * @author liu
 * @date 2025-12-02
 */
@Data
public class Note extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 笔记ID，主键 */
    private Long id;

    /** 所属用户ID */
    @Excel(name = "所属用户ID")
    private Long userId;

    /** 所属分类ID，关联 note_category.id，表示该笔记属于哪个分类（如"个人笔记"） */
    @Excel(name = "所属分类ID，关联 note_category.id，表示该笔记属于哪个分类", readConverterExp = "如=\"个人笔记\"")
    private Long categoryId;

    /** 笔记标题 */
    @Excel(name = "笔记标题")
    private String title;

    /** 原始文件名，如"学习计划.md" */
    @Excel(name = "原始文件名，如\"学习计划.md\"")
    private String filename;

    /** Markdown 内容 */
    @Excel(name = "Markdown 内容")
    private String markdownContent;

    /** HTML 渲染内容 */
    @Excel(name = "HTML 渲染内容")
    private String htmlContent;

    /** 字数统计 */
    @Excel(name = "字数统计")
    private Long wordCount;

    /** 标签数组，如["Java","学习"] */
    @Excel(name = "标签数组，如['Java','学习']")
    private String tags;

    /** 是否公开：0-私有，1-公开 */
    @Excel(name = "是否公开：0-私有，1-公开")
    private Integer isPublic;

    /** 状态：draft-草稿，published-已发布，archived-已归档 */
    @Excel(name = "状态：draft-草稿，published-已发布，archived-已归档")
    private String status;

    /** 笔记在当前分类内的排序权重 */
    @Excel(name = "笔记在当前分类内的排序权重")
    private Long sortOrder;

    /** 作者昵称（连表查询，不对应数据库字段） */
    private String authorName;

    /** 作者头像（连表查询，不对应数据库字段） */
    private String avatar;

    /** 总点击数（连表统计，不对应数据库字段） */
    private Long clickCount;

    /** 点击日期（榜单用，不对应数据库字段） */
    private LocalDate clickDate;

    /** 排序字段（搜索参数，不存数据库） */
    private String sortBy;

    /** 排序方向（搜索参数，不存数据库，对应前端 sortOrder 参数：asc/desc） */
    private String order;

    /** 开始时间（搜索参数，不存数据库） */
    private String startTime;

    /** 结束时间（搜索参数，不存数据库） */
    private String endTime;

    /** 公开分区ID */
    @Excel(name = "公开分区ID")
    private Long publicSectionId;

    /** 分区名称（连表查询，不对应数据库字段） */
    private String sectionName;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setUserId(Long userId) 
    {
        this.userId = userId;
    }

    public Long getUserId() 
    {
        return userId;
    }

    public void setCategoryId(Long categoryId) 
    {
        this.categoryId = categoryId;
    }

    public Long getCategoryId() 
    {
        return categoryId;
    }

    public void setTitle(String title) 
    {
        this.title = title;
    }

    public String getTitle() 
    {
        return title;
    }

    public void setFilename(String filename) 
    {
        this.filename = filename;
    }

    public String getFilename() 
    {
        return filename;
    }

    public void setMarkdownContent(String markdownContent) 
    {
        this.markdownContent = markdownContent;
    }

    public String getMarkdownContent() 
    {
        return markdownContent;
    }

    public void setHtmlContent(String htmlContent) 
    {
        this.htmlContent = htmlContent;
    }

    public String getHtmlContent() 
    {
        return htmlContent;
    }

    public void setWordCount(Long wordCount) 
    {
        this.wordCount = wordCount;
    }

    public Long getWordCount() 
    {
        return wordCount;
    }

    public void setIsPublic(Integer isPublic) 
    {
        this.isPublic = isPublic;
    }

    public Integer getIsPublic() 
    {
        return isPublic;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    public void setSortOrder(Long sortOrder) 
    {
        this.sortOrder = sortOrder;
    }

    public Long getSortOrder() 
    {
        return sortOrder;
    }

    public void setPublicSectionId(Long publicSectionId) 
    {
        this.publicSectionId = publicSectionId;
    }

    public Long getPublicSectionId() 
    {
        return publicSectionId;
    }

    public void setSectionName(String sectionName) 
    {
        this.sectionName = sectionName;
    }

    public String getSectionName() 
    {
        return sectionName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("userId", getUserId())
            .append("categoryId", getCategoryId())
            .append("title", getTitle())
            .append("filename", getFilename())
            .append("markdownContent", getMarkdownContent())
            .append("htmlContent", getHtmlContent())
            .append("wordCount", getWordCount())
            .append("tags", getTags())
            .append("isPublic", getIsPublic())
            .append("status", getStatus())
            .append("sortOrder", getSortOrder())
            .append("publicSectionId", getPublicSectionId())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
