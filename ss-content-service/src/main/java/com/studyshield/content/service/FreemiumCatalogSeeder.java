package com.studyshield.content.service;

import com.studyshield.content.entity.*;
import com.studyshield.content.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Ensures freemium catalog exists for a class: subjects with 5 FREEMIUM quizzes × 10 questions.
 */
@Service
public class FreemiumCatalogSeeder {

    private static final Logger log = LoggerFactory.getLogger(FreemiumCatalogSeeder.class);
    public static final int QUIZZES_PER_SUBJECT = 5;
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

    public FreemiumCatalogSeeder(
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
        if (subjects.isEmpty()) {
            subjects = createDefaultSubjects(classGrade);
        }

        for (Subject subject : subjects) {
            ensureFreemiumPackForSubject(subject);
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
        int gradeNumber = parseGradeNumber(name);
        log.info("[FreemiumSeed] Creating class grade name={} gradeNumber={}", name, gradeNumber);
        return classGradeRepository.save(ClassGrade.builder()
                .name(name)
                .gradeNumber(gradeNumber)
                .board(board)
                .description("Auto-seeded for freemium")
                .build());
    }

    private List<Subject> createDefaultSubjects(ClassGrade classGrade) {
        List<Subject> created = new ArrayList<>();
        for (String subjectName : DEFAULT_SUBJECTS) {
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

    private void ensureFreemiumPackForSubject(Subject subject) {
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

        for (int index = 1; index <= QUIZZES_PER_SUBJECT; index++) {
            final int freemiumIndex = index;
            Quiz quiz = existing.stream()
                    .filter(q -> freemiumIndex == (q.getFreemiumIndex() == null ? -1 : q.getFreemiumIndex()))
                    .findFirst()
                    .orElseGet(() -> createQuiz(pack, subject.getName(), freemiumIndex));

            long activeCount = questionRepository.findByQuizIdAndBlacklistedFalse(quiz.getId()).size();
            if (activeCount < QUESTIONS_PER_QUIZ) {
                seedQuestions(quiz, subject.getName(), freemiumIndex, (int) activeCount);
            }
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

    private void seedQuestions(Quiz quiz, String subjectName, int freemiumIndex, int startOrder) {
        List<Question> batch = new ArrayList<>();
        for (int i = startOrder; i < QUESTIONS_PER_QUIZ; i++) {
            int n = i + 1;
            String resourceId = "seed_" + subjectName.toLowerCase().replace(" ", "_")
                    + "_q" + freemiumIndex + "_" + n;
            List<QuestionOption> options = List.of(
                    new QuestionOption("a", "Option A for " + subjectName + " #" + n, null),
                    new QuestionOption("b", "Option B for " + subjectName + " #" + n, null),
                    new QuestionOption("c", "Option C for " + subjectName + " #" + n, null),
                    new QuestionOption("d", "Option D for " + subjectName + " #" + n, null)
            );
            batch.add(Question.builder()
                    .resourceId(resourceId)
                    .questionText("[" + subjectName + "] Sample question " + n + " (quiz " + freemiumIndex + ")?")
                    .questionType(QuestionType.SINGLE_CHOICE)
                    .options(options)
                    .correctAnswers(List.of("a"))
                    .points(1)
                    .difficulty(Difficulty.EASY)
                    .languages(List.of("English"))
                    .tags(List.of("seed", subjectName.toLowerCase()))
                    .quiz(quiz)
                    .orderIndex(i)
                    .blacklisted(false)
                    .build());
        }
        if (!batch.isEmpty()) {
            questionRepository.saveAll(batch);
        }
    }

    static String normalizeClassName(String className) {
        if (className == null) return "";
        String t = className.trim();
        // Accept "1", "1st", "Class 1"
        if (t.matches("\\d+")) {
            return "Class " + t;
        }
        if (t.matches("\\d+(st|nd|rd|th)")) {
            return "Class " + t.replaceAll("(st|nd|rd|th)", "");
        }
        return t;
    }

    private static int parseGradeNumber(String name) {
        String digits = name.replaceAll("\\D+", "");
        if (digits.isEmpty()) {
            // Nursery / KG heuristic
            String lower = name.toLowerCase();
            if (lower.contains("nursery")) return 0;
            if (lower.contains("junior") || lower.contains("lkg")) return 0;
            if (lower.contains("senior") || lower.contains("ukg")) return 0;
            return 1;
        }
        try {
            return Integer.parseInt(digits);
        } catch (NumberFormatException e) {
            return 1;
        }
    }
}
