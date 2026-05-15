package com.openstudy.system.mapper;

import java.util.List;
import java.util.Map;

/**
 * 仪表盘统计Mapper接口
 */
public interface SysDashboardMapper {

    /**
     * 查询用户总数
     */
    int selectUserCount();

    /**
     * 查询题库总数
     */
    int selectQuestionBankCount();

    /**
     * 查询题目总数
     */
    int selectQuestionCount();

    /**
     * 查询笔记总数
     */
    int selectNoteCount();

    /**
     * 查询题库分类统计
     */
    List<Map<String, Object>> selectCategoryStats();

    /**
     * 查询用户增长趋势（近7天）
     */
    List<Map<String, Object>> selectUserGrowth();
}
