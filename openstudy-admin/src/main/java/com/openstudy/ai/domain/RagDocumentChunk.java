package com.openstudy.ai.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

@Data
public class RagDocumentChunk {
    private Long id;
    private Long documentId;
    private Integer chunkIndex;
    private String content;
    private Integer contentLength;
    private String vectorId;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}