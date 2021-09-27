package com.example.covidapp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


// Glownym zadaniem tej klasy jest przechowywanie danych z pliku csv, ale takze
// podzial danych w tym pliku na rozne listy, np nazwy krajow, daty dostepne w liscie
// danego kraju itp. Nazwy list, mam nadzieje, dosc dobrze tlumacza, co przechowuje kazda z nich
public class DataHolder {
    public static boolean isScoreListReady = false;
    private static ArrayList<String[]> scoreList;

    private static String[] indexList;

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

    private static String defaultCountryName = "";

    // Zmienne pomocne przy odswiezaniu fragmentow
    public static Object updateLock = new Object();
    public static boolean isFragmentUpdateRequired = false;

    public static int LOCATION, DATE, TOTAL_CASES, NEW_CASES, TOTAL_DEATHS, NEW_DEATHS, NEW_CASES_SMOOTHED, NEW_DEATHS_SMOOTHED;
    public static int TOTAL_CASES_PER_MILLION, TOTAL_DEATHS_PER_MILLION, TOTAL_TESTS, NEW_TESTS;
    public static int TOTAL_VACCINATIONS, NEW_VACCINATIONS, POPULATION, PEOPLE_FULLY_VACCINATED;



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
    public static void setScoreList(ArrayList<String[]> data) {
        scoreList = data;
        indexList = scoreList.get(0);
        setUpIndexes();
        isScoreListReady = true;
        if(isChosenCountryNameReady) {
            updateChosenCountryName(chosenCountryName);
        }
    }

    public static ArrayList<String[]> getScoreList() { return scoreList; }

    public static ArrayList<ArrayList<String[]>> getListDividedByCountries() {
        // System.out.println("Wywolano metode getListDividedByCountries");
        if(isDividedListReady) {
            return listDividedByCountries;
        }
        else {
            divideListIntoCountries();
            return listDividedByCountries;
        }
    }

    public static int getIndex(String chosenParameter) {
        for(int index = 0; index < indexList.length; index++) {
            String parameter = indexList[index];
            if(chosenParameter.equals(parameter)) {
                return index;
            }
        }
        return -1;
    }

    public static ArrayList<String> getCountryNameList() {
        // System.out.println("Wywolano metode getCountryNameList");
        if(isCountryNameListReady) {
            return countryNameList;
        }
        else {
            updateCountryNames();
            return countryNameList;
        }
    }

    public static ArrayList<String[]> getChosenCountryList() {
        // System.out.println("Wywolano metode getChosenCountryList");
        if(isChosenCountryListReady) {
            return chosenCountryList;
        }
        else {
            updateChosenCountryList();
            return chosenCountryList;
        }
    }

    public static String getChosenCountryName() {
        // System.out.println("Wywolano metode getChosenCountryName");
        if(isChosenCountryNameReady) {
            return chosenCountryName;
        }
        else {
            updateChosenCountryName();
            return chosenCountryName;
        }
    }

    public static String getChosenDate() {
        // System.out.println("Wywolano metode getChosenDate");
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
        // System.out.println("Wywolano metode getChosenRecord");
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
        // System.out.println("Wywolano metode updateChosenCountryName1");

        if(listDividedByCountries != null) {
            getCountryNameList();

            isChosenCountryNameReady = false;
            //if(isCountryNameListReady) {
            for (String countryName : countryNameList) {
                if (countryName.equals(newChosenCountryName)) {
                    // Sprawdzam, czy potrzebne bedzie wywolanie funkcji updateChosenRecord()
                    boolean isRecordUpdateRequired = (chosenCountryName != newChosenCountryName);
                    chosenCountryName = newChosenCountryName;
                    isChosenCountryNameReady = true;
                    updateChosenCountryList();
                    if (isRecordUpdateRequired) {
                        updateChosenRecord();
                    }
                    break;
                }
            }
            if (!isChosenCountryNameReady) {
                updateChosenCountryName();
            }
        }
        // Jesli plik csv nie jest dostepny (pobrany), to i tak ustawie wybrana nazwe, a potem
        // metoda setScoreList wywola te metode jeszcze raz, zeby sprawdzic czy nazwa jest poprawna
        else {
            chosenCountryName = newChosenCountryName;
            isChosenCountryNameReady = true;
        }
        /*}
        else {
            updateCountryNames();
            updateChosenCountryName(newChosenCountryName);
        }*/
    }

    // newDate powinna miec format "yyyy-MM-dd"
    public static void updateChosenDate(String newDate) {
        // System.out.println("Wywolano metode updateChosenDate1");
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
            scoreList = null;
            isScoreListReady = false;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateCountryNames() {
        // System.out.println("Wywolano metode updateCountryNames");
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
        // System.out.println("Wywolano metode updateChosenCountryList");
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
                fixInfectionAndDeathZeros();
                updateChosenDate(chosenDate);
                break;
            }
        }

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

