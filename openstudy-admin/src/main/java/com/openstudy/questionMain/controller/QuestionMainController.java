package com.openstudy.questionMain.controller;

import com.openstudy.common.annotation.Log;
import com.openstudy.common.core.controller.BaseController;
import com.openstudy.common.core.domain.AjaxResult;
import com.openstudy.common.core.page.TableDataInfo;
import com.openstudy.common.enums.BusinessType;
import com.openstudy.common.exception.ServiceException;
import com.openstudy.common.utils.poi.ExcelUtil;
import com.openstudy.questionBank.domain.QuestionBank;
import com.openstudy.questionBank.service.IQuestionBankService;
import com.openstudy.questionMain.domain.QuestionMain;
import com.openstudy.questionMain.service.IQuestionMainService;
import com.openstudy.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 一级题目Controller
 *
 * @author ruoyi
 * @date 2025-12-06
 */
@PreAuthorize("@ss.hasRole('common')")
@RestController
@RequestMapping("/questionMain/questionMain")
public class QuestionMainController extends BaseController
{
    @Autowired
    private IQuestionMainService questionMainService;

    @Autowired
    private ISysUserService userService;  // 用户服务

    @Autowired
    private IQuestionBankService questionBankService;  // 题库服务

    /**
     * 查询一级题目列表
     */
    @GetMapping("/list")
    public TableDataInfo list(QuestionMain questionMain)
    {
        startPage();
        List<QuestionMain> list = questionMainService.selectQuestionMainList(questionMain);
        return getDataTable(list);
    }

    /**
     * 查询一级题目列表,不分页
     */
    @GetMapping("/all")
    public AjaxResult listAll(QuestionMain questionMain)
    {
        List<QuestionMain> list = questionMainService.selectQuestionMainAll(questionMain);
        return success(list);
    }

    /**
     * 导出一级题目列表
     */
    @Log(title = "一级题目", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, QuestionMain questionMain)
    {
        List<QuestionMain> list = questionMainService.selectQuestionMainList(questionMain);
        ExcelUtil<QuestionMain> util = new ExcelUtil<QuestionMain>(QuestionMain.class);
        util.exportExcel(response, list, "一级题目数据");
    }

    /**
     * 获取一级题目详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        QuestionMain questionMain = questionMainService.selectQuestionMainById(id);
        if (questionMain == null) {
            return error("题目ID " + id + " 不存在");
        }
        return success(questionMain);
    }

    /**
     * 新增一级题目
     */
    @Log(title = "一级题目", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody QuestionMain questionMain)
    {
        try {
            // 验证题库是否存在
            if (questionMain.getBankId() == null) {
                return error("题库ID不能为空");
            }

            // 调用题库服务检查题库是否存在
            QuestionBank bank = questionBankService.selectQuestionBankById(questionMain.getBankId());
            if (bank == null) {
                return error("题库ID " + questionMain.getBankId() + " 不存在，请输入有效的题库ID");
            }

            // 验证题目类型
            if (questionMain.getQuestionType() == null || questionMain.getQuestionType() < 1 || questionMain.getQuestionType() > 7) {
                return error("题目类型必须为1-7之间的数字");
            }

            // 验证题目内容
            if (questionMain.getQuestionText() == null || questionMain.getQuestionText().trim().isEmpty()) {
                return error("题目内容不能为空");
            }

            // ✅ 修改：组合题（questionType=6）不需要验证答案，其他题型需要验证
            boolean isComposite = questionMain.getQuestionType() == 6;
            if (!isComposite && (questionMain.getAnswer() == null || questionMain.getAnswer().trim().isEmpty())) {
                return error("题目答案不能为空");
            }

            return toAjax(questionMainService.insertQuestionMain(questionMain));
        } catch (ServiceException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            logger.error("新增题目失败", e);
            return error("新增失败：" + e.getMessage());
        }
    }

