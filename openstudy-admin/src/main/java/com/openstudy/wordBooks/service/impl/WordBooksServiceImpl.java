package com.openstudy.wordBooks.service.impl;

import com.openstudy.wordBooks.domain.WordBooks;
import com.openstudy.wordBooks.mapper.WordBooksMapper;
import com.openstudy.wordBooks.service.IWordBooksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 单词本Service业务层处理
 * 
 * @author liu
 * @date 2025-10-24
 */
@Service
public class WordBooksServiceImpl implements IWordBooksService 
{
    @Autowired
    private WordBooksMapper wordBooksMapper;

    /**
     * 查询单词本
     * 
     * @param id 单词本主键
     * @return 单词本
     */
    @Override
    public WordBooks selectWordBooksById(Long id)
    {
        return wordBooksMapper.selectWordBooksById(id);
    }

    /**
     * 查询单词本列表
     * 
     * @param wordBooks 单词本
     * @return 单词本
     */
    @Override
    public List<WordBooks> selectWordBooksList(WordBooks wordBooks)
    {
        return wordBooksMapper.selectWordBooksList(wordBooks);
    }

    /**
     * 新增单词本
     * 
     * @param wordBooks 单词本
     * @return 结果
     */
    @Override
    public int insertWordBooks(WordBooks wordBooks)
    {
        return wordBooksMapper.insertWordBooks(wordBooks);
    }

    /**
     * 修改单词本
     * 
     * @param wordBooks 单词本
     * @return 结果
     */
    @Override
    public int updateWordBooks(WordBooks wordBooks)
    {
        return wordBooksMapper.updateWordBooks(wordBooks);
    }

    /**
     * 批量删除单词本
     * 
     * @param ids 需要删除的单词本主键
     * @return 结果
     */
    @Override
    public int deleteWordBooksByIds(Long[] ids)
    {
        return wordBooksMapper.deleteWordBooksByIds(ids);
    }

    /**
     * 删除单词本信息
     * 
     * @param id 单词本主键
     * @return 结果
     */
    @Override
    public int deleteWordBooksById(Long id)
    {
        return wordBooksMapper.deleteWordBooksById(id);
    }

    @Override
    public List<WordBooks> selectFrontWordBooksList(WordBooks wordBooks) {
        return wordBooksMapper.selectFrontWordBooksList(wordBooks);
    }
}
