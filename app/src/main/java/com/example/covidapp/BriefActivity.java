package com.example.covidapp;

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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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

    ArrayList<ArrayList<String[]>> listDividedByCountries;
    ArrayList<String[]> chosenCountryList;
    String chosenCountryName;
    String chosenDate;
    String[] chosenRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brief);

        totalInfectionsText = findViewById(R.id.totalInfectionsText);
        newInfectionsText = findViewById(R.id.newInfectionsText);
        totalDeathsText = findViewById(R.id.totalDeathsText);
        newDeathsText = findViewById(R.id.newDeathsText);
        totalTestsText = findViewById(R.id.totalTestsText);
        newTestsText = findViewById(R.id.newTestsText);
        dateText = findViewById(R.id.textView4);
        spinner = (Spinner)findViewById(R.id.countrySpinner);
        statisticsActivityButton = findViewById(R.id.statisticsActivityButton);
        statisticsActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), StatisticsActivity.class);
                intent.putExtra("Region", spinner.getSelectedItem().toString());
                startActivity(intent);
            }
        });

        Intent intent = getIntent();
        chosenCountryName = intent.getStringExtra("Region");
        
        final ArrayList<String[]> scoreList = DataHolder.getScoreList();

        listDividedByCountries = DataHolder.getListDividedByCountries();
        ArrayList<String> countries = DataHolder.getCountryNameList();
        chosenCountryList = DataHolder.getChosenCountryList(chosenCountryName);
        chosenDate = DataHolder.getChosenDate();
        chosenRecord = DataHolder.getChosenRecord();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, countries);
        spinner.setAdapter(adapter);

        if(countries.indexOf(chosenCountryName) >= 0) {
            spinner.setSelection(countries.indexOf(chosenCountryName));
        }

        setTextsForCountry(totalInfectionsText,newInfectionsText,
                totalDeathsText,newDeathsText,totalTestsText,newTestsText);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                chosenCountryName = spinner.getSelectedItem().toString();
                chosenCountryList = DataHolder.getChosenCountryList(chosenCountryName);
                chosenDate = DataHolder.getChosenDate();
                chosenRecord = DataHolder.getChosenRecord();
                setTextsForCountry(totalInfectionsText,newInfectionsText,
                        totalDeathsText,newDeathsText,totalTestsText,newTestsText);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        dateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar calendar = Calendar.getInstance();
                mDate = calendar.get(Calendar.DATE);
                mMonth = calendar.get(Calendar.MONTH);
                mYear = calendar.get(Calendar.YEAR);
                DatePickerDialog datePickerDialog = new DatePickerDialog(BriefActivity.this, android.R.style.Theme_DeviceDefault_Dialog, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int date) {
                        String yearStr = String.valueOf(year);
                        String monthStr = "";
                        String dateStr = "";
                        if(month < 10) {
                            monthStr += "0";
                        }
                        month++;
                        monthStr += String.valueOf(month);
                        if(date < 10) {
                            dateStr += "0";
                        }
                        dateStr += String.valueOf(date);
                        String newDate = yearStr + "-" + monthStr + "-" + dateStr;
                        DataHolder.updateChosenDate(newDate);
                        DataHolder.updateChosenRecord();
                        chosenDate = DataHolder.getChosenDate();
                        chosenRecord = DataHolder.getChosenRecord();
                        setTextsForCountry(totalInfectionsText,newInfectionsText,
                                totalDeathsText,newDeathsText,totalTestsText,newTestsText);
                    }
                }, mYear, mMonth, mDate);
                datePickerDialog.show();
            }
        });
    }

    public void setTextsForCountry(TextView a,TextView b,TextView c,TextView d, TextView e, TextView f){
        dateText.setText(chosenDate);

        // String aStr = removeFloatingPointFromString(latestRecord[4]);
        a.setText(chosenRecord[4]);
        // String bStr = removeFloatingPointFromString(latestRecord[5]);
        b.setText("+" + chosenRecord[5]);
        // String cStr = removeFloatingPointFromString(latestRecord[7]);
        c.setText(chosenRecord[7]);
        // String dStr = removeFloatingPointFromString(latestRecord[8]);
        d.setText("+" + chosenRecord[8]);
        //Nasz plik nie ma danych dla nowych testow dla ostatnich dób, dlatego raczej zrezygnujemy z tego
        /*String eStr = removeFloatingPointFromString(scoreList.get(i)[24]);
        e.setText(eStr);
        String fStr = removeFloatingPointFromString(scoreList.get(i)[25]);
        f.setText("+" + fStr);*/
    }

    public String removeFloatingPointFromString(String text) {
        if(text.indexOf(".") >= 0) {
            text = text.substring(0, text.indexOf("."));
        }
        return text;
    }
}