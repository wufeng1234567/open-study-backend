package com.openstudy.questionError.controller;

import com.openstudy.common.annotation.Log;
import com.openstudy.common.core.controller.BaseController;
import com.openstudy.common.core.domain.AjaxResult;
import com.openstudy.common.core.page.TableDataInfo;
import com.openstudy.common.enums.BusinessType;
import com.openstudy.common.utils.poi.ExcelUtil;
import com.openstudy.questionError.domain.UserQuestionError;
import com.openstudy.questionError.service.IUserQuestionErrorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 用户错题记录（支持复习与掌握跟踪）Controller
 * 
 * @author liu
 * @date 2025-12-16
 */
@RestController
@RequestMapping("/questionError/questionError")
@PreAuthorize("@ss.hasRole('common')")
public class UserQuestionErrorController extends BaseController
{
    @Autowired
    private IUserQuestionErrorService userQuestionErrorService;

    /**
     * 查询用户错题记录（支持复习与掌握跟踪）列表
     */
    // @PreAuthorize("@ss.hasPermi('questionError:questionError:list')")
    @GetMapping("/list")
    public TableDataInfo list(UserQuestionError userQuestionError)
    {
        startPage();
        List<UserQuestionError> list = userQuestionErrorService.selectUserQuestionErrorList(userQuestionError);
        return getDataTable(list);
    }
    /**
     * 查询用户错题记录（支持复习与掌握跟踪）列表 不分页
     */
    // @PreAuthorize("@ss.hasPermi('questionError:questionError:list')")
    @GetMapping("/all")
    public AjaxResult all(UserQuestionError userQuestionError)
    {

        List<UserQuestionError> list = userQuestionErrorService.selectUserQuestionErrorList(userQuestionError);
        return success(list);
    }

    /**
     * 导出用户错题记录（支持复习与掌握跟踪）列表
     */
    // @PreAuthorize("@ss.hasPermi('questionError:questionError:export')")
    @Log(title = "用户错题记录（支持复习与掌握跟踪）", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, UserQuestionError userQuestionError)
    {
        List<UserQuestionError> list = userQuestionErrorService.selectUserQuestionErrorList(userQuestionError);
        ExcelUtil<UserQuestionError> util = new ExcelUtil<UserQuestionError>(UserQuestionError.class);
        util.exportExcel(response, list, "用户错题记录（支持复习与掌握跟踪）数据");
    }

    /**
     * 获取用户错题记录（支持复习与掌握跟踪）详细信息
     */
    // @PreAuthorize("@ss.hasPermi('questionError:questionError:query')")
    @GetMapping(value = "/{errorId}")
    public AjaxResult getInfo(@PathVariable("errorId") Long errorId)
    {
        return success(userQuestionErrorService.selectUserQuestionErrorByErrorId(errorId));
    }

    /**
     * 新增用户错题记录（支持复习与掌握跟踪）
     */
    // @PreAuthorize("@ss.hasPermi('questionError:questionError:add')")
    @Log(title = "用户错题记录（支持复习与掌握跟踪）", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody UserQuestionError userQuestionError)
    {
        return toAjax(userQuestionErrorService.insertUserQuestionError(userQuestionError));
    }

    /**
     * 修改用户错题记录（支持复习与掌握跟踪）
     */
    // @PreAuthorize("@ss.hasPermi('questionError:questionError:edit')")
    @Log(title = "用户错题记录（支持复习与掌握跟踪）", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody UserQuestionError userQuestionError)
    {
        return toAjax(userQuestionErrorService.updateUserQuestionError(userQuestionError));
    }

    /**
     * 删除用户错题记录（支持复习与掌握跟踪）
     */
    // @PreAuthorize("@ss.hasPermi('questionError:questionError:remove')")
    @Log(title = "用户错题记录（支持复习与掌握跟踪）", businessType = BusinessType.DELETE)
	@DeleteMapping("/{errorIds}")
    public AjaxResult remove(@PathVariable Long[] errorIds)
    {
        return toAjax(userQuestionErrorService.deleteUserQuestionErrorByErrorIds(errorIds));
    }
    /**
     * 检查当前用户对某题的错题记录是否存在
     * 返回给前端：true-存在，false-不存在
     */
    @Log(title = "用户错题记录（支持复习与掌握跟踪）", businessType = BusinessType.OTHER)
    @GetMapping("/checkUserErrorExists/{questionId}")
    public AjaxResult checkUserErrorExists(@PathVariable Long questionId)
    {
        if (questionId == null || questionId <= 0) {
            return AjaxResult.error("题目ID不能为空");
        }

        // 获取当前登录用户ID
        Long userId = getUserId();
        if (userId == null) {
            return AjaxResult.error("用户未登录");
        }

        boolean exists = userQuestionErrorService.checkUserQuestionErrorExists(userId, questionId);

        return AjaxResult.success(exists);
    }

    /**
     * 记录错题（智能方法）
     * 返回给前端：true-成功，false-失败
     */
    @Log(title = "用户错题记录（支持复习与掌握跟踪）", businessType = BusinessType.OTHER)
    @PostMapping("/recordOrUpdateError")
    public AjaxResult recordOrUpdateError(@RequestBody UserQuestionError errorRecord)
    {
        if (errorRecord == null) {
            return AjaxResult.error("错题记录不能为空");
        }

        // 设置当前用户ID（如果未指定）
        if (errorRecord.getUserId() == null) {
            Long userId = getUserId();
            if (userId == null) {
                return AjaxResult.error("用户未登录");
            }
            errorRecord.setUserId(userId);
        }

        // 验证必要字段
        if (errorRecord.getQuestionId() == null) {
            return AjaxResult.error("题目ID不能为空");
        }
        if (errorRecord.getBankId() == null) {
            return AjaxResult.error("题库ID不能为空");
        }

        boolean success = userQuestionErrorService.recordOrUpdateQuestionError(errorRecord);

        return AjaxResult.success(success);
    }
}
