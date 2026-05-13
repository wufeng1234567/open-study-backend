package com.openstudy.ai.controller;

import com.openstudy.ai.domain.RagDocument;
import com.openstudy.ai.domain.RagDocumentChunk;
import com.openstudy.ai.service.RagDocumentChunkService;
import com.openstudy.ai.service.RagDocumentService;
import com.openstudy.ai.service.rag.RagDocumentAsyncService;
import com.openstudy.ai.service.rag.RagDocumentSyncService;
import com.openstudy.ai.service.rag.RagDocumentUploadService;
import com.openstudy.ai.service.rag.RagQuestionService;
import com.openstudy.ai.service.rag.RagVectorService;
import com.openstudy.common.core.controller.BaseController;
import com.openstudy.common.core.domain.AjaxResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RAG 文档管理 Controller
 */
@Slf4j
@Tag(name = "RAG文档管理")
@RestController
@RequestMapping("/rag/document")
@RequiredArgsConstructor
public class RagDocumentController extends BaseController {

    private final RagDocumentUploadService uploadService;
    private final RagVectorService vectorService;
    private final RagQuestionService questionService;
    private final RagDocumentAsyncService asyncService;
    private final RagDocumentSyncService syncService; // 临时调试用
    private final RagDocumentService documentService;
    private final RagDocumentChunkService chunkService;

    @Operation(summary = "上传文档")
    @PostMapping("/upload")
    public AjaxResult upload(@RequestParam("file") MultipartFile file,
            @RequestParam("knowledgeBaseId") Long knowledgeBaseId,
            @RequestParam(defaultValue = "1") Long userId) {
        log.info("收到文档上传请求: fileName={}, kbId={}, userId={}",
                file.getOriginalFilename(), knowledgeBaseId, userId);

        try {
            // 调用上传服务
            Long documentId = uploadService.uploadDocument(file, knowledgeBaseId, userId);

            // 【临时方案】使用同步处理，确保功能正常
            // TODO: 异步正常工作后改回 asyncService.asyncFullProcess(documentId)
            log.info("开始同步处理文档，documentId: {}", documentId);
            syncService.syncFullProcess(documentId);

            // 返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("documentId", documentId);
            result.put("fileName", file.getOriginalFilename());
            result.put("message", "文档处理完成");

            log.info("文档上传并处理成功: documentId={}", documentId);
            return success(result);

        } catch (RuntimeException e) {
            log.error("文档上传失败", e);
            return error(e.getMessage());
        } catch (Exception e) {
            log.error("文档上传异常", e);
            return error("文档上传失败: " + e.getMessage());
        }
    }

    @Operation(summary = "向量化文档")
    @PostMapping("/vectorize/{documentId}")
    public AjaxResult vectorizeDocument(@PathVariable Long documentId) {
        log.info("开始向量化文档，documentId: {}", documentId);

        try {
            vectorService.vectorizeDocument(documentId);

            Map<String, Object> result = new HashMap<>();
            result.put("documentId", documentId);
            result.put("message", "文档向量化成功");

            log.info("文档向量化成功: documentId={}", documentId);
            return success(result);

        } catch (Exception e) {
            log.error("文档向量化失败", e);
            return error("向量化失败: " + e.getMessage());
        }
    }

    @Operation(summary = "查询知识库下的文档列表")
    @GetMapping("/list/{knowledgeBaseId}")
    public AjaxResult listByKnowledgeBaseId(@PathVariable Long knowledgeBaseId) {
        log.info("查询知识库文档列表: knowledgeBaseId={}", knowledgeBaseId);

        try {
            List<RagDocument> documents = documentService.listByKnowledgeBaseId(knowledgeBaseId);
            return success(documents);
        } catch (Exception e) {
            log.error("查询文档列表失败", e);
            return error("查询文档列表失败: " + e.getMessage());
        }
    }

    @Operation(summary = "删除文档")
    @DeleteMapping("/{documentId}")
    public AjaxResult deleteDocument(@PathVariable Long documentId) {
        log.info("删除文档: documentId={}", documentId);

        try {
            int result = documentService.deleteById(documentId);
            if (result > 0) {
                return success("文档删除成功");
            } else {
                return error("文档不存在或删除失败");
            }
        } catch (Exception e) {
            log.error("删除文档失败", e);
            return error("删除文档失败: " + e.getMessage());
        }
    }

