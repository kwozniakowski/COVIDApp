package com.example.covidapp;

import java.util.ArrayList;


// Glownym zadaniem tej klasy jest przechowywanie danych z pliku csv, ale takze
// podzial danych w tym pliku na rozne listy, np nazwy krajow, daty dostepne w liscie
// danego kraju itp. Nazwy list, mam nadzieje, dosc dobrze tlumacza, co przechowuje kazda z nich
public class DataHolder {
    private static ArrayList<String[]> scoreList;

    private static boolean isDividedListReady = false;
    private static ArrayList<ArrayList<String[]>> listDividedByCountries = new ArrayList<ArrayList<String[]>>();

    private static boolean isCountryNameListReady = false;
    private static ArrayList<String> countryNameList = new ArrayList<String>();

    private static boolean isChosenCountryListReady = false;
    private static ArrayList<String[]> chosenCountryList = new ArrayList<String[]>();

    private static boolean isChosenCountryNameReady = false;
    private static String chosenCountryName = "";

    private static boolean isChosenDateReady = false;
    private static String chosenDate = "";

    // Obecny rekord to ten wybrany na podstawie kraju i daty
    private static boolean isChosenRecordReady = false;
    private static String[] chosenRecord;

    private static String defaultCountryName;



    // GETTERY I SETTERY

    // !!! WAZNE !!!
    // Generalnie nalezy korzystac tylko z ponizszych getterow (jest tez jeden setter -
    // informacje o nim znajduja sie w komentarzu nad nim), nie ma potrzeby korzystania z
    // funkcji typu "update...", poniewaz gettery w razie potrzeby same aktualizuja dane
    // Wyjatkami sa
    // updateChosenDate(String newDate) i updateChosenCountryName(String newChosenCountryName),
    // nalezy z nich korzystac jesli wybrana data lub kraj ulebly zmianie


    // Jedyny setter tutaj, korzysta sie z niego tylko raz MainActivity, po pobraniu danych
    // z pliku csv. Generalnie w zadnym innym miejscu nie ma potrzeby korzystania z niej
    public static void setScoreList(ArrayList<String[]> data) { scoreList = data; }

    public static ArrayList<String[]> getScoreList() { return scoreList; }

    public static ArrayList<ArrayList<String[]>> getListDividedByCountries() {
        System.out.println("Wywolano metode getListDividedByCountries");
        if(isDividedListReady) {
            return listDividedByCountries;
        }
        else {
            divideListIntoCountries();
            return listDividedByCountries;
        }
    }

    public static ArrayList<String> getCountryNameList() {
        System.out.println("Wywolano metode getCountryNameList");
        if(isCountryNameListReady) {
            return countryNameList;
        }
        else {
            updateCountryNames();
            return countryNameList;
        }
    }

    public static ArrayList<String[]> getChosenCountryList() {
        System.out.println("Wywolano metode getChosenCountryList");
        if(isChosenCountryListReady) {
            return chosenCountryList;
        }
        else {
            updateChosenCountryList();
            return chosenCountryList;
        }
    }

    public static String getChosenCountryName() {
        System.out.println("Wywolano metode getChosenCountryName");
        if(isChosenCountryNameReady) {
            return chosenCountryName;
        }
        else {
            updateChosenCountryName();
            return chosenCountryName;
        }
    }

