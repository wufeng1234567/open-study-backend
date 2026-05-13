package com.openstudy.web.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketHandler.class);

    private final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    private final Map<String, Long> sessionUserMap = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = getUserIdFromSession(session);
        log.info("WebSocket 连接建立: userId={}, sessionId={}, uri={}, currentSessions={}",
                userId, session.getId(), session.getUri(), userSessions.size());
        if (userId != null) {
            WebSocketSession oldSession = userSessions.get(userId);
            if (oldSession != null && oldSession.isOpen()) {
                log.info("用户 {} 已存在旧会话 {}，先关闭旧会话", userId, oldSession.getId());
                oldSession.close();
            }
            userSessions.remove(userId);
            for (Map.Entry<String, Long> entry : sessionUserMap.entrySet()) {
                if (entry.getValue().equals(userId)) {
                    sessionUserMap.remove(entry.getKey());
                    break;
                }
            }
            userSessions.put(userId, session);
            sessionUserMap.put(session.getId(), userId);
            log.info("WebSocket 连接建立完成: userId={}, sessionId={}", userId, session.getId());
        } else {
            log.warn("WebSocket 连接建立失败: 无法从session获取userId, uri={}", session.getUri());
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long userId = getUserIdFromSession(session);
        log.debug("收到消息 from userId={}: {}", userId, message.getPayload());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = sessionUserMap.remove(session.getId());
        if (userId != null) {
            WebSocketSession currentSession = userSessions.get(userId);
            if (currentSession != null && currentSession.getId().equals(session.getId())) {
                userSessions.remove(userId);
                log.info("WebSocket 连接关闭: userId={}, status={}, sessionId={}", userId, status, session.getId());
            } else {
                log.info("WebSocket 连接关闭 (忽略 stale session): userId={}, status={}, closedSessionId={}, currentSessionId={}",
                        userId, status, session.getId(), currentSession != null ? currentSession.getId() : "null");
            }
        }
    }

    public void sendMessageToUser(Long userId, String message) {
        WebSocketSession session = userSessions.get(userId);
        log.info("sendMessageToUser called: userId={}, sessionFound={}, messageLength={}",
                userId, session != null, message != null ? message.length() : 0);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
                log.debug("发送消息 to userId={}: {}", userId, message);
            } catch (IOException e) {
                log.error("发送消息失败 to userId={}", userId, e);
            }
        } else {
            log.warn("发送消息失败: userId={} 的WebSocket会话不存在或已关闭", userId);
        }
    }

    public boolean isUserOnline(Long userId) {
        WebSocketSession session = userSessions.get(userId);
        return session != null && session.isOpen();
    }

    public int getOnlineCount() {
        return (int) userSessions.values().stream().filter(WebSocketSession::isOpen).count();
    }

    public String getOnlineUsersDebug() {
        StringBuilder sb = new StringBuilder("Online users: ");
        for (Map.Entry<Long, WebSocketSession> entry : userSessions.entrySet()) {
            sb.append(String.format("userId=%d(isOpen=%b), ", entry.getKey(), entry.getValue().isOpen()));
        }
        return sb.toString();
    }

    private Long getUserIdFromSession(WebSocketSession session) {
        String query = session.getUri() != null ? session.getUri().getQuery() : null;
        if (query != null) {
            for (String param : query.split("&")) {
                String[] parts = param.split("=");
                if (parts.length == 2 && "userId".equals(parts[0])) {
                    try {
                        return Long.parseLong(parts[1]);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        return null;
    }
}
