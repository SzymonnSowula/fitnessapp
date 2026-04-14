package com.example.fitnessapp;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CsvImporter {
    private static final String TAG = "CsvImporter";

    public static List<Exercise> loadExercisesFromCsv(Context context) {
        List<Exercise> exercises = new ArrayList<>();
        try {
            InputStream is = context.getAssets().open("cwiczenia_seniorzy.csv");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            
            // Skip header
            String headerLine = reader.readLine();
            if (headerLine == null) return exercises;
            
            String[] headers = headerLine.split(",");
            
            while ((line = reader.readLine()) != null) {
                // Prosty parser CSV (nie obsługuje przecinków wewnątrz cudzysłowów, ale w tym pliku nie widać ich w kluczowych polach)
                String[] columns = line.split(",");
                if (columns.length < 25) continue;
                
                Exercise e = new Exercise();
                e.name = columns[0]; // nazwa_cwiczenia
                
                // Mapowanie kategorii (kolumna 3 - kategoria)
                // W pliku są kategorie typu "równowaga_chód", "siła", "elastyczność"
                // Model ONNX spodziewa się: "kardio", "mieszana", "mobilnosc", "postura", "rownowaga", "sila"
                String rawCategory = columns[3].toLowerCase();
                if (rawCategory.contains("sila") || rawCategory.contains("siła")) e.category = "sila";
                else if (rawCategory.contains("kardio")) e.category = "kardio";
                else if (rawCategory.contains("elastyczn") || rawCategory.contains("mobiln")) e.category = "mobilnosc";
                else if (rawCategory.contains("rownowaga") || rawCategory.contains("równowaga")) e.category = "rownowaga";
                else if (rawCategory.contains("postura") || rawCategory.contains("plecy") || rawCategory.contains("postawe") || rawCategory.contains("postawę") || rawCategory.contains("core")) e.category = "postura";
                else e.category = "mieszana";

                // Poziom trudności (kolumna 4)
                e.poziomTrudnosciNum = parseInfluence(columns[4]);
                
                // Binaria (kolumny 11-15)
                e.wspomaganeKrzeslemBin = parseBinary(columns[11]);
                e.moznaWLozkuBin = parseBinary(columns[12]);
                e.moznaSiedzacBin = parseBinary(columns[13]);
                e.wymagaStaniaBin = parseBinary(columns[14]);
                e.wymagaPodlogiBin = parseBinary(columns[15]);
                
                // Wpływy (kolumny 19-24)
                // W pliku: rownowaga, sile, elastycznosc, kardio, postawe
                // W Exercise: sile, elastycznosc, kardio, postawe
                e.wplywNaSileNum = parseInfluence(columns[20]);
                e.wplywNaElastycznoscNum = parseInfluence(columns[21]);
                e.wplywNaKardioNum = parseInfluence(columns[22]);
                e.wplywNaPostaweNum = parseInfluence(columns[23]);
                
                // Intensywność (kolumna 24)
                e.intensywnoscNum = parseInfluence(columns[24]);
                
                // Źródło (kolumna 2 - zrodlo) - zaszyfrujmy jako 0 dla uproszczenia
                e.zrodloEnc = 0.0f;

                exercises.add(e);
            }
            reader.close();
        } catch (Exception e) {
            Log.e(TAG, "Error parsing CSV", e);
        }
        return exercises;
    }

    private static float parseInfluence(String val) {
        val = val.toLowerCase();
        if (val.contains("wysoki") || val.contains("wysoka") || val.contains("trudny")) return 1.0f;
        if (val.contains("średni") || val.contains("średnia")) return 0.5f;
        if (val.contains("niski") || val.contains("niska") || val.contains("łatwy")) return 0.2f;
        if (val.contains("bardzo niska")) return 0.1f;
        return 0.0f;
    }

    private static float parseBinary(String val) {
        val = val.toLowerCase();
        if (val.equals("tak") || val.equals("tak (opcjonalnie)") || val.equals("opcjonalnie")) return 1.0f;
        return 0.0f;
    }
}