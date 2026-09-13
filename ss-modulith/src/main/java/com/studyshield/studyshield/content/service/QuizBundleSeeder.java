package com.studyshield.studyshield.content.service;

import com.studyshield.studyshield.content.entity.*;
import com.studyshield.studyshield.content.repository.*;
import com.studyshield.studyshield.content.seed.QuestionBankContent;
import com.studyshield.studyshield.content.seed.QuestionBankContent.SeedQuestion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Ensures a freemium catalog exists for a board+class offering: <strong>one FREEMIUM
 * quiz per offering that has questions</strong> ({@value #QUESTIONS_PER_QUIZ} questions each).
 * <p>
 * The class is resolved to its global ordinal via {@link AcademicCatalogResolver}; content
 * is anchored to {@link BoardClassSubject} offerings — never to display names. Offerings
 * that carry no curated or loaded questions are skipped.
 */
@Service
public class QuizBundleSeeder {

    private static final Logger log = LoggerFactory.getLogger(QuizBundleSeeder.class);
    /**
     * Historical name: used to cap the whole class at 2 quizzes (Math+EVS only).
     * Bundles now issue <em>one quiz per offering that has questions</em>; this is no longer a cap.
     */
    public static final int QUIZZES_PER_CLASS = Integer.MAX_VALUE;
    public static final int QUESTIONS_PER_QUIZ = 10;

    private final AcademicCatalogResolver catalogResolver;
    private final ContentPackRepository contentPackRepository;
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;

    public QuizBundleSeeder(
            AcademicCatalogResolver catalogResolver,
            ContentPackRepository contentPackRepository,
            QuizRepository quizRepository,
            QuestionRepository questionRepository
    ) {
        this.catalogResolver = catalogResolver;
        this.contentPackRepository = contentPackRepository;
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
    }

    /**
     * Ensure the board class + its freemium catalog exist; return the board class.
     * One offering per global subject gets a quiz when it has bank or loaded questions.
     */
    @Transactional
    public BoardClass ensureCatalogForClass(String className, String boardCode) {
        BoardClass boardClass = catalogResolver.resolveBoardClass(boardCode, className);
        String band = QuestionBankContent.bandForClassName(boardClass.getDisplayName());
        if (band == null) {
            band = QuestionBankContent.bandForClassName(className);
        }

        List<BoardClassSubject> offerings = catalogResolver.resolveOfferings(boardCode,
                boardClass.getClassLevel().getOrdinal());
        for (BoardClassSubject offering : offerings) {
            if (!shouldOffer(offering, band)) {
                continue;
            }
            ensureQuizBundleForOffering(offering, band);
        }
        return boardClass;
    }

    /**
     * Offer an offering when the curated bank has questions for its subject, or when a pack/quiz
     * was already loaded (e.g. {@code POST /questions/load}).
     */
    private boolean shouldOffer(BoardClassSubject offering, String band) {
        if (hasBankQuestions(band, offering.getSubject().getName())) {
            return true;
        }
        ContentPack pack = pickActiveDeliveryPack(
                contentPackRepository.findByOfferingId(offering.getId()));
        if (pack == null) {
            return false;
        }
        return !quizRepository
                .findByContentPackIdAndContentTierAndActiveTrueOrderByFreemiumIndexAsc(
                        pack.getId(), ContentTier.FREEMIUM)
                .isEmpty();
    }

    static boolean hasBankQuestions(String band, String subjectName) {
        if (band == null || subjectName == null) {
            return false;
        }
        List<SeedQuestion> list = QuestionBankContent.BANK
                .getOrDefault(band, Map.of())
                .get(subjectName);
        return list != null && !list.isEmpty();
    }

    private void ensureQuizBundleForOffering(BoardClassSubject offering, String band) {
        Subject subject = offering.getSubject();
        ContentPack existing = pickActiveDeliveryPack(
                contentPackRepository.findByOfferingId(offering.getId()));
        ContentPack pack = existing != null
                ? existing
                : contentPackRepository.save(ContentPack.builder()
                        .name("Freemium " + subject.getName())
                        .description("Freemium catalog pack")
                        .offering(offering)
                        .version(1)
                        .active(true)
                        .build());

        List<Quiz> quizzes = quizRepository
                .findByContentPackIdAndContentTierAndActiveTrueOrderByFreemiumIndexAsc(
                        pack.getId(), ContentTier.FREEMIUM);

        final int freemiumIndex = 1;
        Quiz quiz = quizzes.stream()
                .filter(q -> freemiumIndex == (q.getFreemiumIndex() == null ? -1 : q.getFreemiumIndex()))
                .findFirst()
                .orElseGet(() -> createQuiz(pack, subject.getName(), freemiumIndex));

        long activeCount = questionRepository.findByQuizIdAndBlacklistedFalse(quiz.getId()).size();
        if (activeCount < QUESTIONS_PER_QUIZ) {
            seedQuestions(quiz, band, subject.getName(), freemiumIndex, (int) activeCount);
        }
    }

    /**
     * Selects the active delivery pack for an offering: the freemium-named pack when one exists
     * (the convention {@link QuizBundleService} and the startup seeder share), otherwise any
     * other active pack. Bank-loaded packs ({@code Loaded <Subject>}) are therefore served too,
     * so freshly loaded content is never invisible to the bundle. Returns {@code null} when the
     * offering has no active pack at all.
     */
    static ContentPack pickActiveDeliveryPack(List<ContentPack> packs) {
        ContentPack namedFreemium = null;
        ContentPack typedFreemium = null;
        ContentPack anyActive = null;
        for (ContentPack pack : packs) {
            if (!pack.isActive()) {
                continue;
            }
            if (anyActive == null) {
                anyActive = pack;
            }
            boolean named = pack.getName() != null
                    && pack.getName().toLowerCase(Locale.ROOT).contains("freemium");
            if (named && namedFreemium == null) {
                namedFreemium = pack;
            }
            if (pack.getPackType() == ContentTier.FREEMIUM && typedFreemium == null) {
                typedFreemium = pack;
            }
        }
        if (namedFreemium != null) {
            return namedFreemium;
        }
        if (typedFreemium != null) {
            return typedFreemium;
        }
        return anyActive;
    }

    private Quiz createQuiz(ContentPack pack, String subjectName, int freemiumIndex) {
        return quizRepository.save(Quiz.builder()
                .title(subjectName + " · Quiz " + freemiumIndex)
                .description("Freemium quiz " + freemiumIndex + " for " + subjectName)
                .contentPack(pack)
                .quizType(Quiz.QuizType.STANDARD)
                .questionCount(QUESTIONS_PER_QUIZ)
                .contentTier(ContentTier.FREEMIUM)
                .freemiumIndex(freemiumIndex)
                .language("English")
                .active(true)
                .build());
    }

    /**
     * Fill a quiz from the curated bank (3 real questions per quiz for known bands) or the
     * real fallback bank (full 10 per quiz) so every session has usable, age-appropriate content.
     */
    private void seedQuestions(Quiz quiz, String band, String subjectName, int freemiumIndex, int activeCount) {
        List<SeedQuestion> source = pickSource(band, subjectName, freemiumIndex);
        int slots = Math.min(QUESTIONS_PER_QUIZ, source.size());
        List<Question> batch = new ArrayList<>();
        String bandSlug = band == null ? "common" : slug(band);

        for (int order = activeCount; order < slots; order++) {
            SeedQuestion sq = source.get(order % source.size());
            int n = order + 1;
            batch.add(toQuestion(sq, quiz, "qb_" + bandSlug + "_" + slug(subjectName)
                    + "_q" + freemiumIndex + "_" + n, order));
        }
        if (!batch.isEmpty()) {
            questionRepository.saveAll(batch);
            log.info("[QuestionBank] Seeded {} curated/fallback questions into quiz {} ({}/{})",
                    batch.size(), quiz.getId(), slots, subjectName);
        }
    }

    /** Curated bands draw from their per-subject bank; others share the fallback. */
    private List<SeedQuestion> pickSource(String band, String subjectName, int freemiumIndex) {
        if (band != null) {
            List<SeedQuestion> bank = QuestionBankContent.BANK.getOrDefault(band, Map.of()).get(subjectName);
            if (bank != null && !bank.isEmpty()) {
                return bank;
            }
        }
        return QuestionBankContent.FALLBACK_BANK;
    }

    private Question toQuestion(SeedQuestion sq, Quiz quiz, String resourceId, int orderIndex) {
        boolean tf = sq.trueFalse();
        List<String> texts = new ArrayList<>(sq.options());
        if (!tf && texts.size() > 1) {
            // Correct answer must not always sit at position A
            java.util.Collections.shuffle(texts);
        }
        List<QuestionOption> options = new ArrayList<>();
        for (int i = 0; i < texts.size(); i++) {
            options.add(new QuestionOption(OPTION_IDS.get(i), texts.get(i), null));
        }
        String correctId = correctOptionId(options, sq.correct());
        return Question.builder()
                .resourceId(resourceId)
                .questionText(sq.text())
                .questionType(tf ? QuestionType.TRUE_FALSE : QuestionType.SINGLE_CHOICE)
                .options(options)
                .correctAnswers(List.of(correctId))
                .correctOption(correctId.toUpperCase(Locale.ROOT))
                .optionA(texts.size() > 0 ? texts.get(0) : "")
                .optionB(texts.size() > 1 ? texts.get(1) : "")
                .optionC(texts.size() > 2 ? texts.get(2) : "")
                .optionD(texts.size() > 3 ? texts.get(3) : "")
                .points(1)
                .difficulty(Difficulty.EASY)
                .languages(List.of("English"))
                .tags(List.of("question-bank", tf ? "true-false" : "single-choice"))
                .quiz(quiz)
                .orderIndex(orderIndex)
                .blacklisted(false)
                .build();
    }

    private static final List<String> OPTION_IDS = List.of("a", "b", "c", "d");

    private static String correctOptionId(List<QuestionOption> options, String correctText) {
        return options.stream()
                .filter(o -> o.getText() != null && o.getText().equals(correctText))
                .map(QuestionOption::getId)
                .findFirst()
                .orElse(options.get(0).getId());
    }

    private static String slug(String value) {
        return value.toLowerCase(Locale.ROOT).replace(" ", "_").replaceAll("[^a-z0-9_]", "");
    }

    static String normalizeClassName(String className) {
        if (className == null) return "";
        String t = className.trim();
        if (t.matches("\\d+")) {
            return "Class " + t;
        }
        if (t.matches("\\d+(st|nd|rd|th)")) {
            return "Class " + t.replaceAll("(st|nd|rd|th)", "");
        }
        return t;
    }
}