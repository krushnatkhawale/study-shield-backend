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
                    .containsKeys("Math", "EVS", "English", "Hindi");
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
                .containsKeys("Math", "EVS", "English", "Hindi");
        assertThat(QuestionBankContent.BANK.get(QuestionBankContent.BAND_CLASS_1))
                .containsKeys("Math", "EVS", "English", "Hindi");
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
        assertThat(QuestionBankContent.classNameForAge(3)).isEqualTo("Nursery");
        assertThat(QuestionBankContent.classNameForAge(4)).isEqualTo("Junior KG");
        assertThat(QuestionBankContent.classNameForAge(5)).isEqualTo("Sr KG");
        assertThat(QuestionBankContent.classNameForAge(6)).isEqualTo("Class 1");
        assertThat(QuestionBankContent.classNameForAge(7)).isEqualTo("Class 1");
        assertThat(QuestionBankContent.classNameForAge(9)).isEqualTo("Class 4");
    }

    @Test
    void theNurseryBandHasEnoughQuestionsForEveryQuiz() {
        var nursery = QuestionBankContent.BANK.get(QuestionBankContent.BAND_NURSERY);
        assertThat(nursery).as("Nursery bank").isNotNull();
        assertThat(nursery).as("Nursery subjects")
                .containsKeys("Math", "EVS", "English", "Hindi");
        for (var subject : nursery.values()) {
            assertThat(subject.size()).isGreaterThanOrEqualTo(10);
        }
    }

    @Test
    void theJuniorKgBandHasEnoughQuestionsForEveryQuiz() {
        var lkg = QuestionBankContent.BANK.get(QuestionBankContent.BAND_LKG);
        assertThat(lkg).as("Junior KG bank").isNotNull();
        assertThat(lkg).as("Junior KG subjects")
                .containsKeys("Math", "EVS", "English", "Hindi");
        for (var subject : lkg.values()) {
            assertThat(subject.size()).isGreaterThanOrEqualTo(10);
        }
    }

    @Test
    void trialAndExpAreRecognizedAsNurseryBand() {
        assertThat(QuestionBankContent.bandForClassName("Trial")).isEqualTo(QuestionBankContent.BAND_NURSERY);
        assertThat(QuestionBankContent.bandForClassName("trial")).isEqualTo(QuestionBankContent.BAND_NURSERY);
        assertThat(QuestionBankContent.bandForClassName("Exp")).isEqualTo(QuestionBankContent.BAND_NURSERY);
        assertThat(QuestionBankContent.bandForClassName("exp")).isEqualTo(QuestionBankContent.BAND_NURSERY);
        assertThat(QuestionBankContent.bandForClassName("Experimental")).isEqualTo(QuestionBankContent.BAND_NURSERY);
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

    @Test
    void everyBandFromNurseryToClass4HasItsOwnHindiNativeQuizInDevanagari() {
        var bands = List.of("Nursery", "Junior KG", "Sr KG",
                "Class 1", "Class 2", "Class 3", "Class 4");
        var firstQuestions = new HashSet<String>();
        for (String band : bands) {
            var subjects = QuestionBankContent.BANK.get(band);
            assertThat(subjects).as("band %s", band).isNotNull();
            var hindiNative = subjects.get(QuestionBankContent.SUBJECT_HINDI_NATIVE);
            assertThat(hindiNative).as("Hindi Native in %s", band).isNotNull();
            assertThat(hindiNative.size()).as("Hindi Native questions in %s", band)
                    .isGreaterThanOrEqualTo(10);
            for (var q : hindiNative) {
                assertThat(q.text()).as(q.text()).matches(".*\\p{IsDevanagari}.*");
            }
            firstQuestions.add(hindiNative.get(0).text());
        }
        assertThat(firstQuestions.size()).as("each band opens with a different question").isEqualTo(bands.size());
    }

    @Test
    void preNurseryHasItsOwnHindiNativeTierSharingNoQuestionWithAnyOtherTier() {
        var tiers = List.of("PreNursery", "Nursery", "Junior KG", "Sr KG",
                "Class 1", "Class 2", "Class 3", "Class 4");
        var seen = new HashSet<String>();
        for (String tier : tiers) {
            var bank = QuestionBankContent.hindiNativeForBand(tier);
            assertThat(bank).as("Hindi Native tier %s", tier).isNotNull();
            assertThat(bank.size()).as("Hindi Native questions in %s", tier).isGreaterThanOrEqualTo(10);
            for (var q : bank) {
                assertThat(q.text()).as(q.text()).matches(".*\\p{IsDevanagari}.*");
                assertThat(seen.add(q.text())).as("shared question across tiers: " + q.text()).isTrue();
            }
        }
    }

    @Test
    void noQuizServesMoreThanTenQuestions() {
        for (var band : QuestionBankContent.BANK.entrySet()) {
            for (var subject : band.getValue().entrySet()) {
                assertThat(subject.getValue().size())
                        .as("questions in %s / %s", band.getKey(), subject.getKey())
                        .isLessThanOrEqualTo(QuestionBankContent.MAX_QUESTIONS_PER_QUIZ);
            }
        }
        for (String band : List.of("PreNursery", "Nursery", "Junior KG", "Sr KG",
                "Class 1", "Class 2", "Class 3", "Class 4", "Class 9", "Class 10")) {
            assertThat(QuestionBankContent.hindiNativeForBand(band).size())
                    .as("Hindi Native questions in %s", band)
                    .isLessThanOrEqualTo(QuestionBankContent.MAX_QUESTIONS_PER_QUIZ);
        }
    }

    @Test
    void languageSubjectsHaveNoCountingOrArithmeticQuestions() {
        for (var band : QuestionBankContent.BANK.entrySet()) {
            for (String subject : List.of("English", "Hindi", QuestionBankContent.SUBJECT_HINDI_NATIVE)) {
                var questions = band.getValue().get(subject);
                if (questions == null) continue;
                for (QuestionBankContent.SeedQuestion q : questions) {
                    assertThat(q.text().toLowerCase()).as("[%s/%s] %s", band.getKey(), subject, q.text())
                            .doesNotContain("how many");
                    assertThat(q.text()).as("[%s/%s] %s", band.getKey(), subject, q.text())
                            .doesNotMatch(".*\\bWhat is \\d.*");
                }
            }
        }
    }

    @Test
    void pictureQuestionsKeepThePictureInTextAndDictationInDescription() {
        for (QuestionBankContent.SeedQuestion q : allQuestions()) {
            if (q.text().startsWith("🖼️")) {
                assertThat(q.text()).as(q.text()).matches("🖼️ \\[.+ picture: .+\\]");
                assertThat(q.description()).as("dictation of " + q.text()).isNotBlank();
            } else {
                assertThat(q.description()).as("description of text question: " + q.text()).isNull();
            }
        }
    }

    private List<QuestionBankContent.SeedQuestion> allQuestions() {
        return QuestionBankContent.BANK.values().stream()
                .flatMap(s -> s.values().stream())
                .flatMap(List::stream)
                .toList();
    }
}
