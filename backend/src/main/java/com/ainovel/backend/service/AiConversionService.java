package com.ainovel.backend.service;

import com.ainovel.backend.model.entity.Novel;

import java.util.List;

public interface AiConversionService {

    /**
     * Convert novel text to screenplay YAML using DeepSeek API
     */
    AiConversionResult convertNovelToScript(Novel novel);

    /**
     * Convert selected chapters of a novel to screenplay YAML
     *
     * @param novel the novel entity
     * @param chapterTexts list of chapter text contents to convert
     */
    AiConversionResult convertNovelToScript(Novel novel, List<String> chapterTexts);
}
