package com.example.fitnessapp;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Locale;

@Entity(tableName = "exercises")
public class Exercise {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public float wplywNaSileNum;
    public float wplywNaRownowageNum;
    public float wplywNaElastycznoscNum;
    public float wplywNaKardioNum;
    public float wplywNaPostaweNum;
    public float intensywnoscNum;
    public float poziomTrudnosciNum;
    public float wspomaganeKrzeslemBin;
    public float moznaWLozkuBin;
    public float moznaSiedzacBin;
    public float wymagaStaniaBin;
    public float wymagaPodlogiBin;
    public float zrodloEnc;
    public String category;

    // NOWE POLA POTRZEBNE DO FILTROWANIA I WYŚWIETLANIA:
    public String opis;
    public String przeciwwskazania;
    public String potrzebnySprzet;
    public String ruchGlowny;
    public String zalecaneSchorzenia;

    public String getRuchKey() {
        if (ruchGlowny == null || ruchGlowny.isEmpty()) return String.valueOf(id);
        String[] words = ruchGlowny.toLowerCase(Locale.ROOT).replaceAll("[^a-ząćęłńóśźż\\s]", "").split("\\s+");
        if (words.length >= 2) return words[0] + " " + words[1];
        if (words.length == 1) return words[0];
        return String.valueOf(id);
    }

    public float[] toFeatureArray() {
        return new float[]{
                wplywNaSileNum,
                wplywNaElastycznoscNum,
                wplywNaKardioNum,
                wplywNaPostaweNum,
                intensywnoscNum,
                poziomTrudnosciNum,
                wspomaganeKrzeslemBin,
                moznaWLozkuBin,
                moznaSiedzacBin,
                wymagaStaniaBin,
                wymagaPodlogiBin,
                zrodloEnc
        };
    }
}