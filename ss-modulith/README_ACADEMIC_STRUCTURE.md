# Academic Structure Migration

This document describes the academic structure migration that implements a global education grade system with per-board labels.

## Database Schema Overview

The migration introduces several key tables to support global academic structures:

1. `class_levels` - Global ordinal-based education levels
2. `countries` - Country information
3. `boards` - Educational boards (CBSE, ICSE, etc.)
4. `board_class` - Mapping between boards and class levels
5. `subjects` - Subject definitions
6. `board_class_subject` - The offering that links boards, classes, and subjects

## How to Add a New Board

To add a new board:

1. Insert a new record into the `boards` table with:
   - `country_id`: Reference to the country (or NULL for generic)
   - `code`: Unique identifier (e.g., 'IB', 'FR', 'DE')
   - `name`: Full name of the board
   - `min_ordinal`: Minimum ordinal level supported
   - `max_ordinal`: Maximum ordinal level supported

2. Insert records into `board_class` table for each class level that this board supports:
   - `board_id`: Reference to the new board
   - `class_level_id`: Reference to the class level
   - `display_name`: Appropriate name for this board (e.g., "Class 3", "Year 4")

Example SQL for adding a new board like France:

```sql
-- Insert the French board
INSERT INTO "ss-dev".boards (country_id, code, name, min_ordinal, max_ordinal)
VALUES (
    (SELECT id FROM "ss-dev".countries WHERE iso2 = 'FR'),
    'FR',
    'French National Education',
    2,
    16
);

-- Insert the class mappings for this board
INSERT INTO "ss-dev".board_class (board_id, class_level_id, display_name)
SELECT 
    (SELECT id FROM "ss-dev".boards WHERE code = 'FR'),
    cl.id,
    CASE 
        WHEN cl.ordinal = 2 THEN 'Petite Section'
        WHEN cl.ordinal = 3 THEN 'Moyenne Section'
        WHEN cl.ordinal = 4 THEN 'Grande Section'
        WHEN cl.ordinal = 5 THEN 'CP'
        WHEN cl.ordinal = 6 THEN 'CE1'
        WHEN cl.ordinal = 7 THEN 'CE2'
        WHEN cl.ordinal = 8 THEN 'CM1'
        WHEN cl.ordinal = 9 THEN 'CM2'
        WHEN cl.ordinal = 10 THEN '6ème'
        WHEN cl.ordinal = 11 THEN '5ème'
        WHEN cl.ordinal = 12 THEN '4ème'
        WHEN cl.ordinal = 13 THEN '3ème'
        WHEN cl.ordinal = 14 THEN '2nde'
        WHEN cl.ordinal = 15 THEN '1ère'
        WHEN cl.ordinal = 16 THEN 'Terminale'
    END AS display_name
FROM "ss-dev".class_levels cl 
WHERE cl.ordinal BETWEEN 2 AND 16;
```

## Key Features

- Global ordinal system (1-17) for consistent level tracking across countries
- Per-board display names to maintain local conventions
- Offering-based content linking (board + class level + subject)
- Backwards compatible with existing data through migration