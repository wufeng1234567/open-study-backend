package com.openstudy.noteCategory.service;

import com.openstudy.noteCategory.domain.NoteCategory;

import java.util.List;

/**
 * 笔记分类，支持用户自定义单层分类，每个分类下可包含多篇笔记Service接口
 * 
 * @author ruoyi
 * @date 2025-12-02
 */
public interface INoteCategoryService 
{
    /**
     * 查询笔记分类，支持用户自定义单层分类，每个分类下可包含多篇笔记
     * 
     * @param id 笔记分类，支持用户自定义单层分类，每个分类下可包含多篇笔记主键
     * @return 笔记分类，支持用户自定义单层分类，每个分类下可包含多篇笔记
     */
    public NoteCategory selectNoteCategoryById(Long id);

    /**
     * 查询笔记分类，支持用户自定义单层分类，每个分类下可包含多篇笔记列表
     * 
     * @param noteCategory 笔记分类，支持用户自定义单层分类，每个分类下可包含多篇笔记
     * @return 笔记分类，支持用户自定义单层分类，每个分类下可包含多篇笔记集合
     */
    public List<NoteCategory> selectNoteCategoryList(NoteCategory noteCategory);

    /**
     * 新增笔记分类，支持用户自定义单层分类，每个分类下可包含多篇笔记
     * 
     * @param noteCategory 笔记分类，支持用户自定义单层分类，每个分类下可包含多篇笔记
     * @return 结果
     */
    public int insertNoteCategory(NoteCategory noteCategory);

    /**
     * 修改笔记分类，支持用户自定义单层分类，每个分类下可包含多篇笔记
     * 
     * @param noteCategory 笔记分类，支持用户自定义单层分类，每个分类下可包含多篇笔记
     * @return 结果
     */
    public int updateNoteCategory(NoteCategory noteCategory);

    /**
     * 批量删除笔记分类，支持用户自定义单层分类，每个分类下可包含多篇笔记
     * 
     * @param ids 需要删除的笔记分类，支持用户自定义单层分类，每个分类下可包含多篇笔记主键集合
     * @return 结果
     */
    public int deleteNoteCategoryByIds(Long[] ids);

    /**
     * 删除笔记分类，支持用户自定义单层分类，每个分类下可包含多篇笔记信息
     * 
     * @param id 笔记分类，支持用户自定义单层分类，每个分类下可包含多篇笔记主键
     * @return 结果
     */
    public int deleteNoteCategoryById(Long id);
}
