package com.example.fitnessapp;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import android.content.Context;

@Database(entities = {Exercise.class, ExerciseSession.class, GameSession.class}, version = 4, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ExerciseDao exerciseDao();
    public abstract ExerciseSessionDao exerciseSessionDao();
    public abstract GameSessionDao gameSessionDao();

    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS exercise_sessions (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, exerciseId INTEGER NOT NULL, exerciseName TEXT, category TEXT, moodType INTEGER NOT NULL, durationSeconds INTEGER NOT NULL, completedAt INTEGER NOT NULL)");
            database.execSQL("CREATE TABLE IF NOT EXISTS game_sessions (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, gameType TEXT, score INTEGER NOT NULL, level INTEGER NOT NULL, completedAt INTEGER NOT NULL)");
        }
    };

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "fitness_database")
                            .allowMainThreadQueries()
                            .addMigrations(MIGRATION_3_4)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}