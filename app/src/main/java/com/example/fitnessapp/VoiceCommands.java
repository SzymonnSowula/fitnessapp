package com.example.fitnessapp;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class VoiceCommands {

    private static final String TAG = "VoiceCommands";

    public static final Map<String, String> NAVIGATION_COMMANDS = new HashMap<>();
    public static final Map<String, String> EXERCISE_COMMANDS = new HashMap<>();
    public static final Map<String, String> GAME_COMMANDS = new HashMap<>();
    public static final Map<String, String> MOOD_COMMANDS = new HashMap<>();

    static {
        NAVIGATION_COMMANDS.put("następne", "next");
        NAVIGATION_COMMANDS.put("nastepne", "next");
        NAVIGATION_COMMANDS.put("dalej", "next");
        NAVIGATION_COMMANDS.put("przejdź dalej", "next");
        NAVIGATION_COMMANDS.put("przejdz dalej", "next");
        NAVIGATION_COMMANDS.put("poprzednie", "previous");
        NAVIGATION_COMMANDS.put("poprzedni", "previous");
        NAVIGATION_COMMANDS.put("wstecz", "back");
        NAVIGATION_COMMANDS.put("wyjdź", "exit");
        NAVIGATION_COMMANDS.put("wyjdz", "exit");
        NAVIGATION_COMMANDS.put("exit", "exit");
        NAVIGATION_COMMANDS.put("zamknij", "exit");
        NAVIGATION_COMMANDS.put("czytaj więcej", "read_more");
        NAVIGATION_COMMANDS.put("czytaj wiecej", "read_more");
        NAVIGATION_COMMANDS.put("czytaj opis", "read_description");
        NAVIGATION_COMMANDS.put("czytaj", "read");
        NAVIGATION_COMMANDS.put("powtórz", "repeat");
        NAVIGATION_COMMANDS.put("powtorz", "repeat");
        NAVIGATION_COMMANDS.put("stop", "stop");
        NAVIGATION_COMMANDS.put("cisza", "stop");
        NAVIGATION_COMMANDS.put("milcz", "stop");
        NAVIGATION_COMMANDS.put("pomoc", "help");
        NAVIGATION_COMMANDS.put("komendy", "help");
        NAVIGATION_COMMANDS.put("home", "home");
        NAVIGATION_COMMANDS.put("strona główna", "home");
        NAVIGATION_COMMANDS.put("strona glowna", "home");
        NAVIGATION_COMMANDS.put("ćwiczenia", "exercises");
        NAVIGATION_COMMANDS.put("cwiczenia", "exercises");
        NAVIGATION_COMMANDS.put("przejdź do ćwiczeń", "exercises");
        NAVIGATION_COMMANDS.put("przejdz do cwiczen", "exercises");
        NAVIGATION_COMMANDS.put("chcę ćwiczyć", "exercises");
        NAVIGATION_COMMANDS.put("chce cwiczyc", "exercises");
        NAVIGATION_COMMANDS.put("trening", "exercises");
        NAVIGATION_COMMANDS.put("idź do ćwiczeń", "exercises");
        NAVIGATION_COMMANDS.put("ciało", "body");
        NAVIGATION_COMMANDS.put("cialo", "body");
        NAVIGATION_COMMANDS.put("umysł", "mind");
        NAVIGATION_COMMANDS.put("umysl", "mind");
        NAVIGATION_COMMANDS.put("gry", "games");
        NAVIGATION_COMMANDS.put("pobawmy się", "games");
        NAVIGATION_COMMANDS.put("pobawmy sie", "games");
        NAVIGATION_COMMANDS.put("otwórz gry", "games");
        NAVIGATION_COMMANDS.put("ustawienia", "settings");
        NAVIGATION_COMMANDS.put("opcje", "settings");
        NAVIGATION_COMMANDS.put("konfiguracja", "settings");

        EXERCISE_COMMANDS.put("następne ćwiczenie", "next_exercise");
        EXERCISE_COMMANDS.put("nastepne cwiczenie", "next_exercise");
        EXERCISE_COMMANDS.put("kolejne ćwiczenie", "next_exercise");
        EXERCISE_COMMANDS.put("zakończ", "finish");
        EXERCISE_COMMANDS.put("zakoncz", "finish");
        EXERCISE_COMMANDS.put("koniec", "finish");
        EXERCISE_COMMANDS.put("start", "start");
        EXERCISE_COMMANDS.put("rozpocznij", "start");

        GAME_COMMANDS.put("nowa gra", "new_game");
        GAME_COMMANDS.put("reset", "reset");
        GAME_COMMANDS.put("resetuj", "reset");
        GAME_COMMANDS.put("restart", "restart");
        GAME_COMMANDS.put("następny poziom", "next_level");
        GAME_COMMANDS.put("nastepny poziom", "next_level");
        GAME_COMMANDS.put("poziom wyżej", "next_level");
        GAME_COMMANDS.put("poziom nizej", "previous_level");
        GAME_COMMANDS.put("ład", "good");
        GAME_COMMANDS.put("dobrze", "good");
        GAME_COMMANDS.put("źle", "wrong");
        GAME_COMMANDS.put("zle", "wrong");
        GAME_COMMANDS.put("memory", "game_memory");
        GAME_COMMANDS.put("pamięć", "game_memory");
        GAME_COMMANDS.put("pamiec", "game_memory");
        GAME_COMMANDS.put("kolory", "game_colors");
        GAME_COMMANDS.put("barwy", "game_colors");
        GAME_COMMANDS.put("płyny", "game_liquid");
        GAME_COMMANDS.put("plyny", "game_liquid");
        GAME_COMMANDS.put("sortowanie", "game_liquid");

        MOOD_COMMANDS.put("czuję się dobrze", "mood_happy");
        MOOD_COMMANDS.put("czuje sie dobrze", "mood_happy");
        MOOD_COMMANDS.put("czuję się świetnie", "mood_happy");
        MOOD_COMMANDS.put("czuje sie swietnie", "mood_happy");
        MOOD_COMMANDS.put("bardzo dobrze", "mood_happy");
        MOOD_COMMANDS.put("jestem zmęczony", "mood_sad");
        MOOD_COMMANDS.put("jestem zmeczony", "mood_sad");
        MOOD_COMMANDS.put("czuję się średnio", "mood_sad");
        MOOD_COMMANDS.put("czuje sie srednio", "mood_sad");
        MOOD_COMMANDS.put("tak sobie", "mood_sad");
        MOOD_COMMANDS.put("nie czuję się dobrze", "mood_very_sad");
        MOOD_COMMANDS.put("nie czuje sie dobrze", "mood_very_sad");
        MOOD_COMMANDS.put("czuję się źle", "mood_very_sad");
        MOOD_COMMANDS.put("czuje sie zle", "mood_very_sad");
        MOOD_COMMANDS.put("bardzo źle", "mood_very_sad");
        MOOD_COMMANDS.put("bardzo zle", "mood_very_sad");
    }

    public static String matchCommand(String text) {
        if (text == null || text.trim().isEmpty()) return null;

        String lowerText = text.toLowerCase().trim();
        String paddedText = " " + lowerText + " ";

        // Szukamy w mapach całych fraz, bez wymuszania słowa kluczowego
        String res = searchInMap(paddedText, NAVIGATION_COMMANDS);
        if (res == null) res = searchInMap(paddedText, EXERCISE_COMMANDS);
        if (res == null) res = searchInMap(paddedText, GAME_COMMANDS);
        if (res == null) res = searchInMap(paddedText, MOOD_COMMANDS);

        return res;
    }

    private static String searchInMap(String fullText, Map<String, String> map) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (fullText.contains(" " + entry.getKey() + " ")) {
                return entry.getValue();
            }
        }
        return null;
    }

    public static String getCommandDescription(String command) {
        switch (command) {
            case "next":
                return "Przejdź do następnego elementu";
            case "previous":
                return "Wróć do poprzedniego elementu";
            case "back":
                return "Wróć do poprzedniego ekranu";
            case "exit":
                return "Wyjdź z obecnego ekranu";
            case "read_more":
                return "Przeczytaj więcej informacji";
            case "read_description":
                return "Przeczytaj opis";
            case "read":
                return "Czytaj tekst na ekranie";
            case "repeat":
                return "Powtórz ostatnią wiadomość";
            case "stop":
                return "Zatrzymaj mówienie";
            case "help":
                return "Pokaż dostępne komendy";
            case "home":
                return "Przejdź do strony głównej";
            case "exercises":
                return "Przejdź do ćwiczeń";
            case "games":
                return "Przejdź do gier";
            case "settings":
                return "Przejdź do ustawień";
            case "next_exercise":
                return "Przejdź do następnego ćwiczenia";
            case "finish":
                return "Zakończ i wyjdź";
            case "start":
                return "Rozpocznij";
            case "new_game":
                return "Rozpocznij nową grę";
            case "reset":
            case "restart":
                return "Zacznij od nowa";
            case "next_level":
                return "Przejdź do następnego poziomu";
            default:
                return "Nieznana komenda";
        }
    }

    public static String getHelpText() {
        StringBuilder sb = new StringBuilder();
        sb.append("Dostępne komendy głosowe:\n");
        sb.append("• następne - następny element\n");
        sb.append("• poprzednie - poprzedni element\n");
        sb.append("• wstecz - wróć do tyłu\n");
        sb.append("• wyjdź - wyjdź z ekranu\n");
        sb.append("• czytaj - przeczytaj opis\n");
        sb.append("• stop - zatrzymaj mówienie\n");
        sb.append("• pomoc - pokaż tę listę\n");
        return sb.toString();
    }

    public static String getExerciseHelpText() {
        StringBuilder sb = new StringBuilder();
        sb.append("Komendy dla ćwiczeń:\n");
        sb.append("• następne ćwiczenie - następny\n");
        sb.append("• zakończ - zakończ trening\n");
        sb.append("• czytaj opis - przeczytaj opis ćwiczenia\n");
        return sb.toString();
    }

    public static String getGameHelpText() {
        StringBuilder sb = new StringBuilder();
        sb.append("Komendy dla gier:\n");
        sb.append("• nowa gra - rozpocznij od nowa\n");
        sb.append("• reset - resetuj grę\n");
        sb.append("• następny poziom - następny poziom\n");
        return sb.toString();
    }
}