    /**
     * 修改一级题目
     */
    @Log(title = "一级题目", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody QuestionMain questionMain)
    {
        try {
            // 验证题目是否存在
            if (questionMain.getId() == null) {
                return error("题目ID不能为空");
            }

            QuestionMain existingQuestion = questionMainService.selectQuestionMainById(questionMain.getId());
            if (existingQuestion == null) {
                return error("题目ID " + questionMain.getId() + " 不存在，无法修改");
            }

            // 验证题库是否存在（如果题库ID有变更）
            if (questionMain.getBankId() != null && !questionMain.getBankId().equals(existingQuestion.getBankId())) {
                QuestionBank bank = questionBankService.selectQuestionBankById(questionMain.getBankId());
                if (bank == null) {
                    return error("题库ID " + questionMain.getBankId() + " 不存在，请输入有效的题库ID");
                }
            }

            // 验证题目类型
            if (questionMain.getQuestionType() != null && (questionMain.getQuestionType() < 1 || questionMain.getQuestionType() > 7)) {
                return error("题目类型必须为1-7之间的数字");
            }

            // ✅ 修改：组合题（questionType=6）不需要验证答案，其他题型需要验证
            boolean isComposite = questionMain.getQuestionType() != null && questionMain.getQuestionType() == 6;
            if (!isComposite && (questionMain.getAnswer() == null || questionMain.getAnswer().trim().isEmpty())) {
                return error("题目答案不能为空");
            }

            return toAjax(questionMainService.updateQuestionMain(questionMain));
        } catch (ServiceException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            logger.error("修改题目失败", e);
            return error("修改失败：" + e.getMessage());
        }
    }

    /**
     * 删除一级题目
     */
    @Log(title = "一级题目", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        try {
            // 验证每个题目是否存在
            for (Long id : ids) {
                QuestionMain questionMain = questionMainService.selectQuestionMainById(id);
                if (questionMain == null) {
                    return error("题目ID " + id + " 不存在，删除失败");
                }
            }

            return toAjax(questionMainService.deleteQuestionMainByIds(ids));
        } catch (Exception e) {
            logger.error("删除题目失败", e);
            return error("删除失败：" + e.getMessage());
        }
    }

    /**
     * 验证题目是否存在
     */
    @GetMapping("/checkExists/{questionId}")
    public AjaxResult checkQuestionExists(@PathVariable Long questionId) {
        try {
            // 验证题目ID格式
            if (questionId == null || questionId <= 0) {
                return error("题目ID格式错误");
            }

            boolean exists = questionMainService.checkQuestionExists(questionId);
            Map<String, Object> result = new HashMap<>();
            result.put("exists", exists);
            result.put("questionId", questionId);

            // 如果题目存在，返回详细信息
            if (exists) {
                QuestionMain questionMain = questionMainService.selectQuestionMainById(questionId);
                if (questionMain != null) {
                    result.put("questionText", questionMain.getQuestionText());
                    result.put("questionType", questionMain.getQuestionType());
                    result.put("bankId", questionMain.getBankId());
                    result.put("difficulty", questionMain.getDifficulty());
                    result.put("analysis", questionMain.getAnalysis());

                    // 获取题库名称
                    if (questionMain.getBankId() != null) {
                        QuestionBank bank = questionBankService.selectQuestionBankById(questionMain.getBankId());
                        if (bank != null) {
                            result.put("bankName", bank.getBankName());
                        }
                    }
                }
            }

            return success(result);
        } catch (Exception e) {
            logger.error("验证题目存在性失败", e);
            return error("验证失败：" + e.getMessage());
        }
    }

    /**
     * 获取题目简单信息
     */
    @GetMapping("/simpleInfo/{questionId}")
    public AjaxResult getQuestionSimpleInfo(@PathVariable Long questionId) {
        try {
            // 验证题目ID格式
            if (questionId == null || questionId <= 0) {
                return error("题目ID格式错误");
            }

            Map<String, Object> info = questionMainService.getQuestionSimpleInfo(questionId);
            if (info == null || info.isEmpty()) {
                return error("题目ID " + questionId + " 不存在");
            }

            // 补充题库名称
            Object bankIdObj = info.get("bankId");
            if (bankIdObj != null) {
                try {
                    Long bankId = Long.parseLong(bankIdObj.toString());
                    QuestionBank bank = questionBankService.selectQuestionBankById(bankId);
                    if (bank != null) {
                        info.put("bankName", bank.getBankName());
                    }
                } catch (NumberFormatException e) {
                    logger.warn("题库ID格式错误: " + bankIdObj);
                }
            }

            return success(info);
        } catch (Exception e) {
            logger.error("获取题目信息失败", e);
            return error("获取信息失败：" + e.getMessage());
        }
    }

