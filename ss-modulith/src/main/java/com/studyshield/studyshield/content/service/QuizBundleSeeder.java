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
 * Ensures freemium catalog exists for a class: subjects with {@value #QUIZZES_PER_CLASS}
 * FREEMIUM quizzes total (10 questions each).
 * <p>
 * Curated bands ({@code Sr KG}, {@code Class 1}) are seeded from {@link QuestionBankContent}
 * (real age-appropriate questions); any other class is topped up from a real fallback bank so a
 * session never starts empty.
 */
@Service
public class QuizBundleSeeder {

    private static final Logger log = LoggerFactory.getLogger(QuizBundleSeeder.class);
    public static final int QUIZZES_PER_CLASS = 2;
    public static final int QUESTIONS_PER_QUIZ = 10;

    private static final List<String> DEFAULT_SUBJECTS = List.of(
            "Math", "EVS", "English", "General Knowledge"
    );

    private final BoardRepository boardRepository;
    private final ClassGradeRepository classGradeRepository;
    private final SubjectRepository subjectRepository;
    private final ContentPackRepository contentPackRepository;
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;

    public QuizBundleSeeder(
            BoardRepository boardRepository,
            ClassGradeRepository classGradeRepository,
            SubjectRepository subjectRepository,
            ContentPackRepository contentPackRepository,
            QuizRepository quizRepository,
            QuestionRepository questionRepository
    ) {
        this.boardRepository = boardRepository;
        this.classGradeRepository = classGradeRepository;
        this.subjectRepository = subjectRepository;
        this.contentPackRepository = contentPackRepository;
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
    }

    /**
     * Ensure class grade + freemium catalog exist; return the class grade.
     */
    @Transactional
    public ClassGrade ensureCatalogForClass(String className, String boardCode) {
        Board board = resolveOrCreateBoard(boardCode);
        ClassGrade classGrade = classGradeRepository.findFirstByNameIgnoreCase(normalizeClassName(className))
                .or(() -> classGradeRepository.findFirstByNameIgnoreCase(className))
                .orElseGet(() -> createClassGrade(board, className));

        List<Subject> subjects = subjectRepository.findByClassGradeId(classGrade.getId());
        String band = QuestionBankContent.bandForClassName(classGrade.getName());
        if (subjects.isEmpty()) {
            subjects = createDefaultSubjects(classGrade, band);
        }

        // Right-sized catalog: QUIZZES_PER_CLASS quizzes total per class (one per subject,
        // first subjects win), 10 questions each — never the old 5-per-subject sprawl.
        int seededSubjects = Math.min(QUIZZES_PER_CLASS, subjects.size());
        for (Subject subject : subjects.subList(0, seededSubjects)) {
            ensureQuizBundleForSubject(subject, band);
        }
        return classGrade;
    }

    private Board resolveOrCreateBoard(String boardCode) {
        String code = (boardCode == null || boardCode.isBlank() || "all".equalsIgnoreCase(boardCode))
                ? "ALL"
                : boardCode.trim().toUpperCase();
        return boardRepository.findByCode(code).orElseGet(() ->
                boardRepository.save(Board.builder()
                        .name(code.equals("ALL") ? "All Boards" : code)
                        .code(code)
                        .description("Freemium catalog board")
                        .active(true)
                        .build()));
    }

    private ClassGrade createClassGrade(Board board, String className) {
        String name = normalizeClassName(className);
        log.info("[FreemiumSeed] Creating class grade name={}", name);
        return classGradeRepository.save(ClassGrade.builder()
                .name(name)
                .board(board)
                .description("Auto-seeded for freemium")
                .build());
    }

    private List<Subject> createDefaultSubjects(ClassGrade classGrade, String band) {
        List<String> subjectNames = curatedSubjectNames(band);
        if (subjectNames.isEmpty()) {
            subjectNames = DEFAULT_SUBJECTS;
        }
        List<Subject> created = new ArrayList<>();
        for (String subjectName : subjectNames) {
            String code = subjectName.toUpperCase().replace(" ", "_");
            created.add(subjectRepository.save(Subject.builder()
                    .name(subjectName)
                    .code(code)
                    .classGrade(classGrade)
                    .active(true)
                    .build()));
        }
        return created;
    }

    /** Subject names from a curated band's bank, so e.g. Exp gets only its Welcome subject. */
    private List<String> curatedSubjectNames(String band) {
        if (band == null) return List.of();
        return List.copyOf(QuestionBankContent.BANK.getOrDefault(band, Map.of()).keySet());
    }

    private void ensureQuizBundleForSubject(Subject subject, String band) {
        ContentPack pack = contentPackRepository.findBySubjectId(subject.getId()).stream()
                .filter(ContentPack::isActive)
                .filter(p -> p.getName() != null && p.getName().toLowerCase().contains("freemium"))
                .findFirst()
                .orElseGet(() -> contentPackRepository.save(ContentPack.builder()
                        .name("Freemium " + subject.getName())
                        .description("Freemium catalog pack")
                        .subject(subject)
                        .version(1)
                        .active(true)
                        .build()));

        List<Quiz> existing = quizRepository
                .findByContentPackIdAndContentTierAndActiveTrueOrderByFreemiumIndexAsc(
                        pack.getId(), ContentTier.FREEMIUM);

        final int freemiumIndex = 1;
        Quiz quiz = existing.stream()
                .filter(q -> freemiumIndex == (q.getFreemiumIndex() == null ? -1 : q.getFreemiumIndex()))
                .findFirst()
                .orElseGet(() -> createQuiz(pack, subject.getName(), freemiumIndex));

        long activeCount = questionRepository.findByQuizIdAndBlacklistedFalse(quiz.getId()).size();
        if (activeCount < QUESTIONS_PER_QUIZ) {
            seedQuestions(quiz, band, subject.getName(), freemiumIndex, (int) activeCount);
        }
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
