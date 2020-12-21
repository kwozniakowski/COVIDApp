package com.example.covidapp;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;

public class StatisticsActivity extends AppCompatActivity {
    Spinner countrySpinner;
    Spinner infectionsSpinner;
    Spinner deathsSpinner;
    Spinner testsSpinner;
    BarChart barChart1, barChart2, barChart3;

    String chosenCountryName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        countrySpinner = findViewById(R.id.countrySpinner);

        //Ponizej odbywa sie cala zabawa z wykresami, na razie jest to losowy kraj i losowy dzien itp,
        //trzeba zrobic zeby dalo sie fajnie wybrac i chyba nie bedzie to trudne
        barChart1 = findViewById(R.id.barChart1);
        BarDataSet barDataSet1 = new BarDataSet(dataValues1(),"Label");
        BarData barData = new BarData(barDataSet1);
        barChart1.setData(barData);
        barChart1.animateY(2000);

        barChart2 = findViewById(R.id.barChart2);
        barChart3 = findViewById(R.id.barChart3);
        final ArrayList<String> countryNameList = DataHolder.getCountryNameList();
        chosenCountryName = DataHolder.getChosenCountryName();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, countryNameList);
        countrySpinner.setAdapter(adapter);
        countrySpinner.setSelection(countryNameList.indexOf(chosenCountryName));
        countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                DataHolder.updateChosenCountryName(countrySpinner.getSelectedItem().toString());
                chosenCountryName = DataHolder.getChosenCountryName();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        //Trzeba bedzie ustawic tu liste wyboru panstwa dla spinnera ale to juz potem
    }

    private ArrayList<BarEntry> dataValues1()
    {
        DataHolder.getChosenCountryName();
        ArrayList<BarEntry> list = new ArrayList<>();
        list.add(new BarEntry(0,Float.parseFloat(DataHolder.getListDividedByCountries().get(15).get(200)[4])));
        list.add(new BarEntry(1,Float.parseFloat(DataHolder.getListDividedByCountries().get(15).get(201)[4])));
        list.add(new BarEntry(2,Float.parseFloat(DataHolder.getListDividedByCountries().get(15).get(202)[4])));
        list.add(new BarEntry(3,Float.parseFloat(DataHolder.getListDividedByCountries().get(15).get(203)[4])));
        list.add(new BarEntry(4,Float.parseFloat(DataHolder.getListDividedByCountries().get(15).get(204)[4])));
        list.add(new BarEntry(5,Float.parseFloat(DataHolder.getListDividedByCountries().get(15).get(205)[4])));
        return list;

    }
}