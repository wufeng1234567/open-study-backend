package com.openstudy.system.service.impl;

import com.openstudy.system.mapper.SysDashboardMapper;
import com.openstudy.system.service.ISysDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 仪表盘统计Service实现
 */
@Service
public class SysDashboardServiceImpl implements ISysDashboardService {

    @Autowired
    private SysDashboardMapper dashboardMapper;

    @Override
    public Map<String, Object> getStats() {
        Map<String, Object> result = new HashMap<>();

        // 基础统计
        result.put("userCount", dashboardMapper.selectUserCount());
        result.put("questionBankCount", dashboardMapper.selectQuestionBankCount());
        result.put("questionCount", dashboardMapper.selectQuestionCount());
        result.put("noteCount", dashboardMapper.selectNoteCount());

        // 题库分类统计
        result.put("categoryStats", dashboardMapper.selectCategoryStats());

        // 用户增长趋势（近7天）
        result.put("userGrowth", dashboardMapper.selectUserGrowth());

        return result;
    }
}
