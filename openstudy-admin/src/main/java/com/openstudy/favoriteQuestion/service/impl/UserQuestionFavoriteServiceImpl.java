package com.openstudy.favoriteQuestion.service.impl;

import com.openstudy.common.exception.ServiceException;
import com.openstudy.common.utils.DateUtils;
import com.openstudy.favoriteQuestion.domain.UserQuestionFavorite;
import com.openstudy.favoriteQuestion.mapper.UserQuestionFavoriteMapper;
import com.openstudy.favoriteQuestion.service.IUserQuestionFavoriteService;
import com.openstudy.questionMain.domain.QuestionMain;
import com.openstudy.questionMain.service.IQuestionMainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户题目收藏（支持复习功能）Service业务层处理
 *
 * @author liu
 * @date 2025-12-10
 */
@Service
public class UserQuestionFavoriteServiceImpl implements IUserQuestionFavoriteService
{
    @Autowired
    private UserQuestionFavoriteMapper userQuestionFavoriteMapper;

    @Autowired
    private IQuestionMainService questionMainService;

    /**
     * 查询用户题目收藏（支持复习功能）
     *
     * @param favoriteId 用户题目收藏（支持复习功能）主键
     * @return 用户题目收藏（支持复习功能）
     */
    @Override
    public UserQuestionFavorite selectUserQuestionFavoriteByFavoriteId(Long favoriteId)
    {
        return userQuestionFavoriteMapper.selectUserQuestionFavoriteByFavoriteId(favoriteId);
    }

    /**
     * 查询用户题目收藏（支持复习功能）列表
     *
     * @param userQuestionFavorite 用户题目收藏（支持复习功能）
     * @return 用户题目收藏（支持复习功能）
     */
    @Override
    public List<UserQuestionFavorite> selectUserQuestionFavoriteList(UserQuestionFavorite userQuestionFavorite)
    {
        return userQuestionFavoriteMapper.selectUserQuestionFavoriteList(userQuestionFavorite);
    }

    /**
     * 新增用户题目收藏（支持复习功能）
     *
     * @param userQuestionFavorite 用户题目收藏（支持复习功能）
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertUserQuestionFavorite(UserQuestionFavorite userQuestionFavorite)
    {
        // 1. 基本参数验证
        validateRequiredFields(userQuestionFavorite);

        // 2. 验证题目是否存在
        validateQuestionExists(userQuestionFavorite.getQuestionId());

        // 3. 验证是否已收藏该题目
        validateNotDuplicateFavorite(userQuestionFavorite.getUserId(),
                userQuestionFavorite.getQuestionId(),
                null);

        // 4. 设置默认值
        setDefaultValues(userQuestionFavorite);

        // 5. 设置创建时间
        userQuestionFavorite.setCreateTime(DateUtils.getNowDate());

        // 6. 执行插入
        return userQuestionFavoriteMapper.insertUserQuestionFavorite(userQuestionFavorite);
    }

    /**
     * 修改用户题目收藏（支持复习功能）
     *
     * @param userQuestionFavorite 用户题目收藏（支持复习功能）
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateUserQuestionFavorite(UserQuestionFavorite userQuestionFavorite)
    {
        // 1. 验证收藏记录是否存在
        if (userQuestionFavorite.getFavoriteId() == null) {
            throw new ServiceException("收藏ID不能为空");
        }

        UserQuestionFavorite existingFavorite = selectUserQuestionFavoriteByFavoriteId(
                userQuestionFavorite.getFavoriteId()
        );
        if (existingFavorite == null) {
            throw new ServiceException("收藏记录不存在");
        }

        // 2. 如果修改了题目ID，需要验证新题目
        Long newQuestionId = userQuestionFavorite.getQuestionId();
        if (newQuestionId != null && !newQuestionId.equals(existingFavorite.getQuestionId())) {
            // 验证新题目是否存在
            validateQuestionExists(newQuestionId);

            // 验证是否已收藏新题目（排除当前记录）
            validateNotDuplicateFavorite(existingFavorite.getUserId(),
                    newQuestionId,
                    userQuestionFavorite.getFavoriteId());
        }

        // 3. 设置更新时间
        userQuestionFavorite.setUpdateTime(DateUtils.getNowDate());

        // 4. 执行更新
        return userQuestionFavoriteMapper.updateUserQuestionFavorite(userQuestionFavorite);
    }

    /**
     * 批量删除用户题目收藏（支持复习功能）
     *
     * @param favoriteIds 需要删除的用户题目收藏（支持复习功能）主键
     * @return 结果
     */
    @Override
    public int deleteUserQuestionFavoriteByFavoriteIds(Long[] favoriteIds)
    {
        return userQuestionFavoriteMapper.deleteUserQuestionFavoriteByFavoriteIds(favoriteIds);
    }

