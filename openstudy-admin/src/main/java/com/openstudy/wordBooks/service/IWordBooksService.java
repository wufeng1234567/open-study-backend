package com.openstudy.wordBooks.service;

import com.openstudy.wordBooks.domain.WordBooks;

import java.util.List;

/**
 * 单词本Service接口
 * 
 * @author liu
 * @date 2025-10-24
 */
public interface IWordBooksService 
{
    /**
     * 查询单词本
     * 
     * @param id 单词本主键
     * @return 单词本
     */
    public WordBooks selectWordBooksById(Long id);

    /**
     * 查询单词本列表
     * 
     * @param wordBooks 单词本
     * @return 单词本集合
     */
    public List<WordBooks> selectWordBooksList(WordBooks wordBooks);

    /**
     * 新增单词本
     * 
     * @param wordBooks 单词本
     * @return 结果
     */
    public int insertWordBooks(WordBooks wordBooks);

    /**
     * 修改单词本
     * 
     * @param wordBooks 单词本
     * @return 结果
     */
    public int updateWordBooks(WordBooks wordBooks);

    /**
     * 批量删除单词本
     * 
     * @param ids 需要删除的单词本主键集合
     * @return 结果
     */
    public int deleteWordBooksByIds(Long[] ids);

    /**
     * 删除单词本信息
     * 
     * @param id 单词本主键
     * @return 结果
     */
    public int deleteWordBooksById(Long id);

    /**
     * 前台查询用户可见的词库列表
     * @param wordBooks 查询参数
     * @return 词库列表
     */
    public List<WordBooks> selectFrontWordBooksList(WordBooks wordBooks);

}