        return text;
    }

    // Jesli np w najnowszym rekordzie jest 0, zamienie to na najwieksza ostatnia liczbe
    private static void fixInfectionAndDeathZeros() {
        fixZeros(4);
        fixZeros(7);
    }

    private static void fixZeros(int index) {
        for(int recordNr = 1; recordNr < chosenCountryList.size(); recordNr++) {
            String[] record = chosenCountryList.get(recordNr);
            if(record[index].equals("0")) {
                for(int previousNr = recordNr-1; previousNr >=0; previousNr--) {
                    String[] previousRecord = chosenCountryList.get(previousNr);
                    int current = Integer.parseInt(record[index]);
                    int previous = Integer.parseInt(previousRecord[index]);
                    if(previous > current) {
                        chosenCountryList.get(recordNr)[index] = previousRecord[index];
                        break;
                    }
                }
            }
        }
    }

    // W tym przypadku zostanie przypisana najnowsza data z wybranego kraju,
    // o ile kraj zostal wybrany
    private static void updateChosenDate() {

        if(isChosenCountryListReady) {
            chosenDate = chosenCountryList.get(chosenCountryList.size() - 1)[3];
            isChosenDateReady = true;
            updateChosenRecord();
        }
    }

    private static void updateChosenCountryName() {

        getCountryNameList();

        // Na wypadek gdyby podano bledna nazwe kraju (nie ma jej w liscie)

            chosenCountryName = defaultCountryName;//countryNameList.get(0);
            isChosenCountryNameReady = true;
            updateChosenCountryList();

    }

    private static void updateChosenRecord() {
        // System.out.println("Wywolano metode updateChosenRecord");
        getChosenCountryList();
        getChosenDate();


                for(String[] record:chosenCountryList) {
                    String currentRecordDate = record[3];
                    if(currentRecordDate.equals(chosenDate)) {
                        chosenRecord = record;
                        isChosenRecordReady = true;
                        break;
                    }
                }
    }

    private static void checkScoreList() throws Exception {
        if(!isScoreListReady) {
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
        isChosenCountryListReady = false;
        isChosenDateReady = false;
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
                return date;
            }
        }
        return "";
    }

    public static String getLatestVaccinationDate() {
        return getLatestDateForParameter(TOTAL_VACCINATIONS);
    }

    public static String getLatestInfectionDate() {
        return getLatestDateForParameter(TOTAL_CASES);
    }

    public static String getLatestPopulationDate() {
        return getLatestDateForParameter(POPULATION);
    }

    public static String[] getRecordForDate(String date) {
        getChosenCountryList();
        getChosenDate();

        String[] selectedRecord = null;
        for(String[] record:chosenCountryList) {
            String currentRecordDate = record[3];
            if(currentRecordDate.equals(date)) {
                selectedRecord = record;
                break;
            }
        }
        return selectedRecord;
    }

    public static int getWeeklyInfections() {
        getChosenCountryList();
        getChosenDate();

        int sum = 0;
        int index = 0;
        for(int i = chosenCountryList.size()-1; i > 0 ;i --)
        {
            if(chosenCountryList.get(i)[3].equals(chosenDate))
            {
                index = i;
            }
        }
        for (int i = index; i > index - 7 && i > 0; i --)
        {
            try {
                sum = sum + Integer.parseInt(getChosenCountryList().get(i)[NEW_CASES]);
            }
            catch (Exception e){
            }
        }

        return sum;
    }

    public static int getWeeklyDeaths() {
        getChosenCountryList();
        getChosenDate();

        int sum = 0;
        int index = 30;
        for(int i = chosenCountryList.size()-1; i > 0 ;i --)
        {
            if(chosenCountryList.get(i)[3].equals(chosenDate))
            {
                index = i;
            }
        }
        for (int i = index; i > index - 7 && i > 0; i --)
        {
            try {
                sum = sum + Integer.parseInt(getChosenCountryList().get(i)[NEW_DEATHS]);
            }
            catch (Exception e){
            }
        }
        return sum;
    }

    public static int getMonthlyInfections() {
        getChosenCountryList();
        getChosenDate();

        int sum = 0;
        int index = 0;
        for(int i = chosenCountryList.size()-1; i > 0 ;i --)
        {
            if(chosenCountryList.get(i)[3].equals(chosenDate))
            {
                index = i;
            }
        }
        for (int i = index; i > index - 30 && i > 0; i --)
        {
            try {
                sum = sum + Integer.parseInt(getChosenCountryList().get(i)[NEW_CASES]);
            }
            catch (Exception e){
            }
        }
        return sum;
    }

    public static int getMonthlyDeaths() {
        getChosenCountryList();
        getChosenDate();

        int sum = 0;
        int index = 0;
        for(int i = chosenCountryList.size()-1; i > 0 ;i --)
        {
            if(chosenCountryList.get(i)[3].equals(chosenDate))
            {
                index = i;
            }
        }
        for (int i = index; i > index - 30 && i > 0; i --)
        {
            try {
                sum = sum + Integer.parseInt(getChosenCountryList().get(i)[NEW_DEATHS]);
            }
            catch (Exception e){
            }
        }
        return sum;
    }

    public static String[] getMonthlyData() {
        getChosenCountryList();
        getChosenDate();

        String[] monthlyList = new String[chosenRecord.length];
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date = simpleDateFormat.parse(chosenDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int days_to_subtract = calendar.get(calendar.DAY_OF_MONTH);
            calendar.add(calendar.DATE, -1*days_to_subtract);
            String previousDate = simpleDateFormat.format(calendar.getTime());
            boolean isDateInRange = (previousDate.equals(isDateInChosenCountry(previousDate)));
            previousDate = isDateInChosenCountry(previousDate);
            String[] previousRecord = getRecordForDate(previousDate);
            for(int index = 0; index < chosenRecord.length; index++) {
                String currentParameter = chosenRecord[index];
                String previousParameter = previousRecord[index];
                if(currentParameter == null || previousParameter == null) {
                    monthlyList[index] = null;
                } else {
                    try {
                        float test = Float.parseFloat(currentParameter);
                        if(currentParameter.indexOf('.') >= 0 || previousParameter.indexOf('.') >= 0) {
                            float resultParameter;
                            if(isDateInRange) {
                                resultParameter = Float.parseFloat(currentParameter) - Float.parseFloat(previousParameter);
                            } else {
                                resultParameter = Float.parseFloat(currentParameter);
                            }
                            monthlyList[index] = Float.toString(resultParameter);
                        } else {
                            int resultParameter;
                            if(isDateInRange) {
                                resultParameter = Integer.parseInt(currentParameter) - Integer.parseInt(previousParameter);
                            } else {
                                resultParameter = Integer.parseInt(currentParameter);
                            }
                            monthlyList[index] = Integer.toString(resultParameter);
                        }
                    } catch(NumberFormatException e) {
                        monthlyList[index] = currentParameter;
                    }
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return monthlyList;
    }

    private static void setUpIndexes() {
        LOCATION = getIndex("location");
        DATE = getIndex("date");
        TOTAL_CASES = getIndex("total_cases");
        NEW_CASES = getIndex("new_cases");
        TOTAL_DEATHS = getIndex("total_deaths");
        NEW_DEATHS = getIndex("new_deaths");
        TOTAL_CASES_PER_MILLION = getIndex("total_cases_per_million");
        TOTAL_DEATHS_PER_MILLION = getIndex("total_deaths_per_million");
        TOTAL_TESTS = getIndex("total_tests");
        NEW_TESTS = getIndex("new_tests");
        TOTAL_VACCINATIONS = getIndex("total_vaccinations");
        NEW_VACCINATIONS = getIndex("new_vaccinations");
        POPULATION = getIndex("population");
        PEOPLE_FULLY_VACCINATED = getIndex("people_fully_vaccinated");
        NEW_CASES_SMOOTHED = getIndex("new_cases_smoothed");
        NEW_DEATHS_SMOOTHED = getIndex("new_deaths_smoothed");
    }

    public static ArrayList<VaccinationDataRow> getAllRecentVaccinations() {
        ArrayList<VaccinationDataRow> list = new ArrayList<>();
        for(String country : getCountryNameList())
        {
            String value = "";
            String population = "";
            chosenCountryName = country;
            updateChosenCountryList();
            String lastestDate = getLatestDateForParameter(PEOPLE_FULLY_VACCINATED);

            for(int i = getChosenCountryList().size() - 1; i >=0 ; i --)
            {
                if(getChosenCountryList().get(i)[3].equals(lastestDate))
                {
                    value = getChosenCountryList().get(i)[PEOPLE_FULLY_VACCINATED];
                    try {
                        population = getChosenCountryList().get(i)[POPULATION];
                    }
                    catch (IndexOutOfBoundsException e)
                    {
                        System.out.println(e.toString());
                        population = "";
                    }
                }
                if(value.equals("")) value = "0";
                if(population.equals("")) population = "1";
            }

            list.add(new VaccinationDataRow(chosenCountryName,Double.parseDouble(value)/Double.parseDouble(population)));

        }
        return list;
    }
}
