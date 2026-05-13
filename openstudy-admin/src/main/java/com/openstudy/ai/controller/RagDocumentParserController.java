package com.openstudy.ai.controller;

import com.openstudy.ai.domain.RagDocument;
import com.openstudy.ai.mapper.RagDocumentMapper;
import com.openstudy.ai.service.rag.RagDocumentAsyncService;
import com.openstudy.ai.service.rag.RagDocumentChunkService;
import com.openstudy.ai.service.rag.RagDocumentParserService;
import com.openstudy.common.core.controller.BaseController;
import com.openstudy.common.core.domain.AjaxResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Tag(name = "文档解析管理")
@RestController
@RequestMapping("/rag/document")
@RequiredArgsConstructor
public class RagDocumentParserController extends BaseController {

    private final RagDocumentMapper ragDocumentMapper;
    private final RagDocumentParserService parserService;
    private final RagDocumentChunkService chunkService;
    private final RagDocumentAsyncService asyncService;

    @Operation(summary = "手动解析单个文档（异步）")
    @PostMapping("/parse/{documentId}")
    public AjaxResult parseDocument(@PathVariable Long documentId) {
        log.info("收到解析请求，documentId: {}", documentId);

        try {
            // 异步处理：解析 + 分块 + 向量化
            asyncService.asyncFullProcess(documentId);

            Map<String, Object> result = new HashMap<>();
            result.put("documentId", documentId);
            result.put("message", "已开始异步处理，请稍后查询状态");

            return success(result);
        } catch (Exception e) {
            log.error("启动解析失败", e);
            return error("启动解析失败: " + e.getMessage());
        }
    }

    @Operation(summary = "手动解析单个文档（同步-调试用）")
    @PostMapping("/parse/sync/{documentId}")
    public AjaxResult parseDocumentSync(@PathVariable Long documentId) {
        log.info("收到同步解析请求，documentId: {}", documentId);

        try {
            // 同步处理：用于调试
            asyncService.asyncFullProcess(documentId);

            // 等待一下让异步任务执行（仅用于测试）
            Thread.sleep(2000);

            Map<String, Object> result = new HashMap<>();
            result.put("documentId", documentId);
            result.put("message", "已启动处理，请轮询状态接口查看进度");

            return success(result);
        } catch (Exception e) {
            log.error("启动解析失败", e);
            return error("启动解析失败: " + e.getMessage());
        }
    }

    @Operation(summary = "批量解析所有待解析文档")
    @PostMapping("/parse/pending")
    public AjaxResult parsePendingDocuments() {
        log.info("批量解析待解析文档");

        // 查询 status=1（待解析）的文档
        List<RagDocument> pendingDocs = ragDocumentMapper.selectByStatus(1);

        if (pendingDocs == null || pendingDocs.isEmpty()) {
            return success("没有待解析的文档");
        }

        int successCount = 0;
        int failCount = 0;

        for (RagDocument doc : pendingDocs) {
            try {
                parserService.parseDocument(doc.getId());
                chunkService.chunkDocument(doc.getId());
                successCount++;
                log.info("文档解析成功: {}", doc.getFileName());
            } catch (Exception e) {
                failCount++;
                log.error("文档解析失败: {}", doc.getFileName(), e);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total", pendingDocs.size());
        result.put("successCount", successCount);
        result.put("failCount", failCount);

        return success(result);
    }
}
