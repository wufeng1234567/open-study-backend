package com.openstudy.system.service.impl;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.openstudy.system.domain.SysNoticeRead;
import com.openstudy.system.domain.SysNotice;
import com.openstudy.system.mapper.SysNoticeReadMapper;
import com.openstudy.system.service.ISysNoticeReadService;

/**
 * 公告已读记录 服务层实现
 *
 * @author ruoyi
 */
@Service
public class SysNoticeReadServiceImpl implements ISysNoticeReadService {
    private static final Logger log = LoggerFactory.getLogger(SysNoticeReadServiceImpl.class);
    @Autowired
    private SysNoticeReadMapper noticeReadMapper;

    /**
     * 标记已读
     */
    @Override
    public void markRead(Long noticeId, Long userId) {
        SysNoticeRead record = new SysNoticeRead();
        record.setNoticeId(noticeId);
        record.setUserId(userId);
        noticeReadMapper.insertNoticeRead(record);
        log.info("=== 已读记录插入完成: noticeId={}, userId={}", noticeId, userId);
    }

    /**
     * 查询某用户未读公告数量
     */
    @Override
    public int selectUnreadCount(Long userId, String username) {
        return noticeReadMapper.selectUnreadCount(userId, username);
    }

    /**
     * 统计未读公告数量（别名方法）
     */
    @Override
    public int countUnread(Long userId, String username) {
        return noticeReadMapper.selectUnreadCount(userId, username);
    }

    /**
     * 查询公告列表并标记当前用户已读状态
     */
    @Override
    public List<SysNotice> selectNoticeListWithReadStatus(Long userId, String username, int limit) {
        return noticeReadMapper.selectNoticeListWithReadStatus(userId, username, limit);
    }

    /**
     * 查询全部公告列表并标记当前用户已读状态（无 LIMIT，用于消息中心）
     */
    @Override
    public List<SysNotice> selectAllNoticeListWithReadStatus(Long userId, String username) {
        return noticeReadMapper.selectAllNoticeListWithReadStatus(userId, username);
    }

    /**
     * 批量标记已读
     */
    @Override
    public void markReadBatch(Long userId, Long[] noticeIds) {
        if (noticeIds == null || noticeIds.length == 0) {
            return;
        }
        noticeReadMapper.insertNoticeReadBatch(userId, noticeIds);
    }

    /**
     * 删除公告时清理对应已读记录
     */
    @Override
    public void deleteByNoticeIds(Long[] noticeIds) {
        noticeReadMapper.deleteByNoticeIds(noticeIds);
    }

    /**
     * 前台用户删除单条通知时清理已读记录
     */
    @Override
    public void deleteByNoticeIdAndUserId(Long noticeId, Long userId) {
        noticeReadMapper.deleteByNoticeIdAndUserId(noticeId, userId);
    }

    /**
     * 前台用户批量删除通知时清理已读记录
     */
    @Override
    public void deleteByNoticeIdsAndUserId(Long[] noticeIds, Long userId) {
        noticeReadMapper.deleteByNoticeIdsAndUserId(noticeIds, userId);
    }
}
