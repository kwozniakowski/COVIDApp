package com.example.covidapp;

import androidx.appcompat.app.AppCompatActivity;
import org.json.*;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import com.opencsv.CSVReader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.List;


import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class BriefActivity extends AppCompatActivity {

    TextView multiAutoCompleteTextView;
	TextView totalInfectionsText;
    TextView newInfectionsText;
    TextView totalDeathsText;
    TextView newDeathsText;
    String[] countries;

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