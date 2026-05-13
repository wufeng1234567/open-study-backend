package com.openstudy.ai.controller;

import com.openstudy.ai.domain.RagQaRecord;
import com.openstudy.ai.service.RagQaRecordService;
import com.openstudy.common.core.controller.BaseController;
import com.openstudy.common.core.domain.AjaxResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * RAG 问答记录 Controller
 */
@Slf4j
@Tag(name = "RAG问答记录")
@RestController
@RequestMapping("/rag/qa")
@RequiredArgsConstructor
public class RagQaRecordController extends BaseController {
    
    private final RagQaRecordService qaRecordService;
    
    @Operation(summary = "保存问答记录")
    @PostMapping("/save")
    public AjaxResult save(@RequestBody RagQaRecord record) {
        log.info("保存问答记录: knowledgeBaseId={}", record.getKnowledgeBaseId());
        
        try {
            RagQaRecord saved = qaRecordService.save(record);
            return success(saved);
        } catch (Exception e) {
            log.error("保存问答记录失败", e);
            return error("保存问答记录失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "查询知识库的问答历史")
    @GetMapping("/list/{knowledgeBaseId}")
    public AjaxResult list(@PathVariable Long knowledgeBaseId) {
        log.info("查询知识库问答历史: knowledgeBaseId={}", knowledgeBaseId);
        
        try {
            List<RagQaRecord> records = qaRecordService.listByKnowledgeBaseId(knowledgeBaseId);
            return success(records);
        } catch (Exception e) {
            log.error("查询问答历史失败", e);
            return error("查询问答历史失败: " + e.getMessage());
        }
    }
}
