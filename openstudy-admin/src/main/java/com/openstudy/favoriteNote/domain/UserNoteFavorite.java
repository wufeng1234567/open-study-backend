package com.openstudy.favoriteNote.domain;

import com.openstudy.common.core.domain.BaseEntity;
import lombok.Data;

/**
 * 用户笔记收藏实体
 *
 * @author liu
 * @date 2026-05-04
 */
@Data
public class UserNoteFavorite extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 收藏ID */
    private Long favoriteId;

    /** 用户ID */
    private Long userId;

    /** 笔记ID */
    private Long noteId;
}
