package com.studyshield.studyshield.content.repository;

import com.studyshield.studyshield.content.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    /** All rows (old and current versions) for a quiz; used for de-duplication during load. */
    List<Question> findByQuizId(Long quizId);

    /**
     * Active (non-blacklisted) questions for a quiz, restricted to the latest version of each
     * question ({@code supersededBy} null). Everything that serves a quiz must call this so a
     * superseded revision never reaches the app.
     */
    @Query("select q from Question q where q.quiz.id = :quizId and q.blacklisted = false and q.supersededBy is null")
    List<Question> findByQuizIdAndBlacklistedFalse(@Param("quizId") Long quizId);

    /** Latest version of each question in the quiz regardless of blacklist flag. */
    @Query("select q from Question q where q.quiz.id = :quizId and q.supersededBy is null")
    List<Question> findByQuizIdAndSupersededByNull(@Param("quizId") Long quizId);

    /** Full revision chain of a question, oldest first. */
    List<Question> findByVersionGroupIdOrderByVersionNumberAsc(String versionGroupId);

    List<Question> findByVersionGroupId(String versionGroupId);

    List<Question> findBySupersededByIsNull();
}