package com.studyshield.studyshield.content.seed;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Business-readable checks on the seeded question bank (issue #1).
 * Each test describes a promise a parent or content reviewer cares about.
 */
class QuestionBankContentTest {

    @Test
    void theBankHasEnoughQuestionsForAllCuratedBands() {
        long total = QuestionBankContent.BANK.values().stream()
                .flatMap(s -> s.values().stream())
                .mapToLong(List::size)
                .sum();
        assertThat(total).as("total curated questions across all bands").isGreaterThanOrEqualTo(400L);
    }

    @Test
    void everyClassBandFrom2To10CoversAllFourSubjects() {
        for (int n = QuestionBankContent.MIN_CURATED_CLASS; n <= QuestionBankContent.MAX_CURATED_CLASS; n++) {
            var band = QuestionBankContent.BANK.get("Class " + n);
            assertThat(band).as("band Class %d", n).isNotNull();
            assertThat(band).as("subjects of Class %d", n)
                    .containsKeys("Math", "EVS", "English", "General Knowledge");
        }
    }

    @Test
    void everyClassBandHasAtLeastTenQuestionsPerSubjectSoQuizzesFillUp() {
        for (String bandKey : QuestionBankContent.CLASS_BANDS) {
            for (var bank : QuestionBankContent.BANK.get(bandKey).values()) {
                assertThat(bank.size()).as("questions in %s", bandKey).isGreaterThanOrEqualTo(10);
            }
        }
    }

    @Test
    void srKgAndClass1BothCoverAllFourSubjects() {
        assertThat(QuestionBankContent.BANK.get(QuestionBankContent.BAND_SR_KG))
                .containsKeys("Math", "EVS", "English", "General Knowledge");
        assertThat(QuestionBankContent.BANK.get(QuestionBankContent.BAND_CLASS_1))
                .containsKeys("Math", "EVS", "English", "General Knowledge");
    }

    @Test
    void everyQuestionIsSingleChoiceOrTrueFalse() {
        for (QuestionBankContent.SeedQuestion q : allQuestions()) {
            if (q.trueFalse()) {
                assertThat(q.options()).as(q.text()).containsExactly("True", "False");
            } else {
                assertThat(q.options()).as(q.text()).hasSize(4);
            }
        }
    }

    @Test
    void everyQuestionHasExactlyOneAnswerThatIsOneOfItsOptions() {
        for (QuestionBankContent.SeedQuestion q : allQuestions()) {
            assertThat(q.correct()).as(q.text()).isIn(q.options());
        }
    }

    @Test
    void questionsAreAgeAppropriateAndNotPlaceholders() {
        for (QuestionBankContent.SeedQuestion q : allQuestions()) {
            assertThat(q.text()).as(q.text()).doesNotContain("Option A", "Sample question");
            assertThat(q.text().length()).as(q.text()).isGreaterThan(8);
        }
    }

    @Test
    void noDuplicateQuestionsWithinABand() {
        for (var band : QuestionBankContent.BANK.values()) {
            Set<String> seen = new HashSet<>();
            for (List<QuestionBankContent.SeedQuestion> bank : band.values()) {
                for (QuestionBankContent.SeedQuestion q : bank) {
                    assertThat(seen.add(q.text())).as("duplicate: " + q.text()).isTrue();
                }
            }
        }
    }

    @Test
    void aChildsAgeMapsToAReasonableClass() {
        assertThat(QuestionBankContent.classNameForAge(4)).isEqualTo("Sr KG");
        assertThat(QuestionBankContent.classNameForAge(5)).isEqualTo("Sr KG");
        assertThat(QuestionBankContent.classNameForAge(6)).isEqualTo("Class 1");
        assertThat(QuestionBankContent.classNameForAge(7)).isEqualTo("Class 1");
        assertThat(QuestionBankContent.classNameForAge(9)).isEqualTo("Class 4");
    }

    @Test
    void theExpPromoBandHasEnoughQuestionsForEveryQuiz() {
        var welcome = QuestionBankContent.BANK.get(QuestionBankContent.BAND_EXP).get("Welcome");
        assertThat(welcome).as("Exp Welcome bank").isNotNull();
        // 5 freemium quizzes, at least 3 active questions each before a session can start
        assertThat(welcome.size()).isGreaterThanOrEqualTo(15);
    }

    @Test
    void expIsRecognizedAsACuratedBand() {
        assertThat(QuestionBankContent.bandForClassName("Exp")).isEqualTo(QuestionBankContent.BAND_EXP);
        assertThat(QuestionBankContent.bandForClassName("exp")).isEqualTo(QuestionBankContent.BAND_EXP);
        assertThat(QuestionBankContent.bandForClassName("Experimental")).isEqualTo(QuestionBankContent.BAND_EXP);
    }

    @Test
    void classes2To10AreRecognizedAsCuratedBands() {
        assertThat(QuestionBankContent.bandForClassName("Class 2")).isEqualTo("Class 2");
        assertThat(QuestionBankContent.bandForClassName("grade 7")).isEqualTo("Class 7");
        assertThat(QuestionBankContent.bandForClassName("std 10")).isEqualTo("Class 10");
        assertThat(QuestionBankContent.bandForClassName("10")).isEqualTo("Class 10");
    }

    @Test
    void theFallbackBankIsRealAndUsableSoSessionsNeverStartEmpty() {
        assertThat(QuestionBankContent.FALLBACK_BANK.size()).isGreaterThanOrEqualTo(10);
        for (QuestionBankContent.SeedQuestion q : QuestionBankContent.FALLBACK_BANK) {
            assertThat(q.correct()).as(q.text()).isIn(q.options());
        }
    }

    private List<QuestionBankContent.SeedQuestion> allQuestions() {
        return QuestionBankContent.BANK.values().stream()
                .flatMap(s -> s.values().stream())
                .flatMap(List::stream)
                .toList();
    }
}
