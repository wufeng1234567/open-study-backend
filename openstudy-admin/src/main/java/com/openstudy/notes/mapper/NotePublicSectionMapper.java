package com.openstudy.notes.mapper;

import com.openstudy.notes.domain.NotePublicSection;

import java.util.List;

/**
 * 公开笔记分区Mapper接口
 * 
 * @author openstudy
 * @date 2026-04-28
 */
public interface NotePublicSectionMapper 
{
    /**
     * 查询所有公开笔记分区，按 sort_order 排序
     * 
     * @return 公开笔记分区列表
     */
    public List<NotePublicSection> selectAll();
}
