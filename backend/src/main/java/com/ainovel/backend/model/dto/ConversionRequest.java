package com.ainovel.backend.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ConversionRequest {

    @NotNull(message = "小说ID不能为空")
    private Long novelId;

    /** Chapter indices to convert (null or empty = all chapters) */
    private List<Integer> chapterIndices;
}
