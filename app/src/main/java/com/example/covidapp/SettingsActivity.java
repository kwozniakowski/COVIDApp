package com.example.covidapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class SettingsActivity extends AppCompatActivity {

    ArrayList<String> countryNameList;
    Spinner countrySettingSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        countrySettingSpinner = (Spinner)findViewById(R.id.countrySettingSpinner);

        countryNameList = (ArrayList<String>)DataHolder.getCountryNameList().clone();
        countryNameList.remove(0);

        // Spinner (dropdown-menu)
        // Przekazuje spinnerowi nazwy krajow
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, countryNameList);
        countrySettingSpinner.setAdapter(adapter);

        System.out.println("Wybrany nr indeksu: " + countryNameList.indexOf(DataHolder.getDefaultCountryName()));
        countrySettingSpinner.setSelection(countryNameList.indexOf(DataHolder.getDefaultCountryName()));

        countrySettingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                DataHolder.updateDefaultCountryName(countrySettingSpinner.getSelectedItem().toString());
                writeToFile("settings.txt", countrySettingSpinner.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void writeToFile(String fileName, String dataToWrite) {
        FileOutputStream fos = null;
        try {
            fos = openFileOutput(fileName, MODE_PRIVATE);
            fos.write(dataToWrite.getBytes());
            System.out.println("Finished writing to " + fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}