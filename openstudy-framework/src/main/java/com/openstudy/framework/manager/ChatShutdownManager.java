package com.openstudy.framework.manager;

import com.openstudy.system.service.ChatSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import jakarta.annotation.PreDestroy;

@Component
public class ChatShutdownManager {
    private static final Logger log = LoggerFactory.getLogger(ChatShutdownManager.class);

    @Autowired
    private ChatSyncService chatSyncService;

    @PreDestroy
    public void destroy() {
        try {
            log.info("====关闭聊天消息同步线程====");
            chatSyncService.syncOnShutdown();
        } catch (Exception e) {
            log.error("聊天消息同步失败", e);
        }
    }
}
