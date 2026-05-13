package com.openstudy.web.controller.system;

import com.openstudy.common.annotation.Log;
import com.openstudy.common.core.controller.BaseController;
import com.openstudy.common.core.domain.AjaxResult;
import com.openstudy.common.core.page.TableDataInfo;
import com.openstudy.common.enums.BusinessType;
import com.openstudy.common.utils.SecurityUtils;
import com.openstudy.system.domain.SysAiConfig;
import com.openstudy.system.service.ISysAiConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI模型配置Controller
 *
 * @author openstudy
 */
@Tag(name = "AI模型配置管理")
@RestController
@RequestMapping("/system/ai/config")
@RequiredArgsConstructor
public class SysAiConfigController extends BaseController {

    private final ISysAiConfigService sysAiConfigService;

    @Operation(summary = "查询用户的AI模型配置列表")
    @GetMapping("/list")
    public TableDataInfo list() {
        Long userId = getUserId();
        List<SysAiConfig> list = sysAiConfigService.selectByUserId(userId);
        return getDataTable(list);
    }

    @Operation(summary = "获取用户的有效配置（优先用户配置 > 系统默认）")
    @GetMapping("/effective")
    public AjaxResult getEffectiveConfig() {
        Long userId = getUserId();
        SysAiConfig config = sysAiConfigService.selectUserEffectiveConfig(userId);
        return success(config);
    }

    @Operation(summary = "获取当前用户的默认模型信息")
    @GetMapping("/current")
    public AjaxResult getCurrentModel() {
        Long userId = getUserId();
        SysAiConfig config = sysAiConfigService.selectUserEffectiveConfig(userId);

        if (config != null) {
            return success(config);
        }

        // 返回系统默认
        SysAiConfig defaultConfig = new SysAiConfig();
        defaultConfig.setProvider("zhipuai");
        defaultConfig.setProviderName("智谱AI");
        defaultConfig.setModel("glm-4-plus");
        defaultConfig.setIsDefault(1);
        return success(defaultConfig);
    }

    @Operation(summary = "导出AI模型配置列表")
    @PreAuthorize("@ss.hasPermi('system:aiConfig:export')")
    @Log(title = "AI模型配置", businessType = BusinessType.EXPORT)
    public AjaxResult export() {
        List<SysAiConfig> list = sysAiConfigService.selectSysAiConfigList(new SysAiConfig());
        return success(list);
    }

    @Operation(summary = "获取AI模型配置详细信息")
    @GetMapping("/{configId}")
    public AjaxResult getInfo(@PathVariable Long configId) {
        SysAiConfig config = sysAiConfigService.selectSysAiConfigById(configId);
        return success(config);
    }

    @Operation(summary = "新增AI模型配置")
    @PreAuthorize("isAuthenticated()")
    @Log(title = "AI模型配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SysAiConfig sysAiConfig) {
        Long userId = getUserId();
        sysAiConfig.setUserId(userId);
        sysAiConfig.setCreateBy(SecurityUtils.getUsername());

        // 如果是第一个配置，自动设为默认
        List<SysAiConfig> existingConfigs = sysAiConfigService.selectByUserId(userId);
        if (existingConfigs == null || existingConfigs.isEmpty()) {
            sysAiConfig.setIsDefault(1);
        }

        return toAjax(sysAiConfigService.insertSysAiConfig(sysAiConfig));
    }

    @Operation(summary = "修改AI模型配置")
    @PreAuthorize("isAuthenticated()")
    @Log(title = "AI模型配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SysAiConfig sysAiConfig) {
        SysAiConfig existing = sysAiConfigService.selectSysAiConfigById(sysAiConfig.getConfigId());
        if (existing == null) {
            return error("配置不存在");
        }

        // 验证是否是该用户的配置
        if (!getUserId().equals(existing.getUserId())) {
            return error("无权操作此配置");
        }

        sysAiConfig.setUserId(getUserId());
        sysAiConfig.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(sysAiConfigService.updateSysAiConfig(sysAiConfig));
    }

    @Operation(summary = "删除AI模型配置")
    @PreAuthorize("isAuthenticated()")
    @Log(title = "AI模型配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{configIds}")
    public AjaxResult remove(@PathVariable Long[] configIds) {
        for (Long configId : configIds) {
            SysAiConfig existing = sysAiConfigService.selectSysAiConfigById(configId);
            if (existing == null) {
                return error("配置不存在: " + configId);
            }
            if (!getUserId().equals(existing.getUserId())) {
                return error("无权操作配置: " + configId);
            }
        }
        return toAjax(sysAiConfigService.deleteSysAiConfigByIds(configIds));
    }

    @Operation(summary = "设置为默认模型")
    @PreAuthorize("isAuthenticated()")
    @Log(title = "AI模型配置", businessType = BusinessType.UPDATE)
    @PutMapping("/default/{configId}")
    public AjaxResult setDefault(@PathVariable Long configId) {
        Long userId = getUserId();
        boolean result = sysAiConfigService.setAsDefault(configId, userId);
        return result ? success() : error("设置失败");
    }

    @Operation(summary = "测试AI连接")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/test")
    public AjaxResult testConnection(@RequestBody SysAiConfig sysAiConfig) {
        String error = sysAiConfigService.testConnection(sysAiConfig);
        if (error == null) {
            return success("连接成功");
        } else {
            return error("连接失败: " + error);
        }
    }
}
