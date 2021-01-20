package com.example.covidapp;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

public class VaccinationsFragment extends Fragment {
    int POPULATION, TOTAL_VACCINATIONS;

    PieChart chart1, chart2;
    ArrayList<ArrayList<String[]>> listDividedByCountries;
    ArrayList<String[]> chosenCountryList;
    String chosenCountryName;
    ArrayList<String> countryNameList;
    Spinner spinner;
    TextView vaccinedText, vaccinedNumberText, noDataText;
    SwipeRefreshLayout swipeRefreshLayout;

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

        swipeRefreshLayout = view.findViewById(R.id.vaccinationsRefresh);

        POPULATION = DataHolder.POPULATION;
        TOTAL_VACCINATIONS = DataHolder.TOTAL_VACCINATIONS;

        setUpCharts();

        ArrayList<VaccinationDataRow> vaccinations = DataHolder.getAllRecentVaccinations();
        Collections.sort(vaccinations, new Comparator<VaccinationDataRow>() {
            @Override
            public int compare(VaccinationDataRow o1, VaccinationDataRow o2) {
                return o1.compareTo(o2);
            }
        });
        for (int i = 0; i<20; i++) {
            final TableLayout mainTable = (TableLayout) view.findViewById(R.id.mainTable);
            final TableRow tableRow = (TableRow) getLayoutInflater().inflate(R.layout.table_row_layout,null);
            TextView countryText;
            TextView valueText;

            DecimalFormat df = new DecimalFormat();
            df.setMaximumFractionDigits(2);

            countryText = (TextView) tableRow.findViewById(R.id.countryText);
            countryText.setText(vaccinations.get(i).getCountry());
            countryText.setTextColor(Color.rgb(204,204,204));

            valueText = (TextView) tableRow.findViewById(R.id.valueText);
            valueText.setTextColor(Color.rgb(204,204,204));
            valueText.setText(String.valueOf(df.format(vaccinations.get(i).getValue() * 100) ));

            mainTable.addView(tableRow);
        }

        spinner = view.findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_item, countryNameList);
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

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        DataHolder.isFragmentUpdateRequired = false;
                        ((MainActivity)getActivity()).checkForFileUpdates(false);
                        synchronized (DataHolder.updateLock) {
                            try {
                                DataHolder.updateLock.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        swipeRefreshLayout.setRefreshing(false);
                        if(DataHolder.isFragmentUpdateRequired) {
                            updateChosenStuff();
                        }
                    }
                };
                Thread thread = new Thread(runnable);
                thread.start();
            }
        });

        return view;
    }

    public void setUpChart1()
    {
        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        float population = Float.parseFloat(DataHolder.getChosenRecord()[POPULATION]);
        float vaccined = Float.parseFloat(DataHolder.getChosenRecord()[TOTAL_VACCINATIONS]);
        startCountAnimation1(vaccinedNumberText, vaccined ," doses");
        startCountAnimation2(vaccinedText,vaccined/population * 100,"%");

        //float population = Float.parseFloat(chosenCountryList.get(chosenCountryList.size()-1)[39]);
        //float vaccined = Float.parseFloat(chosenCountryList.get(chosenCountryList.size()-1)[34]);
        pieEntries.add(new PieEntry(population - vaccined,"population"));
        pieEntries.add(new PieEntry(vaccined,"vaccined"));
        PieDataSet dataSet = new PieDataSet(pieEntries,"");
        dataSet.setColors( Color.rgb(38,89,141),Color.rgb(50,178,50));
        PieData data = new PieData(dataSet);
        chart1.setData(data);
        chart1.setDrawSliceText(false);
        chart1.getData().setDrawValues(false);
        chart1.setDrawEntryLabels(false);
        chart1.getDescription().setEnabled(false);
        chart1.getLegend().setEnabled(false);
        chart1.animateY(1000);
    }


    private void updateChosenStuff() {
        POPULATION = DataHolder.POPULATION;
        TOTAL_VACCINATIONS = DataHolder.TOTAL_VACCINATIONS;

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
        if(infectionDate.equals("") || vaccinationDate.equals("") || populationDate.equals("")) {
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