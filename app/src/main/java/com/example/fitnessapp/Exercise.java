package com.example.fitnessapp;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "exercises")
public class Exercise {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public float wplywNaSileNum;
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