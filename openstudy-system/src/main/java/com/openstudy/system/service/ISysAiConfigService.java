package com.openstudy.system.service;

import com.openstudy.system.domain.SysAiConfig;

import java.util.List;

/**
 * AI模型配置Service接口
 *
 * @author openstudy
 */
public interface ISysAiConfigService {

    /**
     * 查询AI模型配置
     *
     * @param configId 配置ID
     * @return AI模型配置
     */
    SysAiConfig selectSysAiConfigById(Long configId);

    /**
     * 查询AI模型配置列表
     *
     * @param sysAiConfig AI模型配置
     * @return AI模型配置集合
     */
    List<SysAiConfig> selectSysAiConfigList(SysAiConfig sysAiConfig);

    /**
     * 查询用户的AI模型配置列表
     *
     * @param userId 用户ID
     * @return AI模型配置列表
     */
    List<SysAiConfig> selectByUserId(Long userId);

    /**
     * 查询用户的默认模型配置（优先用户配置 > 系统默认）
     *
     * @param userId 用户ID
     * @return AI模型配置
     */
    SysAiConfig selectUserEffectiveConfig(Long userId);

    /**
     * 查询用户对指定提供商的自定义配置
     *
     * @param userId   用户ID
     * @param provider 提供商
     * @return AI模型配置
     */
    SysAiConfig selectByUserAndProvider(Long userId, String provider);

    /**
     * 新增AI模型配置
     *
     * @param sysAiConfig AI模型配置
     * @return 结果
     */
    int insertSysAiConfig(SysAiConfig sysAiConfig);

    /**
     * 修改AI模型配置
     *
     * @param sysAiConfig AI模型配置
     * @return 结果
     */
    int updateSysAiConfig(SysAiConfig sysAiConfig);

    /**
     * 删除AI模型配置
     *
     * @param configId 配置ID
     * @return 结果
     */
    int deleteSysAiConfigById(Long configId);

    /**
     * 批量删除AI模型配置
     *
     * @param configIds 需要删除的配置ID
     * @return 结果
     */
    int deleteSysAiConfigByIds(Long[] configIds);

    /**
     * 设置为用户默认模型
     *
     * @param configId 配置ID
     * @param userId   用户ID
     * @return 结果
     */
    boolean setAsDefault(Long configId, Long userId);

    /**
     * 测试连接
     *
     * @param sysAiConfig AI模型配置
     * @return 测试结果（成功返回null，失败返回错误信息）
     */
    String testConnection(SysAiConfig sysAiConfig);
}
