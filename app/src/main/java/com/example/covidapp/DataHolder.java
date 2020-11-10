package com.example.covidapp;

import java.util.ArrayList;

public class DataHolder {
    static ArrayList<String[]> scoreList;

    public static void setScoreList(ArrayList<String[]> data) {
        scoreList = data;
    }

    public static ArrayList<String[]> getScoreList() {
        return scoreList;
    }
}
