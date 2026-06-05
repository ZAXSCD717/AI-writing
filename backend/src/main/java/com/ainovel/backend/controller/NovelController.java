package com.ainovel.backend.controller;

import com.ainovel.backend.common.R;
import com.ainovel.backend.model.dto.ConversionRequest;
import com.ainovel.backend.model.dto.NovelResponse;
import com.ainovel.backend.model.dto.NovelUploadRequest;
import com.ainovel.backend.model.dto.ScriptResponse;
import com.ainovel.backend.service.NovelService;
import com.ainovel.backend.service.ScriptService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/novels")
@RequiredArgsConstructor
public class NovelController {

    private final NovelService novelService;
    private final ScriptService scriptService;

    /**
     * Upload a novel
     */
    @PostMapping
    public R<NovelResponse> uploadNovel(@Valid @RequestBody NovelUploadRequest request) {
        NovelResponse response = novelService.uploadNovel(request);
        return R.ok(response);
    }

    /**
     * Get novel by ID
     */
    @GetMapping("/{id}")
    public R<NovelResponse> getNovel(@PathVariable Long id) {
        NovelResponse response = novelService.getNovelResponse(id);
        if (response == null) {
            return R.error(404, "小说不存在");
        }
        return R.ok(response);
    }

    /**
     * List novels with pagination
     */
    @GetMapping
    public R<IPage<NovelResponse>> listNovels(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        IPage<NovelResponse> result = novelService.listNovels(page, size);
        return R.ok(result);
    }

    /**
     * Convert novel to screenplay (initiates AI conversion)
     */
    @PostMapping("/{id}/convert")
    public R<ScriptResponse> convertNovel(@PathVariable Long id) {
        ScriptResponse response = scriptService.convertNovelToScript(id);
        return R.ok(response);
    }
}
