package com.openstudy.questionBank.service.impl;

import com.openstudy.common.utils.DateUtils;
import com.openstudy.questionBank.domain.QuestionBank;
import com.openstudy.questionBank.mapper.QuestionBankMapper;
import com.openstudy.questionBank.service.IQuestionBankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 题库主Service业务层处理
 * 
 * @author liu
 * @date 2025-12-06
 */
@Service
public class QuestionBankServiceImpl implements IQuestionBankService
{
    @Autowired
    private QuestionBankMapper questionBankMapper;

    /**
     * 查询题库主
     * 
     * @param id 题库主主键
     * @return 题库主
     */
    @Override
    public QuestionBank selectQuestionBankById(Long id)
    {
        return questionBankMapper.selectQuestionBankById(id);
    }

    /**
     * 查询题库主列表
     * 
     * @param questionBank 题库主
     * @return 题库主
     */
    @Override
    public List<QuestionBank> selectQuestionBankList(QuestionBank questionBank)
    {
        return questionBankMapper.selectQuestionBankList(questionBank);
    }

    /**
     * 新增题库主
     * 
     * @param questionBank 题库主
     * @return 结果
     */
    @Override
    public int insertQuestionBank(QuestionBank questionBank)
    {
        questionBank.setCreateTime(DateUtils.getNowDate());
        return questionBankMapper.insertQuestionBank(questionBank);
    }

    /**
     * 修改题库主
     * 
     * @param questionBank 题库主
     * @return 结果
     */
    @Override
    public int updateQuestionBank(QuestionBank questionBank)
    {
        return questionBankMapper.updateQuestionBank(questionBank);
    }

    /**
     * 批量删除题库主
     * 
     * @param ids 需要删除的题库主主键
     * @return 结果
     */
    @Override
    public int deleteQuestionBankByIds(Long[] ids)
    {
        return questionBankMapper.deleteQuestionBankByIds(ids);
    }

    /**
     * 删除题库主信息
     * 
     * @param id 题库主主键
     * @return 结果
     */
    @Override
    public int deleteQuestionBankById(Long id)
    {
        return questionBankMapper.deleteQuestionBankById(id);
    }

    @Override
    public boolean checkBankExists(Long bankId) {
        int count  = questionBankMapper.checkBankExists(bankId);
       return count > 0;
    }

    // ✅ 新增：根据用户ID查询题库
    @Override
    public List<QuestionBank> selectQuestionBankListByUserId(Long userId)
    {
        return questionBankMapper.selectQuestionBankListByUserId(userId);
    }
}
