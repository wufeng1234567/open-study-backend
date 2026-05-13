package com.openstudy.document.service;

import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

/**
 * 文档转换服务接口
 */
public interface IDocumentConvertService {

    /**
     * 文档格式转换
     *
     * @param sourceFormat 源格式 (pdf, docx, txt, md)
     * @param targetFormat 目标格式 (pdf, docx, txt, md)
     * @param file 上传的文件
     * @return 转换后的文件路径
     * @throws Exception 转换过程中的异常
     */
    Path convert(String sourceFormat, String targetFormat, MultipartFile file) throws Exception;

    /**
     * 异步文档格式转换
     *
     * @param sourceFormat 源格式
     * @param targetFormat 目标格式
     * @param file 上传的文件
     * @return 转换后的文件路径
     * @throws Exception 转换过程中的异常
     */
    Path convertAsync(String sourceFormat, String targetFormat, MultipartFile file) throws Exception;
}