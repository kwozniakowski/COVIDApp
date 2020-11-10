package com.example.covidapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.InputStream;
import java.util.List;

public class BriefActivity extends AppCompatActivity {

    TextView multiAutoCompleteTextView;
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
        multiAutoCompleteTextView = (TextView)findViewById(R.id.multiAutoCompleteTextView);

        Intent intent = getIntent();
        String region = intent.getStringExtra("Region");
        multiAutoCompleteTextView.setText(region);

        InputStream inputStream = getResources().openRawResource(R.raw.covid_data);
        CSVFile csvFile = new CSVFile(inputStream);
        List scoreList = csvFile.read();
    }
}