package com.example.covidapp;

import java.util.ArrayList;

public class DataHolder {
    static ArrayList<String[]> scoreList;
    static boolean isDividedListReady = false;
    static ArrayList<ArrayList<String[]>> listDividedByCountries = new ArrayList<ArrayList<String[]>>();
    static boolean isCountryNameListReady = false;
    static ArrayList<String> countryNameList = new ArrayList<String>();
    static boolean isChosenCountryListReady = false;
    static ArrayList<String[]> chosenCountryList = new ArrayList<String[]>();
    static String currentlyChosenCountryName = "";

    public static void setScoreList(ArrayList<String[]> data) { scoreList = data; }
    public static ArrayList<String[]> getScoreList() { return scoreList; }

    // Przy zwracaniu ponizszych list, zawsze najpierw sprawdzam, czy sa one w ogole
    // przygotowane do odczytu, jesli nie - wywoluje odpowiednia funkcje przygotowujaca
    public static ArrayList<ArrayList<String[]>> getListDividedByCountries() {
        if(isDividedListReady) {
            return listDividedByCountries;
        }
        else {
            divideListIntoCountries();
            return listDividedByCountries;
        }
    }

    public static ArrayList<String> getCountryNameList() {
        if(isCountryNameListReady) {
            return countryNameList;
        }
        else {
            updateCountryNames();
            return countryNameList;
        }
    }


    // Tutaj sprawdzam tez, czy wybrany kraj sie zmienil. Jesli tak, to aktualizuje liste.
    public static ArrayList<String[]> getChosenCountryList(String countryName) {
        if(isChosenCountryListReady) {
            if(countryName.equals(currentlyChosenCountryName)) {
                return chosenCountryList;
            }
            else {
                updateChosenCountryList(countryName);
                return chosenCountryList;
            }
        }
        else {
            updateChosenCountryList(countryName);
            return chosenCountryList;
        }
    }

    // Ponizej funkcje przygotowujace dla list

    // Dzieli cala liste na poszczegolne kraje: z postaci lista[nrRekordu][nrDanej]
    // powstaje lista[nrKraju][nrRekordu][nrDanej]
    public static void divideListIntoCountries() {
        ArrayList<ArrayList<String[]>> dividedList = new ArrayList<ArrayList<String[]>>();
        dividedList.add(new ArrayList<String[]>());
        dividedList.get(0).add(scoreList.get(1));
        int countryNr = 0;
        for(int recordNr = 2; recordNr < scoreList.size(); recordNr++) {
            String[] record = scoreList.get(recordNr);
            String recordCountry = record[2];
            if(!(recordCountry.equals("International"))) {
                if (recordCountry.equals(dividedList.get(countryNr).get(0)[2])) {
                    dividedList.get(countryNr).add(record);
                } else {
                    dividedList.add(new ArrayList<String[]>());
                    countryNr++;
                    dividedList.get(countryNr).add(record);
                }
            }
        }
        listDividedByCountries = dividedList;
        isDividedListReady = true;
    }

    public static void updateCountryNames() {
        if(isDividedListReady) {
            for(int countryNr = 0; countryNr < listDividedByCountries.size(); countryNr++) {
                String countryName = listDividedByCountries.get(countryNr).get(0)[2];
                if(countryName.equals("World")) {
                    if(!(countryNameList.get(0).equals("World"))) {
                        countryNameList.add(0, countryName);
                    }
                }
                else {
                    countryNameList.add(listDividedByCountries.get(countryNr).get(0)[2]);
                }
            }
            isCountryNameListReady = true;
        }
        else {
            divideListIntoCountries();
            updateCountryNames();
        }
    }

    // Jesli podana nazwa kraju znajduje sie w listDividedByCountry,
    // do chosenCountryList zostana wpisane wszystkie rekordy z tego kraju
    public static void updateChosenCountryList(String newChosenCountryName) {
        if(isDividedListReady) {
            for(int countryNr = 0; countryNr < listDividedByCountries.size(); countryNr++) {
                String countryName = listDividedByCountries.get(countryNr).get(0)[2];
                if(countryName.equals(newChosenCountryName)) {
                    chosenCountryList = listDividedByCountries.get(countryNr);
                    removeFloatingPointsFromList();
                    break;
                }
            }
            currentlyChosenCountryName = newChosenCountryName;
            isChosenCountryListReady = true;
        }
        else {
            divideListIntoCountries();
            updateChosenCountryList(newChosenCountryName);
        }
    }

    public static void removeFloatingPointsFromList() {
        for(int recordNr = 0; recordNr < chosenCountryList.size(); recordNr++) {
            String[] record = chosenCountryList.get(recordNr);
            chosenCountryList.get(recordNr)[4] = reformatString(record[4]);
            chosenCountryList.get(recordNr)[5] = reformatString(record[5]);
            chosenCountryList.get(recordNr)[7] = reformatString(record[7]);
            chosenCountryList.get(recordNr)[8] = reformatString(record[8]);
        }
    }

    public static String reformatString(String text) {
        if(text.indexOf(".") >= 0) {
            text = text.substring(0, text.indexOf("."));
        }
        if(text.length() > 3) {
            int number = Integer.parseInt(text);
            text = String.format("%,d", number);
        }
        return text;
    }
}
