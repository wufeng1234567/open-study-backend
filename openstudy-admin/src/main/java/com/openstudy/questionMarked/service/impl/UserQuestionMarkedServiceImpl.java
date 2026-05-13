package com.openstudy.questionMarked.service.impl;

import com.openstudy.common.exception.ServiceException;
import com.openstudy.common.utils.DateUtils;
import com.openstudy.questionMain.domain.QuestionMain;
import com.openstudy.questionMain.service.IQuestionMainService;
import com.openstudy.questionMarked.domain.UserQuestionMarked;
import com.openstudy.questionMarked.mapper.UserQuestionMarkedMapper;
import com.openstudy.questionMarked.service.IUserQuestionMarkedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户斩题（重点攻克题目）Service业务层处理
 * 
 * @author ruoyi
 * @date 2025-12-17
 */
@Service
public class UserQuestionMarkedServiceImpl implements IUserQuestionMarkedService 
{
    @Autowired
    private UserQuestionMarkedMapper userQuestionMarkedMapper;

    @Autowired
    private IQuestionMainService questionMainService;


    /**
     * 查询用户斩题（重点攻克题目）
     * 
     * @param markedId 用户斩题（重点攻克题目）主键
     * @return 用户斩题（重点攻克题目）
     */
    @Override
    public UserQuestionMarked selectUserQuestionMarkedByMarkedId(Long markedId)
    {
        return userQuestionMarkedMapper.selectUserQuestionMarkedByMarkedId(markedId);
    }

    /**
     * 查询用户斩题（重点攻克题目）列表
     * 
     * @param userQuestionMarked 用户斩题（重点攻克题目）
     * @return 用户斩题（重点攻克题目）
     */
    @Override
    public List<UserQuestionMarked> selectUserQuestionMarkedList(UserQuestionMarked userQuestionMarked)
    {
        return userQuestionMarkedMapper.selectUserQuestionMarkedList(userQuestionMarked);
    }

    /**
     * 新增用户斩题（重点攻克题目）
     *
     * @param userQuestionMarked 用户斩题（重点攻克题目）
     * @return 结果
     */
    // @Override
    // public int insertUserQuestionMarked(UserQuestionMarked userQuestionMarked)
    // {
    //     userQuestionMarked.setCreateTime(DateUtils.getNowDate());
    //     return userQuestionMarkedMapper.insertUserQuestionMarked(userQuestionMarked);
    // }

    /**
     * 修改用户斩题（重点攻克题目）
     * 
     * @param userQuestionMarked 用户斩题（重点攻克题目）
     * @return 结果
     */
    @Override
    public int updateUserQuestionMarked(UserQuestionMarked userQuestionMarked)
    {
        userQuestionMarked.setUpdateTime(DateUtils.getNowDate());
        return userQuestionMarkedMapper.updateUserQuestionMarked(userQuestionMarked);
    }

    /**
     * 批量删除用户斩题（重点攻克题目）
     * 
     * @param markedIds 需要删除的用户斩题（重点攻克题目）主键
     * @return 结果
     */
    @Override
    public int deleteUserQuestionMarkedByMarkedIds(Long[] markedIds)
    {
        return userQuestionMarkedMapper.deleteUserQuestionMarkedByMarkedIds(markedIds);
    }

    /**
     * 删除用户斩题（重点攻克题目）信息
     * 
     * @param markedId 用户斩题（重点攻克题目）主键
     * @return 结果
     */
    @Override
    public int deleteUserQuestionMarkedByMarkedId(Long markedId)
    {
        return userQuestionMarkedMapper.deleteUserQuestionMarkedByMarkedId(markedId);
    }


    @Override
    public boolean checkQuestionMarkedExists(Long userId, Long questionId) {
        UserQuestionMarked param = new UserQuestionMarked();
        param.setUserId(userId);
        param.setQuestionId(questionId);
        param.setMarkedStatus(1L); // 只查询活跃状态的斩题
        List<UserQuestionMarked> list = userQuestionMarkedMapper.selectUserQuestionMarkedList(param);
        return list != null && !list.isEmpty();
    }