    /**
     * 删除用户题目收藏（支持复习功能）信息
     *
     * @param favoriteId 用户题目收藏（支持复习功能）主键
     * @return 结果
     */
    @Override
    public int deleteUserQuestionFavoriteByFavoriteId(Long favoriteId)
    {
        return userQuestionFavoriteMapper.deleteUserQuestionFavoriteByFavoriteId(favoriteId);
    }

    @Override
    public Long getQuestionBankId(Long questionId) {
        QuestionMain questionMain = questionMainService.selectQuestionMainById(questionId);
        Long bankId = questionMain.getBankId();
        return bankId;
    }

    /**
     * 检查用户是否已收藏题目
     */
    @Override
    public boolean checkQuestionFavoriteExists(Long userId, Long questionId) {
        UserQuestionFavorite param = new UserQuestionFavorite();
        param.setUserId(userId);
        param.setQuestionId(questionId);
        List<UserQuestionFavorite> list = userQuestionFavoriteMapper.selectUserQuestionFavoriteList(param);
        return list != null && !list.isEmpty();
    }


    /**
     * 根据用户ID和题目ID查询收藏记录
     */
    @Override
    public UserQuestionFavorite selectUserQuestionFavoriteByUserIdAndQuestionId(Long userId, Long questionId) {
        UserQuestionFavorite param = new UserQuestionFavorite();
        param.setUserId(userId);
        param.setQuestionId(questionId);
        List<UserQuestionFavorite> list = userQuestionFavoriteMapper.selectUserQuestionFavoriteList(param);
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    @Override
    public boolean checkQuestionExists(Long questionId) {
        int count = userQuestionFavoriteMapper.checkQuestionExists(questionId);
        return count > 0;  // 将 int 转换为 boolean
    }

    // ================ 私有验证方法 ================

    /**
     * 验证必填字段
     */
    private void validateRequiredFields(UserQuestionFavorite favorite) {
        if (favorite.getUserId() == null) {
            throw new ServiceException("用户ID不能为空");
        }
        if (favorite.getQuestionId() == null) {
            throw new ServiceException("题目ID不能为空");
        }
    }

    /**
     * 验证题目是否存在
     */
    private void validateQuestionExists(Long questionId) {
        int count = userQuestionFavoriteMapper.checkQuestionExists(questionId);
        if (count == 0) {
            throw new ServiceException("题目ID " + questionId + " 不存在，请检查题目ID是否正确");
        }
    }

    /**
     * 验证是否重复收藏
     */
    private void validateNotDuplicateFavorite(Long userId, Long questionId, Long excludeFavoriteId) {
        int count = userQuestionFavoriteMapper.checkFavoriteExists(userId, questionId, excludeFavoriteId);
        if (count > 0) {
            throw new ServiceException("用户已收藏该题目，不能重复收藏");
        }
    }

    /**
     * 设置默认值
     */
    private void setDefaultValues(UserQuestionFavorite favorite) {
        if (favorite.getDifficultyRating() == null) {
            favorite.setDifficultyRating(3L); // 默认3星难度
        } else if (favorite.getDifficultyRating() < 1 || favorite.getDifficultyRating() > 5) {
            throw new ServiceException("难度评级必须在1-5星之间");
        }

        if (favorite.getErrorTimes() == null) {
            favorite.setErrorTimes(0L);
        }

        if (favorite.getCorrectTimes() == null) {
            favorite.setCorrectTimes(0L);
        }

        if (favorite.getReviewCount() == null) {
            favorite.setReviewCount(0L);
        }

        if (favorite.getIsStarred() == null) {
            favorite.setIsStarred(0L); // 默认未标星
        }

        if (favorite.getFavoriteStatus() == null) {
            favorite.setFavoriteStatus(1L); // 默认正常收藏
        }
    }
}