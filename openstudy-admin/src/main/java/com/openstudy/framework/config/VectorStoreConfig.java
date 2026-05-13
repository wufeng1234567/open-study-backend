package com.openstudy.framework.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPooled;

@Slf4j
@Configuration
public class VectorStoreConfig {

    @Value("${spring.ai.vectorstore.redis.index-name:spring-ai-index}")
    private String indexName;

    @Value("${spring.ai.vectorstore.redis.prefix:embedding:}")
    private String prefix;

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Bean
    public JedisPooled jedisPooled() {
        log.info("创建 JedisPooled 连接，地址: {}:{}", redisHost, redisPort);
        return new JedisPooled(redisHost, redisPort);
    }

    @Bean
    public VectorStore redisVectorStore(
            JedisPooled jedisPooled,
            @Qualifier("zhiPuAiEmbeddingModel") EmbeddingModel embeddingModel) {
        log.info("初始化 RedisVectorStore，索引名: {}, 前缀: {}", indexName, prefix);

        return RedisVectorStore.builder(jedisPooled, embeddingModel)
                .indexName(indexName)
                .prefix(prefix)
                .initializeSchema(true)
                .build();
    }
}