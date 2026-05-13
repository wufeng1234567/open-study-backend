package com.openstudy.notes.domain;

import com.openstudy.common.annotation.Excel;
import com.openstudy.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 公开笔记分区对象 note_public_section
 * 
 * @author openstudy
 * @date 2026-04-28
 */
public class NotePublicSection extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 分区ID，主键 */
    private Long id;

    /** 分区名称 */
    @Excel(name = "分区名称")
    private String name;

    /** 排序序号 */
    @Excel(name = "排序序号")
    private Long sortOrder;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setName(String name) 
    {
        this.name = name;
    }

    public String getName() 
    {
        return name;
    }

    public void setSortOrder(Long sortOrder) 
    {
        this.sortOrder = sortOrder;
    }

    public Long getSortOrder() 
    {
        return sortOrder;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("name", getName())
            .append("sortOrder", getSortOrder())
            .append("createTime", getCreateTime())
            .toString();
    }
}
