package com.openstudy.system.service;

import java.util.Map;

/**
 * 仪表盘统计Service接口
 */
public interface ISysDashboardService {

    /**
     * 获取统计数据
     */
    Map<String, Object> getStats();
}
