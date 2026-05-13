package com.openstudy.words.service;

import com.openstudy.words.domain.Words;

import java.util.List;

/**
 * 单词Service接口
 * 
 * @author liu
 * @date 2025-10-24
 */
public interface IWordsService 
{
    /**
     * 查询单词
     * 
     * @param id 单词主键
     * @return 单词
     */
    public Words selectWordsById(Long id);

    /**
     * 查询单词列表
     * 
     * @param words 单词
     * @return 单词集合
     */
    public List<Words> selectWordsList(Words words);

    /**
     * 新增单词
     * 
     * @param words 单词
     * @return 结果
     */
    public int insertWords(Words words);

    /**
     * 修改单词
     * 
     * @param words 单词
     * @return 结果
     */
    public int updateWords(Words words);

    /**
     * 批量删除单词
     * 
     * @param ids 需要删除的单词主键集合
     * @return 结果
     */
    public int deleteWordsByIds(Long[] ids);

    /**
     * 删除单词信息
     * 
     * @param id 单词主键
     * @return 结果
     */
    public int deleteWordsById(Long id);
}
