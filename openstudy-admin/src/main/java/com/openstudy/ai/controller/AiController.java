package com.openstudy.ai.controller;

import com.openstudy.ai.domain.RagKnowledgeBase;
import com.openstudy.ai.model.GenerateRecord;
import com.openstudy.ai.service.*;
import com.openstudy.ai.service.rag.RagChatService;
import com.openstudy.ai.service.rag.RagDocumentService;
import com.openstudy.ai.template.PromptTemplate;
import com.openstudy.common.annotation.Log;
import com.openstudy.common.core.controller.BaseController;
import com.openstudy.common.core.domain.AjaxResult;
import com.openstudy.common.enums.BusinessType;
import com.openstudy.framework.ai.ZhipuAiClient;
import com.openstudy.sensitiveWord.service.ISysSensitiveWordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import jakarta.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.zhipuai.ZhiPuAiChatModel;
import com.openstudy.ai.model.ConversationMessage;

@Slf4j
@Tag(name = "AI智能助手")
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController extends BaseController {

    private final ZhipuAiClient zhipuAiClient;
    private final BankContextService bankContextService;
    private final UserPreferenceService userPreferenceService;
    private final AiService aiService;
    private final AiQuestionService aiQuestionService;
    private final ISysSensitiveWordService sysSensitiveWordService;
    private final GenerateHistoryService generateHistoryService;
    // 在类顶部添加注入
    private final ConversationHistoryService historyService;

    // ========== 新增 RAG 服务 ==========
    private final RagChatService ragChatService;
    private final RagDocumentService ragDocumentService;
    private final RagKnowledgeBaseService knowledgeBaseService;
    private final com.openstudy.ai.service.rag.RagVectorService ragVectorService;

    // Spring AI 客户端（测试用，不影响原有代码）
    @Autowired(required = false)
    private ChatClient springAiChatClient;

    @Autowired(required = false)
    private ZhiPuAiChatModel zhiPuAiChatModel;

    @Operation(summary = "测试接口")
    @GetMapping("/test")
    public AjaxResult test() {
        return success("AI服务正常");
    }

    @Operation(summary = "Spring AI 测试（智谱）")
    @GetMapping("/test/spring-ai")
    public AjaxResult testSpringAi(@RequestParam(defaultValue = "你好，请介绍一下自己") String message) {
        try {
            log.info("测试 Spring AI，消息: {}", message);

            if (springAiChatClient != null) {
                String response = springAiChatClient.prompt()
                        .user(message)
                        .call()
                        .content();
                log.info("ChatClient 响应: {}", response);
                return success(response);
            }

            if (zhiPuAiChatModel != null) {
                org.springframework.ai.chat.prompt.Prompt prompt = new org.springframework.ai.chat.prompt.Prompt(
                        message);
                String response = zhiPuAiChatModel.call(prompt)
                        .getResult()
                        .getOutput()
                        .getContent();
                log.info("ZhiPuAiChatModel 响应: {}", response);
                return success(response);
            }

            return error("Spring AI 客户端未初始化，请检查配置");

        } catch (Exception e) {
            log.error("Spring AI 测试失败", e);
            return error("Spring AI 调用失败: " + e.getMessage());
        }
    }

    @Operation(summary = "简单聊天")
    @PostMapping("/chat")
    public AjaxResult chat(@RequestBody ChatRequest request) {
        // 获取用户ID，未登录时使用默认值0
        Long userId;
        try {
            userId = getUserId();
        } catch (Exception e) {
            userId = 0L; // 未登录用户默认ID
        }

        // 1. 保存用户消息
        ConversationMessage userMsg = ConversationMessage.builder()
                .role("user")
                .content(request.getMessage())
                .build();
        historyService.saveMessage(userId, request.getSessionId(), userMsg);

        // 2. 获取历史上下文
        String context = historyService.buildContextPrompt(userId, request.getSessionId());
        String fullMessage = context.isEmpty() ? request.getMessage() : context + "\n【当前问题】\n" + request.getMessage();

        // 3. 调用 AI
        String response = aiService.chat(fullMessage, request.getProvider());

        // 4. 保存 AI 回复
        ConversationMessage aiMsg = ConversationMessage.builder()
                .role("assistant")
                .content(response)
                .build();
        historyService.saveMessage(userId, request.getSessionId(), aiMsg);

        return success(response);
    }

    @Operation(summary = "带系统提示词的聊天")
    @PostMapping("/chat/system")
    public AjaxResult chatWithSystem(@RequestBody SystemChatRequest request) {
        String response = aiService.chatWithSystem(
                request.getSystemPrompt(),
                request.getMessage(),
                request.getProvider());
        return success(response);
    }

    @Operation(summary = "生成普通题目")
    @PostMapping("/generate/questions")
    public AjaxResult generateQuestions(@RequestBody GenerateQuestionRequest request) {
        Long userId = getUserId();
        String result = aiQuestionService.generateQuestions(
                request.getKnowledgePoint(),
                request.getQuestionType(),
                request.getCount(),
                request.getProvider(),
                userId);
        return success(result);
    }

    @Operation(summary = "解析答案")
    @PostMapping("/analyze")
    public AjaxResult analyzeAnswer(@RequestBody AnalyzeRequest request) {
        String result = aiQuestionService.analyzeAnswer(
                request.getQuestion(),
                request.getUserAnswer(),
                request.getCorrectAnswer(),
                request.getProvider());
        return success(result);
    }

    @Operation(summary = "生成试卷")
    @PostMapping("/generate/exam")
    public AjaxResult generateExamPaper(@RequestBody GenerateExamRequest request) {
        String result = aiQuestionService.generateExamPaper(
                request.getSubject(),
                request.getDifficulty(),
                request.getTotalScore(),
                request.getProvider());
        return success(result);
    }

    @Operation(summary = "获取可用模型列表")
    @GetMapping("/models")
    public AjaxResult getModels() {
        Map<String, Object> result = new HashMap<>();
        result.put("currentModel", aiService.getCurrentModel());
        result.put("availableModels", aiService.getAvailableModels());
        return success(result);
    }

    @Operation(summary = "切换模型")
    @GetMapping("/switch-model")
    public AjaxResult switchModel(@RequestParam(required = false) String provider) {
        log.info("收到切换模型请求，provider: {}", provider);

        if (provider == null || provider.trim().isEmpty()) {
            return error("模型名称不能为空");
        }

        boolean success = aiService.switchModel(provider);
        if (success) {
            return success("模型已切换为: " + provider);
        } else {
            return error("模型不存在: " + provider);
        }
    }

    // ==================== RAG 知识库接口（新增） ====================

    @Operation(summary = "RAG问答（基于知识库）")
    @PostMapping("/rag/chat")
    public AjaxResult ragChat(@RequestBody RagChatRequest request) {
        String answer = ragChatService.chatWithKnowledge(
                request.getQuestion(),
                request.getProvider());
        return success(answer);
    }

    @Operation(summary = "添加文档到知识库")
    @PostMapping("/rag/document")
    public AjaxResult addDocument(@RequestBody AddDocumentRequest request) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("source", request.getSource());
        metadata.put("category", request.getCategory());
        metadata.put("timestamp", System.currentTimeMillis());

        ragDocumentService.addDocument(request.getContent(), metadata);
        return success("文档添加成功");
    }

    // ==================== 流式接口 ====================

    @Operation(summary = "流式生成组合题（SSE）")
    @GetMapping(value = "/generate/composite/stream")
    public ResponseEntity<Flux<String>> generateCompositeStream(
            @RequestParam String requirement,
            @RequestParam Integer questionCount,
            @RequestParam(defaultValue = "zhipuai") String provider) {
        log.info("SSE流式生成组合题，requirement: {}, questionCount: {}", requirement, questionCount);

        Flux<String> flux = aiQuestionService.generateReadingComprehensionStream(requirement, questionCount, provider)
                .concatWith(Flux.just("[DONE]"))
                .doOnSubscribe(subscription -> log.info("SSE 订阅成功"))
                .doOnComplete(() -> log.info("SSE 流式完成"))
                .doOnError(error -> log.error("SSE 流式出错", error));

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(flux);
    }

    @Operation(summary = "流式生成普通题目（SSE）")
    @GetMapping(value = "/generate/questions/stream")
    public ResponseEntity<Flux<String>> generateQuestionsStream(
            @RequestParam String knowledgePoint,
            @RequestParam String questionType,
            @RequestParam Integer count,
            @RequestParam(defaultValue = "zhipuai") String provider) {
        log.info("SSE流式生成普通题目，知识点: {}, 题型: {}, 数量: {}", knowledgePoint, questionType, count);

        String systemPrompt = aiQuestionService.getSystemPrompt();
        String userPrompt = aiQuestionService.buildUserPrompt(knowledgePoint, questionType, count);

        Flux<String> flux = aiService.chatStreamWithSystem(systemPrompt, userPrompt, provider)
                .concatWith(Flux.just("[DONE]"))
                .doOnSubscribe(subscription -> log.info("SSE 订阅成功"))
                .doOnComplete(() -> log.info("SSE 流式完成"))
                .doOnError(error -> log.error("SSE 流式出错", error));

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(flux);
    }

    @Operation(summary = "生成普通题目（非流式）")
    @PostMapping("/generate/questions/sync")
    public AjaxResult generateQuestionsSync(@RequestBody GenerateQuestionRequest request) {
        // 优先使用前端传来的 userId，其次从 JWT 获取
        Long userId = request.getUserId();
        if (userId == null || userId == 0) {
            try {
                userId = getUserId();
            } catch (Exception e) {
                userId = 0L;
            }
        }

        // 1. 构建题库上下文
        String bankContext = "";
        if (request.getBankId() != null) {
            bankContext = bankContextService.buildContextPrompt(request.getBankId());
        }

        // 2. 构建历史对话上下文
        String historyContext = "";
        if (request.getBankId() != null) {
            historyContext = historyService.buildQuestionContext(userId, request.getBankId());
        }

        // 3. 知识库上下文
        if (request.getKnowledgeBaseId() != null && request.getKnowledgeBaseId() > 0) {
            try {
                RagKnowledgeBase kb = knowledgeBaseService.getById(request.getKnowledgeBaseId());
                if (kb != null) {
                    // 检索知识库文档（用知识点作为查询词）
                    String searchQuery = request.getKnowledgePoint();
                    if (searchQuery == null || searchQuery.trim().isEmpty()) {
                        searchQuery = kb.getName(); // 如果知识点为空，用知识库名称检索
                    }
                    java.util.List<String> kbContexts = ragVectorService.searchSimilar(
                            searchQuery,
                            5,
                            request.getKnowledgeBaseId());
                    if (kbContexts != null && !kbContexts.isEmpty()) {
                        String kbContext = String.join("\n\n---\n\n", kbContexts);
                        bankContext = "【知识库「" + kb.getName() + "」参考资料】\n" + kbContext + "\n\n" + bankContext;
                    }
                }
            } catch (Exception e) {
                log.warn("检索知识库文档失败，降级为通用出题: {}", e.getMessage());
                // 检索失败不阻塞出题流程
            }
        }

        // 4. 拼接完整上下文
        String enhancedKnowledge = bankContext + historyContext + request.getKnowledgePoint();

        String result = aiQuestionService.generateQuestions(
                enhancedKnowledge,
                request.getQuestionType(),
                request.getCount(),
                request.getProvider(),
                userId);

        // 5. 保存本次出题到历史
        if (request.getBankId() != null) {
            saveToHistory(userId, request);
        }

        return success(result);
    }

    // 新增辅助方法
    private void saveToHistory(Long userId, GenerateQuestionRequest request) {
        String sessionId = "bank_" + request.getBankId();
        ConversationMessage userMsg = ConversationMessage.builder()
                .role("user")
                .content("请生成" + request.getCount() + "道" + request.getQuestionType() + "，知识点："
                        + request.getKnowledgePoint())
                .build();
        historyService.saveMessage(userId, sessionId, userMsg);
    }

    @Operation(summary = "清空题库对话上下文")
    @DeleteMapping("/conversation/bank/{bankId}")
    public AjaxResult clearBankConversation(@PathVariable Long bankId) {
        Long userId;
        try {
            userId = getUserId();
        } catch (Exception e) {
            userId = 0L;
        }
        String sessionId = "bank_" + bankId;
        historyService.clearHistory(userId, sessionId);
        return success("上下文已清空");
    }

    @Operation(summary = "获取题库AI上下文信息")
    @GetMapping("/bank/context/{bankId}")
    public AjaxResult getBankContextInfo(@PathVariable Long bankId) {
        Map<String, Object> result = new HashMap<>();

        // 题库基本信息
        Map<Object, Object> bankContext = bankContextService.getBankContext(bankId);
        result.put("bankName", bankContext.getOrDefault("bankName", "未知题库"));
        result.put("totalQuestions", bankContext.getOrDefault("totalQuestions", 0));

        // 历史消息数量
        Long userId;
        try {
            userId = getUserId();
        } catch (Exception e) {
            userId = 0L;
        }
        String sessionId = "bank_" + bankId;
        int historyCount = historyService.getRecentMessages(userId, sessionId, 100).size();
        result.put("historyCount", historyCount);

        return success(result);
    }

    @Operation(summary = "生成组合题（非流式）")
    @PostMapping("/generate/composite/sync")
    public AjaxResult generateCompositeSync(@RequestBody GenerateCompositeRequest request) {
        // 优先使用前端传来的 userId，其次从 JWT 获取
        Long userId = request.getUserId();
        if (userId == null || userId == 0) {
            try {
                userId = getUserId();
            } catch (Exception e) {
                userId = 0L;
            }
        }
        String result = aiQuestionService.generateCompositeQuestion(
                request.getRequirement(),
                request.getQuestionCount(),
                request.getWordCount(),
                request.getProvider(),
                userId);
        return success(result);
    }

    @PreAuthorize("@ss.hasPermi('sensitiveWord:sensitiveWord:add')")
    @Log(title = "AI生成敏感词", businessType = BusinessType.INSERT)
    @PostMapping("/aiGenerate")
    public AjaxResult aiGenerate(@RequestBody AiGenerateRequest request) {
        if (request.getTopic() == null || request.getTopic().trim().isEmpty()) {
            return error("主题不能为空");
        }
        if (request.getCategory() == null || request.getCategory().trim().isEmpty()) {
            return error("分类不能为空");
        }

        int count = request.getCount() != null ? request.getCount() : 20;
        if (count < 5)
            count = 20;
        if (count > 50)
            count = 50;

        int addedCount = sysSensitiveWordService.aiGenerateWords(
                request.getTopic().trim(),
                request.getCategory().trim(),
                count);

        return success("成功生成 " + addedCount + " 个敏感词");
    }

    @GetMapping(value = "/assistant/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> assistantStream(
            @RequestParam String message,
            @RequestParam(defaultValue = "zhipuai") String provider,
            @RequestParam(defaultValue = "assistant") String sessionId) {

        Long userId;
        try {
            userId = getUserId();
        } catch (Exception e) {
            userId = 0L;
        }

        // ✅ 复制为 final 变量供 lambda 使用
        final Long finalUserId = userId;

        // 保存用户消息
        ConversationMessage userMsg = ConversationMessage.builder()
                .role("user")
                .content(message)
                .build();
        historyService.saveMessage(finalUserId, sessionId, userMsg);

        // 获取历史上下文
        String context = historyService.buildContextPrompt(finalUserId, sessionId);
        String fullMessage = context.isEmpty() ? message : context + "\n【当前问题】\n" + message;

        // 用于累积完整回复
        StringBuilder fullResponse = new StringBuilder();

        return zhipuAiClient.chatStream(PromptTemplate.ASSISTANT_SYSTEM_PROMPT, fullMessage, "glm-4-plus")
                .doOnNext(chunk -> {
                    fullResponse.append(chunk);
                })
                .doOnComplete(() -> {
                    // 流结束后保存 AI 完整回复
                    ConversationMessage aiMsg = ConversationMessage.builder()
                            .role("assistant")
                            .content(fullResponse.toString())
                            .build();
                    historyService.saveMessage(finalUserId, sessionId, aiMsg); // ✅ 使用 finalUserId
                    log.info("AI 回复已保存，长度: {}", fullResponse.length());
                })
                .doOnError(error -> {
                    log.error("流式调用失败", error);
                });
    }

    // AiController.java
    @GetMapping("/conversation/{sessionId}")
    public AjaxResult getConversation(@PathVariable String sessionId) {
        // 获取用户ID，未登录时使用默认值0
        Long userId;
        try {
            userId = getUserId();
        } catch (Exception e) {
            userId = 0L; // 未登录用户默认ID
        }

        List<ConversationMessage> messages = historyService.getRecentMessages(userId, sessionId, 50);
        return success(messages);
    }

    @DeleteMapping("/conversation/{sessionId}")
    public AjaxResult clearConversation(@PathVariable String sessionId) {
        // 获取用户ID，未登录时使用默认值0
        Long userId;
        try {
            userId = getUserId();
        } catch (Exception e) {
            userId = 0L; // 未登录用户默认ID
        }

        historyService.clearHistory(userId, sessionId);
        return success();
    }

    @Operation(summary = "题目智能解析（流式）")
    @GetMapping(value = "/analyze/question/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> analyzeQuestionStream(
            @RequestParam String question,
            @RequestParam(required = false) String questionType,
            @RequestParam(required = false) String options,
            @RequestParam(required = false) String correctAnswer,
            @RequestParam(defaultValue = "zhipuai") String provider) {

        log.info("题目解析请求，题型: {}, 题目: {}", questionType, question.substring(0, Math.min(50, question.length())));

        // 构建用户消息
        StringBuilder userMessage = new StringBuilder();
        userMessage.append("请解析以下题目：\n\n");
        userMessage.append("【题目】").append(question).append("\n");

        if (options != null && !options.isEmpty()) {
            userMessage.append("【选项】").append(options).append("\n");
        }
        if (correctAnswer != null && !correctAnswer.isEmpty()) {
            userMessage.append("【正确答案】").append(correctAnswer).append("\n");
        }
        if (questionType != null && !questionType.isEmpty()) {
            userMessage.append("【题型】").append(questionType).append("\n");
        }

        return zhipuAiClient.chatStream(
                PromptTemplate.QUESTION_ANALYSIS_SYSTEM_PROMPT,
                userMessage.toString(),
                "glm-4-plus");
    }

    // ==================== 请求内部类 ====================

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ChatRequest {
        @NotBlank(message = "消息内容不能为空")
        private String message;
        private String provider = "zhipuai";
        private String sessionId = "default";
    }

    @lombok.Data
    public static class SystemChatRequest {
        @NotBlank(message = "系统提示词不能为空")
        private String systemPrompt;
        @NotBlank(message = "消息内容不能为空")
        private String message;
        private String provider = "zhipuai";
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class GenerateQuestionRequest {
        @NotBlank(message = "知识点不能为空")
        private String knowledgePoint;
        private String questionType = "single";
        private Integer count = 5;
        private String provider = "zhipuai";
        private Long bankId;
        private Long knowledgeBaseId; // 可选，知识库ID，用于检索文档辅助出题
        private Long userId; // 前端传来的用户ID，用于获取用户个人AI配置
    }

    @lombok.Data
    public static class AnalyzeRequest {
        @NotBlank(message = "题目不能为空")
        private String question;
        private String userAnswer;
        private String correctAnswer;
        private String provider = "zhipuai";
    }

    @lombok.Data
    public static class GenerateExamRequest {
        private String subject = "综合";
        private String difficulty = "中等";
        private Integer totalScore = 100;
        private String provider = "zhipuai";
    }

    @lombok.Data
    public static class GenerateCompositeRequest {
        @NotBlank(message = "出题要求不能为空")
        private String requirement;
        private Integer questionCount = 5;
        private Integer wordCount = 300;
        private String provider = "zhipuai";
        private Long userId; // 前端传来的用户ID，用于获取用户个人AI配置
    }

    @lombok.Data
    public static class AiGenerateRequest {
        @jakarta.validation.constraints.NotBlank(message = "主题不能为空")
        private String topic;
        @jakarta.validation.constraints.NotBlank(message = "分类不能为空")
        private String category;
        private Integer count = 20;
    }

    // ==================== RAG 请求内部类（新增） ====================

    @lombok.Data
    public static class RagChatRequest {
        @NotBlank(message = "问题不能为空")
        private String question;
        private String provider = "zhipuai";
    }

    @lombok.Data
    public static class AddDocumentRequest {
        @NotBlank(message = "文档内容不能为空")
        private String content;
        private String source;
        private String category;
    }
}