package com.openstudy.favoriteBank.service.impl;

import com.openstudy.common.exception.ServiceException;
import com.openstudy.common.utils.DateUtils;
import com.openstudy.favoriteBank.domain.UserBankFavorite;
import com.openstudy.favoriteBank.mapper.UserBankFavoriteMapper;
import com.openstudy.favoriteBank.service.IUserBankFavoriteService;
import com.openstudy.questionBank.domain.QuestionBank;
import com.openstudy.questionBank.service.IQuestionBankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户题库收藏Service业务层处理
 *
 * @author liu
 * @date 2025-12-09
 */
@Service
public class UserBankFavoriteServiceImpl implements IUserBankFavoriteService
{
    @Autowired
    private UserBankFavoriteMapper userBankFavoriteMapper;

    @Autowired
    private IQuestionBankService questionBankService;

    /**
     * 查询用户题库收藏
     *
     * @param favoriteId 用户题库收藏主键
     * @return 用户题库收藏
     */
    @Override
    public UserBankFavorite selectUserBankFavoriteByFavoriteId(Long favoriteId)
    {
        return userBankFavoriteMapper.selectUserBankFavoriteByFavoriteId(favoriteId);
    }

    /**
     * 查询用户题库收藏列表
     *
     * @param userBankFavorite 用户题库收藏
     * @return 用户题库收藏
     */
    @Override
    public List<UserBankFavorite> selectUserBankFavoriteList(UserBankFavorite userBankFavorite)
    {
        return userBankFavoriteMapper.selectUserBankFavoriteList(userBankFavorite);
    }

    /**
     * 新增用户题库收藏
     *
     * @param userBankFavorite 用户题库收藏
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertUserBankFavorite(UserBankFavorite userBankFavorite)
    {
        // 1. 基本参数验证
        validateRequiredFields(userBankFavorite);

        // 2. 验证题库是否存在
        validateBankExists(userBankFavorite.getBankId());

        // 3. 验证是否已收藏该题库
        validateNotDuplicateFavorite(userBankFavorite.getUserId(),
                userBankFavorite.getBankId(),
                null);

        // 4. 设置默认值
        setDefaultValues(userBankFavorite);

        // 5. 设置创建时间
        userBankFavorite.setCreateTime(DateUtils.getNowDate());

        // 6. 执行插入
        return userBankFavoriteMapper.insertUserBankFavorite(userBankFavorite);
    }

    /**
     * 修改用户题库收藏
     *
     * @param userBankFavorite 用户题库收藏
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateUserBankFavorite(UserBankFavorite userBankFavorite)
    {
        // 1. 验证收藏记录是否存在
        if (userBankFavorite.getFavoriteId() == null) {
            throw new ServiceException("收藏ID不能为空");
        }

        UserBankFavorite existingFavorite = selectUserBankFavoriteByFavoriteId(
                userBankFavorite.getFavoriteId()
        );
        if (existingFavorite == null) {
            throw new ServiceException("收藏记录不存在");
        }

        // 2. 如果修改了题库ID，需要验证新题库
        Long newBankId = userBankFavorite.getBankId();
        if (newBankId != null && !newBankId.equals(existingFavorite.getBankId())) {
            // 验证新题库是否存在
            validateBankExists(newBankId);

            // 验证是否已收藏新题库（排除当前记录）
            validateNotDuplicateFavorite(existingFavorite.getUserId(),
                    newBankId,
                    userBankFavorite.getFavoriteId());
        }

        // 3. 设置更新时间
        userBankFavorite.setUpdateTime(DateUtils.getNowDate());

        // 4. 执行更新
        return userBankFavoriteMapper.updateUserBankFavorite(userBankFavorite);
    }

    /**
     * 批量删除用户题库收藏
     *
     * @param favoriteIds 需要删除的用户题库收藏主键
     * @return 结果
     */
    @Override
    public int deleteUserBankFavoriteByFavoriteIds(Long[] favoriteIds)
    {
        return userBankFavoriteMapper.deleteUserBankFavoriteByFavoriteIds(favoriteIds);
    }

    /**
     * 删除用户题库收藏信息
     *
     * @param favoriteId 用户题库收藏主键
     * @return 结果
     */
    @Override
    public int deleteUserBankFavoriteByFavoriteId(Long favoriteId)
    {
        return userBankFavoriteMapper.deleteUserBankFavoriteByFavoriteId(favoriteId);
    }

    /**
     * 检查题库是否存在
     */
    @Override
    public boolean checkBankExists(Long bankId) {
        QuestionBank bank = questionBankService.selectQuestionBankById(bankId);
        return bank != null;
    }

    /**
     * 检查用户是否已收藏指定题库
     */
    @Override
    public boolean checkBankFavoriteExists(Long userId, Long bankId) {
        UserBankFavorite param = new UserBankFavorite();
        param.setUserId(userId);
        param.setBankId(bankId);
        List<UserBankFavorite> list = userBankFavoriteMapper.selectUserBankFavoriteList(param);
        return list != null && !list.isEmpty();
    }

    /**
     * 根据用户ID和题库ID查询收藏记录
     */
    @Override
    public UserBankFavorite selectUserBankFavoriteByUserIdAndBankId(Long userId, Long bankId) {
        UserBankFavorite param = new UserBankFavorite();
        param.setUserId(userId);
        param.setBankId(bankId);
        List<UserBankFavorite> list = userBankFavoriteMapper.selectUserBankFavoriteList(param);
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    /**
     * 根据用户ID和题库ID删除收藏
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteUserBankFavoriteByUserIdAndBankId(Long userId, Long bankId) {
        // 验证参数
        if (userId == null || bankId == null) {
            throw new ServiceException("用户ID和题库ID不能为空");
        }

        // 查询收藏记录
        UserBankFavorite favorite = selectUserBankFavoriteByUserIdAndBankId(userId, bankId);
        if (favorite == null) {
            throw new ServiceException("收藏记录不存在");
        }

        // 删除收藏
        return deleteUserBankFavoriteByFavoriteId(favorite.getFavoriteId());
    }

    // ================ 私有验证方法 ================

    /**
     * 验证必填字段
     */
    private void validateRequiredFields(UserBankFavorite favorite) {
        if (favorite.getUserId() == null) {
            throw new ServiceException("用户ID不能为空");
        }
        if (favorite.getBankId() == null) {
            throw new ServiceException("题库ID不能为空");
        }
    }

    /**
     * 验证题库是否存在
     */
    private void validateBankExists(Long bankId) {
        QuestionBank bank = questionBankService.selectQuestionBankById(bankId);
        if (bank == null) {
            throw new ServiceException("题库ID " + bankId + " 不存在，请检查题库ID是否正确");
        }
    }

    /**
     * 验证是否重复收藏
     */
    private void validateNotDuplicateFavorite(Long userId, Long bankId, Long excludeFavoriteId) {
        int count = userBankFavoriteMapper.checkFavoriteExists(userId, bankId, excludeFavoriteId);
        if (count > 0) {
            throw new ServiceException("用户已收藏该题库，不能重复收藏");
        }
    }

    /**
     * 设置默认值
     */
    private void setDefaultValues(UserBankFavorite favorite) {
        if (favorite.getSortOrder() == null) {
            favorite.setSortOrder(0L);
        }

        if (favorite.getStudyCount() == null) {
            favorite.setStudyCount(0L);
        }

        if (favorite.getIsStarred() == null) {
            favorite.setIsStarred(0L); // 默认未标星
        }

        if (favorite.getFavoriteStatus() == null) {
            favorite.setFavoriteStatus(1L); // 默认有效
        }
    }
}