package com.ainovel.backend.service.impl;

import com.ainovel.backend.mapper.NovelMapper;
import com.ainovel.backend.mapper.ScriptMapper;
import com.ainovel.backend.model.dto.ScriptResponse;
import com.ainovel.backend.model.dto.ScriptUpdateRequest;
import com.ainovel.backend.model.entity.Novel;
import com.ainovel.backend.model.entity.Script;
import com.ainovel.backend.service.AiConversionService;
import com.ainovel.backend.service.NovelService;
import com.ainovel.backend.service.ScriptService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScriptServiceImpl implements ScriptService {

    private final ScriptMapper scriptMapper;
    private final NovelMapper novelMapper;
    private final NovelService novelService;
    private final AiConversionService aiConversionService;

    @Override
    public ScriptResponse convertNovelToScript(Long novelId) {
        Novel novel = novelService.getNovelById(novelId);
        if (novel == null) {
            throw new RuntimeException("小说不存在: " + novelId);
        }

        // Call AI to convert novel to screenplay
        String yamlContent = aiConversionService.convertNovelToScript(novel);

        // Save script
        Script script = new Script();
        script.setNovelId(novelId);
        script.setTitle(novel.getTitle() + "·剧本版");
        script.setDescription("基于《" + novel.getTitle() + "》的AI改编剧本");
        script.setYamlContent(yamlContent);
        script.setVersion("1.0.0");
        script.setStatus("draft");

        scriptMapper.insert(script);

        // Update novel status to converted
        Novel updateTarget = new Novel();
        updateTarget.setId(novelId);
        updateTarget.setStatus("converted");
        novelMapper.updateById(updateTarget);

        return toResponse(script, novel.getTitle());
    }

    @Override
    public Script getScriptById(Long id) {
        return scriptMapper.selectById(id);
    }

    @Override
    public ScriptResponse getScriptResponse(Long id) {
        Script script = scriptMapper.selectById(id);
        if (script == null) {
            return null;
        }
        Novel novel = novelService.getNovelById(script.getNovelId());
        return toResponse(script, novel != null ? novel.getTitle() : "");
    }

    @Override
    public ScriptResponse updateScript(Long id, ScriptUpdateRequest request) {
        Script script = scriptMapper.selectById(id);
        if (script == null) {
            throw new RuntimeException("剧本不存在: " + id);
        }

        if (request.getTitle() != null) {
            script.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            script.setDescription(request.getDescription());
        }
        if (request.getYamlContent() != null) {
            script.setYamlContent(request.getYamlContent());
        }

        scriptMapper.updateById(script);

        Novel novel = novelService.getNovelById(script.getNovelId());
        return toResponse(script, novel != null ? novel.getTitle() : "");
    }

    @Override
    public String exportScriptYaml(Long id) {
        Script script = scriptMapper.selectById(id);
        if (script == null) {
            throw new RuntimeException("剧本不存在: " + id);
        }
        return script.getYamlContent();
    }

    @Override
    public IPage<ScriptResponse> listScripts(int page, int size) {
        Page<Script> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Script> wrapper = new LambdaQueryWrapper<Script>()
                .orderByDesc(Script::getCreatedAt);

        IPage<Script> scriptPage = scriptMapper.selectPage(pageParam, wrapper);
        return scriptPage.convert(script -> {
            Novel novel = novelService.getNovelById(script.getNovelId());
            return toResponse(script, novel != null ? novel.getTitle() : "");
        });
    }

    private ScriptResponse toResponse(Script script, String novelTitle) {
        return ScriptResponse.builder()
                .id(script.getId())
                .novelId(script.getNovelId())
                .novelTitle(novelTitle)
                .title(script.getTitle())
                .description(script.getDescription())
                .yamlContent(script.getYamlContent())
                .version(script.getVersion())
                .status(script.getStatus())
                .createdAt(script.getCreatedAt())
                .updatedAt(script.getUpdatedAt())
                .build();
    }
}
