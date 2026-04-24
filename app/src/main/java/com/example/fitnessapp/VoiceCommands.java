package com.example.fitnessapp;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class VoiceCommands {

    private static final String TAG = "VoiceCommands";

    // Command categories - each maps phrase variants to canonical command
    public static final Map<String, String> NAVIGATION_COMMANDS = new HashMap<>();
    public static final Map<String, String> EXERCISE_COMMANDS = new HashMap<>();
    public static final Map<String, String> GAME_COMMANDS = new HashMap<>();
    public static final Map<String, String> MOOD_COMMANDS = new HashMap<>();
    public static final Map<String, String> GENERAL_COMMANDS = new HashMap<>();

    static {
        // ============ GENERAL COMMANDS ============
        GENERAL_COMMANDS.put("pomoc", "help");
        GENERAL_COMMANDS.put("komendy", "help");
        GENERAL_COMMANDS.put("co mogę powiedzieć", "help");
        GENERAL_COMMANDS.put("co moge powiedziec", "help");
        GENERAL_COMMANDS.put("co mogę mówić", "help");
        GENERAL_COMMANDS.put("co moge Mowic", "help");
        GENERAL_COMMANDS.put("asystent", "help");
        GENERAL_COMMANDS.put("wszystkie komendy", "help");
        GENERAL_COMMANDS.put("listy komend", "help");

        GENERAL_COMMANDS.put("stop", "stop");
        GENERAL_COMMANDS.put("cisza", "stop");
        GENERAL_COMMANDS.put("milcz", "stop");
        GENERAL_COMMANDS.put("zamilknij", "stop");
        GENERAL_COMMANDS.put("przestań mówić", "stop");
        GENERAL_COMMANDS.put("przestan Mowic", "stop");
        GENERAL_COMMANDS.put("zamknij się", "stop");
        GENERAL_COMMANDS.put("zamknij sie", "stop");

        GENERAL_COMMANDS.put("tak", "confirm");
        GENERAL_COMMANDS.put("okej", "confirm");
        GENERAL_COMMANDS.put("ok", "confirm");
        GENERAL_COMMANDS.put("potwierdzam", "confirm");
        GENERAL_COMMANDS.put("dobrze", "confirm");
        GENERAL_COMMANDS.put("zgadzam się", "confirm");
        GENERAL_COMMANDS.put("zgadzam sie", "confirm");

        GENERAL_COMMANDS.put("nie", "cancel");
        GENERAL_COMMANDS.put("anuluj", "cancel");
        GENERAL_COMMANDS.put("nie chcę", "cancel");
        GENERAL_COMMANDS.put("nie chce", "cancel");
        GENERAL_COMMANDS.put("zrezygnuj", "cancel");

        GENERAL_COMMANDS.put("czytaj", "read");
        GENERAL_COMMANDS.put("przeczytaj", "read");
        GENERAL_COMMANDS.put("czytaj na głos", "read");
        GENERAL_COMMANDS.put("czytaj na glos", "read");
        GENERAL_COMMANDS.put("przeczytaj na głos", "read");
        GENERAL_COMMANDS.put("przeczytaj na glos", "read");
        GENERAL_COMMANDS.put("odczytaj", "read");

        GENERAL_COMMANDS.put("powtórz", "repeat");
        GENERAL_COMMANDS.put("powtorz", "repeat");
        GENERAL_COMMANDS.put("jeszcze raz", "repeat");
        GENERAL_COMMANDS.put("powtórz to", "repeat");
        GENERAL_COMMANDS.put("powtorz to", "repeat");

        // ============ NAVIGATION COMMANDS ============
        NAVIGATION_COMMANDS.put("wstecz", "back");
        NAVIGATION_COMMANDS.put("cofnij", "back");
        NAVIGATION_COMMANDS.put("cofnij się", "back");
        NAVIGATION_COMMANDS.put("cofnij sie", "back");
        NAVIGATION_COMMANDS.put("powrót", "back");
        NAVIGATION_COMMANDS.put("powrot", "back");
        NAVIGATION_COMMANDS.put("wróć", "back");
        NAVIGATION_COMMANDS.put("wroc", "back");
        NAVIGATION_COMMANDS.put("wróć do tyłu", "back");
        NAVIGATION_COMMANDS.put("wroc do tylu", "back");

        NAVIGATION_COMMANDS.put("wyjdź", "exit");
        NAVIGATION_COMMANDS.put("wyjdz", "exit");
        NAVIGATION_COMMANDS.put("wyjscie", "exit");
        NAVIGATION_COMMANDS.put("exit", "exit");
        NAVIGATION_COMMANDS.put("zamknij", "exit");
        NAVIGATION_COMMANDS.put("zakończ", "exit");
        NAVIGATION_COMMANDS.put("zakoncz", "exit");
        NAVIGATION_COMMANDS.put("koniec", "exit");
        NAVIGATION_COMMANDS.put("wyloguj", "exit");
        NAVIGATION_COMMANDS.put("wyloguj się", "exit");
        NAVIGATION_COMMANDS.put("wyloguj sie", "exit");

        NAVIGATION_COMMANDS.put("home", "home");
        NAVIGATION_COMMANDS.put("strona główna", "home");
        NAVIGATION_COMMANDS.put("strona glowna", "home");
        NAVIGATION_COMMANDS.put("główna", "home");
        NAVIGATION_COMMANDS.put("glowna", "home");
        NAVIGATION_COMMANDS.put("menu główne", "home");
        NAVIGATION_COMMANDS.put("menu glowne", "home");
        NAVIGATION_COMMANDS.put("idź do domu", "home");
        NAVIGATION_COMMANDS.put("idz do domu", "home");
        NAVIGATION_COMMANDS.put("strona startowa", "home");
        NAVIGATION_COMMANDS.put("powrót do domu", "home");
        NAVIGATION_COMMANDS.put("powrot do domu", "home");

        NAVIGATION_COMMANDS.put("ćwiczenia", "exercises");
        NAVIGATION_COMMANDS.put("cwiczenia", "exercises");
        NAVIGATION_COMMANDS.put("ćwiczyć", "exercises");
        NAVIGATION_COMMANDS.put("cwiczyc", "exercises");
        NAVIGATION_COMMANDS.put("przejdź do ćwiczeń", "exercises");
        NAVIGATION_COMMANDS.put("przejdz do cwiczen", "exercises");
        NAVIGATION_COMMANDS.put("chcę ćwiczyć", "exercises");
        NAVIGATION_COMMANDS.put("chce cwiczyc", "exercises");
        NAVIGATION_COMMANDS.put("trening", "exercises");
        NAVIGATION_COMMANDS.put("trenuj", "exercises");
        NAVIGATION_COMMANDS.put("zaczniemy ćwiczyć", "exercises");
        NAVIGATION_COMMANDS.put("zaczniemy cwiczyc", "exercises");
        NAVIGATION_COMMANDS.put("ciało", "body");
        NAVIGATION_COMMANDS.put("cialo", "body");
        NAVIGATION_COMMANDS.put("ćwiczenia ciała", "body");
        NAVIGATION_COMMANDS.put("cwiczenia ciala", "body");

        NAVIGATION_COMMANDS.put("umysł", "mind");
        NAVIGATION_COMMANDS.put("umysl", "mind");
        NAVIGATION_COMMANDS.put("umysłowe", "mind");
        NAVIGATION_COMMANDS.put("umyslowe", "mind");
        NAVIGATION_COMMANDS.put("gry", "games");
        NAVIGATION_COMMANDS.put("pobawmy się", "games");
        NAVIGATION_COMMANDS.put("pobawmy sie", "games");
        NAVIGATION_COMMANDS.put("pobawmy się grami", "games");
        NAVIGATION_COMMANDS.put("pobawmy sie grami", "games");
        NAVIGATION_COMMANDS.put("otwórz gry", "games");
        NAVIGATION_COMMANDS.put("otworz gry", "games");
        NAVIGATION_COMMANDS.put("zagrajmy", "games");
        NAVIGATION_COMMANDS.put("zagramy", "games");
        NAVIGATION_COMMANDS.put("zaczniemy grę", "games");
        NAVIGATION_COMMANDS.put("zaczniemy gre", "games");
        NAVIGATION_COMMANDS.put("gramy", "games");

        NAVIGATION_COMMANDS.put("ustawienia", "settings");
        NAVIGATION_COMMANDS.put("ustawienie", "settings");
        NAVIGATION_COMMANDS.put("opcje", "settings");
        NAVIGATION_COMMANDS.put("konfiguracja", "settings");
        NAVIGATION_COMMANDS.put("config", "settings");
        NAVIGATION_COMMANDS.put("preferences", "settings");
        NAVIGATION_COMMANDS.put("preferencje", "settings");

        NAVIGATION_COMMANDS.put("profil", "profile");
        NAVIGATION_COMMANDS.put("konto", "profile");
        NAVIGATION_COMMANDS.put("moje konto", "profile");
        NAVIGATION_COMMANDS.put("moje dane", "profile");

        NAVIGATION_COMMANDS.put("następne", "next");
        NAVIGATION_COMMANDS.put("nastepne", "next");
        NAVIGATION_COMMANDS.put("dalej", "next");
        NAVIGATION_COMMANDS.put("przejdź dalej", "next");
        NAVIGATION_COMMANDS.put("przejdz dalej", "next");
        NAVIGATION_COMMANDS.put("kolejne", "next");
        NAVIGATION_COMMANDS.put("następny", "next");
        NAVIGATION_COMMANDS.put("nastepny", "next");

        NAVIGATION_COMMANDS.put("poprzednie", "previous");
        NAVIGATION_COMMANDS.put("poprzedni", "previous");
        NAVIGATION_COMMANDS.put("wstecz", "previous");
        NAVIGATION_COMMANDS.put("cofnij", "previous");

        // ============ EXERCISE COMMANDS ============
        EXERCISE_COMMANDS.put("następne ćwiczenie", "next_exercise");
        EXERCISE_COMMANDS.put("nastepne cwiczenie", "next_exercise");
        EXERCISE_COMMANDS.put("kolejne ćwiczenie", "next_exercise");
        EXERCISE_COMMANDS.put("idź do następnego", "next_exercise");
        NAVIGATION_COMMANDS.put("poprzednie ćwiczenie", "previous_exercise");
        NAVIGATION_COMMANDS.put("poprzednie cwiczenie", "previous_exercise");

        EXERCISE_COMMANDS.put("start", "start");
        EXERCISE_COMMANDS.put("rozpocznij", "start");
        EXERCISE_COMMANDS.put("zacznij", "start");
        EXERCISE_COMMANDS.put("zaczynamy", "start");
        EXERCISE_COMMANDS.put("teraz", "start");
        EXERCISE_COMMANDS.put("no to lecimy", "start");
        EXERCISE_COMMANDS.put("lecisz", "start");

        EXERCISE_COMMANDS.put("zakończ", "finish");
        EXERCISE_COMMANDS.put("zakoncz", "finish");
        EXERCISE_COMMANDS.put("koniec", "finish");
        EXERCISE_COMMANDS.put("stop", "finish");
        EXERCISE_COMMANDS.put("zatrzymaj", "finish");
        EXERCISE_COMMANDS.put("koniec ćwiczenia", "finish");
        EXERCISE_COMMANDS.put("koniec cwiczenia", "finish");
        EXERCISE_COMMANDS.put("zakończ ćwiczenie", "finish");
        EXERCISE_COMMANDS.put("zakoncz cwiczenie", "finish");

        EXERCISE_COMMANDS.put("czytaj opis", "read_description");
        EXERCISE_COMMANDS.put("czytaj opis ćwiczenia", "read_description");
        EXERCISE_COMMANDS.put("opowiedz o ćwiczeniu", "read_description");
        EXERCISE_COMMANDS.put("co to ćwiczenie", "read_description");
        EXERCISE_COMMANDS.put("co to cwiczenie", "read_description");
        EXERCISE_COMMANDS.put("opisz ćwiczenie", "read_description");
        EXERCISE_COMMANDS.put("opisz cwiczenie", "read_description");

        EXERCISE_COMMANDS.put("ile powtórzeń", "ask_reps");
        EXERCISE_COMMANDS.put("ile powtorzen", "ask_reps");
        EXERCISE_COMMANDS.put("ile razy", "ask_reps");
        EXERCISE_COMMANDS.put("powtórzenia", "ask_reps");
        EXERCISE_COMMANDS.put("powtorzenia", "ask_reps");

        EXERCISE_COMMANDS.put("jak długo", "ask_duration");
        EXERCISE_COMMANDS.put("jak dlugo", "ask_duration");
        EXERCISE_COMMANDS.put("czas", "ask_duration");
        EXERCISE_COMMANDS.put("sekundy", "ask_duration");
        EXERCISE_COMMANDS.put("minuty", "ask_duration");

        // ============ GAME COMMANDS ============
        GAME_COMMANDS.put("nowa gra", "new_game");
        GAME_COMMANDS.put("nowa", "new_game");
        GAME_COMMANDS.put("new game", "new_game");
        GAME_COMMANDS.put("reset", "reset");
        GAME_COMMANDS.put("resetuj", "reset");
        GAME_COMMANDS.put("restartuj", "restart");
        GAME_COMMANDS.put("restart", "restart");
        GAME_COMMANDS.put("zacznij od nowa", "restart");
        GAME_COMMANDS.put("od nowa", "restart");
        GAME_COMMANDS.put("od początku", "restart");
        GAME_COMMANDS.put("od poczatku", "restart");

        GAME_COMMANDS.put("następny poziom", "next_level");
        GAME_COMMANDS.put("nastepny poziom", "next_level");
        GAME_COMMANDS.put("poziom wyżej", "next_level");
        GAME_COMMANDS.put("poziom wyzej", "next_level");
        GAME_COMMANDS.put("kolejny poziom", "next_level");
        GAME_COMMANDS.put("następny level", "next_level");
        GAME_COMMANDS.put("nastepny level", "next_level");

        GAME_COMMANDS.put("poprzedni poziom", "previous_level");
        GAME_COMMANDS.put("poziom niżej", "previous_level");
        GAME_COMMANDS.put("poziom nizej", "previous_level");

        GAME_COMMANDS.put("ładnie", "good");
        GAME_COMMANDS.put("ladnie", "good");
        GAME_COMMANDS.put("dobrze", "good");
        GAME_COMMANDS.put("super", "good");
        GAME_COMMANDS.put("świetnie", "good");
        GAME_COMMANDS.put("swietnie", "good");
        GAME_COMMANDS.put("brawo", "good");
        GAME_COMMANDS.put("hura", "good");

        GAME_COMMANDS.put("źle", "wrong");
        GAME_COMMANDS.put("zle", "wrong");
        GAME_COMMANDS.put("nie dobrze", "wrong");
        GAME_COMMANDS.put("niepoprawne", "wrong");
        GAME_COMMANDS.put("błąd", "wrong");
        GAME_COMMANDS.put("blad", "wrong");
        GAME_COMMANDS.put("pudło", "wrong");
        GAME_COMMANDS.put("pudlo", "wrong");

        GAME_COMMANDS.put("memory", "game_memory");
        GAME_COMMANDS.put("pamięć", "game_memory");
        GAME_COMMANDS.put("pamiec", "game_memory");
        GAME_COMMANDS.put("gra pamięć", "game_memory");
        GAME_COMMANDS.put("gra pamiec", "game_memory");
        GAME_COMMANDS.put("karty", "game_memory");

        GAME_COMMANDS.put("kolory", "game_colors");
        GAME_COMMANDS.put("barwy", "game_colors");
        GAME_COMMANDS.put("gra kolory", "game_colors");
        GAME_COMMANDS.put("sekwencja", "game_colors");
        GAME_COMMANDS.put("kolor", "game_colors");

        GAME_COMMANDS.put("płyny", "game_liquid");
        GAME_COMMANDS.put("plyny", "game_liquid");
        GAME_COMMANDS.put("płyn", "game_liquid");
        GAME_COMMANDS.put("plyn", "game_liquid");
        GAME_COMMANDS.put("sortowanie", "game_liquid");
        GAME_COMMANDS.put("probówki", "game_liquid");
        GAME_COMMANDS.put("probowki", "game_liquid");
        GAME_COMMANDS.put("probówka", "game_liquid");

        GAME_COMMANDS.put("łatwy", "easy");
        GAME_COMMANDS.put("poziom łatwy", "easy");
        GAME_COMMANDS.put("latwy", "easy");
        GAME_COMMANDS.put("średni", "medium");
        GAME_COMMANDS.put("poziom średni", "medium");
        GAME_COMMANDS.put("sredni", "medium");
        GAME_COMMANDS.put("trudny", "hard");
        GAME_COMMANDS.put("poziom trudny", "hard");

        GAME_COMMANDS.put("gra 2048", "game_2048");
        GAME_COMMANDS.put("2048", "game_2048");
        GAME_COMMANDS.put("dwadzieścia cztery osiem", "game_2048");
        GAME_COMMANDS.put("dwadziescia cztery osiem", "game_2048");

        GAME_COMMANDS.put("góra", "move_up");
        GAME_COMMANDS.put("gora", "move_up");
        GAME_COMMANDS.put("do góry", "move_up");
        GAME_COMMANDS.put("do gory", "move_up");
        GAME_COMMANDS.put("dół", "move_down");
        GAME_COMMANDS.put("dol", "move_down");
        GAME_COMMANDS.put("w dół", "move_down");
        GAME_COMMANDS.put("w dol", "move_down");
        GAME_COMMANDS.put("lewo", "move_left");
        GAME_COMMANDS.put("w lewo", "move_left");
        GAME_COMMANDS.put("prawo", "move_right");
        GAME_COMMANDS.put("w prawo", "move_right");

        // ============ MOOD COMMANDS ============
        MOOD_COMMANDS.put("czuję się dobrze", "mood_happy");
        MOOD_COMMANDS.put("czuje sie dobrze", "mood_happy");
        MOOD_COMMANDS.put("czuję się świetnie", "mood_happy");
        MOOD_COMMANDS.put("czuje sie swietnie", "mood_happy");
        MOOD_COMMANDS.put("jestem szczęśliwy", "mood_happy");
        MOOD_COMMANDS.put("jestem szczesliwy", "mood_happy");
        MOOD_COMMANDS.put("bardzo dobrze", "mood_happy");
        MOOD_COMMANDS.put("dobrze", "mood_happy");
        MOOD_COMMANDS.put("w porządku", "mood_happy");
        MOOD_COMMANDS.put("w porzadku", "mood_happy");
        MOOD_COMMANDS.put("okej", "mood_happy");
        MOOD_COMMANDS.put("świetnie", "mood_happy");
        MOOD_COMMANDS.put("swietnie", "mood_happy");
        MOOD_COMMANDS.put("fantastycznie", "mood_happy");
        MOOD_COMMANDS.put("wspaniale", "mood_happy");

        MOOD_COMMANDS.put("jestem zmęczony", "mood_sad");
        MOOD_COMMANDS.put("jestem zmeczony", "mood_sad");
        MOOD_COMMANDS.put("czuję się średnio", "mood_sad");
        MOOD_COMMANDS.put("czuje sie srednio", "mood_sad");
        MOOD_COMMANDS.put("tak sobie", "mood_sad");
        MOOD_COMMANDS.put("tak sobe", "mood_sad");
        MOOD_COMMANDS.put("srednio", "mood_sad");
        MOOD_COMMANDS.put("nic specjalnego", "mood_sad");
        MOOD_COMMANDS.put("męczące", "mood_sad");
        MOOD_COMMANDS.put("meczace", "mood_sad");

        MOOD_COMMANDS.put("nie czuję się dobrze", "mood_very_sad");
        MOOD_COMMANDS.put("nie czuje sie dobrze", "mood_very_sad");
        MOOD_COMMANDS.put("czuję się źle", "mood_very_sad");
        MOOD_COMMANDS.put("czuje sie zle", "mood_very_sad");
        MOOD_COMMANDS.put("jestem chory", "mood_very_sad");
        MOOD_COMMANDS.put("boli mnie", "mood_very_sad");
        MOOD_COMMANDS.put("nie dobrze", "mood_very_sad");
        MOOD_COMMANDS.put("bardzo źle", "mood_very_sad");
        MOOD_COMMANDS.put("bardzo zle", "mood_very_sad");
        MOOD_COMMANDS.put("okropnie", "mood_very_sad");
        MOOD_COMMANDS.put("źle", "mood_very_sad");
        MOOD_COMMANDS.put("zle", "mood_very_sad");
    }

    /**
     * Match spoken text against all command maps.
     * Returns the canonical command string or null if no match.
     */
    public static String matchCommand(String text) {
        if (text == null || text.trim().isEmpty()) return null;

        String lowerText = text.toLowerCase().trim();
        String paddedText = " " + lowerText + " ";

        // Check command maps in order of priority
        // MOOD commands checked first because they are specific phrases on MainActivity
        String res = searchInMap(paddedText, MOOD_COMMANDS);
        if (res == null) res = searchInMap(paddedText, NAVIGATION_COMMANDS);
        if (res == null) res = searchInMap(paddedText, EXERCISE_COMMANDS);
        if (res == null) res = searchInMap(paddedText, GAME_COMMANDS);
        if (res == null) res = searchInMap(paddedText, GENERAL_COMMANDS);

        return res;
    }

    /**
     * Search for a phrase within the padded text.
     * Uses word boundaries to avoid partial matches.
     */
    private static String searchInMap(String fullText, Map<String, String> map) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            // Check with word boundaries
            String phrase = " " + entry.getKey() + " ";
            if (fullText.contains(phrase)) {
                Log.d(TAG, "Matched: '" + entry.getKey() + "' -> '" + entry.getValue() + "'");
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * Get human-readable description of a command.
     */
    public static String getCommandDescription(String command) {
        switch (command) {
            // General
            case "help": return "Pokaż dostępne komendy głosowe";
            case "stop": return "Zatrzymaj mówienie";
            case "confirm": return "Potwierdź działanie";
            case "cancel": return "Anuluj działanie";
            case "read": return "Przeczytaj tekst na ekranie";
            case "repeat": return "Powtórz ostatnią wiadomość";

            // Navigation
            case "next": return "Przejdź do następnego elementu";
            case "previous": return "Wróć do poprzedniego elementu";
            case "back": return "Wróć do poprzedniego ekranu";
            case "exit": return "Wyjdź z aplikacji";
            case "home": return "Przejdź do strony głównej";
            case "exercises": return "Przejdź do ćwiczeń";
            case "games": return "Przejdź do gier";
            case "settings": return "Przejdź do ustawień";
            case "body": return "Przejdź do ćwiczeń ciała";
            case "mind": return "Przejdź do gier umysłowych";
            case "profile": return "Przejdź do profilu";

            // Exercises
            case "next_exercise": return "Przejdź do następnego ćwiczenia";
            case "previous_exercise": return "Przejdź do poprzedniego ćwiczenia";
            case "finish": return "Zakończ i wyjdź";
            case "start": return "Rozpocznij";
            case "read_description": return "Przeczytaj opis ćwiczenia";
            case "ask_reps": return "Ile powtórzeń?";
            case "ask_duration": return "Jak długo?";

            // Games
            case "new_game": return "Rozpocznij nową grę";
            case "reset":
            case "restart": return "Zacznij od nowa";
            case "next_level": return "Przejdź do następnego poziomu";
            case "previous_level": return "Wróć do poprzedniego poziomu";
            case "good": return "Dobrze!";
            case "wrong": return "Źle! Spróbuj ponownie";
            case "game_memory": return "Gra Memory";
            case "game_colors": return "Gra Kolory";
            case "game_liquid": return "Gra Płyny";
            case "game_2048": return "Gra 2048";
            case "move_up": return "Ruch w górę";
            case "move_down": return "Ruch w dół";
            case "move_left": return "Ruch w lewo";
            case "move_right": return "Ruch w prawo";

            // Mood
            case "mood_happy": return "Czuję się dobrze";
            case "mood_sad": return "Czuję się średnio";
            case "mood_very_sad": return "Czuję się źle";

            default: return "Nieznana komenda";
        }
    }

    /**
     * Get help text for general commands.
     */
    public static String getHelpText() {
        StringBuilder sb = new StringBuilder();
        sb.append("DOSTĘPNE KOMENDY GŁOSOWE:\n\n");
        sb.append("NAWIGACJA:\n");
        sb.append("• główna lub menu - powrót do strony głównej\n");
        sb.append("• wstecz lub cofnij - wróć do poprzedniego ekranu\n");
        sb.append("• ćwiczenia - przejdź do ćwiczeń\n");
        sb.append("• gry - przejdź do gier umysłowych\n");
        sb.append("• ustawienia - przejdź do ustawień\n");
        sb.append("• wyjdź - zamknij aplikację\n\n");
        sb.append("OGÓLNE:\n");
        sb.append("• pomoc - pokaż wszystkie komendy\n");
        sb.append("• stop lub cisza - zatrzymaj mówienie\n");
        sb.append("• czytaj - przeczytaj tekst na ekranie\n");
        sb.append("• powtórz - powtórz ostatnią wiadomość\n");
        return sb.toString();
    }

    /**
     * Get help text for exercise screens.
     */
    public static String getExerciseHelpText() {
        StringBuilder sb = new StringBuilder();
        sb.append("KOMENDY DLA ĆWICZEŃ:\n\n");
        sb.append("• następne ćwiczenie - przejdź dalej\n");
        sb.append("• start lub zaczynamy - rozpocznij ćwiczenie\n");
        sb.append("• zakończ lub koniec - zakończ trening\n");
        sb.append("• czytaj opis - przeczytaj opis ćwiczenia\n");
        sb.append("• wstecz - wróć do poprzedniego ekranu\n");
        return sb.toString();
    }

    /**
     * Get help text for game screens.
     */
    public static String getGameHelpText() {
        StringBuilder sb = new StringBuilder();
        sb.append("KOMENDY DLA GIER:\n\n");
        sb.append("• nowa gra lub restart - zacznij od nowa\n");
        sb.append("• następny poziom - przejdź dalej\n");
        sb.append("• kolory lub memory lub płyny - wybierz grę\n");
        sb.append("• wstecz - wróć do wyboru gier\n");
        return sb.toString();
    }

    /**
     * Check if a command is a game-related command.
     */
    public static boolean isGameCommand(String command) {
        return command != null && command.startsWith("game_");
    }

    /**
     * Check if a command is a navigation command.
     */
    public static boolean isNavigationCommand(String command) {
        return command != null && (
            command.equals("home") ||
            command.equals("back") ||
            command.equals("exit") ||
            command.equals("exercises") ||
            command.equals("games") ||
            command.equals("settings") ||
            command.equals("body") ||
            command.equals("mind") ||
            command.equals("profile") ||
            command.equals("back_main")
        );
    }
}