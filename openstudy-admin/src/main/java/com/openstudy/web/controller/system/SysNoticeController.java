package com.openstudy.web.controller.system;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.openstudy.common.annotation.Log;
import com.openstudy.common.core.controller.BaseController;
import com.openstudy.common.core.domain.AjaxResult;
import com.openstudy.common.core.page.TableDataInfo;
import com.openstudy.common.core.text.Convert;
import com.openstudy.common.enums.BusinessType;
import com.openstudy.system.domain.SysNotice;
import com.openstudy.system.service.ISysNoticeReadService;
import com.openstudy.system.service.ISysNoticeService;
import lombok.extern.slf4j.Slf4j;

/**
 * 公告 信息操作处理
 * 
 * @author ruoyi
 */
@Slf4j
@RestController
@RequestMapping("/system/notice")
public class SysNoticeController extends BaseController {
    @Autowired
    private ISysNoticeService noticeService;

    @Autowired
    private ISysNoticeReadService noticeReadService;

    /**
     * 获取通知公告列表
     */
    @PreAuthorize("@ss.hasPermi('system:notice:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysNotice notice) {
        startPage();
        List<SysNotice> list = noticeService.selectNoticeList(notice);
        return getDataTable(list);
    }

    /**
     * 根据通知公告编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:notice:query')")
    @GetMapping(value = "/{noticeId}")
    public AjaxResult getInfo(@PathVariable Long noticeId) {
        return success(noticeService.selectNoticeById(noticeId));
    }

    /**
     * 新增通知公告
     */
    @PreAuthorize("@ss.hasPermi('system:notice:add')")
    @Log(title = "通知公告", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysNotice notice) {
        notice.setCreateBy(getUsername());
        return toAjax(noticeService.insertNotice(notice));
    }

    /**
     * 修改通知公告
     */
    @PreAuthorize("@ss.hasPermi('system:notice:edit')")
    @Log(title = "通知公告", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysNotice notice) {
        notice.setUpdateBy(getUsername());
        return toAjax(noticeService.updateNotice(notice));
    }

    /**
     * 首页顶部公告列表（返回全部正常公告，带当前用户已读标记，最多5条）
     */
    @GetMapping("/listTop")
    @ResponseBody
    public AjaxResult listTop() {
        Long userId = getUserId();
        String username = getUsername();
        List<SysNotice> list = noticeReadService.selectNoticeListWithReadStatus(userId, username, 5);
        long unreadCount = list.stream().filter(n -> !n.getIsRead()).count();
        AjaxResult result = AjaxResult.success(list);
        result.put("unreadCount", unreadCount);

        // 打印调试日志
        log.info("=== listTop 接口返回数据 ===");
        log.info("userId: {}, username: {}", userId, username);
        log.info("公告列表大小: {}", list.size());
        log.info("未读数量: {}", unreadCount);

        // 打印每个公告的 isRead 状态
        for (int i = 0; i < list.size(); i++) {
            SysNotice notice = list.get(i);
            log.info("  [{}] noticeId={}, title={}, isRead={}",
                    i + 1, notice.getNoticeId(), notice.getNoticeTitle(), notice.getIsRead());
        }

        // 打印完整 JSON 响应
        log.info("完整 JSON 响应:\n{}", JSON.toJSONString(result, JSONWriter.Feature.PrettyFormat));

        return result;
    }

    /**
     * 消息中心全部公告列表（返回全部正常公告，带当前用户已读标记，无 LIMIT，无需登录）
     */
    @GetMapping("/listAll")
    @ResponseBody
    public AjaxResult listAll() {
        Long userId = getUserId();
        String username = getUsername();
        List<SysNotice> list = noticeReadService.selectAllNoticeListWithReadStatus(userId, username);
        long unreadCount = list.stream().filter(n -> !n.getIsRead()).count();
        AjaxResult result = AjaxResult.success(list);
        result.put("unreadCount", unreadCount);
        log.info("=== listAll 返回: userId={}, username={}, 总条数={}", userId, username, list.size());
        for (SysNotice n : list) {
            log.info("  noticeId={}, isRead={}, remark={}", n.getNoticeId(), n.getIsRead(), n.getRemark());
        }
        return result;
    }

    /**
     * 查询未读公告数量
     */
    @GetMapping("/unreadCount")
    @ResponseBody
    public AjaxResult unreadCount() {
        Long userId = getUserId();
        String username = getUsername();
        int count = noticeReadService.countUnread(userId, username);
        return success(count);
    }

    /**
     * 标记公告已读
     */
    @PostMapping("/markRead")
    @ResponseBody
    public AjaxResult markRead(Long noticeId) {
        Long userId = getUserId();
        log.info("=== markRead 被调用: noticeId={}, userId={}", noticeId, userId);
        noticeReadService.markRead(noticeId, userId);
        return success();
    }

    /**
     * 批量标记已读
     */
    @PostMapping("/markReadAll")
    @ResponseBody
    public AjaxResult markReadAll(String ids) {
        Long userId = getUserId();
        Long[] noticeIds = Convert.toLongArray(ids);
        log.info("=== markReadBatch 被调用: userId={}, noticeIds={}", userId, java.util.Arrays.toString(noticeIds));
        noticeReadService.markReadBatch(userId, noticeIds);
        return success();
    }

    /**
     * 前台用户删除单条通知
     */
    @PreAuthorize("@ss.hasRole('common')")
    @DeleteMapping("/front/{noticeId}")
    public AjaxResult deleteFront(@PathVariable Long noticeId) {
        Long userId = getUserId();
        log.info("=== deleteFront 被调用: noticeId={}, userId={}", noticeId, userId);
        noticeReadService.deleteByNoticeIdAndUserId(noticeId, userId);
        return toAjax(noticeService.deleteNoticeById(noticeId));
    }

    /**
     * 前台用户批量删除通知
     */
    @PreAuthorize("@ss.hasRole('common')")
    @DeleteMapping("/front/batch/{noticeIds}")
    public AjaxResult deleteFrontBatch(@PathVariable Long[] noticeIds) {
        Long userId = getUserId();
        log.info("=== deleteFrontBatch 被调用: noticeIds={}, userId={}", java.util.Arrays.toString(noticeIds), userId);
        noticeReadService.deleteByNoticeIdsAndUserId(noticeIds, userId);
        return toAjax(noticeService.deleteNoticeByIds(noticeIds));
    }

    /**
     * 删除通知公告
     */
    @PreAuthorize("@ss.hasPermi('system:notice:remove')")
    @Log(title = "通知公告", businessType = BusinessType.DELETE)
    @DeleteMapping("/{noticeIds}")
    public AjaxResult remove(@PathVariable Long[] noticeIds) {
        noticeReadService.deleteByNoticeIds(noticeIds);
        return toAjax(noticeService.deleteNoticeByIds(noticeIds));
    }
}
