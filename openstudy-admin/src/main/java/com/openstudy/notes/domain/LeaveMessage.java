package com.openstudy.notes.domain;

import com.openstudy.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 留言板对象 leave_message
 *
 * @author openstudy
 * @date 2026-05-03
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LeaveMessage extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 留言ID */
    private Long id;

    /** 留言用户ID */
    private Long userId;

    /** 留言用户名 */
    private String userName;

    /** 留言内容 */
    private String content;

    /** IP地址 */
    private String ip;

    /** 状态：0-已删除，1-正常 */
    private Integer status;
}
