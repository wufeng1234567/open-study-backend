package com.openstudy.questionMarked.controller;

import com.openstudy.common.annotation.Log;
import com.openstudy.common.core.controller.BaseController;
import com.openstudy.common.core.domain.AjaxResult;
import com.openstudy.common.core.domain.entity.SysUser;
import com.openstudy.common.core.page.TableDataInfo;
import com.openstudy.common.enums.BusinessType;
import com.openstudy.common.utils.poi.ExcelUtil;
import com.openstudy.questionMarked.domain.UserQuestionMarked;
import com.openstudy.questionMarked.service.IUserQuestionMarkedService;
import com.openstudy.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户斩题（重点攻克题目）Controller
 * 
 * @author ruoyi
 * @date 2025-12-17
 */
@RestController
@RequestMapping("/questionMarked/questionMarked")
@PreAuthorize("@ss.hasRole('common')")
public class UserQuestionMarkedController extends BaseController
{
    @Autowired
    private IUserQuestionMarkedService userQuestionMarkedService;

    @Autowired
    private ISysUserService userService;

    /**
     * 查询用户斩题（重点攻克题目）列表
     */
    @GetMapping("/list")
    public TableDataInfo list(UserQuestionMarked userQuestionMarked)
    {
        startPage();
        List<UserQuestionMarked> list = userQuestionMarkedService.selectUserQuestionMarkedList(userQuestionMarked);
        return getDataTable(list);
    }

    /**
     * 查询用户斩题（重点攻克题目）列表 不分页
     */
    @GetMapping("/all")
    public AjaxResult all(UserQuestionMarked userQuestionMarked)
    {
        List<UserQuestionMarked> list = userQuestionMarkedService.selectUserQuestionMarkedList(userQuestionMarked);
        return success(list);
    }



    /**
     * 导出用户斩题（重点攻克题目）列表
     */
    // @PreAuthorize("@ss.hasPermi('questionMarked:questionMarked:export')")
    @Log(title = "用户斩题（重点攻克题目）", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, UserQuestionMarked userQuestionMarked)
    {
        List<UserQuestionMarked> list = userQuestionMarkedService.selectUserQuestionMarkedList(userQuestionMarked);
        ExcelUtil<UserQuestionMarked> util = new ExcelUtil<UserQuestionMarked>(UserQuestionMarked.class);
        util.exportExcel(response, list, "用户斩题（重点攻克题目）数据");
    }

    /**
     * 获取用户斩题（重点攻克题目）详细信息
     */
    // @PreAuthorize("@ss.hasPermi('questionMarked:questionMarked:query')")
    @GetMapping(value = "/{markedId}")
    public AjaxResult getInfo(@PathVariable("markedId") Long markedId)
    {
        return success(userQuestionMarkedService.selectUserQuestionMarkedByMarkedId(markedId));
    }

    /**
     * 新增用户斩题（重点攻克题目）
     */
    // @PreAuthorize("@ss.hasPermi('questionMarked:questionMarked:add')")
    @Log(title = "用户斩题（重点攻克题目）", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody UserQuestionMarked userQuestionMarked)
    {
        return toAjax(userQuestionMarkedService.insertUserQuestionMarked(userQuestionMarked));
    }

    /**
     * 修改用户斩题（重点攻克题目）
     */
    // @PreAuthorize("@ss.hasPermi('questionMarked:questionMarked:edit')")
    @Log(title = "用户斩题（重点攻克题目）", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody UserQuestionMarked userQuestionMarked)
    {
        return toAjax(userQuestionMarkedService.updateUserQuestionMarked(userQuestionMarked));
    }

    /**
     * 删除用户斩题（重点攻克题目）
     */
    // @PreAuthorize("@ss.hasPermi('questionMarked:questionMarked:remove')")
    @Log(title = "用户斩题（重点攻克题目）", businessType = BusinessType.DELETE)
	@DeleteMapping("/{markedIds}")
    public AjaxResult remove(@PathVariable Long[] markedIds)
    {
        return toAjax(userQuestionMarkedService.deleteUserQuestionMarkedByMarkedIds(markedIds));
    }

    /**
     * 检查用户是否已斩题
     */
    // @PreAuthorize("@ss.hasPermi('questionMarked:questionMarked:query')")
    @GetMapping("/check")
    public AjaxResult checkMarkedExists(
            @RequestParam(required = true) Long userId,
            @RequestParam(required = true) Long questionId) {

        try {
            // 验证用户是否存在
            SysUser user = userService.selectUserById(userId);
            if (user == null) {
                return error("用户ID " + userId + " 不存在");
            }

            // 检查是否已斩题
            boolean exists = userQuestionMarkedService.checkQuestionMarkedExists(userId, questionId);

            Map<String, Object> result = new HashMap<>();
            result.put("exists", exists);
            result.put("userId", userId);
            result.put("questionId", questionId);

            // 如果已斩题，获取斩题详情
            if (exists) {
                UserQuestionMarked marked = userQuestionMarkedService.selectUserQuestionMarkedByUserIdAndQuestionId(userId, questionId);
                if (marked != null) {
                    result.put("markedId", marked.getMarkedId());
                    result.put("markedType", marked.getMarkedType());
                    result.put("difficultyLevel", marked.getDifficultyLevel());
                    result.put("notes", marked.getNotes());
                    result.put("createTime", marked.getCreateTime());
                    result.put("isMastered", marked.getIsMastered());
                }
            }

            return success(result);
        } catch (Exception e) {
            logger.error("检查斩题状态失败", e);
            return error("检查失败：" + e.getMessage());
        }
    }

    /**
     * 根据用户ID和题目ID删除斩题记录
     */
    // @PreAuthorize("@ss.hasPermi('questionMarked:questionMarked:remove')")
    @Log(title = "用户斩题", businessType = BusinessType.DELETE)
    @DeleteMapping("/deleteByUserAndQuestion/{userId}/{questionId}")
    public AjaxResult deleteByUserAndQuestion(@PathVariable Long userId, @PathVariable Long questionId) {
        try {
            // 检查用户是否存在
            SysUser user = userService.selectUserById(userId);
            if (user == null) {
                return error("用户ID " + userId + " 不存在");
            }

            // 检查是否已斩题
            boolean exists = userQuestionMarkedService.checkQuestionMarkedExists(userId, questionId);
            if (!exists) {
                return error("该用户未斩此题");
            }

            // 获取斩题记录ID
            UserQuestionMarked marked = userQuestionMarkedService.selectUserQuestionMarkedByUserIdAndQuestionId(userId, questionId);
            if (marked == null) {
                return error("斩题记录不存在");
            }

            // 删除斩题
            return toAjax(userQuestionMarkedService.deleteUserQuestionMarkedByMarkedId(marked.getMarkedId()));
        } catch (Exception e) {
            logger.error("删除斩题失败", e);
            return error("删除失败：" + e.getMessage());
        }
    }

}
