package com.openstudy.questionBank.mapper;

import com.openstudy.questionBank.domain.QuestionBank;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 题库主Mapper接口
 *
 * @author liu
 * @date 2025-12-06
 */
public interface QuestionBankMapper
{
    /**
     * 查询题库主
     *
     * @param id 题库主主键
     * @return 题库主
     */
    public QuestionBank selectQuestionBankById(Long id);

    /**
     * 查询题库主列表
     *
     * @param questionBank 题库主
     * @return 题库主集合
     */
    public List<QuestionBank> selectQuestionBankList(QuestionBank questionBank);

    /**
     * 新增题库主
     *
     * @param questionBank 题库主
     * @return 结果
     */
    public int insertQuestionBank(QuestionBank questionBank);

    /**
     * 修改题库主
     *
     * @param questionBank 题库主
     * @return 结果
     */
    public int updateQuestionBank(QuestionBank questionBank);

    /**
     * 删除题库主
     *
     * @param id 题库主主键
     * @return 结果
     */
    public int deleteQuestionBankById(Long id);

    /**
     * 批量删除题库主
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteQuestionBankByIds(Long[] ids);

    int checkBankExists(Long bankId);

    // ✅ 新增：根据用户ID查询题库
    List<QuestionBank> selectQuestionBankListByUserId(@Param("userId") Long userId);
}
