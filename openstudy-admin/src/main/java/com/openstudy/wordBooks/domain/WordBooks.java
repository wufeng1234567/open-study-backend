package com.openstudy.wordBooks.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.openstudy.common.annotation.Excel;
import com.openstudy.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.ToString;

import java.util.Date;

/**
 * 单词本对象 word_books
 * 
 * @author liu
 * @date 2025-10-24
 */
@Data
@ToString
public class WordBooks extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 单词本名称 */
    @Excel(name = "单词本名称")
    private String name;

    /** 单词本描述 */
    @Excel(name = "单词本描述")
    private String description;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "创建时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date createdTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "更新时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date updatedTime;

    /** 是否为默认单词本 */
    @Excel(name = "是否为默认单词本")
    private Integer isDefault;

    /** 逻辑删除标识: 0-未删除, 1-已删除 */
    @Excel(name = "逻辑删除标识: 0-未删除, 1-已删除")
    private Long isDeleted;

    /** 删除时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "删除时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date deletedTime;

    /** 用户ID */
    @Excel(name = "用户ID")
    private String userId;


}
