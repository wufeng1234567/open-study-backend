package com.openstudy.noteCategory.domain;

import com.openstudy.common.annotation.Excel;
import com.openstudy.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 笔记分类，支持用户自定义单层分类，每个分类下可包含多篇笔记对象 note_category
 * 
 * @author ruoyi
 * @date 2025-12-02
 */
public class NoteCategory extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 分类ID，主键 */
    private Long id;

    /** 所属用户ID，关联 sys_user.id */
    @Excel(name = "所属用户ID，关联 sys_user.id")
    private Long userId;

    /** 分类名称，如“个人笔记”、“工作文档”，由用户自定义 */
    @Excel(name = "分类名称，如“个人笔记”、“工作文档”，由用户自定义")
    private String name;

    /** 排序序号，用于前端拖拽调整分类顺序 */
    @Excel(name = "排序序号，用于前端拖拽调整分类顺序")
    private Long orderNum;

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

    public void setName(String name) 
    {
        this.name = name;
    }

    public String getName() 
    {
        return name;
    }

    public void setOrderNum(Long orderNum) 
    {
        this.orderNum = orderNum;
    }

    public Long getOrderNum() 
    {
        return orderNum;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("userId", getUserId())
            .append("name", getName())
            .append("orderNum", getOrderNum())
            .append("createTime", getCreateTime())
            .toString();
    }
}
