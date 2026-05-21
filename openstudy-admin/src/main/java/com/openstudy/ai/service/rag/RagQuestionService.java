package com.openstudy.ai.service.rag;

import com.openstudy.ai.domain.RagKnowledgeBase;
import com.openstudy.ai.model.RagAnswerResponse;
import com.openstudy.ai.service.AiConfigService;
import com.openstudy.ai.service.RagKnowledgeBaseService;
import com.openstudy.ai.service.infra.AiClient;
import com.openstudy.ai.service.infra.AiClientManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.stream.Collectors;

/**
 * RAG 问答服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagQuestionService {

    private final RagVectorService vectorService;
    private final AiClientManager aiClientManager;
    private final RagKnowledgeBaseService knowledgeBaseService;
    private final AiConfigService aiConfigService;

    @Value("${ai.rag.similarity-threshold:0.65}")
    private Double similarityThreshold;

    @Value("${ai.rag.top-k:3}")
    private Integer topK;

    /**
     * 通用问候语列表（不触发越界检测）
     */
    private static final List<String> GREETING_PHRASES = Arrays.asList(
            // 问候语
            "你好", "您好", "hello", "hi", "嗨",
            // 在吗类
            "在吗", "在不在", "有人吗",
            // 感谢类
            "谢谢", "感谢", "thanks", "thank you",
            // 告别类
            "再见", "拜拜", "bye", "goodbye");

    /**
     * 通用问题关键词列表（直接返回友好回答，不走 RAG）
     * 注意：仅保留真正无关知识库内容的身份类问题
     * "你会什么"类的能力询问需要走RAG，让AI基于知识库文档回答
     */
    private static final List<String> GENERAL_QUESTION_KEYWORDS = Arrays.asList(
            "你是谁", "介绍你自己", "介绍下自己", "说说你自己",
            "帮我出题", "生成题目", "创建题目");

    /**
     * RAG 问答（支持越界检测）
     *
     * @param question        用户问题
     * @param knowledgeBaseId 知识库ID
     * @return 包含来源、置信度等信息的响应对象
     */
    public RagAnswerResponse askQuestion(String question, Long knowledgeBaseId) {
        log.info("RAG 问答，question: {}, knowledgeBaseId: {}", question, knowledgeBaseId);

        // 提前获取知识库信息（后续多个分支需要）
        RagKnowledgeBase currentKB = knowledgeBaseService.getById(knowledgeBaseId);
        String currentKBName = currentKB != null ? currentKB.getName() : "未知知识库";
        log.info("当前知识库: {} (id={})", currentKBName, knowledgeBaseId);

        // 0. 检查是否为通用问候语
        if (isGreeting(question)) {
            log.info("✅ 检测到通用问候语，使用带知识库信息的 AI 回答");

            AiClient aiClient = aiClientManager.getClient("zhipuai");
            String answer = aiClient.chatWithSystem(buildSimpleSystemPrompt(currentKBName), question);

            return RagAnswerResponse.builder()
                    .answer(answer)
                    .question(question)
                    .source("ai_general")
                    .confidence(1.0)
                    .isInScope(true) // 问候语视为在范围内
                    .hasOtherKB(false)
                    .otherKBs(Collections.emptyList())
                    .tip(null) // 不添加提示语
                    .build();
        }

        // 0.5. 检查是否为通用问题（白名单）
        if (isGeneralQuestion(question)) {
            log.info("✅ 检测到通用问题，使用带知识库上下文的 AI 回答");

            AiClient aiClient = aiClientManager.getClient("zhipuai");
            String answer = aiClient.chatWithSystem(buildSimpleSystemPrompt(currentKBName), question);

            return RagAnswerResponse.builder()
                    .answer(answer)
                    .question(question)
                    .source("ai_general")
                    .confidence(1.0)
                    .isInScope(true) // 通用问题视为在范围内，不触发越界提示
                    .hasOtherKB(false)
                    .otherKBs(Collections.emptyList())
                    .tip(null) // 不添加提示语
                    .build();
        }

        // 1. 检索相关文档（带分数）
        long retrieveStart = System.currentTimeMillis();
        List<Document> documents = vectorService.searchSimilarWithScore(question, topK, knowledgeBaseId);
        log.info("向量检索耗时: {}ms, 检索到 {} 个文档", System.currentTimeMillis() - retrieveStart, documents.size());

        // 2. 计算最高相似度得分
        Double maxScore = 0.0;
        if (!documents.isEmpty()) {
            maxScore = documents.stream()
                    .map(this::extractScore)
                    .max(Double::compareTo)
                    .orElse(0.0);
        }

        log.info("最高相似度得分: {}, 阈值: {}", maxScore, similarityThreshold);

        // 3. 查询用户其他知识库
        long otherKbStart = System.currentTimeMillis();
        List<RagKnowledgeBase> otherKBs = knowledgeBaseService.listByUserId(currentKB.getUserId())
                .stream()
                .filter(kb -> !kb.getId().equals(knowledgeBaseId))
                .collect(Collectors.toList());
        log.info("查询其他知识库耗时: {}ms", System.currentTimeMillis() - otherKbStart);

        boolean hasOtherKB = !otherKBs.isEmpty();
        List<Map<String, Object>> otherKBList = otherKBs.stream()
                .map(kb -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", kb.getId());
                    map.put("name", kb.getName());
                    return map;
                })
                .collect(Collectors.toList());

        // 4. 判断是否越界
        boolean isInScope = !documents.isEmpty() && maxScore >= similarityThreshold;

        if (isInScope) {
            // ✅ 在知识库范围内，使用 RAG 流程
            log.info("✅ 问题在「{}」知识库范围内，使用 RAG 回答", currentKBName);

            List<String> contexts = documents.stream()
                    .map(Document::getContent)
                    .collect(Collectors.toList());

            String contextText = String.join("\n\n", contexts);
            String systemPrompt = buildSystemPrompt(contextText, currentKBName);

            AiClient aiClient = aiClientManager.getClient("zhipuai");
            String answer = aiClient.chatWithSystem(systemPrompt, question);

            // 构建提示语
            String tip = null;
            if (maxScore < 0.8) {
                tip = "⚠️ 检索到一些相关内容，但相关度较低，以上回答仅供参考。";
            }

            return RagAnswerResponse.builder()
                    .answer(answer)
                    .question(question)
                    .source("knowledge_base")
                    .confidence(maxScore)
                    .isInScope(true)
                    .hasOtherKB(hasOtherKB)
                    .otherKBs(otherKBList)
                    .tip(tip)
                    .build();
        } else {
            // ❌ 越界，使用通用 AI 回答（告知当前知识库上下文）
            log.info("❌ 问题不在「{}」知识库范围内，降级到通用 AI", currentKBName);

            AiClient aiClient = aiClientManager.getClient("zhipuai");
            String answer = aiClient.chatWithSystem(buildSimpleSystemPrompt(currentKBName), question);

            // 构建提示语
            String tip;
            if (hasOtherKB) {
                String firstOtherKBName = otherKBs.get(0).getName();
                tip = String.format(
                        "⚠️ 当前知识库是「%s」，这个问题不在范围内。你的「%s」可能包含相关内容，要切换过去吗？",
                        currentKBName, firstOtherKBName);
            } else {
                tip = String.format(
                        "⚠️ 当前知识库是「%s」，这个问题不在范围内。我是用通用知识回答的，仅供参考。",
                        currentKBName);
            }

            return RagAnswerResponse.builder()
                    .answer(answer)
                    .question(question)
                    .source("ai_general")
                    .confidence(maxScore)
                    .isInScope(false)
                    .hasOtherKB(hasOtherKB)
                    .otherKBs(otherKBList)
                    .tip(tip)
                    .build();
        }
    }

    /**
     * RAG 流式问答（支持越界检测）
     *
     * @param question        用户问题
     * @param knowledgeBaseId 知识库ID
     * @param provider        AI提供商
     * @param userId          用户ID
     * @return 流式回答（JSON 格式）
     */
    public Flux<String> askWithStream(String question, Long knowledgeBaseId, String provider, Long userId) {
        return Flux.defer(() -> {
            try {
                long totalStart = System.currentTimeMillis();
                log.info("RAG 流式问答开始，question: {}, knowledgeBaseId: {}", question, knowledgeBaseId);

                // 提前获取知识库信息（后续多个分支需要）
                long kbStart = System.currentTimeMillis();
                RagKnowledgeBase currentKB = knowledgeBaseService.getById(knowledgeBaseId);
                String currentKBName = currentKB != null ? currentKB.getName() : "未知知识库";
                log.info("流式问答 - 当前知识库: {} (id={}), 查询耗时: {}ms", currentKBName, knowledgeBaseId,
                        System.currentTimeMillis() - kbStart);

                // 0. 检查是否为通用问候语
                if (isGreeting(question)) {
                    log.info("✅ 流式问答：检测到通用问候语，使用带知识库信息的 AI 回答");

                    AiClient aiClient = aiConfigService.getClient(provider, userId);

                    // 告知 AI 当前知识库
                    Flux<String> contentFlux = aiClient.chatStream(buildSimpleSystemPrompt(currentKBName), question)
                            .map(chunk -> toJson("content", chunk, null));

                    String endSignal = buildEndSignal("ai_general", 1.0, true, false, Collections.emptyList(), null);
                    return contentFlux.concatWith(Flux.just(endSignal));
                }

                // 0.5. 检查是否为通用问题（白名单）
                if (isGeneralQuestion(question)) {
                    log.info("✅ 流式问答：检测到通用问题，直接返回友好回答");

                    String friendlyAnswer = "我能做很多事情！比如回答问题、写代码、帮你学习、创作内容、处理数据等。有什么我可以帮你的吗？";

                    Flux<String> contentFlux = Flux.just(toJson("content", friendlyAnswer, null));
                    String endSignal = buildEndSignal("ai_general", 1.0, true, false, Collections.emptyList(), null);
                    return contentFlux.concatWith(Flux.just(endSignal));
                }

                // 1. 检索相关文档（带分数）
                long retrieveStart = System.currentTimeMillis();
                List<Document> documents = vectorService.searchSimilarWithScore(question, topK, knowledgeBaseId);
                log.info("向量检索耗时: {}ms, 检索到 {} 个文档", System.currentTimeMillis() - retrieveStart, documents.size());

                // 2. 计算最高相似度得分
                Double maxScore = 0.0;
                if (!documents.isEmpty()) {
                    maxScore = documents.stream()
                            .map(this::extractScore)
                            .max(Double::compareTo)
                            .orElse(0.0);
                }

                log.info("最高相似度得分: {}, 阈值: {}", maxScore, similarityThreshold);

                // 3. 查询用户其他知识库
                long otherKbStart = System.currentTimeMillis();
                List<RagKnowledgeBase> otherKBs = knowledgeBaseService.listByUserId(currentKB.getUserId())
                        .stream()
                        .filter(kb -> !kb.getId().equals(knowledgeBaseId))
                        .collect(Collectors.toList());
                log.info("查询其他知识库耗时: {}ms", System.currentTimeMillis() - otherKbStart);

                boolean hasOtherKB = !otherKBs.isEmpty();
                List<Map<String, Object>> otherKBList = otherKBs.stream()
                        .map(kb -> {
                            Map<String, Object> map = new HashMap<>();
                            map.put("id", kb.getId());
                            map.put("name", kb.getName());
                            return map;
                        })
                        .collect(Collectors.toList());

                // 4. 判断是否越界
                boolean isInScope = !documents.isEmpty() && maxScore >= similarityThreshold;

                if (isInScope) {
                    // ✅ 在知识库范围内
                    log.info("✅ 流式问答：问题在「{}」知识库范围内", currentKBName);

                    List<String> contexts = documents.stream()
                            .map(Document::getContent)
                            .collect(Collectors.toList());

                    String contextText = String.join("\n\n", contexts);
                    String systemPrompt = buildSystemPrompt(contextText, currentKBName);

                    AiClient aiClient = aiConfigService.getClient(provider, userId);

                    Flux<String> contentFlux = aiClient.chatStream(systemPrompt, question)
                            .map(chunk -> toJson("content", chunk, null));

                    String endSignal = buildEndSignal("knowledge_base", maxScore, true, hasOtherKB, otherKBList, null);
                    return contentFlux.concatWith(Flux.just(endSignal));
                } else {
                    // ❌ 越界，使用通用 AI（告知知识库上下文）
                    log.info("❌ 流式问答：问题不在「{}」知识库范围内，降级到通用 AI", currentKBName);

                    String tip;
                    if (hasOtherKB) {
                        String firstOtherKBName = otherKBs.get(0).getName();
                        tip = String.format(
                                "⚠️ 当前知识库是「%s」，这个问题不在范围内。你的「%s」可能包含相关内容，要切换过去吗？",
                                currentKBName, firstOtherKBName);
                    } else {
                        tip = String.format(
                                "⚠️ 当前知识库是「%s」，这个问题不在范围内。我是用通用知识回答的，仅供参考。",
                                currentKBName);
                    }

                    AiClient aiClient = aiConfigService.getClient(provider, userId);

                    // 告知 AI 当前在哪个知识库
                    Flux<String> contentFlux = aiClient.chatStream(buildSimpleSystemPrompt(currentKBName), question)
                            .map(chunk -> toJson("content", chunk, null));

                    String endSignal = buildEndSignal("ai_general", maxScore, false, hasOtherKB, otherKBList, tip);
                    return contentFlux.concatWith(Flux.just(endSignal));
                }
            } catch (Exception e) {
                log.error("RAG 流式问答失败", e);
                return Flux.error(e);
            }
        });
    }

    /**
     * 从 Document 中提取相似度分数
     */
    private Double extractScore(Document document) {
        if (document == null || document.getMetadata() == null) {
            return 0.0;
        }

        Map<String, Object> metadata = document.getMetadata();

        // 尝试获取 "score" 字段（余弦相似度）
        if (metadata.containsKey("score")) {
            Object scoreObj = metadata.get("score");
            if (scoreObj instanceof Number) {
                return ((Number) scoreObj).doubleValue();
            }
        }

        // 尝试获取 "distance" 字段（欧氏距离，需要转换）
        if (metadata.containsKey("distance")) {
            Object distanceObj = metadata.get("distance");
            if (distanceObj instanceof Number) {
                double distance = ((Number) distanceObj).doubleValue();
                // 转换公式：similarity = 1 / (1 + distance)
                return 1.0 / (1.0 + distance);
            }
        }

        return 0.0;
    }

    /**
     * 将内容转换为 JSON 格式（用于流式输出）
     */
    private String toJson(String type, String text, String extra) {
        if (text == null)
            text = "";
        // 转义特殊字符
        String escapedText = text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");

        return String.format("data: {\"type\": \"%s\", \"text\": \"%s\"}", type, escapedText);
    }

    /**
     * 构建结束信号 JSON
     */
    private String buildEndSignal(String source, Double confidence, Boolean isInScope,
            Boolean hasOtherKB, List<Map<String, Object>> otherKBs, String tip) {
        StringBuilder json = new StringBuilder();
        json.append("data: {");
        json.append("\"type\": \"end\",");
        json.append("\"source\": \"").append(source).append("\",");
        json.append("\"confidence\": ").append(confidence).append(",");
        json.append("\"isInScope\": ").append(isInScope).append(",");
        json.append("\"hasOtherKB\": ").append(hasOtherKB).append(",");

        // otherKBs 数组
        json.append("\"otherKBs\": [");
        if (otherKBs != null && !otherKBs.isEmpty()) {
            for (int i = 0; i < otherKBs.size(); i++) {
                Map<String, Object> kb = otherKBs.get(i);
                if (i > 0)
                    json.append(",");
                json.append("{\"id\": ").append(kb.get("id"))
                        .append(", \"name\": \"").append(kb.get("name")).append("\"}");
            }
        }
        json.append("],");

        // tip
        if (tip != null) {
            String escapedTip = tip.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n");
            json.append("\"tip\": \"").append(escapedTip).append("\"");
        } else {
            json.append("\"tip\": null");
        }

        json.append("}");
        return json.toString();
    }

    /**
     * 构建系统提示词（带知识库名称）
     */
    private String buildSystemPrompt(String context, String kbName) {
        return """
                你是一个智能助手，当前正在「%s」知识库中回答用户问题。

                【背景知识】（来自知识库文档）
                %s

                【要求】
                1. 请基于上述背景知识回答问题，优先使用知识库中的内容
                2. 如果背景知识中没有相关信息，请明确说明「在当前知识库中未找到相关内容」
                3. 回答要准确、简洁、有条理
                4. 不要编造背景知识中不存在的信息
                5. 你正在回答的是关于「%s」的问题，请围绕这个主题
                """.formatted(kbName, context, kbName);
    }

    /**
     * 构建系统提示词（无背景知识时，告知当前知识库）
     */
    private String buildSimpleSystemPrompt(String kbName) {
        return """
                你是一个智能助手，当前正在「%s」知识库中回答用户问题。

                当前知识库中没有检索到相关文档内容，请根据你的通用知识回答，但要告知用户：
                - 你正在「%s」知识库中
                - 当前回答基于通用知识，非知识库文档内容
                """.formatted(kbName, kbName);
    }

    /**
     * 检查是否为通用问候语
     *
     * @param question 用户问题
     * @return true 如果是问候语，false 否则
     */
    private boolean isGreeting(String question) {
        if (question == null || question.trim().isEmpty()) {
            return false;
        }

        String trimmedQuestion = question.trim().toLowerCase();

        // 精确匹配
        for (String greeting : GREETING_PHRASES) {
            if (trimmedQuestion.equals(greeting.toLowerCase())) {
                return true;
            }
        }

        // 包含匹配（例如：“你好啊”、“您好！”）
        for (String greeting : GREETING_PHRASES) {
            if (trimmedQuestion.contains(greeting.toLowerCase())) {
                // 确保不是长句子中包含问候语（长度不超过问候语的2倍）
                if (trimmedQuestion.length() <= greeting.length() * 2) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 检查是否为通用问题（白名单）
     * 通用问题直接返回友好回答，不走 RAG 流程，不触发越界提示
     *
     * @param question 用户问题
     * @return true 如果是通用问题，false 否则
     */
    private boolean isGeneralQuestion(String question) {
        if (question == null || question.trim().isEmpty()) {
            return false;
        }

        String trimmedQuestion = question.trim().toLowerCase();

        // 精确匹配通用问题关键词（避免 "你的知识库里有什么" 误匹配 "你有什么功能"）
        for (String keyword : GENERAL_QUESTION_KEYWORDS) {
            if (trimmedQuestion.equals(keyword.toLowerCase())) {
                log.debug("匹配到通用问题关键词: {}", keyword);
                return true;
            }
        }

        return false;
    }
}
