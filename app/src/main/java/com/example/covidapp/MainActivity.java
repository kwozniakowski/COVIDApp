package com.example.covidapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

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

    BottomNavigationView bottomNavigationView;

    boolean isDownloadSuccessful, isCountryChangeRequired, isUpdateInProgress;
    String latestCommitId;
    String txtFilename, csvFilename;
    Fragment selectedFragment;

    Object downloadLock, mainUpdateLock, connectionLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        txtFilename = "covid_data_file_info.txt";
        csvFilename = "covid_data.csv";

        selectedFragment = null;
        isCountryChangeRequired = true;
        isUpdateInProgress = false;
        selectedFragment = null;

        deleteSomeFile(csvFilename);
        deleteSomeFile(txtFilename);

        downloadLock = new Object();
        mainUpdateLock = new Object();
        connectionLock = new Object();

        loadSettings();

        //InputStream inputStream = getResources().openRawResource(R.raw.covid_data);
        if(!isFileEmpty(csvFilename)) {
            loadDataFromCsvFile();
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                selectedFragment = null;

                switch (item.getItemId()) {
                    case R.id.nav_country_brief:
                        if(isCountryChangeRequired) {
                            DataHolder.updateChosenCountryName(DataHolder.getDefaultCountryName());
                        }
                        isCountryChangeRequired = true;
                        selectedFragment = new CountryBriefFragment();
                        break;
                    case R.id.nav_world_brief:
                        DataHolder.updateChosenCountryName("World");
                        selectedFragment = new WorldBriefFragment();
                        break;
                    case R.id.nav_vaccinations:
                        DataHolder.updateChosenCountryName(DataHolder.getDefaultCountryName());
                        selectedFragment = new VaccinationsFragment();
                        break;
                    case R.id.nav_settings:
                        selectedFragment = new SettingsFragment();
                        break;
                }

                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        if(isUpdateInProgress) {
                            synchronized (DataHolder.updateLock) {
                                try {
                                    DataHolder.updateLock.wait();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        changeFragment(selectedFragment);
                    }
                };
                Thread thread = new Thread(runnable);
                thread.start();
                return true;
            }
        });

        checkForFileUpdates(true);
        bottomNavigationView.setSelectedItemId(R.id.nav_country_brief);
    }

    public void checkForFileUpdates(final boolean isLoadingScreenRequired) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                isUpdateInProgress = true;
                if(isLoadingScreenRequired) {
                    Fragment loadingFragment = new LoadingFragment();
                    changeFragment(loadingFragment);
                }

                isDownloadSuccessful = false;

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
                        makeToast("Update available, updating...");
                        downloadCsvFile();
                        // Czekam na pobranie pliku
                        synchronized (downloadLock) {
                            try { downloadLock.wait(); }
                            catch (InterruptedException e) { e.printStackTrace(); }
                        }

                        if(isDownloadSuccessful) {
                            loadDataFromCsvFile();
                            writeToFile(txtFilename, latestCommitId);
                            makeToast("Data updated!");
                            DataHolder.isFragmentUpdateRequired = true;
                        } else {
                            makeToast("Cannot update data");
                        }
                    }
                    else {
                        makeToast("Everything is up to date");
                    }

                } catch (IOException e) {
                    makeToast("Cannot check for updates. Please, check your internet connection");
                    if(isFileEmpty(csvFilename)) {
                        waitForConnection(isLoadingScreenRequired);
                    }
                } finally {
                    if(DataHolder.isScoreListReady) {
                        isUpdateInProgress = false;
                        synchronized (DataHolder.updateLock) {
                            DataHolder.updateLock.notify();
                        }
                    }
                    /*if(!isFileEmpty(csvFilename) && newFragment != null) {
                        changeFragment(newFragment);
                    }*/
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    private void waitForConnection(boolean isLoadingScreenRequired) {
        try {
            Document doc = Jsoup.connect("https://github.com/owid/covid-19-data/commits/master/public/data/owid-covid-data.csv")
                    .get();
            checkForFileUpdates(isLoadingScreenRequired);
        } catch (IOException e) {
            try {
                Thread.sleep(2000);
                waitForConnection(isLoadingScreenRequired);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
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

    public boolean deleteSomeFile(String fileName) {
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
                sb.append(text);
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

        public void makeToast(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 600);
                toast.show();
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
                synchronized (downloadLock) {
                    downloadLock.notify();
                }
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if(response.isSuccessful()) {
                    String responseStr = response.body().string();
                    writeToFile(csvFilename, responseStr);
                    isDownloadSuccessful = true;
                    synchronized (downloadLock) {
                        downloadLock.notify();
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
        //DataHolder.updateChosenCountryName(DataHolder.getDefaultCountryName());
        DataHolder.updateData();
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

    public void changeFragment(Fragment fragment) {
        if(fragment instanceof LoadingFragment) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    fragment).commit();
        } else {
            selectedFragment = fragment;
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    selectedFragment).commit();
        }
    }

    @Override
    public void onBackPressed() {
        int selectedItem = bottomNavigationView.getSelectedItemId();
        if(selectedItem != R.id.nav_country_brief) {
            bottomNavigationView.setSelectedItemId(R.id.nav_country_brief);
        }
        else {
            if(selectedFragment instanceof StatisticsFragment) {
                isCountryChangeRequired = false;
                bottomNavigationView.setSelectedItemId(R.id.nav_country_brief);
            } else {
                super.onBackPressed();
            }
        }
    }
}