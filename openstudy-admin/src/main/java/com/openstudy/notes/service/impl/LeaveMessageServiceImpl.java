package com.openstudy.notes.service.impl;

import com.openstudy.notes.domain.LeaveMessage;
import com.openstudy.notes.mapper.LeaveMessageMapper;
import com.openstudy.notes.service.ILeaveMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 留言板Service实现
 *
 * @author openstudy
 * @date 2026-05-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LeaveMessageServiceImpl implements ILeaveMessageService {
    private final LeaveMessageMapper leaveMessageMapper;

    /**
     * 查询留言列表
     */
    @Override
    public List<LeaveMessage> selectLeaveMessageList(LeaveMessage leaveMessage) {
        return leaveMessageMapper.selectLeaveMessageList(leaveMessage);
    }

    /**
     * 查询留言详情
     */
    @Override
    public LeaveMessage selectLeaveMessageById(Long id) {
        return leaveMessageMapper.selectLeaveMessageById(id);
    }

    /**
     * 新增留言
     */
    @Override
    public int insertLeaveMessage(LeaveMessage leaveMessage) {
        leaveMessage.setStatus(1);
        return leaveMessageMapper.insertLeaveMessage(leaveMessage);
    }

    /**
     * 删除留言
     */
    @Override
    public int deleteLeaveMessageById(Long id) {
        return leaveMessageMapper.deleteLeaveMessageById(id);
    }

    /**
     * 批量删除留言
     */
    @Override
    public int deleteLeaveMessageByIds(Long[] ids) {
        return leaveMessageMapper.deleteLeaveMessageByIds(ids);
    }

    /**
     * 修改留言
     */
    @Override
    public int updateLeaveMessage(LeaveMessage leaveMessage) {
        return leaveMessageMapper.updateLeaveMessage(leaveMessage);
    }
}
