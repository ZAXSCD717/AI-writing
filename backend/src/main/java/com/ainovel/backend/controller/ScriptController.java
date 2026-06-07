package com.ainovel.backend.controller;

import com.ainovel.backend.common.R;
import com.ainovel.backend.model.dto.ScriptResponse;
import com.ainovel.backend.model.dto.ScriptUpdateRequest;
import com.ainovel.backend.service.ScriptService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/scripts")
@RequiredArgsConstructor
public class ScriptController {

    private final ScriptService scriptService;

    /**
     * Get script by ID
     */
    @GetMapping("/{id}")
    public R<ScriptResponse> getScript(@PathVariable Long id) {
        ScriptResponse response = scriptService.getScriptResponse(id);
        if (response == null) {
            return R.error(404, "剧本不存在");
        }
        return R.ok(response);
    }

    /**
     * List scripts with pagination
     */
    @GetMapping
    public R<IPage<ScriptResponse>> listScripts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        IPage<ScriptResponse> result = scriptService.listScripts(page, size);
        return R.ok(result);
    }

    /**
     * Delete a script
     */
    @DeleteMapping("/{id}")
    public R<Void> deleteScript(@PathVariable Long id) {
        scriptService.deleteScript(id);
        return R.ok();
    }

    /**
     * Update script (manual editing)
     */
    @PutMapping("/{id}")
    public R<ScriptResponse> updateScript(
            @PathVariable Long id,
            @Valid @RequestBody ScriptUpdateRequest request) {
        ScriptResponse response = scriptService.updateScript(id, request);
        return R.ok(response);
    }

    /**
     * Export script as YAML file download
     */
    @GetMapping("/{id}/export")
    public ResponseEntity<byte[]> exportScript(@PathVariable Long id) {
        String yamlContent = scriptService.exportScriptYaml(id);
        ScriptResponse script = scriptService.getScriptResponse(id);

        String filename = (script != null ? script.getTitle() : "script") + ".yaml";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/x-yaml"));
        headers.setContentDispositionFormData("attachment", filename);

        return ResponseEntity.ok()
                .headers(headers)
                .body(yamlContent.getBytes(StandardCharsets.UTF_8));
    }
}
