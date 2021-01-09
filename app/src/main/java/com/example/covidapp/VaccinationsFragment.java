package com.example.covidapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

public class VaccinationsFragment extends Fragment {
    PieChart chart1, chart2;
    ArrayList<ArrayList<String[]>> listDividedByCountries;
    ArrayList<String[]> chosenCountryList;
    String chosenCountryName;
    ArrayList<String> countryNameList;
    Spinner spinner;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_vaccinations, container, false);

        chart1 = view.findViewById(R.id.chart1);
        chart2 = view.findViewById(R.id.chart2);
        chosenCountryList = DataHolder.getChosenCountryList();
        chosenCountryName = DataHolder.getChosenCountryName();
        countryNameList = DataHolder.getCountryNameList();

        setUpCharts();
        //setUpDate();

        //setUpChart1();
        //setUpChart2();
        spinner = view.findViewById(R.id.spinner);
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
        //float population = Float.parseFloat(chosenCountryList.get(chosenCountryList.size()-1)[39]);
        //float vaccined = Float.parseFloat(chosenCountryList.get(chosenCountryList.size()-1)[34]);
        pieEntries.add(new PieEntry(population - vaccined,"infected"));
        pieEntries.add(new PieEntry(vaccined,"vaccined"));
        PieDataSet dataSet = new PieDataSet(pieEntries,"");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        PieData data = new PieData(dataSet);
        chart1.setData(data);
        chart1.animateY(1000);
    }

    public void setUpChart2()
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
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        PieData data = new PieData(dataSet);
        chart2.setData(data);
        chart2.animateY(1000);
    }

    private void updateChosenStuff() {
        chosenCountryName = DataHolder.getChosenCountryName();
        chosenCountryList = DataHolder.getChosenCountryList();
    }

    private void setUpCharts() {
        if(setUpDate()) {
            setUpChart1();
            setUpChart2();
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
}