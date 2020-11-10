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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BriefActivity extends AppCompatActivity {

    EditText countryInputText;
    Spinner spinner;
    TextView totalInfectionsText;
    TextView newInfectionsText;
    TextView totalDeathsText;
    TextView newDeathsText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brief);

        totalInfectionsText = findViewById(R.id.totalInfectionsText);
        newInfectionsText = findViewById(R.id.newInfectionsText);
        totalDeathsText = findViewById(R.id.totalDeathsText);
        newDeathsText = findViewById(R.id.newDeathsText);
        // countryInputText = (EditText)findViewById(R.id.countryInputText);
        spinner = (Spinner)findViewById(R.id.spinner);

        Intent intent = getIntent();
        String region = intent.getStringExtra("Region");
        // countryInputText.setText(region);

        InputStream inputStream = getResources().openRawResource(R.raw.covid_data);
        CSVFile csvFile = new CSVFile(inputStream);
        final ArrayList<String[]> scoreList = csvFile.read();

        List<String> countries = updateCountryList(scoreList);
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
                //Na sztywno zadeklarowalem 8 listopada. Trzeba bedzie to zmienic na date dzisiejsza, jakos z systemu pobrac i zapisac w
                //odpowiednim formacie
                if(scoreList.get(i)[3].equals("2020-11-08"))
                {
                    a.setText(scoreList.get(i)[4]);
                    b.setText("+" +scoreList.get(i)[5]);
                    c.setText(scoreList.get(i)[7]);
                    d.setText("+" + scoreList.get(i)[8]);
                }
            }
        }
    }

    public List<String> updateCountryList(ArrayList<String[]> scoreList) {
        List<String> countries = new ArrayList<String>();
        countries.add(scoreList.get(1)[2]);
        for(int recordNr = 2; recordNr < scoreList.size(); recordNr++) {
            String recordCountry = scoreList.get(recordNr)[2];
            if( !(recordCountry.equals(countries.get(countries.size() - 1))) ) {
                if(recordCountry.equals("World")) {
                    if(!(countries.get(0).equals("World"))) {
                        countries.add(0, recordCountry);
                    }
                }
                else {
                    countries.add(recordCountry);
                }
            }
        }
        return countries;
    }
}