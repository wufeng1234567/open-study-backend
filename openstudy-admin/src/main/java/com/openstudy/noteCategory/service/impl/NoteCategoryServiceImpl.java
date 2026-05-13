package com.openstudy.noteCategory.service.impl;

import com.openstudy.common.exception.ServiceException;
import com.openstudy.common.utils.DateUtils;
import com.openstudy.noteCategory.domain.NoteCategory;
import com.openstudy.noteCategory.mapper.NoteCategoryMapper;
import com.openstudy.noteCategory.service.INoteCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 笔记分类，支持用户自定义单层分类，每个分类下可包含多篇笔记Service业务层处理
 * 
 * @author ruoyi
 * @date 2025-12-02
 */
@Service
public class NoteCategoryServiceImpl implements INoteCategoryService 
{
    @Autowired
    private NoteCategoryMapper noteCategoryMapper;

    /**
     * 查询笔记分类，支持用户自定义单层分类，每个分类下可包含多篇笔记
     * 
     * @param id 笔记分类，支持用户自定义单层分类，每个分类下可包含多篇笔记主键
     * @return 笔记分类，支持用户自定义单层分类，每个分类下可包含多篇笔记
     */
    @Override
    public NoteCategory selectNoteCategoryById(Long id)
    {
        return noteCategoryMapper.selectNoteCategoryById(id);
    }

    /**
     * 查询笔记分类，支持用户自定义单层分类，每个分类下可包含多篇笔记列表
     * 
     * @param noteCategory 笔记分类，支持用户自定义单层分类，每个分类下可包含多篇笔记
     * @return 笔记分类，支持用户自定义单层分类，每个分类下可包含多篇笔记
     */
    @Override
    public List<NoteCategory> selectNoteCategoryList(NoteCategory noteCategory)
    {
        return noteCategoryMapper.selectNoteCategoryList(noteCategory);
    }

    /**
     * 新增笔记分类，支持用户自定义单层分类，每个分类下可包含多篇笔记
     * 
     * @param noteCategory 笔记分类，支持用户自定义单层分类，每个分类下可包含多篇笔记
     * @return 结果
     */
    @Override
    public int insertNoteCategory(NoteCategory noteCategory)
    {
        noteCategory.setCreateTime(DateUtils.getNowDate());
        try {
            return noteCategoryMapper.insertNoteCategory(noteCategory);
        } catch (DuplicateKeyException e) {
            throw new ServiceException("该分类名称已存在");
        }

    }

    /**
     * 修改笔记分类，支持用户自定义单层分类，每个分类下可包含多篇笔记
     * 
     * @param noteCategory 笔记分类，支持用户自定义单层分类，每个分类下可包含多篇笔记
     * @return 结果
     */
    @Override
    public int updateNoteCategory(NoteCategory noteCategory)
    {
        return noteCategoryMapper.updateNoteCategory(noteCategory);
    }

    /**
     * 批量删除笔记分类，支持用户自定义单层分类，每个分类下可包含多篇笔记
     * 
     * @param ids 需要删除的笔记分类，支持用户自定义单层分类，每个分类下可包含多篇笔记主键
     * @return 结果
     */
    @Override
    public int deleteNoteCategoryByIds(Long[] ids)
    {
        return noteCategoryMapper.deleteNoteCategoryByIds(ids);
    }

    /**
     * 删除笔记分类，支持用户自定义单层分类，每个分类下可包含多篇笔记信息
     * 
     * @param id 笔记分类，支持用户自定义单层分类，每个分类下可包含多篇笔记主键
     * @return 结果
     */
    @Override
    public int deleteNoteCategoryById(Long id)
    {
        return noteCategoryMapper.deleteNoteCategoryById(id);
    }
}
