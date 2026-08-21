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
    void theBankHasEnoughQuestionsForTheTwoAgeBands() {
        long total = QuestionBankContent.BANK.values().stream()
                .flatMap(s -> s.values().stream())
                .mapToLong(List::size)
                .sum();
        assertThat(total).as("total curated questions across Sr KG and Class 1").isBetween(80L, 150L);
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
