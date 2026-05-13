package com.openstudy.ocr.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.openstudy.common.config.RuoYiConfig;
import com.openstudy.ocr.domain.OcrResult;
import com.openstudy.ocr.service.IOcrService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * 硅基流动 OCR 服务实现类（直接 HTTP 调用）
 */
@Slf4j
@Service("siliconflowOcrService")
public class SiliconflowOcrServiceImpl implements IOcrService {

    @Value("${spring.ai.siliconflow.api-key}")
    private String apiKey;

    @Value("${spring.ai.siliconflow.base-url:https://api.siliconflow.cn}")
    private String baseUrl;

    @Value("${spring.ai.siliconflow.chat.options.model:deepseek-ai/DeepSeek-OCR}")
    private String model;


    @Override
    public OcrResult recognize(MultipartFile file) throws Exception {
        // 1. 验证文件
        validateFile(file);

        // 2. 保存文件并获取 Base64
        String base64Image = saveAndEncodeToBase64(file);
        log.info("图片已转为 Base64，长度: {}", base64Image.length());

        // 3. 直接 HTTP 调用硅基流动 API
        String response = callSiliconflowOcrApi(base64Image, file.getContentType());
        log.info("OCR 识别结果: {}", response);

        // 4. 解析 JSON 并评估质量
        OcrResult result = parseAndEvaluate(response);

        return result;
    }

