package com.ainovel.backend.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ConversionRequest {

    @NotNull(message = "小说ID不能为空")
    private Long novelId;
}
