package com.openstudy.sensitiveWord.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.openstudy.common.annotation.Excel;
import com.openstudy.common.core.domain.BaseEntity;

/**
 * 敏感词管理对象 sys_sensitive_word
 * 
 * @author liu
 * @date 2026-04-17
 */
public class SysSensitiveWord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 敏感词 */
    @Excel(name = "敏感词")
    private String word;

    /** 分类：涉政/色情/暴恐/辱骂/广告/其他 */
    @Excel(name = "分类：涉政/色情/暴恐/辱骂/广告/其他")
    private String category;

    /** 状态：1=启用 0=禁用 */
    @Excel(name = "状态：1=启用 0=禁用")
    private Long status;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setWord(String word) 
    {
        this.word = word;
    }

    public String getWord() 
    {
        return word;
    }

    public void setCategory(String category) 
    {
        this.category = category;
    }

    public String getCategory() 
    {
        return category;
    }

    public void setStatus(Long status) 
    {
        this.status = status;
    }

    public Long getStatus() 
    {
        return status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("word", getWord())
            .append("category", getCategory())
            .append("status", getStatus())
            .append("remark", getRemark())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