    /**
     * 解析 JSON 并评估识别质量
     */
    private OcrResult parseAndEvaluate(String jsonResponse) {
        OcrResult result = new OcrResult();
        result.setText(jsonResponse);

        List<Map<String, String>> words = new ArrayList<>();

        try {
            // 清理可能的 markdown 代码块标记
            String cleanJson = jsonResponse
                    .replaceAll("```json\\s*", "")
                    .replaceAll("```\\s*", "")
                    .trim();

            JSONArray array = JSON.parseArray(cleanJson);
            if (array != null) {
                for (int i = 0; i < array.size(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    Map<String, String> word = new HashMap<>();
                    word.put("english", obj.getString("english"));
                    word.put("chinese", obj.getString("chinese"));
                    word.put("phonetic", obj.getString("phonetic"));
                    word.put("type", obj.getString("type"));

                    // ✅ 过滤幻觉内容
                    if (isValidVocabularyEntry(word)) {
                        words.add(word);
                    } else {
                        log.warn("过滤无效词条: english={}, chinese={}",
                                word.get("english"), word.get("chinese"));
                    }
                }
            }
            result.setWords(words);
        } catch (Exception e) {
            log.warn("JSON 解析失败: {}", e.getMessage());
            words = new ArrayList<>();
        }

        // 统计
        int total = words.size();
        int complete = 0;
        int incomplete = 0;

        for (Map<String, String> word : words) {
            String english = word.get("english");
            String chinese = word.get("chinese");
            if (english != null && !english.isEmpty() &&
                    chinese != null && !chinese.isEmpty()) {
                complete++;
            } else {
                incomplete++;
            }
        }

        // 构建统计
        OcrResult.OcrStats stats = new OcrResult.OcrStats();
        stats.setTotal(total);
        stats.setComplete(complete);
        stats.setIncomplete(incomplete);
        result.setStats(stats);

        // 构建状态（如果过滤后没有有效词条，状态为 error）
        OcrResult.OcrStatus status;
        if (total == 0) {
            status = new OcrResult.OcrStatus();
            status.setLevel("error");
            status.setIcon("😕");
            status.setTitle("识别内容无效");
            status.setSuggestion("请上传包含英文单词、词组或句子的图片");
        } else {
            status = evaluateStatus(total, complete, incomplete);
        }
        result.setStatus(status);
        result.setMessage(status.getTitle());

        return result;
    }

    /**
     * 校验词条是否有效（过滤幻觉内容）
     */
    private boolean isValidVocabularyEntry(Map<String, String> word) {
        String english = word.get("english");
        String chinese = word.get("chinese");

        // 1. 基本非空校验
        if (english == null || english.trim().isEmpty()) {
            return false;
        }

        english = english.trim();

        // 2. 长度校验：英文太长或太短都不合理
        if (english.length() < 2 || english.length() > 200) {
            return false;
        }

        // 3. 幻觉特征检测
        // 检测序号格式（如 "1. 1986"、"112. 2093"）
        if (english.matches("^\\d+\\.\\s*\\d+.*$") || english.matches("^\\d+\\s+\\d+.*$")) {
            return false;
        }

        // 检测纯数字或日期格式
        if (english.matches("^[\\d\\s\\.\\-\\/]+$")) {
            return false;
        }

        // 检测重复模式（如 "Use the following formula" 重复出现）
        String lowerEnglish = english.toLowerCase();
        String[] hallucinationPatterns = {
                "use the following formula",
                "the quick brown fox",
                "lazy dog",
                "nasa",
                "空间科学",
                "美国国家航空航天局"
        };

        for (String pattern : hallucinationPatterns) {
            if (lowerEnglish.contains(pattern)) {
                return false;
            }
        }

        // 4. 英文内容必须包含字母
        if (!english.matches(".*[a-zA-Z].*")) {
            return false;
        }

        // 5. 不能是纯数字+标点
        if (english.replaceAll("[\\d\\s\\.\\,\\-\\_\\:\\;\\'\\\"\\?\\!]", "").length() < 2) {
            return false;
        }

        // 6. 中文校验（如果有中文，必须是真正的中文）
        if (chinese != null && !chinese.isEmpty()) {
            // 中文不能是纯数字或纯英文
            if (!chinese.matches(".*[\\u4e00-\\u9fa5].*")) {
                // 如果没有中文字符，检查是否是幻觉内容
                if (chinese.matches(".*(nasa|NASA|formula|quick brown fox).*")) {
                    return false;
                }
            }

            // 中文长度不能太离谱
            if (chinese.length() > 500) {
                return false;
            }
        }

        return true;
    }
    /**
     * 评估识别状态
     */
    private OcrResult.OcrStatus evaluateStatus(int total, int complete, int incomplete) {
        OcrResult.OcrStatus status = new OcrResult.OcrStatus();

        if (total == 0) {
            // 完全失败
            status.setLevel("error");
            status.setIcon("😕");
            status.setTitle("未能识别到任何文字");
            status.setSuggestion("检查图片清晰度、裁剪文字区域");
        } else if (total <= 2) {
            // 结果较少
            status.setLevel("warning");
            status.setIcon("📝");
            status.setTitle("识别结果较少");
            status.setSuggestion("检查图片完整性、手动添加");
        } else if (incomplete > 0) {
            // 信息不完整
            status.setLevel("warning");
            status.setIcon("✏️");
            status.setTitle("部分词条信息不完整");
            status.setSuggestion("有 " + incomplete + " 个词条缺少中文释义，请手动补充");
        } else {
            // 识别成功
            status.setLevel("success");
            status.setIcon("✅");
            status.setTitle("识别成功");
            status.setSuggestion("共识别到 " + total + " 个词条，请检查并确认后导入");
        }

        return status;
    }

    /**
     * 直接 HTTP 调用硅基流动 API
     */
    private String callSiliconflowOcrApi(String base64Image, String contentType) throws Exception {
        String apiUrl = baseUrl + "/v1/chat/completions";
        log.info("调用硅基流动 API: {}", apiUrl);

        // 构建请求体
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", model);
        requestBody.put("temperature", 0.1);
        requestBody.put("max_tokens", 4096);

        // 构建 messages
        JSONArray messages = new JSONArray();
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");

        // 构建 content 数组
        JSONArray content = new JSONArray();

        // 添加图片 - 注意 MIME 类型
        JSONObject imagePart = new JSONObject();
        imagePart.put("type", "image_url");
        JSONObject imageUrl = new JSONObject();
        // 根据文件类型使用正确的 MIME 类型
        String mimeType = contentType != null ? contentType : "image/jpeg";
        imageUrl.put("url", "data:" + mimeType + ";base64," + base64Image);
        imagePart.put("image_url", imageUrl);
        content.add(imagePart);

        // 添加文本提示词 - 优化 PaddleOCR 输出格式
        JSONObject textPart = new JSONObject();
        textPart.put("type", "text");
        textPart.put("text", "<image>\n<|grounding|>OCR this image.");

        content.add(textPart);

        userMessage.put("content", content);
        messages.add(userMessage);
        requestBody.put("messages", messages);

        // log.debug("请求体: {}", requestBody.toJSONString());

        // 发送 HTTP 请求
        HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        connection.setDoOutput(true);
        connection.setConnectTimeout(30000);
        connection.setReadTimeout(120000);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = requestBody.toJSONString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            String response = new String(connection.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            // log.debug("API 响应: {}", response);

            JSONObject result = JSON.parseObject(response);
            JSONArray choices = result.getJSONArray("choices");
            if (choices != null && !choices.isEmpty()) {
                JSONObject message = choices.getJSONObject(0).getJSONObject("message");
                if (message != null) {
                    String rawContent = message.getString("content");
                    log.info("========== AI 原始返回完整内容 ==========");
                    log.info("{}", rawContent);
                    log.info("==========================================");

                    // 先提取 <|ref|> 中的内容
                    String extracted = extractRefContent(rawContent);

                    // 尝试解析为 JSON，如果不是 JSON 则尝试转换
                    String jsonResult = convertToJson(extracted);
                    log.info("JSON 结果: {}", jsonResult);
                    return jsonResult;
                }
            }
            return "";
        } else {
            String error = connection.getErrorStream() != null
                    ? new String(connection.getErrorStream().readAllBytes(), StandardCharsets.UTF_8)
                    : "未知错误";
            log.error("API 调用失败，响应码: {}, 错误: {}", responseCode, error);

            // 解析错误信息，给出友好提示
            String friendlyMessage = parseApiError(error, responseCode);
            throw new RuntimeException(friendlyMessage);
        }
    }

    /**
     * 解析 API 错误信息，返回友好提示
     */
    private String parseApiError(String error, int responseCode) {
        try {
            JSONObject errorJson = JSON.parseObject(error);
            if (errorJson != null) {
                Integer code = errorJson.getInteger("code");
                String message = errorJson.getString("message");

                if (code != null) {
                    switch (code) {
                        case 30001:
                            return "账户余额不足，请前往 SiliconFlow 充值后再试";
                        case 10009:
                            return "调用次数已达上限，请稍后再试";
                        case 10004:
                            return "API Key 无效或已过期，请检查配置";
                        case 10002:
                            return "参数错误，请联系开发者";
                        default:
                            return message != null ? message : "OCR 识别失败";
                    }
                }
            }
        } catch (Exception e) {
            log.warn("解析错误响应失败: {}", e.getMessage());
        }

        if (responseCode == 401) {
            return "API Key 无效或已过期，请检查配置";
        } else if (responseCode == 403) {
            return "API Key 无权限访问，请检查 API Key 是否正确";
        } else if (responseCode == 429) {
            return "请求频率超限，请稍后再试";
        }

        return "OCR 识别失败: " + error;
    }


    /**
     * 将 <|ref|> 格式的文本转换为干净的 JSON
     */
    private String convertToJson(String text) {
        if (text == null || text.isEmpty()) {
            return "[]";
        }

        List<Map<String, Object>> words = new ArrayList<>();
        String[] lines = text.split("\n");

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            // 跳过纯中文的续行（已经合并处理）
            if (line.matches("^[\u4e00-\u9fa5]") && !line.matches(".*[a-zA-Z].*")) {
                continue;
            }

            Map<String, Object> word = parseRefLine(line);
            if (word != null && isValidEntry(word)) {
                words.add(word);
            }
        }

        // 合并 Sherlock Holmes 这种被拆分的情况
        words = mergeAdjacentEntries(words);

        // 清理每个词条
        for (Map<String, Object> word : words) {
            String english = (String) word.get("english");

            // 清理英文：移除音标和词性
            english = cleanEnglishFromRef(english);

            word.put("english", english);

            // 添加类型
            int wordType = detectWordType(english);
            word.put("type", getTypeString(wordType));
        }

        return JSON.toJSONString(words);
    }

