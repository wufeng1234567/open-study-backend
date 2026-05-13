package com.openstudy.noteImage.domain;

import com.openstudy.common.annotation.Excel;
import com.openstudy.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 笔记关联的图片资源对象 note_image
 * 
 * @author ruoyi
 * @date 2025-12-02
 */
public class NoteImage extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 图片ID */
    private Long id;

    /** 所属笔记ID */
    @Excel(name = "所属笔记ID")
    private Long noteId;

    /** 原始文件名 */
    @Excel(name = "原始文件名")
    private String fileName;

    /** 相对存储路径，如 /upload/notes/img/2025/12/xxx.png */
    @Excel(name = "相对存储路径，如 /upload/notes/img/2025/12/xxx.png")
    private String filePath;

    /** 文件大小（字节） */
    @Excel(name = "文件大小", readConverterExp = "字=节")
    private Long fileSize;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setNoteId(Long noteId) 
    {
        this.noteId = noteId;
    }

    public Long getNoteId() 
    {
        return noteId;
    }

    public void setFileName(String fileName) 
    {
        this.fileName = fileName;
    }

    public String getFileName() 
    {
        return fileName;
    }

    public void setFilePath(String filePath) 
    {
        this.filePath = filePath;
    }

    public String getFilePath() 
    {
        return filePath;
    }

    public void setFileSize(Long fileSize) 
    {
        this.fileSize = fileSize;
    }

    public Long getFileSize() 
    {
        return fileSize;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("noteId", getNoteId())
            .append("fileName", getFileName())
            .append("filePath", getFilePath())
            .append("fileSize", getFileSize())
            .append("createTime", getCreateTime())
            .toString();
    }
}
