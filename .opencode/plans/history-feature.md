# Plan: Historia ćwiczeń fizycznych i umysłowych

## Cel
Dodać funkcjonalność zapisywania lokalnej historii wykonanych ćwiczeń fizycznych i gier umysłowych, z dostępem przez ekran Ustawień.

## Architektura danych

### Nowe encje Room (migracja 3→4)

**ExerciseSession**
- id: Long (PK, auto)
- exerciseId: Int
- exerciseName: String
- category: String
- moodType: Int (1=zle, 2=srednio, 3=dobrze)
- durationSeconds: Int
- completedAt: Long (timestamp)

**GameSession**
- id: Long (PK, auto)
- gameType: String ("memory" / "colors" / "liquid")
- score: Int
- level: Int
- completedAt: Long (timestamp)

### DAOs
- ExerciseSessionDao: insert, getAllDesc(), getCount(), deleteAll()
- GameSessionDao: insert, getAllDesc(), getByGameType(), getCount(), deleteAll()

### Migracja
- Migration 3→4 w AppDatabase (zamiast fallbackToDestructiveMigration)
- Nowe tabele exercise_sessions i game_sessions

## Logika zapisu

### Ćwiczenia fizyczne
- **Lokalizacja**: SingleExerciseActivity
- **Trigger**: Nowy przycisk "UKOŃCZ TO ĆWICZENIE"
- **Dane**: exerciseId, name, category, moodType (z intentu), duration (od onResume do click), timestamp
- **TTS**: "Ćwiczenie zapisane w historii"

### Gry umysłowe
- **MemoryGameActivity**: showWinDialog() — zapis GameSession("memory", score, gridLevel)
- **ColorTapActivity**: userSequence.size() == colorSequence.size() — zapis GameSession("colors", score, level)
- **LiquidSortActivity**: checkWin() == true — zapis GameSession("liquid", currentLevel, currentLevel)

## UI

### Nowe komponenty
- HistoryActivity (TabLayout + ViewPager2)
  - ExerciseHistoryFragment (RecyclerView)
  - GameHistoryFragment (RecyclerView)
- Adapters: ExerciseSessionAdapter, GameSessionAdapter
- Layouts: activity_history.xml, fragment_exercise_history.xml, fragment_game_history.xml, item_exercise_session.xml, item_game_session.xml

### Modyfikacje istniejących
- SettingsActivity: nowa pozycja "Twoja historia" → HistoryActivity
- SingleExerciseActivity: nowy przycisk "UKOŃCZ"

## Pipeline agentów

1. debugger: verify build compiles before changes
2. coder: create entities + DAOs + migration + update AppDatabase
3. debugger: verify build
4. git-pusher: push intermediate
5. coder: modify game activities (Memory, ColorTap, LiquidSort) to save GameSession
6. coder: modify SingleExerciseActivity (complete button + ExerciseSession save)
7. debugger: verify build
8. git-pusher: push intermediate
9. coder: create HistoryActivity + fragments + adapters + layouts
10. coder: modify SettingsActivity to add history entry
11. debugger: verify build
12. git-pusher: final push

## Librarian (równolegle)
- Loguje każdy etap w BRAIN
- Tworzy: Sessions/, Decisions/, Features/, Knowledge/
- Reorganizuje testingAgentsProject w vaultcie
