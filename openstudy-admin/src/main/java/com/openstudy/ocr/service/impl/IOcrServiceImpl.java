package com.openstudy.ocr.service.impl;

import com.openstudy.ocr.domain.OcrResult;
import com.openstudy.ocr.service.IOcrService;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * OCR服务实现类 - 专门处理英文单词卡
 */
@Service
public class IOcrServiceImpl implements IOcrService {

    // 文件大小限制：10MB
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    // 支持的图片格式
    private static final List<String> SUPPORTED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/bmp", "image/tiff"
    );

    // 支持的文件扩展名
    private static final List<String> SUPPORTED_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "bmp", "tif", "tiff"
    );

    @Override
    public OcrResult recognize(MultipartFile file) throws IOException, TesseractException {
        // 文件验证
        validateFile(file);

        Path tempFile = null;
        try {
            // 将上传的文件保存为临时文件
            String fileExtension = getFileExtension(file.getOriginalFilename());
            tempFile = Files.createTempFile("ocr_", "." + fileExtension);
            Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);

            // 初始化Tesseract OCR引擎
            ITesseract tesseract = new Tesseract();

            // 设置语言包路径
            String tessdataPath = getTessdataPath();
            tesseract.setDatapath(tessdataPath);

            // 设置识别语言
            tesseract.setLanguage("eng+chi_sim");

            // 设置OCR参数（提高识别精度）
            setTesseractParameters(tesseract);

            // 进行OCR识别
            String result = tesseract.doOCR(tempFile.toFile());

            // 处理识别结果，提取单词和中文意思
            String processedResult = processOcrResult(result);

            // 构造返回结果
            OcrResult ocrResult = new OcrResult();
            ocrResult.setText(processedResult);
            return ocrResult;

        } finally {
            // 确保临时文件被删除
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (Exception e) {
                    System.err.println("警告: 临时文件删除失败: " + e.getMessage());
                }
            }
        }
    }

    /**
     * 设置Tesseract参数以提高识别精度
     */
    private void setTesseractParameters(ITesseract tesseract) {
        // 设置PSM模式（页面分割模式）
        // 3 = 完全自动页面分割，但不进行方向检测（适合单列文本）
        // 6 = 假定为统一的文本块
        tesseract.setPageSegMode(6);

        // 设置OCR引擎模式
        // 3 = 两者结合（默认）
        tesseract.setOcrEngineMode(3);
    }

    /**
     * 处理OCR识别结果，提取英文单词和中文意思
     */
    private String processOcrResult(String ocrText) {
        if (ocrText == null || ocrText.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        String[] lines = ocrText.split("\\r?\\n");

        // 存储每个单词条目的信息
        List<WordEntry> entries = new ArrayList<>();
        WordEntry currentEntry = null;

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }

            // 处理当前行
            processLine(line, entries);
        }

        // 格式化输出
        for (WordEntry entry : entries) {
            if (entry.isValid()) {
                result.append(entry.format()).append("\n");
            }
        }

        return result.toString().trim();
    }

    /**
     * 处理单行文本
     */
    private void processLine(String line, List<WordEntry> entries) {
        // 删除音标部分（两个斜杠之间的内容）
        String lineWithoutPhonetic = TextProcessingUtil.removePhonetic(line);

        // 提取英文单词
        String englishWords = TextProcessingUtil.extractEnglishWords(lineWithoutPhonetic);

        // 提取中文释义
        String chineseMeaning = TextProcessingUtil.extractChineseWithContext(line);

        if (!englishWords.isEmpty() && !chineseMeaning.isEmpty()) {
            // 如果有多个英文单词，可能是短语
            String[] words = englishWords.split("\\s+");

            if (words.length == 1) {
                // 单个单词
                WordEntry entry = new WordEntry();
                entry.english = words[0];
                entry.chinese = chineseMeaning;
                entries.add(entry);
            } else {
                // 短语或多个单词
                WordEntry entry = new WordEntry();
                entry.english = englishWords;
                entry.chinese = chineseMeaning;
                entries.add(entry);
            }
        } else if (!englishWords.isEmpty()) {
            // 只有英文，可能是标题或单独的单词行
            // 检查是否为常见英文短语
            if (isCommonEnglishPhrase(englishWords)) {
                // 可能是短语标题，跳过
                return;
            }

            // 创建新条目，中文可能在下行
            WordEntry entry = new WordEntry();
            entry.english = englishWords;
            entries.add(entry);
        } else if (!chineseMeaning.isEmpty()) {
            // 只有中文，可能是上一行的释义
            if (!entries.isEmpty()) {
                WordEntry lastEntry = entries.get(entries.size() - 1);
                if (lastEntry.chinese == null || lastEntry.chinese.isEmpty()) {
                    lastEntry.chinese = chineseMeaning;
                }
            }
        }
    }

    /**
     * 判断是否为常见英文短语（如标题等）
     */
    private boolean isCommonEnglishPhrase(String text) {
        String lower = text.toLowerCase();
        return lower.equals("word tips") ||
                lower.equals("vocabulary") ||
                lower.equals("words") ||
                lower.matches("^[a-z]+\\s+[a-z]+$") && lower.length() < 15;
    }

    /**
     * 单词条目类
     */
    private static class WordEntry {
        String english;
        String chinese;

        boolean isValid() {
            return english != null && !english.isEmpty() &&
                    chinese != null && !chinese.isEmpty();
        }

        String format() {
            return english + ": " + chinese;
        }
    }

    /**
     * 验证上传的文件
     */
    private void validateFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("上传文件不能为空");
        }

        // 检查文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IOException("文件大小不能超过10MB");
        }

        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !SUPPORTED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new IOException("不支持的文件类型，请上传JPG、PNG、GIF、BMP、TIFF格式的图片");
        }

        // 检查文件扩展名
        String extension = getFileExtension(file.getOriginalFilename()).toLowerCase();
        if (!SUPPORTED_EXTENSIONS.contains(extension)) {
            throw new IOException("不支持的文件扩展名");
        }
    }

    /**
     * 获取tessdata路径
     */
    private String getTessdataPath() throws IOException {
        // 首先尝试从classpath获取resources下的tessdata目录
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource("tessdata");

        if (resource != null) {
            String path = new File(resource.getFile()).getAbsolutePath();
            // 验证目录下是否有语言包
            File tessdataDir = new File(path);
            if (tessdataDir.exists() && tessdataDir.isDirectory()) {
                File[] langFiles = tessdataDir.listFiles((dir, name) ->
                        name.startsWith("eng.") && name.endsWith(".traineddata"));
                if (langFiles != null && langFiles.length > 0) {
                    return path;
                }
            }
        }

        // 尝试多种路径获取tessdata目录
        String[] possiblePaths = {
                System.getProperty("user.dir") + File.separator + "tessdata",
                "tessdata",
                "./tessdata",
                "/usr/share/tesseract-ocr/tessdata",
                "/usr/share/tessdata"
        };

        for (String path : possiblePaths) {
            File tessdataDir = new File(path);
            if (tessdataDir.exists() && tessdataDir.isDirectory()) {
                File[] langFiles = tessdataDir.listFiles((dir, name) ->
                        name.startsWith("eng.") && name.endsWith(".traineddata"));
                if (langFiles != null && langFiles.length > 0) {
                    return path;
                }
            }
        }

        throw new IOException("未找到tessdata目录或缺少英语语言包，请确保已正确安装Tesseract OCR引擎");
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return "jpg"; // 默认扩展名
        }
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
        return extension.isEmpty() ? "jpg" : extension;
    }
}