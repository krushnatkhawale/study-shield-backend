package com.studyshield.studyshield.content.academicstructure;

import com.fasterxml.jackson.databind.JsonNode;
import com.studyshield.studyshield.content.academicstructure.dto.LoadReport;
import com.studyshield.studyshield.content.academicstructure.dto.PurgeReport;
import com.studyshield.studyshield.content.academicstructure.dto.StructureStatus;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Academic-structure data loop (ADMIN only, enforced in SecurityConfig):
 * <pre>
 * POST   /api/v1/academic-structure/load/{entity?}   initialize from GitHub/upload (?dryRun=true)
 * GET    /api/v1/academic-structure/export/{entity?} snapshot as load-schema JSON (download)
 * DELETE /api/v1/academic-structure/{entity}         purge rows (?confirm=true required)
 * GET    /api/v1/academic-structure/status           row counts + pinned ref
 * </pre>
 * Entity is one of class-levels | countries | boards | board-classes | subjects |
 * offerings; omitted or {@code all} covers every entity in FK order.
 */
@RestController
@RequestMapping("/api/v1/academic-structure")
public class AcademicStructureController {

    private final AcademicStructureLoadService loadService;
    private final AcademicStructureExportService exportService;
    private final AcademicStructurePurgeService purgeService;
    private final AcademicStructureFileSource fileSource;
    private final AcademicDataProperties properties;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    public AcademicStructureController(
            AcademicStructureLoadService loadService,
            AcademicStructureExportService exportService,
            AcademicStructurePurgeService purgeService,
            AcademicStructureFileSource fileSource,
            AcademicDataProperties properties,
            com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        this.loadService = loadService;
        this.exportService = exportService;
        this.purgeService = purgeService;
        this.fileSource = fileSource;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @PostMapping({"/load", "/load/{entity}"})
    public LoadReport load(
            @PathVariable(required = false) String entity,
            @RequestParam(defaultValue = "false") boolean dryRun,
            @RequestBody(required = false) JsonNode body) {
        return loadService.load(entity, dryRun, body);
    }

    @GetMapping({"/export", "/export/{entity}"})
    public ResponseEntity<?> export(@PathVariable(required = false) String entity) {
        if (entity == null || entity.isBlank() || "all".equalsIgnoreCase(entity.trim())) {
            return ResponseEntity.ok(exportService.exportAll());
        }
        AcademicStructureEntity resolved = AcademicStructureEntity.fromKey(entity)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown academic-structure entity '" + entity + "'"));
        List<?> rows = exportService.exportEntity(resolved);
        byte[] json = toPrettyJson(rows);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(resolved.key() + ".json", StandardCharsets.UTF_8)
                .build());
        return ResponseEntity.ok().headers(headers).body(json);
    }

    @GetMapping("/status")
    public StructureStatus status() {
        List<StructureStatus.EntityCount> counts = loadService
                .currentCounts(List.of(AcademicStructureEntity.values()))
                .entrySet().stream()
                .map(e -> new StructureStatus.EntityCount(e.getKey().key(), e.getValue()))
                .toList();
        return new StructureStatus(
                fileSource.describe(),
                properties.getRepo(),
                properties.getRef(),
                counts);
    }

    @DeleteMapping("/{entity}")
    public PurgeReport purge(
            @PathVariable String entity,
            @RequestParam(defaultValue = "false") boolean confirm) {
        return purgeService.purge(entity, confirm);
    }

    private byte[] toPrettyJson(Object value) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(value);
        } catch (Exception ex) {
            throw new IllegalStateException("Could not serialize export", ex);
        }
    }
}
