package com.openstudy.notes.domain;

import com.openstudy.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 笔记评论对象 note_comment
 * 
 * @author liu
 * @date 2026-04-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NoteComment extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 评论ID，主键 */
    private Long id;

    /** 所属笔记ID */
    private Long noteId;

    /** 父评论ID（NULL表示一级评论） */
    private Long parentId;

    /** 回复目标用户ID */
    private Long replyToUserId;

    /** 回复目标用户名 */
    private String replyToUserName;

    /** 评论用户ID */
    private Long userId;

    /** 评论用户名 */
    private String userName;

    /** 用户头像 */
    private String avatar;

    /** 评论内容 */
    private String content;

    /** 状态：0-已删除，1-正常 */
    private Integer status;

    /** 子回复数量（非数据库字段，用于列表展示） */
    private Integer replyCount;

    /** 子回复列表（非数据库字段，用于树形结构） */
    private List<NoteComment> children;
}
