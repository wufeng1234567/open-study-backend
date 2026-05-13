package com.openstudy.notes.service;

import com.openstudy.notes.domain.LeaveMessage;

import java.util.List;

/**
 * 留言板Service接口
 *
 * @author openstudy
 * @date 2026-05-03
 */
public interface ILeaveMessageService {
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
    public LeaveMessage selectLeaveMessageById(Long id);

    /**
     * 新增留言
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
    public int deleteLeaveMessageById(Long id);

    /**
     * 批量删除留言（软删除）
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteLeaveMessageByIds(Long[] ids);

    /**
     * 修改留言
     *
     * @param leaveMessage 留言对象
     * @return 结果
     */
    public int updateLeaveMessage(LeaveMessage leaveMessage);
}