    /**
     * 解析单行 <|ref|> 内容
     */
    private Map<String, Object> parseRefLine(String line) {
        Map<String, Object> word = new HashMap<>();

        // 1. 提取音标（/.../）
        String phonetic = "";
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("/[^/]+/").matcher(line);
        if (m.find()) {
            phonetic = m.group();
            line = line.replace(phonetic, "").trim();
        }

        // 2. 分离英文和中文
        String english;
        String chinese = "";

        java.util.regex.Matcher chineseMatcher = java.util.regex.Pattern.compile("[\u4e00-\u9fa5]").matcher(line);

        if (chineseMatcher.find()) {
            int chineseStart = chineseMatcher.start();
            english = line.substring(0, chineseStart).trim();
            chinese = line.substring(chineseStart).trim();
        } else {
            english = line.trim();
        }

        // 3. 清理英文
        english = cleanEnglishFromRef(english);

        // 4. 清理中文：移除开头的括号和词性
        chinese = cleanChineseFromRef(chinese);

        if (english.isEmpty()) {
            return null;
        }

        word.put("english", english);
        word.put("chinese", chinese);
        word.put("phonetic", phonetic);

        return word;
    }


    /**
     * 清理中文部分
     */
    private String cleanChineseFromRef(String text) {
        if (text == null || text.isEmpty()) return "";

        // 移除开头的词性标注（如 "n.礼貌" -> "礼貌"）
        text = text.replaceAll("^\\s*[nvadp]\\.?\\s*", "");
        // 移除开头的 [pl.] 等
        text = text.replaceAll("^\\s*\\[.*?\\]\\s*", "");
        // 清理多余的括号（如果中文以括号开头但括号内没有内容）
        text = text.replaceAll("^[（(]\\s*[）)]", "");

        return text.trim();
    }