    @Operation(summary = "文本内容上传")
    @PostMapping("/uploadText")
    public AjaxResult uploadText(@RequestBody Map<String, Object> params) {
        String content = (String) params.get("content");
        Long knowledgeBaseId = Long.valueOf(params.get("knowledgeBaseId").toString());
        Long userId = Long.valueOf(params.get("userId").toString());
        String fileName = (String) params.getOrDefault("fileName", "文本内容_" + System.currentTimeMillis() + ".txt");

        log.info("收到文本内容上传请求: kbId={}, userId={}, contentLength={}",
                knowledgeBaseId, userId, content != null ? content.length() : 0);

        try {
            if (content == null || content.trim().isEmpty()) {
                return error("内容不能为空");
            }

            Long documentId = uploadService.uploadTextContent(content, fileName, knowledgeBaseId, userId);

            log.info("开始同步处理文本内容，documentId: {}", documentId);
            syncService.syncFullProcess(documentId);

            Map<String, Object> result = new HashMap<>();
            result.put("documentId", documentId);
            result.put("fileName", fileName);
            result.put("message", "文本内容上传并处理成功");

            log.info("文本内容上传成功: documentId={}", documentId);
            return success(result);

        } catch (RuntimeException e) {
            log.error("文本内容上传失败", e);
            return error(e.getMessage());
        } catch (Exception e) {
            log.error("文本内容上传异常", e);
            return error("文本内容上传失败: " + e.getMessage());
        }
    }

    @Operation(summary = "从笔记导入知识库")
    @PostMapping("/importFromNote")
    public AjaxResult importFromNote(@RequestBody Map<String, Long> params) {
        Long noteId = params.get("noteId");
        Long knowledgeBaseId = params.get("knowledgeBaseId");

        log.info("收到笔记导入知识库请求: noteId={}, kbId={}", noteId, knowledgeBaseId);

        try {
            if (noteId == null || knowledgeBaseId == null) {
                return error("参数不能为空");
            }

            Long documentId = documentService.importFromNote(noteId, knowledgeBaseId);

            Map<String, Object> result = new HashMap<>();
            result.put("documentId", documentId);
            result.put("message", "导入成功");

            log.info("笔记导入知识库成功: documentId={}", documentId);
            return success(result);

        } catch (RuntimeException e) {
            log.error("笔记导入知识库失败", e);
            return error(e.getMessage());
        } catch (Exception e) {
            log.error("笔记导入知识库异常", e);
            return error("导入失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取文档内容")
    @GetMapping("/content/{documentId}")
    public AjaxResult getDocumentContent(@PathVariable Long documentId) {
        log.info("获取文档内容: documentId={}", documentId);

        try {
            RagDocument doc = documentService.getById(documentId);
            if (doc == null) {
                return error("文档不存在");
            }

            // 优先尝试从临时文件读取完整内容
            String content = readContentFromTempFile(documentId, doc.getFileType());

            // 如果临时文件不存在，使用 rawContent
            if (content == null || content.isEmpty()) {
                content = doc.getRawContent();
                log.info("从数据库 rawContent 读取内容: {} 字符", content != null ? content.length() : 0);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("id", doc.getId());
            result.put("fileName", doc.getFileName());
            result.put("fileType", doc.getFileType());
            result.put("content", content);
            result.put("chunkCount", doc.getChunkCount());

            log.info("获取文档内容成功: documentId={}, contentLength={}", documentId, content != null ? content.length() : 0);
            return success(result);

        } catch (Exception e) {
            log.error("获取文档内容失败", e);
            return error("获取文档内容失败: " + e.getMessage());
        }
    }

    /**
     * 从临时文件读取完整内容
     */
    private String readContentFromTempFile(Long documentId, String fileType) {
        String[] paths = {
                "D:/ruoyi/uploadPath/rag/temp/doc_" + documentId + ".txt",
                "D:/ruoyi/uploadPath/rag/temp/doc_" + documentId + ".md",
                "D:/ruoyi/uploadPath/rag/temp/doc_" + documentId + ".pdf"
        };

        for (String path : paths) {
            try {
                if (Files.exists(Paths.get(path))) {
                    String content = Files.readString(Paths.get(path));
                    log.info("从临时文件读取内容成功: {} ({} 字符)", path, content.length());
                    return content;
                }
            } catch (IOException e) {
                log.warn("读取临时文件失败: {}", path, e.getMessage());
            }
        }

        return null;
    }

    @Operation(summary = "查询文档的分块列表")
    @GetMapping("/chunks/{documentId}")
    public AjaxResult getDocumentChunks(@PathVariable Long documentId) {
        log.info("查询文档分块列表: documentId={}", documentId);

        try {
            RagDocument doc = documentService.getById(documentId);
            if (doc == null) {
                return error("文档不存在");
            }

            // 调用已有的 chunkService
            List<RagDocumentChunk> chunks = chunkService.listByDocumentId(documentId);

            log.info("查询到 {} 个分块", chunks != null ? chunks.size() : 0);
            return success(chunks != null ? chunks : java.util.Collections.emptyList());

        } catch (Exception e) {
            log.error("查询文档分块列表失败", e);
            return error("查询分块列表失败: " + e.getMessage());
        }
    }
}
