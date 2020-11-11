package com.example.covidapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

public class StatisticsActivity extends AppCompatActivity {
    Spinner countrySpinner;
    Spinner infectionsSpinner;
    Spinner deathsSpinner;
    Spinner testsSpinner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        Intent intent = getIntent();
        String region = intent.getStringExtra("Region");
        countrySpinner = findViewById(R.id.countrySpinner);
        //Trzeba bedzie ustawic tu liste wyboru panstwa dla spinnera ale to juz potem
    }
}