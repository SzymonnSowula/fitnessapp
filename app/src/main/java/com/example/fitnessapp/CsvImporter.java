package com.example.fitnessapp;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CsvImporter {
    private static final String TAG = "CsvImporter";
    public static final int CSV_VERSION = 2; // Zwiększ przy zmianach w parserze lub pliku CSV

    public static List<Exercise> loadExercisesFromCsv(Context context) {
        List<Exercise> exercises = new ArrayList<>();
        try {
            InputStream is = context.getAssets().open("cwiczenia_seniorzy.csv");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            
            // Skip header
            String headerLine = reader.readLine();
            if (headerLine == null) {
                Log.e(TAG, "Plik CSV jest pusty!");
                return exercises;
            }
            
            Log.d(TAG, "Nagłówki CSV: " + headerLine);
            
            String line;
            while ((line = reader.readLine()) != null) {
                // Parsowanie CSV z obsługą cudzysłowów (przecinki wewnątrz pól)
                String[] columns = parseCsvLine(line);
                if (columns.length < 25) {
                    Log.w(TAG, "Pominięto wiersz (zbyt mało kolumn: " + columns.length + "): " + line);
                    continue;
                }
                
                Exercise e = new Exercise();
                e.name = columns[0];
                
                // Mapowanie kategorii
                String rawCategory = columns[3].toLowerCase(Locale.ROOT);
                if (rawCategory.contains("sila") || rawCategory.contains("siła")) e.category = "sila";
                else if (rawCategory.contains("kardio")) e.category = "kardio";
                else if (rawCategory.contains("elastyczn") || rawCategory.contains("mobiln")) e.category = "mobilnosc";
                else if (rawCategory.contains("rownowaga") || rawCategory.contains("równowaga") || rawCategory.contains("balans")) e.category = "rownowaga";
                else if (rawCategory.contains("postura") || rawCategory.contains("plecy") || rawCategory.contains("postawe") || rawCategory.contains("postawę") || rawCategory.contains("core") || rawCategory.contains("kregoslup") || rawCategory.contains("kręgosłup")) e.category = "postura";
                else e.category = "mieszana";

                if (exercises.size() % 50 == 0) {
                    Log.d(TAG, "Mapowanie: " + columns[0] + " -> " + e.category);
                }

                // Pola tekstowe
                if (columns.length > 6) e.potrzebnySprzet = columns[6].trim();
                if (columns.length > 10) e.ruchGlowny = columns[10].trim();
                if (columns.length > 16) e.zalecaneSchorzenia = columns[16].trim();
                if (columns.length > 17) e.przeciwwskazania = columns[17].trim();
                if (columns.length > 26) e.opis = columns[26].trim();

                // Poziom trudności (kolumna 4)
                e.poziomTrudnosciNum = parseInfluence(columns[4]);
                
                // Binaria (kolumny 11-15)
                e.wspomaganeKrzeslemBin = parseBinary(columns[11]);
                e.moznaWLozkuBin = parseBinary(columns[12]);
                e.moznaSiedzacBin = parseBinary(columns[13]);
                e.wymagaStaniaBin = parseBinary(columns[14]);
                e.wymagaPodlogiBin = parseBinary(columns[15]);
                
                // Wpływy (kolumny 19-23)
                e.wplywNaRownowageNum = parseInfluence(columns[19]);
                e.wplywNaSileNum = parseInfluence(columns[20]);
                e.wplywNaElastycznoscNum = parseInfluence(columns[21]);
                e.wplywNaKardioNum = parseInfluence(columns[22]);
                e.wplywNaPostaweNum = parseInfluence(columns[23]);
                
                // Intensywność (kolumna 24)
                e.intensywnoscNum = parseInfluence(columns[24]);
                
                // Źródło (kolumna 2)
                e.zrodloEnc = 0.0f;

                exercises.add(e);
            }
            reader.close();
        } catch (Exception e) {
            Log.e(TAG, "Error parsing CSV", e);
        }
        return exercises;
    }

    private static String[] parseCsvLine(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString().trim());
        return tokens.toArray(new String[0]);
    }

    private static float parseInfluence(String val) {
        if (val == null) return 1.0f;
        val = val.toLowerCase(Locale.ROOT);
        if (val.contains("wysoki") || val.contains("wysoka") || val.contains("trudny")) return 3.0f;
        if (val.contains("średni") || val.contains("średnia") || val.contains("sredni")) return 2.0f;
        if (val.contains("niski") || val.contains("niska") || val.contains("łatwy") || val.contains("latwy")) return 1.0f;
        if (val.contains("bardzo niska")) return 1.0f;
        return 1.0f;
    }

    private static float parseBinary(String val) {
        if (val == null) return 0.0f;
        val = val.toLowerCase(Locale.ROOT);
        if (val.equals("tak") || val.equals("tak (opcjonalnie)") || val.equals("opcjonalnie")) return 1.0f;
        return 0.0f;
    }
}
