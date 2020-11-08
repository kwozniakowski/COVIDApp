package com.example.covidapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

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

        countryBriefButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent briefIntent = new Intent(getApplicationContext(), BriefActivity.class);
                startActivity(briefIntent);
            }
        });

        worldBriefButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), BriefActivity.class);
                startActivity(intent);
            }
        });

        createModelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent modelingIntent = new Intent(getApplicationContext(), ModelingActivity.class);
                startActivity(modelingIntent);
            }
        });
    }
}