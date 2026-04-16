# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

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

## Asset Files

- `assets/cwiczenia_seniorzy.csv` - Exercise database
- `assets/rf_rekomendator.onnx` - Trained sklearn model
- `assets/metadata.json` - Model class labels