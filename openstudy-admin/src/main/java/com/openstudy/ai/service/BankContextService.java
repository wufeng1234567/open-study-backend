package com.openstudy.ai.service;

import com.openstudy.ai.util.AiRedisUtil;
import com.openstudy.questionBank.domain.QuestionBank;
import com.openstudy.questionBank.mapper.QuestionBankMapper;
import com.openstudy.questionMain.mapper.QuestionMainMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class BankContextService {

    private final AiRedisUtil redisUtil;
    private final QuestionBankMapper bankMapper;
    private final QuestionMainMapper questionMainMapper;

    private static final String BANK_CONTEXT_KEY = "ai:bank:context:";

    /**
     * 获取题库上下文（优先从Redis，没有则从MySQL加载）
     */
    public Map<Object, Object> getBankContext(Long bankId) {
        String key = BANK_CONTEXT_KEY + bankId;
        Map<Object, Object> cached = redisUtil.hashGetAll(key);

        if (cached == null || cached.isEmpty()) {
            cached = loadBankContext(bankId);
            if (cached != null && !cached.isEmpty()) {
                Map<String, Object> stringMap = new HashMap<>();
                cached.forEach((k, v) -> stringMap.put(String.valueOf(k), v));
                redisUtil.hashPutAll(key, stringMap);
                redisUtil.expire(key, 10, TimeUnit.MINUTES);
            }
        }
        return cached != null ? cached : new HashMap<>();
    }

    /**
     * 从MySQL加载题库上下文
     */
    private Map<Object, Object> loadBankContext(Long bankId) {
        QuestionBank bank = bankMapper.selectQuestionBankById(bankId);
        if (bank == null) return new HashMap<>();

        // 获取已有题目摘要
        List<String> existingQuestions = questionMainMapper.selectQuestionTextsByBankId(bankId, 10);

        Map<Object, Object> context = new HashMap<>();
        context.put("bankId", bankId);
        context.put("bankName", bank.getBankName());
        context.put("subject", bank.getSubject());
        context.put("totalQuestions", bank.getTotalQuestions());
        context.put("existingQuestions", existingQuestions != null ? String.join("；", existingQuestions) : "");

        log.debug("加载题库 {} 上下文，已有题目 {} 道", bankId, existingQuestions != null ? existingQuestions.size() : 0);
        return context;
    }

    /**
     * 构建出题用的上下文提示词
     */
    public String buildContextPrompt(Long bankId) {
        if (bankId == null) return "";

        Map<Object, Object> context = getBankContext(bankId);
        if (context.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        sb.append("\n【题库上下文 - 请参考以下信息出题】\n");
        sb.append("- 题库名称：").append(context.get("bankName")).append("\n");
        sb.append("- 科目：").append(context.get("subject")).append("\n");

        Object total = context.get("totalQuestions");
        sb.append("- 已有题目数量：").append(total != null ? total : "0").append("\n");

        Object existing = context.get("existingQuestions");
        if (existing != null && !existing.toString().isEmpty()) {
            sb.append("- 已有题目示例：").append(existing).append("\n");
            sb.append("- 请避免生成与以上题目重复或高度相似的题目\n");
        }

        return sb.toString();
    }

    /**
     * 题库变动时清除缓存
     */
    public void clearCache(Long bankId) {
        redisUtil.delete(BANK_CONTEXT_KEY + bankId);
    }
}