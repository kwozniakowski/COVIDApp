package com.example.covidapp;

import java.util.ArrayList;


// Glownym zadaniem tej klasy jest przechowywanie danych z pliku csv, ale takze
// podzial danych w tym pliku na rozne listy, np nazwy krajow, daty dostepne w liscie
// danego kraju itp. Nazwy list, mam nadzieje, dosc dobrze tlumacza, co przechowuje kazda z nich
public class DataHolder {
    static ArrayList<String[]> scoreList;

    static boolean isDividedListReady = false;
    static ArrayList<ArrayList<String[]>> listDividedByCountries = new ArrayList<ArrayList<String[]>>();

    static boolean isCountryNameListReady = false;
    static ArrayList<String> countryNameList = new ArrayList<String>();

    static boolean isChosenCountryListReady = false;
    static ArrayList<String[]> chosenCountryList = new ArrayList<String[]>();
    static String chosenCountryName = "";

    static boolean isChosenDateReady = false;
    static String chosenDate = "";

    // Obecny rekord to ten wybrany na podstawie kraju i daty
    static boolean isChosenRecordReady = false;
    static String[] chosenRecord;



    // GETTERY I SETTERY

    // !!! WAZNE !!!
    // Generalnie nalezy korzystac tylko z ponizszych getterow (jest tez jeden setter -
    // informacje o nim znajduja sie w komentarzu nad nim), nie ma potrzeby korzystania z
    // funkcji typu "update...", poniewaz gettery w razie potrzeby same aktualizuja dane
    // Wyjatkiem jest updateChosenDate(String newDate), nalezy z niego korzystac jesli
    // wybrana data zostala zmieniona

    // Jedyny setter tutaj, korzysta z niej tylko raz MainActivity, po pobraniu danych
    // z pliku csv. Generalnie w zadnym innym miejscu nie ma potrzeby korzystania z niej
    public static void setScoreList(ArrayList<String[]> data) { scoreList = data; }

    public static ArrayList<String[]> getScoreList() { return scoreList; }

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
    public static ArrayList<String[]> getChosenCountryList(String newCountryName) {
        if(isChosenCountryListReady) {
            if(newCountryName.equals(chosenCountryName)) {
                return chosenCountryList;
            }
            else {
                updateChosenCountryList(newCountryName);
                return chosenCountryList;
            }
        }
        else {
            updateChosenCountryList(newCountryName);
            return chosenCountryList;
        }
    }

    public static String getChosenDate() {
        if(isChosenDateReady) {
            return chosenDate;
        }
        else {
            updateChosenDate();
            if(isChosenDateReady) {
                return chosenDate;
            }
            // To nigdy nie powinno sie wykonac, ale na wszelki wypadek jest
            else return null;
        }
    }

    public static String[] getChosenRecord() {
        updateChosenRecord();
        if(isChosenRecordReady) {
            return chosenRecord;
        }
        else return null;
    }

    // Koniec getterow i setterow


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
    public static void updateChosenCountryList(String newCountryName) {
        if(isDividedListReady) {
            isChosenCountryListReady = false;
            for(int countryNr = 0; countryNr < listDividedByCountries.size(); countryNr++) {
                String countryName = listDividedByCountries.get(countryNr).get(0)[2];
                if(countryName.equals(newCountryName)) {
                    chosenCountryList.clear();
                    chosenCountryList = (ArrayList<String[]>) listDividedByCountries.get(countryNr).clone();
                    removeFloatingPointsFromList();
                    updateChosenDate(chosenDate);
                    chosenCountryName = newCountryName;
                    isChosenCountryListReady = true;
                    break;
                }
            }
            // Na wypadek gdyby podano bledna nazwe kraju (nie ma jej w liscie)
            if(!isChosenCountryListReady) {
                updateChosenCountryList("World");
            }
        }
        else {
            divideListIntoCountries();
            updateChosenCountryList(newCountryName);
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
        if(text.isEmpty()) {
            text = "0";
        }
        /*if(text.length() > 3 && text.indexOf(",") < 0) {
            int number = Integer.parseInt(text);
            text = String.format("%,d", number);
        }*/
        return text;
    }

    // W tym przypadku zostanie przypisana najnowsza data z wybranego kraju,
    // o ile kraj zostal wybrany
    public static void updateChosenDate() {
        if(isChosenCountryListReady) {
            chosenDate = chosenCountryList.get(chosenCountryList.size() - 1)[3];
            isChosenDateReady = true;
            chosenRecord = chosenCountryList.get(chosenCountryList.size() - 1);
            isChosenRecordReady = true;
        }
    }

    // newDate powinna miec format "yyyy-MM-dd"
    public static void updateChosenDate(String newDate) {
        if(isChosenCountryListReady) {
            isChosenDateReady = false;
            for(int recordNr = 0; recordNr < chosenCountryList.size(); recordNr++) {
                String[] currentRecord = chosenCountryList.get(recordNr);
                if(currentRecord[3].equals(newDate)) {
                    chosenDate = newDate;
                    isChosenDateReady = true;
                    updateChosenRecord();
                    break;
                }
            }
            if(!isChosenDateReady) {
                updateChosenDate();
            }
        }
    }

    public static void updateChosenRecord() {
        isChosenRecordReady = false;
        if(isChosenCountryListReady) {
            if(isChosenDateReady) {
                for(int recordNr = 0; recordNr < chosenCountryList.size(); recordNr++) {
                    String currentRecordDate = chosenCountryList.get(recordNr)[3];
                    if(currentRecordDate.equals(chosenDate)) {
                        chosenRecord = chosenCountryList.get(recordNr);
                        isChosenRecordReady = true;
                        break;
                    }
                }
            }
            else {
                updateChosenDate();
                updateChosenRecord();
            }
        }
    }
}
