package com.example.fitnessapp;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "exercise_sessions")
public class ExerciseSession {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public int exerciseId;
    public String exerciseName;
    public String category;
    public int moodType; // 1=zle, 2=srednio, 3=dobrze
    public int durationSeconds;
    public long completedAt; // System.currentTimeMillis()
}
