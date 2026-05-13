package com.openstudy.questionMain.mapper;

import com.openstudy.questionMain.domain.QuestionMain;
import com.openstudy.questionMain.domain.QuestionSub;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 一级题目Mapper接口
 * 
 * @author ruoyi
 * @date 2025-12-06
 */
public interface QuestionMainMapper
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
     * 删除一级题目
     * 
     * @param id 一级题目主键
     * @return 结果
     */
    public int deleteQuestionMainById(Long id);

    /**
     * 批量删除一级题目
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteQuestionMainByIds(Long[] ids);

    /**
     * 批量删除二级题目（子题）
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteQuestionSubByMainIds(Long[] ids);
    
    /**
     * 批量新增二级题目（子题）
     * 
     * @param questionSubList 二级题目（子题）列表
     * @return 结果
     */
    public int batchQuestionSub(List<QuestionSub> questionSubList);
    

    /**
     * 通过一级题目主键删除二级题目（子题）信息
     * 
     * @param id 一级题目ID
     * @return 结果
     */
    public int deleteQuestionSubByMainId(Long id);


    List<QuestionMain> selectQuestionMainAll(QuestionMain query);

    // ✅ 新增方法：验证题目是否存在
    int checkQuestionExists(@Param("questionId") Long questionId);

    // ✅ 新增方法：获取题目简单信息
    Map<String, Object> selectQuestionSimpleInfoById(@Param("questionId") Long questionId);

    // ✅ 新增方法：根据题目ID列表批量查询题目信息
    List<Map<String, Object>> selectQuestionListByIds(@Param("questionIds") List<Long> questionIds);

    /**
     * 根据题库ID获取题目题干列表
     */
    List<String> selectQuestionTextsByBankId(@Param("bankId") Long bankId, @Param("limit") int limit);
}
