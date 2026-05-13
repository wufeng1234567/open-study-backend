package com.openstudy.notes.controller;

import com.openstudy.common.core.controller.BaseController;
import com.openstudy.common.core.domain.AjaxResult;
import com.openstudy.common.utils.SecurityUtils;
import com.openstudy.common.utils.ip.IpUtils;
import com.openstudy.notes.domain.LeaveMessage;
import com.openstudy.notes.service.ILeaveMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 留言板Controller
 *
 * @author openstudy
 * @date 2026-05-03
 */
@Tag(name = "留言板管理")
@RestController
@RequestMapping("/leaveMessage")
@RequiredArgsConstructor
public class LeaveMessageController extends BaseController {
    private final ILeaveMessageService leaveMessageService;

    /**
     * 查询留言列表（前台用 - 已登录用户）
     */
    @Operation(summary = "查询留言列表")
    @PreAuthorize("@ss.hasRole('common')")
    @GetMapping("/list")
    public AjaxResult list() {
        List<LeaveMessage> list = leaveMessageService.selectLeaveMessageList(new LeaveMessage());
        return success(list);
    }

    /**
     * 发表评论留言
     */
    @Operation(summary = "发表评论留言")
    @PreAuthorize("@ss.hasRole('common')")
    @PostMapping("/add")
    public AjaxResult add(@RequestBody LeaveMessage leaveMessage, HttpServletRequest request) {
        leaveMessage.setUserId(SecurityUtils.getUserId());
        leaveMessage.setUserName(SecurityUtils.getUsername());
        leaveMessage.setIp(IpUtils.getIpAddr(request));
        int result = leaveMessageService.insertLeaveMessage(leaveMessage);
        if (result > 0) {
            return success(leaveMessage);
        }
        return error("留言失败");
    }

    /**
     * 后台管理：查询所有留言列表
     */
    @Operation(summary = "后台管理：查询留言列表")
    @PreAuthorize("@ss.hasRole('admin')")
    @GetMapping("/admin/list")
    public AjaxResult adminList(@RequestParam(required = false) String userName,
            @RequestParam(required = false) String content) {
        LeaveMessage query = new LeaveMessage();
        query.setUserName(userName);
        query.setContent(content);
        List<LeaveMessage> list = leaveMessageService.selectLeaveMessageList(query);
        return success(list);
    }

    /**
     * 后台管理：新增留言
     */
    @Operation(summary = "后台管理：新增留言")
    @PreAuthorize("@ss.hasRole('admin')")
    @PostMapping("/admin/add")
    public AjaxResult adminAdd(@RequestBody LeaveMessage leaveMessage) {
        leaveMessage.setUserId(SecurityUtils.getUserId());
        leaveMessage.setUserName(SecurityUtils.getUsername());
        leaveMessage.setStatus(1);
        int result = leaveMessageService.insertLeaveMessage(leaveMessage);
        if (result > 0) {
            return success(leaveMessage);
        }
        return error("新增失败");
    }

    /**
     * 后台管理：删除留言
     */
    @Operation(summary = "后台管理：删除留言")
    @PreAuthorize("@ss.hasPermi('system:message:remove')")
    @DeleteMapping("/{id}")
    public AjaxResult delete(@PathVariable Long id) {
        int result = leaveMessageService.deleteLeaveMessageById(id);
        if (result > 0) {
            return success();
        }
        return error("删除失败");
    }

    /**
     * 后台管理：修改留言
     */
    @Operation(summary = "后台管理：修改留言")
    @PreAuthorize("@ss.hasPermi('system:message:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody LeaveMessage leaveMessage) {
        int result = leaveMessageService.updateLeaveMessage(leaveMessage);
        if (result > 0) {
            return success();
        }
        return error("修改失败");
    }
}
