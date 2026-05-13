package com.openstudy.favoriteQuestion.controller;

import com.openstudy.common.annotation.Log;
import com.openstudy.common.core.controller.BaseController;
import com.openstudy.common.core.domain.AjaxResult;
import com.openstudy.common.core.page.TableDataInfo;
import com.openstudy.common.enums.BusinessType;
import com.openstudy.common.exception.ServiceException;
import com.openstudy.common.utils.poi.ExcelUtil;
import com.openstudy.favoriteQuestion.domain.UserQuestionFavorite;
import com.openstudy.favoriteQuestion.service.IUserQuestionFavoriteService;
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
 * 用户题目收藏（支持复习功能）Controller
 *
 * @author liu
 * @date 2025-12-10
 */
@RestController
@RequestMapping("/favoriteQuestion/favoriteQuestion")
@PreAuthorize("@ss.hasRole('common')")
public class UserQuestionFavoriteController extends BaseController
{
    @Autowired
    private IUserQuestionFavoriteService userQuestionFavoriteService;

    @Autowired
    private IQuestionBankService questionBankService;  // 添加题库服务注入

    @Autowired
    private ISysUserService userService;



    /**
     * 查询用户题目收藏（支持复习功能）列表
     */
    @GetMapping("/list")
    public TableDataInfo list(UserQuestionFavorite userQuestionFavorite)
    {
        startPage();
        List<UserQuestionFavorite> list = userQuestionFavoriteService.selectUserQuestionFavoriteList(userQuestionFavorite);
        return getDataTable(list);
    }

    /**
     * 查询用户题目收藏列表（不分页）
     */
    @GetMapping("/all")
    public AjaxResult all(UserQuestionFavorite userQuestionFavorite) {
        List<UserQuestionFavorite> list = userQuestionFavoriteService.selectUserQuestionFavoriteList(userQuestionFavorite);
        return success(list);
    }


    /**
     * 导出用户题目收藏（支持复习功能）列表
     */
    @Log(title = "用户题目收藏（支持复习功能）", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, UserQuestionFavorite userQuestionFavorite)
    {
        List<UserQuestionFavorite> list = userQuestionFavoriteService.selectUserQuestionFavoriteList(userQuestionFavorite);
        ExcelUtil<UserQuestionFavorite> util = new ExcelUtil<UserQuestionFavorite>(UserQuestionFavorite.class);
        util.exportExcel(response, list, "用户题目收藏（支持复习功能）数据");
    }

    /**
     * 验证题目是否存在并返回题目信息
     */
    @GetMapping("/check/{questionId}")
    public AjaxResult checkQuestionExists(@PathVariable Long questionId) {
        // 这里调用题目Service检查题目是否存在
        boolean exists = userQuestionFavoriteService.checkQuestionExists(questionId);
        Map<String, Object> result = new HashMap<>();
        result.put("exists", exists);
        result.put("questionId", questionId);

        // 如果题目存在，查询题目详细信息
        if (exists) {
            try {
                // 假设您有题目服务可以查询题目详细信息
                // 这里需要根据您的实际情况调整
                // 如果暂时没有题目详情查询，可以只返回基础信息

                // 临时方案：假设题目表是 question_main，可以通过 Mapper 直接查询
                // 这里需要注入 questionMainMapper 或相关服务

                result.put("title", "题目ID: " + questionId);  // 临时占位
                result.put("difficulty", 3);  // 默认难度

                // 这里需要根据题目ID查询题目所属的题库ID
                // 假设有一个方法可以获取题目的题库ID
                Long bankId = userQuestionFavoriteService.getQuestionBankId(questionId);
                if (bankId != null) {
                    result.put("bankId", bankId);

                    // 查询题库名称
                    QuestionBank bank = questionBankService.selectQuestionBankById(bankId);
                    if (bank != null) {
                        result.put("bankName", bank.getBankName());
                    }
                }

            } catch (Exception e) {
                logger.error("查询题目详情失败", e);
                // 即使查询详情失败，只要题目存在就返回 true
                result.put("exists", true);
                result.put("error", "查询题目详情失败，但题目存在");
            }
        }

        return AjaxResult.success(result);
    }
    /**
     * 获取用户题目收藏（支持复习功能）详细信息
     */
    @GetMapping(value = "/{favoriteId}")
    public AjaxResult getInfo(@PathVariable("favoriteId") Long favoriteId)
    {
        return success(userQuestionFavoriteService.selectUserQuestionFavoriteByFavoriteId(favoriteId));
    }
    /**
     * 新增用户题目收藏（支持复习功能）
     */
    @Log(title = "用户题目收藏（支持复习功能）", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody UserQuestionFavorite userQuestionFavorite)
    {
        try {
            // 验证用户ID是否存在
            if (userQuestionFavorite.getUserId() == null) {
                return error("用户ID不能为空");
            }

            // 调用用户服务检查用户是否存在
            com.openstudy.common.core.domain.entity.SysUser user = userService.selectUserById(userQuestionFavorite.getUserId());
            if (user == null) {
                System.out.println("用户ID " + userQuestionFavorite.getUserId() + " 不存在被执行");
                return error("用户ID " + userQuestionFavorite.getUserId() + " 不存在，请输入有效的用户ID");
            }

            // 验证题目ID是否存在
            if (userQuestionFavorite.getQuestionId() == null) {
                return error("题目ID不能为空");
            }

            // 检查题目是否存在，并获取题库ID
            // 这里假设你有一个方法可以获取题目所属的题库ID
            Long bankId = userQuestionFavoriteService.getQuestionBankId(userQuestionFavorite.getQuestionId());
            if (bankId == null) {
                return error("无法获取题目所属的题库信息");
            }

            // 设置题库ID
            userQuestionFavorite.setBankId(bankId);

            // 检查是否已收藏
            boolean isFavorited = userQuestionFavoriteService.checkQuestionFavoriteExists(
                    userQuestionFavorite.getUserId(),
                    userQuestionFavorite.getQuestionId()
            );

            if (isFavorited) {
                return error("该用户已收藏此题，请不要重复收藏");
            }

            return toAjax(userQuestionFavoriteService.insertUserQuestionFavorite(userQuestionFavorite));
        } catch (ServiceException e) {
            // 捕获业务异常，返回具体的错误信息
            return error(e.getMessage());
        } catch (Exception e) {
            // 捕获其他异常，返回通用错误信息
            logger.error("新增用户题目收藏失败", e);
            return error("新增失败：" + e.getMessage());
        }
    }

