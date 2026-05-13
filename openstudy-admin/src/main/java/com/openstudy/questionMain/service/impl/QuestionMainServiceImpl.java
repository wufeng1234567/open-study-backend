package com.openstudy.questionMain.service.impl;

import com.openstudy.common.utils.DateUtils;
import com.openstudy.common.utils.StringUtils;
import com.openstudy.questionMain.domain.QuestionMain;
import com.openstudy.questionMain.domain.QuestionSub;
import com.openstudy.questionMain.mapper.QuestionMainMapper;
import com.openstudy.questionMain.service.IQuestionMainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 一级题目Service业务层处理
 * 
 * @author ruoyi
 * @date 2025-12-06
 */
@Service
public class QuestionMainServiceImpl implements IQuestionMainService 
{
    @Autowired
    private QuestionMainMapper questionMainMapper;

    /**
     * 查询一级题目
     * 
     * @param id 一级题目主键
     * @return 一级题目
     */
    @Override
    public QuestionMain selectQuestionMainById(Long id)
    {
        return questionMainMapper.selectQuestionMainById(id);
    }

    /**
     * 查询一级题目列表
     * 
     * @param questionMain 一级题目
     * @return 一级题目
     */
    @Override
    public List<QuestionMain> selectQuestionMainList(QuestionMain questionMain)
    {
        return questionMainMapper.selectQuestionMainList(questionMain);
    }

    @Override
    public List<QuestionMain> selectQuestionMainAll(QuestionMain query) {
        return questionMainMapper.selectQuestionMainAll(query);
    }

    /**
     * 新增一级题目
     * 
     * @param questionMain 一级题目
     * @return 结果
     */
    @Transactional
    @Override
    public int insertQuestionMain(QuestionMain questionMain)
    {
        questionMain.setCreateTime(DateUtils.getNowDate());
        int rows = questionMainMapper.insertQuestionMain(questionMain);
        insertQuestionSub(questionMain);
        return rows;
    }

    /**
     * 修改一级题目
     * 
     * @param questionMain 一级题目
     * @return 结果
     */
    @Transactional
    @Override
    public int updateQuestionMain(QuestionMain questionMain)
    {
        questionMainMapper.deleteQuestionSubByMainId(questionMain.getId());
        insertQuestionSub(questionMain);
        return questionMainMapper.updateQuestionMain(questionMain);
    }

    /**
     * 批量删除一级题目
     * 
     * @param ids 需要删除的一级题目主键
     * @return 结果
     */
    @Transactional
    @Override
    public int deleteQuestionMainByIds(Long[] ids)
    {
        questionMainMapper.deleteQuestionSubByMainIds(ids);
        return questionMainMapper.deleteQuestionMainByIds(ids);
    }

    /**
     * 删除一级题目信息
     * 
     * @param id 一级题目主键
     * @return 结果
     */
    @Transactional
    @Override
    public int deleteQuestionMainById(Long id)
    {
        questionMainMapper.deleteQuestionSubByMainId(id);
        return questionMainMapper.deleteQuestionMainById(id);
    }

    /**
     * 新增二级题目（子题）信息
     * 
     * @param questionMain 一级题目对象
     */
    public void insertQuestionSub(QuestionMain questionMain)
    {
        List<QuestionSub> questionSubList = questionMain.getQuestionSubList();
        Long id = questionMain.getId();
        if (StringUtils.isNotNull(questionSubList))
        {
            List<QuestionSub> list = new ArrayList<QuestionSub>();
            for (QuestionSub questionSub : questionSubList)
            {
                questionSub.setMainId(id);
                list.add(questionSub);
            }
            if (list.size() > 0)
            {
                questionMainMapper.batchQuestionSub(list);
            }
        }
    }
    /**
     * 验证题目是否存在
     * @param questionId 题目ID
     * @return true-存在 false-不存在
     */
    @Override
    public boolean checkQuestionExists(Long questionId) {
        if (questionId == null) {
            return false;
        }
        int count = questionMainMapper.checkQuestionExists(questionId);
        return count > 0;
    }

    /**
     * 获取题目简单信息
     * @param questionId 题目ID
     * @return 题目信息
     */
    @Override
    public Map<String, Object> getQuestionSimpleInfo(Long questionId) {
        return questionMainMapper.selectQuestionSimpleInfoById(questionId);
    }

    /**
     * 批量获取题目信息
     * @param questionIds 题目ID列表
     * @return 题目信息列表
     */
    @Override
    public List<Map<String, Object>> getQuestionListByIds(List<Long> questionIds) {
        if (questionIds == null || questionIds.isEmpty()) {
            return new ArrayList<>();
        }
        return questionMainMapper.selectQuestionListByIds(questionIds);
    }


}
