package com.openstudy.notes.controller;

import com.github.pagehelper.PageInfo;
import com.openstudy.common.core.controller.BaseController;
import com.openstudy.common.core.domain.AjaxResult;
import com.openstudy.common.utils.SecurityUtils;
import com.openstudy.notes.domain.NoteComment;
import com.openstudy.notes.service.INoteCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 笔记评论Controller
 * 
 * @author liu
 * @date 2026-04-28
 */
@Tag(name = "笔记评论")
@RestController
@RequestMapping("/notes/comment")
@RequiredArgsConstructor
public class NoteCommentController extends BaseController
{
    private final INoteCommentService commentService;

    /**
     * 查询一级评论列表（分页）
     */
    @Operation(summary = "查询一级评论列表")
    @GetMapping("/list")
    public AjaxResult list(@RequestParam Long noteId,
                           @RequestParam(defaultValue = "1") Integer pageNum,
                           @RequestParam(defaultValue = "10") Integer pageSize)
    {
        PageInfo<NoteComment> pageInfo = commentService.selectCommentList(noteId, pageNum, pageSize);
        return success(pageInfo);
    }

    /**
     * 查询子回复（分页）
     */
    @Operation(summary = "查询子回复")
    @GetMapping("/replies")
    public AjaxResult replies(@RequestParam Long parentId,
                              @RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "5") Integer pageSize)
    {
        PageInfo<NoteComment> pageInfo = commentService.selectReplies(parentId, pageNum, pageSize);
        return success(pageInfo);
    }

    /**
     * 查询完整对话树
     */
    @Operation(summary = "查询完整对话树")
    @GetMapping("/thread/{commentId}")
    public AjaxResult thread(@PathVariable Long commentId)
    {
        NoteComment thread = commentService.selectCommentThread(commentId);
        return success(thread);
    }

    /**
     * 发表评论/回复
     */
    @Operation(summary = "发表评论/回复")
    @PostMapping("/create")
    public AjaxResult create(@RequestBody NoteComment comment)
    {
        // 校验必填参数
        if (comment.getNoteId() == null) {
            return error("笔记ID不能为空");
        }
        if (comment.getContent() == null || comment.getContent().trim().isEmpty()) {
            return error("评论内容不能为空");
        }
        
        commentService.insertComment(comment);
        return success(comment);
    }

    /**
     * 删除评论
     */
    @Operation(summary = "删除评论")
    @DeleteMapping("/delete/{id}")
    public AjaxResult delete(@PathVariable Long id)
    {
        Long userId = SecurityUtils.getUserId();
        commentService.deleteById(id, userId);
        return AjaxResult.success();
    }
}
