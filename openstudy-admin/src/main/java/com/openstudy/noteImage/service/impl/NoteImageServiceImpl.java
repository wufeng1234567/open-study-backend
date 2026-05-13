package com.openstudy.noteImage.service.impl;

import com.openstudy.common.utils.DateUtils;
import com.openstudy.noteImage.domain.NoteImage;
import com.openstudy.noteImage.mapper.NoteImageMapper;
import com.openstudy.noteImage.service.INoteImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 笔记关联的图片资源Service业务层处理
 * 
 * @author ruoyi
 * @date 2025-12-02
 */
@Service
public class NoteImageServiceImpl implements INoteImageService 
{
    @Autowired
    private NoteImageMapper noteImageMapper;

    /**
     * 查询笔记关联的图片资源
     * 
     * @param id 笔记关联的图片资源主键
     * @return 笔记关联的图片资源
     */
    @Override
    public NoteImage selectNoteImageById(Long id)
    {
        return noteImageMapper.selectNoteImageById(id);
    }

    /**
     * 查询笔记关联的图片资源列表
     * 
     * @param noteImage 笔记关联的图片资源
     * @return 笔记关联的图片资源
     */
    @Override
    public List<NoteImage> selectNoteImageList(NoteImage noteImage)
    {
        return noteImageMapper.selectNoteImageList(noteImage);
    }

    /**
     * 新增笔记关联的图片资源
     * 
     * @param noteImage 笔记关联的图片资源
     * @return 结果
     */
    @Override
    public int insertNoteImage(NoteImage noteImage)
    {
        noteImage.setCreateTime(DateUtils.getNowDate());
        return noteImageMapper.insertNoteImage(noteImage);
    }

    /**
     * 修改笔记关联的图片资源
     * 
     * @param noteImage 笔记关联的图片资源
     * @return 结果
     */
    @Override
    public int updateNoteImage(NoteImage noteImage)
    {
        return noteImageMapper.updateNoteImage(noteImage);
    }

    /**
     * 批量删除笔记关联的图片资源
     * 
     * @param ids 需要删除的笔记关联的图片资源主键
     * @return 结果
     */
    @Override
    public int deleteNoteImageByIds(Long[] ids)
    {
        return noteImageMapper.deleteNoteImageByIds(ids);
    }

    /**
     * 删除笔记关联的图片资源信息
     * 
     * @param id 笔记关联的图片资源主键
     * @return 结果
     */
    @Override
    public int deleteNoteImageById(Long id)
    {
        return noteImageMapper.deleteNoteImageById(id);
    }
}
