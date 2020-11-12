package com.example.covidapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class StatisticsActivity extends AppCompatActivity {
    Spinner countrySpinner;
    Spinner infectionsSpinner;
    Spinner deathsSpinner;
    Spinner testsSpinner;

    String chosenCountryName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        countrySpinner = findViewById(R.id.countrySpinner);

        final ArrayList<String> countryNameList = DataHolder.getCountryNameList();
        chosenCountryName = DataHolder.getChosenCountryName();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, countryNameList);
        countrySpinner.setAdapter(adapter);
        countrySpinner.setSelection(countryNameList.indexOf(chosenCountryName));
        //Trzeba bedzie ustawic tu liste wyboru panstwa dla spinnera ale to juz potem
    }
}