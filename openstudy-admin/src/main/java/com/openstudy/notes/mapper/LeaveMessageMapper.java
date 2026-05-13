package com.openstudy.notes.mapper;

import com.openstudy.notes.domain.LeaveMessage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 留言板Mapper接口
 *
 * @author openstudy
 * @date 2026-05-03
 */
public interface LeaveMessageMapper {
    /**
     * 查询留言列表（按时间倒序）
     *
     * @param leaveMessage 查询条件
     * @return 留言集合
     */
    public List<LeaveMessage> selectLeaveMessageList(LeaveMessage leaveMessage);

    /**
     * 查询留言详情
     *
     * @param id 留言ID
     * @return 留言对象
     */
    public LeaveMessage selectLeaveMessageById(@Param("id") Long id);

    /**
     * 插入留言
     *
     * @param leaveMessage 留言对象
     * @return 结果
     */
    public int insertLeaveMessage(LeaveMessage leaveMessage);

    /**
     * 删除留言（软删除）
     *
     * @param id 留言ID
     * @return 结果
     */
    public int deleteLeaveMessageById(@Param("id") Long id);

    /**
     * 批量删除留言（软删除）
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteLeaveMessageByIds(@Param("ids") Long[] ids);

    /**
     * 修改留言
     *
     * @param leaveMessage 留言对象
     * @return 结果
     */
    public int updateLeaveMessage(LeaveMessage leaveMessage);
}
