# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository. **Always read this file at the start of every session.**

## Project Overview

Polish-language Android fitness app for seniors. Features mood-based exercise recommendations powered by an ONNX ML model, a Room database of exercises imported from CSV, Firebase authentication, and mind training games.

## Build Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run unit tests
./gradlew test

# Run Android instrumented tests
./gradlew connectedAndroidTest

# Clean and rebuild
./gradlew clean assembleDebug
```

## Architecture

### Navigation Flow
```
SplashActivity → LoginActivity/RegisterActivity → OnboardingActivity → ChoiceActivity
 ↓
 MainActivity ←→ MindGamesActivity
 ↓
 RecommendationListActivity → ExerciseDetailActivity
 ↓
 SingleExerciseActivity
```

### Mind Games Flow (IMPORTANT)
```
MindGamesActivity → GameInstructionActivity → [GameActivity]
                                           ↓
                        EasyGamesActivity (Memory) / ColorTapActivity / LiquidSortActivity
```

**Each mind game has a dedicated instruction screen (GameInstructionActivity) that shows before the game starts.**
- Game types: GAME_MEMORY, GAME_COLORS, GAME_LIQUID
- Instruction layout: `activity_game_instruction.xml`
- Instruction text is defined in `GameInstructionActivity.java`

### Data Layer
- **Room Database** (`AppDatabase`): Single database `fitness_database` with `Exercise` entity
- **CSV Importer** (`CsvImporter`): Loads exercises from `assets/cwiczenia_seniorzy.csv` into Room on app startup
- **Exercise Categories**: `sila`, `kardio`, `mobilnosc`, `rownowaga`, `postura`, `mieszana`

### ML Recommendation System
- **ONNX Model** (`rf_rekomendator.onnx`): Random Forest classifier for exercise category recommendation
- **Metadata** (`metadata.json`): Contains class labels matching model outputs
- **Feature Array** (12 floats): `[sile, elastycznosc, kardio, postura, intensywnosc, trudnosc, krzeslo, lozek, sitting, standing, floor, zrodlo]`
- `ModelRunner` handles ONNX session creation, inference, and output parsing

### Authentication
- **Firebase Auth**: Email/password authentication via `FirebaseAuth`
- **Firestore**: User profile sync (preferences, onboarding data)
- Anonymous accounts created via `SplashActivity` for new users

### Preferences
- SharedPreferences file `FitnessAppPrefs` stores:
 - User name, onboarding completion flag
 - Physical capabilities (can_stand, can_exercise_floor, needs_chair, can_exercise_bed, can_exercise_sitting)
 - Health conditions set

## Voice System (TTS + Speech Recognition)

### Overview
The app uses a comprehensive voice control system designed for seniors:
- **TTS (Text-to-Speech)**: Reads content aloud in Polish
- **Speech Recognition**: Listens for voice commands
- **VoiceNavigator**: Per-activity voice handler
- **VoiceCommands**: Central command matching

### Key Classes

| Class | Purpose |
|-------|---------|
| `VoiceManager` | Singleton managing TTS and SpeechRecognizer |
| `VoiceNavigator` | Per-activity voice handling, callback interface |
| `VoiceCommands` | Maps Polish phrases to canonical commands |
| `VoiceHelpDialog` | Dialog showing available voice commands |

### Voice Help Dialog
A help dialog showing available voice commands is accessible via a **?** icon in the top-right corner of each screen:
- **Icon**: `ic_help.xml` - blue question mark vector drawable
- **Layout**: `dialog_voice_help.xml` - shows commands organized by category
- **Class**: `VoiceHelpDialog.java` - simple dialog helper

To add the help button to a new screen:
1. Add `ImageButton` with `android:id="@+id/btn_help"` and `android:src="@drawable/ic_help"` in the header
2. In the Activity's `onCreate`, add:
```java
ImageButton btnHelp = findViewById(R.id.btn_help);
if (btnHelp != null) {
    btnHelp.setOnClickListener(v -> VoiceHelpDialog.show(this));
}
```

### Voice Commands Priority
Command categories are checked in this order:
1. **MOOD** (highest priority) - "czuję się dobrze", etc.
2. **NAVIGATION** - home, back, exercises, games, etc.
3. **EXERCISE** - next_exercise, start, finish, etc.
4. **GAME** - new_game, restart, game_memory, etc.
5. **GENERAL** (lowest priority) - help, stop, read, etc.

This ensures mood commands like "czuję się dobrze" are matched before generic words like "dobrze" (which could match `confirm`).

### VoiceManager (VoiceManager.java)
Singleton that manages both TTS and speech recognition:
- **TTS**: Polish language (pl-PL), speech rate 0.85f for seniors
- **Speech Recognition**: Creates new recognizer instance per listening session
- **Callbacks**: `onSpeechResult`, `onSpeechError`, `onTTSReady`, `onTTSStarted`, `onTTSDone`, `onListeningStarted`, `onListeningStopped`
- **Retry logic**: Auto-restarts on errors with delays
- **Preferences**: Stores `tts_enabled`, `speech_enabled`, `speech_rate` in `VoiceSettings`

### VoiceNavigator Usage
Each activity that uses voice should:
```java
private VoiceNavigator voiceNavigator;

