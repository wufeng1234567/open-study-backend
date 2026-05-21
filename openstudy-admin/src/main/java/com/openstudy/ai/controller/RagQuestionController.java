package com.openstudy.ai.controller;

import com.openstudy.ai.model.RagAnswerResponse;
import com.openstudy.ai.service.rag.RagQuestionService;
import com.openstudy.common.core.controller.BaseController;
import com.openstudy.common.core.domain.AjaxResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * RAG 问答 Controller
 */
@Slf4j
@Tag(name = "RAG智能问答")
@RestController
@RequestMapping("/rag")
@RequiredArgsConstructor
public class RagQuestionController extends BaseController {

    private final RagQuestionService questionService;

    @Operation(summary = "RAG 问答（支持越界检测）")
    @PostMapping("/ask")
    public AjaxResult askQuestion(@RequestBody AskRequest request) {
        log.info("收到 RAG 问答请求: question={}, knowledgeBaseId={}", 
                request.getQuestion(), request.getKnowledgeBaseId());

        try {
            RagAnswerResponse response = questionService.askQuestion(
                    request.getQuestion(), 
                    request.getKnowledgeBaseId()
            );

            return success(response);
        } catch (Exception e) {
            log.error("RAG 问答失败", e);
            return error("问答失败: " + e.getMessage());
        }
    }

    @Operation(summary = "RAG 流式问答（JSON 格式）")
    @GetMapping(value = "/ask/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> askWithStream(@RequestParam String question,
                                       @RequestParam(required = false) Long knowledgeBaseId,
                                       @RequestParam(defaultValue = "zhipuai") String provider,
                                       @RequestParam(defaultValue = "0") Long userId) {
        log.info("收到 RAG 流式问答请求: question={}, provider={}, userId={}", question, provider, userId);

        try {
            return questionService.askWithStream(question, knowledgeBaseId, provider, userId);
        } catch (Exception e) {
            log.error("RAG 流式问答失败", e);
            return Flux.error(e);
        }
    }

    /**
     * 问答请求对象
     */
    public static class AskRequest {
        private String question;
        private Long knowledgeBaseId;

        public String getQuestion() {
            return question;
        }

        public void setQuestion(String question) {
            this.question = question;
        }

        public Long getKnowledgeBaseId() {
            return knowledgeBaseId;
        }

        public void setKnowledgeBaseId(Long knowledgeBaseId) {
            this.knowledgeBaseId = knowledgeBaseId;
        }
    }
}
