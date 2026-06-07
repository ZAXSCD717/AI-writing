package com.ainovel.backend.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result of AI conversion, including the generated YAML and whether
 * the source content was compressed to fit within context limits.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiConversionResult {

    /** The generated screenplay YAML */
    private String yamlContent;

    /** Whether the source novel content was compressed before conversion */
    private boolean compressed;
}
