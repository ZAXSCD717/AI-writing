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
public class ScriptResponse {

    private Long id;
    private Long novelId;
    private String novelTitle;
    private String title;
    private String description;
    private String yamlContent;
    private String version;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
