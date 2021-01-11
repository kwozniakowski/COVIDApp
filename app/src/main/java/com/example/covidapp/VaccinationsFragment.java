package com.example.covidapp;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class VaccinationsFragment extends Fragment {
    PieChart chart1, chart2;
    ArrayList<ArrayList<String[]>> listDividedByCountries;
    ArrayList<String[]> chosenCountryList;
    String chosenCountryName;
    ArrayList<String> countryNameList;
    Spinner spinner;
    TextView vaccinedText, vaccinedNumberText, noDataText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_vaccinations, container, false);

        chart1 = view.findViewById(R.id.chart1);
        vaccinedText= view.findViewById(R.id.vaccinedText);
        vaccinedNumberText = view.findViewById(R.id.vaccinedNumberText);
        noDataText = view.findViewById(R.id.noDataText);

        //chart2 = view.findViewById(R.id.chart2);
        chosenCountryList = DataHolder.getChosenCountryList();
        chosenCountryName = DataHolder.getChosenCountryName();
        countryNameList = DataHolder.getCountryNameList();

        setUpCharts();
        //setUpDate();

        //setUpChart1();
        //setUpChart2();
        spinner = view.findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, countryNameList);
        spinner.setAdapter(adapter);
        spinner.setSelection(countryNameList.indexOf(chosenCountryName));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                DataHolder.updateChosenCountryName(spinner.getSelectedItem().toString());
                chosenCountryName = DataHolder.getChosenCountryName();
                updateChosenStuff();
                setUpCharts();
                //setUpChart1();
                //setUpChart2();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return view;
    }

    public void setUpChart1()
    {
        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        float population = Float.parseFloat(DataHolder.getChosenRecord()[39]);
        float vaccined = Float.parseFloat(DataHolder.getChosenRecord()[34]);
        startCountAnimation1(vaccinedNumberText, vaccined ," doses");
        startCountAnimation2(vaccinedText,vaccined/population * 100,"%");

        //float population = Float.parseFloat(chosenCountryList.get(chosenCountryList.size()-1)[39]);
        //float vaccined = Float.parseFloat(chosenCountryList.get(chosenCountryList.size()-1)[34]);
        pieEntries.add(new PieEntry(population - vaccined,"population"));
        pieEntries.add(new PieEntry(vaccined,"vaccined"));
        PieDataSet dataSet = new PieDataSet(pieEntries,"");
        dataSet.setColors( Color.rgb(128,0,0),Color.rgb(0,128,0));
        PieData data = new PieData(dataSet);
        chart1.setData(data);
        chart1.setDrawSliceText(false);
        chart1.getData().setDrawValues(false);
        chart1.setDrawEntryLabels(false);
        chart1.getDescription().setEnabled(false);
        chart1.getLegend().setEnabled(false);
        chart1.animateY(1000);
    }

    /*public void setUpChart2()
    {
        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        //float population = Float.parseFloat(chosenCountryList.get(chosenCountryList.size()-1)[39]);
        //float vaccined = Float.parseFloat(chosenCountryList.get(chosenCountryList.size()-1)[34]);
        //float infected = Float.parseFloat(chosenCountryList.get(chosenCountryList.size()-1)[4]);
        float population = Float.parseFloat(DataHolder.getChosenRecord()[39]);
        float vaccined = Float.parseFloat(DataHolder.getChosenRecord()[34]);
        float infected = Float.parseFloat(DataHolder.getChosenRecord()[4]);
        float possiblyInfected = infected * 9;
        pieEntries.add(new PieEntry(population - vaccined - infected - possiblyInfected,"never infected"));
        pieEntries.add(new PieEntry(vaccined,"vaccined"));
        pieEntries.add(new PieEntry(infected,"infected"));
        pieEntries.add(new PieEntry(possiblyInfected,"possibly infected"));
        PieDataSet dataSet = new PieDataSet(pieEntries,"");
        dataSet.setColors( Color.rgb(200,200,200),Color.rgb(0,255,0),Color.rgb(255,0,0),Color.rgb(0,0,255));
        PieData data = new PieData(dataSet);
        chart2.setData(data);
        chart2.setDrawSliceText(false);
        chart2.getData().setDrawValues(false);
        chart2.setDrawEntryLabels(false);
        chart2.getDescription().setEnabled(false);
        chart2.getLegend().setEnabled(false);
        chart2.animateY(1000);
    }*/

    private void updateChosenStuff() {
        chosenCountryName = DataHolder.getChosenCountryName();
        chosenCountryList = DataHolder.getChosenCountryList();
    }

    private void setUpCharts() {
        if(setUpDate()) {
            noDataText.setVisibility(View.INVISIBLE);
            setUpChart1();
            //setUpChart2();
        }
        else
        {
            noDataText.setVisibility(View.VISIBLE);
        }
    }

    private boolean setUpDate() {
        String infectionDate = DataHolder.getLatestInfectionDate();
        String vaccinationDate = DataHolder.getLatestVaccinationDate();
        String populationDate = DataHolder.getLatestPopulationDate();
        if(infectionDate.isEmpty() || vaccinationDate.isEmpty() || populationDate.isEmpty()) {
            return false;
        } else {
            String minDate = infectionDate;
            if(minDate.compareTo(vaccinationDate) > 0) {
                minDate = vaccinationDate;
            }
            if(minDate.compareTo(populationDate) > 0) {
                minDate = populationDate;
            }
            DataHolder.updateChosenDate(minDate);
            return true;
        }
    }

    private void startCountAnimation1(final TextView textView, final float finalValue, final String additionalText) {
        ValueAnimator animator = ValueAnimator.ofFloat(0, finalValue);
        animator.setDuration(1000);
        final NumberFormat numberFormat = NumberFormat.getIntegerInstance(Locale.UK);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                String newText = "";
                newText += numberFormat.format(animation.getAnimatedValue());
                newText += additionalText;
                textView.setText(newText);
            }
        });
        animator.start();
    }

    private void startCountAnimation2(final TextView textView, final float finalValue, final String additionalText) {
        ValueAnimator animator = ValueAnimator.ofFloat(0, finalValue);
        animator.setDuration(1000);
        final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.UK);
        numberFormat.setMaximumFractionDigits(2);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                String newText = "";
                newText += numberFormat.format(animation.getAnimatedValue());
                newText += additionalText;
                textView.setText(newText);
            }
        });
        animator.start();
    }
}