    @Override
    public UserQuestionMarked selectUserQuestionMarkedByUserIdAndQuestionId(Long userId, Long questionId) {
        UserQuestionMarked param = new UserQuestionMarked();
        param.setUserId(userId);
        param.setQuestionId(questionId);
        param.setMarkedStatus(1L);
        List<UserQuestionMarked> list = userQuestionMarkedMapper.selectUserQuestionMarkedList(param);
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    /**
     * 新增用户斩题时的验证逻辑（在insertUserQuestionMarked方法中添加）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertUserQuestionMarked(UserQuestionMarked userQuestionMarked) {
        // 1. 基本参数验证
        validateRequiredFields(userQuestionMarked);

        // 2. 验证题目是否存在
        validateQuestionExists(userQuestionMarked.getQuestionId());

        // 3. 验证是否已斩该题目
        validateNotDuplicateMarked(userQuestionMarked.getUserId(),
                userQuestionMarked.getQuestionId(),
                null);

        // 4. 获取题库ID（如果未提供）
        if (userQuestionMarked.getBankId() == null) {
            Long bankId = getQuestionBankId(userQuestionMarked.getQuestionId());
            userQuestionMarked.setBankId(bankId);
        }

        // 5. 设置默认值
        setDefaultValues(userQuestionMarked);

        // 6. 设置创建时间
        userQuestionMarked.setCreateTime(DateUtils.getNowDate());

        // 7. 执行插入
        return userQuestionMarkedMapper.insertUserQuestionMarked(userQuestionMarked);
    }

    /**
     * 根据题目ID获取题库ID
     */
    @Override
    public Long getQuestionBankId(Long questionId) {
        QuestionMain questionMain = questionMainService.selectQuestionMainById(questionId);
        if (questionMain == null) {
            throw new ServiceException("题目ID " + questionId + " 不存在");
        }
        return questionMain.getBankId();
    }

// ================ 私有验证方法 ================

    /**
     * 验证必填字段
     */
    private void validateRequiredFields(UserQuestionMarked marked) {
        if (marked.getUserId() == null) {
            throw new ServiceException("用户ID不能为空");
        }
        if (marked.getQuestionId() == null) {
            throw new ServiceException("题目ID不能为空");
        }
        if (marked.getMarkedType() == null) {
            throw new ServiceException("斩题类型不能为空");
        }
    }

    /**
     * 验证题目是否存在
     */
    private void validateQuestionExists(Long questionId) {
        boolean exists = questionMainService.checkQuestionExists(questionId);
        if (!exists) {
            throw new ServiceException("题目ID " + questionId + " 不存在，请检查题目ID是否正确");
        }
    }

    /**
     * 验证是否重复斩题
     */
    private void validateNotDuplicateMarked(Long userId, Long questionId, Long excludeMarkedId) {
        UserQuestionMarked param = new UserQuestionMarked();
        param.setUserId(userId);
        param.setQuestionId(questionId);
        param.setMarkedStatus(1L);

        List<UserQuestionMarked> list = userQuestionMarkedMapper.selectUserQuestionMarkedList(param);

        if (excludeMarkedId == null) {
            // 新增时，只要存在记录就报错
            if (list != null && !list.isEmpty()) {
                throw new ServiceException("用户已斩该题目，不能重复斩题");
            }
        } else {
            // 修改时，检查除当前记录外是否还有其他记录
            if (list != null && !list.isEmpty()) {
                for (UserQuestionMarked marked : list) {
                    if (!marked.getMarkedId().equals(excludeMarkedId)) {
                        throw new ServiceException("用户已斩该题目，不能重复斩题");
                    }
                }
            }
        }
    }

    /**
     * 设置默认值
     */
    private void setDefaultValues(UserQuestionMarked marked) {
        if (marked.getMarkedStatus() == null) {
            marked.setMarkedStatus(1L); // 默认活跃状态
        }
        if (marked.getIsMastered() == null) {
            marked.setIsMastered(0L); // 默认未掌握
        }
        if (marked.getDifficultyLevel() == null) {
            marked.setDifficultyLevel(2L); // 默认中等难度
        }
        if (marked.getErrorTimesBeforeMark() == null) {
            marked.setErrorTimesBeforeMark(0L);
        }
        if (marked.getCorrectTimesBeforeMark() == null) {
            marked.setCorrectTimesBeforeMark(0L);
        }
        if (marked.getReviewCount() == null) {
            marked.setReviewCount(0L);
        }
    }



}
