package com.openstudy.favoriteBank.controller;

import com.openstudy.common.annotation.Log;
import com.openstudy.common.core.controller.BaseController;
import com.openstudy.common.core.domain.AjaxResult;
import com.openstudy.common.core.page.TableDataInfo;
import com.openstudy.common.enums.BusinessType;
import com.openstudy.common.exception.ServiceException;
import com.openstudy.common.utils.poi.ExcelUtil;
import com.openstudy.favoriteBank.domain.UserBankFavorite;
import com.openstudy.favoriteBank.service.IUserBankFavoriteService;
import com.openstudy.questionBank.domain.QuestionBank;
import com.openstudy.questionBank.service.IQuestionBankService;
import com.openstudy.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户题库收藏Controller
 *
 * @author liu
 * @date 2025-12-09
 */
@RestController
@RequestMapping("/favoriteBank/favoriteBank")
@PreAuthorize("@ss.hasRole('common')")
public class UserBankFavoriteController extends BaseController
{
    @Autowired
    private IUserBankFavoriteService userBankFavoriteService;

    @Autowired
    private IQuestionBankService questionBankService;  // 添加题库服务注入

    @Autowired
    private ISysUserService userService;  // 添加用户服务注入

    /**
     * 查询用户题库收藏列表
     */
    @GetMapping("/list")
    public TableDataInfo list(UserBankFavorite userBankFavorite)
    {
        startPage();
        List<UserBankFavorite> list = userBankFavoriteService.selectUserBankFavoriteList(userBankFavorite);
        return getDataTable(list);
    }
    /**
     * 查询用户题库收藏列表
     */
    @GetMapping("/all")
    public AjaxResult all(UserBankFavorite userBankFavorite)
    {
        List<UserBankFavorite> list = userBankFavoriteService.selectUserBankFavoriteList(userBankFavorite);
        return success(list);
    }

    /**
     * 导出用户题库收藏列表
     */
    @Log(title = "用户题库收藏", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, UserBankFavorite userBankFavorite)
    {
        List<UserBankFavorite> list = userBankFavoriteService.selectUserBankFavoriteList(userBankFavorite);
        ExcelUtil<UserBankFavorite> util = new ExcelUtil<UserBankFavorite>(UserBankFavorite.class);
        util.exportExcel(response, list, "用户题库收藏数据");
    }

    /**
     * 验证题库是否存在并返回题库信息
     */
    @GetMapping("/check/{bankId}")
    public AjaxResult checkBankExists(@PathVariable Long bankId) {
        // 调用题库Service检查题库是否存在
        boolean exists = userBankFavoriteService.checkBankExists(bankId);
        Map<String, Object> result = new HashMap<>();
        result.put("exists", exists);
        result.put("bankId", bankId);

        // 如果题库存在，查询题库详细信息
        if (exists) {
            try {
                // 查询题库详细信息
                QuestionBank bank = questionBankService.selectQuestionBankById(bankId);
                if (bank != null) {
                    result.put("bankName", bank.getBankName());
                    result.put("description", bank.getDescription());
                    // 可以根据需要添加更多题库信息
                }
            } catch (Exception e) {
                logger.error("查询题库详情失败", e);
                // 即使查询详情失败，只要题库存在就返回 true
                result.put("exists", true);
                result.put("error", "查询题库详情失败，但题库存在");
            }
        }

        return AjaxResult.success(result);
    }

