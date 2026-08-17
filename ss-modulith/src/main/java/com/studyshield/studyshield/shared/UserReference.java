package com.studyshield.studyshield.shared;

public interface UserReference {
    boolean existsUser(Long id);
    boolean existsChildProfile(Long id);
}
