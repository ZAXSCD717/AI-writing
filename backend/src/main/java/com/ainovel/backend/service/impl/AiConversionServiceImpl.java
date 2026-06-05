package com.ainovel.backend.service.impl;

import com.ainovel.backend.config.AiPrompts;
import com.ainovel.backend.model.entity.Novel;
import com.ainovel.backend.service.AiConversionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class AiConversionServiceImpl implements AiConversionService {

    private final ChatClient chatClient;

    public AiConversionServiceImpl(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public String convertNovelToScript(Novel novel) {
        String userPrompt = buildUserPrompt(novel);

        log.info("Sending novel '{}' ({} chapters) to AI for conversion...",
                novel.getTitle(), novel.getChapters());

        try {
            String response = chatClient.prompt()
                    .system(AiPrompts.SYSTEM_PROMPT)
                    .user(userPrompt)
                    .call()
                    .content();

            if (response == null || response.isBlank()) {
                throw new RuntimeException("AI 返回为空");
            }

            // Extract YAML from the response (in case AI wraps it in markdown code blocks)
            String yaml = extractYaml(response);

            log.info("Successfully converted novel '{}' to screenplay ({} chars)",
                    novel.getTitle(), yaml.length());

            return yaml;

        } catch (Exception e) {
            log.error("AI conversion failed for novel '{}': {}", novel.getTitle(), e.getMessage());
            throw new RuntimeException("AI 转换失败: " + e.getMessage(), e);
        }
    }

    /**
     * Build the user prompt with novel content.
     * If the novel is very long, we truncate to fit within token limits.
     */
    private String buildUserPrompt(Novel novel) {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String content = novel.getContent();

        // Truncate if too long (DeepSeek has ~64K context, but we reserve tokens for output)
        int maxContentLength = 30000;
        if (content.length() > maxContentLength) {
            log.warn("Novel content too long ({} chars), truncating to {} chars",
                    content.length(), maxContentLength);
            content = content.substring(0, maxContentLength)
                    + "\n\n[注意：小说内容过长，已截断。请基于以上内容完成剧本改编。]";
        }

        return String.format("""
                请将以下小说改编为剧本。
                
                ## 小说信息
                - 标题：%s
                - 作者：%s
                - 章节数：约 %d 章
                - 当前日期：%s
                
                ## 小说正文
                
                %s
                """,
                novel.getTitle(),
                novel.getAuthor(),
                novel.getChapters(),
                today,
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
