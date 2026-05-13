package com.openstudy.sensitiveWord.mapper;

import java.util.List;
import com.openstudy.sensitiveWord.domain.SysSensitiveWord;

/**
 * 敏感词管理Mapper接口
 * 
 * @author liu
 * @date 2026-04-17
 */
public interface SysSensitiveWordMapper 
{
    /**
     * 查询敏感词管理
     * 
     * @param id 敏感词管理主键
     * @return 敏感词管理
     */
    public SysSensitiveWord selectSysSensitiveWordById(Long id);

    /**
     * 查询敏感词管理列表
     * 
     * @param sysSensitiveWord 敏感词管理
     * @return 敏感词管理集合
     */
    public List<SysSensitiveWord> selectSysSensitiveWordList(SysSensitiveWord sysSensitiveWord);

    /**
     * 新增敏感词管理
     * 
     * @param sysSensitiveWord 敏感词管理
     * @return 结果
     */
    public int insertSysSensitiveWord(SysSensitiveWord sysSensitiveWord);

    /**
     * 修改敏感词管理
     * 
     * @param sysSensitiveWord 敏感词管理
     * @return 结果
     */
    public int updateSysSensitiveWord(SysSensitiveWord sysSensitiveWord);

    /**
     * 删除敏感词管理
     * 
     * @param id 敏感词管理主键
     * @return 结果
     */
    public int deleteSysSensitiveWordById(Long id);

    /**
     * 批量删除敏感词管理
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteSysSensitiveWordByIds(Long[] ids);
}
