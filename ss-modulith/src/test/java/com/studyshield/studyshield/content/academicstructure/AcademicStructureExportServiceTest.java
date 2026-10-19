package com.studyshield.studyshield.content.academicstructure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyshield.studyshield.content.repository.BoardClassRepository;
import com.studyshield.studyshield.content.repository.BoardClassSubjectRepository;
import com.studyshield.studyshield.content.repository.BoardRepository;
import com.studyshield.studyshield.content.repository.ClassLevelRepository;
import com.studyshield.studyshield.content.repository.CountryRepository;
import com.studyshield.studyshield.content.repository.SubjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Export writes back exactly what load consumes: same schema, same rows, sorted by id,
 * so {@code load(export(x))} round-trips and git diffs stay clean. Empty tables are
 * a conflict — there is nothing to back up.
 */
@SpringBootTest
@ActiveProfiles("test")
class AcademicStructureExportServiceTest {

    @Autowired
    private AcademicStructureLoadService loadService;
    @Autowired
    private AcademicStructureExportService exportService;
    @Autowired
    private AcademicStructurePurgeService purgeService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ClassLevelRepository classLevelRepository;
    @Autowired
    private CountryRepository countryRepository;
    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private BoardClassRepository boardClassRepository;
    @Autowired
    private SubjectRepository subjectRepository;
    @Autowired
    private BoardClassSubjectRepository boardClassSubjectRepository;

    @BeforeEach
    void startEmpty() {
        purgeService.purge("all", true);
    }

    @Test
    void exportReproducesTheLoadedSourceRowsExactly() throws Exception {
        loadService.load(null, false, null);

        for (AcademicStructureEntity entity : AcademicStructureEntity.values()) {
            List<?> exported = exportService.exportEntity(entity);
            JsonNode expected = readClasspathFile(entity);
            JsonNode actual = objectMapper.valueToTree(exported);

            assertThat(actual.size())
                    .as("row count for " + entity.key())
                    .isEqualTo(expected.size());
            for (int i = 0; i < expected.size(); i++) {
                assertThat(withoutNulls(actual.get(i)))
                        .as(entity.key() + " row " + i + " (id order)")
                        .isEqualTo(withoutNulls(expected.get(i)));
            }
        }
    }

    @Test
    void exportAllReturnsEveryEntityInLoadOrder() {
        loadService.load(null, false, null);

        Map<String, List<?>> all = exportService.exportAll();

        assertThat(all.keySet()).containsExactly(
                "class-levels", "countries", "boards", "board-classes", "subjects", "offerings");
        assertThat(all.get("class-levels")).hasSize(17);
        assertThat(all.get("offerings")).hasSize(121);
    }

    @Test
    void exportingEmptyTablesIsAConflictNotAnEmptyFile() {
        assertThatThrownBy(() -> exportService.exportEntity(AcademicStructureEntity.BOARDS))
                .isInstanceOf(AcademicStructureConflictException.class)
                .hasMessageContaining("Nothing to export");
    }

    private JsonNode readClasspathFile(AcademicStructureEntity entity) throws Exception {
        try (var in = new ClassPathResource("academic-structure/" + entity.key() + ".json").getInputStream()) {
            return objectMapper.readTree(in);
        }
    }

    /**
     * Null-valued fields are equivalent to absent fields (non-null serialization).
     * Numbers are compared by value, not JSON shape — the fixture may write {@code 3.0}
     * where the DB round-trip returns {@code 3}.
     */
    private static JsonNode withoutNulls(JsonNode node) {
        if (!node.isObject()) {
            return node;
        }
        var factory = com.fasterxml.jackson.databind.node.JsonNodeFactory.instance;
        var sorted = factory.objectNode();
        java.util.List<String> fields = new java.util.ArrayList<>();
        node.fieldNames().forEachRemaining(fields::add);
        fields.sort(String::compareTo);
        for (String field : fields) {
            JsonNode value = node.get(field);
            if (value.isNull()) {
                continue;
            }
            if (value.isNumber()) {
                sorted.set(field, new com.fasterxml.jackson.databind.node.DecimalNode(
                        new java.math.BigDecimal(value.asText()).stripTrailingZeros()));
            } else if (value.isObject()) {
                sorted.set(field, withoutNulls(value));
            } else {
                sorted.set(field, value);
            }
        }
        return sorted;
    }
}
