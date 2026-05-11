<p align="center">
  <img src="https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android">
  <img src="https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java">
  <img src="https://img.shields.io/badge/Room-3DDC84?style=for-the-badge&logo=sqlite&logoColor=white" alt="Room">
  <img src="https://img.shields.io/badge/ONNX_Runtime-0054A6?style=for-the-badge&logo=onnx&logoColor=white" alt="ONNX">
</p>

<h1 align="center">🏃‍♂️ FitnessApp dla Seniorów</h1>

<p align="center">
  <strong>Aplikacja mobilna wspierająca aktywność fizyczną i trening umysłowy osób starszych</strong><br>
  <em>Z rekomendacjami opartymi o nastrój, sterowaniem głosem i grami logicznymi</em>
</p>

---

## 📱 O projekcie

FitnessApp to dedykowana aplikacja na Androida zaprojektowana z myślą o **seniorach**. Łączy w sobie spersonalizowane plany ćwiczeń fizycznych z treningiem umysłowym, wszystko w przyjaznym, dużym i czytelnym interfejsie z pełnym wsparciem sterowania głosowego.

Aplikacja rekomenduje ćwiczenia na podstawie **samopoczucia użytkownika**, wykorzystując wytrenowany model uczenia maszynowego (Random Forest) zapisany w formacie ONNX. Użytkownik może ćwiczyć przy krześle, łóżku, stojąc lub siedząc – system automatycznie dobiera odpowiednie ćwiczenia do możliwości fizycznych.

---

## ✨ Kluczowe funkcjonalności

| Moduł | Opis |
|-------|------|
| 🤖 **ML Rekomendacje** | Model Random Forest (ONNX) sugeruje kategorię ćwiczeń (siła, kardio, mobilność, równowaga, postura) na podstawie nastroju i profilu użytkownika |
| 🗣️ **Sterowanie głosem** | Pełna obsługa głosowa (TTS + Speech Recognition) – czytanie treści, komendy nawigacyjne, sterowanie grami |
| 🧠 **Gry umysłowe** | Memory, Color Tap, Liquid Sort, 2048 – trzy poziomy trudności, zapis wyników |
| 🏋️ **Baza ćwiczeń** | Ponad 100 ćwiczeń zaimportowanych z CSV do lokalnej bazy Room z filtrami (krzesło, łóżko, stojąc, siedząc) |
| 🎥 **Wideo instruktażowe** | Odtwarzacz wideo dla każdego ćwiczenia z instruktażem |
| 📊 **Historia aktywności** | Zapis ukończonych sesji ćwiczeń i gier z datą, czasem i wynikiem |
| ⚙️ **Ustawienia dostępności** | Schorzenia, możliwości fizyczne, szybkość syntezy mowy |

---

## 🎮 Gry umysłowe

Aplikacja zawiera **4 gry logiczne** wspierające pamięć, koncentrację i planowanie:

- 🃏 **Memory** – znajdź pary jednakowych obrazków (3×2, 4×3, 4×4, 5×4)
- 🎨 **Color Tap** – powtarzaj sekwencję kolorów (Simon Says)
- 🧪 **Liquid Sort** – sortuj kolorowe ciecze w probówkach
- 🔢 **2048** – łącz kafelki, aby osiągnąć cel (64, 512, 2048)

Każda gra posiada **3 poziomy trudności** oraz ekran instruktażu przed rozpoczęciem.

---

## 🗣️ System głosowy

Aplikacja posiada zaawansowany system głosowy zaprojektowany specjalnie dla seniorów:

- **TTS (Text-to-Speech)** – czyta treści w języku polskim, tempo `0.85x` dla lepszej zrozumiałości
- **Rozpoznawanie mowy** – pełna obsługa komend głosowych w każdym ekranie
- **Komendy** – *„następne ćwiczenie”*, *„czytaj opis”*, *„góra / dół / lewo / prawo”*, *„pomoc”*, *„stop”*
- **Ciągłe nasłuchiwanie** – system automatycznie restartuje rozpoznawanie po każdej komendzie

---

## 🏗️ Architektura

