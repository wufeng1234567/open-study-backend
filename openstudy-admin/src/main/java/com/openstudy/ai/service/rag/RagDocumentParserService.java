package com.openstudy.ai.service.rag;

import com.openstudy.ai.domain.RagDocument;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class RagDocumentParserService {

    private final RagDocumentMapper ragDocumentMapper;

    /**
     * 解析文档（优化版：不存储完整文本到数据库）
     */
    public void parseDocument(Long documentId) {
        log.info("开始解析文档，documentId: {}", documentId);

        // 1. 查询文档
        RagDocument doc = ragDocumentMapper.selectById(documentId);
        if (doc == null) {
            log.error("文档不存在，documentId: {}", documentId);
            throw new RuntimeException("文档不存在");
        }

        // 2. 更新状态为解析中
        doc.setStatus(2);
        ragDocumentMapper.update(doc);

        try {
            String content;

            // 3. 检查是否有 raw_content（文本上传的内容）
            String rawContent = doc.getRawContent();
            if (rawContent != null && !rawContent.isEmpty()) {
                // 文本内容直接使用 raw_content
                log.info("使用已有的 raw_content 作为文档内容");
                content = rawContent;
            } else {
                // 4. 获取文件路径（文件上传的情况）
                String filePath = doc.getFilePath();
                if (filePath == null || filePath.isEmpty()) {
                    throw new RuntimeException("文件路径为空");
                }

                // 若依存储的路径是 /profile/xxx，需要转换为实际路径
                String realPath = filePath.replace("/profile/", "D:/ruoyi/uploadPath/");
                java.nio.file.Path path = java.nio.file.Paths.get(realPath);

                if (!java.nio.file.Files.exists(path)) {
                    throw new RuntimeException("文件不存在: " + realPath);
                }

                // 4. 根据文件类型解析文本（流式读取）
                content = extractText(realPath, doc.getFileType());
            }

            // 5. 限制内容长度，最多处理 200000 字符（支持 100 页 PDF）
            if (content.length() > 200000) {
                log.warn("文档内容过长: {} 字符，截断为 200000 字符", content.length());
                content = content.substring(0, 200000);
            }

            log.info("文档解析完成，内容长度: {}", content.length());

            // 6. 只保存前 500 字符作为预览，不存储完整文本
            String preview = content.length() > 500 ? content.substring(0, 500) : content;
            doc.setRawContent(preview);
            doc.setStatus(3);  // 解析完成
            ragDocumentMapper.update(doc);

            // 7. 将完整内容保存到临时文件（供分块使用）
            saveContentToTempFile(documentId, content);

        } catch (Exception e) {
            log.error("文档解析失败", e);
            doc.setStatus(4);  // 解析失败
            doc.setErrorMsg(e.getMessage());
            ragDocumentMapper.update(doc);
            throw new RuntimeException("文档解析失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 将完整内容保存到临时文件
     */
    private void saveContentToTempFile(Long documentId, String content) throws IOException {
        String tempDir = "D:/ruoyi/uploadPath/rag/temp/";
        java.nio.file.Files.createDirectories(java.nio.file.Paths.get(tempDir));
        
        String tempFilePath = tempDir + "doc_" + documentId + ".txt";
        java.nio.file.Files.writeString(java.nio.file.Paths.get(tempFilePath), content);
        log.info("完整内容已保存到临时文件: {}", tempFilePath);
    }

    /**
     * 根据文件类型提取文本
     */
    private String extractText(String filePath, String fileType) throws IOException {
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

    /**
     * 解析 PDF 文本（优化版：支持最多 100 页）
     */
    private String extractPdfText(String filePath) throws IOException {
        // 设置临时目录
        System.setProperty("java.io.tmpdir", "D:/ruoyi/uploadPath/rag/temp/");
        
        try (PDDocument document = Loader.loadPDF(new java.io.File(filePath))) {
            // 检查页数限制
            int pageCount = document.getNumberOfPages();
            if (pageCount > 100) {
                throw new RuntimeException("PDF 文件超过 100 页限制，当前: " + pageCount + " 页");
            }
            
            log.info("PDF 总页数: {}", pageCount);
            
            PDFTextStripper stripper = new PDFTextStripper();
            
            // 分批处理，每 5 页释放一次内存（更频繁的 GC）
            StringBuilder fullText = new StringBuilder();
            int totalPages = document.getNumberOfPages();
            
            for (int startPage = 1; startPage <= totalPages; startPage += 5) {
                int endPage = Math.min(startPage + 4, totalPages);
                stripper.setStartPage(startPage);
                stripper.setEndPage(endPage);
                
                String pageText = stripper.getText(document);
                fullText.append(pageText);
                
                log.info("已处理 PDF 第 {}/{} 页", endPage, totalPages);
                
                // 每批处理后强制 GC，防止内存溢出
                if (startPage % 5 == 0) {
                    System.gc();
                    log.debug("执行 GC，已处理 {} 页", endPage);
                }
            }
            
            log.info("PDF 解析完成，总字符数: {}", fullText.length());
            return fullText.toString();
        }
    }

    /**
     * 解析 Word 文本
     */
    private String extractWordText(String filePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath);
             XWPFDocument document = new XWPFDocument(fis);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        }
    }
}
