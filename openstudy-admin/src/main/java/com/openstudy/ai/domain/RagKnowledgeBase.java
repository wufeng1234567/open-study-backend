package com.openstudy.ai.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

@Data
public class RagKnowledgeBase {
    private Long id;
    private Long userId;
    private String name;
    private String description;
    private String icon;
    private Integer isPublic;
    private Integer documentCount;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}