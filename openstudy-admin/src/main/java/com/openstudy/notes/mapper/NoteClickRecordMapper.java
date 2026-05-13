package com.openstudy.notes.mapper;

import com.openstudy.notes.domain.NoteClickRecord;
import org.apache.ibatis.annotations.Param;

/**
 * 笔记点击记录Mapper接口
 * 
 * @author openstudy
 */
public interface NoteClickRecordMapper 
{
    /**
     * 新增笔记点击记录（同一用户同一笔记只记录一次）
     * 
     * @param record 点击记录
     * @return 结果
     */
    int insert(NoteClickRecord record);
}
