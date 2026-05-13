package com.openstudy.ai.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

@Data
public class RagDocument {
    private Long id;
    private Long knowledgeBaseId;
    private Long userId;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String filePath;
    private String rawContent;
    private Integer chunkCount;
    private Integer status = 1; // 默认状态：1-正常
    private String errorMsg;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}