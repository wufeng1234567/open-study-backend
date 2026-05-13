package com.openstudy.notes.domain;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 笔记点击记录对象 note_click_record
 * 
 * @author openstudy
 */
@Data
public class NoteClickRecord
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 笔记ID */
    private Long noteId;

    /** 用户ID */
    private Long userId;

    /** 点击时间 */
    private LocalDateTime createTime;
}
