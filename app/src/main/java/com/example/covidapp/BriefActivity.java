package com.example.covidapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

public class BriefActivity extends AppCompatActivity {

    EditText countryInputText;
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
        countryInputText = (EditText)findViewById(R.id.countryInputText);

        Intent intent = getIntent();
        String region = intent.getStringExtra("Region");
        countryInputText.setText(region);

        InputStream inputStream = getResources().openRawResource(R.raw.covid_data);
        CSVFile csvFile = new CSVFile(inputStream);
        final ArrayList<String[]> scoreList = csvFile.read();
        setTextsForCountry(countryInputText.getText().toString(),scoreList,totalInfectionsText,newInfectionsText,totalDeathsText,newDeathsText);

        countryInputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                setTextsForCountry(countryInputText.getText().toString(),scoreList,totalInfectionsText,newInfectionsText,totalDeathsText,newDeathsText);
            }
        });

        System.out.println(Arrays.toString(scoreList.get(2)));
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
}