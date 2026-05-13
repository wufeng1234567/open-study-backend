package com.openstudy.ocr.controller;

import com.openstudy.common.core.controller.BaseController;
import com.openstudy.common.core.domain.AjaxResult;
import com.openstudy.ocr.domain.OcrResult;
import com.openstudy.ocr.service.IOcrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * OCR控制器
 */
@RestController
@RequestMapping("/ocr")
public class OcrController extends BaseController {

    @Autowired
    @org.springframework.beans.factory.annotation.Qualifier("siliconflowOcrService")
    private IOcrService ocrService;



    /**
     * 上传图片并进行OCR识别
     */
    @PostMapping("/recognize")
    public AjaxResult recognize(@RequestParam("file") MultipartFile file) {
        logger.info("接收到 OCR 请求，文件名: {}, 大小: {}",
                file != null ? file.getOriginalFilename() : "null",
                file != null ? file.getSize() : 0);
        try {
            OcrResult result = ocrService.recognize(file);
            return AjaxResult.success(result);
        } catch (Exception e) {
            logger.error("OCR识别失败", e);
            return AjaxResult.error("OCR识别失败：" + e.getMessage());
        }
    }
}
