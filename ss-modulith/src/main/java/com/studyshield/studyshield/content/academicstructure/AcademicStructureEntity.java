package com.studyshield.studyshield.content.academicstructure;

import java.util.Arrays;
import java.util.Optional;

/**
 * The six academic-structure entities that round-trip between GitHub JSON files
 * ({@code academic-structure/<key>.json}) and the database via the load/export/purge
 * endpoints. Declaration order is the FK load order; purge runs the reverse.
 */
public enum AcademicStructureEntity {

    CLASS_LEVELS("class-levels", "class_levels"),
    COUNTRIES("countries", "countries"),
    BOARDS("boards", "boards"),
    BOARD_CLASSES("board-classes", "board_class"),
    SUBJECTS("subjects", "subjects"),
    OFFERINGS("offerings", "board_class_subject");

    private final String key;
    private final String tableName;

    AcademicStructureEntity(String key, String tableName) {
        this.key = key;
        this.tableName = tableName;
    }

    public String key() {
        return key;
    }

    public String tableName() {
        return tableName;
    }

    public static Optional<AcademicStructureEntity> fromKey(String key) {
        if (key == null) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(e -> e.key.equalsIgnoreCase(key.trim()))
                .findFirst();
    }
}
