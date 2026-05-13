package com.openstudy.ai.service.rag;

import com.openstudy.ai.domain.RagDocument;
import com.openstudy.ai.mapper.RagDocumentMapper;
import com.openstudy.ai.mapper.RagKnowledgeBaseMapper;
import com.openstudy.common.exception.file.FileSizeLimitExceededException;
import com.openstudy.common.exception.file.InvalidExtensionException;
import com.openstudy.common.utils.file.FileUploadUtils;
import com.openstudy.common.utils.file.MimeTypeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * RAG 文档上传服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagDocumentUploadService {
    
    private final RagDocumentMapper documentMapper;
    private final RagKnowledgeBaseMapper ragKnowledgeBaseMapper;
    
    /**
     * 允许的文件扩展名
     */
    private static final String[] ALLOWED_EXTENSIONS = {"pdf", "docx", "txt", "md"};
    
    /**
     * 最大文件大小：3MB（从10MB改为3MB）
     */
    private static final long MAX_FILE_SIZE = 3 * 1024 * 1024L;
    
    /**
     * 上传文档
     *
     * @param file 上传的文件
     * @param knowledgeBaseId 知识库ID
     * @param userId 用户ID
     * @return 文档ID
     */
    public Long uploadDocument(MultipartFile file, Long knowledgeBaseId, Long userId) {
        log.info("开始上传文档: fileName={}, kbId={}, userId={}", 
                file.getOriginalFilename(), knowledgeBaseId, userId);
        
        try {
            // 1. 校验文件
            validateFile(file);
            
            // 2. 上传文件到服务器
            String filePath = uploadFile(file);
            log.info("文件上传成功: {}", filePath);
            
            // 3. 创建文档记录
            RagDocument document = createDocumentRecord(file, knowledgeBaseId, userId, filePath);
            documentMapper.insert(document);
            
            log.info("文档记录创建成功，ID: {}", document.getId());
            
            // 4. 更新知识库文档数量
            ragKnowledgeBaseMapper.incrementDocumentCount(knowledgeBaseId, 1);
            log.info("知识库文档数量已更新，kbId: {}", knowledgeBaseId);
            
            return document.getId();
            
        } catch (FileSizeLimitExceededException e) {
            log.error("文件大小超出限制", e);
            throw new RuntimeException("文件大小不能超过3MB，当前文件: " + (file.getSize() / 1024 / 1024) + "MB");
        } catch (InvalidExtensionException e) {
            log.error("不支持的文件类型", e);
            throw new RuntimeException("只支持 pdf, docx, txt, md 格式的文件");
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("上传文档异常", e);
            throw new RuntimeException("上传文档失败: " + e.getMessage());
        }
    }
    
    /**
     * 校验文件
     */
    private void validateFile(MultipartFile file) throws FileSizeLimitExceededException, InvalidExtensionException {
        // 检查文件是否为空
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }
        
        // 检查文件大小
        long fileSize = file.getSize();
        if (fileSize > MAX_FILE_SIZE) {
            throw new FileSizeLimitExceededException(MAX_FILE_SIZE / 1024 / 1024);
        }
        
        // 检查文件类型
        String extension = FileUploadUtils.getExtension(file).toLowerCase();
        boolean allowed = false;
        for (String ext : ALLOWED_EXTENSIONS) {
            if (ext.equalsIgnoreCase(extension)) {
                allowed = true;
                break;
            }
        }
        
        if (!allowed) {
            throw new InvalidExtensionException(ALLOWED_EXTENSIONS, extension, file.getOriginalFilename());
        }
    }
    
    /**
     * 上传文件
     */
    private String uploadFile(MultipartFile file) throws IOException, InvalidExtensionException {
        // 使用若依的文件上传工具类
        // 上传到 rag/documents 目录
        String baseDir = FileUploadUtils.getDefaultBaseDir() + "/rag/documents";
        return FileUploadUtils.upload(baseDir, file, ALLOWED_EXTENSIONS);
    }
    
    /**
     * 创建文档记录
     */
    private RagDocument createDocumentRecord(MultipartFile file, Long knowledgeBaseId,
                                              Long userId, String filePath) {
        RagDocument document = new RagDocument();
        document.setKnowledgeBaseId(knowledgeBaseId);
        document.setUserId(userId);
        document.setFileName(file.getOriginalFilename());
        document.setFileType(FileUploadUtils.getExtension(file).toLowerCase());
        document.setFileSize(file.getSize());
        document.setFilePath(filePath);
        document.setChunkCount(0);
        document.setStatus(1); // 1-待解析
        document.setErrorMsg("");

        return document;
    }

    /**
     * 上传文本内容
     *
     * @param content 文本内容
     * @param fileName 文件名
     * @param knowledgeBaseId 知识库ID
     * @param userId 用户ID
     * @return 文档ID
     */
    public Long uploadTextContent(String content, String fileName, Long knowledgeBaseId, Long userId) {
        log.info("开始上传文本内容: fileName={}, kbId={}, userId={}, contentLength={}",
                fileName, knowledgeBaseId, userId, content != null ? content.length() : 0);

        try {
            RagDocument document = new RagDocument();
            document.setKnowledgeBaseId(knowledgeBaseId);
            document.setUserId(userId);
            document.setFileName(fileName);
            document.setFileType("txt");
            document.setFileSize((long) content.getBytes().length);
            document.setRawContent(content);
            document.setChunkCount(0);
            document.setStatus(1);
            document.setErrorMsg("");

            documentMapper.insert(document);
            log.info("文本内容记录创建成功，ID: {}", document.getId());

            ragKnowledgeBaseMapper.incrementDocumentCount(knowledgeBaseId, 1);
            log.info("知识库文档数量已更新，kbId: {}", knowledgeBaseId);

            return document.getId();

        } catch (Exception e) {
            log.error("上传文本内容异常", e);
            throw new RuntimeException("上传文本内容失败: " + e.getMessage());
        }
    }
}
