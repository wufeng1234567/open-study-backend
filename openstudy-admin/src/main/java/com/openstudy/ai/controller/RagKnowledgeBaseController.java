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
 * RAG 知识库管理 Controller
 */
@Slf4j
@Tag(name = "RAG知识库管理")
@RestController
@RequestMapping("/rag/knowledgeBase")
@RequiredArgsConstructor
public class RagKnowledgeBaseController extends BaseController {

    private final RagKnowledgeBaseService knowledgeBaseService;

    @Operation(summary = "查询用户的知识库列表")
    @GetMapping("/list")
    public AjaxResult list(@RequestParam(defaultValue = "1") Long userId) {
        log.info("查询知识库列表: userId={}", userId);

        try {
            List<RagKnowledgeBase> list = knowledgeBaseService.listByUserId(userId);
            return success(list);
        } catch (Exception e) {
            log.error("查询知识库列表失败", e);
            return error("查询知识库列表失败: " + e.getMessage());
        }
    }

    @Operation(summary = "查询知识库详情")
    @GetMapping("/{id}")
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

    @Operation(summary = "更新知识库")
    @PutMapping("/{id}")
    public AjaxResult update(@PathVariable Long id,
            @RequestBody RagKnowledgeBase kb) {
        log.info("更新知识库: id={}, name={}", id, kb.getName());

        try {
            kb.setId(id);
            int result = knowledgeBaseService.update(kb);
            if (result > 0) {
                return success("知识库更新成功");
            } else {
                return error("知识库更新失败");
            }
        } catch (Exception e) {
            log.error("更新知识库失败", e);
            return error("更新知识库失败: " + e.getMessage());
        }
    }

    @Operation(summary = "删除知识库")
    @DeleteMapping("/{id}")
    public AjaxResult delete(@PathVariable Long id) {
        log.info("删除知识库: id={}", id);

        try {
            int result = knowledgeBaseService.deleteById(id);
            if (result > 0) {
                return success("知识库删除成功");
            } else {
                return error("知识库不存在或删除失败");
            }
        } catch (Exception e) {
            log.error("删除知识库失败", e);
            return error("删除知识库失败: " + e.getMessage());
        }
    }
}
