package com.openstudy.questionMain.domain;

import com.openstudy.common.annotation.Excel;
import com.openstudy.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.math.BigDecimal;

/**
 * 二级题目（子题）对象 question_sub
 * 
 * @author ruoyi
 * @date 2025-12-06
 */
public class QuestionSub extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 子题ID */
    private Long id;

    /** 所属主题ID（外键→question_main.id） */
    @Excel(name = "所属主题ID", readConverterExp = "外=键→question_main.id")
    private Long mainId;

    /** 子题题干 */
    @Excel(name = "子题题干")
    private String questionText;

    /** 题型：1单选 2多选 3判断 4填空 5简答 */
    @Excel(name = "题型：1单选 2多选 3判断 4填空 5简答")
    private Long questionType;

    /** 选项（JSON格式，选择题用） */
    @Excel(name = "选项", readConverterExp = "J=SON格式，选择题用")
    private String options;

    /** 答案 */
    @Excel(name = "答案")
    private String answer;

    /** 解析 */
    @Excel(name = "解析")
    private String analysis;

    /** 子题排序 */
    @Excel(name = "子题排序")
    private Long sortOrder;

    /** 分值 */
    @Excel(name = "分值")
    private BigDecimal score;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }
    public void setMainId(Long mainId) 
    {
        this.mainId = mainId;
    }

    public Long getMainId() 
    {
        return mainId;
    }
    public void setQuestionText(String questionText) 
    {
        this.questionText = questionText;
    }

    public String getQuestionText() 
    {
        return questionText;
    }
    public void setQuestionType(Long questionType) 
    {
        this.questionType = questionType;
    }

    public Long getQuestionType() 
    {
        return questionType;
    }
    public void setOptions(String options) 
    {
        this.options = options;
    }

    public String getOptions() 
    {
        return options;
    }
    public void setAnswer(String answer) 
    {
        this.answer = answer;
    }

    public String getAnswer() 
    {
        return answer;
    }
    public void setAnalysis(String analysis) 
    {
        this.analysis = analysis;
    }

    public String getAnalysis() 
    {
        return analysis;
    }
    public void setSortOrder(Long sortOrder) 
    {
        this.sortOrder = sortOrder;
    }

    public Long getSortOrder() 
    {
        return sortOrder;
    }
    public void setScore(BigDecimal score) 
    {
        this.score = score;
    }

    public BigDecimal getScore() 
    {
        return score;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("mainId", getMainId())
            .append("questionText", getQuestionText())
            .append("questionType", getQuestionType())
            .append("options", getOptions())
            .append("answer", getAnswer())
            .append("analysis", getAnalysis())
            .append("sortOrder", getSortOrder())
            .append("score", getScore())
            .toString();
    }
}
