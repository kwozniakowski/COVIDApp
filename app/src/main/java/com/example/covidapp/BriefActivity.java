package com.example.covidapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class BriefActivity extends AppCompatActivity {

    Spinner spinner;
    Button statisticsActivityButton;
    TextView totalInfectionsText;
    TextView newInfectionsText;
    TextView totalDeathsText;
    TextView newDeathsText;
    TextView totalTestsText;
    TextView newTestsText;
    TextView dateText;

    ArrayList<ArrayList<String[]>> listDividedByCountries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brief);

        totalInfectionsText = findViewById(R.id.totalInfectionsText);
        newInfectionsText = findViewById(R.id.newInfectionsText);
        totalDeathsText = findViewById(R.id.totalDeathsText);
        newDeathsText = findViewById(R.id.newDeathsText);
        totalTestsText = findViewById(R.id.totalTestsText);
        newTestsText = findViewById(R.id.newTestsText);
        dateText = findViewById(R.id.textView4);
        spinner = (Spinner)findViewById(R.id.countrySpinner);
        statisticsActivityButton = findViewById(R.id.statisticsActivityButton);
        statisticsActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), StatisticsActivity.class);
                intent.putExtra("Region", spinner.getSelectedItem().toString());
                startActivity(intent);
            }
        });

        Intent intent = getIntent();
        String region = intent.getStringExtra("Region");
        
        final ArrayList<String[]> scoreList = DataHolder.getScoreList();

        listDividedByCountries = divideListIntoCountries(scoreList);
        List<String> countries = updateCountryList(listDividedByCountries);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, countries);
        spinner.setAdapter(adapter);

        if(countries.indexOf(region) >= 0) {
            spinner.setSelection(countries.indexOf(region));
        }

        setTextsForCountry(spinner.getSelectedItem().toString(),scoreList,totalInfectionsText,newInfectionsText,
                totalDeathsText,newDeathsText,totalTestsText,newTestsText);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                setTextsForCountry(spinner.getSelectedItem().toString(),scoreList,totalInfectionsText,newInfectionsText,
                        totalDeathsText,newDeathsText,totalTestsText,newTestsText);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    public void setTextsForCountry(String country, ArrayList<String[]> scoreList, TextView a,TextView b,TextView c,TextView d, TextView e, TextView f){
        for(int i=0; i<scoreList.size(); i++)
        {
            if( scoreList.get(i)[2].equals(country) )
            {
                String latestDate = checkLatestDate(country, listDividedByCountries);
                if(scoreList.get(i)[3].equals(latestDate))
                {
                    dateText.setText(latestDate);

                    String aStr = removeFloatingPointFromString(scoreList.get(i)[4]);
                    a.setText(aStr);
                    String bStr = removeFloatingPointFromString(scoreList.get(i)[5]);
                    b.setText("+" + bStr);
                    String cStr = removeFloatingPointFromString(scoreList.get(i)[7]);
                    c.setText(cStr);
                    String dStr = removeFloatingPointFromString(scoreList.get(i)[8]);
                    d.setText("+" + dStr);
                    //Nasz plik nie ma danych dla nowych testow dla ostatnich dób, dlatego raczej zrezygnujemy z tego
                    /*String eStr = removeFloatingPointFromString(scoreList.get(i)[24]);
                    e.setText(eStr);
                    String fStr = removeFloatingPointFromString(scoreList.get(i)[25]);
                    f.setText("+" + fStr);*/
                }
            }
        }
    }

    public List<String> updateCountryList(ArrayList<ArrayList<String[]>> dividedList) {
        List<String> countries = new ArrayList<String>();
        countries.add(dividedList.get(0).get(0)[2]);
        for(int countryNr = 1; countryNr < dividedList.size(); countryNr++) {
            String countryName = dividedList.get(countryNr).get(0)[2];
            if(countryName.equals("World")) {
                if(!(countries.get(0).equals("World"))) {
                    countries.add(0, countryName);
                }
            }
            countries.add(dividedList.get(countryNr).get(0)[2]);
        }
        return countries;
    }

    // Dzieli cala liste na poszczegolne kraje: z postaci lista[nrRekordu][nrDanej]
    // powstaje lista[nrKraju][nrRekordu][nrDanej]
    public ArrayList<ArrayList<String[]>> divideListIntoCountries(ArrayList<String[]> scoreList) {
        ArrayList<ArrayList<String[]>> dividedList = new ArrayList<ArrayList<String[]>>();
        dividedList.add(new ArrayList<String[]>());
        dividedList.get(0).add(scoreList.get(1));
        int countryNr = 0;
        for(int recordNr = 2; recordNr < scoreList.size(); recordNr++) {
            String[] record = scoreList.get(recordNr);
            String recordCountry = record[2];
            if(recordCountry.equals(dividedList.get(countryNr).get(0)[2]) ) {
                dividedList.get(countryNr).add(record);
            } else {
                dividedList.add(new ArrayList<String[]>());
                countryNr++;
                dividedList.get(countryNr).add(record);
            }
        }
        return dividedList;
    }

    public String checkLatestDate(String country, ArrayList<ArrayList<String[]>> dividedList) {
        ArrayList<String[]> scoreList = new ArrayList<String[]>();

        for(int countryNr = 0; countryNr < dividedList.size(); countryNr++) {
            if(dividedList.get(countryNr).get(0)[2].equals(country)) {
                scoreList = dividedList.get(countryNr);
                break;
            }
        }

        return scoreList.get(scoreList.size() - 1)[3];
    }

    public String removeFloatingPointFromString(String text) {
        if(text.indexOf(".") >= 0) {
            text = text.substring(0, text.indexOf("."));
        }
        return text;
    }
}