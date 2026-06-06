package com.ainovel.backend.service;

import com.ainovel.backend.model.dto.ScriptResponse;
import com.ainovel.backend.model.dto.ScriptUpdateRequest;
import com.ainovel.backend.model.entity.Script;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

public interface ScriptService {

    /**
     * Generate screenplay from novel using AI (all chapters)
     */
    ScriptResponse convertNovelToScript(Long novelId);

    /**
     * Generate screenplay from novel using AI (selected chapters)
     */
    ScriptResponse convertNovelToScript(Long novelId, List<Integer> chapterIndices);

    /**
     * Get script by ID
     */
    Script getScriptById(Long id);

    /**
     * Get script response by ID
     */
    ScriptResponse getScriptResponse(Long id);

    /**
     * Update script content (manual editing)
     */
    ScriptResponse updateScript(Long id, ScriptUpdateRequest request);

    /**
     * Export script as YAML string
     */
    String exportScriptYaml(Long id);

    /**
     * List scripts with pagination
     */
    IPage<ScriptResponse> listScripts(int page, int size);
}