    /**
     * 验证用户是否存在
     */
    @GetMapping("/checkUserExists/{userId}")
    public AjaxResult checkUserExists(@PathVariable Long userId) {
        try {
            // 验证用户ID格式
            if (userId == null || userId <= 0) {
                return error("用户ID格式错误");
            }

            // 调用用户服务检查用户是否存在
            com.openstudy.common.core.domain.entity.SysUser user = userService.selectUserById(userId);
            boolean exists = user != null;

            Map<String, Object> result = new HashMap<>();
            result.put("exists", exists);
            result.put("userId", userId);

            // 如果用户存在，返回用户基本信息
            if (exists && user != null) {
                result.put("userName", user.getUserName());
                result.put("nickName", user.getNickName());
                result.put("phonenumber", user.getPhonenumber());
            }

            return success(result);
        } catch (Exception e) {
            logger.error("验证用户存在性失败", e);
            return error("验证失败：" + e.getMessage());
        }
    }

    /**
     * 验证题目和用户是否存在（组合验证）
     */
    @GetMapping("/checkQuestionAndUser/{questionId}/{userId}")
    public AjaxResult checkQuestionAndUser(@PathVariable Long questionId, @PathVariable Long userId) {
        try {
            Map<String, Object> result = new HashMap<>();

            // 验证用户
            if (userId == null || userId <= 0) {
                result.put("userValid", false);
                result.put("userError", "用户ID格式错误");
            } else {
                com.openstudy.common.core.domain.entity.SysUser user = userService.selectUserById(userId);
                boolean userExists = user != null;
                result.put("userValid", userExists);
                result.put("userExists", userExists);
                if (userExists && user != null) {
                    result.put("userName", user.getUserName());
                    result.put("nickName", user.getNickName());
                } else {
                    result.put("userError", "用户ID " + userId + " 不存在");
                }
            }

            // 验证题目
            if (questionId == null || questionId <= 0) {
                result.put("questionValid", false);
                result.put("questionError", "题目ID格式错误");
            } else {
                boolean questionExists = questionMainService.checkQuestionExists(questionId);
                result.put("questionValid", questionExists);
                result.put("questionExists", questionExists);

                if (questionExists) {
                    QuestionMain questionMain = questionMainService.selectQuestionMainById(questionId);
                    if (questionMain != null) {
                        result.put("questionText", questionMain.getQuestionText());
                        result.put("questionType", questionMain.getQuestionType());
                        result.put("bankId", questionMain.getBankId());

                        // 获取题库名称
                        if (questionMain.getBankId() != null) {
                            QuestionBank bank = questionBankService.selectQuestionBankById(questionMain.getBankId());
                            if (bank != null) {
                                result.put("bankName", bank.getBankName());
                            }
                        }
                    }
                } else {
                    result.put("questionError", "题目ID " + questionId + " 不存在");
                }
            }

            // 整体验证结果
            boolean allValid = (Boolean) result.getOrDefault("userValid", false) &&
                    (Boolean) result.getOrDefault("questionValid", false);
            result.put("allValid", allValid);

            return success(result);
        } catch (Exception e) {
            logger.error("验证题目和用户失败", e);
            return error("验证失败：" + e.getMessage());
        }
    }

    /**
     * 根据条件搜索题目（用于前端选择器）
     */
    @GetMapping("/search")
    public TableDataInfo searchQuestions(QuestionMain questionMain) {
        try {
            startPage();
            List<QuestionMain> list = questionMainService.selectQuestionMainList(questionMain);
            return getDataTable(list);  // 这里需要传递list参数
        } catch (Exception e) {
            logger.error("搜索题目失败", e);
            List<QuestionMain> emptyList = new ArrayList<>();
            return getDataTable(emptyList);
        }
    }

    /**
     * 批量获取题目信息
     */
    @PostMapping("/batchInfo")
    public AjaxResult getBatchQuestionInfo(@RequestBody List<Long> questionIds) {
        try {
            if (questionIds == null || questionIds.isEmpty()) {
                return error("题目ID列表不能为空");
            }

            if (questionIds.size() > 100) {
                return error("一次最多查询100个题目");
            }

            List<Map<String, Object>> infoList = questionMainService.getQuestionListByIds(questionIds);

            // 补充题库名称
            for (Map<String, Object> info : infoList) {
                Object bankIdObj = info.get("bankId");
                if (bankIdObj != null) {
                    try {
                        Long bankId = Long.parseLong(bankIdObj.toString());
                        QuestionBank bank = questionBankService.selectQuestionBankById(bankId);
                        if (bank != null) {
                            info.put("bankName", bank.getBankName());
                        }
                    } catch (NumberFormatException e) {
                        logger.warn("题库ID格式错误: " + bankIdObj);
                    }
                }
            }

            return success(infoList);
        } catch (Exception e) {
            logger.error("批量获取题目信息失败", e);
            return error("获取信息失败：" + e.getMessage());
        }
    }
}