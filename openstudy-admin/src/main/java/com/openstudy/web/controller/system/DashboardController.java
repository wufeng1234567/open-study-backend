package com.openstudy.web.controller.system;

import com.openstudy.common.core.controller.BaseController;
import com.openstudy.common.core.domain.AjaxResult;
import com.openstudy.system.service.ISysDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 仪表盘统计Controller
 */
@RestController
@RequestMapping("/system/dashboard")
public class DashboardController extends BaseController {

    @Autowired
    private ISysDashboardService dashboardService;

    /**
     * 获取统计数据
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @GetMapping("/stats")
    public AjaxResult stats() {
        return success(dashboardService.getStats());
    }
}
