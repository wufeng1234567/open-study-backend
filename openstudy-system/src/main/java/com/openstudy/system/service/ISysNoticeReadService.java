package com.openstudy.system.service;

import java.util.List;
import com.openstudy.system.domain.SysNotice;

/**
 * 公告已读记录 服务层
 *
 * @author ruoyi
 */
public interface ISysNoticeReadService {
    /**
     * 标记已读（幂等，重复调用不报错）
     *
     * @param noticeId 公告ID
     * @param userId   用户ID
     */
    public void markRead(Long noticeId, Long userId);

    /**
     * 查询某用户未读公告数量
     *
     * @param userId   用户ID
     * @param username 用户名
     * @return 未读数量
     */
    public int selectUnreadCount(Long userId, String username);

    /**
     * 统计未读公告数量（别名方法，与 selectUnreadCount 功能相同）
     *
     * @param userId   用户ID
     * @param username 用户名
     * @return 未读数量
     */
    public int countUnread(Long userId, String username);

    /**
     * 查询公告列表并标记当前用户已读状态（用于首页展示）
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param limit    最多返回条数
     * @return 带 isRead 标记的公告列表
     */
    public List<SysNotice> selectNoticeListWithReadStatus(Long userId, String username, int limit);

    /**
     * 查询全部公告列表并标记当前用户已读状态（无 LIMIT，用于消息中心）
     *
     * @param userId   用户ID
     * @param username 用户名
     * @return 带 isRead 标记的全部公告列表
     */
    public List<SysNotice> selectAllNoticeListWithReadStatus(Long userId, String username);

    /**
     * 批量标记已读
     *
     * @param userId    用户ID
     * @param noticeIds 公告ID数组
     */
    public void markReadBatch(Long userId, Long[] noticeIds);

    /**
     * 删除公告时清理对应已读记录
     *
     * @param noticeIds 公告ID数组
     */
    public void deleteByNoticeIds(Long[] noticeIds);

    /**
     * 前台用户删除单条通知时清理已读记录
     *
     * @param noticeId 公告ID
     * @param userId   用户ID
     */
    public void deleteByNoticeIdAndUserId(Long noticeId, Long userId);

    /**
     * 前台用户批量删除通知时清理已读记录
     *
     * @param noticeIds 公告ID数组
     * @param userId    用户ID
     */
    public void deleteByNoticeIdsAndUserId(Long[] noticeIds, Long userId);
}
