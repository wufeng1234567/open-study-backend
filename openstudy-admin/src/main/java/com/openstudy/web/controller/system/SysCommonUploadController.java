package com.openstudy.web.controller.system;

import com.openstudy.common.annotation.Anonymous;
import com.openstudy.common.annotation.Log;
import com.openstudy.common.config.RuoYiConfig;
import com.openstudy.common.core.controller.BaseController;
import com.openstudy.common.core.domain.AjaxResult;
import com.openstudy.common.enums.BusinessType;
import com.openstudy.common.utils.file.FileUploadUtils;
import com.openstudy.common.utils.file.MimeTypeUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 通用文件上传（用于 Markdown、富文本等）
 * 
 * @author ruoyi
 */
@Anonymous
@RestController
@RequestMapping("/system/common")
public class SysCommonUploadController extends BaseController
{
    /**
     * 通用图片上传
     */
    @Log(title = "通用图片", businessType = BusinessType.INSERT)
    @PostMapping("/upload")
    public AjaxResult upload(@RequestParam("file") MultipartFile file) throws Exception
    {
        if (file.isEmpty())
        {
            return error("上传文件不能为空");
        }

        // 限制只允许图片类型
        String[] allowedExtensions = MimeTypeUtils.IMAGE_EXTENSION;
        String filePath = FileUploadUtils.upload(RuoYiConfig.getProfile() + "/upload", file, allowedExtensions, false);

        AjaxResult ajax = AjaxResult.success();
        ajax.put("url", filePath); // 返回相对路径，如 /profile/upload/2025/12/03/xxx.png
        return ajax;
    }
}