voiceNavigator = new VoiceNavigator(this, new VoiceNavigator.VoiceCallback() {
    @Override
    public void onVoiceCommand(String command) {
        runOnUiThread(() -> handleVoiceCommand(command));
    }
});
voiceNavigator.setup();

// When activity is destroyed:
if (voiceNavigator != null) {
    voiceNavigator.cleanup();
}
```

### Voice Commands

#### General Commands (always available)
| Command | Phrases |
|---------|---------|
| `help` | pomoc, komendy, co mogę powiedzieć |
| `stop` | stop, cisza, milcz, zamknij się |
| `read` | czytaj, przeczytaj, odczytaj |
| `repeat` | powtórz, jeszcze raz |
| `confirm` | tak, okej, potwierdzam, dobrze |
| `cancel` | nie, anuluj |

#### Navigation Commands
| Command | Phrases |
|---------|---------|
| `home` | główna, menu główne, strona startowa |
| `back` | wstecz, cofnij, wróć |
| `exit` | wyjdź, zamknij, koniec, wyloguj |
| `exercises` | ćwiczenia, trening, chcę ćwiczyć |
| `body` | ciało, ćwiczenia ciała |
| `games` | gry, pobawmy się, otwórz gry |
| `mind` | umysł, umysłowe |
| `settings` | ustawienia, opcje |
| `next` | następne, dalej, kolejne |
| `previous` | poprzednie, wstecz |

#### Exercise Commands
| Command | Phrases |
|---------|---------|
| `next_exercise` | następne ćwiczenie, kolejne ćwiczenie |
| `previous_exercise` | poprzednie ćwiczenie |
| `start` | start, rozpocznij, zaczynamy |
| `finish` | zakończ, koniec, stop |
| `read_description` | czytaj opis, opisz ćwiczenie |
| `ask_reps` | ile powtórzeń, ile razy |
| `ask_duration` | jak długo, czas |

#### Game Commands
| Command | Phrases |
|---------|---------|
| `new_game` | nowa gra |
| `restart` | restart, resetuj, od nowa |
| `next_level` | następny poziom |
| `game_memory` | memory, pamięć, karty |
| `game_colors` | kolory, barwy |
| `game_liquid` | płyny, probówki, sortowanie |
| `good` | dobrze, super, świetnie, brawo |
| `wrong` | źle, błąd, pudło |

#### Mood Commands (on MainActivity)
| Command | Phrases |
|---------|---------|
| `mood_happy` | czuję się dobrze, czuję się świetnie, jestem szczęśliwy, bardzo dobrze, dobrze |
| `mood_sad` | jestem zmęczony, czuję się średnio, tak sobie, średnio |
| `mood_very_sad` | nie czuję się dobrze, czuję się źle, jestem chory, boli mnie, bardzo źle |

### Voice Command Flow
1. User speaks → SpeechRecognizer captures audio
2. VoiceManager receives result via `onSpeechResult`
3. VoiceNavigator calls `VoiceCommands.matchCommand(text)`
4. Matched command string returned (e.g., "back", "home", "games")
5. Navigation commands handled by VoiceNavigator
6. Other commands passed to activity callback via `onVoiceCommand(command)`

### Adding Voice to New Screens
1. Add `VoiceNavigator voiceNavigator` field
2. Initialize in `onCreate` with callback
3. Handle commands in `handleVoiceCommand(String command)`
4. Call `voiceNavigator.speak(text)` to read content aloud
5. Cleanup in `onDestroy`

### Animations
- `fab_pulse.xml`: Pulse animation for listening FAB
- `fab_click.xml`: Click feedback animation

## Design Conventions

### UI Text Style
- **Game titles**: Uppercase, bold, 44-48sp, color #004A99
- **Headers**: Uppercase, bold, 20-22sp, color #004A99
- **Body text**: Bold, 18-22sp, color #1E293B
- **Instruction text**: Bold, 22sp with lineSpacingExtra="8dp"

### Colors
- Primary blue: #004A99
- Success green: #057A32
- Background: #F8FAFC
- Card background: #FFFFFF
- Light blue accent: #DBEAFE
- Neon green (for game highlights): #39FF14

### Mind Game Visual Enhancements

#### Color Tap Game (ColorTapActivity)
- When a color flashes, it changes to **neon green (#39FF14)** for maximum visibility
- Flash duration: 400ms
- After flash, returns to original color

#### Liquid Sort Game (LiquidSortActivity)
- Selected tube displays **neon green (#39FF14)** glow effect
- Glow is drawn as multiple stroke layers around the tube outline
- Selection animation: tube moves up by 60dp

## Key Classes

| Class | Purpose |
|-------|---------|
| `MainActivity` | Home screen with mood cards, triggers ML recommendation |
| `ModelRunner` | Loads and runs ONNX inference |
| `AppDatabase` | Room database singleton |
| `ExerciseDao` | DAO with queries for filtering by category/difficulty/intensity |
| `CsvImporter` | Parses CSV to `Exercise` entities |
| `OnboardingActivity` | 4-step ViewPager2 wizard collecting user capabilities |
| `ChoiceActivity` | Hub choosing between Body exercises and Mind games |
| `MindGamesActivity` | Container for memory/2048 games |
| `GameInstructionActivity` | Shows instruction screen before each mind game |
| `ColorTapActivity` | Color sequence memory game |
| `LiquidSortActivity` | Liquid sorting puzzle game |
| `MemoryGameActivity` | Card matching memory game |
| `VoiceManager` | Singleton for TTS and speech recognition |
| `VoiceNavigator` | Per-activity voice handling |
| `VoiceCommands` | Maps Polish phrases to canonical commands |

## Asset Files

- `assets/cwiczenia_seniorzy.csv` - Exercise database
- `assets/rf_rekomendator.onnx` - Trained sklearn model
- `assets/metadata.json` - Model class labels

## Adding New Mind Games

1. Add game type constant to `GameInstructionActivity.java`
2. Add case in `setupGameInstruction()` with icon, title, instructions, and launch intent
3. Add click handler in `MindGamesActivity.java` to launch `GameInstructionActivity`
4. Use neon green (#39FF14) for any visual highlights in the game

## Adding Voice Support to New Activities

1. Add `VoiceNavigator` field and initialize in `onCreate()`
2. Create `handleVoiceCommand()` method for command handling
3. Add voice commands to `VoiceCommands.java` if needed
4. Call `speak()` to read content, `speakDelayed()` for delayed announcements
5. Cleanup in `onDestroy()`