package com.openstudy.system.domain;

import lombok.Data;
import java.util.Date;

@Data
public class UserAiPreference {
    private Long id;
    private Long userId;
    private String preferSubject;
    private String preferQuestionTypes;  // JSON字符串
    private Integer preferDifficulty;
    private Integer preferOptionsCount;
    private Integer preferWithAnalysis;
    private String commonKnowledgePoints;  // JSON字符串
    private Date createTime;
    private Date updateTime;
}