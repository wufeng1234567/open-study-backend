package com.openstudy.ocr.domain;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * OCR识别结果对象
 */
@Data
public class OcrResult {
    // 识别文本（JSON 格式）
    private String text;

    // 词条列表
    private List<Map<String, String>> words;

    // 识别状态
    private OcrStatus status;

    // 提示信息
    private String message;

    // 统计信息
    private OcrStats stats;

    @Data
    public static class OcrStatus {
        private String level;      // success / warning / error
        private String icon;       // ✅ / ⚠️ / 😕 / 📝 / ✏️
        private String title;      // 标题
        private String suggestion; // 建议
    }

    @Data
    public static class OcrStats {
        private int total;         // 总词条数
        private int complete;      // 完整词条数（有英文和中文）
        private int incomplete;    // 不完整词条数
    }
}