package com.openstudy.document.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * 文档转换工具类
 * 支持 PDF、Word、TXT、MD 四种格式的相互转换
 */
@Slf4j
public class DocumentConverterUtil {

    private static final int MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB

    /**
     * 验证文件大小
     */
    public static void validateFileSize(long fileSize) throws Exception {
        if (fileSize <= 0) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        if (fileSize > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("文件大小不能超过 50MB");
        }
    }

    /**
     * PDF 转换为纯文本
     */
    public static String pdfToText(InputStream inputStream) throws Exception {
        try (PDDocument document = Loader.loadPDF(inputStream.readAllBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            return stripper.getText(document);
        }
    }

    /**
     * PDF 转换为纯文本（从文件路径）
     */
    public static String pdfToText(Path filePath) throws Exception {
        try (PDDocument document = Loader.loadPDF(filePath.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            return stripper.getText(document);
        }
    }

    /**
     * Word 文档读取为纯文本
     */
    public static String wordToText(InputStream inputStream) throws Exception {
        StringBuilder text = new StringBuilder();
        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            for (XWPFParagraph paragraph : paragraphs) {
                text.append(paragraph.getText()).append("\n");
            }
        }
        return text.toString();
    }

    /**
     * Word 文档读取为纯文本（从文件路径）
     */
    public static String wordToText(Path filePath) throws Exception {
        StringBuilder text = new StringBuilder();
        try (InputStream inputStream = Files.newInputStream(filePath);
                XWPFDocument document = new XWPFDocument(inputStream)) {
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            for (XWPFParagraph paragraph : paragraphs) {
                text.append(paragraph.getText()).append("\n");
            }
        }
        return text.toString();
    }

    /**
     * 文本内容写入文件
     */
    public static Path writeTextToFile(String content, Path directory, String fileName) throws Exception {
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }
        Path filePath = directory.resolve(fileName);
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
            writer.write(content);
        }
        return filePath;
    }

    /**
     * 复制输入流到文件
     */
    public static Path copyToFile(InputStream inputStream, Path directory, String fileName) throws Exception {
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }
        Path filePath = directory.resolve(fileName);
        try (OutputStream outputStream = Files.newOutputStream(filePath)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        return filePath;
    }

    /**
     * 创建临时目录
     */
    public static Path createTempDirectory(String prefix) throws Exception {
        Path tempDir = Files.createTempDirectory(prefix);
        return tempDir;
    }

    /**
     * 删除文件或目录
     */
    public static void deleteFile(Path path) {
        if (path == null)
            return;
        try {
            if (Files.isDirectory(path)) {
                Files.walk(path)
                        .sorted((a, b) -> b.compareTo(a))
                        .forEach(p -> {
                            try {
                                Files.delete(p);
                            } catch (IOException e) {
                                log.warn("删除文件失败: {}", p);
                            }
                        });
            } else {
                Files.deleteIfExists(path);
            }
        } catch (IOException e) {
            log.warn("清理临时文件失败: {}", path);
        }
    }

    /**
     * 将 Word 文档写入文件
     */
    public static Path writeWordToFile(XWPFDocument document, Path directory, String fileName) throws Exception {
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }
        Path filePath = directory.resolve(fileName);
        try (OutputStream outputStream = Files.newOutputStream(filePath)) {
            document.write(outputStream);
        }
        return filePath;
    }

    /**
     * 创建 Word 文档从文本内容
     */
    public static XWPFDocument createWordFromText(String content) {
        XWPFDocument document = new XWPFDocument();
        String[] lines = content.split("\n");
        for (String line : lines) {
            XWPFParagraph paragraph = document.createParagraph();
            paragraph.createRun().setText(line);
        }
        return document;
    }

    /**
     * MD 转 TXT（移除 markdown 语法，保留文本）
     */
    public static String mdToText(String mdContent) {
        if (mdContent == null || mdContent.isEmpty()) {
            return "";
        }
        String text = mdContent;
        // 移除标题标记
        text = text.replaceAll("^#{1,6}\\s+", "");
        // 移除加粗和斜体
        text = text.replaceAll("\\*\\*(.+?)\\*\\*", "$1");
        text = text.replaceAll("\\*(.+?)\\*", "$1");
        text = text.replaceAll("__(.+?)__", "$1");
        text = text.replaceAll("_(.+?)_", "$1");
        // 移除代码块
        text = text.replaceAll("```[\\s\\S]*?```", "");
        text = text.replaceAll("`(.+?)`", "$1");
        // 移除链接，保留文本
        text = text.replaceAll("\\[([^\\]]+)\\]\\([^)]+\\)", "$1");
        // 移除图片
        text = text.replaceAll("!\\[([^\\]]*)]\\([^)]+\\)", "");
        // 移除列表标记
        text = text.replaceAll("^\\s*[-*+]\\s+", "");
        text = text.replaceAll("^\\s*\\d+\\.\\s+", "");
        // 移除水平线
        text = text.replaceAll("^[-*_]{3,}$", "");
        return text.trim();
    }

    /**
     * TXT 转 MD（简单的文本到 markdown 转换）
     */
    public static String textToMd(String textContent) {
        if (textContent == null || textContent.isEmpty()) {
            return "";
        }
        // 简单的文本到 MD 转换
        // 将连续的空行转换为段落的分隔
        String md = textContent;
        // 将 URL 转换为 markdown 链接（简单的检测）
        md = md.replaceAll("(https?://[^\\s]+)", "[链接]($1)");
        return md;
    }

    /**
     * 获取文件扩展名
     */
    public static String getExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }

    /**
     * 获取不带扩展名的文件名
     */
    public static String getNameWithoutExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return fileName;
        }
        return fileName.substring(0, lastDotIndex);
    }

    /**
     * 判断是否为支持的文档格式
     */
    public static boolean isSupportedFormat(String format) {
        if (format == null)
            return false;
        String f = format.toLowerCase();
        return "pdf".equals(f) || "docx".equals(f) || "txt".equals(f) || "md".equals(f);
    }

    /**
     * 判断源格式是否可转换为目标格式
     */
    public static boolean canConvert(String sourceFormat, String targetFormat) {
        if (sourceFormat == null || targetFormat == null)
            return false;
        String source = sourceFormat.toLowerCase();
        String target = targetFormat.toLowerCase();
        // 所有格式都可以转换为 txt
        if ("txt".equals(target))
            return true;
        // md 可以转换为 docx
        if ("md".equals(source) && "docx".equals(target))
            return true;
        // txt 可以转换为 md 或 docx
        if ("txt".equals(source) && ("md".equals(target) || "docx".equals(target)))
            return true;
        // docx 可以转换为 pdf、txt、md
        if ("docx".equals(source) && ("pdf".equals(target) || "txt".equals(target) || "md".equals(target)))
            return true;
        // pdf 可以转换为 txt、md 或 docx
        if ("pdf".equals(source) && ("txt".equals(target) || "md".equals(target) || "docx".equals(target)))
            return true;
        return false;
    }
}