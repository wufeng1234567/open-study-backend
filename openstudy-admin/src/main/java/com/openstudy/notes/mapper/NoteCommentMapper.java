package com.openstudy.notes.mapper;

import com.openstudy.notes.domain.NoteComment;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 笔记评论Mapper接口
 * 
 * @author liu
 * @date 2026-04-28
 */
public interface NoteCommentMapper
{
    /**
     * 查询一级评论列表（分页）
     * 
     * @param noteComment 查询条件
     * @return 评论集合
     */
    public List<NoteComment> selectCommentList(NoteComment noteComment);

    /**
     * 查询某条评论的子回复
     * 
     * @param parentId 父评论ID
     * @return 子回复集合
     */
    public List<NoteComment> selectReplies(@Param("parentId") Long parentId);

    /**
     * 插入评论
     * 
     * @param comment 评论对象
     * @return 结果
     */
    public int insertComment(NoteComment comment);

    /**
     * 删除评论（软删除）
     * 
     * @param id 评论ID
     * @param userId 用户ID（用于权限校验）
     * @return 结果
     */
    public int deleteById(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 统计笔记的评论总数
     * 
     * @param noteId 笔记ID
     * @return 评论数量
     */
    public int countByNoteId(@Param("noteId") Long noteId);

    /**
     * 统计某条评论的子回复总数
     * 
     * @param parentId 父评论ID
     * @return 子回复数量
     */
    public int countReplies(@Param("parentId") Long parentId);

    /**
     * 查询评论详情
     * 
     * @param id 评论ID
     * @return 评论对象
     */
    public NoteComment selectCommentById(@Param("id") Long id);

    /**
     * 查询某条评论的所有子孙回复（用于构建对话树）
     * 
     * @param parentId 父评论ID
     * @return 所有子孙回复
     */
    public List<NoteComment> selectAllDescendants(@Param("parentId") Long parentId);
}
