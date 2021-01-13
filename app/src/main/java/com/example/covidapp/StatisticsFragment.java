package com.example.covidapp;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.RectF;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.MPPointF;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class StatisticsFragment extends Fragment {

    Spinner countrySpinner;
    Spinner chartSpinner1, chartSpinner2,chartSpinner3;
    ArrayList<ArrayList<String[]>> listDividedByCountries;
    ArrayList<String[]> chosenCountryList;
    String chosenCountryName;
    ArrayList<String> countryNameList;
    BarChart barChart1, barChart2, barChart3;
    ArrayList<String> statisticalData;
    TextView dateText1, dateText2;
    private int mDate, mMonth, mYear;
    String chosenDate, chosenStartDate, chosenEndDate;
    String[] chosenRecord;

    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        listDividedByCountries = DataHolder.getListDividedByCountries();
        countryNameList = DataHolder.getCountryNameList();
        final ArrayList<String> countryNameList = DataHolder.getCountryNameList();
        chosenCountryName = DataHolder.getChosenCountryName();

        //spinnery
        countrySpinner = view.findViewById(R.id.header);
        chartSpinner1 = view.findViewById(R.id.chartSpinner1);
        chartSpinner2 = view.findViewById(R.id.chartSpinner2);
        chartSpinner3 = view.findViewById(R.id.chartSpinner3);

        swipeRefreshLayout = view.findViewById(R.id.statisticsRefresh);

        //wykresy
        barChart1 = view.findViewById(R.id.barChart1);
        barChart2 = view.findViewById(R.id.barChart2);
        barChart3 = view.findViewById(R.id.barChart3);

        //text kalendarza
        dateText1 = view.findViewById(R.id.dateText1);
        dateText2 = view.findViewById(R.id.dateText2);
        //Funkcja odpowiedzialna za rysowanie wykresu
        drawChart(barChart1, "new infections");
        drawChart(barChart2, "new deaths");
        drawChart(barChart3, "new tests");

        chosenEndDate = DataHolder.getChosenDate();
        chosenStartDate = subtractDaysFromDate(chosenEndDate, 7); //DataHolder.getChosenDate();
        updateChosenStuff();

        statisticalData = new ArrayList<String>();
        setStatisticalData();

        setUpCalendar();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, countryNameList);
        countrySpinner.setAdapter(adapter);
        countrySpinner.setSelection(countryNameList.indexOf(chosenCountryName));
        countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                DataHolder.updateChosenCountryName(countrySpinner.getSelectedItem().toString());
                chosenCountryName = DataHolder.getChosenCountryName();
                updateChosenStuff();
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

        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(getActivity(),android.R.layout.simple_spinner_dropdown_item, statisticalData);
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

        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(getActivity(),android.R.layout.simple_spinner_dropdown_item, statisticalData);
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

        ArrayAdapter<String> adapter3 = new ArrayAdapter<>(getActivity(),android.R.layout.simple_spinner_dropdown_item, statisticalData);
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
                System.out.println(chosenCountryList.get((int)h.getX())[3]);
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

        barDataSet.setColor(Color.rgb(204,204,204));
        BarData barData = new BarData(barDataSet);
        barData.setDrawValues(false);
        chart.animateY(1000);
        chart.getAxisLeft().setDrawGridLines(false);
        chart.getXAxis().setDrawGridLines(false);

        chart.setAutoScaleMinMaxEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.getDescription().setEnabled(false);
        chart.getXAxis().setEnabled(false);
        chart.getAxisLeft().setEnabled(true);
        chart.getAxisLeft().setTextColor(Color.rgb(204,204,204));

        //Nie chce dzialac i nie wiem dlaczego
        if(!label.equals("day to day % growth"))
        {
            System.out.println("Weszlo");
            chart.getAxisLeft().setAxisMinimum(0);
        }
        else
        {
            System.out.println("Kur*a");
            chart.getAxisLeft().resetAxisMinimum();
        }

        chart.setTouchEnabled(true);
        chart.setHighlightPerTapEnabled(true);
        chart.setData(barData);


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
        statisticalData.add("death rate");
    }

    private ArrayList<BarEntry> dataValues(String statisticalData) {
        ArrayList<String[]> chosenCountryList = DataHolder.getChosenCountryList();
        ArrayList<BarEntry> list = new ArrayList<>();
        int startIndex = chosenCountryList.size()-2;
        int endIndex = chosenCountryList.size()-1;
        //Tutaj wyswietlimy sobie zakazenia dla ostatnigo tygodnia
        for (int i = 0; i <= chosenCountryList.size() - 1; i++)
        {
            if(chosenCountryList.get(i)[3].equals(chosenEndDate)) endIndex = i;
            if(chosenCountryList.get(i)[3].equals(chosenStartDate)) startIndex = i;
        }
        for(int i = startIndex; i <= endIndex ; i ++)
        {
            if(statisticalData == "new infections")
            {
                list.add(new BarEntry(i, Integer.parseInt(chosenCountryList.get(i)[5] )));
            }
            else if(statisticalData == "new deaths")
            {
                list.add(new BarEntry(i, Integer.parseInt(chosenCountryList.get(i)[8] )));
            }
            else if(statisticalData == "new tests")
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
            else if(statisticalData == "total infections per 1 mln")
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
            else if(statisticalData == "total deaths per 1 mln")
            {
                list.add(new BarEntry(i, Float.parseFloat(chosenCountryList.get(i)[13] )));
            }
            else if(statisticalData == "% of positive tests")
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
            else if(statisticalData == "day to day % growth")
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
            else if(statisticalData == "death rate")
            {
                list.add(new BarEntry(i,(Float.parseFloat(chosenCountryList.get(i)[9]) /
                        Float.parseFloat(chosenCountryList.get(i)[6]) * 100)));
            }
            else
            {
                list.add(new BarEntry(i, Integer.parseInt(chosenCountryList.get(i)[5] )));
            }
        }
        return list;
    }

    private void setUpCalendar() {
        // Wyswietlanie kalendarza do wyboru daty
        dateText1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Dane potrzebne do ustawien
                final Calendar calendar = Calendar.getInstance();
                mDate = calendar.get(Calendar.DATE);
                mMonth = calendar.get(Calendar.MONTH);
                mYear = calendar.get(Calendar.YEAR);
                // Pobieram minimalna i maksymalna date dostepna dla obecnie wybranego kraju
                long minDate = getChosenCountryMinTime();
                long maxDate = getChosenCountryMaxTime();

                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), android.R.style.Theme_DeviceDefault_Dialog, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int date) {
                        // Ta czesc wykona sie po wybraniu daty

                        // Zamieniam date wybrana w kalendarzu do postaci stringa ("yyyy-MM-dd")
                        String newDate = calendarDateToString(year, month, date);
                        // Zmieniam wybrana date w DataHolderze
                        chosenStartDate = newDate;
                        // Zmieniajac date w DataHolderze, zmienil sie tam tez chosenRecord
                        // Dlatego tutaj tez trzeba zaktualizowac dane
                        updateChosenStuff();
                        drawChart(barChart1,chartSpinner1.getSelectedItem().toString());
                        drawChart(barChart2,chartSpinner2.getSelectedItem().toString());
                        drawChart(barChart3,chartSpinner3.getSelectedItem().toString());

                    }
                }, mYear, mMonth, mDate);

                // Ustawiam minimalna i maksymalna date, ktore wczesniej pobralem
                datePickerDialog.getDatePicker().setMinDate(minDate);
                datePickerDialog.getDatePicker().setMaxDate(maxDate);

                // Jesli zmieniono date z najnowszej na inna, zapisze ten wybor,
                // bo inaczej gdyby zostal zmieniony kraj, data z powrotem bedzie najnowsza
                if(!(chosenStartDate.equals(chosenCountryList.get(chosenCountryList.size() - 1)[3]))) {
                    int[] parts = stringDateToInt(chosenStartDate);
                    datePickerDialog.getDatePicker().init(parts[0], parts[1], parts[2], null);
                }
                datePickerDialog.show();
            }
        });
        dateText2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Dane potrzebne do ustawien
                final Calendar calendar = Calendar.getInstance();
                mDate = calendar.get(Calendar.DATE);
                mMonth = calendar.get(Calendar.MONTH);
                mYear = calendar.get(Calendar.YEAR);
                // Pobieram minimalna i maksymalna date dostepna dla obecnie wybranego kraju
                long minDate = getChosenCountryMinTime();
                long maxDate = getChosenCountryMaxTime();

                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), android.R.style.Theme_DeviceDefault_Dialog, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int date) {
                        // Ta czesc wykona sie po wybraniu daty

                        // Zamieniam date wybrana w kalendarzu do postaci stringa ("yyyy-MM-dd")
                        String newDate = calendarDateToString(year, month, date);
                        // Zmieniam wybrana date w DataHolderze
                        chosenEndDate = newDate;
                        // Zmieniajac date w DataHolderze, zmienil sie tam tez chosenRecord
                        // Dlatego tutaj tez trzeba zaktualizowac dane
                        updateChosenStuff();
                        drawChart(barChart1,chartSpinner1.getSelectedItem().toString());
                        drawChart(barChart2,chartSpinner2.getSelectedItem().toString());
                        drawChart(barChart3,chartSpinner3.getSelectedItem().toString());

                    }
                }, mYear, mMonth, mDate);

                // Ustawiam minimalna i maksymalna date, ktore wczesniej pobralem
                datePickerDialog.getDatePicker().setMinDate(minDate);
                datePickerDialog.getDatePicker().setMaxDate(maxDate);

                // Jesli zmieniono date z najnowszej na inna, zapisze ten wybor,
                // bo inaczej gdyby zostal zmieniony kraj, data z powrotem bedzie najnowsza
                if(!(chosenEndDate.equals(chosenCountryList.get(chosenCountryList.size() - 1)[3]))) {
                    int[] parts = stringDateToInt(chosenEndDate);
                    datePickerDialog.getDatePicker().init(parts[0], parts[1], parts[2], null);
                }
                datePickerDialog.show();
            }
        });
    }

    private void updateChosenStuff() {
        chosenCountryName = DataHolder.getChosenCountryName();
        chosenCountryList = DataHolder.getChosenCountryList();
        chosenDate = DataHolder.getChosenDate();

        chosenRecord = DataHolder.getChosenRecord();
        chosenStartDate = DataHolder.isDateInChosenCountry(chosenStartDate);
        chosenEndDate = DataHolder.isDateInChosenCountry(chosenEndDate);
        dateText1.setText(chosenStartDate);
        dateText2.setText(chosenEndDate);
    }

    public long getChosenCountryMinTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateStr = chosenCountryList.get(0)[3] + " 00:00:00";
        Date date = null;
        try {
            date = sdf.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long millis = date.getTime();
        return millis;
    }

    // Funkcja pobiera najnowsza date dostepna dla obecnie wybranego kraju i zwraca ja
    // w postaci milisekund - uzywane przy zmianie daty w kalendarzu
    public long getChosenCountryMaxTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateStr = chosenCountryList.get(chosenCountryList.size() - 1)[3] + " 00:00:00";
        //String dateStr = DataHolder.getLatestInfectionDate() + " 00:00:00";
        Date date = null;
        try {
            date = sdf.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long millis = date.getTime();
        return millis;
    }

    // Funkcja do przeksztalcenia daty wybranej przez uzytkownika w kalendarzu
    // do formy Stringa
    public String calendarDateToString(int year, int month, int date) {
        String yearStr = String.valueOf(year);
        String monthStr = "";
        String dateStr = "";
        if(month < 9) {
            monthStr += "0";
        }
        month++;
        monthStr += String.valueOf(month);
        if(date < 10) {
            dateStr += "0";
        }
        dateStr += String.valueOf(date);
        return yearStr + "-" + monthStr + "-" + dateStr;
    }

    // Zamienia stringa w formacie "yyyy-MM-dd" na prosty do odczytania przez kalendarz format
    public int[] stringDateToInt(String date) {
        int[] parts = new int[3];
        parts[0] = Integer.parseInt(date.substring(0,4));
        parts[1] = Integer.parseInt(date.substring(5,7));
        parts[1]--;
        parts[2] = Integer.parseInt(date.substring(8,10));
        return parts;
    }

    // Zwraca date o 7 dni mniejsza niz podana
    private String subtractDaysFromDate(String date1Str, int numberOfDays) {
        String dt = date1Str;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date1;
        long difference = 0;
        try {
            date1 = sdf.parse(date1Str);
            difference = date1.getTime() - (numberOfDays* 24 * 60 * 60 * 1000);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        String result = new SimpleDateFormat("yyyy-MM-dd").format(new Date(difference));
        return result;
    }
}