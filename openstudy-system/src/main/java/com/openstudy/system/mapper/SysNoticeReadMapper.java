package com.openstudy.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.openstudy.system.domain.SysNoticeRead;
import com.openstudy.system.domain.SysNotice;

/**
 * 公告已读记录 数据层
 *
 * @author ruoyi
 */
public interface SysNoticeReadMapper {
    /**
     * 新增已读记录（忽略重复）
     *
     * @param noticeRead 已读记录
     * @return 结果
     */
    public int insertNoticeRead(SysNoticeRead noticeRead);

    /**
     * 查询某用户未读公告数量
     *
     * @param userId   用户ID
     * @param username 用户名
     * @return 未读数量
     */
    public int selectUnreadCount(@Param("userId") Long userId, @Param("username") String username);

    /**
     * 查询某用户是否已读某公告
     *
     * @param noticeId 公告ID
     * @param userId   用户ID
     * @return 已读记录数（0未读 1已读）
     */
    public int selectIsRead(@Param("noticeId") Long noticeId, @Param("userId") Long userId);

    /**
     * 批量标记已读
     *
     * @param userId    用户ID
     * @param noticeIds 公告ID数组
     * @return 结果
     */
    public int insertNoticeReadBatch(@Param("userId") Long userId, @Param("noticeIds") Long[] noticeIds);

    /**
     * 查询带已读状态的公告列表（SQL层限制条数，一次查询完成）
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param limit    最多返回条数
     * @return 带 isRead 标记的公告列表
     */
    public List<SysNotice> selectNoticeListWithReadStatus(@Param("userId") Long userId,
            @Param("username") String username, @Param("limit") int limit);

    /**
     * 查询全部带已读状态的公告列表（无 LIMIT 限制，用于消息中心）
     *
     * @param userId   用户ID
     * @param username 用户名
     * @return 带 isRead 标记的全部公告列表
     */
    public List<SysNotice> selectAllNoticeListWithReadStatus(@Param("userId") Long userId,
            @Param("username") String username);

    /**
     * 公告删除时清理对应已读记录
     *
     * @param noticeIds 公告ID数组
     * @return 结果
     */
    public int deleteByNoticeIds(@Param("noticeIds") Long[] noticeIds);

    /**
     * 前台用户删除单条通知时清理已读记录
     *
     * @param noticeId 公告ID
     * @param userId   用户ID
     * @return 结果
     */
    public int deleteByNoticeIdAndUserId(@Param("noticeId") Long noticeId, @Param("userId") Long userId);

    /**
     * 前台用户批量删除通知时清理已读记录
     *
     * @param noticeIds 公告ID数组
     * @param userId    用户ID
     * @return 结果
     */
    public int deleteByNoticeIdsAndUserId(@Param("noticeIds") Long[] noticeIds, @Param("userId") Long userId);
}
