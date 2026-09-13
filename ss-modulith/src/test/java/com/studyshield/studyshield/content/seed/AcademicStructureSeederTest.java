package com.studyshield.studyshield.content.seed;

import com.studyshield.studyshield.content.entity.ClassLevel;
import com.studyshield.studyshield.content.entity.Subject;
import com.studyshield.studyshield.content.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The boot matrix seeder is disabled in the base test profile, so this test enables it
 * explicitly and asserts the seeded matrix: the global ordinal spine, boards with their
 * ordinal coverage, per-board labels (including duplicate labels), and the offerings grid
 * (ALL + CBSE only in v1). Fails fast if the matrix drifts.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "app.academic-structure-seeding.enabled=true",
        "app.catalog-seeding.enabled=false"
})
class AcademicStructureSeederTest {

    @Autowired
    private AcademicStructureSeeder seeder;

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

    @Test
    void seedsTheGlobalMatrixAndIsIdempotent() {
        seeder.run(new DefaultApplicationArguments(new String[0]));
        // Re-run: nothing may be duplicated or clobbered.
        seeder.run(new DefaultApplicationArguments(new String[0]));

        assertThat(classLevelRepository.findAll()).hasSize(17);
        assertThat(classLevelRepository.findByOrdinal(1)).isPresent();
        assertThat(classLevelRepository.findByOrdinal(17)).isPresent();

        assertThat(countryRepository.count()).isEqualTo(3);
        assertThat(boardRepository.count()).isEqualTo(7);
        assertThat(subjectRepository.count()).isEqualTo(7);

        // Boards cover their ordinal spans.
        List<String> codes = boardRepository.findAll().stream()
                .map(b -> b.getCode()).toList();
        assertThat(codes).containsExactlyInAnyOrder(
                "ALL", "CBSE", "MH", "ICSE", "ENG", "US", "IB");
        var all = boardRepository.findByCode("ALL").orElseThrow();
        var cbse = boardRepository.findByCode("CBSE").orElseThrow();
        var eng = boardRepository.findByCode("ENG").orElseThrow();
        var ib = boardRepository.findByCode("IB").orElseThrow();
        assertThat(all.getMinOrdinal()).isEqualTo(2);
        assertThat(eng.getMinOrdinal()).isEqualTo(2);
        assertThat(cbse.getMinOrdinal()).isEqualTo(1);
        assertThat(all.getMaxOrdinal()).isEqualTo(16);
        assertThat(cbse.getMaxOrdinal()).isEqualTo(16);

        // board_class rows: 15 + 16×3 (CBSE/MH/ICSE) + 15×3 (ENG/US/IB) = 15+48+45 = 108
        assertThat(boardClassRepository.count()).isEqualTo(108);
        // Identity constraint is (board, ordinal) — duplicate labels are allowed.
        assertThat(countBoardClassLabels(eng, "Nursery")).isEqualTo(2);
        assertThat(countBoardClassLabels(eng, "Year 7")).isEqualTo(1);
        assertThat(countBoardClassLabels(boardRepository.findByCode("US").orElseThrow(), "Preschool"))
                .isEqualTo(2);
        assertThat(countBoardClassLabels(cbse, "Class 3")).isEqualTo(1);

        // CBSE label at the correct ordinal: "Class 3" ↔ ordinal 7.
        assertThat(boardClassRepository.findByBoard_IdAndClassLevel_Ordinal(cbse.getId(), 7)
                .orElseThrow().getDisplayName()).isEqualTo("Class 3");

        // Offerings: ALL ordinals 2–9 → 3 subjects, 10–16 → 5 subjects (24+35=59);
        // CBSE ordinal 1–9 → 27, 10–16 → 35 (total 62). Global total = 121.
        assertThat(boardClassSubjectRepository.count()).isEqualTo(121);
        Subject math = subjectRepository.findByCode("MATH").orElseThrow();
        Subject sci = subjectRepository.findByCode("SCI").orElseThrow();

        long allMath = boardClassSubjectRepository.findByBoardCodeAndOrdinal("ALL", 7).stream()
                .filter(o -> o.getSubject().getId().equals(math.getId())).count();
        assertThat(allMath).isEqualTo(1);
        assertThat(boardClassSubjectRepository.findByBoardCodeAndOrdinal("ALL", 7)).hasSize(3);
        assertThat(boardClassSubjectRepository.findByBoardCodeAndOrdinal("ALL", 10)).hasSize(5);
        assertThat(boardClassSubjectRepository.findByBoardCodeAndOrdinal("CBSE", 16)).hasSize(5);

        // No offerings for MH/ENG/US/IB (fallback to ALL at request time).
        assertThat(boardClassSubjectRepository.findByBoardCodeAndOrdinal("MH", 7)).isEmpty();
        assertThat(boardClassSubjectRepository.findByBoardCodeAndOrdinal("ENG", 7)).isEmpty();

        // Class levels carry the expected spine metadata.
        ClassLevel nursery = classLevelRepository.findByOrdinal(2).orElseThrow();
        assertThat(nursery.getCanonicalName()).isEqualTo("Nursery");
        assertThat(nursery.getNotes()).contains("Trial/Exp");
    }

    private long countBoardClassLabels(com.studyshield.studyshield.content.entity.Board board, String displayName) {
        return boardClassRepository.findByBoardBoardCodeAndDisplayNameEnumerable(board.getCode(), displayName)
                .stream()
                .filter(bc -> bc.getBoard().getId().equals(board.getId()))
                .count();
    }
}