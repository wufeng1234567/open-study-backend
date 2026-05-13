package com.openstudy.wordBooks.mapper;

import com.openstudy.wordBooks.domain.WordBooks;

import java.util.List;

/**
 * 单词本Mapper接口
 * 
 * @author liu
 * @date 2025-10-24
 */
public interface WordBooksMapper 
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
     * 删除单词本
     * 
     * @param id 单词本主键
     * @return 结果
     */
    public int deleteWordBooksById(Long id);

    /**
     * 批量删除单词本
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteWordBooksByIds(Long[] ids);

    /**
     * 前台查询用户可见的词库列表
     * @param wordBooks 查询参数（必须包含 userId）
     * @return 词库列表
     */
    public List<WordBooks> selectFrontWordBooksList(WordBooks wordBooks);
}
