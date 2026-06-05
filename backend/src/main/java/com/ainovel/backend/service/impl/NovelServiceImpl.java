package com.ainovel.backend.service.impl;

import com.ainovel.backend.mapper.NovelMapper;
import com.ainovel.backend.model.dto.NovelResponse;
import com.ainovel.backend.model.dto.NovelUploadRequest;
import com.ainovel.backend.model.entity.Novel;
import com.ainovel.backend.service.NovelService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NovelServiceImpl implements NovelService {

    private final NovelMapper novelMapper;

    @Override
    public NovelResponse uploadNovel(NovelUploadRequest request) {
        // Count chapters by "第X章", "Chapter X", or double newline patterns
        String content = request.getContent();
        int chapters = countChapters(content);

        Novel novel = new Novel();
        novel.setTitle(request.getTitle());
        novel.setAuthor(request.getAuthor() != null ? request.getAuthor() : "");
        novel.setContent(content);
        novel.setChapters(chapters);
        novel.setStatus("draft");

        novelMapper.insert(novel);
        return toResponse(novel);
    }

    @Override
    public Novel getNovelById(Long id) {
        return novelMapper.selectById(id);
    }

    @Override
    public NovelResponse getNovelResponse(Long id) {
        Novel novel = novelMapper.selectById(id);
        if (novel == null) {
            return null;
        }
        return toResponse(novel);
    }

    @Override
    public IPage<NovelResponse> listNovels(int page, int size) {
        Page<Novel> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Novel> wrapper = new LambdaQueryWrapper<Novel>()
                .orderByDesc(Novel::getCreatedAt);

        IPage<Novel> novelPage = novelMapper.selectPage(pageParam, wrapper);
        return novelPage.convert(this::toResponse);
    }

    private NovelResponse toResponse(Novel novel) {
        return NovelResponse.builder()
                .id(novel.getId())
                .title(novel.getTitle())
                .author(novel.getAuthor())
                .chapters(novel.getChapters())
                .status(novel.getStatus())
                .createdAt(novel.getCreatedAt())
                .updatedAt(novel.getUpdatedAt())
                .build();
    }

    /**
     * Count chapters in novel text.
     * Supports Chinese chapter markers (第X章, 第X节) and English (Chapter X).
     */
    private int countChapters(String content) {
        if (content == null || content.isBlank()) {
            return 0;
        }

        // Count patterns like "第[一二三四五六七八九十百千零\d]+章" or "第X节" or "Chapter X"
        int count = 0;
        String[] lines = content.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.matches("^第[一二三四五六七八九十百千零\\d]+[章节部].*") ||
                    trimmed.matches("^[Cc]hapter\\s+\\d+.*") ||
                    trimmed.matches("^[0-9]+\\.\\s+.*")) {
                count++;
            }
        }

        // Fallback: if no chapter markers found, count paragraphs with more than 200 chars as chapters
        if (count == 0) {
            int paraCount = 0;
            for (String line : lines) {
                if (line.trim().length() > 200) {
                    paraCount++;
                }
            }
            count = Math.max(1, paraCount);
        }

        return count;
    }
}
