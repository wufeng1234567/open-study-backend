package com.openstudy.notes.service;

import com.openstudy.notes.domain.Note;

import java.util.List;

/**
 * 用户笔记主，每篇笔记必须归属于一个用户自定义的分类Service接口
 * 
 * @author liu
 * @date 2025-12-02
 */
public interface INoteService 
{
    /**
     * 查询用户笔记主，每篇笔记必须归属于一个用户自定义的分类
     * 
     * @param id 用户笔记主，每篇笔记必须归属于一个用户自定义的分类主键
     * @return 用户笔记主，每篇笔记必须归属于一个用户自定义的分类
     */
    public Note selectNoteById(Long id);

    /**
     * 查询用户笔记主，每篇笔记必须归属于一个用户自定义的分类列表
     * 
     * @param note 用户笔记主，每篇笔记必须归属于一个用户自定义的分类
     * @return 用户笔记主，每篇笔记必须归属于一个用户自定义的分类集合
     */
    public List<Note> selectNoteList(Note note);

    /**
     * 新增用户笔记主，每篇笔记必须归属于一个用户自定义的分类
     * 
     * @param note 用户笔记主，每篇笔记必须归属于一个用户自定义的分类
     * @return 结果
     */
    public int insertNote(Note note);

    /**
     * 修改用户笔记主，每篇笔记必须归属于一个用户自定义的分类
     * 
     * @param note 用户笔记主，每篇笔记必须归属于一个用户自定义的分类
     * @return 结果
     */
    public int updateNote(Note note);

    /**
     * 批量删除用户笔记主，每篇笔记必须归属于一个用户自定义的分类
     * 
     * @param ids 需要删除的用户笔记主，每篇笔记必须归属于一个用户自定义的分类主键集合
     * @return 结果
     */
    public int deleteNoteByIds(Long[] ids);

    /**
     * 删除用户笔记主，每篇笔记必须归属于一个用户自定义的分类信息
     * 
     * @param id 用户笔记主，每篇笔记必须归属于一个用户自定义的分类主键
     * @return 结果
     */
    public int deleteNoteById(Long id);

    /**
     * 查询公开笔记榜单（今日/本周点击榜）
     * 
     * @param type 榜单类型：today-今日，week-本周
     * @param limit 返回条数
     * @return 笔记列表
     */
    public List<Note> selectRankingList(String type, Integer limit);
}
