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
    ArrayList<ArrayList<String[]>> listDividedByCountries;
    ArrayList<String[]> chosenCountryList;
    String chosenCountryName;
    ArrayList<String> countryNameList;
    BarChart barChart1, barChart2, barChart3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        listDividedByCountries = DataHolder.getListDividedByCountries();
        countryNameList = DataHolder.getCountryNameList();

        countrySpinner = findViewById(R.id.countrySpinner);

        //Ponizej odbywa sie cala zabawa z wykresami, na razie jest to losowy kraj i losowy dzien itp,
        //trzeba zrobic zeby dalo sie fajnie wybrac i chyba nie bedzie to trudne
        barChart1 = findViewById(R.id.barChart1);
        barChart2 = findViewById(R.id.barChart2);
        barChart3 = findViewById(R.id.barChart3);
        //Funkcja odpowiedzialna za rysowanie wykresu
        drawChart(barChart1, "infections");
        drawChart(barChart2, "deaths");
        drawChart(barChart3, "tests");


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
                drawChart(barChart1, "infections");
                drawChart(barChart2, "deaths");
                drawChart(barChart3, "tests");
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        //Trzeba bedzie ustawic tu liste wyboru panstwa dla spinnera ale to juz potem
    }

    private ArrayList<BarEntry> dataValues1()
    {
        ArrayList<String[]> chosenCountryList = DataHolder.getChosenCountryList();
        ArrayList<BarEntry> list = new ArrayList<>();
        //Tutaj wyswietlimy sobie zakazenia dla ostatnigo tygodnia
        for(int i=chosenCountryList.size() -1; i > chosenCountryList.size() - 30; i--)
        {
            list.add(new BarEntry(i, Integer.parseInt(chosenCountryList.get(i)[5] )));
        }
        return list;
    }
    private ArrayList<BarEntry> dataValues2()
    {
        ArrayList<String[]> chosenCountryList = DataHolder.getChosenCountryList();
        ArrayList<BarEntry> list = new ArrayList<>();
        //Tutaj wyswietlimy sobie zakazenia dla ostatnigo tygodnia
        for(int i=chosenCountryList.size() -1; i > chosenCountryList.size() - 30; i--)
        {
            list.add(new BarEntry(i, Integer.parseInt(chosenCountryList.get(i)[8] )));
        }
        return list;
    }
    //tu bedzie funkcja do wyswietlania trzeciego wykresu, ciezko powiedziec co bedzie on przedstawiac
    /*private ArrayList<BarEntry> dataValues3()
    {
        ArrayList<String[]> chosenCountryList = DataHolder.getChosenCountryList();
        ArrayList<BarEntry> list = new ArrayList<>();
        //Tutaj wyswietlimy sobie zakazenia dla ostatnigo tygodnia
        for(int i=chosenCountryList.size() -1; i > chosenCountryList.size() - 8; i--)
        {
            list.add(new BarEntry(i, Integer.parseInt(chosenCountryList.get(i)[5] )));
        }
        return list;
    }*/

    private void drawChart(BarChart chart, String label)
    {
        BarDataSet barDataSet;
        if(label == "infections")
        {
            barDataSet = new BarDataSet(dataValues1(),label);
        }
        if(label == "deaths")
        {
            barDataSet = new BarDataSet(dataValues2(),label);
        }
        else barDataSet = new BarDataSet(dataValues1(), "infections");
        BarData barData = new BarData(barDataSet);
        chart.setData(barData);
        chart.animateY(1000);
        chart.getAxisLeft().setDrawGridLines(false);
        chart.getXAxis().setDrawGridLines(false);
        chart.setAutoScaleMinMaxEnabled(false);
        chart.getAxisRight().setEnabled(false);
    }
}