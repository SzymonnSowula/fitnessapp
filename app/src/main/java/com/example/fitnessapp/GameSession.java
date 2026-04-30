package com.example.fitnessapp;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "game_sessions")
public class GameSession {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String gameType; // "memory", "colors", "liquid"
    public int score;
    public int level;
    public long completedAt; // System.currentTimeMillis()
}
