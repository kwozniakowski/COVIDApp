package com.example.covidapp;

import android.animation.ValueAnimator;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CountryBriefFragment extends Fragment {

    Spinner spinner;
    Button statisticsActivityButton;
    TextView totalInfectionsText;
    TextView newInfectionsText;
    TextView totalDeathsText;
    TextView newDeathsText;
    TextView totalTestsText;
    TextView newTestsText;
    TextView dateText;
    PieChart infectionsChart, deathsChart;
    SwipeRefreshLayout swipeRefreshLayout;

    private int mDate, mMonth, mYear;

    // Zmienne do ktorych beda wpisywane odpowiednie dane z DataHoldera
    ArrayList<ArrayList<String[]>> listDividedByCountries;
    ArrayList<String[]> chosenCountryList;
    String chosenCountryName;
    String chosenDate;
    String[] chosenRecord;
    ArrayList<String> countryNameList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_country_brief, container, false);

        totalInfectionsText = view.findViewById(R.id.totalInfectionsText);
        newInfectionsText = view.findViewById(R.id.newInfectionsText);
        totalDeathsText = view.findViewById(R.id.totalDeathsText);
        newDeathsText = view.findViewById(R.id.newDeathsText);
        //totalTestsText = findViewById(R.id.totalTestsText);
        //newTestsText = findViewById(R.id.newTestsText);
        dateText = view.findViewById(R.id.dateButton);
        spinner = (Spinner)view.findViewById(R.id.header);
        statisticsActivityButton = view.findViewById(R.id.statisticsActivityButton);
        swipeRefreshLayout = view.findViewById(R.id.countryBriefRefresh);


        // Pobieram dane wygenerowane przez DataHoldera
        listDividedByCountries = DataHolder.getListDividedByCountries();
        countryNameList = DataHolder.getCountryNameList();

        infectionsChart = view.findViewById(R.id.infectionsChart);
        deathsChart = view.findViewById(R.id.deathsChart);
        setUpCharts();

        //DataHolder.setLatestInfectionDate();

        // Tu pobieram pozostale dane (czesto bede to robic, wiec zrobilem do tego funkcje)
        updateChosenStuff(false);

        // Spinner (dropdown-menu)
        // Przekazuje spinnerowi nazwy krajow
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, countryNameList);
        spinner.setAdapter(adapter);

        // Tu ustawiam spinnerowi nazwe kraju, ktora ma ustawic przy uruchomieniu tej aktywnosci
        // Jest to zwiazane z tym, ze MainActivity przekazuje tutaj nazwe regionu, ktory ma byc wybrany
        // (np. world lub kraj w ktorym znajduje sie uzytkownik).
        spinner.setSelection(countryNameList.indexOf(chosenCountryName));

        // Update danych jesli zostal zmieniony kraj
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                DataHolder.updateChosenCountryName(spinner.getSelectedItem().toString());
                updateChosenStuff();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // Ustawiam wszystko co potrzebne, zeby dzialal kalendarz
        // kodu jest sporo, wiec wrzucilem to wszystko do funkcji
        setUpCalendar();

        statisticsActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new StatisticsFragment();
                ((MainActivity)getActivity()).changeFragment(fragment);
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        //((MainActivity)getActivity()).deleteSomeFile("covid_data_file_info.txt");
                        DataHolder.isFragmentUpdateRequired = false;
                        ((MainActivity)getActivity()).checkForFileUpdates(false);
                        swipeRefreshLayout.setRefreshing(false);
                        synchronized (DataHolder.updateLock) {
                            try {
                                DataHolder.updateLock.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
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

    // Funkcje

    public void setTextsForCountry(TextView a,TextView b,TextView c,TextView d, TextView e, TextView f){
        dateText.setText(chosenDate);

        int infectedTotal = Integer.parseInt(chosenRecord[4]);
        startCountAnimation(a, infectedTotal, "");

        int infectedDaily = Integer.parseInt(chosenRecord[5]);
        startCountAnimation(b, infectedDaily, "+");

        int deathsTotal = Integer.parseInt(chosenRecord[7]);
        startCountAnimation(c, deathsTotal, "");

        int deathsDaily = Integer.parseInt(chosenRecord[8]);
        startCountAnimation(d, deathsDaily, "+");

        //Nasz plik nie ma danych dla nowych testow dla ostatnich dób, dlatego raczej zrezygnujemy z tego
        /*String eStr = removeFloatingPointFromString(scoreList.get(i)[24]);
        e.setText(eStr);
        String fStr = removeFloatingPointFromString(scoreList.get(i)[25]);
        f.setText("+" + fStr);*/
    }

    // Funkcja pobiera najstarsza date dostepna dla obecnie wybranego kraju i zwraca ja
    // w postaci milisekund - uzywane przy zmianie daty w kalendarzu
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

    // Funkcja odpowiadajaca za animacje liczb
    private void startCountAnimation(final TextView textView, final int finalValue, final String additionalText) {
        ValueAnimator animator = ValueAnimator.ofInt(0, finalValue);
        /*if(finalValue > 1000000) {
            animator.setDuration(3000);
        } else if(finalValue > 100000) {
            animator.setDuration(2000);
        }  else if(finalValue > 1000) {
            animator.setDuration(1000);
        } else if(finalValue > 20) {
            animator.setDuration(500);
        } else {
            animator.setDuration(100);
        }*/
        animator.setDuration(1000);
        final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.UK);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                String newText = additionalText;
                newText += numberFormat.format(animation.getAnimatedValue());
                textView.setText(newText);
            }
        });
        animator.start();
    }

    // Aktualizuje wartosci zmiennych
    public void updateChosenStuff() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                chosenCountryName = DataHolder.getChosenCountryName();
                chosenCountryList = DataHolder.getChosenCountryList();
                chosenDate = DataHolder.getChosenDate();
                chosenRecord = DataHolder.getChosenRecord();
                setTextsForCountry(totalInfectionsText,newInfectionsText,
                        totalDeathsText,newDeathsText,totalTestsText,newTestsText);
                setUpCharts();
            }
        });
    }

    public void updateChosenStuff(boolean isVisualUpdateRequired) {
        chosenCountryName = DataHolder.getChosenCountryName();
        chosenCountryList = DataHolder.getChosenCountryList();
        chosenDate = DataHolder.getChosenDate();
        chosenRecord = DataHolder.getChosenRecord();
        if(isVisualUpdateRequired) {
            setTextsForCountry(totalInfectionsText, newInfectionsText,
                    totalDeathsText, newDeathsText, totalTestsText, newTestsText);
            setUpCharts();
        }
    }

    private void setUpCalendar() {
        // Wyswietlanie kalendarza do wyboru daty
        dateText.setOnClickListener(new View.OnClickListener() {
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
                        DataHolder.updateChosenDate(newDate);
                        // Zmieniajac date w DataHolderze, zmienil sie tam tez chosenRecord
                        // Dlatego tutaj tez trzeba zaktualizowac dane
                        updateChosenStuff();

                    }
                }, mYear, mMonth, mDate);

                // Ustawiam minimalna i maksymalna date, ktore wczesniej pobralem
                datePickerDialog.getDatePicker().setMinDate(minDate);
                datePickerDialog.getDatePicker().setMaxDate(maxDate);

                // Jesli zmieniono date z najnowszej na inna, zapisze ten wybor,
                // bo inaczej gdyby zostal zmieniony kraj, data z powrotem bedzie najnowsza
                if(!(chosenDate.equals(chosenCountryList.get(chosenCountryList.size() - 1)[3]))) {
                    int[] parts = stringDateToInt(chosenDate);
                    datePickerDialog.getDatePicker().init(parts[0], parts[1], parts[2], null);
                }
                datePickerDialog.show();
            }
        });
    }
    private void setUpCharts() {
            setUpChart1();
            setUpChart2();
    }
    public void setUpChart1()
    {
        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        float infected = Float.parseFloat(DataHolder.getChosenRecord()[4]);
        float newInfected = Float.parseFloat(DataHolder.getChosenRecord()[5]);
        //float population = Float.parseFloat(chosenCountryList.get(chosenCountryList.size()-1)[39]);
        //float vaccined = Float.parseFloat(chosenCountryList.get(chosenCountryList.size()-1)[34]);
        pieEntries.add(new PieEntry(infected - newInfected,"total"));
        pieEntries.add(new PieEntry(newInfected,"new"));
        PieDataSet dataSet = new PieDataSet(pieEntries,"");
        dataSet.setColors( Color.rgb(10,10,10),Color.rgb(255,0,0));
        PieData data = new PieData(dataSet);
        infectionsChart.setData(data);
        infectionsChart.setDrawSliceText(false);
        infectionsChart.getData().setDrawValues(false);
        infectionsChart.setDrawEntryLabels(false);
        infectionsChart.getDescription().setEnabled(false);
        infectionsChart.getLegend().setEnabled(false);
        infectionsChart.animateY(1000);
    }

    public void setUpChart2()
    {
        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        //float population = Float.parseFloat(chosenCountryList.get(chosenCountryList.size()-1)[39]);
        //float vaccined = Float.parseFloat(chosenCountryList.get(chosenCountryList.size()-1)[34]);
        //float infected = Float.parseFloat(chosenCountryList.get(chosenCountryList.size()-1)[4]);
        float deaths = Float.parseFloat(DataHolder.getChosenRecord()[7]);
        float newDeaths = Float.parseFloat(DataHolder.getChosenRecord()[8]);
        pieEntries.add(new PieEntry(deaths - newDeaths,"total"));
        pieEntries.add(new PieEntry(newDeaths,"new"));
        PieDataSet dataSet = new PieDataSet(pieEntries,"");
        dataSet.setColors( Color.rgb(10,10,10),Color.rgb(255,0,0));
        PieData data = new PieData(dataSet);
        deathsChart.setData(data);
        deathsChart.setDrawSliceText(false);
        deathsChart.getData().setDrawValues(false);
        deathsChart.setDrawEntryLabels(false);
        deathsChart.getDescription().setEnabled(false);
        deathsChart.getLegend().setEnabled(false);
        deathsChart.animateY(1000);
    }

}