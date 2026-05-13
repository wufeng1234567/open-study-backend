package com.openstudy.notes.service.impl;

import com.alibaba.fastjson2.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.openstudy.common.exception.ServiceException;
import com.openstudy.common.utils.DateUtils;
import com.openstudy.common.utils.SecurityUtils;
import com.openstudy.notes.domain.Note;
import com.openstudy.notes.domain.NoteComment;
import com.openstudy.notes.mapper.NoteCommentMapper;
import com.openstudy.notes.mapper.NoteMapper;
import com.openstudy.notes.service.INoteCommentService;
import com.openstudy.system.domain.SysNotice;
import com.openstudy.common.core.domain.entity.SysUser;
import com.openstudy.system.service.ISysNoticeService;
import com.openstudy.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 笔记评论Service业务层处理
 * 
 * @author liu
 * @date 2026-04-28
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NoteCommentServiceImpl implements INoteCommentService {
    private final NoteCommentMapper commentMapper;
    private final NoteMapper noteMapper;
    private final ISysNoticeService noticeService;
    private final ISysUserService userService;

    /**
     * 查询一级评论列表（分页）
     */
    @Override
    public PageInfo<NoteComment> selectCommentList(Long noteId, Integer pageNum, Integer pageSize) {
        log.info("查询笔记评论列表: noteId={}, pageNum={}, pageSize={}", noteId, pageNum, pageSize);

        // 校验笔记是否存在
        Note note = noteMapper.selectNoteById(noteId);
        if (note == null) {
            throw new ServiceException("笔记不存在");
        }

        // 开启分页
        PageHelper.startPage(pageNum != null ? pageNum : 1, pageSize != null ? pageSize : 10);

        NoteComment query = new NoteComment();
        query.setNoteId(noteId);
        List<NoteComment> list = commentMapper.selectCommentList(query);

        PageInfo<NoteComment> pageInfo = new PageInfo<>(list);
        log.info("查询到 {} 条一级评论", pageInfo.getTotal());

        return pageInfo;
    }

    /**
     * 查询某条评论的子回复（分页）
     */
    @Override
    public PageInfo<NoteComment> selectReplies(Long parentId, Integer pageNum, Integer pageSize) {
        log.info("查询子回复: parentId={}, pageNum={}, pageSize={}", parentId, pageNum, pageSize);

        // 校验父评论是否存在
        NoteComment parent = commentMapper.selectCommentById(parentId);
        if (parent == null) {
            throw new ServiceException("父评论不存在");
        }

        // 开启分页
        PageHelper.startPage(pageNum != null ? pageNum : 1, pageSize != null ? pageSize : 5);

        List<NoteComment> list = commentMapper.selectReplies(parentId);
        PageInfo<NoteComment> pageInfo = new PageInfo<>(list);

        log.info("查询到 {} 条子回复", pageInfo.getTotal());

        return pageInfo;
    }

    /**
     * 查询完整对话树
     */
    @Override
    public NoteComment selectCommentThread(Long commentId) {
        log.info("查询完整对话树: commentId={}", commentId);

        // 查询根评论
        NoteComment root = commentMapper.selectCommentById(commentId);
        if (root == null) {
            throw new ServiceException("评论不存在");
        }

        // 查询所有子孙回复
        List<NoteComment> allDescendants = commentMapper.selectAllDescendants(commentId);

        // 在内存中构建树形结构
        Map<Long, NoteComment> commentMap = new HashMap<>();
        commentMap.put(root.getId(), root);

        for (NoteComment comment : allDescendants) {
            commentMap.put(comment.getId(), comment);
        }

        // 构建父子关系
        for (NoteComment comment : allDescendants) {
            Long parentId = comment.getParentId();
            if (parentId != null && commentMap.containsKey(parentId)) {
                NoteComment parent = commentMap.get(parentId);
                if (parent.getChildren() == null) {
                    parent.setChildren(new ArrayList<>());
                }
                parent.getChildren().add(comment);
            }
        }

        log.info("构建对话树完成，共 {} 个节点", commentMap.size());

        return root;
    }

    /**
     * 发表评论/回复
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertComment(NoteComment comment) {
        log.info("发表评论: noteId={}, parentId={}", comment.getNoteId(), comment.getParentId());

        // 校验笔记是否存在
        Note note = noteMapper.selectNoteById(comment.getNoteId());
        if (note == null) {
            throw new ServiceException("笔记不存在");
        }

        // 校验内容
        if (comment.getContent() == null || comment.getContent().trim().isEmpty()) {
            throw new ServiceException("评论内容不能为空");
        }

        String content = comment.getContent().trim();
        if (content.length() > 500) {
            throw new ServiceException("评论内容不能超过500字");
        }
        comment.setContent(content);

        // 如果是一级评论，parentId 应该为 null
        if (comment.getParentId() == null) {
            comment.setParentId(null);
            comment.setReplyToUserId(null);
        } else {
            // 如果是回复，校验父评论存在且属于同一笔记
            NoteComment parent = commentMapper.selectCommentById(comment.getParentId());
            if (parent == null) {
                throw new ServiceException("父评论不存在");
            }
            if (!parent.getNoteId().equals(comment.getNoteId())) {
                throw new ServiceException("父评论不属于该笔记");
            }
        }

        // 设置当前用户信息
        Long userId = SecurityUtils.getUserId();
        comment.setUserId(userId);
        comment.setStatus(1); // 正常状态
        comment.setCreateTime(DateUtils.getNowDate());

        int result = commentMapper.insertComment(comment);
        log.info("评论发表成功: id={}", comment.getId());

        // 发送系统通知
        try {
            sendCommentNotification(comment, note);
        } catch (Exception e) {
            log.error("发送评论通知失败", e);
            // 通知失败不影响评论发表
        }

        return result;
    }

    /**
     * 删除评论（软删除）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(Long id, Long userId) {
        log.info("删除评论: id={}, userId={}", id, userId);

        // 校验评论是否存在
        NoteComment comment = commentMapper.selectCommentById(id);
        if (comment == null) {
            throw new ServiceException("评论不存在");
        }

        // 只能删除自己的评论
        if (!comment.getUserId().equals(userId)) {
            throw new ServiceException("只能删除自己的评论");
        }

        int result = commentMapper.deleteById(id, userId);
        log.info("评论删除成功");

        return result;
    }

    /**
     * 统计笔记的评论总数
     */
    @Override
    public int countByNoteId(Long noteId) {
        return commentMapper.countByNoteId(noteId);
    }

    /**
     * 发送评论通知（有 @ 就发 mention 通知，没 @ 才发普通通知，互斥）
     */
    private void sendCommentNotification(NoteComment comment, Note note) {
        Long currentUserId = comment.getUserId();

        List<Object[]> mentionedUsers = extractMentionedUsers(comment.getContent());
        if (!mentionedUsers.isEmpty()) {
            sendMentionNotifications(comment, note, currentUserId, mentionedUsers);
            return;
        }

        Long targetUserId = null;
        String noticeTitle = "";

        if (comment.getParentId() != null) {
            NoteComment parentComment = commentMapper.selectCommentById(comment.getParentId());
            if (parentComment != null) {
                targetUserId = parentComment.getUserId();
                noticeTitle = "有人回复了你的评论";
            }
        } else {
            targetUserId = note.getUserId();
            noticeTitle = "有人评论了你的笔记";
        }

        if (targetUserId == null || targetUserId.equals(currentUserId)) {
            log.info("无需发送通知：targetUserId={}, currentUserId={}", targetUserId, currentUserId);
            return;
        }

        String content = comment.getContent();
        String noticeContent = content.length() > 50 ? content.substring(0, 50) + "..." : content;

        Map<String, Object> remarkMap = new HashMap<>();
        remarkMap.put("type", comment.getParentId() != null ? "reply" : "comment");
        remarkMap.put("fromUserId", currentUserId);
        remarkMap.put("toUserId", targetUserId);
        remarkMap.put("noteId", comment.getNoteId());
        remarkMap.put("commentId", comment.getId());
        String remarkJson = JSON.toJSONString(remarkMap);

        SysNotice notice = new SysNotice();
        notice.setNoticeTitle(noticeTitle);
        notice.setNoticeContent(noticeContent);
        notice.setNoticeType("1");
        notice.setStatus("0");
        notice.setRemark(remarkJson);
        notice.setCreateBy("system");
        notice.setCreateTime(DateUtils.getNowDate());

        noticeService.insertNotice(notice);

        log.info("发送评论通知成功: targetUserId={}, noticeId={}, type={}",
                targetUserId, notice.getNoticeId(), comment.getParentId() != null ? "REPLY" : "COMMENT");
    }

    /**
     * 从评论内容中提取 @ 提及的用户信息（支持新格式 @{userId} nickName 和旧格式 @username）
     * 返回的列表每个元素是 [userId, 显示名] 或 [null, username]（旧格式无法解析userId）
     */
    private List<Object[]> extractMentionedUsers(String content) {
        List<Object[]> results = new ArrayList<>();
        if (content == null || content.isEmpty())
            return results;

        Pattern newFormat = Pattern.compile("@\\{(\\d+)\\}\\s*([\\u4e00-\\u9fa5a-zA-Z0-9_]+)");
        Matcher newMatcher = newFormat.matcher(content);
        Set<String> processed = new LinkedHashSet<>();

        while (newMatcher.find()) {
            Long userId = Long.parseLong(newMatcher.group(1));
            String displayName = newMatcher.group(2);
            results.add(new Object[] { userId, displayName });
            processed.add(newMatcher.group(0));
        }

        Pattern oldFormat = Pattern.compile("(?<!\\{@\\d+\\}\\s*)(?:@|＠)([\\u4e00-\\u9fa5a-zA-Z0-9_]+)(?!\\s*\\{)");
        Matcher oldMatcher = oldFormat.matcher(content);
        while (oldMatcher.find()) {
            String fullMatch = oldMatcher.group(0);
            if (!processed.contains(fullMatch)) {
                results.add(new Object[] { null, oldMatcher.group(1) });
            }
        }

        return results;
    }

    /**
     * 从评论内容中提取 @ 的用户名（去重，保持顺序）- 兼容旧格式
     */
    private Set<String> extractMentionedUserNames(String content) {
        Set<String> names = new LinkedHashSet<>();
        if (content == null || content.isEmpty())
            return names;

        Pattern pattern = Pattern.compile("(?<!@\\{\\d+\\}\\s*)(?:@|＠)([\\u4e00-\\u9fa5a-zA-Z0-9_]+)");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            names.add(matcher.group(1));
        }
        return names;
    }

    /**
     * 发送 @mention 通知（支持通过 userId 直接定位或通过 username 回退定位）
     */
    private void sendMentionNotifications(NoteComment comment, Note note, Long fromUserId,
            List<Object[]> mentionedUsers) {
        if (mentionedUsers.isEmpty())
            return;

        String content = comment.getContent();
        String fromUserName = SecurityUtils.getUsername();

        String noteTitle = note.getTitle();
        if (noteTitle != null && noteTitle.length() > 30) {
            noteTitle = noteTitle.substring(0, 30) + "...";
        }

        for (Object[] userInfo : mentionedUsers) {
            Long mentionedUserId = (Long) userInfo[0];
            String displayName = (String) userInfo[1];

            if (mentionedUserId == null) {
                SysUser mentionedUser = userService.selectUserByUserName(displayName);
                if (mentionedUser == null || mentionedUser.getUserId() == null) {
                    List<SysUser> allUsers = userService.selectUserListFront(new SysUser());
                    for (SysUser u : allUsers) {
                        if (displayName.equals(u.getNickName()) || displayName.equals(u.getUserName())) {
                            mentionedUserId = u.getUserId();
                            break;
                        }
                    }
                } else {
                    mentionedUserId = mentionedUser.getUserId();
                }
                if (mentionedUserId == null)
                    continue;
            }

            if (mentionedUserId.equals(fromUserId))
                continue;

            String noticeTitle = fromUserName + " 在评论中@了你";
            String noticeContent = "在笔记《" + noteTitle + "》的评论中提到了你："
                    + (content.length() > 50 ? content.substring(0, 50) + "..." : content);

            Map<String, Object> remarkMap = new HashMap<>();
            remarkMap.put("type", "mention");
            remarkMap.put("fromUserId", fromUserId);
            remarkMap.put("toUserId", mentionedUserId);
            remarkMap.put("noteId", comment.getNoteId());
            remarkMap.put("commentId", comment.getId());
            String remarkJson = JSON.toJSONString(remarkMap);

            SysNotice notice = new SysNotice();
            notice.setNoticeTitle(noticeTitle);
            notice.setNoticeContent(noticeContent);
            notice.setNoticeType("1");
            notice.setStatus("0");
            notice.setRemark(remarkJson);
            notice.setCreateBy("system");
            notice.setCreateTime(DateUtils.getNowDate());

            noticeService.insertNotice(notice);

            log.info("发送 @mention 通知: fromUserId={}, toUserId={}, displayName={}",
                    fromUserId, mentionedUserId, displayName);
        }
    }
}
