package com.example.covidapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.InputStream;
import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class BriefActivity extends AppCompatActivity {

    Spinner spinner;
    TextView totalInfectionsText;
    TextView newInfectionsText;
    TextView totalDeathsText;
    TextView newDeathsText;
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
        dateText = findViewById(R.id.textView4);
        spinner = (Spinner)findViewById(R.id.spinner);

        Intent intent = getIntent();
        String region = intent.getStringExtra("Region");

        InputStream inputStream = getResources().openRawResource(R.raw.covid_data);
        CSVFile csvFile = new CSVFile(inputStream);
        final ArrayList<String[]> scoreList = csvFile.read();

        listDividedByCountries = divideListIntoCountries(scoreList);
        List<String> countries = updateCountryList(listDividedByCountries);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, countries);
        spinner.setAdapter(adapter);

        if(countries.indexOf(region) >= 0) {
            spinner.setSelection(countries.indexOf(region));
        }

        setTextsForCountry(spinner.getSelectedItem().toString(),scoreList,totalInfectionsText,newInfectionsText,totalDeathsText,newDeathsText);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                setTextsForCountry(spinner.getSelectedItem().toString(),scoreList,totalInfectionsText,newInfectionsText,totalDeathsText,newDeathsText);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    public void setTextsForCountry(String country, ArrayList<String[]> scoreList, TextView a,TextView b,TextView c,TextView d){
        for(int i=0; i<scoreList.size(); i++)
        {
            if( scoreList.get(i)[2].equals(country) )
            {
                String latestDate = checkLatestDate(country, listDividedByCountries);
                if(scoreList.get(i)[3].equals(latestDate))
                {
                    dateText.setText(latestDate);
                    a.setText(scoreList.get(i)[4]);
                    b.setText("+" +scoreList.get(i)[5]);
                    c.setText(scoreList.get(i)[7]);
                    d.setText("+" + scoreList.get(i)[8]);
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
}