    public static String getChosenDate() {
        System.out.println("Wywolano metode getChosenDate");
        if(isChosenDateReady) {
            // Robie update na wypadek, gdyby zmienil sie kraj i dane z obecnie wybranej
            // daty dla tego kraju by nie istnialy (jest to sprawdzane podczas update'u)
            updateChosenDate(chosenDate);
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
        System.out.println("Wywolano metode getChosenRecord");
        updateChosenRecord();
        if(isChosenRecordReady) {
            return chosenRecord;
        }
        else return null;
    }

    public static String getDefaultCountryName() {
        return defaultCountryName;
    }

    // Koniec getterow i setterow




    // Ponizej funkcje przygotowujace dla list

    public static void updateChosenCountryName(String newChosenCountryName) {
        System.out.println("Wywolano metode updateChosenCountryName1");
        getCountryNameList();

        isChosenCountryNameReady = false;
        //if(isCountryNameListReady) {
        for(String countryName:countryNameList) {
            if(countryName.equals(newChosenCountryName)) {
                // Sprawdzam, czy potrzebne bedzie wywolanie funkcji updateChosenRecord()
                boolean isRecordUpdateRequired = (chosenCountryName != newChosenCountryName);
                chosenCountryName = newChosenCountryName;
                isChosenCountryNameReady = true;
                updateChosenCountryList();
                if(isRecordUpdateRequired) {
                    updateChosenRecord();
                }
                break;
            }
        }
        if(!isChosenCountryNameReady) {
            updateChosenCountryName();
        }
        /*}
        else {
            updateCountryNames();
            updateChosenCountryName(newChosenCountryName);
        }*/
    }

    // newDate powinna miec format "yyyy-MM-dd"
    public static void updateChosenDate(String newDate) {
        System.out.println("Wywolano metode updateChosenDate1");
        getChosenCountryList();

        isChosenDateReady = false;
        //if(isChosenCountryListReady) {
        for(String[] currentRecord:chosenCountryList) {
            if(currentRecord[3].equals(newDate)) {
                // Sprawdzam, czy potrzebne bedzie wywolanie funkcji updateChosenRecord()
                boolean isRecordUpdateRequired = (chosenDate != newDate);
                chosenDate = newDate;
                isChosenDateReady = true;
                if(isRecordUpdateRequired) {
                    updateChosenRecord();
                }
                break;
            }
        }
        if(!isChosenDateReady) {
            updateChosenDate();
        }
        /*}
        else {
            updateChosenCountryList();
        }*/
    }

    // Dzieli cala liste na poszczegolne kraje: z postaci lista[nrRekordu][nrDanej]
    // powstaje lista[nrKraju][nrRekordu][nrDanej]
    public static void divideListIntoCountries() {
        try {
            // Upewniam sie ze scoreList nie jest puste, w przeciwnym wypadku
            // ponizsza funkcja rzuci wyjatek
            checkScoreList();

            ArrayList<ArrayList<String[]>> dividedList = new ArrayList<ArrayList<String[]>>();
            dividedList.add(new ArrayList<String[]>());
            dividedList.get(0).add(scoreList.get(1));
            int countryNr = 0;

            for(String[] record:scoreList) {
                String recordCountry = record[2];
                if(!(recordCountry.equals("International") || recordCountry.equals("location"))) {
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
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateCountryNames() {
        System.out.println("Wywolano metode updateCountryNames");
        getListDividedByCountries();

        //if(isDividedListReady) {
        countryNameList.clear();
        for(ArrayList<String[]> country:listDividedByCountries) {
            String countryName = country.get(0)[2];
            if(countryName.equals("World")) {
                if(!(countryNameList.get(0).equals("World"))) {
                    countryNameList.add(0, countryName);
                }
            }
            else {
                countryNameList.add(country.get(0)[2]);
            }
        }
        isCountryNameListReady = true;
        /*}
        else {
            divideListIntoCountries();
            updateCountryNames();
        }*/
    }

    // Jesli podana nazwa kraju znajduje sie w listDividedByCountry,
    // do chosenCountryList zostana wpisane wszystkie rekordy z tego kraju
    public static void updateChosenCountryList() {
        System.out.println("Wywolano metode updateChosenCountryList");
        getListDividedByCountries();
        getChosenCountryName();

        /*if(isDividedListReady) {
            if(isChosenCountryNameReady) {*/
        for (ArrayList<String[]> countryList : listDividedByCountries) {
            String countryName = countryList.get(0)[2];
            if (countryName.equals(chosenCountryName)) {
                chosenCountryList = (ArrayList<String[]>) countryList.clone();
                isChosenCountryListReady = true;
                removeFloatingPointsFromList();
                updateChosenDate(chosenDate);
                break;
            }
        }
            /*}
            // Tu nie wywoluje updateChosenCountryList, poniewaz funkcja updateChosenCountryName
            // sama to zrobi, o ile uda jej sie odswiezyc chosenCountryName
            else {
                updateChosenCountryName();
            }
        }
        else {
            divideListIntoCountries();
            updateChosenCountryList();
        }*/
    }

    private static void removeFloatingPointsFromList() {
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
    private static void updateChosenDate() {
        System.out.println("Wywolano metode updateChosenDate2");
        if(isChosenCountryListReady) {
            chosenDate = chosenCountryList.get(chosenCountryList.size() - 1)[3];
            isChosenDateReady = true;
            updateChosenRecord();
        }
    }

    private static void updateChosenCountryName() {
        System.out.println("Wywolano metode updateChosenCountryName2");
        getCountryNameList();

        // Na wypadek gdyby podano bledna nazwe kraju (nie ma jej w liscie)
        //if(isCountryNameListReady) {
            chosenCountryName = countryNameList.get(0);
            isChosenCountryNameReady = true;
            updateChosenCountryList();
        /*}
        else {
            updateCountryNames();
            updateChosenCountryName();
        }*/
    }

    private static void updateChosenRecord() {
        System.out.println("Wywolano metode updateChosenRecord");
        getChosenCountryList();
        getChosenDate();

        /*if(isChosenCountryListReady) {
            if(isChosenDateReady) {*/
                for(String[] record:chosenCountryList) {
                    String currentRecordDate = record[3];
                    if(currentRecordDate.equals(chosenDate)) {
                        chosenRecord = record;
                        isChosenRecordReady = true;
                        break;
                    }
                }
            /*}
            // Nie wywoluje tu updateChosenRecord, bo funkcja updateChosenDate() sama to zrobi,
            // o ile uda jej sie odswiezyc date
            else {
                updateChosenDate();
            }
        }
        else {
            updateChosenCountryList();
        }*/
    }

    private static void checkScoreList() throws Exception {
        if(scoreList.isEmpty()) {
            throw new Exception("scoreList is empty!");
        }
    }

    // Ponizsza funkcja sprawdza czy dla wybranego kraju sa dane z podanej daty
    // Przyklad:
    // Mamy dane od 1.03.2020 do 31.03.2020
    // Jesli funkcja otrzyma 5.03.2020, zwroci 5.03.2020, bo to poprawna data
    // Jesli otrzyma 1.02.2020, zwroci 1.03.2020, bo to pierwsza mozliwa data
    // Jesli otrzyma 1.05.2020, zwroci 31.03.2020, bo to ostatnia mozliwa data
    public static String isDateInChosenCountry(String date) {

        // To tylko na wypadek gdyby chosenCountryList bylo puste, chociaz
        // nigdy nie powinno to miec miejsca
        getChosenCountryList();

        String chosenCountryMinDate = chosenCountryList.get(0)[3];
        String chosenCountryMaxDate = chosenCountryList.get(chosenCountryList.size() - 1)[3];

        if(date.compareTo(chosenCountryMinDate) < 0) {
            return chosenCountryMinDate;
        }
        else if(date.compareTo(chosenCountryMaxDate) > 0) {
            return chosenCountryMaxDate;
        }
        else {
            return date;
        }
    }

    public static void updateData() {
        isDividedListReady = false;
        isCountryNameListReady = false;
        updateChosenRecord();
    }

    public static void updateDefaultCountryName(String countryName) {
        defaultCountryName = countryName;
    }

    // Sprawdza, czy dla danego kraju sa dostepne dane odnosnie szczepien
    // i jesli tak, ustawia chosenDate na date z najnowszymi danymi
    public static String getLatestDateForParameter(int index) {
        getChosenCountryList();

        int lastRecordIndexNr = chosenCountryList.size()-1;
        // Szukam od konca
        for(int recordNr = lastRecordIndexNr; recordNr >= 0; recordNr--) {
            String[] record = chosenCountryList.get(recordNr);
            String parameter = record[index];
            String date = record[3];
            if(!parameter.isEmpty()) {
                // Jesli chcemy sprawdzic infekcje, to sprawa jest troche trudniejsza, bo
                // wczesniej (reformatString()) wpisuje 0 tam gdzie nie ma danych
                if(index == 4 && recordNr == lastRecordIndexNr) {
                    String[] previousRecord = chosenCountryList.get(recordNr-1);
                    String previousInfections = previousRecord[index];
                    String previousDate = previousRecord[3];
                    if(previousInfections.compareTo(parameter) > 0) {
                        return previousDate;
                    } else {
                        return date;
                    }
                }
                return date;
            }
        }
        return "";
    }

    public static String getLatestVaccinationDate() {
        return getLatestDateForParameter(34);
    }

    public static String getLatestInfectionDate() {
        return getLatestDateForParameter(4);
    }

    public static String getLatestPopulationDate() {
        return getLatestDateForParameter(39);
    }

    public static void setLatestInfectionDate() {
        chosenDate = getLatestInfectionDate();
    }
}
