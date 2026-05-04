package com.example.fitnessapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.Random;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String PREFS_NAME = "FitnessAppPrefs";
    private static final String KEY_USER_NAME = "user_name";

    private static final java.text.SimpleDateFormat TIME_FORMAT =
            new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
    private static final java.text.SimpleDateFormat DATE_FORMAT =
            new java.text.SimpleDateFormat("EEEE, d MMMM", new java.util.Locale("pl", "PL"));

    private static final String[] MOTIVATIONS = {
        "Każdy krok się liczy. Nawet mały ruch to postęp!",
        "Ćwiczenia to najlepszy prezent, jaki możesz dać swojemu ciału.",
        "Masz dziś dużo energii. Wykorzystaj ją!",
        "Ruch to zdrowie. Dbaj o siebie każdego dnia!",
        "Jesteś silniejszy niż myślisz!"
    };

    private static final String[] TIPS = {
        "Pamiętaj o nawodnieniu. Wypijaj co najmniej 8 szklanek wody dziennie.",
        "Regularne ćwiczenia poprawiają nastrój i sen.",
        "Rozciągaj się codziennie przez 10 minut.",
        "Spaceruj codziennie chociaż 30 minut.",
        "Dbaj o prawidłową postawę podczas siedzenia."
    };

    private ModelRunner modelRunner;
    private AppDatabase db;
    private volatile boolean dbReady = false;
    private VoiceNavigator voiceNavigator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Dodanie sprawdzenia uprawnień
        if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            androidx.core.app.ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.RECORD_AUDIO}, 1);
        }

        TextView tvWelcome = findViewById(R.id.tv_welcome_title);
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String name = prefs.getString(KEY_USER_NAME, "");
        if (!name.isEmpty() && tvWelcome != null) {
            tvWelcome.setText(getString(R.string.welcome_personalized, name));
        }

        NavbarHelper.initNavbar(this);

        // Initialize voice manager
        VoiceManager.getInstance().init(this);

        modelRunner = new ModelRunner();
        db = AppDatabase.getDatabase(this);
        try {
            modelRunner.init(this);
            new Thread(this::loadExerciseDatabase).start();
        } catch (Exception e) {
            Log.e(TAG, "Błąd inicjalizacji", e);
            Toast.makeText(this, "Błąd ładowania systemu rekomendacji", Toast.LENGTH_SHORT).show();
        }

        // Setup voice navigator with listening state callback
        voiceNavigator = new VoiceNavigator(this, new VoiceNavigator.VoiceCallback() {
            @Override
            public void onVoiceCommand(String command) {
                runOnUiThread(() -> handleVoiceCommand(command));
            }
        });
        voiceNavigator.setup();
        voiceNavigator.startListening();

        voiceNavigator.speakDelayed("Witaj! Jak się dzisiaj czujesz?", 500);

        // Help button listener
        ImageButton btnHelp = findViewById(R.id.btn_help);
        if (btnHelp != null) {
            btnHelp.setOnClickListener(v -> VoiceHelpDialog.show(this));
        }

        findViewById(R.id.card_mood_happy).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SingleExerciseActivity.class);
            intent.putExtra(SingleExerciseActivity.EXTRA_MOOD_TYPE, SingleExerciseActivity.MOOD_HAPPY);
            startActivity(intent);
        });
        findViewById(R.id.card_mood_sad).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SingleExerciseActivity.class);
            intent.putExtra(SingleExerciseActivity.EXTRA_MOOD_TYPE, SingleExerciseActivity.MOOD_SAD);
            startActivity(intent);
        });
        findViewById(R.id.card_mood_very_sad).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SingleExerciseActivity.class);
            intent.putExtra(SingleExerciseActivity.EXTRA_MOOD_TYPE, SingleExerciseActivity.MOOD_VERY_SAD);
            startActivity(intent);
        });
    }

    private void handleVoiceCommand(String command) {
        switch (command) {
            case "next":
                voiceNavigator.speak("Czuję się dobrze - trudniejsze ćwiczenia. Jestem zmęczony - umiarkowane. Nie czuję się dobrze - łatwe ćwiczenia.");
                break;
            case "exercises":
            case "read":
            case "repeat":
                voiceNavigator.speak("Wybierz nastrój aby otrzymać rekomendację ćwiczeń.");
                break;
            case "mood_happy":
                voiceNavigator.speak("Świetnie! Przygotowałam dla Ciebie zestaw intensywniejszych ćwiczeń.");
                navigateToMood(SingleExerciseActivity.MOOD_HAPPY);
                break;
            case "mood_sad":
                voiceNavigator.speak("Rozumiem. Spróbujmy łagodnych ćwiczeń na rozruszanie.");
                navigateToMood(SingleExerciseActivity.MOOD_SAD);
                break;
            case "mood_very_sad":
                voiceNavigator.speak("Pamiętaj, że ruch poprawia humor. Przygotowałam bardzo proste ćwiczenia.");
                navigateToMood(SingleExerciseActivity.MOOD_VERY_SAD);
                break;
            case "stop":
                voiceNavigator.stopSpeaking();
                break;
            case "help":
                voiceNavigator.speak("Powiedz 'dobrze' aby wybrać trudniejsze ćwiczenia, 'zmęczony' dla umiarkowanych, lub 'nie dobrze' dla łatwych.");
                break;
            case "time":
                String time = TIME_FORMAT.format(new java.util.Date());
                voiceNavigator.speak("Jest teraz godzina " + time.replace(":", " ") + ".");
                break;
            case "date":
                String date = DATE_FORMAT.format(new java.util.Date());
                voiceNavigator.speak("Dzisiaj jest " + date + ".");
                break;
            case "motivation":
                voiceNavigator.speak(MOTIVATIONS[new java.util.Random().nextInt(MOTIVATIONS.length)]);
                break;
            case "advice":
                voiceNavigator.speak(TIPS[new java.util.Random().nextInt(TIPS.length)]);
                break;
            case "weather":
                voiceNavigator.speak("Aktualnie nie mogę sprawdzić pogody, ale zachęcam do wyjścia na świeże powietrze!");
                break;
            case "quiet_mode":
                VoiceManager.getInstance().setQuietMode(true);
                voiceNavigator.speak("Wyciszam mikrofon. Okresowo będę nasłuchiwać cicho.");
                break;
            case "louder":
                VoiceManager.getInstance().setSpeechRate(0.85f);
                voiceNavigator.speak("Zwiększam głośność mowy.");
                break;
            case "slower":
                VoiceManager.getInstance().setSpeechRate(0.65f);
                voiceNavigator.speak("Zwalniam mowę.");
                break;
            case "faster":
                VoiceManager.getInstance().setSpeechRate(0.85f);
                voiceNavigator.speak("Przyspieszam mowę.");
                break;
            case "wake":
                voiceNavigator.speak("Tak? Słucham Cię uważnie. Powiedz czego potrzebujesz.");
                break;
        }
    }

    private void navigateToMood(int moodType) {
        Intent intent = new Intent(this, SingleExerciseActivity.class);
        intent.putExtra(SingleExerciseActivity.EXTRA_MOOD_TYPE, moodType);
        startActivity(intent);
    }

    private void navigateTo(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private static final String KEY_CSV_VERSION = "csv_version";

    private void loadExerciseDatabase() {
        try {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            int currentCsvVersion = CsvImporter.CSV_VERSION;
            int savedCsvVersion = prefs.getInt(KEY_CSV_VERSION, 0);

            int count = db.exerciseDao().getCount();
            Log.d(TAG, "Aktualna liczba ćwiczeń w bazie: " + count);

            if (count > 0 && savedCsvVersion == currentCsvVersion) {
                Log.d(TAG, "Baza zawiera " + count + " ćwiczeń i wersja CSV się nie zmieniła. Pomijam import.");
                dbReady = true;
                return;
            }

            Log.d(TAG, "Importuję ćwiczenia z CSV (wersja " + currentCsvVersion + ")...");
            List<Exercise> exercises = CsvImporter.loadExercisesFromCsv(this);
            if (!exercises.isEmpty()) {
                db.exerciseDao().replaceAll(exercises);
                int newCount = db.exerciseDao().getCount();
                Log.d(TAG, "Zaimportowano " + exercises.size() + " ćwiczeń. Nowy stan bazy: " + newCount);
                prefs.edit().putInt(KEY_CSV_VERSION, currentCsvVersion).apply();
                dbReady = true;
            } else {
                Log.e(TAG, "Nie zaimportowano żadnych ćwiczeń!");
                dbReady = true;
            }
        } catch (Throwable t) {
            Log.e(TAG, "Błąd bazy danych", t);
            dbReady = true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (modelRunner != null) {
            try {
                modelRunner.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (voiceNavigator != null) {
            voiceNavigator.cleanup();
        }
    }


}
