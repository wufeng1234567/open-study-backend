package com.openstudy.ocr.service;

import com.openstudy.ocr.domain.OcrResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * OCR服务接口
 */
public interface IOcrService {

    /**
     * 对上传的图片文件进行OCR识别
     *
     * @param file 图片文件
     * @return OCR识别结果
     * @throws IOException 文件读取异常
     */
    OcrResult recognize(MultipartFile file) throws Exception;
}
