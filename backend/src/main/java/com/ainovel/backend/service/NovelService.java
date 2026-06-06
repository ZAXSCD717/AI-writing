package com.ainovel.backend.service;

import com.ainovel.backend.model.dto.ChapterResponse;
import com.ainovel.backend.model.dto.NovelResponse;
import com.ainovel.backend.model.dto.NovelUploadRequest;
import com.ainovel.backend.model.entity.Novel;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

public interface NovelService {

    /**
     * Upload a new novel
     */
    NovelResponse uploadNovel(NovelUploadRequest request);

    /**
     * Get novel by ID
     */
    Novel getNovelById(Long id);

    /**
     * Get novel response by ID
     */
    NovelResponse getNovelResponse(Long id);

    /**
     * List novels with pagination
     */
    IPage<NovelResponse> listNovels(int page, int size);

    /**
     * Parse novel content into chapter list
     */
    List<ChapterResponse> getChapters(Long novelId);
}
