package com.studyshield.studyshield.content.service;

import com.studyshield.studyshield.content.dto.QuestionBankLoadItem;
import com.studyshield.studyshield.content.dto.QuestionBankLoadResponse;
import com.studyshield.studyshield.content.entity.*;
import com.studyshield.studyshield.content.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Loads questions via {@code POST /api/v1/questions/load}.
 * <p>
 * Each item carries enough metadata (boardCode, className, age, subject) to auto-create
 * the full Board → ClassGrade → Subject → ContentPack → Quiz → Question chain.
 * Items are grouped by (boardCode, className, subject) so a single batch can span
 * multiple grades and subjects.  Duplicate questions (same text within a quiz) are skipped.
 */
@Service
public class QuestionBankLoader {

    private static final Logger log = LoggerFactory.getLogger(QuestionBankLoader.class);
    private static final int QUIZ_CAPACITY = 50;

    private final BoardRepository boardRepository;
    private final ClassGradeRepository classGradeRepository;
    private final SubjectRepository subjectRepository;
    private final ContentPackRepository contentPackRepository;
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;

    public QuestionBankLoader(
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

    @Transactional
    public QuestionBankLoadResponse load(List<QuestionBankLoadItem> items) {
        int boardsCreated = 0, classGradesCreated = 0, subjectsCreated = 0;
        int contentPacksCreated = 0, quizzesCreated = 0, questionsCreated = 0, questionsSkipped = 0;

        // Group by (boardCode → className → subject)
        Map<String, Map<String, Map<String, List<QuestionBankLoadItem>>>> grouped = items.stream()
                .collect(Collectors.groupingBy(
                        QuestionBankLoadItem::boardCode,
                        Collectors.groupingBy(
                                QuestionBankLoadItem::className,
                                Collectors.groupingBy(QuestionBankLoadItem::subject))));

        for (var boardEntry : grouped.entrySet()) {
            String boardCode = boardEntry.getKey();
            Board board = resolveOrCreateBoard(boardCode);
            if (board.getId() == null) boardsCreated++;

            for (var classEntry : boardEntry.getValue().entrySet()) {
                String className = normalizeClassName(classEntry.getKey());
                ClassGrade classGrade = classGradeRepository.findFirstByNameIgnoreCase(className)
                        .or(() -> classGradeRepository.findFirstByNameIgnoreCase(classEntry.getKey()))
                        .orElseGet(() -> {
                            log.info("[BankLoad] Creating class grade name={}", className);
                            return classGradeRepository.save(ClassGrade.builder()
                                    .name(className)
                                    .board(board)
                                    .description("Auto-loaded via /questions/load")
                                    .build());
                        });
                if (classGrade.getId() == null) classGradesCreated++;

                for (var subjectEntry : classEntry.getValue().entrySet()) {
                    String subjectName = subjectEntry.getKey();
                    List<QuestionBankLoadItem> questions = subjectEntry.getValue();

                    Subject subject = resolveOrCreateSubject(subjectName, classGrade);
                    if (subject.getId() == null) subjectsCreated++;

                    ContentPack pack = resolveOrCreateContentPack(subject);
                    if (pack.getId() == null) contentPacksCreated++;

                    Quiz quiz = resolveOrCreateQuiz(pack, subjectName);
                    if (quiz.getId() == null) quizzesCreated++;

                    // Collect existing question texts to detect duplicates
                    var existingTexts = questionRepository.findByQuizId(quiz.getId()).stream()
                            .map(Question::getQuestionText)
                            .collect(Collectors.toSet());

                    List<Question> batch = new ArrayList<>();
                    for (QuestionBankLoadItem item : questions) {
                        if (existingTexts.contains(item.questionText().trim())) {
                            questionsSkipped++;
                            continue;
                        }
                        batch.add(toQuestion(item, quiz));
                        existingTexts.add(item.questionText().trim());
                    }
                    if (!batch.isEmpty()) {
                        questionRepository.saveAll(batch);
                        questionsCreated += batch.size();
                        log.info("[BankLoad] Loaded {} questions into quiz {} ({}/{})",
                                batch.size(), quiz.getId(), subjectName, className);
                    }

                    // If quiz exceeds capacity, create a new one for remaining items
                    long totalInQuiz = questionRepository.findByQuizIdAndBlacklistedFalse(quiz.getId()).size();
                    if (totalInQuiz >= QUIZ_CAPACITY) {
                        quiz = createQuiz(pack, subjectName, quiz);
                        quizzesCreated++;
                    }
                }
            }
        }

        log.info("[BankLoad] Done: boards={}, classGrades={}, subjects={}, packs={}, quizzes={}, questions={}, skipped={}",
                boardsCreated, classGradesCreated, subjectsCreated, contentPacksCreated,
                quizzesCreated, questionsCreated, questionsSkipped);

        return new QuestionBankLoadResponse(boardsCreated, classGradesCreated, subjectsCreated,
                contentPacksCreated, quizzesCreated, questionsCreated, questionsSkipped);
    }

    private Board resolveOrCreateBoard(String boardCode) {
        String code = (boardCode == null || boardCode.isBlank() || "all".equalsIgnoreCase(boardCode))
                ? "ALL"
                : boardCode.trim().toUpperCase(Locale.ROOT);
        return boardRepository.findByCode(code).orElseGet(() ->
                boardRepository.save(Board.builder()
                        .name(code.equals("ALL") ? "All Boards" : code)
                        .code(code)
                        .description("Loaded via /questions/load")
                        .active(true)
                        .build()));
    }

    private Subject resolveOrCreateSubject(String subjectName, ClassGrade classGrade) {
        return subjectRepository.findByClassGradeIdAndNameIgnoreCase(classGrade.getId(), subjectName)
                .orElseGet(() -> {
                    String code = subjectName.toUpperCase(Locale.ROOT).replace(" ", "_");
                    return subjectRepository.save(Subject.builder()
                            .name(subjectName)
                            .code(code)
                            .classGrade(classGrade)
                            .active(true)
                            .build());
                });
    }

    private ContentPack resolveOrCreateContentPack(Subject subject) {
        return contentPackRepository.findBySubjectId(subject.getId()).stream()
                .filter(ContentPack::isActive)
                .findFirst()
                .orElseGet(() -> contentPackRepository.save(ContentPack.builder()
                        .name("Loaded " + subject.getName())
                        .description("Auto-loaded content pack")
                        .subject(subject)
                        .version(1)
                        .active(true)
                        .build()));
    }

    private Quiz resolveOrCreateQuiz(ContentPack pack, String subjectName) {
        return quizRepository.findByContentPackIdAndContentTierAndActiveTrueOrderByFreemiumIndexAsc(
                        pack.getId(), ContentTier.FREEMIUM).stream()
                .findFirst()
                .orElseGet(() -> createQuiz(pack, subjectName, null));
    }

    private Quiz createQuiz(ContentPack pack, String subjectName, Quiz previous) {
        int nextIndex = 1;
        if (previous != null && previous.getFreemiumIndex() != null) {
            nextIndex = previous.getFreemiumIndex() + 1;
        }
        return quizRepository.save(Quiz.builder()
                .title(subjectName + " · Quiz " + nextIndex)
                .description("Loaded quiz " + nextIndex + " for " + subjectName)
                .contentPack(pack)
                .quizType(Quiz.QuizType.STANDARD)
                .questionCount(QUIZ_CAPACITY)
                .contentTier(ContentTier.FREEMIUM)
                .freemiumIndex(nextIndex)
                .language("English")
                .active(true)
                .build());
    }

    private Question toQuestion(QuestionBankLoadItem item, Quiz quiz) {
        boolean tf = item.questionType() == QuestionType.TRUE_FALSE;
        List<String> opts = item.options() != null ? item.options() : List.of("True", "False");
        List<String> texts = new ArrayList<>(opts);

        List<QuestionOption> options = new ArrayList<>();
        String correctId = "a";
        for (int i = 0; i < texts.size(); i++) {
            String id = OPTION_IDS.get(i);
            options.add(new QuestionOption(id, texts.get(i), null));
            if (texts.get(i).equals(item.correctAnswer())) {
                correctId = id;
            }
        }

        return Question.builder()
                .resourceId("bankload_" + slug(item.className()) + "_" + slug(item.subject())
                        + "_q" + (item.orderIndex() != null ? item.orderIndex() : System.nanoTime()))
                .questionText(item.questionText().trim())
                .questionType(item.questionType())
                .options(options)
                .correctAnswers(List.of(correctId))
                .correctOption(correctId.toUpperCase(Locale.ROOT))
                .optionA(texts.size() > 0 ? texts.get(0) : "")
                .optionB(texts.size() > 1 ? texts.get(1) : "")
                .optionC(texts.size() > 2 ? texts.get(2) : "")
                .optionD(texts.size() > 3 ? texts.get(3) : "")
                .explanation(item.explanation())
                .points(1)
                .difficulty(Difficulty.EASY)
                .languages(List.of("English"))
                .tags(List.of("question-bank-load", tf ? "true-false" : "single-choice"))
                .quiz(quiz)
                .orderIndex(item.orderIndex() != null ? item.orderIndex() : 0)
                .blacklisted(false)
                .build();
    }

    private static final List<String> OPTION_IDS = List.of("a", "b", "c", "d");

    private static String normalizeClassName(String className) {
        if (className == null) return "";
        String t = className.trim();
        if (t.matches("\\d+")) return "Class " + t;
        if (t.matches("\\d+(st|nd|rd|th)")) return "Class " + t.replaceAll("(st|nd|rd|th)", "");
        return t;
    }

    private static String slug(String value) {
        return value.toLowerCase(Locale.ROOT).replace(" ", "_").replaceAll("[^a-z0-9_]", "");
    }
}
