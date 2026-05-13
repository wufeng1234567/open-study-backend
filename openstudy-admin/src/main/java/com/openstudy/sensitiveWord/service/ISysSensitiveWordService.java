package com.openstudy.sensitiveWord.service;

import java.util.List;
import com.openstudy.sensitiveWord.domain.SysSensitiveWord;

/**
 * 敏感词管理Service接口
 *
 * @author liu
 * @date 2026-04-17
 */
public interface ISysSensitiveWordService
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
     * 批量删除敏感词管理
     *
     * @param ids 需要删除的敏感词管理主键集合
     * @return 结果
     */
    public int deleteSysSensitiveWordByIds(Long[] ids);

    /**
     * 删除敏感词管理信息
     *
     * @param id 敏感词管理主键
     * @return 结果
     */
    public int deleteSysSensitiveWordById(Long id);

    /**
     * AI生成敏感词并批量入库
     * @param topic 主题（如：涉政、色情、广告等）
     * @param category 分类
     * @param count 期望生成数量
     * @return 成功添加的数量
     */
    public int aiGenerateWords(String topic, String category, int count);
}