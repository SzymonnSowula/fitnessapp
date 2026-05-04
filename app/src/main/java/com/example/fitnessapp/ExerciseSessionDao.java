package com.example.fitnessapp;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ExerciseSessionDao {
    @Insert
    void insert(ExerciseSession session);

    @Query("SELECT * FROM exercise_sessions ORDER BY completedAt DESC LIMIT 50")
    List<ExerciseSession> getAllDesc();

    @Query("SELECT COUNT(*) FROM exercise_sessions")
    int getCount();

    @Query("DELETE FROM exercise_sessions")
    void deleteAll();
}