    /**
     * 检查用户是否已收藏题库
     */
    @GetMapping("/checkFavorite/{userId}/{bankId}")
    public AjaxResult checkBankFavoriteExists(@PathVariable Long userId, @PathVariable Long bankId) {
        try {
            // 检查用户是否存在
            com.openstudy.common.core.domain.entity.SysUser user = userService.selectUserById(userId);
            if (user == null) {
                return error("用户ID " + userId + " 不存在");
            }

            // 检查题库是否存在
            QuestionBank bank = questionBankService.selectQuestionBankById(bankId);
            if (bank == null) {
                return error("题库ID " + bankId + " 不存在");
            }

            // 检查是否已收藏
            boolean isFavorited = userBankFavoriteService.checkBankFavoriteExists(userId, bankId);

            Map<String, Object> result = new HashMap<>();
            result.put("userId", userId);
            result.put("bankId", bankId);
            result.put("bankName", bank.getBankName());
            result.put("isFavorited", isFavorited);

            // 如果已收藏，获取收藏详情
            if (isFavorited) {
                UserBankFavorite favorite = userBankFavoriteService.selectUserBankFavoriteByUserIdAndBankId(userId, bankId);
                if (favorite != null) {
                    result.put("favoriteId", favorite.getFavoriteId());
                    result.put("notes", favorite.getNotes());
                    result.put("isStarred", favorite.getIsStarred());
                    result.put("favoriteStatus", favorite.getFavoriteStatus());
                    result.put("createTime", favorite.getCreateTime());
                }
            }

            return AjaxResult.success(result);

        } catch (Exception e) {
            logger.error("检查题库收藏状态失败", e);
            return error("检查失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户题库收藏详细信息
     */
    @GetMapping(value = "/{favoriteId}")
    public AjaxResult getInfo(@PathVariable("favoriteId") Long favoriteId)
    {
        return success(userBankFavoriteService.selectUserBankFavoriteByFavoriteId(favoriteId));
    }

    /**
     * 新增用户题库收藏
     */
    @Log(title = "用户题库收藏", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody UserBankFavorite userBankFavorite)
    {
        try {
            // 验证用户ID是否存在
            if (userBankFavorite.getUserId() == null) {
                return error("用户ID不能为空");
            }

            // 调用用户服务检查用户是否存在
            com.openstudy.common.core.domain.entity.SysUser user = userService.selectUserById(userBankFavorite.getUserId());
            if (user == null) {
                logger.info("用户ID " + userBankFavorite.getUserId() + " 不存在");
                return error("用户ID " + userBankFavorite.getUserId() + " 不存在，请输入有效的用户ID");
            }

            // 验证题库ID是否存在
            if (userBankFavorite.getBankId() == null) {
                return error("题库ID不能为空");
            }

            // 调用题库服务检查题库是否存在
            QuestionBank bank = questionBankService.selectQuestionBankById(userBankFavorite.getBankId());
            if (bank == null) {
                logger.info("题库ID " + userBankFavorite.getBankId() + " 不存在");
                return error("题库ID " + userBankFavorite.getBankId() + " 不存在，请输入有效的题库ID");
            }

            // 检查是否已收藏
            boolean isFavorited = userBankFavoriteService.checkBankFavoriteExists(
                    userBankFavorite.getUserId(),
                    userBankFavorite.getBankId()
            );

            if (isFavorited) {
                return error("该用户已收藏此题库，请不要重复收藏");
            }

            return toAjax(userBankFavoriteService.insertUserBankFavorite(userBankFavorite));
        } catch (ServiceException e) {
            // 捕获业务异常，返回具体的错误信息
            return error(e.getMessage());
        } catch (Exception e) {
            // 捕获其他异常，返回通用错误信息
            logger.error("新增用户题库收藏失败", e);
            return error("新增失败：" + e.getMessage());
        }
    }

    /**
     * 修改用户题库收藏
     */
    @Log(title = "用户题库收藏", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody UserBankFavorite userBankFavorite)
    {
        try {
            // 验证用户ID是否存在
            if (userBankFavorite.getUserId() == null) {
                return error("用户ID不能为空");
            }

            // 调用用户服务检查用户是否存在
            com.openstudy.common.core.domain.entity.SysUser user = userService.selectUserById(userBankFavorite.getUserId());
            if (user == null) {
                return error("用户ID " + userBankFavorite.getUserId() + " 不存在，请输入有效的用户ID");
            }

            // 验证题库ID是否存在
            if (userBankFavorite.getBankId() == null) {
                return error("题库ID不能为空");
            }

            // 调用题库服务检查题库是否存在
            QuestionBank bank = questionBankService.selectQuestionBankById(userBankFavorite.getBankId());
            if (bank == null) {
                return error("题库ID " + userBankFavorite.getBankId() + " 不存在，请输入有效的题库ID");
            }

            return toAjax(userBankFavoriteService.updateUserBankFavorite(userBankFavorite));
        } catch (ServiceException e) {
            // 捕获业务异常，返回具体的错误信息
            return error(e.getMessage());
        } catch (Exception e) {
            // 捕获其他异常，返回通用错误信息
            logger.error("修改用户题库收藏失败", e);
            return error("修改失败：" + e.getMessage());
        }
    }

    /**
     * 删除用户题库收藏
     */
    @Log(title = "用户题库收藏", businessType = BusinessType.DELETE)
    @DeleteMapping("/{favoriteIds}")
    public AjaxResult remove(@PathVariable Long[] favoriteIds)
    {
        try {
            return toAjax(userBankFavoriteService.deleteUserBankFavoriteByFavoriteIds(favoriteIds));
        } catch (Exception e) {
            // 捕获异常，返回错误信息
            logger.error("删除用户题库收藏失败", e);
            return error("删除失败：" + e.getMessage());
        }
    }

    /**
     * 根据用户ID和题库ID删除收藏
     */
    @DeleteMapping("/deleteByUserAndBank/{userId}/{bankId}")
    public AjaxResult deleteByUserAndBank(@PathVariable Long userId, @PathVariable Long bankId) {
        try {
            // 检查用户是否存在
            com.openstudy.common.core.domain.entity.SysUser user = userService.selectUserById(userId);
            if (user == null) {
                return error("用户ID " + userId + " 不存在");
            }

            // 检查题库是否存在
            QuestionBank bank = questionBankService.selectQuestionBankById(bankId);
            if (bank == null) {
                return error("题库ID " + bankId + " 不存在");
            }

            // 检查是否已收藏
            boolean isFavorited = userBankFavoriteService.checkBankFavoriteExists(userId, bankId);
            if (!isFavorited) {
                return error("该用户未收藏此题库");
            }

            // 获取收藏记录ID
            UserBankFavorite favorite = userBankFavoriteService.selectUserBankFavoriteByUserIdAndBankId(userId, bankId);
            if (favorite == null) {
                return error("收藏记录不存在");
            }

            // 删除收藏
            return toAjax(userBankFavoriteService.deleteUserBankFavoriteByFavoriteId(favorite.getFavoriteId()));
        } catch (Exception e) {
            logger.error("删除收藏失败", e);
            return error("删除失败：" + e.getMessage());
        }
    }
}