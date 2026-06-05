package com.ainovel.backend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NovelResponse {

    private Long id;
    private String title;
    private String author;
    private Integer chapters;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
