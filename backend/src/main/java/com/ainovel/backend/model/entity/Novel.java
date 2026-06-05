package com.ainovel.backend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Novel entity - stores uploaded novel content
 */
@Data
@TableName("novel")
public class Novel {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String author;

    private String content;

    private Integer chapters;

    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
