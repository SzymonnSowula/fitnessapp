package com.example.fitnessapp;

/**
 * Centralized constants for game difficulty and configuration.
 */
public final class GameConstants {

    private GameConstants() {}

    // ============ General Difficulty Levels ============
    public static final int DIFFICULTY_EASY = 0;
    public static final int DIFFICULTY_MEDIUM = 1;
    public static final int DIFFICULTY_HARD = 2;

    // ============ Memory Game Difficulty Progression ============
    // Progression: 2x3 (6 cards) → 3x4 (12 cards) → 4x4 (16 cards) → 4x5 (20 cards)
    public static final int MEMORY_EASY_COLUMNS = 2;
    public static final int MEMORY_EASY_ROWS = 3;

    public static final int MEMORY_MEDIUM_COLUMNS = 3;
    public static final int MEMORY_MEDIUM_ROWS = 4;

    public static final int MEMORY_HARD_COLUMNS = 4;
    public static final int MEMORY_HARD_ROWS = 4;

    public static final int MEMORY_EXPERT_COLUMNS = 4;
    public static final int MEMORY_EXPERT_ROWS = 5;

    // Memory game level progression (columns, rows)
    public static final int[][] MEMORY_DIFFICULTY_PROGRESSION = {
        {MEMORY_EASY_COLUMNS, MEMORY_EASY_ROWS},       // Level 1: 2x3
        {MEMORY_MEDIUM_COLUMNS, MEMORY_MEDIUM_ROWS},   // Level 2: 3x4
        {MEMORY_HARD_COLUMNS, MEMORY_HARD_ROWS},       // Level 3: 4x4
        {MEMORY_EXPERT_COLUMNS, MEMORY_EXPERT_ROWS}    // Level 4: 4x5
    };

    // ============ Color Tap Game ============
    public static final int COLOR_EASY_MIN_LENGTH = 2;
    public static final int COLOR_EASY_MAX_LENGTH = 4;
    public static final int COLOR_EASY_DELAY_MS = 1200;

    public static final int COLOR_MEDIUM_MIN_LENGTH = 3;
    public static final int COLOR_MEDIUM_MAX_LENGTH = 6;
    public static final int COLOR_MEDIUM_DELAY_MS = 900;

    public static final int COLOR_HARD_MIN_LENGTH = 4;
    public static final int COLOR_HARD_MAX_LENGTH = 8;
    public static final int COLOR_HARD_DELAY_MS = 700;

    // ============ 2048 Game Target Scores ============
    public static final int TARGET_2048_EASY = 512;
    public static final int TARGET_2048_MEDIUM = 1024;
    public static final int TARGET_2048_HARD = 2048;

    // ============ Liquid Sort Game ============
    public static final int LIQUID_EASY_BASE_LEVEL = 1;
    public static final int LIQUID_MEDIUM_BASE_LEVEL = 3;
    public static final int LIQUID_HARD_BASE_LEVEL = 6;
}
