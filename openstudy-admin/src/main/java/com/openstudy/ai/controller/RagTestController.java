package com.openstudy.ai.controller;

import com.openstudy.ai.domain.RagKnowledgeBase;
import com.openstudy.ai.service.RagKnowledgeBaseService;
import com.openstudy.common.core.controller.BaseController;
import com.openstudy.common.core.domain.AjaxResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * RAG 知识库测试 Controller
 */
@Slf4j
@Tag(name = "RAG知识库测试")
@RestController
@RequestMapping("/test/rag")
@RequiredArgsConstructor
public class RagTestController extends BaseController {
    
    private final RagKnowledgeBaseService knowledgeBaseService;
    
    @Operation(summary = "健康检查")
    @GetMapping("/ping")
    public AjaxResult ping() {
        log.info("RAG 服务健康检查");
        return success("rag service ok");
    }
    
    @Operation(summary = "创建知识库")
    @PostMapping("/create")
    public AjaxResult create(@RequestParam String name, 
                             @RequestParam(required = false) String description,
                             @RequestParam(defaultValue = "1") Long userId) {
        log.info("创建知识库: name={}, description={}, userId={}", name, description, userId);
        
        try {
            RagKnowledgeBase kb = new RagKnowledgeBase();
            kb.setUserId(userId);
            kb.setName(name);
            kb.setDescription(description);
            kb.setIcon("");
            kb.setIsPublic(0);
            
            RagKnowledgeBase result = knowledgeBaseService.create(kb);
            log.info("知识库创建成功，ID: {}", result.getId());
            
            return success(result);
        } catch (Exception e) {
            log.error("创建知识库失败", e);
            return error("创建知识库失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "查询知识库列表")
    @GetMapping("/list")
    public AjaxResult list(@RequestParam(defaultValue = "1") Long userId) {
        log.info("查询知识库列表: userId={}", userId);
        
        try {
            List<RagKnowledgeBase> list = knowledgeBaseService.listByUserId(userId);
            log.info("查询到 {} 个知识库", list.size());
            return success(list);
        } catch (Exception e) {
            log.error("查询知识库列表失败", e);
            return error("查询知识库列表失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "查询知识库详情")
    @GetMapping("/get/{id}")
    public AjaxResult getById(@PathVariable Long id) {
        log.info("查询知识库详情: id={}", id);
        
        try {
            RagKnowledgeBase kb = knowledgeBaseService.getById(id);
            if (kb == null) {
                return error("知识库不存在");
            }
            return success(kb);
        } catch (Exception e) {
            log.error("查询知识库详情失败", e);
            return error("查询知识库详情失败: " + e.getMessage());
        }
    }
}
