package com.studyshield.studyshield.shared;

public interface ContentReference {
    boolean existsBoard(Long id);
    boolean existsClassGrade(Long id);
    boolean existsQuiz(Long id);
    boolean existsQuestion(Long id);
}
