package com.ainovel.backend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Script entity - stores generated screenplay in YAML format
 */
@Data
@TableName("script")
public class Script {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long novelId;

    private String title;

    private String description;

    private String yamlContent;

    private String version;

    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