    /**
     * 从 <|ref|> 格式的英文中提取干净的单词
     */
    private String cleanEnglishFromRef(String text) {
        if (text == null || text.isEmpty()) return "";

        // 1. 先移除音标部分（/.../）
        text = text.replaceAll("/[^/]+/", "").trim();

        // 2. 找到第一个中文字符的位置，中文之前的部分就是英文
        java.util.regex.Matcher chineseMatcher = java.util.regex.Pattern.compile("[\u4e00-\u9fa5]").matcher(text);
        if (chineseMatcher.find()) {
            text = text.substring(0, chineseMatcher.start()).trim();
        }

        // 3. 清理英文中的词性标注（词性前面必须有空格）
        text = text.replaceAll("\\s+[nvadp]+\\.?\\s*$", "");      // n., v., a., adv., adj., prep. 等
        text = text.replaceAll("\\s+\\[.*?\\]\\s*$", "");         // [pl.] 等
        text = text.replaceAll("\\s+modal\\s+verb\\s*$", "");     // modal verb
        text = text.replaceAll("\\s+prep\\.?\\s*$", "");          // prep.

        // 4. 移除末尾的括号内容（如 "turn up（" -> "turn up"）
        text = text.replaceAll("[（(][^）)]*$", "").trim();

        // 5. 修复大小写粘连（SherlockHolmes -> Sherlock Holmes）
        text = text.replaceAll("([a-z])([A-Z])", "$1 $2");

        return text.trim();
    }





    /**
     * 合并相邻的被拆分的词条
     * 例如: "Sherlock Holmes" 只有英文，下一行是中文解释
     */
    private List<Map<String, Object>> mergeAdjacentEntries(List<Map<String, Object>> words) {
        List<Map<String, Object>> result = new ArrayList<>();

        for (int i = 0; i < words.size(); i++) {
            Map<String, Object> current = words.get(i);
            String english = (String) current.get("english");
            String chinese = (String) current.get("chinese");

            // 如果当前没有中文，且下一行只有中文，则合并
            if ((chinese == null || chinese.isEmpty()) && i + 1 < words.size()) {
                Map<String, Object> next = words.get(i + 1);
                String nextEnglish = (String) next.get("english");
                String nextChinese = (String) next.get("chinese");

                // 如果下一行只有中文（没有英文），则合并中文
                if ((nextEnglish == null || nextEnglish.isEmpty()) &&
                        nextChinese != null && !nextChinese.isEmpty()) {
                    current.put("chinese", nextChinese);
                    i++; // 跳过下一行
                }
            }

            if (isValidEntry(current)) {
                result.add(current);
            }
        }

        return result;
    }



    /**
     * 合并被错误拆分的词条（如 Sherlock Holmes 和它的中文解释）
     */
    private List<Map<String, Object>> mergeSplitEntries(List<Map<String, Object>> words) {
        List<Map<String, Object>> result = new ArrayList<>();

        for (int i = 0; i < words.size(); i++) {
            Map<String, Object> current = words.get(i);
            String english = (String) current.get("english");
            String chinese = (String) current.get("chinese");

            // 如果当前词条只有英文没有中文，且下一个词条只有中文没有英文，则合并
            if ((chinese == null || chinese.isEmpty()) && i + 1 < words.size()) {
                Map<String, Object> next = words.get(i + 1);
                String nextEnglish = (String) next.get("english");
                String nextChinese = (String) next.get("chinese");

                if ((nextEnglish == null || nextEnglish.isEmpty()) && nextChinese != null && !nextChinese.isEmpty()) {
                    // 合并：当前英文 + 下一个中文
                    current.put("chinese", nextChinese);
                    // 合并音标
                    String nextPhonetic = (String) next.get("phonetic");
                    if (nextPhonetic != null) {
                        String currentPhonetic = (String) current.get("phonetic");
                        if (currentPhonetic == null) {
                            current.put("phonetic", nextPhonetic);
                        }
                    }
                    i++; // 跳过下一个
                }
            }

            if (isValidEntry(current)) {
                result.add(current);
            }
        }

        return result;
    }