    /**
     * 修改用户题目收藏（支持复习功能）
     */
    @Log(title = "用户题目收藏（支持复习功能）", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody UserQuestionFavorite userQuestionFavorite)
    {
        try {
            // 验证用户ID是否存在
            if (userQuestionFavorite.getUserId() == null) {
                return error("用户ID不能为空");
            }

            // 调用用户服务检查用户是否存在
            com.openstudy.common.core.domain.entity.SysUser user = userService.selectUserById(userQuestionFavorite.getUserId());
            if (user == null) {
                return error("用户ID " + userQuestionFavorite.getUserId() + " 不存在，请输入有效的用户ID");
            }

            return toAjax(userQuestionFavoriteService.updateUserQuestionFavorite(userQuestionFavorite));
        } catch (ServiceException e) {
            // 捕获业务异常，返回具体的错误信息
            return error(e.getMessage());
        } catch (Exception e) {
            // 捕获其他异常，返回通用错误信息
            logger.error("修改用户题目收藏失败", e);
            return error("修改失败：" + e.getMessage());
        }
    }

    /**
     * 删除用户题目收藏（支持复习功能）
     */
    @Log(title = "用户题目收藏（支持复习功能）", businessType = BusinessType.DELETE)
    @DeleteMapping("/{favoriteIds}")
    public AjaxResult remove(@PathVariable Long[] favoriteIds)
    {
        try {
            return toAjax(userQuestionFavoriteService.deleteUserQuestionFavoriteByFavoriteIds(favoriteIds));
        } catch (Exception e) {
            // 捕获异常，返回错误信息
            logger.error("删除用户题目收藏失败", e);
            return error("删除失败：" + e.getMessage());
        }
    }


    /**
     * 检查用户是否已收藏题目
     */
    @GetMapping("/checkFavorite/{userId}/{questionId}")
    public AjaxResult checkQuestionFavoriteExists(@PathVariable Long userId, @PathVariable Long questionId) {
        try {
            // 检查用户是否存在
            com.openstudy.common.core.domain.entity.SysUser user = userService.selectUserById(userId);
            if (user == null) {
                return error("用户ID " + userId + " 不存在");
            }

            // 检查是否已收藏
            boolean isFavorited = userQuestionFavoriteService.checkQuestionFavoriteExists(userId, questionId);

            Map<String, Object> result = new HashMap<>();
            result.put("userId", userId);
            result.put("questionId", questionId);
            result.put("isFavorited", isFavorited);

            // 如果已收藏，获取收藏详情
            if (isFavorited) {
                UserQuestionFavorite favorite = userQuestionFavoriteService.selectUserQuestionFavoriteByUserIdAndQuestionId(userId, questionId);
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
            logger.error("检查题目收藏状态失败", e);
            return error("检查失败：" + e.getMessage());
        }
    }

    /**
     * 根据用户ID和题目ID删除收藏
     */
    @DeleteMapping("/deleteByUserAndQuestion/{userId}/{questionId}")
    public AjaxResult deleteByUserAndQuestion(@PathVariable Long userId, @PathVariable Long questionId) {
        try {
            // 检查用户是否存在
            com.openstudy.common.core.domain.entity.SysUser user = userService.selectUserById(userId);
            if (user == null) {
                return error("用户ID " + userId + " 不存在");
            }

            // 检查是否已收藏
            boolean isFavorited = userQuestionFavoriteService.checkQuestionFavoriteExists(userId, questionId);
            if (!isFavorited) {
                return error("该用户未收藏此题");
            }

            // 获取收藏记录ID
            UserQuestionFavorite favorite = userQuestionFavoriteService.selectUserQuestionFavoriteByUserIdAndQuestionId(userId, questionId);
            if (favorite == null) {
                return error("收藏记录不存在");
            }

            // 删除收藏
            return toAjax(userQuestionFavoriteService.deleteUserQuestionFavoriteByFavoriteId(favorite.getFavoriteId()));
        } catch (Exception e) {
            logger.error("删除收藏失败", e);
            return error("删除失败：" + e.getMessage());
        }
    }
}