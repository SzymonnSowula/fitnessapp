package com.example.fitnessapp;

public class Exercise {
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
