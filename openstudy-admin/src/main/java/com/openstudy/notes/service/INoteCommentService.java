package com.openstudy.notes.service;

import com.github.pagehelper.PageInfo;
import com.openstudy.notes.domain.NoteComment;

import java.util.List;

/**
 * 笔记评论Service接口
 * 
 * @author liu
 * @date 2026-04-28
 */
public interface INoteCommentService
{
    /**
     * 查询一级评论列表（分页）
     * 
     * @param noteId 笔记ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 分页数据
     */
    public PageInfo<NoteComment> selectCommentList(Long noteId, Integer pageNum, Integer pageSize);

    /**
     * 查询某条评论的子回复（分页）
     * 
     * @param parentId 父评论ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 分页数据
     */
    public PageInfo<NoteComment> selectReplies(Long parentId, Integer pageNum, Integer pageSize);

    /**
     * 查询完整对话树
     * 
     * @param commentId 评论ID
     * @return 对话树（包含所有子孙回复）
     */
    public NoteComment selectCommentThread(Long commentId);

    /**
     * 发表评论/回复
     * 
     * @param comment 评论对象
     * @return 结果
     */
    public int insertComment(NoteComment comment);

    /**
     * 删除评论（软删除）
     * 
     * @param id 评论ID
     * @param userId 用户ID
     * @return 结果
     */
    public int deleteById(Long id, Long userId);

    /**
     * 统计笔记的评论总数
     * 
     * @param noteId 笔记ID
     * @return 评论数量
     */
    public int countByNoteId(Long noteId);
}
