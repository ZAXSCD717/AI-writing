package com.ainovel.backend.service.impl;

import com.ainovel.backend.config.AiPrompts;
import com.ainovel.backend.model.entity.Novel;
import com.ainovel.backend.service.AiConversionResult;
import com.ainovel.backend.service.AiConversionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AiConversionServiceImpl implements AiConversionService {

    /** DeepSeek has 1M token context — ~700K chars is a safe limit reserving room for output */
    private static final int MAX_CONTENT_CHARS = 700_000;

    private final ChatClient chatClient;

    public AiConversionServiceImpl(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public AiConversionResult convertNovelToScript(Novel novel) {
        // Split into individual chapter texts for compression logic
        List<String> chapterTexts = new ArrayList<>();
        // Try to split by chapter markers (same logic as NovelServiceImpl)
        String content = novel.getContent();
        String[] lines = content.split("\n", -1);
        StringBuilder currentChapter = new StringBuilder();
        boolean hasChapterMarker = false;
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.matches("^第[一二三四五六七八九十百千零\\d]+[章节部].*") ||
                trimmed.matches("^[Cc]hapter\\s+\\d+.*")) {
                if (hasChapterMarker && currentChapter.length() > 0) {
                    chapterTexts.add(currentChapter.toString().trim());
                    currentChapter = new StringBuilder();
                }
                hasChapterMarker = true;
            }
            currentChapter.append(line).append("\n");
        }
        if (currentChapter.length() > 0) {
            chapterTexts.add(currentChapter.toString().trim());
        }
        // Fallback: single chunk
        if (!hasChapterMarker && chapterTexts.isEmpty()) {
            chapterTexts = List.of(content);
        }

        return convertNovelToScript(novel, chapterTexts);
    }

    @Override
    public AiConversionResult convertNovelToScript(Novel novel, List<String> chapterTexts) {
        int chapterCount = chapterTexts.size();
        log.info("Sending novel '{}' ({} chapters selected) to AI for conversion...",
                novel.getTitle(), chapterCount);

        // Check total length and compress with AI if needed
        boolean compressed = false;
        int totalChars = chapterTexts.stream().mapToInt(String::length).sum();

        if (totalChars > MAX_CONTENT_CHARS) {
            log.warn("Novel content too long ({} chars across {} chapters), compressing with AI...",
                    totalChars, chapterCount);
            String compressedContent = compressContent(novel, chapterTexts, chapterCount);
            chapterTexts = List.of(compressedContent);
            compressed = true;
            log.info("AI compression complete: {} chars -> {} chars",
                    totalChars, compressedContent.length());
        }

        String joinedContent = chapterTexts.size() == 1
                ? chapterTexts.get(0)
                : chapterTexts.stream().collect(Collectors.joining("\n\n---\n\n"));

        String userPrompt = buildUserPrompt(
                novel.getTitle(), novel.getAuthor(), chapterCount, joinedContent, compressed
        );

        try {
            String response = chatClient.prompt()
                    .system(AiPrompts.SYSTEM_PROMPT)
                    .user(userPrompt)
                    .call()
                    .content();

            if (response == null || response.isBlank()) {
                throw new RuntimeException("AI 返回为空");
            }

            String yaml = extractYaml(response);

            log.info("Successfully converted novel '{}' to screenplay ({} chars, compressed={})",
                    novel.getTitle(), yaml.length(), compressed);

            return AiConversionResult.builder()
                    .yamlContent(yaml)
                    .compressed(compressed)
                    .build();

        } catch (Exception e) {
            log.error("AI conversion failed for novel '{}': {}", novel.getTitle(), e.getMessage());
            throw new RuntimeException("AI 转换失败: " + e.getMessage(), e);
        }
    }

    /**
     * Compress novel content using AI to fit within context limits.
     * Sends all chapters in one batch call and returns the compressed result.
     */
    private String compressContent(Novel novel, List<String> chapterTexts, int chapterCount) {
        String content = chapterTexts.stream().collect(Collectors.joining("\n\n---\n\n"));
        String prompt = buildCompressionPrompt(
                novel.getTitle(), novel.getAuthor(), chapterCount, content
        );

        String compressionSystemPrompt = """
                你是一个小说内容压缩专家。你的任务是将小说内容压缩精简，
                同时保留所有核心情节、角色关系和场景结构。
                每个章节压缩到约800字，保留章节标题。
                确保压缩后的内容叙事连贯、情节完整。
                """;

        try {
            String response = chatClient.prompt()
                    .system(compressionSystemPrompt)
                    .user(prompt)
                    .call()
                    .content();

            if (response == null || response.isBlank()) {
                throw new RuntimeException("AI 压缩返回为空");
            }

            return response.trim();

        } catch (Exception e) {
            log.error("AI compression failed for novel '{}': {}", novel.getTitle(), e.getMessage());
            throw new RuntimeException("AI 压缩失败: " + e.getMessage(), e);
        }
    }

    private String buildCompressionPrompt(String title, String author, int chapters, String content) {
        return String.format("""
                请将以下小说内容压缩。
                
                要求：
                - 保留所有章节标题和结构
                - 每个章节压缩到约800字
                - 保留核心情节推进、关键对话、角色互动和场景变化
                - 确保压缩后的内容叙事连贯、情节完整
                
                小说标题：%s
                作者：%s
                章节数：约 %d 章
                
                小说正文：
                
                %s
                
                请直接返回压缩后的完整小说内容，不要添加任何额外说明。
                """,
                title,
                author,
                chapters,
                content
        );
    }

    /**
     * Build the user prompt with novel content.
     */
    private String buildUserPrompt(String title, String author, int chapters, String content, boolean compressed) {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String compressionNote = compressed
                ? "\n\n> ⚠️ 注意：由于小说内容过长，系统已自动调用 AI 压缩章节内容（每章约800字），剧本生成可能遗漏部分细节，建议生成后检查补充。\n"
                : "";

        return String.format("""
                请将以下小说改编为剧本。
                
                ## 小说信息
                - 标题：%s
                - 作者：%s
                - 章节数：约 %d 章
                - 当前日期：%s
                %s
                ## 小说正文
                
                %s
                """,
                title,
                author,
                chapters,
                today,
                compressionNote,
                content
        );
    }

    /**
     * Extract YAML content from AI response.
     * Handles cases where AI wraps YAML in markdown code blocks.
     */
    private String extractYaml(String response) {
        // Try to extract content between ```yaml ... ``` markers
        int yamlStart = response.indexOf("```yaml");
        if (yamlStart >= 0) {
            int contentStart = yamlStart + 7; // length of "```yaml"
            int yamlEnd = response.indexOf("```", contentStart);
            if (yamlEnd >= 0) {
                return response.substring(contentStart, yamlEnd).trim();
            }
        }

        // Try ```yaml\n ... \n```
        int codeBlockStart = response.indexOf("```");
        if (codeBlockStart >= 0) {
            int contentStart = response.indexOf('\n', codeBlockStart) + 1;
            int codeBlockEnd = response.indexOf("```", contentStart);
            if (codeBlockEnd >= 0) {
                return response.substring(contentStart, codeBlockEnd).trim();
            }
        }

        // If no code block markers, return the response as-is (trimmed)
        return response.trim();
    }
}
