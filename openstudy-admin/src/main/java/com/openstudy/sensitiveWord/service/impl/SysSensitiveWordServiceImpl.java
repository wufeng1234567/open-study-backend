package com.openstudy.sensitiveWord.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.openstudy.common.exception.BusinessException;
import com.openstudy.common.utils.DateUtils;
import com.openstudy.ai.service.AiService;
import com.openstudy.ai.template.PromptTemplate;
import com.openstudy.sensitiveWord.filter.SensitiveWordFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.openstudy.sensitiveWord.mapper.SysSensitiveWordMapper;
import com.openstudy.sensitiveWord.domain.SysSensitiveWord;
import com.openstudy.sensitiveWord.service.ISysSensitiveWordService;

/**
 * 敏感词管理Service业务层处理
 *
 * @author liu
 * @date 2026-04-17
 */
@Slf4j
@Service
public class SysSensitiveWordServiceImpl implements ISysSensitiveWordService
{
    @Autowired
    private SysSensitiveWordMapper sysSensitiveWordMapper;

    @Autowired
    private AiService aiService;

    @Autowired
    private PromptTemplate promptTemplate;

    @Autowired(required = false)
    private SensitiveWordFilter sensitiveWordFilter;

    /**
     * 查询敏感词管理
     *
     * @param id 敏感词管理主键
     * @return 敏感词管理
     */
    @Override
    public SysSensitiveWord selectSysSensitiveWordById(Long id)
    {
        return sysSensitiveWordMapper.selectSysSensitiveWordById(id);
    }

    /**
     * 查询敏感词管理列表
     *
     * @param sysSensitiveWord 敏感词管理
     * @return 敏感词管理
     */
    @Override
    public List<SysSensitiveWord> selectSysSensitiveWordList(SysSensitiveWord sysSensitiveWord)
    {
        return sysSensitiveWordMapper.selectSysSensitiveWordList(sysSensitiveWord);
    }

    /**
     * 新增敏感词管理
     *
     * @param sysSensitiveWord 敏感词管理
     * @return 结果
     */
    @Override
    public int insertSysSensitiveWord(SysSensitiveWord sysSensitiveWord) {
        sysSensitiveWord.setCreateTime(DateUtils.getNowDate());
        int result = sysSensitiveWordMapper.insertSysSensitiveWord(sysSensitiveWord);
        refreshFilter();
        return result;
    }

    /**
     * 修改敏感词管理
     *
     * @param sysSensitiveWord 敏感词管理
     * @return 结果
     */
    @Override
    public int updateSysSensitiveWord(SysSensitiveWord sysSensitiveWord)
    {
        sysSensitiveWord.setUpdateTime(DateUtils.getNowDate());
        int result = sysSensitiveWordMapper.updateSysSensitiveWord(sysSensitiveWord);
        refreshFilter();
        return result;
    }

    /**
     * 批量删除敏感词管理
     *
     * @param ids 需要删除的敏感词管理主键
     * @return 结果
     */
    @Override
    public int deleteSysSensitiveWordByIds(Long[] ids)
    {
        int result = sysSensitiveWordMapper.deleteSysSensitiveWordByIds(ids);
        refreshFilter();
        return result;
    }

    /**
     * 删除敏感词管理信息
     *
     * @param id 敏感词管理主键
     * @return 结果
     */
    @Override
    public int deleteSysSensitiveWordById(Long id)
    {
        return sysSensitiveWordMapper.deleteSysSensitiveWordById(id);
    }

