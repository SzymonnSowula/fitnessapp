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

## Asset Files

- `assets/cwiczenia_seniorzy.csv` - Exercise database
- `assets/rf_rekomendator.onnx` - Trained sklearn model
- `assets/metadata.json` - Model class labels

## Adding New Mind Games

1. Add game type constant to `GameInstructionActivity.java`
2. Add case in `setupGameInstruction()` with icon, title, instructions, and launch intent
3. Add click handler in `MindGamesActivity.java` to launch `GameInstructionActivity`
4. Use neon green (#39FF14) for any visual highlights in the game