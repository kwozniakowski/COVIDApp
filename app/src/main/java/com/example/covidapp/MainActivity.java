package com.example.covidapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;

import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    Button countryBriefButton;
    Button worldBriefButton;
    Button createModelButton;
    Button settingsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        countryBriefButton = (Button)findViewById(R.id.countryBriefButton);
        worldBriefButton = (Button)findViewById(R.id.worldBriefButton);
        createModelButton = (Button)findViewById(R.id.createModelButton);
        settingsButton = (Button)findViewById(R.id.settingsButton);

        InputStream inputStream = getResources().openRawResource(R.raw.covid_data);
        CSVFile csvFile = new CSVFile(inputStream);
        final ArrayList<String[]> scoreList = csvFile.read();
        DataHolder.setScoreList(scoreList);

        countryBriefButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), BriefActivity.class);
                intent.putExtra("Region", "Poland");
                startActivity(intent);
            }
        });

        worldBriefButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), BriefActivity.class);
                intent.putExtra("Region", "World");
                startActivity(intent);
            }
        });

        createModelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ModelingActivity.class);
                startActivity(intent);
            }
        });
    }
}