    /**
     * 清理英文部分（分离词性标注和残留字符）
     */
    private String cleanEnglish(String text) {
        if (text == null) return "";

        // 1. 移除词性标注 (n., v., adj., adv., a., prep. 等)
        text = text.replaceAll("\\s*[nvadj]+\\.?\\s*$", "");
        text = text.replaceAll("\\s*[nvadj]+\\.?\\s+", " ");

        // 2. 移除残留的单字母词性标记（如 performance n -> performance）
        text = text.replaceAll("\\s+[nvadp]\\.?$", "");

        // 3. 处理粘连的英文（如 stealtheshow -> steal the show）
        // 在大小写交界处添加空格（但保留连字符和撇号）
        // 使用更智能的方法：检测已知单词边界

        // 4. 移除多余空格
        text = text.replaceAll("\\s+", " ").trim();

        return text;
    }

    /**
     * 从混合文本中提取英文
     */
    private String extractEnglish(String text) {
        // 匹配英文字母、连字符、空格、斜杠（用于音标前）
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("^([a-zA-Z\\s\\-']+)").matcher(text);
        if (m.find()) {
            return cleanEnglish(m.group(1).trim());
        }
        return "";
    }

    /**
     * 从混合文本中提取中文
     */
    private String extractChinese(String text) {
        // 匹配中文字符
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("[\u4e00-\u9fa5].*$").matcher(text);
        if (m.find()) {
            return m.group().trim();
        }
        return "";
    }

    /**
     * 检查词条是否有效
     */
    private boolean isValidEntry(Map<String, Object> entry) {
        String english = (String) entry.get("english");
        return english != null && !english.isEmpty() && english.length() >= 2;
    }



    /**
     * 修复粘连的英文词组
     * 例如: stealtheshow -> steal the show
     */
    private String fixConcatenatedWords(String text) {
        if (text == null || text.isEmpty()) return text;

        // 常见词组字典
        String[] commonPhrases = {"steal the show", "give up", "take off", "look after"};
        for (String phrase : commonPhrases) {
            String concatenated = phrase.replaceAll("\\s+", "");
            if (text.equalsIgnoreCase(concatenated)) {
                return phrase;
            }
        }

        // 尝试在大小写边界添加空格（但保留连字符）
        // 例如: "stealTheShow" -> "steal The Show"
        text = text.replaceAll("([a-z])([A-Z])", "$1 $2");

        return text;
    }



    /**
     * 提取 <|ref|>...</|ref|> 中的内容
     */
    private String extractRefContent(String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }

        // 使用正则提取所有 <|ref|>...</|ref|> 中的文本
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("<\\|ref\\|>(.*?)<\\|/ref\\|>");
        java.util.regex.Matcher matcher = pattern.matcher(content);

        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            if (result.length() > 0) {
                result.append("\n");
            }
            result.append(matcher.group(1));
        }

        // 如果没有提取到，返回原内容
        if (result.length() == 0) {
            return content;
        }

        return result.toString();
    }
    /**
     * 保存文件并转为 Base64
     */
    private String saveAndEncodeToBase64(MultipartFile file) throws Exception {
        String profile = RuoYiConfig.getProfile();
        Path uploadPath = Paths.get(profile, "ocr");
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFilename = UUID.randomUUID().toString() + extension;

        Path filePath = uploadPath.resolve(newFilename);
        file.transferTo(filePath.toFile());

        log.info("图片保存到: {}", filePath.toAbsolutePath());

        // 读取文件并转为 Base64
        byte[] bytes = Files.readAllBytes(filePath);
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * 判断词条类型
     */
    private int detectWordType(String english) {
        if (english == null || english.isEmpty()) {
            return 1; // 默认单词
        }

        // 检查是否包含空格
        if (!english.contains(" ")) {
            return 1; // 单词
        }

        // 按空格分割计算单词数
        String[] words = english.split("\\s+");
        int wordCount = words.length;

        if (wordCount >= 5) {
            return 3; // 句子
        } else {
            return 2; // 词组
        }
    }

    /**
     * 获取类型字符串
     */
    private String getTypeString(int type) {
        switch (type) {
            case 1: return "word";
            case 2: return "phrase";
            case 3: return "sentence";
            default: return "word";
        }
    }

    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("请上传图片文件");
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("文件大小不能超过 10MB");
        }
    }
}