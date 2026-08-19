package com.studyshield.studyshield;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class StudyShieldApplication {

    private static final Logger log = LoggerFactory.getLogger(StudyShieldApplication.class);

    public static void main(String[] args) {
        String dbUrl = System.getenv("DATABASE_URL");
        String dbUser = System.getenv("DB_USERNAME");
        log.info("DB_URL set: {}, DB_USERNAME set: {}", dbUrl != null, dbUser != null);
        SpringApplication.run(StudyShieldApplication.class, args);
    }
}
