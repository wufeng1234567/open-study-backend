package com.openstudy.questionError.service.impl;

import com.openstudy.common.utils.DateUtils;
import com.openstudy.questionError.domain.UserQuestionError;
import com.openstudy.questionError.mapper.UserQuestionErrorMapper;
import com.openstudy.questionError.service.IUserQuestionErrorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户错题记录（支持复习与掌握跟踪）Service业务层处理
 *
 * @author liu
 * @date 2025-12-16
 */
@Service
public class UserQuestionErrorServiceImpl implements IUserQuestionErrorService
{
    @Autowired
    private UserQuestionErrorMapper userQuestionErrorMapper;

    /**
     * 查询用户错题记录（支持复习与掌握跟踪）
     *
     * @param errorId 用户错题记录（支持复习与掌握跟踪）主键
     * @return 用户错题记录（支持复习与掌握跟踪）
     */
    @Override
    public UserQuestionError selectUserQuestionErrorByErrorId(Long errorId)
    {
        return userQuestionErrorMapper.selectUserQuestionErrorByErrorId(errorId);
    }

    /**
     * 查询用户错题记录（支持复习与掌握跟踪）列表
     *
     * @param userQuestionError 用户错题记录（支持复习与掌握跟踪）
     * @return 用户错题记录（支持复习与掌握跟踪）
     */
    @Override
    public List<UserQuestionError> selectUserQuestionErrorList(UserQuestionError userQuestionError)
    {
        return userQuestionErrorMapper.selectUserQuestionErrorList(userQuestionError);
    }

    /**
     * 新增用户错题记录（支持复习与掌握跟踪）
     *
     * @param userQuestionError 用户错题记录（支持复习与掌握跟踪）
     * @return 结果
     */
    @Override
    public int insertUserQuestionError(UserQuestionError userQuestionError)
    {
        userQuestionError.setCreateTime(DateUtils.getNowDate());
        return userQuestionErrorMapper.insertUserQuestionError(userQuestionError);
    }

    /**
     * 修改用户错题记录（支持复习与掌握跟踪）
     *
     * @param userQuestionError 用户错题记录（支持复习与掌握跟踪）
     * @return 结果
     */
    @Override
    public int updateUserQuestionError(UserQuestionError userQuestionError)
    {
        userQuestionError.setUpdateTime(DateUtils.getNowDate());
        return userQuestionErrorMapper.updateUserQuestionError(userQuestionError);
    }

    /**
     * 批量删除用户错题记录（支持复习与掌握跟踪）
     *
     * @param errorIds 需要删除的用户错题记录（支持复习与掌握跟踪）主键
     * @return 结果
     */
    @Override
    public int deleteUserQuestionErrorByErrorIds(Long[] errorIds)
    {
        return userQuestionErrorMapper.deleteUserQuestionErrorByErrorIds(errorIds);
    }

    /**
     * 删除用户错题记录（支持复习与掌握跟踪）信息
     *
     * @param errorId 用户错题记录（支持复习与掌握跟踪）主键
     * @return 结果
     */
    @Override
    public int deleteUserQuestionErrorByErrorId(Long errorId)
    {
        return userQuestionErrorMapper.deleteUserQuestionErrorByErrorId(errorId);
    }

    @Override
    public boolean checkUserQuestionErrorExists(Long userId, Long questionId) {
        if (userId == null || userId <= 0 || questionId == null || questionId <= 0) {
            return false;
        }

        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("questionId", questionId);

        int count = userQuestionErrorMapper.checkUserQuestionErrorExists(params);
        return count > 0;
    }

    @Override
    public boolean incrementErrorCount(Long userId, Long questionId) {
        if (userId == null || userId <= 0 || questionId == null || questionId <= 0) {
            return false;
        }

        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("questionId", questionId);

        int affectedRows = userQuestionErrorMapper.incrementErrorCount(params);
        return affectedRows > 0;
    }

    @Override
    public boolean recordOrUpdateQuestionError(UserQuestionError errorRecord) {
        if (errorRecord == null || errorRecord.getUserId() == null || errorRecord.getQuestionId() == null) {
            return false;
        }

        Long userId = errorRecord.getUserId();
        Long questionId = errorRecord.getQuestionId();

        // 检查是否已存在
        if (checkUserQuestionErrorExists(userId, questionId)) {
            // 已存在，增加错误次数
            return incrementErrorCount(userId, questionId);
        } else {
            // 不存在，新增记录
            // 设置默认值
            if (errorRecord.getErrorCount() == null) {
                errorRecord.setErrorCount(1L);
            }
            if (errorRecord.getStatus() == null) {
                errorRecord.setStatus(1L);
            }
            if (errorRecord.getIsMastered() == null) {
                errorRecord.setIsMastered(0L);
            }

            // 设置创建时间
            errorRecord.setCreateTime(DateUtils.getNowDate());

            int result = userQuestionErrorMapper.insertUserQuestionError(errorRecord);
            return result > 0;
        }
    }
}