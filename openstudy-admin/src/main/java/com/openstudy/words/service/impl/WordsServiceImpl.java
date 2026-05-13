package com.openstudy.words.service.impl;

import com.openstudy.words.domain.Words;
import com.openstudy.words.mapper.WordsMapper;
import com.openstudy.words.service.IWordsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 单词Service业务层处理
 * 
 * @author liu
 * @date 2025-10-24
 */
@Service
public class WordsServiceImpl implements IWordsService 
{
    @Autowired
    private WordsMapper wordsMapper;

    /**
     * 查询单词
     * 
     * @param id 单词主键
     * @return 单词
     */
    @Override
    public Words selectWordsById(Long id)
    {
        return wordsMapper.selectWordsById(id);
    }

    /**
     * 查询单词列表
     * 
     * @param words 单词
     * @return 单词
     */
    @Override
    public List<Words> selectWordsList(Words words)
    {
        return wordsMapper.selectWordsList(words);
    }

    /**
     * 新增单词
     * 
     * @param words 单词
     * @return 结果
     */
    @Override
    public int insertWords(Words words)
    {
        return wordsMapper.insertWords(words);
    }

    /**
     * 修改单词
     * 
     * @param words 单词
     * @return 结果
     */
    @Override
    public int updateWords(Words words)
    {
        return wordsMapper.updateWords(words);
    }

    /**
     * 批量删除单词
     * 
     * @param ids 需要删除的单词主键
     * @return 结果
     */
    @Override
    public int deleteWordsByIds(Long[] ids)
    {
        return wordsMapper.deleteWordsByIds(ids);
    }

    /**
     * 删除单词信息
     * 
     * @param id 单词主键
     * @return 结果
     */
    @Override
    public int deleteWordsById(Long id)
    {
        return wordsMapper.deleteWordsById(id);
    }
}
