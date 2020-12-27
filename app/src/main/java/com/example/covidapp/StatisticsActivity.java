package com.example.covidapp;

import android.graphics.RectF;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.MPPointF;

import java.util.ArrayList;

public class StatisticsActivity extends AppCompatActivity {
    Spinner countrySpinner;
    Spinner chartSpinner1, chartSpinner2,chartSpinner3;
    ArrayList<ArrayList<String[]>> listDividedByCountries;
    ArrayList<String[]> chosenCountryList;
    String chosenCountryName;
    ArrayList<String> countryNameList;
    BarChart barChart1, barChart2, barChart3;
    ArrayList<String> statisticalData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        listDividedByCountries = DataHolder.getListDividedByCountries();
        countryNameList = DataHolder.getCountryNameList();
        final ArrayList<String> countryNameList = DataHolder.getCountryNameList();
        chosenCountryName = DataHolder.getChosenCountryName();

        //spinnery
        countrySpinner = findViewById(R.id.countrySpinner);
        chartSpinner1 = findViewById(R.id.chartSpinner1);
        chartSpinner2 = findViewById(R.id.chartSpinner2);
        chartSpinner3 = findViewById(R.id.chartSpinner3);

        //wykresy
        barChart1 = findViewById(R.id.barChart1);
        barChart2 = findViewById(R.id.barChart2);
        barChart3 = findViewById(R.id.barChart3);
        //Funkcja odpowiedzialna za rysowanie wykresu
        drawChart(barChart1, "new infections");
        drawChart(barChart2, "new deaths");
        drawChart(barChart3, "new tests");

