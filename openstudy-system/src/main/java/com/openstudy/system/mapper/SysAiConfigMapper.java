package com.openstudy.system.mapper;

import com.openstudy.system.domain.SysAiConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * AI模型配置Mapper接口
 *
 * @author openstudy
 */
@Mapper
public interface SysAiConfigMapper {

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
     * 查询用户的指定服务商配置
     *
     * @param userId   用户ID
     * @param provider 服务商
     * @return AI模型配置
     */
    SysAiConfig selectByUserAndProvider(@Param("userId") Long userId, @Param("provider") String provider);

    /**
     * 查询用户的默认模型配置
     *
     * @param userId 用户ID
     * @return AI模型配置
     */
    SysAiConfig selectDefaultByUserId(Long userId);

    /**
     * 查询系统默认配置
     *
     * @return AI模型配置
     */
    SysAiConfig selectSystemDefault();

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
     * 取消用户的默认配置
     *
     * @param userId 用户ID
     * @return 结果
     */
    int clearUserDefault(Long userId);

    /**
     * 测试连接
     *
     * @param sysAiConfig AI模型配置
     * @return 是否成功
     */
    int testConnection(SysAiConfig sysAiConfig);
}
