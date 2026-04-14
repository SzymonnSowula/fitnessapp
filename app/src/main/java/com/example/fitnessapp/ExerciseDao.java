package com.example.fitnessapp;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

@Dao
public interface ExerciseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Exercise> exercises);

    @Transaction
    default void replaceAll(List<Exercise> exercises) {
        deleteAll();
        insertAll(exercises);
    }

    @Query("SELECT * FROM exercises")
    List<Exercise> getAll();

    @Query("SELECT * FROM exercises WHERE category = :category COLLATE NOCASE")
    List<Exercise> getByCategory(String category);

    @Query("SELECT * FROM exercises WHERE category = :category COLLATE NOCASE AND poziomTrudnosciNum <= :maxDifficulty AND intensywnoscNum <= :maxIntensity")
    List<Exercise> getByCategoryAndMood(String category, float maxDifficulty, float maxIntensity);

    @Query("SELECT * FROM exercises WHERE category = :category COLLATE NOCASE ORDER BY ABS(poziomTrudnosciNum - :prefDifficulty) + ABS(intensywnoscNum - :prefIntensity) ASC")
    List<Exercise> getByCategorySortedByMood(String category, float prefDifficulty, float prefIntensity);

    @Query("SELECT * FROM exercises WHERE id = :id")
    Exercise getById(int id);

    @Query("DELETE FROM exercises")
    void deleteAll();

    @Query("SELECT COUNT(*) FROM exercises")
    int getCount();
}