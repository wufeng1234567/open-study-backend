package com.openstudy.sensitiveWord.filter;

import com.openstudy.sensitiveWord.domain.SysSensitiveWord;
import com.openstudy.sensitiveWord.mapper.SysSensitiveWordMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 敏感词过滤器
 * 支持热加载词库，线程安全
 */
@Slf4j
@Component
public class SensitiveWordFilter {

    @Autowired
    private SysSensitiveWordMapper sensitiveWordMapper;

    private volatile AhoCorasick acMachine;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    @PostConstruct
    public void init() {
        refreshWordBank();
    }

    /**
     * 定时刷新词库（每5分钟）
     */
    @Scheduled(fixedDelay = 300000)
    public void scheduledRefresh() {
        refreshWordBank();
    }

    /**
     * 手动刷新词库（管理端增删改后调用）
     */
    public void refreshWordBank() {
        try {
            List<SysSensitiveWord> words = loadEnabledWords();
            AhoCorasick newMachine = new AhoCorasick();
            for (SysSensitiveWord word : words) {
                newMachine.insert(word.getWord());
            }
            newMachine.buildFailureLinks();
            
            lock.writeLock().lock();
            try {
                this.acMachine = newMachine;
            } finally {
                lock.writeLock().unlock();
            }
            log.info("敏感词库刷新完成，加载 {} 个词", words.size());
        } catch (Exception e) {
            log.error("刷新敏感词库失败", e);
        }
    }

    private List<SysSensitiveWord> loadEnabledWords() {
        SysSensitiveWord query = new SysSensitiveWord();
        query.setStatus(1L); // 只加载启用状态的词
        return sensitiveWordMapper.selectSysSensitiveWordList(query);
    }

    /**
     * 检查文本是否包含敏感词
     */
    public boolean contains(String text) {
        if (text == null || text.isEmpty()) return false;
        lock.readLock().lock();
        try {
            return acMachine.contains(text);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 查找第一个敏感词
     */
    public String findFirst(String text) {
        if (text == null || text.isEmpty()) return null;
        lock.readLock().lock();
        try {
            return acMachine.findFirst(text);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 查找所有敏感词
     */
    public List<String> findAll(String text) {
        if (text == null || text.isEmpty()) return java.util.Collections.emptyList();
        lock.readLock().lock();
        try {
            return acMachine.findAll(text);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 替换敏感词（用于日志脱敏）
     */
    public String replace(String text) {
        if (text == null || text.isEmpty()) return text;
        lock.readLock().lock();
        try {
            return acMachine.replace(text, '*');
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 获取当前词库大小
     */
    public int getWordCount() {
        lock.readLock().lock();
        try {
            return acMachine == null ? 0 : acMachine.size();
        } finally {
            lock.readLock().unlock();
        }
    }
}