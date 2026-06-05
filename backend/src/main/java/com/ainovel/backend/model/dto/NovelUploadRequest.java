package com.ainovel.backend.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NovelUploadRequest {

    @NotBlank(message = "小说标题不能为空")
    private String title;

    private String author;

    @NotBlank(message = "小说内容不能为空")
    private String content;
}
