package com.example.fitnessapp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExerciseRecommender {

    public static class UserProfile {
        public boolean mozeStac;
        public boolean mozePodloge;
        public boolean krzeslo;
        public boolean lozko;
        public boolean mozeSiedzac;
        public int intensywnosc; 
        public int trudnosc;     
        public String cel;       
        public Set<String> schorzenia;
        public int samopoczucie; // 1=Źle, 2=Średnio, 3=Dobrze

        public UserProfile() {
            schorzenia = new HashSet<>();
            samopoczucie = 2;
        }
    }

    public static List<Exercise> recommend(List<Exercise> allExercises, UserProfile profile, int limit) {
        List<ScoredExercise> candidates = new ArrayList<>();

        int effectiveMaxIntensity = profile.intensywnosc;
        int effectiveMaxDifficulty = profile.trudnosc;

        if (profile.samopoczucie == 1) { 
            effectiveMaxIntensity = 1;
            effectiveMaxDifficulty = 1;
        } else if (profile.samopoczucie == 3) {
            // Zwiększamy limity, aby "odblokować" trudniejsze ćwiczenia
            effectiveMaxIntensity = Math.min(3, profile.intensywnosc + 1);
            effectiveMaxDifficulty = Math.min(3, profile.trudnosc + 1);
        }

        for (Exercise ex : allExercises) {
            // Filtry bezpieczeństwa
            if (!profile.mozeStac && ex.wymagaStaniaBin == 1) continue;
            if (!profile.mozePodloge && ex.wymagaPodlogiBin == 1 && ex.moznaWLozkuBin == 0) continue;
            if (profile.krzeslo && ex.wspomaganeKrzeslemBin == 0) continue;
            if (isContraindicated(ex, profile.schorzenia)) continue;

            // Filtry intensywności i trudności
            if (ex.intensywnoscNum > effectiveMaxIntensity) continue;
            if (ex.poziomTrudnosciNum > effectiveMaxDifficulty) continue;

            double score = 0;
            score += 2.0 * normalize(getGoalImpact(ex, profile.cel), 1, 3);
            score += 0.5 * normalize(ex.wplywNaRownowageNum, 1, 3);
            
            double schorzeniaScore = computeSchorzeniaScore(ex, profile.schorzenia);
            score += 4.0 * schorzeniaScore;

            // BONUS ZA TRUDNOŚĆ (Tylko gdy użytkownik czuje się dobrze)
            // To sprawi, że średnie/trudne ćwiczenia trafią na początek listy
            if (profile.samopoczucie == 3) {
                score += 1.0 * normalize(ex.poziomTrudnosciNum, 1, 3);
            }

            if (!profile.schorzenia.isEmpty() && schorzeniaScore < 0.1) {
                score *= 0.6; 
            }

            candidates.add(new ScoredExercise(ex, score));
        }

        Collections.sort(candidates, (a, b) -> Double.compare(b.score, a.score));

        // Różnorodność sprzętu
        Set<String> seenSprzet = new HashSet<>();
        for (ScoredExercise se : candidates) {
            String sprzet = se.exercise.potrzebnySprzet;
            if (sprzet != null && !sprzet.isEmpty() && !sprzet.equalsIgnoreCase("brak")) {
                if (seenSprzet.contains(sprzet)) se.score *= 0.7;
                seenSprzet.add(sprzet);
            }
        }

        Collections.sort(candidates, (a, b) -> Double.compare(b.score, a.score));

        List<Exercise> result = new ArrayList<>();
        Set<String> seenRuch = new HashSet<>();
        for (ScoredExercise se : candidates) {
            String ruchKey = se.exercise.getRuchKey();
            if (seenRuch.contains(ruchKey)) continue;
            result.add(se.exercise);
            seenRuch.add(ruchKey);
            if (result.size() >= limit) break;
        }

        return result;
    }

    private static double getGoalImpact(Exercise ex, String cel) {
        if (cel == null) return 1.0;
        switch (cel.toLowerCase()) {
            case "sila": case "siła": return ex.wplywNaSileNum;
            case "rownowaga": case "równowaga": return ex.wplywNaRownowageNum;
            case "mobilnosc": case "mobilność": return ex.wplywNaElastycznoscNum;
            case "kardio": return ex.wplywNaKardioNum;
            case "postura": return ex.wplywNaPostaweNum;
            case "mieszana": return (ex.wplywNaSileNum + ex.wplywNaRownowageNum + ex.wplywNaElastycznoscNum + ex.wplywNaKardioNum + ex.wplywNaPostaweNum) / 5.0;
            default: return 1.0;
        }
    }

    private static double computeSchorzeniaScore(Exercise ex, Set<String> userSchorzenia) {
        if (userSchorzenia == null || userSchorzenia.isEmpty() || ex.zalecaneSchorzenia == null) return 0;
        String zalecane = ex.zalecaneSchorzenia.toLowerCase();
        int matches = 0;
        for (String s : userSchorzenia) { if (zalecane.contains(s.toLowerCase())) matches++; }
        return matches == 0 ? 0 : 0.5 + (0.1 * matches);
    }

    private static boolean isContraindicated(Exercise ex, Set<String> userSchorzenia) {
        if (ex.przeciwwskazania == null || ex.przeciwwskazania.equalsIgnoreCase("brak")) return false;
        String contra = ex.przeciwwskazania.toLowerCase();
        for (String s : userSchorzenia) { if (contra.contains(s.toLowerCase())) return true; }
        return false;
    }

    private static double normalize(double val, double min, double max) {
        if (max <= min) return 0;
        return (val - min) / (max - min);
    }

    private static class ScoredExercise {
        Exercise exercise;
        double score;
        ScoredExercise(Exercise e, double s) { this.exercise = e; this.score = s; }
    }
}