    /**
     * AI生成敏感词并批量入库
     */
    @Override
    public int aiGenerateWords(String topic, String category, int count) {
        log.info("AI生成敏感词，主题: {}, 分类: {}, 数量: {}", topic, category, count);

        // 1. 参数校验
        if (topic == null || topic.trim().isEmpty()) {
            throw new BusinessException("主题不能为空");
        }
        if (category == null || category.trim().isEmpty()) {
            throw new BusinessException("分类不能为空");
        }
        if (count < 5 || count > 50) {
            count = 20;
        }

        // 2. 构建提示词
        String systemPrompt = PromptTemplate.SENSITIVE_WORD_SYSTEM_PROMPT;
        String userPrompt = promptTemplate.buildSensitiveWordPrompt(topic.trim(), count);

        log.debug("【敏感词生成-系统提示词】: {}", systemPrompt);
        log.debug("【敏感词生成-用户提示词】: {}", userPrompt);

        // 3. 调用AI（支持重试）
        String response = null;
        int maxRetries = 2;
        Exception lastException = null;

        for (int i = 0; i <= maxRetries; i++) {
            try {
                response = aiService.chatWithSystem(systemPrompt, userPrompt, "zhipuai");
                log.debug("【AI原始响应】: {}", response);
                break;
            } catch (Exception e) {
                lastException = e;
                if (i == maxRetries) {
                    log.error("AI调用失败，已重试{}次", maxRetries, e);
                    throw new BusinessException("AI服务暂时不可用，请稍后重试");
                }
                log.warn("AI调用失败，第{}次重试", i + 1);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {}
            }
        }

        if (response == null) {
            throw new BusinessException("AI未返回有效内容");
        }

        // 4. 解析JSON数组
        List<String> words = parseWordsFromAiResponse(response);
        log.info("AI返回 {} 个敏感词", words.size());

        if (words.isEmpty()) {
            log.warn("AI未返回任何有效敏感词");
            return 0;
        }

        // 5. 过滤和清洗
        words = filterAndCleanWords(words);
        log.info("清洗后剩余 {} 个有效敏感词", words.size());

        if (words.isEmpty()) {
            log.warn("清洗后无有效敏感词");
            return 0;
        }

        // 6. 批量入库（去重）
        int addedCount = batchInsertWords(words, category);

        // 7. 刷新过滤器
        refreshFilter();

        log.info("AI生成敏感词完成，成功添加 {} 个", addedCount);
        return addedCount;
    }

    /**
     * 从AI响应中解析敏感词数组
     */
    private List<String> parseWordsFromAiResponse(String response) {
        List<String> result = new ArrayList<>();
        if (response == null || response.isEmpty()) {
            return result;
        }

        try {
            // 清洗并尝试解析JSON
            String cleaned = response
                    .replaceAll("```json\\s*", "")
                    .replaceAll("```\\s*", "")
                    .trim();

            // 提取数组部分
            int start = cleaned.indexOf('[');
            int end = cleaned.lastIndexOf(']');
            if (start != -1 && end != -1 && end > start) {
                cleaned = cleaned.substring(start, end + 1);
            }

            List<String> parsed = com.alibaba.fastjson.JSON.parseArray(cleaned, String.class);
            if (parsed != null) {
                result.addAll(parsed);
            }
        } catch (Exception e) {
            log.error("解析AI生成的敏感词失败，响应内容: {}", response, e);
        }
        return result;
    }

    /**
     * 过滤和清洗敏感词
     */
    private List<String> filterAndCleanWords(List<String> words) {
        if (words == null || words.isEmpty()) {
            return new ArrayList<>();
        }

        return words.stream()
                .filter(w -> w != null && !w.trim().isEmpty())
                .map(String::trim)
                .filter(w -> w.length() >= 2 && w.length() <= 20)
                .filter(w -> !w.matches(".*[，。！？；：\"'【】《》（）\\[\\]{}].*"))
                .filter(w -> !w.matches("\\d+"))
                .filter(w -> !w.matches("[a-zA-Z]+"))
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 批量插入敏感词
     */
    private int batchInsertWords(List<String> words, String category) {
        int addedCount = 0;
        int reEnabledCount = 0;
        int duplicateCount = 0;

        for (String word : words) {
            // 检查是否已存在
            SysSensitiveWord query = new SysSensitiveWord();
            query.setWord(word);
            List<SysSensitiveWord> existList = sysSensitiveWordMapper.selectSysSensitiveWordList(query);

            if (!existList.isEmpty()) {
                duplicateCount++;
                SysSensitiveWord exist = existList.get(0);
                // 如果已禁用，则重新启用
                if (exist.getStatus() != null && exist.getStatus() == 0L) {
                    exist.setStatus(1L);
                    exist.setRemark("AI生成-重新启用");
                    exist.setUpdateTime(DateUtils.getNowDate());
                    sysSensitiveWordMapper.updateSysSensitiveWord(exist);
                    reEnabledCount++;
                }
                continue;
            }

            // 新增
            SysSensitiveWord newWord = new SysSensitiveWord();
            newWord.setWord(word);
            newWord.setCategory(category);
            newWord.setStatus(1L);
            newWord.setRemark("AI生成");
            newWord.setCreateTime(DateUtils.getNowDate());
            sysSensitiveWordMapper.insertSysSensitiveWord(newWord);
            addedCount++;
        }

        log.info("批量插入完成 - 新增: {}, 重新启用: {}, 跳过重复: {}",
                addedCount, reEnabledCount, duplicateCount - reEnabledCount);

        return addedCount + reEnabledCount;
    }

    /**
     * 刷新敏感词过滤器缓存
     */
    private void refreshFilter() {
        if (sensitiveWordFilter != null) {
            new Thread(sensitiveWordFilter::refreshWordBank).start();
        }
    }
}