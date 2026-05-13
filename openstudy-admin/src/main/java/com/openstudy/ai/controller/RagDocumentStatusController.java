package com.openstudy.ai.controller;

import com.openstudy.ai.domain.RagDocument;
import com.openstudy.ai.mapper.RagDocumentMapper;
import com.openstudy.common.core.controller.BaseController;
import com.openstudy.common.core.domain.AjaxResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * RAG 文档状态查询 Controller
 */
@Slf4j
@Tag(name = "RAG文档状态查询")
@RestController
@RequestMapping("/rag/document")
@RequiredArgsConstructor
public class RagDocumentStatusController extends BaseController {

    private final RagDocumentMapper ragDocumentMapper;

    @Operation(summary = "查询文档处理状态")
    @GetMapping("/status/{documentId}")
    public AjaxResult getDocumentStatus(@PathVariable Long documentId) {
        log.info("查询文档状态，documentId: {}", documentId);

        try {
            // 1. 查询文档
            RagDocument doc = ragDocumentMapper.selectById(documentId);
            if (doc == null) {
                return error("文档不存在");
            }

            // 2. 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("documentId", documentId);
            result.put("fileName", doc.getFileName());
            result.put("status", doc.getStatus());
            result.put("chunkCount", doc.getChunkCount() != null ? doc.getChunkCount() : 0);
            result.put("errorMsg", doc.getErrorMsg());
            
            // 判断是否完成（status >= 3 表示解析完成或已向量化）
            boolean isCompleted = doc.getStatus() != null && doc.getStatus() >= 3;
            result.put("isCompleted", isCompleted);
            
            // 状态描述
            String statusDesc = getStatusDescription(doc.getStatus());
            result.put("statusDesc", statusDesc);

            log.info("文档状态: {}, 分块数: {}, 是否完成: {}", 
                    statusDesc, doc.getChunkCount(), isCompleted);

            return success(result);

        } catch (Exception e) {
            log.error("查询文档状态失败", e);
            return error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取状态描述
     */
    private String getStatusDescription(Integer status) {
        if (status == null) {
            return "未知";
        }
        
        switch (status) {
            case 0:
                return "上传中";
            case 1:
                return "待解析";
            case 2:
                return "解析中";
            case 3:
                return "解析完成";
            case 4:
                return "处理失败";
            case 5:
                return "已向量化";
            default:
                return "未知状态";
        }
    }
}
