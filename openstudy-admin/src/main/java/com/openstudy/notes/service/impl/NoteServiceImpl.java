package com.openstudy.notes.service.impl;

import com.openstudy.common.exception.ServiceException;
import com.openstudy.common.utils.DateUtils;
import com.openstudy.notes.domain.Note;
import com.openstudy.notes.mapper.NoteMapper;
import com.openstudy.notes.service.INoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户笔记主，每篇笔记必须归属于一个用户自定义的分类Service业务层处理
 * 
 * @author liu
 * @date 2025-12-02
 */
@Service
public class NoteServiceImpl implements INoteService 
{
    @Autowired
    private NoteMapper noteMapper;

    /**
     * 查询用户笔记主，每篇笔记必须归属于一个用户自定义的分类
     * 
     * @param id 用户笔记主，每篇笔记必须归属于一个用户自定义的分类主键
     * @return 用户笔记主，每篇笔记必须归属于一个用户自定义的分类
     */
    @Override
    public Note selectNoteById(Long id)
    {
        return noteMapper.selectNoteById(id);
    }

    /**
     * 查询用户笔记主，每篇笔记必须归属于一个用户自定义的分类列表
     * 
     * @param note 用户笔记主，每篇笔记必须归属于一个用户自定义的分类
     * @return 用户笔记主，每篇笔记必须归属于一个用户自定义的分类
     */
    @Override
    public List<Note> selectNoteList(Note note)
    {
        return noteMapper.selectNoteList(note);
    }

    /**
     * 新增用户笔记主，每篇笔记必须归属于一个用户自定义的分类
     * 
     * @param note 用户笔记主，每篇笔记必须归属于一个用户自定义的分类
     * @return 结果
     */
    @Override
    public int insertNote(Note note)
    {
        // 如果 filename 为空，自动用标题拼接
        if (note.getFilename() == null || note.getFilename().isEmpty()) {
            note.setFilename(note.getTitle() + ".md");
        }
        
        // 自动计算字数统计（去除空白字符）
        if (note.getWordCount() == null || note.getWordCount() == 0) {
            String content = note.getMarkdownContent();
            if (content != null) {
                note.setWordCount((long) content.replaceAll("\\s+", "").length());
            }
        }
        
        // 根据 isPublic 自动设置 status
        if (note.getIsPublic() != null) {
            if (note.getIsPublic() == 1) {
                note.setStatus("published");  // 公开笔记自动设置为已发布
            } else {
                note.setStatus("draft");  // 私有笔记默认为草稿
            }
        } else {
            // 如果 isPublic 未设置，默认为私有草稿
            note.setIsPublic(0);
            note.setStatus("draft");
        }
        
        note.setCreateTime(DateUtils.getNowDate());
        try {
            return noteMapper.insertNote(note);
        } catch (DuplicateKeyException e) {
            // 捕获唯一索引冲突
            throw new ServiceException("该笔记标题已存在，请修改标题");
        }
    }

    /**
     * 修改用户笔记主，每篇笔记必须归属于一个用户自定义的分类
     * 
     * @param note 用户笔记主，每篇笔记必须归属于一个用户自定义的分类
     * @return 结果
     */
    @Override
    public int updateNote(Note note)
    {
        // 自动计算字数统计（去除空白字符）
        if (note.getMarkdownContent() != null) {
            note.setWordCount((long) note.getMarkdownContent().replaceAll("\\s+", "").length());
        }
        
        note.setUpdateTime(DateUtils.getNowDate());
        return noteMapper.updateNote(note);
    }

    /**
     * 批量删除用户笔记主，每篇笔记必须归属于一个用户自定义的分类
     * 
     * @param ids 需要删除的用户笔记主，每篇笔记必须归属于一个用户自定义的分类主键
     * @return 结果
     */
    @Override
    public int deleteNoteByIds(Long[] ids)
    {
        return noteMapper.deleteNoteByIds(ids);
    }

    /**
     * 删除用户笔记主，每篇笔记必须归属于一个用户自定义的分类信息
     * 
     * @param id 用户笔记主，每篇笔记必须归属于一个用户自定义的分类主键
     * @return 结果
     */
    @Override
    public int deleteNoteById(Long id)
    {
        return noteMapper.deleteNoteById(id);
    }

    /**
     * 查询公开笔记榜单（今日/本周点击榜）
     * 
     * @param type 榜单类型：today-今日，week-本周
     * @param limit 返回条数
     * @return 笔记列表
     */
    @Override
    public List<Note> selectRankingList(String type, Integer limit)
    {
        return noteMapper.selectRankingList(type, limit);
    }
}
