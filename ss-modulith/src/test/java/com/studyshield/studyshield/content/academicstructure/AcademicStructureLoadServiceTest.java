package com.studyshield.studyshield.content.academicstructure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyshield.studyshield.content.academicstructure.dto.LoadReport;
import com.studyshield.studyshield.content.entity.Country;
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
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Load is initialize-only: JSON files (or an uploaded document) seed empty tables,
 * parents are never stub-created, ids round-trip, and identity sequences stay usable.
 */
@SpringBootTest
@ActiveProfiles("test")
class AcademicStructureLoadServiceTest {

    @Autowired
    private AcademicStructureLoadService loadService;
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
    void loadingAllFilesInitializesEveryStructureTable() {
        LoadReport report = loadService.load(null, false, null);

        assertThat(report.status()).isEqualTo("ok");
        assertThat(report.entities()).extracting(LoadReport.LoadEntityResult::entity)
                .containsExactly("class-levels", "countries", "boards", "board-classes", "subjects", "offerings");
        assertThat(classLevelRepository.count()).isEqualTo(17);
        assertThat(countryRepository.count()).isEqualTo(3);
        assertThat(boardRepository.count()).isEqualTo(7);
        assertThat(boardClassRepository.count()).isEqualTo(108);
        assertThat(subjectRepository.count()).isEqualTo(7);
        assertThat(boardClassSubjectRepository.count()).isEqualTo(121);
    }

    @Test
    void loadingIsRefusedWhenTablesAlreadyHaveRows() {
        loadService.load(null, false, null);

        assertThatThrownBy(() -> loadService.load(null, false, null))
                .isInstanceOf(AcademicStructureConflictException.class)
                .hasMessageContaining("initialize-only");

        assertThat(classLevelRepository.count()).isEqualTo(17);
        assertThat(boardClassSubjectRepository.count()).isEqualTo(121);
    }

    @Test
    void dryRunReportsThePlanWithoutWritingAnything() {
        LoadReport report = loadService.load(null, true, null);

        assertThat(report.status()).isEqualTo("planned");
        assertThat(report.dryRun()).isTrue();
        assertThat(report.entities()).allSatisfy(result -> {
            assertThat(result.rowsCreated()).isZero();
            assertThat(result.rowsPlanned()).isPositive();
        });
        assertThat(classLevelRepository.count()).isZero();
        assertThat(boardClassSubjectRepository.count()).isZero();
    }

    @Test
    void anUploadedDocumentLoadsInsteadOfTheSourceFiles() throws Exception {
        JsonNode uploaded = objectMapper.readTree("""
                [{"iso2":"NZ","name":"New Zealand"},{"iso2":"AU","name":"Australia"}]
                """);

        LoadReport report = loadService.load("countries", false, uploaded);

        assertThat(report.status()).isEqualTo("ok");
        assertThat(countryRepository.count()).isEqualTo(2);
        assertThat(countryRepository.findByIso2("NZ")).isPresent();
    }

    @Test
    void duplicateRowsInTheFileAreSkippedNotFailed() throws Exception {
        JsonNode uploaded = objectMapper.readTree("""
                [{"iso2":"NZ","name":"New Zealand"},
                 {"iso2":"NZ","name":"New Zealand duplicate"},
                 {"iso2":"AU","name":"Australia"}]
                """);

        LoadReport report = loadService.load("countries", false, uploaded);

        LoadReport.LoadEntityResult result = report.entities().get(0);
        assertThat(result.rowsCreated()).isEqualTo(2);
        assertThat(result.rowsSkipped()).isEqualTo(1);
        assertThat(countryRepository.findByIso2("NZ").orElseThrow().getName()).isEqualTo("New Zealand");
    }

    @Test
    void missingParentsAreReportedAndNeverStubCreated() throws Exception {
        JsonNode uploaded = objectMapper.readTree("""
                [{"boardCode":"NOPE","classOrdinal":1,"subjectCode":"MATH"}]
                """);

        assertThatThrownBy(() -> loadService.load("offerings", false, uploaded))
                .isInstanceOf(AcademicStructureConflictException.class)
                .hasMessageContaining("Parent rows absent");

        assertThat(boardClassSubjectRepository.count()).isZero();
        assertThat(boardRepository.count()).isZero();
        assertThat(subjectRepository.count()).isZero();
    }

    @Test
    void rowsMissingRequiredNaturalKeysAreRejected() throws Exception {
        JsonNode uploaded = objectMapper.readTree("[{}]");

        assertThatThrownBy(() -> loadService.load("countries", false, uploaded))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("iso2");
    }

    @Test
    void unknownEntityNamesAreRejected() {
        assertThatThrownBy(() -> loadService.load("grade-subjects", false, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown academic-structure entity");
    }

    @Test
    void identitySequencesStayUsableForNewRowsAfterAnExplicitIdLoad() {
        loadService.load(null, false, null);

        Country newRow = countryRepository.save(new Country("XX", "Testland"));

        assertThat(newRow.getId()).isGreaterThan(3);
        assertThat(countryRepository.findById(newRow.getId())).isPresent();
    }
}
