package com.ainovel.backend.service;

import com.ainovel.backend.model.entity.Novel;

public interface AiConversionService {

    /**
     * Convert novel text to screenplay YAML using DeepSeek API
     */
    String convertNovelToScript(Novel novel);
}
