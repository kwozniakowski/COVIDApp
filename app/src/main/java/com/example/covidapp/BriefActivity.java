package com.example.covidapp;

import android.animation.ValueAnimator;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class BriefActivity extends AppCompatActivity {

    Spinner spinner;
    Button statisticsActivityButton;
    TextView totalInfectionsText;
    TextView newInfectionsText;
    TextView totalDeathsText;
    TextView newDeathsText;
    TextView totalTestsText;
    TextView newTestsText;
    TextView dateText;

    private int mDate, mMonth, mYear;

    // Zmienne do ktorych beda wpisywane odpowiednie dane z DataHoldera
    ArrayList<ArrayList<String[]>> listDividedByCountries;
    ArrayList<String[]> chosenCountryList;
    String chosenCountryName;
    String chosenDate;
    String[] chosenRecord;
    ArrayList<String> countryNameList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brief);

        totalInfectionsText = findViewById(R.id.totalInfectionsText);
        newInfectionsText = findViewById(R.id.newInfectionsText);
        totalDeathsText = findViewById(R.id.totalDeathsText);
        newDeathsText = findViewById(R.id.newDeathsText);
        //totalTestsText = findViewById(R.id.totalTestsText);
        //newTestsText = findViewById(R.id.newTestsText);
        dateText = findViewById(R.id.dateButton);
        spinner = (Spinner)findViewById(R.id.countrySpinner);
        statisticsActivityButton = findViewById(R.id.statisticsActivityButton);



        // Pobieram dane wygenerowane przez DataHoldera
        listDividedByCountries = DataHolder.getListDividedByCountries();
        countryNameList = DataHolder.getCountryNameList();

        DataHolder.setLatestInfectionDate();

        // Tu pobieram pozostale dane (czesto bede to robic, wiec zrobilem do tego funkcje)
        updateChosenStuff();

        // Spinner (dropdown-menu)
        // Przekazuje spinnerowi nazwy krajow
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, countryNameList);
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
                Intent intent = new Intent(getApplicationContext(), StatisticsActivity.class);
                startActivity(intent);
            }
        });
    }

    // To sie wykona jestli np uzytkownik wejdzie w statystyki, a potem sie cofnie
    // Odswiezam po prostu dane na wypadek, gdyby uzytkownik np zmienil kraj w aktywnosci
    // ze statystykami
    @Override
    protected void onRestart() {
        super.onRestart();
        updateChosenStuff();
        spinner.setSelection(countryNameList.indexOf(chosenCountryName));
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
        //String dateStr = chosenCountryList.get(chosenCountryList.size() - 1)[3] + " 00:00:00";
        String dateStr = DataHolder.getLatestInfectionDate() + " 00:00:00";
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
        if(finalValue > 1000000) {
            animator.setDuration(3000);
        } else if(finalValue > 100000) {
            animator.setDuration(2000);
        }  else if(finalValue > 1000) {
            animator.setDuration(1000);
        } else if(finalValue > 20) {
            animator.setDuration(500);
        } else {
            animator.setDuration(100);
        }
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
    private void updateChosenStuff() {
        chosenCountryName = DataHolder.getChosenCountryName();
        chosenCountryList = DataHolder.getChosenCountryList();
        chosenDate = DataHolder.getChosenDate();
        chosenRecord = DataHolder.getChosenRecord();
        setTextsForCountry(totalInfectionsText,newInfectionsText,
                totalDeathsText,newDeathsText,totalTestsText,newTestsText);
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

                DatePickerDialog datePickerDialog = new DatePickerDialog(BriefActivity.this, android.R.style.Theme_DeviceDefault_Dialog, new DatePickerDialog.OnDateSetListener() {
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
}