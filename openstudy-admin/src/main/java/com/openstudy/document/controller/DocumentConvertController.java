package com.openstudy.document.controller;

import com.openstudy.common.core.controller.BaseController;
import com.openstudy.common.core.domain.AjaxResult;
import com.openstudy.document.service.IDocumentConvertService;
import com.openstudy.document.util.DocumentConverterUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 文档转换控制器
 * 支持 PDF、Word、TXT、MD 四种格式相互转换
 */
@Slf4j
@RestController
@RequestMapping("/document")
public class DocumentConvertController extends BaseController {

    @Autowired
    private IDocumentConvertService documentConvertService;

    /**
     * 文档格式转换
     * @param sourceFormat 源格式 (pdf, docx, txt, md)
     * @param targetFormat 目标格式 (pdf, docx, txt, md)
     * @param file 上传的文件
     * @return 转换后的文件
     */
    @PostMapping("/convert")
    public ResponseEntity<byte[]> convert(
            @RequestParam("sourceFormat") String sourceFormat,
            @RequestParam("targetFormat") String targetFormat,
            @RequestParam("file") MultipartFile file) {
        log.info("接收到文档转换请求，源格式: {}, 目标格式: {}, 文件名: {}, 大小: {}",
                sourceFormat, targetFormat,
                file != null ? file.getOriginalFilename() : "null",
                file != null ? file.getSize() : 0);

        Path resultFile = null;
        try {
            // 执行转换
            resultFile = documentConvertService.convert(sourceFormat, targetFormat, file);

            // 读取转换后的文件
            byte[] fileContent = Files.readAllBytes(resultFile);

            // 确定 MIME 类型
            String contentType = getContentType(targetFormat);
            String fileName = DocumentConverterUtil.getNameWithoutExtension(
                    file.getOriginalFilename()) + "." + targetFormat.toLowerCase();

            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentDispositionFormData("attachment", fileName);

            log.info("文档转换成功，返回文件: {}", fileName);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(fileContent);

        } catch (Exception e) {
            log.error("文档转换失败", e);
            return ResponseEntity.internalServerError().build();
        } finally {
            // 删除临时文件
            if (resultFile != null) {
                DocumentConverterUtil.deleteFile(resultFile.getParent());
            }
        }
    }

    /**
     * 获取支持转换的格式列表
     */
    @GetMapping("/formats")
    public AjaxResult getSupportedFormats() {
        String[] formats = {"pdf", "docx", "txt", "md"};
        return AjaxResult.success(formats);
    }

    /**
     * 检查转换是否支持
     */
    @GetMapping("/check")
    public AjaxResult checkConversion(@RequestParam String sourceFormat, @RequestParam String targetFormat) {
        boolean canConvert = DocumentConverterUtil.canConvert(sourceFormat, targetFormat);
        if (canConvert) {
            return AjaxResult.success("支持该转换");
        } else {
            return AjaxResult.error("不支持从 " + sourceFormat + " 转换为 " + targetFormat);
        }
    }

    /**
     * 根据文件格式获取 MIME 类型
     */
    private String getContentType(String format) {
        switch (format.toLowerCase()) {
            case "pdf":
                return "application/pdf";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "txt":
                return "text/plain";
            case "md":
                return "text/markdown";
            default:
                return "application/octet-stream";
        }
    }
}