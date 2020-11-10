package com.example.covidapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class BriefActivity extends AppCompatActivity {

    TextView multiAutoCompleteTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brief);

        multiAutoCompleteTextView = (TextView)findViewById(R.id.multiAutoCompleteTextView);

        Intent intent = getIntent();
        String region = intent.getStringExtra("Region");
        multiAutoCompleteTextView.setText(region);
    }
}