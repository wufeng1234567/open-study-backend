package com.openstudy.web.controller.system;

import com.openstudy.common.annotation.Log;
import com.openstudy.common.core.controller.BaseController;
import com.openstudy.common.core.domain.AjaxResult;
import com.openstudy.common.core.redis.RedisCache;
import com.openstudy.common.enums.BusinessType;
import com.openstudy.system.domain.ChatMessage;
import com.openstudy.common.core.domain.entity.SysUser;
import com.openstudy.system.service.IChatMessageService;
import com.openstudy.system.service.ISysUserService;
import com.openstudy.web.websocket.ChatWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/system/chat")
public class SysChatController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(SysChatController.class);

    @Autowired
    private IChatMessageService chatMessageService;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private ChatWebSocketHandler chatWebSocketHandler;

    @PreAuthorize("@ss.hasRole('common')")
    @GetMapping("/conversations")
    public AjaxResult getConversations() {
        Long userId = getUserId();
        List<ChatMessage> conversations = chatMessageService.getRecentConversations(userId, 50);

        List<Map<String, Object>> result = new ArrayList<>();
        Set<Long> userIds = new HashSet<>();
        for (ChatMessage msg : conversations) {
            Long otherUserId = msg.getSenderId().equals(userId) ? msg.getReceiverId() : msg.getSenderId();
            userIds.add(otherUserId);
        }

        Map<Long, SysUser> userMap = new HashMap<>();
        for (Long uid : userIds) {
            SysUser user = userService.selectUserById(uid);
            if (user != null) {
                userMap.put(uid, user);
            }
        }

        for (ChatMessage msg : conversations) {
            Long otherUserId = msg.getSenderId().equals(userId) ? msg.getReceiverId() : msg.getSenderId();
            SysUser otherUser = userMap.get(otherUserId);

            Map<String, Object> item = new HashMap<>();
            item.put("userId", otherUserId);
            item.put("nickname", otherUser != null ? otherUser.getNickName() : "未知用户");
            item.put("avatar", otherUser != null ? otherUser.getAvatar() : null);

            boolean isSent = msg.getSenderId().equals(userId);
            item.put("lastMessage", isSent ? "我: " + msg.getContent() : msg.getContent());
            item.put("lastMessageTime", msg.getCreateTime());
            item.put("unreadCount", chatMessageService.getUnreadCountFromUser(userId, otherUserId));

            result.add(item);
        }

        return success(result);
    }

    @PreAuthorize("@ss.hasRole('common')")
    @GetMapping("/history/{otherUserId}")
    public AjaxResult getHistory(@PathVariable Long otherUserId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = getUserId();
        List<ChatMessage> messages = chatMessageService.getConversation(userId, otherUserId, page, size);

        Collections.reverse(messages);

        List<Map<String, Object>> result = new ArrayList<>();
        for (ChatMessage msg : messages) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", msg.getId());
            item.put("senderId", msg.getSenderId());
            item.put("senderNickname", msg.getSenderNickname());
            item.put("senderAvatar", msg.getSenderAvatar());
            item.put("receiverId", msg.getReceiverId());
            item.put("receiverNickname", msg.getReceiverNickname());
            item.put("receiverAvatar", msg.getReceiverAvatar());
            item.put("content", msg.getContent());
            item.put("isRead", msg.isRead());
            item.put("createTime", msg.getCreateTime());
            result.add(item);
        }

        return success(result);
    }

    @PreAuthorize("@ss.hasRole('common')")
    @PostMapping("/send")
    public AjaxResult sendMessage(@RequestBody Map<String, Object> params) {
        Long senderId = getUserId();
        String senderName = getUsername();
        Long receiverId = Long.valueOf(params.get("receiverId").toString());
        String content = params.get("content").toString();

        SysUser sender = userService.selectUserById(senderId);
        SysUser receiver = userService.selectUserById(receiverId);

        if (sender == null || receiver == null) {
            return error("用户不存在");
        }

        ChatMessage message = new ChatMessage();
        message.setSenderId(senderId);
        message.setSenderName(senderName);
        message.setSenderNickname(sender.getNickName());
        message.setSenderAvatar(sender.getAvatar());
        message.setReceiverId(receiverId);
        message.setReceiverName(receiver.getUserName());
        message.setReceiverNickname(receiver.getNickName());
        message.setReceiverAvatar(receiver.getAvatar());
        message.setContent(content);

        ChatMessage saved = chatMessageService.sendMessage(message);

        Map<String, Object> result = new HashMap<>();
        result.put("id", saved.getId());
        result.put("senderId", saved.getSenderId());
        result.put("senderNickname", saved.getSenderNickname());
        result.put("senderAvatar", saved.getSenderAvatar());
        result.put("receiverId", saved.getReceiverId());
        result.put("receiverNickname", saved.getReceiverNickname());
        result.put("receiverAvatar", saved.getReceiverAvatar());
        result.put("content", saved.getContent());
        result.put("createTime", saved.getCreateTime());

        return success(result);
    }

    @PreAuthorize("@ss.hasRole('common')")
    @PostMapping("/markRead/{senderId}")
    public AjaxResult markRead(@PathVariable Long senderId) {
        Long receiverId = getUserId();
        chatMessageService.markAsRead(receiverId, senderId);
        return success();
    }

    @PreAuthorize("@ss.hasRole('common')")
    @DeleteMapping("/message/{messageId}")
    public AjaxResult deleteMessage(@PathVariable Long messageId) {
        Long userId = getUserId();
        chatMessageService.deleteMessage(messageId, userId);
        return success();
    }

    @PreAuthorize("@ss.hasRole('common')")
    @DeleteMapping("/conversation/{otherUserId}")
    public AjaxResult deleteConversation(@PathVariable Long otherUserId) {
        Long userId = getUserId();
        chatMessageService.deleteConversation(userId, otherUserId);
        return success();
    }

    @PreAuthorize("@ss.hasRole('common')")
    @PostMapping("/history/clear/{otherUserId}")
    public AjaxResult clearChatHistory(@PathVariable Long otherUserId) {
        Long userId = getUserId();
        chatMessageService.hideChatHistoryForUser(userId, otherUserId);
        return success();
    }

    @PreAuthorize("@ss.hasRole('common')")
    @GetMapping("/unreadCount")
    public AjaxResult getUnreadCount() {
        Long userId = getUserId();
        int count = chatMessageService.getUnreadCount(userId);
        return success(count);
    }

    @GetMapping("/ws/debug")
    public AjaxResult getWsDebug() {
        String debugInfo = chatWebSocketHandler.getOnlineUsersDebug();
        int onlineCount = chatWebSocketHandler.getOnlineCount();
        Map<String, Object> result = new HashMap<>();
        result.put("onlineCount", onlineCount);
        result.put("details", debugInfo);
        return success(result);
    }

    @PreAuthorize("@ss.hasRole('common')")
    @PostMapping("/sync")
    public AjaxResult syncToDatabase() {
        Long userId = getUserId();
        chatMessageService.syncToDatabase(userId);
        return success();
    }

    @PreAuthorize("@ss.hasRole('common')")
    @GetMapping("/users")
    public AjaxResult searchUsers(@RequestParam(required = false) String keyword) {
        Long currentUserId = getUserId();
        List<SysUser> users = userService.selectUserList(null);

        List<Map<String, Object>> result = new ArrayList<>();
        for (SysUser user : users) {
            if (user.getUserId().equals(currentUserId))
                continue;

            if (keyword != null && !keyword.isEmpty()) {
                String lowerKeyword = keyword.toLowerCase();
                boolean match = false;
                if (user.getUserName() != null && user.getUserName().toLowerCase().contains(lowerKeyword)) {
                    match = true;
                }
                if (user.getNickName() != null && user.getNickName().toLowerCase().contains(lowerKeyword)) {
                    match = true;
                }
                if (!match)
                    continue;
            }

            Map<String, Object> item = new HashMap<>();
            item.put("userId", user.getUserId());
            item.put("username", user.getUserName());
            item.put("nickname", user.getNickName());
            item.put("avatar", user.getAvatar());
            result.add(item);
        }

        return success(result);
    }
}
