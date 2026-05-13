package com.openstudy.document.service.impl;

import com.openstudy.document.service.IDocumentConvertService;
import com.openstudy.document.util.DocumentConverterUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * 文档转换服务实现类
 */
@Slf4j
@Service
public class DocumentConvertServiceImpl implements IDocumentConvertService {

    @Override
    public Path convert(String sourceFormat, String targetFormat, MultipartFile file) throws Exception {
        // 验证文件大小
        DocumentConverterUtil.validateFileSize(file.getSize());

        // 验证格式支持
        if (!DocumentConverterUtil.isSupportedFormat(sourceFormat)) {
            throw new IllegalArgumentException("不支持的源文件格式: " + sourceFormat);
        }
        if (!DocumentConverterUtil.isSupportedFormat(targetFormat)) {
            throw new IllegalArgumentException("不支持的目标文件格式: " + targetFormat);
        }
        if (!DocumentConverterUtil.canConvert(sourceFormat, targetFormat)) {
            throw new IllegalArgumentException("不支持从 " + sourceFormat + " 转换为 " + targetFormat);
        }

        // 创建临时目录
        Path tempDir = DocumentConverterUtil.createTempDirectory("document-convert-");
        Path sourceFile = null;
        Path targetFile = null;

        try {
            // 保存源文件
            String originalFilename = file.getOriginalFilename();
            String sourceFileName = originalFilename != null ? originalFilename : "source." + sourceFormat;
            sourceFile = DocumentConverterUtil.copyToFile(file.getInputStream(), tempDir, sourceFileName);

            log.info("开始文档转换: {} -> {}, 文件: {}", sourceFormat, targetFormat, sourceFileName);

            // 执行转换
            targetFile = executeConversion(sourceFormat, targetFormat, sourceFile, tempDir);

            log.info("文档转换完成: {} -> {}, 输出文件: {}", sourceFormat, targetFormat, targetFile.getFileName());

            return targetFile;
        } catch (Exception e) {
            log.error("文档转换失败: {} -> {}", sourceFormat, targetFormat, e);
            throw e;
        } finally {
            // 清理源文件，保留目标文件由调用方处理
            if (sourceFile != null) {
                DocumentConverterUtil.deleteFile(sourceFile);
            }
        }
    }

    @Override
    @Async
    public Path convertAsync(String sourceFormat, String targetFormat, MultipartFile file) throws Exception {
        return convert(sourceFormat, targetFormat, file);
    }

    /**
     * 执行转换逻辑
     */
    private Path executeConversion(String sourceFormat, String targetFormat, Path sourceFile, Path tempDir) throws Exception {
        String sourceExt = sourceFormat.toLowerCase();
        String targetExt = targetFormat.toLowerCase();

        String baseName = DocumentConverterUtil.getNameWithoutExtension(sourceFile.getFileName().toString());
        String targetFileName = baseName + "." + targetExt;

        // 获取文本内容
        String textContent;
        switch (sourceExt) {
            case "pdf":
                textContent = DocumentConverterUtil.pdfToText(sourceFile);
                break;
            case "docx":
                textContent = DocumentConverterUtil.wordToText(sourceFile);
                break;
            case "md":
                // MD 文件直接读取为文本，然后转换 markdown 语法
                textContent = Files.readString(sourceFile);
                textContent = DocumentConverterUtil.mdToText(textContent);
                break;
            case "txt":
            default:
                textContent = Files.readString(sourceFile);
                break;
        }

        // 转换为目标格式
        Path targetFile;
        switch (targetExt) {
            case "txt":
                targetFile = DocumentConverterUtil.writeTextToFile(textContent, tempDir, targetFileName);
                break;
            case "md":
                // TXT 转 MD
                String mdContent = DocumentConverterUtil.textToMd(textContent);
                targetFile = DocumentConverterUtil.writeTextToFile(mdContent, tempDir, targetFileName);
                break;
            case "docx":
                // 文本或 MD 转 Word
                XWPFDocument wordDoc = DocumentConverterUtil.createWordFromText(textContent);
                targetFile = DocumentConverterUtil.writeWordToFile(wordDoc, tempDir, targetFileName);
                break;
            case "pdf":
                // PDF 转换需要特殊处理，这里仅支持将文本写入 PDF
                // 由于完整 PDF 生成需要额外库，这里抛出不支持异常
                throw new UnsupportedOperationException("PDF 生成暂不支持，请使用其他方式转换");
            default:
                throw new IllegalArgumentException("不支持的目标格式: " + targetExt);
        }

        return targetFile;
    }

    /**
     * 异步转换，返回 CompletableFuture
     */
    public CompletableFuture<Path> convertInFuture(String sourceFormat, String targetFormat, MultipartFile file) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return convert(sourceFormat, targetFormat, file);
            } catch (Exception e) {
                throw new RuntimeException("文档转换失败: " + e.getMessage(), e);
            }
        });
    }
}