package com.ainovel.backend.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ScriptUpdateRequest {

    private String title;

    private String description;

    @NotBlank(message = "YAML内容不能为空")
    private String yamlContent;
}
