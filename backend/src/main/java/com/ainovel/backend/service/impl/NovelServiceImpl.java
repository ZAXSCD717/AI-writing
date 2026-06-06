package com.ainovel.backend.service.impl;

import com.ainovel.backend.mapper.NovelMapper;
import com.ainovel.backend.model.dto.ChapterResponse;
import com.ainovel.backend.model.dto.NovelResponse;
import com.ainovel.backend.model.dto.NovelUploadRequest;
import com.ainovel.backend.model.entity.Novel;
import com.ainovel.backend.service.NovelService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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

    @Override
    public List<ChapterResponse> getChapters(Long novelId) {
        Novel novel = novelMapper.selectById(novelId);
        if (novel == null) {
            return List.of();
        }
        return splitIntoChapters(novel.getContent());
    }

    /**
     * Split novel content into chapters with index, title, preview.
     */
    private List<ChapterResponse> splitIntoChapters(String content) {
        List<ChapterResponse> chapters = new ArrayList<>();
        if (content == null || content.isBlank()) {
            return chapters;
        }

        String[] lines = content.split("\n", -1);

        // Find chapter boundaries
        List<Integer> chapterStartLines = new ArrayList<>();
        for (int i = 0; i < lines.length; i++) {
            String trimmed = lines[i].trim();
            if (isChapterHeading(trimmed)) {
                chapterStartLines.add(i);
            }
        }

        // If no chapter markers found, split by paragraphs of >200 chars
        if (chapterStartLines.isEmpty()) {
            List<Integer> paraBreaks = new ArrayList<>();
            paraBreaks.add(0);
            for (int i = 0; i < lines.length; i++) {
                if (lines[i].trim().length() > 200) {
                    paraBreaks.add(i);
                }
            }
            // Remove consecutive duplicates
            List<Integer> unique = new ArrayList<>();
            for (int i = 0; i < paraBreaks.size() - 1; i++) {
                if (!paraBreaks.get(i).equals(paraBreaks.get(i + 1))) {
                    unique.add(paraBreaks.get(i));
                }
            }
            unique.add(paraBreaks.get(paraBreaks.size() - 1));

            for (int i = 0; i < unique.size(); i++) {
                int start = unique.get(i);
                int end = (i + 1 < unique.size()) ? unique.get(i + 1) : lines.length;
                String chapterContent = extractLines(lines, start, end);
                if (!chapterContent.isBlank()) {
                    chapters.add(ChapterResponse.builder()
                            .index(i)
                            .title("第" + (i + 1) + "部分")
                            .preview(getPreview(chapterContent))
                            .charCount(chapterContent.length())
                            .selected(true)
                            .build());
                }
            }
            return chapters;
        }

        // Build chapters from detected heading boundaries
        for (int i = 0; i < chapterStartLines.size(); i++) {
            int startLine = chapterStartLines.get(i);
            int endLine = (i + 1 < chapterStartLines.size()) ? chapterStartLines.get(i + 1) : lines.length;
            String chapterTitle = lines[startLine].trim();
            String chapterContent = extractLines(lines, startLine + 1, endLine);

            chapters.add(ChapterResponse.builder()
                    .index(i)
                    .title(chapterTitle)
                    .preview(getPreview(chapterContent))
                    .charCount(chapterContent.length())
                    .selected(true)
                    .build());
        }

        return chapters;
    }

    private boolean isChapterHeading(String line) {
        if (line.matches("^第[一二三四五六七八九十百千零\\d]+[章节部].*")) return true;
        if (line.matches("^[Cc]hapter\\s+\\d+.*")) return true;
        if (line.matches("^[0-9]+\\.\\s+.*")) return true;
        return false;
    }

    private String extractLines(String[] lines, int start, int end) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < end && i < lines.length; i++) {
            sb.append(lines[i]).append("\n");
        }
        return sb.toString().trim();
    }

    private String getPreview(String content) {
        if (content == null || content.isBlank()) return "(空)";
        // Take first 80 non-whitespace chars
        String plain = content.replaceAll("\\s+", " ").trim();
        return plain.length() > 80 ? plain.substring(0, 80) + "..." : plain;
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
