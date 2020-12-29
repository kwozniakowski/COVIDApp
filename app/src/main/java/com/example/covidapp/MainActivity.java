package com.example.covidapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    Button countryBriefButton;
    Button worldBriefButton;
    Button createModelButton;
    Button settingsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        countryBriefButton = (Button) findViewById(R.id.countryBriefButton);
        worldBriefButton = (Button) findViewById(R.id.worldBriefButton);
        createModelButton = (Button) findViewById(R.id.createModelButton);
        settingsButton = (Button) findViewById(R.id.settingsButton);

        checkForFileUpdates();

        InputStream inputStream = getResources().openRawResource(R.raw.covid_data);
        CSVFile csvFile = new CSVFile(inputStream);
        final ArrayList<String[]> scoreList = csvFile.read();
        DataHolder.setScoreList(scoreList);

        countryBriefButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), BriefActivity.class);
                // intent.putExtra("Region", "Poland");
                DataHolder.updateChosenCountryName("Poland");
                startActivity(intent);
            }
        });

        worldBriefButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), BriefActivity.class);
                // intent.putExtra("Region", "World");
                DataHolder.updateChosenCountryName("World");
                startActivity(intent);
            }
        });

        createModelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ModelingActivity.class);
                startActivity(intent);
            }
        });
    }

    private void checkForFileUpdates() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                // Sprawdzam jaka wersja pliku csv jest obecnie pobrana, zeby porownac ja z dostepna
                // w internecie
                String dataFromFile = readFromFile("covid_data_file_info.txt");
                System.out.println("Dane odczytane z pliku: " + dataFromFile);

                try {
                    // Laczenie sie ze strona zawierajaca informacje o zmianach w pliku csv
                    Document doc = Jsoup.connect("https://github.com/owid/covid-19-data/commits/master/public/data/owid-covid-data.csv")
                            .get();
                    // Jako odpowiedz aplikacja otrzyma zwykly dokument HTML ze wszystkimi potrzebnymi informacjami

                    // Przeszukuje otrzymany dokument, zeby znalezc informacje o najnowszych commitach
                    // Interesuje mnie glownie id commitu
                    Element latestUpdates = doc.getElementsByClass("TimelineItem-body").get(0);
                    Element latestCommits = latestUpdates.getElementsByTag("ol").get(0);
                    Element latestCommit = latestCommits.getElementsByTag("li").get(0);
                    Element buttonGroup = latestCommit.getElementsByClass("BtnGroup").get(0);
                    String latestCommitId = buttonGroup.getElementsByTag("a").get(0).text();

                    // Zapisuje rowniez informacje o dacie utworzenia tego commitu
                    // Poki co nie jest to nigdzie uzywane, ale moze sie jeszcze przydac
                    String latestUpdateDate = latestUpdates.getElementsByTag("h2").get(0).text();
                    if(latestUpdateDate.indexOf("Commits on ") == 0) {
                        latestUpdateDate = latestUpdateDate.substring(11);
                    }

                    System.out.println("Najnowsza aktualizacja: " + latestUpdateDate);
                    System.out.println("Id commitu: " + latestCommitId);

                    if(!(dataFromFile.equals(latestCommitId + "\n"))) {
                        // Tu bedzie pobieranie pliku
                        makeToast("Update available, updating...");
                        writeToFile("covid_data_file_info.txt", latestCommitId);
                    }
                    else {
                        makeToast("Everything is up to date");
                    }

                } catch (IOException e) {
                    makeToast("Cannot check for updates");
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    private String readFromFile(String fileName) {
        FileInputStream fis = null;
        StringBuilder sb = new StringBuilder();
        try {
            fis = openFileInput(fileName);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            String text = "";

            while ((text = br.readLine()) != null) {
                sb.append(text).append("\n");
            }
        } catch (FileNotFoundException e) {
            return "";
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

    private void writeToFile(String fileName, String dataToWrite) {
        FileOutputStream fos = null;
        try {
            fos = openFileOutput(fileName, MODE_PRIVATE);
            fos.write(dataToWrite.getBytes());
            makeToast("Data updated!");
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

    private void makeToast(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
            }
        });
    }
}