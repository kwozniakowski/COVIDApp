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

public class BriefActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        TextView totalInfectionsText;
        TextView newInfectionsText;
        TextView totalDeathsText;
        TextView newDeathsText;
        String[] countries;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brief);

        totalInfectionsText = findViewById(R.id.totalInfectionsText);
        newInfectionsText = findViewById(R.id.newInfectionsText);
        totalDeathsText = findViewById(R.id.totalDeathsText);
        newDeathsText = findViewById(R.id.newDeathsText);

        InputStream inputStream = getResources().openRawResource(R.raw.covid_data);
        CSVFile csvFile = new CSVFile(inputStream);
        List scoreList = csvFile.read();

    }


}