```
SplashActivity
    ↓
OnboardingActivity (4 kroki – zebranie możliwości fizycznych)
    ↓
MainActivity (ekran główny z wyborem nastroju)
    ↓
    ├─→ ChoiceActivity ──→ SingleExerciseActivity (trening)
    │                        └─→ ExerciseDetailActivity
    │
    └─→ MindGamesActivity ──→ GameInstructionActivity ──→ [Gra]
```

### Stack technologiczny

| Warstwa | Technologia |
|---------|-------------|
| Język | Java |
| UI | XML Layouts, Material Design Components |
| Baza danych | Room (SQLite) |
| ML | ONNX Runtime (Random Forest) |
| TTS / STT | Android TextToSpeech + SpeechRecognizer |
| Wideo | Android VideoView |
| Asynchroniczność | AppExecutors (własny singleton) |

---

## 📂 Struktura projektu

```
app/src/main/
├── assets/
│   ├── cwiczenia_seniorzy.csv      # Baza ćwiczeń
│   ├── rf_rekomendator.onnx        # Model ML
│   └── metadata.json               # Etykiety klas modelu
├── java/com/example/fitnessapp/
│   ├── MainActivity.java           # Ekran główny + nastrój
│   ├── SingleExerciseActivity.java # Przepływ treningu
│   ├── ExerciseDetailActivity.java # Szczegóły ćwiczenia
│   ├── SettingsActivity.java       # Ustawienia + historia
│   ├── MindGamesActivity.java      # Menu gier
│   ├── GameInstructionActivity.java# Instrukcje gry
│   ├── Game2048Activity.java       # Gra 2048
│   ├── ColorTapActivity.java       # Gra Color Tap
│   ├── LiquidSortActivity.java     # Gra Liquid Sort
│   ├── MemoryGameActivity.java     # Gra Memory
│   ├── OnboardingActivity.java     # Onboarding 4-krokowy
│   ├── HistoryActivity.java        # Historia aktywności
│   ├── voice/
│   │   ├── VoiceManager.java       # Singleton TTS + STT
│   │   ├── VoiceNavigator.java     # Obsługa głosu per-activity
│   │   ├── VoiceCommands.java      # Mapowanie komend PL
│   │   └── VoiceHelpDialog.java    # Dialog z komendami
│   ├── ml/
│   │   └── ModelRunner.java        # ONNX inference
│   ├── db/
│   │   ├── AppDatabase.java        # Room Database
│   │   ├── Exercise.java           # Encja ćwiczenia
│   │   ├── ExerciseDao.java        # DAO ćwiczeń
│   │   ├── ExerciseSession.java    # Encja sesji treningowej
│   │   └── GameSession.java        # Encja sesji gry
│   └── utils/
│       ├── AppExecutors.java       # Singleton wątków
│       ├── ScreenUtils.java        # Skalowanie UI
│       └── CsvImporter.java        # Import CSV do Room
└── res/
    ├── layout/                     # Layouty ekranów
    ├── values/strings.xml          # Wszystkie teksty (PL)
    └── drawable/                   # Ikony i tła
```

---

## 🚀 Uruchomienie

### Wymagania
- Android Studio (Ladybug lub nowsza)
- JDK 17
- Android SDK 34
- Urządzenie lub emulator z Android 8.0+ (API 26)

### Kompilacja

```bash
# Debug APK
./gradlew assembleDebug

# Instalacja na urządzeniu
./gradlew installDebug

# Testy jednostkowe
./gradlew test
```

### Model ML
Model ONNX (`rf_rekomendator.onnx`) jest automatycznie kopiowany z `assets/` do pamięci wewnętrznej przy pierwszym uruchomieniu aplikacji. Nie wymaga dodatkowej konfiguracji.

---

## 🎯 Cele projektu

1. **Dostępność** – duży kontrast, czytelne czcionki (20–44sp), wysokie przyciski
2. **Personalizacja** – ćwiczenia dopasowane do nastroju, schorzeń i możliwości fizycznych
3. **Multimodalność** – dotyk + głos, dla osób z ogriczoną sprawnością manualną
4. **Motywacja** – gry umysłowe + historia postępów zachęcają do regularnej aktywności


---

<p align="center">
  <sub>Stworzone z myślą o aktywności seniorów 💙</sub>
</p>