        statisticalData = new ArrayList<String>();
        setStatisticalData();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, countryNameList);
        countrySpinner.setAdapter(adapter);
        countrySpinner.setSelection(countryNameList.indexOf(chosenCountryName));
        countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                DataHolder.updateChosenCountryName(countrySpinner.getSelectedItem().toString());
                chosenCountryName = DataHolder.getChosenCountryName();
                drawChart(barChart1, "new infections");
                drawChart(barChart2, "new deaths");
                drawChart(barChart3, "new tests");
                chartSpinner1.setSelection(0);
                chartSpinner2.setSelection(1);
                chartSpinner3.setSelection(2);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item, statisticalData);
        chartSpinner1.setAdapter(adapter1);
        chartSpinner1.setSelection(0);
        chartSpinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                DataHolder.updateChosenCountryName(countrySpinner.getSelectedItem().toString());
                chosenCountryName = DataHolder.getChosenCountryName();
                drawChart(barChart1, chartSpinner1.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item, statisticalData);
        chartSpinner2.setAdapter(adapter2);
        chartSpinner2.setSelection(0);
        chartSpinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                DataHolder.updateChosenCountryName(countrySpinner.getSelectedItem().toString());
                chosenCountryName = DataHolder.getChosenCountryName();
                drawChart(barChart2, chartSpinner2.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ArrayAdapter<String> adapter3 = new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item, statisticalData);
        chartSpinner3.setAdapter(adapter3);
        chartSpinner3.setSelection(0);
        chartSpinner3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                DataHolder.updateChosenCountryName(countrySpinner.getSelectedItem().toString());
                chosenCountryName = DataHolder.getChosenCountryName();
                drawChart(barChart3, chartSpinner3.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //Poniższe nie działa, trzeba zrobić żeby po naciśnięciu na słupek pokazywała się dokładna wartość z datą
        barChart1.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            protected RectF mOnValueSelectedRectF = new RectF();
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                if (e == null)
                    return;

                RectF bounds = mOnValueSelectedRectF;
                barChart1.getBarBounds((BarEntry) e, bounds);
                MPPointF position = barChart1.getPosition(e, YAxis.AxisDependency.LEFT);

                System.out.println("bounds"+ bounds.toString());
                System.out.println("position"+ position.toString());

                System.out.println("x-index" +
                        "low: " + barChart1.getLowestVisibleX() + ", high: "
                                + barChart1.getHighestVisibleX());

                MPPointF.recycleInstance(position);
            }

            @Override
            public void onNothingSelected() {

            }
        });
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

    private void drawChart(BarChart chart, String label)
    {
        BarDataSet barDataSet = new BarDataSet(dataValues(label),label);
        BarData barData = new BarData(barDataSet);
        barData.setDrawValues(false);
        chart.setData(barData);
        chart.animateY(1000);
        chart.getAxisLeft().setDrawGridLines(false);
        chart.getXAxis().setDrawGridLines(false);
        chart.setAutoScaleMinMaxEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setEnabled(true);
        chart.setTouchEnabled(true);
    }

    public void setStatisticalData()
    {
        statisticalData.add("new infections");
        statisticalData.add("new deaths");
        statisticalData.add("new tests");
        statisticalData.add("total infections per 1 mln");
        statisticalData.add("total deaths per 1 mln");
        statisticalData.add("% of positive tests");
        statisticalData.add("day to day % growth");
    }

    private ArrayList<BarEntry> dataValues(String statisticalData)
    {
        ArrayList<String[]> chosenCountryList = DataHolder.getChosenCountryList();
        ArrayList<BarEntry> list = new ArrayList<>();
        //Tutaj wyswietlimy sobie zakazenia dla ostatnigo tygodnia
        if(statisticalData == "new infections")
        {
            for(int i=chosenCountryList.size() -1; i > chosenCountryList.size() - 30; i--)
            {
                list.add(new BarEntry(i, Integer.parseInt(chosenCountryList.get(i)[5] )));
            }
        }
        else if(statisticalData == "new deaths")
        {
            for(int i=chosenCountryList.size() -1; i > chosenCountryList.size() - 30; i--)
            {
                list.add(new BarEntry(i, Integer.parseInt(chosenCountryList.get(i)[8] )));
            }
        }
        else if(statisticalData == "new tests")
        {
            for(int i=chosenCountryList.size() -1; i > chosenCountryList.size() - 30; i--)
            {
                if(!chosenCountryList.get(i)[25].equals(""))
                {
                    list.add(new BarEntry(i, Float.parseFloat(chosenCountryList.get(i)[25] )));
                }
                else
                {
                    list.add(new BarEntry(i, 0 ));
                }
            }
        }
        else if(statisticalData == "total infections per 1 mln")
        {
            for(int i=chosenCountryList.size() -1; i > chosenCountryList.size() - 30; i--)
            {
                try
                {
                    list.add(new BarEntry(i, Float.parseFloat(chosenCountryList.get(i)[10] )));
                }
                catch (Exception e)
                {
                    list.add(new BarEntry(i, 0));
                }

            }
        }
        else if(statisticalData == "total deaths per 1 mln")
        {
            for(int i=chosenCountryList.size() -1; i > chosenCountryList.size() - 30; i--)
            {
                list.add(new BarEntry(i, Float.parseFloat(chosenCountryList.get(i)[13] )));
            }
        }
        else if(statisticalData == "% of positive tests")
        {
            for(int i=chosenCountryList.size() -1; i > chosenCountryList.size() - 30; i--)
            {
                if(!chosenCountryList.get(i)[25].equals(""))
                {
                    list.add(new BarEntry(i, (Float) Float.parseFloat(chosenCountryList.get(i)[5] ) /
                            Float.parseFloat(chosenCountryList.get(i)[25] ) * 100 ));//lub 26
                }
                else
                {
                    list.add(new BarEntry(i,0));
                }

            }
        }
        else if(statisticalData == "day to day % growth")
        {
            for(int i=chosenCountryList.size() -1; i > chosenCountryList.size() - 30; i--)
            {
                try {
                    list.add(new BarEntry(i, (Float.parseFloat(chosenCountryList.get(i-1)[5]) /
                            Integer.parseInt(chosenCountryList.get(i)[5] ) - 1 )* 100));
                }
                catch (Exception e)
                {
                    list.add(new BarEntry(i,0));
                }
            }
        }
        else
        {
            for(int i=chosenCountryList.size() -1; i > chosenCountryList.size() - 30; i--)
            {
                list.add(new BarEntry(i, Integer.parseInt(chosenCountryList.get(i)[5] )));
            }
        }
        return list;
    }
}