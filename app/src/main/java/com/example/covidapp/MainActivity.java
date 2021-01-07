package com.example.covidapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {

    Button countryBriefButton;
    Button worldBriefButton;
    Button vaccinationsButton;
    Button settingsButton;
    Button resetButton;

    boolean isCsvFileEmpty;
    String latestCommitId;
    String txtFilename, csvFilename;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        countryBriefButton = (Button) findViewById(R.id.countryBriefButton);
        worldBriefButton = (Button) findViewById(R.id.worldBriefButton);
        vaccinationsButton = (Button) findViewById(R.id.vaccinationsButton);
        settingsButton = (Button) findViewById(R.id.settingsButton);
        resetButton = (Button) findViewById(R.id.resetButton);

        txtFilename = "covid_data_file_info.txt";
        csvFilename = "covid_data.csv";

        intent = null;

        loadSettings();
        checkForFileUpdates();

        //InputStream inputStream = getResources().openRawResource(R.raw.covid_data);
        loadDataFromCsvFile();

        countryBriefButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent = new Intent(getApplicationContext(), BriefActivity.class);
                // intent.putExtra("Region", "Poland");
                DataHolder.updateChosenCountryName(DataHolder.getDefaultCountryName());
                //startActivity(intent);
                checkForFileUpdates();
            }
        });

        worldBriefButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent = new Intent(getApplicationContext(), BriefActivity.class);
                // intent.putExtra("Region", "World");
                DataHolder.updateChosenCountryName("World");
                //startActivity(intent);
                checkForFileUpdates();
            }
        });

        vaccinationsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent = new Intent(getApplicationContext(), VaccinationsActivity.class);
                DataHolder.updateChosenCountryName(DataHolder.getDefaultCountryName());
                //startActivity(intent);
                checkForFileUpdates();
            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent = new Intent(getApplicationContext(), SettingsActivity.class);
                //DataHolder.updateChosenCountryName(DataHolder.getDefaultCountryName());
                //startActivity(intent);
                checkForFileUpdates();
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //writeToFile(txtFilename, "");
                //writeToFile(csvFilename, "");
                //writeToFile("settings.txt", "");
                if(deleteSomeFile(txtFilename)) {
                    System.out.println("txt file deleted");
                } else {
                    System.out.println("Cannot delete txt file");
                }

                if(deleteSomeFile(csvFilename)) {
                    System.out.println("csv file deleted");
                } else {
                    System.out.println("Cannot delete csv file");
                }

                if(deleteSomeFile("settings.txt")) {
                    System.out.println("Settings deleted");
                } else {
                    System.out.println("Cannot delete settings file");
                }
                makeToast("Data cleared");
            }
        });
    }

    private void checkForFileUpdates() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                setButtonsClickableProperty(false);
                // Sprawdzam jaka wersja pliku csv jest obecnie pobrana, zeby porownac ja z dostepna
                // w internecie
                String dataFromFile = readFromFile(txtFilename);
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
                    latestCommitId = buttonGroup.getElementsByTag("a").get(0).text();

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
                        downloadCsvFile();
                    }
                    else {
                        makeToast("Everything is up to date");
                        setButtonsClickableProperty(true);
                        if(!isFileEmpty(csvFilename) && intent != null) {
                            startActivity(intent);
                        }
                    }

                } catch (IOException e) {
                    makeToast("Cannot check for updates. Please, check your internet connection");
                    setButtonsClickableProperty(true);
                    if(!isFileEmpty(csvFilename) && intent != null) {
                        startActivity(intent);
                    }
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
            writeToFile(fileName, "");
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

    private boolean deleteSomeFile(String fileName) {
        File dir = getFilesDir();
        File file = new File(dir, fileName);
        boolean deleted = file.delete();
        return deleted;
    }

    private boolean isFileEmpty(String fileName) {
        FileInputStream fis = null;
        StringBuilder sb = new StringBuilder();
        try {
            fis = openFileInput(fileName);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            String text = "";

            if ((text = br.readLine()) != null) {
                sb.append(text).append("\n");
            }
            if (sb.toString().isEmpty()) {
                return true;
            }
        } catch (FileNotFoundException e) {
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

        private void makeToast(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setButtonsClickableProperty(final boolean value) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                countryBriefButton.setClickable(value);
                worldBriefButton.setClickable(value);
            }
        });
    }

    private void downloadCsvFile() {
        OkHttpClient client = new OkHttpClient();
        String url = "https://raw.githubusercontent.com/owid/covid-19-data/master/public/data/owid-covid-data.csv";
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                makeToast("Cannot update data");
                setButtonsClickableProperty(true);
                if(!isFileEmpty(csvFilename) && intent != null) {
                    startActivity(intent);
                }
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if(response.isSuccessful()) {
                    String responseStr = response.body().string();
                    writeToFile(csvFilename, responseStr);
                    writeToFile(txtFilename, latestCommitId);
                    loadDataFromCsvFile();
                    makeToast("Data updated!");
                    setButtonsClickableProperty(true);
                    if(!isFileEmpty(csvFilename) && intent != null) {
                        startActivity(intent);
                    }
                }
            }
        });
    }

    private void loadDataFromCsvFile() {
        InputStream inputStream = null;
        try {
            inputStream = openFileInput(csvFilename);
        } catch (FileNotFoundException e) {
            writeToFile(csvFilename, "");
            try {
                inputStream = openFileInput(csvFilename);
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        }
        //InputStream inputStream = getResources().openRawResource(R.raw.covid_data);
        CSVFile csvFile = new CSVFile(inputStream);
        final ArrayList<String[]> scoreList = csvFile.read();
        DataHolder.setScoreList(scoreList);
    }

    private void loadSettings() {
        String settings = readFromFile("settings.txt");
        if(settings.indexOf('\n') >= 0) {
            settings = settings.substring(0, settings.indexOf('\n'));
        }
        if(settings.isEmpty()) {
            writeToFile("settings.txt", "Poland");
            DataHolder.updateDefaultCountryName("Poland");
        } else {
            DataHolder.updateDefaultCountryName(settings);
            System.out.println("Odczytane dane z pliku settings.txt: " + settings);
        }
    }
}