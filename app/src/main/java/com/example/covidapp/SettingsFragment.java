package com.example.covidapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;


public class SettingsFragment extends Fragment {

    ArrayList<String> countryNameList;
    Spinner countrySettingSpinner;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        countrySettingSpinner = (Spinner)view.findViewById(R.id.countrySettingSpinner);

        countryNameList = (ArrayList<String>)DataHolder.getCountryNameList().clone();
        countryNameList.remove(0);

        // Spinner (dropdown-menu)
        // Przekazuje spinnerowi nazwy krajow
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_item, countryNameList);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown);
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

        return view;
    }

    private void writeToFile(String fileName, String dataToWrite) {
        FileOutputStream fos = null;
        try {
            fos = getActivity().openFileOutput(fileName, getActivity().MODE_PRIVATE);
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