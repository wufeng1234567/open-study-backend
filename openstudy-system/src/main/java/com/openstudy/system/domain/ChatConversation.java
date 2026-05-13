package com.openstudy.system.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openstudy.common.core.domain.BaseEntity;

public class ChatConversation extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private Long otherUserId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getOtherUserId() {
        return otherUserId;
    }

    public void setOtherUserId(Long otherUserId) {
        this.otherUserId = otherUserId;
    }

    @Override
    public String toString() {
        return "ChatConversation{" +
                "id=" + id +
                ", userId=" + userId +
                ", otherUserId=" + otherUserId +
                '}';
    }
}
