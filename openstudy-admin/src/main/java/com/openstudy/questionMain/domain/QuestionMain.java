package com.openstudy.questionMain.domain;

import com.openstudy.common.annotation.Excel;
import com.openstudy.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * 一级题目对象 question_main
 * 
 * @author ruoyi
 * @date 2025-12-06
 */
@Data
@ToString
public class QuestionMain extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 题目ID */
    @Excel(name = "题目ID")
    private Long id;

    /** 所属题库ID（外键→question_bank.id） */
    @Excel(name = "所属题库ID", readConverterExp = "外=键→question_bank.id")
    private Long bankId;

    /** 题型：1单选 2多选 3判断 4填空 5简答 6阅读理解 7完形填空 */
    @Excel(name = "题型：1单选 2多选 3判断 4填空 5简答 6阅读理解 7完形填空")
    private Long questionType;

    /** 难度：1简单 2中等 3困难 4极难 */
    @Excel(name = "难度：1简单 2中等 3困难 4极难")
    private Long difficulty;

    /** 题干（对于阅读理解，这里是文章标题） */
    @Excel(name = "题干", readConverterExp = "对=于阅读理解，这里是文章标题")
    private String questionText;

    /** 题目内容（阅读理解的文章内容） */
    @Excel(name = "题目内容", readConverterExp = "阅=读理解的文章内容")
    private String content;

    /** 标准答案（简答题答案或阅读理解的总答案） */
    @Excel(name = "标准答案", readConverterExp = "简=答题答案或阅读理解的总答案")
    private String answer;

    /** 解析 */
    @Excel(name = "解析")
    private String analysis;

    /** 是否有子题：0无 1有 */
    @Excel(name = "是否有子题：0无 1有")
    private Long hasSubQuestions;

    /** 排序 */
    @Excel(name = "排序")
    private Long sortOrder;

    /** 排分数*/
    @Excel(name = "score")
    private Double score;

    /** 二级题目（子题）信息 */
    private List<QuestionSub> questionSubList;

    /** 选项（JSON格式，选择题用） */
    @Excel(name = "选项", readConverterExp = "J=SON格式，选择题用")
    private String options;

    /** 状态：0正常 1停用 */
    @Excel(name = "status")
    private String status;

}
