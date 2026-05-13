package com.openstudy.questionMain.service;

import com.openstudy.questionMain.domain.QuestionMain;

import java.util.List;
import java.util.Map;

/**
 * 一级题目Service接口
 * 
 * @author ruoyi
 * @date 2025-12-06
 */
public interface IQuestionMainService 
{
    /**
     * 查询一级题目
     * 
     * @param id 一级题目主键
     * @return 一级题目
     */
    public QuestionMain selectQuestionMainById(Long id);

    /**
     * 查询一级题目列表
     * 
     * @param questionMain 一级题目
     * @return 一级题目集合
     */
    public List<QuestionMain> selectQuestionMainList(QuestionMain questionMain);

    List<QuestionMain> selectQuestionMainAll(QuestionMain query);

    /**
     * 新增一级题目
     * 
     * @param questionMain 一级题目
     * @return 结果
     */
    public int insertQuestionMain(QuestionMain questionMain);

    /**
     * 修改一级题目
     * 
     * @param questionMain 一级题目
     * @return 结果
     */
    public int updateQuestionMain(QuestionMain questionMain);

    /**
     * 批量删除一级题目
     * 
     * @param ids 需要删除的一级题目主键集合
     * @return 结果
     */
    public int deleteQuestionMainByIds(Long[] ids);

    /**
     * 删除一级题目信息
     * 
     * @param id 一级题目主键
     * @return 结果
     */
    public int deleteQuestionMainById(Long id);

    boolean checkQuestionExists(Long questionId);

    Map<String, Object> getQuestionSimpleInfo(Long questionId);

    List<Map<String, Object>> getQuestionListByIds(List<Long> questionIds);
}
