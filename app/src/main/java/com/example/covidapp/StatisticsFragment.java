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

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.MPPointF;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class StatisticsFragment extends Fragment {

    int DATE, NEW_INFECTIONS, NEW_DEATHS, NEW_TESTS, TOTAL_INFECTIONS_PER_MILLION;
    int TOTAL_DEATHS_PER_MILLION;

    Spinner countrySpinner;
    Spinner chartSpinner1, chartSpinner2;
    ArrayList<ArrayList<String[]>> listDividedByCountries;
    ArrayList<String[]> chosenCountryList;
    String chosenCountryName;
    ArrayList<String> countryNameList;
    CombinedChart combinedChart;
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

        DATE = DataHolder.DATE;
        NEW_INFECTIONS = DataHolder.NEW_CASES;
        NEW_DEATHS = DataHolder.NEW_DEATHS;
        NEW_TESTS = DataHolder.NEW_TESTS;
        TOTAL_INFECTIONS_PER_MILLION = DataHolder.TOTAL_CASES_PER_MILLION;
        TOTAL_DEATHS_PER_MILLION = DataHolder.TOTAL_DEATHS_PER_MILLION;

        listDividedByCountries = DataHolder.getListDividedByCountries();
        countryNameList = DataHolder.getCountryNameList();
        final ArrayList<String> countryNameList = DataHolder.getCountryNameList();
        chosenCountryName = DataHolder.getChosenCountryName();

        //spinnery
        countrySpinner = view.findViewById(R.id.header);
        chartSpinner1 = view.findViewById(R.id.chartSpinner1);
        chartSpinner2 = view.findViewById(R.id.chartSpinner2);

        swipeRefreshLayout = view.findViewById(R.id.statisticsRefresh);

        //wykresy
        combinedChart = view.findViewById(R.id.combinedChart);

        //text kalendarza
        dateText1 = view.findViewById(R.id.dateText1);
        dateText2 = view.findViewById(R.id.dateText2);

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
                drawChart(combinedChart);
                chartSpinner1.setSelection(0);
                chartSpinner2.setSelection(1);
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
                drawChart(combinedChart);
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
                drawChart(combinedChart);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //Funkcja odpowiedzialna za rysowanie wykresu
        drawChart(combinedChart);

        //Poniższe nie działa, trzeba zrobić żeby po naciśnięciu na słupek pokazywała się dokładna wartość z datą
        combinedChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            protected RectF mOnValueSelectedRectF = new RectF();
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                if (e == null)
                    return;

                RectF bounds = mOnValueSelectedRectF;
                System.out.println(chosenCountryList.get((int)h.getX())[DATE]);
                combinedChart.getClipBounds();//getBarBounds((BarEntry) e, bounds);
                MPPointF position = combinedChart.getPosition(e, YAxis.AxisDependency.LEFT);

                System.out.println("bounds"+ bounds.toString());
                System.out.println("position"+ position.toString());

                System.out.println("x-index" +
                        "low: " + combinedChart.getLowestVisibleX() + ", high: "
                        + combinedChart.getHighestVisibleX());

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
            list.add(new BarEntry(i, Integer.parseInt(chosenCountryList.get(i)[NEW_INFECTIONS] )));
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
            list.add(new BarEntry(i, Integer.parseInt(chosenCountryList.get(i)[NEW_DEATHS] )));
        }
        return list;
    }

    private void drawChart(CombinedChart chart)
    {
        String label1 = chartSpinner1.getSelectedItem().toString();
        String label2 = chartSpinner2.getSelectedItem().toString();
        BarDataSet barDataSet;
        LineDataSet lineDataSet;
        CombinedData combinedData = new CombinedData();

        barDataSet = new BarDataSet(barDataValues(label1),label1);
        barDataSet.setColor(Color.rgb(204,204,204));
        if(label1.equals("nothing")) barDataSet.setVisible(false);
        barDataSet.setAxisDependency(chart.getAxisLeft().getAxisDependency());
        BarData barData = new BarData(barDataSet);
        barData.setDrawValues(false);
        combinedData.setData(barData);

        lineDataSet = new LineDataSet(lineDataValues(label2),label2);
        lineDataSet.setColor(Color.rgb(255,50,50));
        lineDataSet.setCircleColor(Color.rgb(255,50,50));
        if(label2.equals("nothing")) lineDataSet.setVisible(false);
        lineDataSet.setAxisDependency(chart.getAxisRight().getAxisDependency());
        LineData lineData = new LineData(lineDataSet);
        lineData.setDrawValues(false);

        combinedData.setData(lineData);
        MyMarkerView mv = new MyMarkerView (getContext(), R.layout.my_marker_view_layout);
        mv.setChartView(chart);
        chart.setMarkerView(mv);

        chart.animateY(1000);
        chart.getAxisLeft().setDrawGridLines(false);
        chart.getAxisRight().setDrawGridLines(false);
        chart.getAxisRight().setGranularity(10f);
        chart.getAxisLeft().setGranularity(1f);
        chart.getAxisLeft().setAxisMinimum(0f);

        chart.getXAxis().setDrawGridLines(false);
        chart.setAutoScaleMinMaxEnabled(true);
        chart.getLegend().setEnabled(false);
        chart.getDescription().setEnabled(false);
        chart.getXAxis().setEnabled(false);
        chart.getAxisLeft().setEnabled(true);
        chart.getAxisLeft().setTextColor(Color.rgb(204,204,204));
        chart.getAxisRight().setTextColor(Color.rgb(255,50,50));

        if(!label1.equals("day to day % growth"))
        {
            chart.getAxisLeft().setAxisMinimum(0);
        }
        else
        {
            chart.getAxisLeft().resetAxisMinimum();
        }
        if(!label2.equals("day to day % growth"))
        {
            chart.getAxisRight().setAxisMinimum(0);
        }
        else
        {
            chart.getAxisRight().resetAxisMinimum();
        }

        chart.setTouchEnabled(true);
        chart.setHighlightPerTapEnabled(true);
        chart.setData(combinedData);

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
        statisticalData.add("nothing");
    }

    private ArrayList<BarEntry> barDataValues(String statisticalData) {
        ArrayList<String[]> chosenCountryList = DataHolder.getChosenCountryList();
        ArrayList<BarEntry> list = new ArrayList<>();
        int startIndex = chosenCountryList.size()-2;
        int endIndex = chosenCountryList.size()-1;

        //Tutaj wyswietlimy sobie zakazenia dla ostatnigo tygodnia
        for (int i = 0; i <= chosenCountryList.size() - 1; i++)
        {
            if(chosenCountryList.get(i)[DATE].equals(chosenEndDate)) endIndex = i;
            if(chosenCountryList.get(i)[DATE].equals(chosenStartDate)) startIndex = i;
        }

        for(int i = startIndex; i <= endIndex ; i ++)
        {
            boolean [] barDataList; //Przechowuje BOOLEAN, ktore wskazuja czy do tekstu ma byc dodany kolejno "%" oraz
            //czy maja byc miejsca po przecinku w wyniku
            if(statisticalData == "new infections")
            {
                barDataList = new boolean[]{false,false};
                list.add(new BarEntry(i, Integer.parseInt(chosenCountryList.get(i)[NEW_INFECTIONS] ), barDataList));
            }
            else if(statisticalData == "new deaths")
            {
                barDataList = new boolean[]{false,false};
                list.add(new BarEntry(i, Integer.parseInt(chosenCountryList.get(i)[NEW_DEATHS] ), barDataList));
            }
            else if(statisticalData == "new tests")
            {
                barDataList = new boolean[]{false,false};
                if(!chosenCountryList.get(i)[NEW_TESTS].equals(""))
                {
                    list.add(new BarEntry(i, Float.parseFloat(chosenCountryList.get(i)[NEW_TESTS] ),barDataList));
                }
                else
                {
                    list.add(new BarEntry(i, 0 ,barDataList));
                }
            }
            else if(statisticalData == "total infections per 1 mln")
            {
                barDataList = new boolean[]{false,true};
                try
                {
                    list.add(new BarEntry(i, Float.parseFloat(chosenCountryList.get(i)[TOTAL_INFECTIONS_PER_MILLION]),barDataList));
                }
                catch (Exception e)
                {
                    list.add(new BarEntry(i, 0,barDataList));
                }
            }
            else if(statisticalData == "total deaths per 1 mln")
            {
                barDataList = new boolean[]{false,true};
                list.add(new BarEntry(i, Float.parseFloat(chosenCountryList.get(i)[TOTAL_DEATHS_PER_MILLION] ),barDataList));
            }
            else if(statisticalData == "% of positive tests")
            {
                barDataList = new boolean[]{true,true};
                if(!chosenCountryList.get(i)[NEW_TESTS].equals(""))
                {
                    list.add(new BarEntry(i, (Float) Float.parseFloat(chosenCountryList.get(i)[NEW_INFECTIONS] ) /
                            Float.parseFloat(chosenCountryList.get(i)[NEW_TESTS] ) * 100 ,barDataList ));//lub 26
                }
                else
                {
                    list.add(new BarEntry(i,0,barDataList));
                }
            }
            else if(statisticalData == "day to day % growth")
            {
                barDataList = new boolean[]{true,true};
                try {
                    list.add(new BarEntry(i, (Float.parseFloat(chosenCountryList.get(i-1)[NEW_INFECTIONS]) /
                            Integer.parseInt(chosenCountryList.get(i)[NEW_INFECTIONS] ) - 1 )* 100, barDataList));
                }
                catch (Exception e)
                {
                    list.add(new BarEntry(i,0,barDataList));
                }
            }
            else if(statisticalData == "death rate")
            {
                barDataList = new boolean[]{false,true};
                try{
                    list.add(new BarEntry(i,(Float.parseFloat(chosenCountryList.get(i)[9]) /
                            Float.parseFloat(chosenCountryList.get(i)[6]) * 100),barDataList));
                }
                catch (Exception e)
                {
                    list.add(new BarEntry(i,0,barDataList));
                }

            }
            else if(statisticalData == "nothing")
            {
                barDataList = new boolean[]{false,false};
                list.add(new BarEntry(i,0,barDataList));
            }
            else
            {
                barDataList = new boolean[]{false,false};
                list.add(new BarEntry(i, Integer.parseInt(chosenCountryList.get(i)[5] ),barDataList));
            }
        }
        return list;
    }

    private ArrayList<Entry> lineDataValues(String statisticalData) {
        ArrayList<String[]> chosenCountryList = DataHolder.getChosenCountryList();
        ArrayList<Entry> list = new ArrayList<>();
        int startIndex = chosenCountryList.size()-2;
        int endIndex = chosenCountryList.size()-1;

        //Tutaj wyswietlimy sobie zakazenia dla ostatnigo tygodnia
        for (int i = 0; i <= chosenCountryList.size() - 1; i++)
        {
            if(chosenCountryList.get(i)[DATE].equals(chosenEndDate)) endIndex = i;
            if(chosenCountryList.get(i)[DATE].equals(chosenStartDate)) startIndex = i;
        }

        for(int i = startIndex; i <= endIndex ; i ++)
        {
            boolean [] lineDataList; //Przechowuje BOOLEAN, ktore wskazuja czy do tekstu ma byc dodany kolejno "%" oraz
            //czy maja byc miejsca po przecinku w wyniku
            if(statisticalData == "new infections")
            {
                lineDataList = new boolean[]{false,false};
                list.add(new Entry(i, Integer.parseInt(chosenCountryList.get(i)[NEW_INFECTIONS] ), lineDataList));
            }
            else if(statisticalData == "new deaths")
            {
                lineDataList = new boolean[]{false,false};
                list.add(new Entry(i, Integer.parseInt(chosenCountryList.get(i)[NEW_DEATHS] ), lineDataList));
            }
            else if(statisticalData == "new tests")
            {
                lineDataList = new boolean[]{false,false};
                if(!chosenCountryList.get(i)[NEW_TESTS].equals(""))
                {
                    list.add(new Entry(i, Float.parseFloat(chosenCountryList.get(i)[NEW_TESTS] ),lineDataList));
                }
                else
                {
                    list.add(new Entry(i, 0 ,lineDataList));
                }
            }
            else if(statisticalData == "total infections per 1 mln")
            {
                lineDataList = new boolean[]{false,true};
                try
                {
                    list.add(new Entry(i, Float.parseFloat(chosenCountryList.get(i)[TOTAL_INFECTIONS_PER_MILLION]),lineDataList));
                }
                catch (Exception e)
                {
                    list.add(new Entry(i, 0,lineDataList));
                }
            }
            else if(statisticalData == "total deaths per 1 mln")
            {
                lineDataList = new boolean[]{false,true};
                list.add(new Entry(i, Float.parseFloat(chosenCountryList.get(i)[TOTAL_DEATHS_PER_MILLION] ),lineDataList));
            }
            else if(statisticalData == "% of positive tests")
            {
                lineDataList = new boolean[]{true,true};
                if(!chosenCountryList.get(i)[NEW_TESTS].equals(""))
                {
                    list.add(new Entry(i, (Float) Float.parseFloat(chosenCountryList.get(i)[NEW_INFECTIONS] ) /
                            Float.parseFloat(chosenCountryList.get(i)[NEW_TESTS] ) * 100 ,lineDataList ));//lub 26
                }
                else
                {
                    list.add(new Entry(i,0,lineDataList));
                }
            }
            else if(statisticalData == "day to day % growth")
            {
                lineDataList = new boolean[]{true,true};
                try {
                    list.add(new Entry(i, (Float.parseFloat(chosenCountryList.get(i-1)[NEW_INFECTIONS]) /
                            Integer.parseInt(chosenCountryList.get(i)[NEW_INFECTIONS] ) - 1 )* 100, lineDataList));
                }
                catch (Exception e)
                {
                    list.add(new Entry(i,0,lineDataList));
                }
            }
            else if(statisticalData == "death rate")
            {
                lineDataList = new boolean[]{false,true};
                try{
                    list.add(new Entry(i,(Float.parseFloat(chosenCountryList.get(i)[9]) /
                            Float.parseFloat(chosenCountryList.get(i)[6]) * 100),lineDataList));
                }
                catch (Exception e)
                {
                    list.add(new Entry(i,0,lineDataList));
                }

            }
            else if(statisticalData == "nothing")
            {
                lineDataList = new boolean[]{false,false};
                list.add(new Entry(i,0,lineDataList));
            }
            else
            {
                lineDataList = new boolean[]{false,false};
                list.add(new Entry(i, Integer.parseInt(chosenCountryList.get(i)[5] ),lineDataList));
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
                        drawChart(combinedChart);
                        //drawChart(barChart2,chartSpinner2.getSelectedItem().toString());
                        //drawChart(barChart3,chartSpinner3.getSelectedItem().toString());

                    }
                }, mYear, mMonth, mDate);

                // Ustawiam minimalna i maksymalna date, ktore wczesniej pobralem
                datePickerDialog.getDatePicker().setMinDate(minDate);
                datePickerDialog.getDatePicker().setMaxDate(maxDate);

                // Jesli zmieniono date z najnowszej na inna, zapisze ten wybor,
                // bo inaczej gdyby zostal zmieniony kraj, data z powrotem bedzie najnowsza
                if(!(chosenStartDate.equals(chosenCountryList.get(chosenCountryList.size() - 1)[DATE]))) {
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
                        drawChart(combinedChart);
                        //drawChart(barChart2,chartSpinner2.getSelectedItem().toString());
                        //drawChart(barChart3,chartSpinner3.getSelectedItem().toString());

                    }
                }, mYear, mMonth, mDate);

                // Ustawiam minimalna i maksymalna date, ktore wczesniej pobralem
                datePickerDialog.getDatePicker().setMinDate(minDate);
                datePickerDialog.getDatePicker().setMaxDate(maxDate);

                // Jesli zmieniono date z najnowszej na inna, zapisze ten wybor,
                // bo inaczej gdyby zostal zmieniony kraj, data z powrotem bedzie najnowsza
                if(!(chosenEndDate.equals(chosenCountryList.get(chosenCountryList.size() - 1)[DATE]))) {
                    int[] parts = stringDateToInt(chosenEndDate);
                    datePickerDialog.getDatePicker().init(parts[0], parts[1], parts[2], null);
                }
                datePickerDialog.show();
            }
        });
    }

    private void updateChosenStuff() {
        DATE = DataHolder.DATE;
        NEW_INFECTIONS = DataHolder.NEW_CASES;
        NEW_DEATHS = DataHolder.NEW_DEATHS;
        NEW_TESTS = DataHolder.NEW_TESTS;
        TOTAL_INFECTIONS_PER_MILLION = DataHolder.TOTAL_CASES_PER_MILLION;
        TOTAL_DEATHS_PER_MILLION = DataHolder.TOTAL_DEATHS_PER_MILLION;

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
        String dateStr = chosenCountryList.get(0)[DATE] + " 00:00:00";
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
        String dateStr = chosenCountryList.get(chosenCountryList.size() - 1)[DATE] + " 00:00:00";
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