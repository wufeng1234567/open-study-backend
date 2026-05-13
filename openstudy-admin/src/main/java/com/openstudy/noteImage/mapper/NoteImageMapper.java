package com.openstudy.noteImage.mapper;

import com.openstudy.noteImage.domain.NoteImage;

import java.util.List;

/**
 * 笔记关联的图片资源Mapper接口
 * 
 * @author ruoyi
 * @date 2025-12-02
 */
public interface NoteImageMapper 
{
    /**
     * 查询笔记关联的图片资源
     * 
     * @param id 笔记关联的图片资源主键
     * @return 笔记关联的图片资源
     */
    public NoteImage selectNoteImageById(Long id);

    /**
     * 查询笔记关联的图片资源列表
     * 
     * @param noteImage 笔记关联的图片资源
     * @return 笔记关联的图片资源集合
     */
    public List<NoteImage> selectNoteImageList(NoteImage noteImage);

    /**
     * 新增笔记关联的图片资源
     * 
     * @param noteImage 笔记关联的图片资源
     * @return 结果
     */
    public int insertNoteImage(NoteImage noteImage);

    /**
     * 修改笔记关联的图片资源
     * 
     * @param noteImage 笔记关联的图片资源
     * @return 结果
     */
    public int updateNoteImage(NoteImage noteImage);

    /**
     * 删除笔记关联的图片资源
     * 
     * @param id 笔记关联的图片资源主键
     * @return 结果
     */
    public int deleteNoteImageById(Long id);

    /**
     * 批量删除笔记关联的图片资源
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteNoteImageByIds(Long[] ids);
}
