package com.openstudy.ai.service.rag;

import com.openstudy.ai.domain.RagDocument;
import com.openstudy.ai.domain.RagDocumentChunk;
import com.openstudy.ai.mapper.RagDocumentChunkMapper;
import com.openstudy.ai.mapper.RagDocumentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagDocumentChunkService {

    private static final int CHUNK_SIZE = 800; // 每块最大字符数（从500改为800）
    private static final int OVERLAP_SIZE = 0; // 移除重叠，降低分块数量
    private static final int MAX_CHUNKS = 100; // 最大分块数量
    private static final int MAX_CONTENT_LENGTH = 50000; // 最大内容长度

    private final RagDocumentMapper ragDocumentMapper;
    private final RagDocumentChunkMapper chunkMapper;

    /**
     * 对文档进行分块（优化版：从临时文件读取）
     */
    public void chunkDocument(Long documentId) {
        log.info("开始分块文档，documentId: {}", documentId);

        // 1. 查询文档
        RagDocument doc = ragDocumentMapper.selectById(documentId);
        if (doc == null) {
            throw new RuntimeException("文档不存在");
        }

        // 2. 从临时文件读取完整内容
        String content;
        try {
            content = readContentFromTempFile(documentId);
            if (content == null || content.isEmpty()) {
                // 如果临时文件不存在，尝试从数据库读取预览
                content = doc.getRawContent();
                if (content == null || content.isEmpty()) {
                    throw new RuntimeException("文档内容为空，请先解析");
                }
                log.warn("临时文件不存在，使用数据库中的预览内容（可能不完整）");
            }
        } catch (Exception e) {
            log.error("读取临时文件失败", e);
            content = doc.getRawContent();
            if (content == null || content.isEmpty()) {
                throw new RuntimeException("文档内容为空，请先解析");
            }
        }

        // 3. 限制内容长度
        if (content.length() > MAX_CONTENT_LENGTH) {
            log.warn("内容过长: {} 字符，截断为 {} 字符", content.length(), MAX_CONTENT_LENGTH);
            content = content.substring(0, MAX_CONTENT_LENGTH);
        }

        // 4. 删除旧的分块
        chunkMapper.deleteByDocumentId(documentId);

        // 5. 进行分块
        List<String> chunks = splitText(content);
        log.info("文档分块完成，共 {} 块", chunks.size());

        // 6. 检查分块数量上限
        if (chunks.size() > MAX_CHUNKS) {
            log.warn("分块数量过多: {}，截断为 {} 块", chunks.size(), MAX_CHUNKS);
            chunks = chunks.subList(0, MAX_CHUNKS);
        }

        // 7. 保存分块（批量插入）
        List<RagDocumentChunk> chunkList = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            String chunkContent = chunks.get(i);
            RagDocumentChunk chunk = new RagDocumentChunk();
            chunk.setDocumentId(documentId);
            chunk.setChunkIndex(i);
            chunk.setContent(chunkContent);
            chunk.setContentLength(chunkContent.length());
            chunk.setVectorId("doc_" + documentId + "_chunk_" + i);
            chunkList.add(chunk);
        }

        // 批量插入，提高性能
        if (!chunkList.isEmpty()) {
            chunkMapper.batchInsert(chunkList);
            log.info("批量插入 {} 个分块", chunkList.size());
        }

        // 8. 更新文档的分块数量
        doc.setChunkCount(chunkList.size());
        ragDocumentMapper.update(doc);

        log.info("分块保存完成，documentId: {}, chunkCount: {}", documentId, chunkList.size());
    }

    /**
     * 从临时文件读取内容
     */
    private String readContentFromTempFile(Long documentId) throws IOException {
        String tempFilePath = "D:/ruoyi/uploadPath/rag/temp/doc_" + documentId + ".txt";
        java.nio.file.Path path = java.nio.file.Paths.get(tempFilePath);

        if (!java.nio.file.Files.exists(path)) {
            return null;
        }

        return java.nio.file.Files.readString(path);
    }

    /**
     * 文本分块算法
     * 优先按段落分割，超过长度再按字符切分，保留重叠
     */
    private List<String> splitText(String text) {
        List<String> result = new ArrayList<>();

        // 1. 按段落分割
        String[] paragraphs = text.split("\n\n");
        List<String> paragraphList = new ArrayList<>();

        for (String para : paragraphs) {
            String trimmed = para.trim();
            if (!trimmed.isEmpty()) {
                paragraphList.add(trimmed);
            }
        }

        // 2. 对每个段落进行分块
        for (String paragraph : paragraphList) {
            if (paragraph.length() <= CHUNK_SIZE) {
                // 短段落直接作为一个块
                result.add(paragraph);
            } else {
                // 长段落按字符切分
                List<String> chunks = splitLongText(paragraph);
                result.addAll(chunks);
            }
        }

        // 3. 如果结果为空，返回原文本
        if (result.isEmpty()) {
            result.add(text);
        }

        return result;
    }

    /**
     * 对长文本按字符切分，保留重叠
     */
    private List<String> splitLongText(String text) {
        List<String> chunks = new ArrayList<>();
        int start = 0;
        int length = text.length();

        while (start < length) {
            int end = Math.min(start + CHUNK_SIZE, length);
            String chunk = text.substring(start, end);
            chunks.add(chunk);

            // 移动到下一个位置，考虑重叠
            start = end - OVERLAP_SIZE;

            // 防止死循环：如果 start 没有前进，强制前进
            if (start <= end - CHUNK_SIZE) {
                start = end;
            }

            // 安全检查：避免无限循环
            if (chunks.size() > 10000) {
                log.warn("分块数量过多，可能存在问题，强制退出");
                break;
            }
        }

        return chunks;
    }

    /**
     * 从文件提取文本（与 RagDocumentParserService 相同逻辑）
     */
    private String extractTextFromFile(String filePath, String fileType) throws IOException {
        switch (fileType.toLowerCase()) {
            case "txt":
            case "md":
                return Files.readString(Paths.get(filePath));
            case "pdf":
                return extractPdfText(filePath);
            case "docx":
                return extractWordText(filePath);
            default:
                throw new RuntimeException("不支持的文件类型: " + fileType);
        }
    }

    private String extractPdfText(String filePath) throws IOException {
        // PDFBox 3.0 使用默认配置加载，内存管理由内部处理
        try (PDDocument document = Loader.loadPDF(new java.io.File(filePath))) {
            PDFTextStripper stripper = new PDFTextStripper();
            // 限制最大页数
            int pageCount = document.getNumberOfPages();
            if (pageCount > 100) {
                log.warn("PDF 页数过多: {}，只处理前100页", pageCount);
                stripper.setEndPage(100);
            }
            return stripper.getText(document);
        }
    }

    private String extractWordText(String filePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath);
                XWPFDocument document = new XWPFDocument(fis);
                XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        }
    }
}
