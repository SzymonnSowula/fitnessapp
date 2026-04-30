package com.example.fitnessapp;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface GameSessionDao {
    @Insert
    void insert(GameSession session);

    @Query("SELECT * FROM game_sessions ORDER BY completedAt DESC")
    List<GameSession> getAllDesc();

    @Query("SELECT * FROM game_sessions WHERE gameType = :type ORDER BY completedAt DESC")
    List<GameSession> getByGameType(String type);

    @Query("SELECT COUNT(*) FROM game_sessions")
    int getCount();

    @Query("DELETE FROM game_sessions")
    void deleteAll();
}
