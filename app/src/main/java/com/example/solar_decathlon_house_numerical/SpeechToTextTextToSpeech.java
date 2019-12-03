package com.example.solar_decathlon_house_numerical;

import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.lang.String;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;

public class SpeechToTextTextToSpeech extends AppCompatActivity {
    private final int REQ_CODE = 100;
    TextToSpeech textToSpeech;

    volatile double total, average, minimum, maximum, current;
    volatile boolean whilebool = true;
    Thread thread = new Thread();

    String value, toSpeak;
    String user = "rpihubteam6";  //Samba User name
    String pass ="raspberrypi";   //Samba Password
    String sharedFolder="share";  //Samba Shared folder
    String domain = "rpihubteam6";    //Samba domain name
    String ipAddressWireless = "192.168.1.10"; //IP address for rpihubteam6 when it is wirelessly connected with the router
    String ipAddressEthernet = "192.168.1.11"; //IP address for rpihubteam6 when it is wired with the router
    String powerFileName = "power.csv";
    String temperatureFileName = "heatsignature.csv";
    String water1FileName = "water1.csv";
    String water2FileName = "water2.csv";
    String humidityFileName = "humiditysignature.csv";
    String houseInfo = "houseSummary.txt";
    String houseWelcome = "houseWelcome.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stt_tts);

        ImageView speak = findViewById(R.id.microphone);
        speak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US);
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Please, speak for me to assist you.");
                try {
                    startActivityForResult(intent, REQ_CODE);
                } catch (ActivityNotFoundException a) {
                    Toast.makeText(getApplicationContext(),
                            "Sorry your device is not supported",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.US);
                    textToSpeech.setSpeechRate((float) 0.90);
                }
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_CODE: {
                if ((resultCode == RESULT_OK) && (null != data)) {
                    /* 1. This Following Array list stores all the variations of what user speaks.
                     * 2. We will take what is store very firstly and store that as a string.
                     * 3. Change the string to lower case.
                     * 4. We will split the string using " " (space) as a delimiter.
                     * 5. We will pass the speech and get the keywords.
                     * 6. We will use those keywords to get the information necessary.
                     */
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String spokenWords = result.get(0).toLowerCase();
                    spokenWords.toLowerCase();
                    String[] wordsToSearch = spokenWords.split(" ");
                    String[] keywords = keywordSearch(wordsToSearch);

                    /* 1. The outermost 'if' checks whether the user requested anything related to
                     *    sensors or the house. If not then we will inform the user about it.
                     * 2. The first outermost 'else if' checks for the presence of 'nohouse' keyword
                     *    along with the major sensor names. If found one then we dive into the other
                     *    sensors.
                     * 3. The second outermost 'else if' checks for the presence of 'nomajors' keywords
                     *    along with the major house keywords. If found one then we dive into other
                     *    information related to the house.
                     */
                    if(Arrays.asList(keywords).contains("nomajor") && Arrays.asList(keywords).contains("nohouse"))
                    {
                        toSpeak = "No major sensors or anything about the house was requested. " +
                                "For major sensors, please, choose between Power, Temperature, " +
                                "Water, or Humidity. For anything related to house, you can welcome " +
                                "new guests or give brief Information about the house.";
                        textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                    }
                    else if(Arrays.asList(keywords).contains("nohouse") && (Arrays.asList(keywords).contains("power") ||
                                    Arrays.asList(keywords).contains("temperature") ||
                                    Arrays.asList(keywords).contains("water") ||
                                    Arrays.asList(keywords).contains("humidity")))
                    {
                        if(Arrays.asList(keywords).contains("twomajors"))
                        {
                            toSpeak = "Information about more than one major sensors was " +
                                    "requested. Please choose just one out of Power, Temperature, " +
                                    "Water, or Humidity.";
                            textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                        }
                        else if(Arrays.asList(keywords).contains("waterheater"))
                        {
                            if(Arrays.asList(keywords).contains("total") || Arrays.asList(keywords).contains("average")
                              || Arrays.asList(keywords).contains("max") || Arrays.asList(keywords).contains("maximum")
                              || Arrays.asList(keywords).contains("min") || Arrays.asList(keywords).contains("minimum"))
                            {
                                jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
                                try{
                                    //Creating a new thread for the file transfer, this takes the load off the main thread.
                                    thread = new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                        try
                                        {
                                            //To get Samba Shared file from the Raspberry Pi
                                            List<String[]> power;

                                            String url1 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/" + powerFileName;
                                            String url2 = "smb://" + ipAddressEthernet + "/" + sharedFolder + "/" + powerFileName;
                                            NtlmPasswordAuthentication auth1 = new NtlmPasswordAuthentication(domain, user, pass);
                                            InputStream smbPowerFileWireless;
                                            InputStream smbPowerFileEthernet;
                                            CSVReader csv_power;

                                            boolean wirelessFileAvailable = new SmbFile(url1, auth1).exists();
                                            if(wirelessFileAvailable) {
                                                smbPowerFileWireless = new SmbFile(url1, auth1).getInputStream();
                                                csv_power = new CSVReader(smbPowerFileWireless, "power");
                                            }
                                            else {
                                                smbPowerFileEthernet = new SmbFile(url2, auth1).getInputStream();
                                                csv_power = new CSVReader(smbPowerFileEthernet, "power");
                                            }

                                            power = csv_power.read();

                                            double totalPower = 0.0;
                                            for (int i = 0; i < power.size(); i++) {
                                                String[] row = power.get(i);
                                                totalPower += (Double.parseDouble(row[5]) * 60) / 1000;
                                            }

                                            total = 0.0;
                                            total = Math.round(totalPower * Math.pow(10, 1)) / Math.pow(10, 1); //To round off to two decimal places.
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        }
                                    });
                                    thread.start();
                                }
                                catch(Exception e)
                                {
                                    e.printStackTrace();
                                }
                                whilebool = true;
                                while(whilebool) {
                                    if (!thread.isAlive()) {
                                        value =  Double.toString(total);
                                        toSpeak = "Total Power Consumption of the Water Heater is " + value + " Watts Per Hour";
                                        textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                                        whilebool = false;
                                    }
                                }
                            }
                            else if(Arrays.asList(keywords).contains("current"))
                            {
                                jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
                                try{
                                    //Creating a new thread for the file transfer, this takes the load off the main thread.
                                    thread = new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                        try
                                        {
                                            //To get Samba Shared file from the Raspberry Pi
                                            List<String[]> power;

                                            String url1 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/" + powerFileName;
                                            String url2 = "smb://" + ipAddressEthernet + "/" + sharedFolder + "/" + powerFileName;
                                            NtlmPasswordAuthentication auth1 = new NtlmPasswordAuthentication(domain, user, pass);
                                            InputStream smbPowerFileWireless;
                                            InputStream smbPowerFileEthernet;
                                            CSVReader csv_power;

                                            boolean wirelessFileAvailable = new SmbFile(url1, auth1).exists();
                                            if(wirelessFileAvailable) {
                                                smbPowerFileWireless = new SmbFile(url1, auth1).getInputStream();
                                                csv_power = new CSVReader(smbPowerFileWireless, "power");
                                            }
                                            else {
                                                smbPowerFileEthernet = new SmbFile(url2, auth1).getInputStream();
                                                csv_power = new CSVReader(smbPowerFileEthernet, "power");
                                            }

                                            power = csv_power.read();

                                            double currentPower = 0.0;
                                            for (int i = 0; i < power.size(); i++) {
                                                String[] row = power.get(i);
                                                if( i == power.size() - 1)
                                                {
                                                    currentPower = (Double.parseDouble(row[5]) * 60) / 1000;
                                                }
                                            }
                                            current = 0.0;
                                            current = Math.round(currentPower * Math.pow(10, 1)) / Math.pow(10, 1); //To round off to two decimal places.
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        }
                                    });
                                    thread.start();
                                }
                                catch(Exception e)
                                {
                                    e.printStackTrace();
                                }
                                whilebool = true;
                                while(whilebool) {
                                    if (!thread.isAlive()) {
                                        value =  Double.toString(current);
                                        toSpeak = "Current Power Consumption of the Water Heater is " + value + " Watts Per Hour";
                                        textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                                        whilebool = false;
                                    }
                                }
                            }
                        }
                        else if(Arrays.asList(keywords).contains("watertank"))
                        {
                            if(Arrays.asList(keywords).contains("average") || Arrays.asList(keywords).contains("total"))
                            {
                                jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
                                try{
                                    //Creating a new thread for the file transfer, this takes the load off the main thread.
                                    thread = new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                        try
                                        {
                                            //To get Samba Shared file from the Raspberry Pi
                                            List<String[]> temperature;

                                            String url1 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/" + temperatureFileName;
                                            String url2 = "smb://" + ipAddressEthernet + "/" + sharedFolder + "/" + temperatureFileName;
                                            NtlmPasswordAuthentication auth1 = new NtlmPasswordAuthentication(domain, user, pass);
                                            InputStream smbTemperatureFileWireless;
                                            InputStream smbTemperatureFileEthernet;
                                            CSVReader csv_temperature;

                                            boolean wirelessFileAvailable = new SmbFile(url1, auth1).exists();
                                            if(wirelessFileAvailable) {
                                                smbTemperatureFileWireless = new SmbFile(url1, auth1).getInputStream();
                                                csv_temperature = new CSVReader(smbTemperatureFileWireless, "heat");
                                            }
                                            else {
                                                smbTemperatureFileEthernet = new SmbFile(url2, auth1).getInputStream();
                                                csv_temperature = new CSVReader(smbTemperatureFileEthernet, "heat");
                                            }

                                            temperature = csv_temperature.read();

                                            double sumOfTemp = 0.0;
                                            for (int i = 0; i < temperature.size(); i++) {
                                                String[] rows = temperature.get(i);
                                                sumOfTemp += Double.parseDouble(rows[7]);
                                            }
                                            double temp = sumOfTemp/(temperature.size());
                                            average = 0.0;
                                            average = Math.round(temp * Math.pow(10, 1)) / Math.pow(10, 1); //To round off to two decimal places.
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        }
                                    });
                                    thread.start();
                                }
                                catch(Exception e)
                                {
                                    e.printStackTrace();
                                }
                                whilebool = true;
                                while(whilebool)
                                {
                                    if(!thread.isAlive())
                                    {
                                        value = Double.toString(average);
                                        toSpeak = "Average Temperature of the Water Tank is " + value + " Fahrenheit";
                                        textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                                        whilebool = false;
                                    }
                                }
                            }
                            else if(Arrays.asList(keywords).contains("max") || Arrays.asList(keywords).contains("maximum"))
                            {
                                jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
                                try{
                                    //Creating a new thread for the file transfer, this takes the load off the main thread.
                                    thread = new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                        try
                                        {
                                            //To get Samba Shared file from the Raspberry Pi
                                            List<String[]> temperature;

                                            String url1 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/" + temperatureFileName;
                                            String url2 = "smb://" + ipAddressEthernet + "/" + sharedFolder + "/" + temperatureFileName;
                                            NtlmPasswordAuthentication auth1 = new NtlmPasswordAuthentication(domain, user, pass);
                                            InputStream smbTemperatureFileWireless;
                                            InputStream smbTemperatureFileEthernet;
                                            CSVReader csv_temperature;

                                            boolean wirelessFileAvailable = new SmbFile(url1, auth1).exists();
                                            if(wirelessFileAvailable) {
                                                smbTemperatureFileWireless = new SmbFile(url1, auth1).getInputStream();
                                                csv_temperature = new CSVReader(smbTemperatureFileWireless, "heat");
                                            }
                                            else {
                                                smbTemperatureFileEthernet = new SmbFile(url2, auth1).getInputStream();
                                                csv_temperature = new CSVReader(smbTemperatureFileEthernet, "heat");
                                            }

                                            temperature = csv_temperature.read();

                                            String[] rows = temperature.get(0);
                                            double maxTemp = Double.parseDouble(rows[7]);
                                            for (int i = 0; i < temperature.size(); i++) {
                                                String[] row = temperature.get(i);
                                                if(Double.parseDouble(row[7]) > maxTemp)
                                                {
                                                    maxTemp = Double.parseDouble(row[7]);
                                                }
                                            }
                                            maximum = 0.0;
                                            maximum = Math.round(maxTemp * Math.pow(10, 1)) / Math.pow(10, 1); //To round off to two decimal places.
                                            value =  Double.toString(maximum);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        }
                                    });
                                    thread.start();
                                }
                                catch(Exception e)
                                {
                                    e.printStackTrace();
                                }
                                whilebool = true;
                                while(whilebool) {
                                    if (!thread.isAlive()) {
                                        toSpeak = "Maximum Temperature of the Water Tank is " + value + " Fahrenheit";
                                        textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                                        whilebool = false;
                                    }
                                }
                            }
                            else if(Arrays.asList(keywords).contains("min") || Arrays.asList(keywords).contains("minimum"))
                            {
                                jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
                                try{
                                    //Creating a new thread for the file transfer, this takes the load off the main thread.
                                    thread = new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                        try
                                        {
                                            //To get Samba Shared file from the Raspberry Pi
                                            List<String[]> temperature;

                                            String url1 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/" + temperatureFileName;
                                            String url2 = "smb://" + ipAddressEthernet + "/" + sharedFolder + "/" + temperatureFileName;
                                            NtlmPasswordAuthentication auth1 = new NtlmPasswordAuthentication(domain, user, pass);
                                            InputStream smbTemperatureFileWireless;
                                            InputStream smbTemperatureFileEthernet;
                                            CSVReader csv_temperature;

                                            boolean wirelessFileAvailable = new SmbFile(url1, auth1).exists();
                                            if(wirelessFileAvailable) {
                                                smbTemperatureFileWireless = new SmbFile(url1, auth1).getInputStream();
                                                csv_temperature = new CSVReader(smbTemperatureFileWireless, "heat");
                                            }
                                            else {
                                                smbTemperatureFileEthernet = new SmbFile(url2, auth1).getInputStream();
                                                csv_temperature = new CSVReader(smbTemperatureFileEthernet, "heat");
                                            }

                                            temperature = csv_temperature.read();

                                            String[] rows = temperature.get(0);
                                            double minTemp = Double.parseDouble(rows[7]);
                                            for (int i = 0; i < temperature.size(); i++) {
                                                String[] row = temperature.get(i);
                                                if(Double.parseDouble(row[7]) < minTemp)
                                                {
                                                    minTemp = Double.parseDouble(row[6]);
                                                }
                                            }
                                            minimum = 0.0;
                                            minimum = Math.round(minTemp * Math.pow(10, 1)) / Math.pow(10, 1); //To round off to two decimal places.
                                            value =  Double.toString(minimum);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        }
                                    });
                                    thread.start();
                                }
                                catch(Exception e)
                                {
                                    e.printStackTrace();
                                }
                                whilebool = true;
                                while(whilebool) {
                                    if (!thread.isAlive()) {
                                        toSpeak = "Minimum Temperature of the Water Tank is " + value + " Fahrenheit";
                                        textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                                        whilebool = false;
                                    }
                                }
                            }
                            else if(Arrays.asList(keywords).contains("current"))
                            {
                                jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
                                try{
                                    //Creating a new thread for the file transfer, this takes the load off the main thread.
                                    thread = new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                        try
                                        {
                                            //To get Samba Shared file from the Raspberry Pi
                                            List<String[]> temperature;

                                            String url1 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/" + temperatureFileName;
                                            String url2 = "smb://" + ipAddressEthernet + "/" + sharedFolder + "/" + temperatureFileName;
                                            NtlmPasswordAuthentication auth1 = new NtlmPasswordAuthentication(domain, user, pass);
                                            InputStream smbTemperatureFileWireless;
                                            InputStream smbTemperatureFileEthernet;
                                            CSVReader csv_temperature;

                                            boolean wirelessFileAvailable = new SmbFile(url1, auth1).exists();
                                            if(wirelessFileAvailable) {
                                                smbTemperatureFileWireless = new SmbFile(url1, auth1).getInputStream();
                                                csv_temperature = new CSVReader(smbTemperatureFileWireless, "heat");
                                            }
                                            else {
                                                smbTemperatureFileEthernet = new SmbFile(url2, auth1).getInputStream();
                                                csv_temperature = new CSVReader(smbTemperatureFileEthernet, "heat");
                                            }

                                            temperature = csv_temperature.read();

                                            double currentTemp = 0.0;
                                            for (int i = 0; i < temperature.size(); i++) {
                                                String[] row = temperature.get(i);
                                                if( i == temperature.size() - 1)
                                                {
                                                    currentTemp = Double.parseDouble(row[7]);
                                                }
                                            }
                                            current = 0.0;
                                            current = Math.round(currentTemp * Math.pow(10, 1)) / Math.pow(10, 1); //To round off to two decimal places.
                                            value =  Double.toString(current);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        }
                                    });
                                    thread.start();
                                }
                                catch(Exception e)
                                {
                                    e.printStackTrace();
                                }
                                whilebool = true;
                                while(whilebool) {
                                    if (!thread.isAlive()) {
                                        toSpeak = "Current Temperature of the Water Tank is " + value + " Fahrenheit";
                                        textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                                        whilebool = false;
                                    }
                                }
                            }
                        }
                        else if(Arrays.asList(keywords).contains("watersolarcollector"))
                        {
                            if(Arrays.asList(keywords).contains("average") || Arrays.asList(keywords).contains("total"))
                            {
                                jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
                                try{
                                    //Creating a new thread for the file transfer, this takes the load off the main thread.
                                    thread = new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                        try
                                        {
                                            //To get Samba Shared file from the Raspberry Pi
                                            List<String[]> temperature;

                                            String url1 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/" + temperatureFileName;
                                            String url2 = "smb://" + ipAddressEthernet + "/" + sharedFolder + "/" + temperatureFileName;
                                            NtlmPasswordAuthentication auth1 = new NtlmPasswordAuthentication(domain, user, pass);
                                            InputStream smbTemperatureFileWireless;
                                            InputStream smbTemperatureFileEthernet;
                                            CSVReader csv_temperature;

                                            boolean wirelessFileAvailable = new SmbFile(url1, auth1).exists();
                                            if(wirelessFileAvailable) {
                                                smbTemperatureFileWireless = new SmbFile(url1, auth1).getInputStream();
                                                csv_temperature = new CSVReader(smbTemperatureFileWireless, "heat");
                                            }
                                            else {
                                                smbTemperatureFileEthernet = new SmbFile(url2, auth1).getInputStream();
                                                csv_temperature = new CSVReader(smbTemperatureFileEthernet, "heat");
                                            }

                                            temperature = csv_temperature.read();

                                            double sumOfTemp = 0.0;
                                            for (int i = 0; i < temperature.size(); i++) {
                                                String[] rows = temperature.get(i);
                                                sumOfTemp += Double.parseDouble(rows[8]);
                                            }
                                            double temp = sumOfTemp/(temperature.size());
                                            average = 0.0;
                                            average = Math.round(temp * Math.pow(10, 1)) / Math.pow(10, 1); //To round off to two decimal places.
                                            value =  Double.toString(average);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        }
                                    });
                                    thread.start();
                                }
                                catch(Exception e)
                                {
                                    e.printStackTrace();
                                }
                                whilebool = true;
                                while(whilebool) {
                                    if (!thread.isAlive()) {
                                        toSpeak = "Average Temperature of the Water Solar Collector is " + value + " Fahrenheit";
                                        textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                                        whilebool = false;
                                    }
                                }
                            }
                            else if(Arrays.asList(keywords).contains("max") || Arrays.asList(keywords).contains("maximum"))
                            {
                                jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
                                try{
                                    //Creating a new thread for the file transfer, this takes the load off the main thread.
                                    thread = new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                        try
                                        {
                                            //To get Samba Shared file from the Raspberry Pi
                                            List<String[]> temperature;

                                            String url1 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/" + temperatureFileName;
                                            String url2 = "smb://" + ipAddressEthernet + "/" + sharedFolder + "/" + temperatureFileName;
                                            NtlmPasswordAuthentication auth1 = new NtlmPasswordAuthentication(domain, user, pass);
                                            InputStream smbTemperatureFileWireless;
                                            InputStream smbTemperatureFileEthernet;
                                            CSVReader csv_temperature;

                                            boolean wirelessFileAvailable = new SmbFile(url1, auth1).exists();
                                            if(wirelessFileAvailable) {
                                                smbTemperatureFileWireless = new SmbFile(url1, auth1).getInputStream();
                                                csv_temperature = new CSVReader(smbTemperatureFileWireless, "heat");
                                            }
                                            else {
                                                smbTemperatureFileEthernet = new SmbFile(url2, auth1).getInputStream();
                                                csv_temperature = new CSVReader(smbTemperatureFileEthernet, "heat");
                                            }

                                            temperature = csv_temperature.read();

                                            String[] rows = temperature.get(0);
                                            double maxTemp = Double.parseDouble(rows[8]);
                                            for (int i = 0; i < temperature.size(); i++) {
                                                String[] row = temperature.get(i);
                                                if(Double.parseDouble(row[8]) > maxTemp)
                                                {
                                                    maxTemp = Double.parseDouble(row[8]);
                                                }
                                            }
                                            maximum = 0.0;
                                            maximum = Math.round(maxTemp * Math.pow(10, 1)) / Math.pow(10, 1); //To round off to two decimal places.
                                            value =  Double.toString(maximum);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        }
                                    });
                                    thread.start();
                                }
                                catch(Exception e)
                                {
                                    e.printStackTrace();
                                }
                                whilebool = true;
                                while(whilebool) {
                                    if (!thread.isAlive()) {
                                        toSpeak = "Maximum Temperature of the Water Solar Collector is " + value + " Fahrenheit";
                                        textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                                        whilebool = false;
                                    }
                                }
                            }
                            else if(Arrays.asList(keywords).contains("min") || Arrays.asList(keywords).contains("minimum"))
                            {
                                jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
                                try{
                                    //Creating a new thread for the file transfer, this takes the load off the main thread.
                                    thread = new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                        try
                                        {
                                            //To get Samba Shared file from the Raspberry Pi
                                            List<String[]> temperature;

                                            String url1 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/" + temperatureFileName;
                                            String url2 = "smb://" + ipAddressEthernet + "/" + sharedFolder + "/" + temperatureFileName;
                                            NtlmPasswordAuthentication auth1 = new NtlmPasswordAuthentication(domain, user, pass);
                                            InputStream smbTemperatureFileWireless;
                                            InputStream smbTemperatureFileEthernet;
                                            CSVReader csv_temperature;

                                            boolean wirelessFileAvailable = new SmbFile(url1, auth1).exists();
                                            if(wirelessFileAvailable) {
                                                smbTemperatureFileWireless = new SmbFile(url1, auth1).getInputStream();
                                                csv_temperature = new CSVReader(smbTemperatureFileWireless, "heat");
                                            }
                                            else {
                                                smbTemperatureFileEthernet = new SmbFile(url2, auth1).getInputStream();
                                                csv_temperature = new CSVReader(smbTemperatureFileEthernet, "heat");
                                            }

                                            temperature = csv_temperature.read();

                                            String[] rows = temperature.get(0);
                                            double minTemp = Double.parseDouble(rows[8]);
                                            for (int i = 0; i < temperature.size(); i++) {
                                                String[] row = temperature.get(i);
                                                if(Double.parseDouble(row[8]) < minTemp)
                                                {
                                                    minTemp = Double.parseDouble(row[8]);
                                                }
                                            }
                                            minimum = 0.0;
                                            minimum = Math.round(minTemp * Math.pow(10, 1)) / Math.pow(10, 1); //To round off to two decimal places.
                                            value =  Double.toString(minimum);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        }
                                    });
                                    thread.start();
                                }
                                catch(Exception e)
                                {
                                    e.printStackTrace();
                                }
                                whilebool = true;
                                while(whilebool) {
                                    if (!thread.isAlive()) {
                                        toSpeak = "Minimum Temperature of the Water Solar Collector is " + value + " Fahrenheit";
                                        textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                                        whilebool = false;
                                    }
                                }
                            }
                            else if(Arrays.asList(keywords).contains("current"))
                            {
                                jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
                                try{
                                    //Creating a new thread for the file transfer, this takes the load off the main thread.
                                    thread = new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                        try
                                        {
                                            //To get Samba Shared file from the Raspberry Pi
                                            List<String[]> temperature;

                                            String url1 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/" + temperatureFileName;
                                            String url2 = "smb://" + ipAddressEthernet + "/" + sharedFolder + "/" + temperatureFileName;
                                            NtlmPasswordAuthentication auth1 = new NtlmPasswordAuthentication(domain, user, pass);
                                            InputStream smbTemperatureFileWireless;
                                            InputStream smbTemperatureFileEthernet;
                                            CSVReader csv_temperature;

                                            boolean wirelessFileAvailable = new SmbFile(url1, auth1).exists();
                                            if(wirelessFileAvailable) {
                                                smbTemperatureFileWireless = new SmbFile(url1, auth1).getInputStream();
                                                csv_temperature = new CSVReader(smbTemperatureFileWireless, "heat");
                                            }
                                            else {
                                                smbTemperatureFileEthernet = new SmbFile(url2, auth1).getInputStream();
                                                csv_temperature = new CSVReader(smbTemperatureFileEthernet, "heat");
                                            }

                                            temperature = csv_temperature.read();

                                            double currentTemp = 0.0;
                                            for (int i = 0; i < temperature.size(); i++) {
                                                String[] row = temperature.get(i);
                                                if( i == temperature.size() - 1)
                                                {
                                                    currentTemp = Double.parseDouble(row[8]);
                                                }
                                            }
                                            current = 0.0;
                                            current = Math.round(currentTemp * Math.pow(10, 1)) / Math.pow(10, 1); //To round off to two decimal places.
                                            value =  Double.toString(current);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        }
                                    });
                                    thread.start();
                                }
                                catch(Exception e)
                                {
                                    e.printStackTrace();
                                }
                                whilebool = true;
                                while(whilebool) {
                                    if (!thread.isAlive()) {
                                        toSpeak = "Current Temperature of the Water Solar Collector is " + value + " Fahrenheit";
                                        textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                                        whilebool = false;
                                    }
                                }
                            }
                        }
                        else if(Arrays.asList(keywords).contains("power"))
                        {
                            if(Arrays.asList(keywords).contains("production"))
                            {

                            }
                            else if(Arrays.asList(keywords).contains("consumption"))
                            {
                                if(Arrays.asList(keywords).contains("lighting"))
                                {
                                    if(Arrays.asList(keywords).contains("total") || Arrays.asList(keywords).contains("average")
                                            || Arrays.asList(keywords).contains("max") || Arrays.asList(keywords).contains("maximum")
                                            || Arrays.asList(keywords).contains("min") || Arrays.asList(keywords).contains("minimum"))
                                    {
                                        jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
                                        try{
                                            //Creating a new thread for the file transfer, this takes the load off the main thread.
                                            thread = new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                try
                                                {
                                                    //To get Samba Shared file from the Raspberry Pi
                                                    List<String[]> power;

                                                    String url1 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/" + powerFileName;
                                                    String url2 = "smb://" + ipAddressEthernet + "/" + sharedFolder + "/" + powerFileName;
                                                    NtlmPasswordAuthentication auth1 = new NtlmPasswordAuthentication(domain, user, pass);
                                                    InputStream smbPowerFileWireless;
                                                    InputStream smbPowerFileEthernet;
                                                    CSVReader csv_power;

                                                    boolean wirelessFileAvailable = new SmbFile(url1, auth1).exists();
                                                    if(wirelessFileAvailable) {
                                                        smbPowerFileWireless = new SmbFile(url1, auth1).getInputStream();
                                                        csv_power = new CSVReader(smbPowerFileWireless, "power");
                                                    }
                                                    else {
                                                        smbPowerFileEthernet = new SmbFile(url2, auth1).getInputStream();
                                                        csv_power = new CSVReader(smbPowerFileEthernet, "power");
                                                    }

                                                    power = csv_power.read();

                                                    double totalPower = 0.0;
                                                    for (int i = 0; i < power.size(); i++) {
                                                        String[] row = power.get(i);
                                                        totalPower += (Double.parseDouble(row[2]) * 60) / 1000;
                                                    }
                                                    total = 0.0;
                                                    total = Math.round(totalPower * Math.pow(10, 1)) / Math.pow(10, 1); //To round off to two decimal places.
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                                }
                                            });
                                            thread.start();
                                        }
                                        catch(Exception e)
                                        {
                                            e.printStackTrace();
                                        }
                                        whilebool = true;
                                        while(whilebool) {
                                            if (!thread.isAlive()) {
                                                value =  Double.toString(total);
                                                toSpeak = "Total Power Consumption of the Lighting Sensor is " + value + " Watts Per Hour";
                                                textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                                                whilebool = false;
                                            }
                                        }
                                    }
                                    else if(Arrays.asList(keywords).contains("current"))
                                    {
                                        jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
                                        try{
                                            //Creating a new thread for the file transfer, this takes the load off the main thread.
                                            thread = new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                try
                                                {
                                                    //To get Samba Shared file from the Raspberry Pi
                                                    List<String[]> power;

                                                    String url1 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/" + powerFileName;
                                                    String url2 = "smb://" + ipAddressEthernet + "/" + sharedFolder + "/" + powerFileName;
                                                    NtlmPasswordAuthentication auth1 = new NtlmPasswordAuthentication(domain, user, pass);
                                                    InputStream smbPowerFileWireless;
                                                    InputStream smbPowerFileEthernet;
                                                    CSVReader csv_power;

                                                    boolean wirelessFileAvailable = new SmbFile(url1, auth1).exists();
                                                    if(wirelessFileAvailable) {
                                                        smbPowerFileWireless = new SmbFile(url1, auth1).getInputStream();
                                                        csv_power = new CSVReader(smbPowerFileWireless, "power");
                                                    }
                                                    else {
                                                        smbPowerFileEthernet = new SmbFile(url2, auth1).getInputStream();
                                                        csv_power = new CSVReader(smbPowerFileEthernet, "power");
                                                    }

                                                    power = csv_power.read();

                                                    double currentPower = 0.0;
                                                    for (int i = 0; i < power.size(); i++) {
                                                        String[] row = power.get(i);
                                                        if( i == power.size() - 1)
                                                        {
                                                            currentPower = (Double.parseDouble(row[2]) * 60) / 1000;
                                                        }
                                                    }
                                                    current = 0.0;
                                                    current = Math.round(currentPower * Math.pow(10, 1)) / Math.pow(10, 1); //To round off to two decimal places.
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                                }
                                            });
                                            thread.start();
                                        }
                                        catch(Exception e)
                                        {
                                            e.printStackTrace();
                                        }
                                        whilebool = true;
                                        while(whilebool) {
                                            if (!thread.isAlive()) {
                                                value =  Double.toString(current);
                                                toSpeak = "Current Power Consumption of the Lighting Sensor is " + value + " Watts Per Hour";
                                                textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                                                whilebool = false;
                                            }
                                        }
                                    }
                                }
                                else if(Arrays.asList(keywords).contains("refrigerator"))
                                {
                                    if(Arrays.asList(keywords).contains("total") || Arrays.asList(keywords).contains("average")
                                            || Arrays.asList(keywords).contains("max") || Arrays.asList(keywords).contains("maximum")
                                            || Arrays.asList(keywords).contains("min") || Arrays.asList(keywords).contains("minimum"))
                                    {
                                        jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
                                        try{
                                            //Creating a new thread for the file transfer, this takes the load off the main thread.
                                            thread = new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                try
                                                {
                                                    //To get Samba Shared file from the Raspberry Pi
                                                    List<String[]> power;

                                                    String url1 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/" + powerFileName;
                                                    String url2 = "smb://" + ipAddressEthernet + "/" + sharedFolder + "/" + powerFileName;
                                                    NtlmPasswordAuthentication auth1 = new NtlmPasswordAuthentication(domain, user, pass);
                                                    InputStream smbPowerFileWireless;
                                                    InputStream smbPowerFileEthernet;
                                                    CSVReader csv_power;

                                                    boolean wirelessFileAvailable = new SmbFile(url1, auth1).exists();
                                                    if(wirelessFileAvailable) {
                                                        smbPowerFileWireless = new SmbFile(url1, auth1).getInputStream();
                                                        csv_power = new CSVReader(smbPowerFileWireless, "power");
                                                    }
                                                    else {
                                                        smbPowerFileEthernet = new SmbFile(url2, auth1).getInputStream();
                                                        csv_power = new CSVReader(smbPowerFileEthernet, "power");
                                                    }

                                                    power = csv_power.read();

                                                    double totalPower = 0.0;
                                                    for (int i = 0; i < power.size(); i++) {
                                                        String[] row = power.get(i);
                                                        totalPower += Double.parseDouble(row[6]) * 60 / 1000;
                                                    }
                                                    total = 0.0;
                                                    total = Math.round(totalPower * Math.pow(10, 1)) / Math.pow(10, 1); //To round off to two decimal places.
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                                }
                                            });
                                            thread.start();
                                        }
                                        catch(Exception e)
                                        {
                                            e.printStackTrace();
                                        }
                                        whilebool = true;
                                        while(whilebool) {
                                            if (!thread.isAlive()) {
                                                value =  Double.toString(total);
                                                toSpeak = "Total Power Consumption of the Refrigerator Sensor is " + value + " Watts Per Hour";
                                                textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                                                whilebool = false;
                                            }
                                        }
                                    }
                                    else if(Arrays.asList(keywords).contains("current"))
                                    {
                                        jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
                                        try{
                                            //Creating a new thread for the file transfer, this takes the load off the main thread.
                                            thread = new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                try
                                                {
                                                    //To get Samba Shared file from the Raspberry Pi
                                                    List<String[]> power;

                                                    String url1 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/" + powerFileName;
                                                    String url2 = "smb://" + ipAddressEthernet + "/" + sharedFolder + "/" + powerFileName;
                                                    NtlmPasswordAuthentication auth1 = new NtlmPasswordAuthentication(domain, user, pass);
                                                    InputStream smbPowerFileWireless;
                                                    InputStream smbPowerFileEthernet;
                                                    CSVReader csv_power;

                                                    boolean wirelessFileAvailable = new SmbFile(url1, auth1).exists();
                                                    if(wirelessFileAvailable) {
                                                        smbPowerFileWireless = new SmbFile(url1, auth1).getInputStream();
                                                        csv_power = new CSVReader(smbPowerFileWireless, "power");
                                                    }
                                                    else {
                                                        smbPowerFileEthernet = new SmbFile(url2, auth1).getInputStream();
                                                        csv_power = new CSVReader(smbPowerFileEthernet, "power");
                                                    }

                                                    power = csv_power.read();

                                                    double currentPower = 0.0;
                                                    for (int i = 0; i < power.size(); i++) {
                                                        String[] row = power.get(i);
                                                        if( i == power.size() - 1)
                                                        {
                                                            currentPower = Double.parseDouble(row[6]) * 60 / 1000;
                                                        }
                                                    }
                                                    current = 0.0;
                                                    current = Math.round(currentPower * Math.pow(10, 1)) / Math.pow(10, 1); //To round off to two decimal places.
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                                }
                                            });
                                            thread.start();
                                        }
                                        catch(Exception e)
                                        {
                                            e.printStackTrace();
                                        }
                                        whilebool = true;
                                        while(whilebool) {
                                            if (!thread.isAlive()) {
                                                value =  Double.toString(current);
                                                toSpeak = "Current Power Consumption of the Refrigerator Sensor is " + value + " Watts Per Hour";
                                                textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                                                whilebool = false;
                                            }
                                        }
                                    }
                                }
                                else if(Arrays.asList(keywords).contains("air")
                                        || Arrays.asList(keywords).contains("conditioner"))
                                {
                                    if(Arrays.asList(keywords).contains("total") || Arrays.asList(keywords).contains("average")
                                            || Arrays.asList(keywords).contains("max") || Arrays.asList(keywords).contains("maximum")
                                            || Arrays.asList(keywords).contains("min") || Arrays.asList(keywords).contains("minimum"))
                                    {
                                        jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
                                        try{
                                            //Creating a new thread for the file transfer, this takes the load off the main thread.
                                            thread = new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                try
                                                {
                                                    //To get Samba Shared file from the Raspberry Pi
                                                    List<String[]> power;

                                                    String url1 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/" + powerFileName;
                                                    String url2 = "smb://" + ipAddressEthernet + "/" + sharedFolder + "/" + powerFileName;
                                                    NtlmPasswordAuthentication auth1 = new NtlmPasswordAuthentication(domain, user, pass);
                                                    InputStream smbPowerFileWireless;
                                                    InputStream smbPowerFileEthernet;
                                                    CSVReader csv_power;

                                                    boolean wirelessFileAvailable = new SmbFile(url1, auth1).exists();
                                                    if(wirelessFileAvailable) {
                                                        smbPowerFileWireless = new SmbFile(url1, auth1).getInputStream();
                                                        csv_power = new CSVReader(smbPowerFileWireless, "power");
                                                    }
                                                    else {
                                                        smbPowerFileEthernet = new SmbFile(url2, auth1).getInputStream();
                                                        csv_power = new CSVReader(smbPowerFileEthernet, "power");
                                                    }

                                                    power = csv_power.read();

                                                    double totalPower = 0.0;
                                                    for (int i = 0; i < power.size(); i++) {
                                                        String[] row = power.get(i);
                                                        totalPower += Double.parseDouble(row[4]) * 60 / 1000;
                                                    }
                                                    total = 0.0;
                                                    total = Math.round(totalPower * Math.pow(10, 1)) / Math.pow(10, 1); //To round off to two decimal places.
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                                }
                                            });
                                            thread.start();
                                        }
                                        catch(Exception e)
                                        {
                                            e.printStackTrace();
                                        }
                                        whilebool = true;
                                        while(whilebool) {
                                            if (!thread.isAlive()) {
                                                value =  Double.toString(total);
                                                toSpeak = "Total Power Consumption of the Air Conditioner Sensor is " + value + " Watts Per Hour";
                                                textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                                                whilebool = false;
                                            }
                                        }
                                    }
                                    else if(Arrays.asList(keywords).contains("current"))
                                    {
                                        jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
                                        try{
                                            //Creating a new thread for the file transfer, this takes the load off the main thread.
                                            thread = new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                try
                                                {
                                                    //To get Samba Shared file from the Raspberry Pi
                                                    List<String[]> power;

                                                    String url1 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/" + powerFileName;
                                                    String url2 = "smb://" + ipAddressEthernet + "/" + sharedFolder + "/" + powerFileName;
                                                    NtlmPasswordAuthentication auth1 = new NtlmPasswordAuthentication(domain, user, pass);
                                                    InputStream smbPowerFileWireless;
                                                    InputStream smbPowerFileEthernet;
                                                    CSVReader csv_power;

                                                    boolean wirelessFileAvailable = new SmbFile(url1, auth1).exists();
                                                    if(wirelessFileAvailable) {
                                                        smbPowerFileWireless = new SmbFile(url1, auth1).getInputStream();
                                                        csv_power = new CSVReader(smbPowerFileWireless, "power");
                                                    }
                                                    else {
                                                        smbPowerFileEthernet = new SmbFile(url2, auth1).getInputStream();
                                                        csv_power = new CSVReader(smbPowerFileEthernet, "power");
                                                    }

                                                    power = csv_power.read();

                                                    double currentPower = 0.0;
                                                    for (int i = 0; i < power.size(); i++) {
                                                        String[] row = power.get(i);
                                                        if( i == power.size() - 1)
                                                        {
                                                            currentPower = Double.parseDouble(row[3]) * 60 / 1000;
                                                        }
                                                    }
                                                    current = 0.0;
                                                    current = Math.round(currentPower * Math.pow(10, 1)) / Math.pow(10, 1); //To round off to two decimal places.
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                                }
                                            });
                                            thread.start();
                                        }
                                        catch(Exception e)
                                        {
                                            e.printStackTrace();
                                        }
                                        whilebool = true;
                                        while(whilebool) {
                                            if (!thread.isAlive()) {
                                                value =  Double.toString(current);
                                                toSpeak = "Current Power Consumption of the Air Conditioner Sensor is " + value + " Watts Per Hour";
                                                textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                                                whilebool = false;
                                            }
                                        }
                                    }
                                }
                                else if(Arrays.asList(keywords).contains("kitchen"))
                                {
                                    if(Arrays.asList(keywords).contains("total") || Arrays.asList(keywords).contains("average")
                                            || Arrays.asList(keywords).contains("max") || Arrays.asList(keywords).contains("maximum")
                                            || Arrays.asList(keywords).contains("min") || Arrays.asList(keywords).contains("minimum"))
                                    {
                                        jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
                                        try{
                                            //Creating a new thread for the file transfer, this takes the load off the main thread.
                                            thread = new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                try
                                                {
                                                    //To get Samba Shared file from the Raspberry Pi
                                                    List<String[]> power;

                                                    String url1 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/" + powerFileName;
                                                    String url2 = "smb://" + ipAddressEthernet + "/" + sharedFolder + "/" + powerFileName;
                                                    NtlmPasswordAuthentication auth1 = new NtlmPasswordAuthentication(domain, user, pass);
                                                    InputStream smbPowerFileWireless;
                                                    InputStream smbPowerFileEthernet;
                                                    CSVReader csv_power;

                                                    boolean wirelessFileAvailable = new SmbFile(url1, auth1).exists();
                                                    if(wirelessFileAvailable) {
                                                        smbPowerFileWireless = new SmbFile(url1, auth1).getInputStream();
                                                        csv_power = new CSVReader(smbPowerFileWireless, "power");
                                                    }
                                                    else {
                                                        smbPowerFileEthernet = new SmbFile(url2, auth1).getInputStream();
                                                        csv_power = new CSVReader(smbPowerFileEthernet, "power");
                                                    }

                                                    power = csv_power.read();

                                                    double totalPower = 0.0;
                                                    for (int i = 0; i < power.size(); i++) {
                                                        String[] row = power.get(i);
                                                        totalPower += Double.parseDouble(row[3]) * 60 / 1000;
                                                    }
                                                    total = 0.0;
                                                    total = Math.round(totalPower * Math.pow(10, 1)) / Math.pow(10, 1); //To round off to two decimal places.
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                                }
                                            });
                                            thread.start();
                                        }
                                        catch(Exception e)
                                        {
                                            e.printStackTrace();
                                        }
                                        whilebool = true;
                                        while(whilebool) {
                                            if (!thread.isAlive()) {
                                                value =  Double.toString(total);
                                                toSpeak = "Total Power Consumption of the Air Conditioner Sensor is " + value + " Watts Per Hour";
                                                textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                                                whilebool = false;
                                            }
                                        }
                                    }
                                    else if(Arrays.asList(keywords).contains("current"))
                                    {
                                        jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
                                        try{
                                            //Creating a new thread for the file transfer, this takes the load off the main thread.
                                            thread = new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                try
                                                {
                                                    //To get Samba Shared file from the Raspberry Pi
                                                    List<String[]> power;

                                                    String url1 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/" + powerFileName;
                                                    String url2 = "smb://" + ipAddressEthernet + "/" + sharedFolder + "/" + powerFileName;
                                                    NtlmPasswordAuthentication auth1 = new NtlmPasswordAuthentication(domain, user, pass);
                                                    InputStream smbPowerFileWireless;
                                                    InputStream smbPowerFileEthernet;
                                                    CSVReader csv_power;

                                                    boolean wirelessFileAvailable = new SmbFile(url1, auth1).exists();
                                                    if(wirelessFileAvailable) {
                                                        smbPowerFileWireless = new SmbFile(url1, auth1).getInputStream();
                                                        csv_power = new CSVReader(smbPowerFileWireless, "power");
                                                    }
                                                    else {
                                                        smbPowerFileEthernet = new SmbFile(url2, auth1).getInputStream();
                                                        csv_power = new CSVReader(smbPowerFileEthernet, "power");
                                                    }

                                                    power = csv_power.read();

                                                    double currentPower = 0.0;
                                                    for (int i = 0; i < power.size(); i++) {
                                                        String[] row = power.get(i);
                                                        if( i == power.size() - 1)
                                                        {
                                                            currentPower = Double.parseDouble(row[3]) * 60 / 1000;
                                                        }
                                                    }
                                                    current = 0.0;
                                                    current = Math.round(currentPower * Math.pow(10, 1)) / Math.pow(10, 1); //To round off to two decimal places.
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                                }
                                            });
                                            thread.start();
                                        }
                                        catch(Exception e)
                                        {
                                            e.printStackTrace();
                                        }
                                        whilebool = true;
                                        while(whilebool) {
                                            if (!thread.isAlive()) {
                                                value =  Double.toString(current);
                                                toSpeak = "Current Power Consumption of the Air Conditioner Sensor is " + value + " Watts Per Hour";
                                                textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                                                whilebool = false;
                                            }
                                        }
                                    }
                                }
                                else if(Arrays.asList(keywords).contains("radiant")
                                        && Arrays.asList(keywords).contains("floor")
                                        && Arrays.asList(keywords).contains("pump"))
                                {
                                    if(Arrays.asList(keywords).contains("total") || Arrays.asList(keywords).contains("average")
                                            || Arrays.asList(keywords).contains("max") || Arrays.asList(keywords).contains("maximum")
                                            || Arrays.asList(keywords).contains("min") || Arrays.asList(keywords).contains("minimum"))
                                    {
                                        jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
                                        try{
                                            //Creating a new thread for the file transfer, this takes the load off the main thread.
                                            thread = new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    try
                                                    {
                                                        //To get Samba Shared file from the Raspberry Pi
                                                        List<String[]> power;

                                                        String url1 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/" + powerFileName;
                                                        String url2 = "smb://" + ipAddressEthernet + "/" + sharedFolder + "/" + powerFileName;
                                                        NtlmPasswordAuthentication auth1 = new NtlmPasswordAuthentication(domain, user, pass);
                                                        InputStream smbPowerFileWireless;
                                                        InputStream smbPowerFileEthernet;
                                                        CSVReader csv_power;

                                                        boolean wirelessFileAvailable = new SmbFile(url1, auth1).exists();
                                                        if(wirelessFileAvailable) {
                                                            smbPowerFileWireless = new SmbFile(url1, auth1).getInputStream();
                                                            csv_power = new CSVReader(smbPowerFileWireless, "power");
                                                        }
                                                        else {
                                                            smbPowerFileEthernet = new SmbFile(url2, auth1).getInputStream();
                                                            csv_power = new CSVReader(smbPowerFileEthernet, "power");
                                                        }

                                                        power = csv_power.read();

                                                        double totalPower = 0.0;
                                                        for (int i = 0; i < power.size(); i++) {
                                                            String[] row = power.get(i);
                                                            totalPower += Double.parseDouble(row[7]) * 60 / 1000;
                                                        }
                                                        total = 0.0;
                                                        total = Math.round(totalPower * Math.pow(10, 1)) / Math.pow(10, 1); //To round off to two decimal places.
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            });
                                            thread.start();
                                        }
                                        catch(Exception e)
                                        {
                                            e.printStackTrace();
                                        }
                                        whilebool = true;
                                        while(whilebool) {
                                            if (!thread.isAlive()) {
                                                value =  Double.toString(total);
                                                toSpeak = "Total Power Consumption of the Radiant Floor Pump is " + value + " Watts Per Hour";
                                                textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                                                whilebool = false;
                                            }
                                        }
                                    }
                                    else if(Arrays.asList(keywords).contains("current"))
                                    {
                                        jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
                                        try{
                                            //Creating a new thread for the file transfer, this takes the load off the main thread.
                                            thread = new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    try
                                                    {
                                                        //To get Samba Shared file from the Raspberry Pi
                                                        List<String[]> power;

                                                        String url1 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/" + powerFileName;
                                                        String url2 = "smb://" + ipAddressEthernet + "/" + sharedFolder + "/" + powerFileName;
                                                        NtlmPasswordAuthentication auth1 = new NtlmPasswordAuthentication(domain, user, pass);
                                                        InputStream smbPowerFileWireless;
                                                        InputStream smbPowerFileEthernet;
                                                        CSVReader csv_power;

                                                        boolean wirelessFileAvailable = new SmbFile(url1, auth1).exists();
                                                        if(wirelessFileAvailable) {
                                                            smbPowerFileWireless = new SmbFile(url1, auth1).getInputStream();
                                                            csv_power = new CSVReader(smbPowerFileWireless, "power");
                                                        }
                                                        else {
                                                            smbPowerFileEthernet = new SmbFile(url2, auth1).getInputStream();
                                                            csv_power = new CSVReader(smbPowerFileEthernet, "power");
                                                        }

                                                        power = csv_power.read();

                                                        double currentPower = 0.0;
                                                        for (int i = 0; i < power.size(); i++) {
                                                            String[] row = power.get(i);
                                                            if( i == power.size() - 1)
                                                            {
                                                                currentPower = Double.parseDouble(row[7]) * 60 / 1000;
                                                            }
                                                        }
                                                        current = 0.0;
                                                        current = Math.round(currentPower * Math.pow(10, 1)) / Math.pow(10, 1); //To round off to two decimal places.
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            });
                                            thread.start();
                                        }
                                        catch(Exception e)
                                        {
                                            e.printStackTrace();
                                        }
                                        whilebool = true;
                                        while(whilebool) {
                                            if (!thread.isAlive()) {
                                                value =  Double.toString(current);
                                                toSpeak = "Current Power Consumption of the Radiant Floor Pump is " + value + " Watts Per Hour";
                                                textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                                                whilebool = false;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        else if(Arrays.asList(keywords).contains("temperature"))
                        {
                            if(Arrays.asList(keywords).contains("south") || Arrays.asList(keywords).contains("exterior") || Arrays.asList(keywords).contains("outside"))
                            {
                                if(Arrays.asList(keywords).contains("average") || Arrays.asList(keywords).contains("total"))
                                {
                                    jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
                                    try{
                                        //Creating a new thread for the file transfer, this takes the load off the main thread.
                                        thread = new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                            try
                                            {
                                                //To get Samba Shared file from the Raspberry Pi
                                                List<String[]> temperature;

                                                String url1 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/" + temperatureFileName;
                                                String url2 = "smb://" + ipAddressEthernet + "/" + sharedFolder + "/" + temperatureFileName;
                                                NtlmPasswordAuthentication auth1 = new NtlmPasswordAuthentication(domain, user, pass);
                                                InputStream smbTemperatureFileWireless;
                                                InputStream smbTemperatureFileEthernet;
                                                CSVReader csv_temperature;

                                                boolean wirelessFileAvailable = new SmbFile(url1, auth1).exists();
                                                if(wirelessFileAvailable) {
                                                    smbTemperatureFileWireless = new SmbFile(url1, auth1).getInputStream();
                                                    csv_temperature = new CSVReader(smbTemperatureFileWireless, "heat");
                                                }
                                                else {
                                                    smbTemperatureFileEthernet = new SmbFile(url2, auth1).getInputStream();
                                                    csv_temperature = new CSVReader(smbTemperatureFileEthernet, "heat");
                                                }

                                                temperature = csv_temperature.read();

                                                double sumOfTemp = 0.0;
                                                for (int i = 0; i < temperature.size(); i++) {
                                                    String[] rows = temperature.get(i);
                                                    sumOfTemp += Double.parseDouble(rows[5]);
                                                }
                                                double temp = sumOfTemp/(temperature.size());
                                                average = Math.round(temp * Math.pow(10, 1)) / Math.pow(10, 1); //To round off to two decimal places.
                                                value = Double.toString(average);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            }
                                        });
                                        thread.start();
                                    }
                                    catch(Exception e)
                                    {
                                        e.printStackTrace();
                                    }
                                    whilebool = true;
                                    while(whilebool) {
                                        if (!thread.isAlive()) {
                                            toSpeak = "Average Temperature of the South Wall is " + value + " Fahrenheit";
                                            textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                                            whilebool = false;
                                        }
                                    }
                                }
                                else if(Arrays.asList(keywords).contains("max") || Arrays.asList(keywords).contains("maximum"))
                                {
                                    jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
                                    try{
                                        //Creating a new thread for the file transfer, this takes the load off the main thread.
                                        thread = new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                            try
                                            {
                                                //To get Samba Shared file from the Raspberry Pi
                                                List<String[]> temperature;

                                                String url1 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/" + temperatureFileName;
                                                String url2 = "smb://" + ipAddressEthernet + "/" + sharedFolder + "/" + temperatureFileName;
                                                NtlmPasswordAuthentication auth1 = new NtlmPasswordAuthentication(domain, user, pass);
                                                InputStream smbTemperatureFileWireless;
                                                InputStream smbTemperatureFileEthernet;
                                                CSVReader csv_temperature;

                                                boolean wirelessFileAvailable = new SmbFile(url1, auth1).exists();
                                                if(wirelessFileAvailable) {
                                                    smbTemperatureFileWireless = new SmbFile(url1, auth1).getInputStream();
                                                    csv_temperature = new CSVReader(smbTemperatureFileWireless, "heat");
                                                }
                                                else {
                                                    smbTemperatureFileEthernet = new SmbFile(url2, auth1).getInputStream();
                                                    csv_temperature = new CSVReader(smbTemperatureFileEthernet, "heat");
                                                }

                                                temperature = csv_temperature.read();

                                                String[] rows = temperature.get(0);
                                                double maxTemp = Double.parseDouble(rows[5]);
                                                for (int i = 0; i < temperature.size(); i++) {
                                                    String[] row = temperature.get(i);
                                                    if(Double.parseDouble(row[5]) > maxTemp)
                                                    {
                                                        maxTemp = Double.parseDouble(row[5]);
                                                    }
                                                }
                                                maximum = 0.0;
                                                maximum = Math.round(maxTemp * Math.pow(10, 1)) / Math.pow(10, 1); //To round off to two decimal places.
                                                value =  Double.toString(maximum);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            }
                                        });
                                        thread.start();
                                    }
                                    catch(Exception e)
                                    {
                                        e.printStackTrace();
                                    }
                                    whilebool = true;
                                    while(whilebool) {
                                        if (!thread.isAlive()) {
                                            toSpeak = "Maximum Temperature of the South Wall is " + value + " Fahrenheit";
                                            textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                                            whilebool = false;
                                        }
                                    }
                                }
                                else if(Arrays.asList(keywords).contains("min") || Arrays.asList(keywords).contains("minimum"))
                                {
                                    jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
                                    try{
                                        //Creating a new thread for the file transfer, this takes the load off the main thread.
                                        thread = new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                            try
                                            {
                                                //To get Samba Shared file from the Raspberry Pi
                                                List<String[]> temperature;

                                                String url1 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/" + temperatureFileName;
                                                String url2 = "smb://" + ipAddressEthernet + "/" + sharedFolder + "/" + temperatureFileName;
                                                NtlmPasswordAuthentication auth1 = new NtlmPasswordAuthentication(domain, user, pass);
                                                InputStream smbTemperatureFileWireless;
                                                InputStream smbTemperatureFileEthernet;
                                                CSVReader csv_temperature;

                                                boolean wirelessFileAvailable = new SmbFile(url1, auth1).exists();
                                                if(wirelessFileAvailable) {
                                                    smbTemperatureFileWireless = new SmbFile(url1, auth1).getInputStream();
                                                    csv_temperature = new CSVReader(smbTemperatureFileWireless, "heat");
                                                }
                                                else {
                                                    smbTemperatureFileEthernet = new SmbFile(url2, auth1).getInputStream();
                                                    csv_temperature = new CSVReader(smbTemperatureFileEthernet, "heat");
                                                }

                                                temperature = csv_temperature.read();

                                                String[] rows = temperature.get(0);
                                                double minTemp = Double.parseDouble(rows[5]);
                                                for (int i = 0; i < temperature.size(); i++) {
                                                    String[] row = temperature.get(i);
                                                    if(Double.parseDouble(row[5]) < minTemp)
                                                    {
                                                        minTemp = Double.parseDouble(row[5]);
                                                    }
                                                }
                                                minimum = 0.0;
                                                minimum = Math.round(minTemp * Math.pow(10, 1)) / Math.pow(10, 1); //To round off to two decimal places.
                                                value =  Double.toString(minimum);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            }
                                        });
                                        thread.start();
                                    }
                                    catch(Exception e)
                                    {
                                        e.printStackTrace();
                                    }
                                    whilebool = true;
                                    while(whilebool) {
                                        if (!thread.isAlive()) {
                                            toSpeak = "Minimum Temperature of the South Wall is " + value + " Fahrenheit";
                                            textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                                            whilebool = false;
                                        }
                                    }
                                }
                                else if(Arrays.asList(keywords).contains("current"))
                                {
                                    jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
                                    try{
                                        //Creating a new thread for the file transfer, this takes the load off the main thread.
                                        thread = new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                            try
                                            {
                                                //To get Samba Shared file from the Raspberry Pi
                                                List<String[]> temperature;

                                                String url1 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/" + temperatureFileName;
                                                String url2 = "smb://" + ipAddressEthernet + "/" + sharedFolder + "/" + temperatureFileName;
                                                NtlmPasswordAuthentication auth1 = new NtlmPasswordAuthentication(domain, user, pass);
                                                InputStream smbTemperatureFileWireless;
                                                InputStream smbTemperatureFileEthernet;
                                                CSVReader csv_temperature;

                                                boolean wirelessFileAvailable = new SmbFile(url1, auth1).exists();
                                                if(wirelessFileAvailable) {
                                                    smbTemperatureFileWireless = new SmbFile(url1, auth1).getInputStream();
                                                    csv_temperature = new CSVReader(smbTemperatureFileWireless, "heat");
                                                }
                                                else {
                                                    smbTemperatureFileEthernet = new SmbFile(url2, auth1).getInputStream();
                                                    csv_temperature = new CSVReader(smbTemperatureFileEthernet, "heat");
                                                }

                                                temperature = csv_temperature.read();

                                                double currentTemp = 0.0;
                                                for (int i = 0; i < temperature.size(); i++) {
                                                    String[] row = temperature.get(i);
                                                    if( i == temperature.size() - 1)
                                                    {
                                                        currentTemp = Double.parseDouble(row[5]);
                                                    }
                                                }
                                                current = 0.0;
                                                current = Math.round(currentTemp * Math.pow(10, 1)) / Math.pow(10, 1); //To round off to two decimal places.
                                                value =  Double.toString(current);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            }
                                        });
                                        thread.start();
                                    }
                                    catch(Exception e)
                                    {
                                        e.printStackTrace();
                                    }
                                    whilebool = true;
                                    while(whilebool) {
                                        if (!thread.isAlive()) {
                                            toSpeak = "Current Temperature of the South Wall is " + value + " Fahrenheit";
                                            textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                                            whilebool = false;
                                        }
                                    }
                                }
                            }
                            else if(Arrays.asList(keywords).contains("interior")
                                    || Arrays.asList(keywords).contains("inside"))
                            {
                                if(Arrays.asList(keywords).contains("average") || Arrays.asList(keywords).contains("total"))
                                {
                                    jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
                                    try{
                                        //Creating a new thread for the file transfer, this takes the load off the main thread.
                                        thread = new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                            try
                                            {
                                                //To get Samba Shared file from the Raspberry Pi
                                                List<String[]> temperature;

                                                String url1 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/" + temperatureFileName;
                                                String url2 = "smb://" + ipAddressEthernet + "/" + sharedFolder + "/" + temperatureFileName;
                                                NtlmPasswordAuthentication auth1 = new NtlmPasswordAuthentication(domain, user, pass);
                                                InputStream smbTemperatureFileWireless;
                                                InputStream smbTemperatureFileEthernet;
                                                CSVReader csv_temperature;

                                                boolean wirelessFileAvailable = new SmbFile(url1, auth1).exists();
                                                if(wirelessFileAvailable) {
                                                    smbTemperatureFileWireless = new SmbFile(url1, auth1).getInputStream();
                                                    csv_temperature = new CSVReader(smbTemperatureFileWireless, "heat");
                                                }
                                                else {
                                                    smbTemperatureFileEthernet = new SmbFile(url2, auth1).getInputStream();
                                                    csv_temperature = new CSVReader(smbTemperatureFileEthernet, "heat");
                                                }

                                                temperature = csv_temperature.read();

                                                double sumOfTemp = 0.0;
                                                for (int i = 0; i < temperature.size(); i++) {
                                                    String[] rows = temperature.get(i);
                                                    sumOfTemp += Double.parseDouble(rows[1]);
                                                }
                                                double temp = sumOfTemp/(temperature.size());
                                                average = 0.0;
                                                average = Math.round(temp * Math.pow(10, 1)) / Math.pow(10, 1); //To round off to two decimal places.
                                                value =  Double.toString(average);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            }
                                        });
                                        thread.start();
                                    }
                                    catch(Exception e)
                                    {
                                        e.printStackTrace();
                                    }
                                    whilebool = true;
                                    while(whilebool) {
                                        if (!thread.isAlive()) {
                                            toSpeak = "Average Interior Temperature is " + value + " Fahrenheit";
                                            textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                                            whilebool = false;
                                        }
                                    }
                                }
                                else if(Arrays.asList(keywords).contains("max") || Arrays.asList(keywords).contains("maximum"))
                                {
                                    jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
                                    try{
                                        //Creating a new thread for the file transfer, this takes the load off the main thread.
                                        thread = new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                            try
                                            {
                                                //To get Samba Shared file from the Raspberry Pi
                                                List<String[]> temperature;

                                                String url1 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/" + temperatureFileName;
                                                String url2 = "smb://" + ipAddressEthernet + "/" + sharedFolder + "/" + temperatureFileName;
                                                NtlmPasswordAuthentication auth1 = new NtlmPasswordAuthentication(domain, user, pass);
                                                InputStream smbTemperatureFileWireless;
                                                InputStream smbTemperatureFileEthernet;
                                                CSVReader csv_temperature;

                                                boolean wirelessFileAvailable = new SmbFile(url1, auth1).exists();
                                                if(wirelessFileAvailable) {
                                                    smbTemperatureFileWireless = new SmbFile(url1, auth1).getInputStream();
                                                    csv_temperature = new CSVReader(smbTemperatureFileWireless, "heat");
                                                }
                                                else {
                                                    smbTemperatureFileEthernet = new SmbFile(url2, auth1).getInputStream();
                                                    csv_temperature = new CSVReader(smbTemperatureFileEthernet, "heat");
                                                }

                                                temperature = csv_temperature.read();

                                                String[] rows = temperature.get(0);
                                                double maxTemp = Double.parseDouble(rows[2]);
                                                for (int i = 0; i < temperature.size(); i++) {
                                                    String[] row = temperature.get(i);
                                                    if(Double.parseDouble(row[2]) > maxTemp)
                                                    {
                                                        maxTemp = Double.parseDouble(row[1]);
                                                    }
                                                }
                                                maximum = 0.0;
                                                maximum = Math.round(maxTemp * Math.pow(10, 1)) / Math.pow(10, 1); //To round off to two decimal places.
                                                value =  Double.toString(maximum);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            }
                                        });
                                        thread.start();
                                    }
                                    catch(Exception e)
                                    {
                                        e.printStackTrace();
                                    }
                                    whilebool = true;
                                    while(whilebool) {
                                        if (!thread.isAlive()) {
                                            toSpeak = "Maximum Interior Temperature is " + value + " Fahrenheit";
                                            textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                                            whilebool = false;
                                        }
                                    }
                                }
                                else if(Arrays.asList(keywords).contains("min") || Arrays.asList(keywords).contains("minimum"))
                                {
                                    jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
                                    try{
                                        //Creating a new thread for the file transfer, this takes the load off the main thread.
                                        thread = new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                            try
                                            {
                                                //To get Samba Shared file from the Raspberry Pi
                                                List<String[]> temperature;

                                                String url1 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/" + temperatureFileName;
                                                String url2 = "smb://" + ipAddressEthernet + "/" + sharedFolder + "/" + temperatureFileName;
                                                NtlmPasswordAuthentication auth1 = new NtlmPasswordAuthentication(domain, user, pass);
                                                InputStream smbTemperatureFileWireless;
                                                InputStream smbTemperatureFileEthernet;
                                                CSVReader csv_temperature;

                                                boolean wirelessFileAvailable = new SmbFile(url1, auth1).exists();
                                                if(wirelessFileAvailable) {
                                                    smbTemperatureFileWireless = new SmbFile(url1, auth1).getInputStream();
                                                    csv_temperature = new CSVReader(smbTemperatureFileWireless, "heat");
                                                }
                                                else {
                                                    smbTemperatureFileEthernet = new SmbFile(url2, auth1).getInputStream();
                                                    csv_temperature = new CSVReader(smbTemperatureFileEthernet, "heat");
                                                }

                                                temperature = csv_temperature.read();

                                                String[] rows = temperature.get(0);
                                                double minTemp = Double.parseDouble(rows[2]);
                                                for (int i = 0; i < temperature.size(); i++) {
                                                    String[] row = temperature.get(i);
                                                    if(Double.parseDouble(row[2]) < minTemp)
                                                    {
                                                        minTemp = Double.parseDouble(row[2]);
                                                    }
                                                }
                                                minimum = 0.0;
                                                minimum = Math.round(minTemp * Math.pow(10, 1)) / Math.pow(10, 1); //To round off to two decimal places.

                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            }
                                        });
                                        thread.start();
                                    }
                                    catch(Exception e)
                                    {
                                        e.printStackTrace();
                                    }
                                    whilebool = true;
                                    while(whilebool) {
                                        if (!thread.isAlive()) {
                                            value = Double.toString(minimum);
                                            toSpeak = "Minimum Interior Temperature is " + value + " Fahrenheit";
                                            textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                                            whilebool = false;
                                        }
                                    }
                                }
                                else if(Arrays.asList(keywords).contains("current"))
                                {
                                    jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
                                    try{
                                        //Creating a new thread for the file transfer, this takes the load off the main thread.
                                        thread = new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                            try
                                            {
                                                //To get Samba Shared file from the Raspberry Pi
                                                List<String[]> temperature;

                                                String url1 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/" + temperatureFileName;
                                                String url2 = "smb://" + ipAddressEthernet + "/" + sharedFolder + "/" + temperatureFileName;
                                                NtlmPasswordAuthentication auth1 = new NtlmPasswordAuthentication(domain, user, pass);
                                                InputStream smbTemperatureFileWireless;
                                                InputStream smbTemperatureFileEthernet;
                                                CSVReader csv_temperature;

                                                boolean wirelessFileAvailable = new SmbFile(url1, auth1).exists();
                                                if(wirelessFileAvailable) {
                                                    smbTemperatureFileWireless = new SmbFile(url1, auth1).getInputStream();
                                                    csv_temperature = new CSVReader(smbTemperatureFileWireless, "heat");
                                                }
                                                else {
                                                    smbTemperatureFileEthernet = new SmbFile(url2, auth1).getInputStream();
                                                    csv_temperature = new CSVReader(smbTemperatureFileEthernet, "heat");
                                                }

                                                temperature = csv_temperature.read();

                                                double currentTemp = 0.0;
                                                for (int i = 0; i < temperature.size(); i++) {
                                                    String[] row = temperature.get(i);
                                                    if( i == temperature.size() - 1)
                                                    {
                                                        currentTemp = Double.parseDouble(row[2]);
                                                    }
                                                }
                                                current = 0.0;
                                                current = Math.round(currentTemp * Math.pow(10, 1)) / Math.pow(10, 1); //To round off to two decimal places.

                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            }
                                        });
                                        thread.start();
                                    }
                                    catch(Exception e)
                                    {
                                        e.printStackTrace();
                                    }
                                    whilebool = true;
                                    while(whilebool) {
                                        if (!thread.isAlive()) {
                                            value =  Double.toString(current);
                                            toSpeak = "Current Interior Temperature is " + value + " Fahrenheit";
                                            textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                                            whilebool = false;
                                        }
                                    }
                                }
                            }
                            else if(Arrays.asList(keywords).contains("solar")
                                    || Arrays.asList(keywords).contains("panel"))
                            {
                                if(Arrays.asList(keywords).contains("average") || Arrays.asList(keywords).contains("total"))
                                {
                                    jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
                                    try{
                                        //Creating a new thread for the file transfer, this takes the load off the main thread.
                                        thread = new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                            try
                                            {
                                                //To get Samba Shared file from the Raspberry Pi
                                                List<String[]> temperature;

                                                String url1 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/" + temperatureFileName;
                                                String url2 = "smb://" + ipAddressEthernet + "/" + sharedFolder + "/" + temperatureFileName;
                                                NtlmPasswordAuthentication auth1 = new NtlmPasswordAuthentication(domain, user, pass);
                                                InputStream smbTemperatureFileWireless;
                                                InputStream smbTemperatureFileEthernet;
                                                CSVReader csv_temperature;

                                                boolean wirelessFileAvailable = new SmbFile(url1, auth1).exists();
                                                if(wirelessFileAvailable) {
                                                    smbTemperatureFileWireless = new SmbFile(url1, auth1).getInputStream();
                                                    csv_temperature = new CSVReader(smbTemperatureFileWireless, "heat");
                                                }
                                                else {
                                                    smbTemperatureFileEthernet = new SmbFile(url2, auth1).getInputStream();
                                                    csv_temperature = new CSVReader(smbTemperatureFileEthernet, "heat");
                                                }

                                                temperature = csv_temperature.read();

                                                double sumOfTemp = 0.0;
                                                for (int i = 0; i < temperature.size(); i++) {
                                                    String[] rows = temperature.get(i);
                                                    sumOfTemp += Double.parseDouble(rows[3]);
                                                }
                                                double temp = sumOfTemp/(temperature.size());
                                                average = 0.0;
                                                average = Math.round(temp * Math.pow(10, 1)) / Math.pow(10, 1); //To round off to two decimal places.
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            }
                                        });
                                        thread.start();
                                    }
                                    catch(Exception e)
                                    {
                                        e.printStackTrace();
                                    }
                                    whilebool = true;
                                    while(whilebool) {
                                        if (!thread.isAlive()) {
                                            value =  Double.toString(average);
                                            toSpeak = "Average Temperature of the Solar Panels is " + value + " Fahrenheit";
                                            textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                                            whilebool = false;
                                        }
                                    }
                                }
                                else if(Arrays.asList(keywords).contains("max") || Arrays.asList(keywords).contains("maximum"))
                                {
                                    jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
                                    try{
                                        //Creating a new thread for the file transfer, this takes the load off the main thread.
                                        thread = new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                            try
                                            {
                                                //To get Samba Shared file from the Raspberry Pi
                                                List<String[]> temperature;

                                                String url1 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/" + temperatureFileName;
                                                String url2 = "smb://" + ipAddressEthernet + "/" + sharedFolder + "/" + temperatureFileName;
                                                NtlmPasswordAuthentication auth1 = new NtlmPasswordAuthentication(domain, user, pass);
                                                InputStream smbTemperatureFileWireless;
                                                InputStream smbTemperatureFileEthernet;
                                                CSVReader csv_temperature;

                                                boolean wirelessFileAvailable = new SmbFile(url1, auth1).exists();
                                                if(wirelessFileAvailable) {
                                                    smbTemperatureFileWireless = new SmbFile(url1, auth1).getInputStream();
                                                    csv_temperature = new CSVReader(smbTemperatureFileWireless, "heat");
                                                }
                                                else {
                                                    smbTemperatureFileEthernet = new SmbFile(url2, auth1).getInputStream();
                                                    csv_temperature = new CSVReader(smbTemperatureFileEthernet, "heat");
                                                }

                                                temperature = csv_temperature.read();

                                                String[] rows = temperature.get(0);
                                                double maxTemp = Double.parseDouble(rows[3]);
                                                for (int i = 0; i < temperature.size(); i++) {
                                                    String[] row = temperature.get(i);
                                                    if(Double.parseDouble(row[3]) > maxTemp)
                                                    {
                                                        maxTemp = Double.parseDouble(row[3]);
                                                    }
                                                }
                                                maximum = 0.0;
                                                maximum = Math.round(maxTemp * Math.pow(10, 1)) / Math.pow(10, 1); //To round off to two decimal places.
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            }
                                        });
                                        thread.start();
                                    }
                                    catch(Exception e)
                                    {
                                        e.printStackTrace();
                                    }
                                    whilebool = true;
                                    while(whilebool) {
                                        if (!thread.isAlive()) {
                                            value =  Double.toString(maximum);
                                            toSpeak = "Maximum Temperature of the Solar Panels is " + value + " Fahrenheit";
                                            textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                                            whilebool = false;
                                        }
                                    }
                                }
                                else if(Arrays.asList(keywords).contains("min") || Arrays.asList(keywords).contains("minimum"))
                                {
                                    jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
                                    try{
                                        //Creating a new thread for the file transfer, this takes the load off the main thread.
                                        thread = new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                            try
                                            {
                                                //To get Samba Shared file from the Raspberry Pi
                                                List<String[]> temperature;

                                                String url1 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/" + temperatureFileName;
                                                String url2 = "smb://" + ipAddressEthernet + "/" + sharedFolder + "/" + temperatureFileName;
                                                NtlmPasswordAuthentication auth1 = new NtlmPasswordAuthentication(domain, user, pass);
                                                InputStream smbTemperatureFileWireless;
                                                InputStream smbTemperatureFileEthernet;
                                                CSVReader csv_temperature;

                                                boolean wirelessFileAvailable = new SmbFile(url1, auth1).exists();
                                                if(wirelessFileAvailable) {
                                                    smbTemperatureFileWireless = new SmbFile(url1, auth1).getInputStream();
                                                    csv_temperature = new CSVReader(smbTemperatureFileWireless, "heat");
                                                }
                                                else {
                                                    smbTemperatureFileEthernet = new SmbFile(url2, auth1).getInputStream();
                                                    csv_temperature = new CSVReader(smbTemperatureFileEthernet, "heat");
                                                }

                                                temperature = csv_temperature.read();

                                                String[] rows = temperature.get(0);
                                                double minTemp = Double.parseDouble(rows[3]);
                                                for (int i = 0; i < temperature.size(); i++) {
                                                    String[] row = temperature.get(i);
                                                    if(Double.parseDouble(row[3]) < minTemp)
                                                    {
                                                        minTemp = Double.parseDouble(row[3]);
                                                    }
                                                }
                                                minimum = 0.0;
                                                minimum = Math.round(minTemp * Math.pow(10, 1)) / Math.pow(10, 1); //To round off to two decimal places.
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            }
                                        });
                                        thread.start();
                                    }
                                    catch(Exception e)
                                    {
                                        e.printStackTrace();
                                    }
                                    whilebool = true;
                                    while(whilebool) {
                                        if (!thread.isAlive()) {
                                            value =  Double.toString(minimum);
                                            toSpeak = "Minimum Temperature of the Solar Panels is " + value +  " Fahrenheit";
                                            textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                                            whilebool = false;
                                        }
                                    }
                                }
                                else if(Arrays.asList(keywords).contains("current"))
                                {
                                    jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
                                    try{
                                        //Creating a new thread for the file transfer, this takes the load off the main thread.
                                        thread = new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                            try
                                            {
                                                //To get Samba Shared file from the Raspberry Pi
                                                List<String[]> temperature;

                                                String url1 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/" + temperatureFileName;
                                                String url2 = "smb://" + ipAddressEthernet + "/" + sharedFolder + "/" + temperatureFileName;
                                                NtlmPasswordAuthentication auth1 = new NtlmPasswordAuthentication(domain, user, pass);
                                                InputStream smbTemperatureFileWireless;
                                                InputStream smbTemperatureFileEthernet;
                                                CSVReader csv_temperature;

                                                boolean wirelessFileAvailable = new SmbFile(url1, auth1).exists();
                                                if(wirelessFileAvailable) {
                                                    smbTemperatureFileWireless = new SmbFile(url1, auth1).getInputStream();
                                                    csv_temperature = new CSVReader(smbTemperatureFileWireless, "heat");
                                                }
                                                else {
                                                    smbTemperatureFileEthernet = new SmbFile(url2, auth1).getInputStream();
                                                    csv_temperature = new CSVReader(smbTemperatureFileEthernet, "heat");
                                                }

                                                temperature = csv_temperature.read();

                                                double currentTemp = 0.0;
                                                for (int i = 0; i < temperature.size(); i++) {
                                                    String[] row = temperature.get(i);
                                                    if( i == temperature.size() - 1)
                                                    {
                                                        currentTemp = Double.parseDouble(row[3]);
                                                    }
                                                }
                                                current = 0.0;
                                                current = Math.round(currentTemp * Math.pow(10, 1)) / Math.pow(10, 1); //To round off to two decimal places.
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            }
                                        });
                                        thread.start();
                                    }
                                    catch(Exception e)
                                    {
                                        e.printStackTrace();
                                    }
                                    whilebool = true;
                                    while(whilebool) {
                                        if (!thread.isAlive()) {
                                            value =  Double.toString(current);
                                            toSpeak = "Current Temperature of the Solar Panel is " + value + " Fahrenheit";
                                            textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                                            whilebool = false;
                                        }
                                    }
                                }
                            }
                            else if(Arrays.asList(keywords).contains("roof"))
                            {
                                if(Arrays.asList(keywords).contains("average") || Arrays.asList(keywords).contains("total"))
                                {
                                    jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
                                    try{
                                        //Creating a new thread for the file transfer, this takes the load off the main thread.
                                        thread = new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                            try
                                            {
                                                //To get Samba Shared file from the Raspberry Pi
                                                List<String[]> temperature;

                                                String url1 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/" + temperatureFileName;
                                                String url2 = "smb://" + ipAddressEthernet + "/" + sharedFolder + "/" + temperatureFileName;
                                                NtlmPasswordAuthentication auth1 = new NtlmPasswordAuthentication(domain, user, pass);
                                                InputStream smbTemperatureFileWireless;
                                                InputStream smbTemperatureFileEthernet;
                                                CSVReader csv_temperature;

                                                boolean wirelessFileAvailable = new SmbFile(url1, auth1).exists();
                                                if(wirelessFileAvailable) {
                                                    smbTemperatureFileWireless = new SmbFile(url1, auth1).getInputStream();
                                                    csv_temperature = new CSVReader(smbTemperatureFileWireless, "heat");
                                                }
                                                else {
                                                    smbTemperatureFileEthernet = new SmbFile(url2, auth1).getInputStream();
                                                    csv_temperature = new CSVReader(smbTemperatureFileEthernet, "heat");
                                                }

                                                temperature = csv_temperature.read();

                                                double sumOfTemp = 0.0;
                                                for (int i = 0; i < temperature.size(); i++) {
                                                    String[] rows = temperature.get(i);
                                                    sumOfTemp += Double.parseDouble(rows[4]);
                                                }
                                                double temp = sumOfTemp/(temperature.size());
                                                average = 0.0;
                                                average = Math.round(temp * Math.pow(10, 1)) / Math.pow(10, 1); //To round off to two decimal places.
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            }
                                        });
                                        thread.start();
                                    }
                                    catch(Exception e)
                                    {
                                        e.printStackTrace();
                                    }
                                    whilebool = true;
                                    while(whilebool) {
                                        if (!thread.isAlive()) {
                                            value =  Double.toString(average);
                                            toSpeak = "Average Temperature of the Roof is " + value + " Fahrenheit";
                                            textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                                            whilebool = false;
                                        }
                                    }
                                }
                                else if(Arrays.asList(keywords).contains("max") || Arrays.asList(keywords).contains("maximum"))
                                {
                                    jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
                                    try{
                                        //Creating a new thread for the file transfer, this takes the load off the main thread.
                                        thread = new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                            try
                                            {
                                                //To get Samba Shared file from the Raspberry Pi
                                                List<String[]> temperature;

                                                String url1 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/" + temperatureFileName;
                                                String url2 = "smb://" + ipAddressEthernet + "/" + sharedFolder + "/" + temperatureFileName;
                                                NtlmPasswordAuthentication auth1 = new NtlmPasswordAuthentication(domain, user, pass);
                                                InputStream smbTemperatureFileWireless;
                                                InputStream smbTemperatureFileEthernet;
                                                CSVReader csv_temperature;

                                                boolean wirelessFileAvailable = new SmbFile(url1, auth1).exists();
                                                if(wirelessFileAvailable) {
                                                    smbTemperatureFileWireless = new SmbFile(url1, auth1).getInputStream();
                                                    csv_temperature = new CSVReader(smbTemperatureFileWireless, "heat");
                                                }
                                                else {
                                                    smbTemperatureFileEthernet = new SmbFile(url2, auth1).getInputStream();
                                                    csv_temperature = new CSVReader(smbTemperatureFileEthernet, "heat");
                                                }

                                                temperature = csv_temperature.read();

                                                String[] rows = temperature.get(0);
                                                double maxTemp = Double.parseDouble(rows[4]);
                                                for (int i = 0; i < temperature.size(); i++) {
                                                    String[] row = temperature.get(i);
                                                    if(Double.parseDouble(row[4]) > maxTemp)
                                                    {
                                                        maxTemp = Double.parseDouble(row[4]);
                                                    }
                                                }
                                                maximum = 0.0;
                                                maximum = Math.round(maxTemp * Math.pow(10, 1)) / Math.pow(10, 1); //To round off to two decimal places.

                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            }
                                        });
                                        thread.start();
                                    }
                                    catch(Exception e)
                                    {
                                        e.printStackTrace();
                                    }
                                    whilebool = true;
                                    while(whilebool) {
                                        if (!thread.isAlive()) {
                                            value =  Double.toString(maximum);
                                            toSpeak = "Maximum Temperature of the roof is " + value + " Fahrenheit";
                                            textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                                            whilebool = false;
                                        }
                                    }
                                }
                                else if(Arrays.asList(keywords).contains("min") || Arrays.asList(keywords).contains("minimum"))
                                {
                                    jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
                                    try{
                                        //Creating a new thread for the file transfer, this takes the load off the main thread.
                                        thread = new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                            try
                                            {
                                                //To get Samba Shared file from the Raspberry Pi
                                                List<String[]> temperature;

                                                String url1 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/" + temperatureFileName;
                                                String url2 = "smb://" + ipAddressEthernet + "/" + sharedFolder + "/" + temperatureFileName;
                                                NtlmPasswordAuthentication auth1 = new NtlmPasswordAuthentication(domain, user, pass);
                                                InputStream smbTemperatureFileWireless;
                                                InputStream smbTemperatureFileEthernet;
                                                CSVReader csv_temperature;

                                                boolean wirelessFileAvailable = new SmbFile(url1, auth1).exists();
                                                if(wirelessFileAvailable) {
                                                    smbTemperatureFileWireless = new SmbFile(url1, auth1).getInputStream();
                                                    csv_temperature = new CSVReader(smbTemperatureFileWireless, "heat");
                                                }
                                                else {
                                                    smbTemperatureFileEthernet = new SmbFile(url2, auth1).getInputStream();
                                                    csv_temperature = new CSVReader(smbTemperatureFileEthernet, "heat");
                                                }

                                                temperature = csv_temperature.read();

                                                String[] rows = temperature.get(0);
                                                double minTemp = Double.parseDouble(rows[4]);
                                                for (int i = 0; i < temperature.size(); i++) {
                                                    String[] row = temperature.get(i);
                                                    if(Double.parseDouble(row[4]) < minTemp)
                                                    {
                                                        minTemp = Double.parseDouble(row[4]);
                                                    }
                                                }
                                                minimum = 0.0;
                                                minimum = Math.round(minTemp * Math.pow(10, 1)) / Math.pow(10, 1); //To round off to two decimal places.

                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            }
                                        });
                                        thread.start();
                                    }
                                    catch(Exception e)
                                    {
                                        e.printStackTrace();
                                    }
                                    whilebool = true;
                                    while(whilebool) {
                                        if (!thread.isAlive()) {
                                            value =  Double.toString(minimum);
                                            toSpeak = "Minimum Temperature of the roof is " + value + " Fahrenheit";
                                            textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                                            whilebool = false;
                                        }
                                    }
                                }
                                else if(Arrays.asList(keywords).contains("current"))
                                {
                                    jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
                                    try{
                                        //Creating a new thread for the file transfer, this takes the load off the main thread.
                                        thread = new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                            try
                                            {
                                                //To get Samba Shared file from the Raspberry Pi
                                                List<String[]> temperature;

                                                String url1 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/" + temperatureFileName;
                                                String url2 = "smb://" + ipAddressEthernet + "/" + sharedFolder + "/" + temperatureFileName;
                                                NtlmPasswordAuthentication auth1 = new NtlmPasswordAuthentication(domain, user, pass);
                                                InputStream smbTemperatureFileWireless;
                                                InputStream smbTemperatureFileEthernet;
                                                CSVReader csv_temperature;

                                                boolean wirelessFileAvailable = new SmbFile(url1, auth1).exists();
                                                if(wirelessFileAvailable) {
                                                    smbTemperatureFileWireless = new SmbFile(url1, auth1).getInputStream();
                                                    csv_temperature = new CSVReader(smbTemperatureFileWireless, "heat");
                                                }
                                                else {
                                                    smbTemperatureFileEthernet = new SmbFile(url2, auth1).getInputStream();
                                                    csv_temperature = new CSVReader(smbTemperatureFileEthernet, "heat");
                                                }

                                                temperature = csv_temperature.read();

                                                double currentTemp = 0.0;
                                                for (int i = 0; i < temperature.size(); i++) {
                                                    String[] row = temperature.get(i);
                                                    if( (i == temperature.size() - 1))
                                                    {
                                                        currentTemp = Double.parseDouble(row[4]);
                                                    }
                                                }
                                                current = 0.0;
                                                current = Math.round(currentTemp * Math.pow(10, 1)) / Math.pow(10, 1); //To round off to two decimal places.

                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            }
                                        });
                                        thread.start();
                                    }
                                    catch(Exception e)
                                    {
                                        e.printStackTrace();
                                    }
                                    whilebool = true;
                                    while(whilebool) {
                                        if (!thread.isAlive()) {
                                            value =  Double.toString(current);
                                            toSpeak = "Current Temperature of the roof is " + value + " Fahrenheit";
                                            textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                                            whilebool = false;
                                        }
                                    }
                                }
                            }
                            else if(Arrays.asList(keywords).contains("north"))
                            {
                                if(Arrays.asList(keywords).contains("average") || Arrays.asList(keywords).contains("total"))
                                {
                                    jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
                                    try{
                                        //Creating a new thread for the file transfer, this takes the load off the main thread.
                                        thread = new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                            try
                                            {
                                                //To get Samba Shared file from the Raspberry Pi
                                                List<String[]> temperature;

                                                String url1 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/" + temperatureFileName;
                                                String url2 = "smb://" + ipAddressEthernet + "/" + sharedFolder + "/" + temperatureFileName;
                                                NtlmPasswordAuthentication auth1 = new NtlmPasswordAuthentication(domain, user, pass);
                                                InputStream smbTemperatureFileWireless;
                                                InputStream smbTemperatureFileEthernet;
                                                CSVReader csv_temperature;

                                                boolean wirelessFileAvailable = new SmbFile(url1, auth1).exists();
                                                if(wirelessFileAvailable) {
                                                    smbTemperatureFileWireless = new SmbFile(url1, auth1).getInputStream();
                                                    csv_temperature = new CSVReader(smbTemperatureFileWireless, "heat");
                                                }
                                                else {
                                                    smbTemperatureFileEthernet = new SmbFile(url2, auth1).getInputStream();
                                                    csv_temperature = new CSVReader(smbTemperatureFileEthernet, "heat");
                                                }

                                                temperature = csv_temperature.read();
                                                double sumOfTemp = 0.0;
                                                for (int i = 0; i < temperature.size(); i++) {
                                                    String[] rows = temperature.get(i);
                                                    sumOfTemp += Double.parseDouble(rows[6]);
                                                }
                                                double temp = sumOfTemp/(temperature.size());
                                                average = 0.0;
                                                average = Math.round(temp * Math.pow(10, 1)) / Math.pow(10, 1); //To round off to two decimal places.
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            }
                                        });
                                        thread.start();
                                    }
                                    catch(Exception e)
                                    {
                                        e.printStackTrace();
                                    }
                                    whilebool = true;
                                    while(whilebool) {
                                        if (!thread.isAlive()) {
                                            value =  Double.toString(average);
                                            toSpeak = "Average Temperature of the North Wall is " + value + " Fahrenheit";
                                            textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                                            whilebool = false;
                                        }
                                    }
                                }
                                else if(Arrays.asList(keywords).contains("max") || Arrays.asList(keywords).contains("maximum"))
                                {
                                    jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
                                    try{
                                        //Creating a new thread for the file transfer, this takes the load off the main thread.
                                        thread = new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                            try
                                            {
                                                //To get Samba Shared file from the Raspberry Pi
                                                List<String[]> temperature;

                                                String url1 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/" + temperatureFileName;
                                                String url2 = "smb://" + ipAddressEthernet + "/" + sharedFolder + "/" + temperatureFileName;
                                                NtlmPasswordAuthentication auth1 = new NtlmPasswordAuthentication(domain, user, pass);
                                                InputStream smbTemperatureFileWireless;
                                                InputStream smbTemperatureFileEthernet;
                                                CSVReader csv_temperature;

                                                boolean wirelessFileAvailable = new SmbFile(url1, auth1).exists();
                                                if(wirelessFileAvailable) {
                                                    smbTemperatureFileWireless = new SmbFile(url1, auth1).getInputStream();
                                                    csv_temperature = new CSVReader(smbTemperatureFileWireless, "heat");
                                                }
                                                else {
                                                    smbTemperatureFileEthernet = new SmbFile(url2, auth1).getInputStream();
                                                    csv_temperature = new CSVReader(smbTemperatureFileEthernet, "heat");
                                                }

                                                temperature = csv_temperature.read();

                                                String[] rows = temperature.get(0);
                                                double maxTemp = Double.parseDouble(rows[6]);
                                                for (int i = 0; i < temperature.size(); i++) {
                                                    String[] row = temperature.get(i);
                                                    if(Double.parseDouble(row[6]) > maxTemp)
                                                    {
                                                        maxTemp = Double.parseDouble(row[6]);
                                                    }
                                                }
                                                maximum = 0.0;
                                                maximum = Math.round(maxTemp * Math.pow(10, 1)) / Math.pow(10, 1); //To round off to two decimal places.

                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            }
                                        });
                                        thread.start();
                                    }
                                    catch(Exception e)
                                    {
                                        e.printStackTrace();
                                    }
                                    whilebool = true;
                                    while(whilebool) {
                                        if (!thread.isAlive()) {
                                            value =  Double.toString(maximum);
                                            toSpeak = "Maximum Temperature of the North Wall is " + value + " Fahrenheit";
                                            textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                                            whilebool = false;
                                        }
                                    }
                                }
                                else if(Arrays.asList(keywords).contains("min") || Arrays.asList(keywords).contains("minimum"))
                                {
                                    jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
                                    try{
                                        //Creating a new thread for the file transfer, this takes the load off the main thread.
                                        thread = new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                            try
                                            {
                                                //To get Samba Shared file from the Raspberry Pi
                                                List<String[]> temperature;

                                                String url1 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/" + temperatureFileName;
                                                String url2 = "smb://" + ipAddressEthernet + "/" + sharedFolder + "/" + temperatureFileName;
                                                NtlmPasswordAuthentication auth1 = new NtlmPasswordAuthentication(domain, user, pass);
                                                InputStream smbTemperatureFileWireless;
                                                InputStream smbTemperatureFileEthernet;
                                                CSVReader csv_temperature;

                                                boolean wirelessFileAvailable = new SmbFile(url1, auth1).exists();
                                                if(wirelessFileAvailable) {
                                                    smbTemperatureFileWireless = new SmbFile(url1, auth1).getInputStream();
                                                    csv_temperature = new CSVReader(smbTemperatureFileWireless, "heat");
                                                }
                                                else {
                                                    smbTemperatureFileEthernet = new SmbFile(url2, auth1).getInputStream();
                                                    csv_temperature = new CSVReader(smbTemperatureFileEthernet, "heat");
                                                }

                                                temperature = csv_temperature.read();

                                                String[] rows = temperature.get(0);
                                                double minTemp = Double.parseDouble(rows[6]);
                                                for (int i = 0; i < temperature.size(); i++) {
                                                    String[] row = temperature.get(i);
                                                    if(Double.parseDouble(row[6]) < minTemp)
                                                    {
                                                        minTemp = Double.parseDouble(row[6]);
                                                    }
                                                }
                                                minimum = 0.0;
                                                minimum = Math.round(minTemp * Math.pow(10, 1)) / Math.pow(10, 1); //To round off to two decimal places.

                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            }
                                        });
                                        thread.start();
                                    }
                                    catch(Exception e)
                                    {
                                        e.printStackTrace();
                                    }
                                    whilebool = true;
                                    while(whilebool) {
                                        if (!thread.isAlive()) {
                                            value =  Double.toString(minimum);
                                            toSpeak = "Minimum Temperature of the North Wall is " + value + " Fahrenheit";
                                            textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                                            whilebool = false;
                                        }
                                    }
                                }
                                else if(Arrays.asList(keywords).contains("current"))
                                {
                                    jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
                                    try{
                                        //Creating a new thread for the file transfer, this takes the load off the main thread.
                                        thread = new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                            try
                                            {
                                                //To get Samba Shared file from the Raspberry Pi
                                                List<String[]> temperature;

                                                String url1 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/" + temperatureFileName;
                                                String url2 = "smb://" + ipAddressEthernet + "/" + sharedFolder + "/" + temperatureFileName;
                                                NtlmPasswordAuthentication auth1 = new NtlmPasswordAuthentication(domain, user, pass);
                                                InputStream smbTemperatureFileWireless;
                                                InputStream smbTemperatureFileEthernet;
                                                CSVReader csv_temperature;

                                                boolean wirelessFileAvailable = new SmbFile(url1, auth1).exists();
                                                if(wirelessFileAvailable) {
                                                    smbTemperatureFileWireless = new SmbFile(url1, auth1).getInputStream();
                                                    csv_temperature = new CSVReader(smbTemperatureFileWireless, "heat");
                                                }
                                                else {
                                                    smbTemperatureFileEthernet = new SmbFile(url2, auth1).getInputStream();
                                                    csv_temperature = new CSVReader(smbTemperatureFileEthernet, "heat");
                                                }

                                                temperature = csv_temperature.read();

                                                double currentTemp = 0.0;
                                                for (int i = 0; i < temperature.size(); i++) {
                                                    String[] row = temperature.get(i);
                                                    if( i == temperature.size() - 1)
                                                    {
                                                        currentTemp = Double.parseDouble(row[6]);
                                                    }
                                                }
                                                current = 0.0;
                                                current = Math.round(currentTemp * Math.pow(10, 1)) / Math.pow(10, 1); //To round off to two decimal places.
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            }
                                        });
                                        thread.start();
                                    }
                                    catch(Exception e)
                                    {
                                        e.printStackTrace();
                                    }
                                    whilebool = true;
                                    while(whilebool) {
                                        if (!thread.isAlive()) {
                                            value =  Double.toString(current);
                                            toSpeak = "Current Temperature of the North Wall is " + value + " Fahrenheit";
                                            textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                                            whilebool = false;
                                        }
                                    }
                                }
                            }
                        }
                        else if(Arrays.asList(keywords).contains("water"))
                        {
                            if(Arrays.asList(keywords).contains("usage")
                                    || Arrays.asList(keywords).contains("use")
                                    || Arrays.asList(keywords).contains("used")
                                    || Arrays.asList(keywords).contains("consumption")
                                    || Arrays.asList(keywords).contains("consumed"))
                            {
                                if(Arrays.asList(keywords).contains("outlet")
                                        || Arrays.asList(keywords).contains("sensor")
                                        || Arrays.asList(keywords).contains("used"))
                                {
                                    if(Arrays.asList(keywords).contains("one")
                                            || Arrays.asList(keywords).contains("1"))
                                    {
                                        if(Arrays.asList(keywords).contains("total") || Arrays.asList(keywords).contains("average")
                                                || Arrays.asList(keywords).contains("max") || Arrays.asList(keywords).contains("maximum")
                                                || Arrays.asList(keywords).contains("min") || Arrays.asList(keywords).contains("minimum")
                                                || Arrays.asList(keywords).contains("current"))
                                        {
                                            jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
                                            try{
                                                //Creating a new thread for the file transfer, this takes the load off the main thread.
                                                thread = new Thread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        try
                                                        {
                                                            //To get Samba Shared file from the Raspberry Pi
                                                            List<String[]> water;

                                                            String url1 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/" + water1FileName;
                                                            String url2 = "smb://" + ipAddressEthernet + "/" + sharedFolder + "/" + water1FileName;
                                                            NtlmPasswordAuthentication auth1 = new NtlmPasswordAuthentication(domain, user, pass);
                                                            InputStream smbWaterFileWireless;
                                                            InputStream smbWaterFileEthernet;
                                                            CSVReader csv_water;

                                                            boolean wirelessFileAvailable = new SmbFile(url1, auth1).exists();
                                                            if(wirelessFileAvailable) {
                                                                smbWaterFileWireless = new SmbFile(url1, auth1).getInputStream();
                                                                csv_water = new CSVReader(smbWaterFileWireless, "water");
                                                            }
                                                            else {
                                                                smbWaterFileEthernet = new SmbFile(url2, auth1).getInputStream();
                                                                csv_water = new CSVReader(smbWaterFileEthernet, "water");
                                                            }

                                                            water = csv_water.read();

                                                            double totalWater = 0.0;
                                                            for (int i = 0; i < water.size(); i++) {
                                                                String[] row = water.get(i);
                                                                totalWater += Double.parseDouble(row[2])*0.264172;
                                                            }
                                                            total = 0.0;
                                                            total = Math.round(totalWater * Math.pow(10, 1)) / Math.pow(10, 1); //To round off to two decimal places.
                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                });
                                                thread.start();
                                            }
                                            catch(Exception e)
                                            {
                                                e.printStackTrace();
                                            }
                                            whilebool = true;
                                            while(whilebool) {
                                                if (!thread.isAlive()) {
                                                    value =  Double.toString(current);
                                                    toSpeak = "Total Water Consumption for sensor 1 is " + value + " Gallons";
                                                    textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                                                    whilebool = false;
                                                }
                                            }
                                        }
                                    }
                                    else if(Arrays.asList(keywords).contains("two")
                                            || Arrays.asList(keywords).contains("2")
                                            || Arrays.asList(keywords).contains("too"))
                                    {
                                        if(Arrays.asList(keywords).contains("total") || Arrays.asList(keywords).contains("average")
                                                || Arrays.asList(keywords).contains("max") || Arrays.asList(keywords).contains("maximum")
                                                || Arrays.asList(keywords).contains("min") || Arrays.asList(keywords).contains("minimum")
                                                || Arrays.asList(keywords).contains("current"))
                                        {
                                            jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
                                            try{
                                                //Creating a new thread for the file transfer, this takes the load off the main thread.
                                                thread = new Thread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        try
                                                        {
                                                            //To get Samba Shared file from the Raspberry Pi
                                                            List<String[]> water;

                                                            String url1 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/" + water2FileName;
                                                            String url2 = "smb://" + ipAddressEthernet + "/" + sharedFolder + "/" + water2FileName;
                                                            NtlmPasswordAuthentication auth1 = new NtlmPasswordAuthentication(domain, user, pass);
                                                            InputStream smbWaterFileWireless;
                                                            InputStream smbWaterFileEthernet;
                                                            CSVReader csv_water;

                                                            boolean wirelessFileAvailable = new SmbFile(url1, auth1).exists();
                                                            if(wirelessFileAvailable) {
                                                                smbWaterFileWireless = new SmbFile(url1, auth1).getInputStream();
                                                                csv_water = new CSVReader(smbWaterFileWireless, "water");
                                                            }
                                                            else {
                                                                smbWaterFileEthernet = new SmbFile(url2, auth1).getInputStream();
                                                                csv_water = new CSVReader(smbWaterFileEthernet, "water");
                                                            }

                                                            water = csv_water.read();

                                                            double totalWater = 0.0;
                                                            for (int i = 0; i < water.size(); i++) {
                                                                String[] row = water.get(i);
                                                                totalWater += Double.parseDouble(row[2])*0.264172;
                                                            }
                                                            total = 0.0;
                                                            total = Math.round(totalWater * Math.pow(10, 1)) / Math.pow(10, 1); //To round off to two decimal places.
                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                });
                                                thread.start();
                                            }
                                            catch(Exception e)
                                            {
                                                e.printStackTrace();
                                            }
                                            whilebool = true;
                                            while(whilebool) {
                                                if (!thread.isAlive()) {
                                                    value =  Double.toString(current);
                                                    toSpeak = "Total Water Consumption for sensor 2 is " + value + " Gallons";
                                                    textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                                                    whilebool = false;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        else if(Arrays.asList(keywords).contains("humidity"))
                        {
                            if(Arrays.asList(keywords).contains("current") || Arrays.asList(keywords).contains("total")
                                    || Arrays.asList(keywords).contains("average")
                                    || Arrays.asList(keywords).contains("max") || Arrays.asList(keywords).contains("maximum")
                                    || Arrays.asList(keywords).contains("min") || Arrays.asList(keywords).contains("minimum"))
                            {
                                jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
                                try{
                                    //Creating a new thread for the file transfer, this takes the load off the main thread.
                                    thread = new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try
                                            {
                                                //To get Samba Shared file from the Raspberry Pi
                                                List<String[]> humidity;

                                                String url1 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/" + humidityFileName;
                                                String url2 = "smb://" + ipAddressEthernet + "/" + sharedFolder + "/" + humidityFileName;
                                                NtlmPasswordAuthentication auth1 = new NtlmPasswordAuthentication(domain, user, pass);
                                                InputStream smbHumidityFileWireless;
                                                InputStream smbHumidityFileEthernet;
                                                CSVReader csv_humidity;

                                                boolean wirelessFileAvailable = new SmbFile(url1, auth1).exists();
                                                if(wirelessFileAvailable) {
                                                    smbHumidityFileWireless = new SmbFile(url1, auth1).getInputStream();
                                                    csv_humidity = new CSVReader(smbHumidityFileWireless, "humidity");
                                                }
                                                else {
                                                    smbHumidityFileEthernet = new SmbFile(url2, auth1).getInputStream();
                                                    csv_humidity = new CSVReader(smbHumidityFileEthernet, "humidity");
                                                }

                                                humidity = csv_humidity.read();

                                                double currentHumidity = 0.0;
                                                for (int i = 0; i < humidity.size(); i++) {
                                                    String[] row = humidity.get(i);
                                                    if( i == humidity.size() - 1)
                                                    {
                                                        currentHumidity = Double.parseDouble(row[2]);
                                                    }
                                                }
                                                current = 0.0;
                                                current = Math.round(currentHumidity * Math.pow(10, 1)) / Math.pow(10, 1); //To round off to two decimal places.
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                    thread.start();
                                }
                                catch(Exception e)
                                {
                                    e.printStackTrace();
                                }
                                whilebool = true;
                                while(whilebool) {
                                    if (!thread.isAlive()) {
                                        value =  Double.toString(current);
                                        toSpeak = "Current Humidity inside the house is " + value + " Percent";
                                        textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                                        whilebool = false;
                                    }
                                }
                            }
                        }
                    }
                    else if(Arrays.asList(keywords).contains("nomajor") &&
                                   Arrays.asList(keywords).contains("house") ||
                                    Arrays.asList(keywords).contains("welcome") ||
                                    Arrays.asList(keywords).contains("info") ||
                                    Arrays.asList(keywords).contains("information") ||
                                    Arrays.asList(keywords).contains("intro") ||
                                    Arrays.asList(keywords).contains("introduction"))
                    {
                        if(Arrays.asList(keywords).contains("house") &&
                                (Arrays.asList(keywords).contains("info") || Arrays.asList(keywords).contains("information") ||
                                        Arrays.asList(keywords).contains("intro") || Arrays.asList(keywords).contains("introduction")) &&
                                !Arrays.asList(keywords).contains("welcome"))
                        {
                            jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
                            try{
                                //Creating a new thread for the file transfer, this takes the load off the main thread.
                                thread = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                    try
                                    {
                                        toSpeak = "";
                                        String url1 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/" + houseInfo;
                                        String url2 = "smb://" + ipAddressEthernet + "/" + sharedFolder + "/" + houseInfo;
                                        NtlmPasswordAuthentication auth1 = new NtlmPasswordAuthentication(domain, user, pass);
                                        SmbFile houseInfo;

                                        boolean wirelessFileAvailable = new SmbFile(url1, auth1).exists();
                                        if(wirelessFileAvailable) {
                                            houseInfo  = new SmbFile(url1, auth1);
                                        }
                                        else {
                                            houseInfo = new SmbFile(url2, auth1);
                                        }

                                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new SmbFileInputStream(houseInfo)))) {
                                            String line = reader.readLine();
                                            while (line != null) {
                                                toSpeak += line;
                                                line = reader.readLine();
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    }
                                });
                                thread.start();
                            }
                            catch(Exception e)
                            {
                                e.printStackTrace();
                            }

                            whilebool = true;
                            while(whilebool) {
                                if (!thread.isAlive()) {
                                    textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                                    whilebool = false;
                                }
                            }
                        }
                        else if(Arrays.asList(keywords).contains("welcome"))
                        {
                            jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
                            try{
                                //Creating a new thread for the file transfer, this takes the load off the main thread.
                                thread = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                    try
                                    {
                                        toSpeak = "";
                                        String url1 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/" + houseWelcome;
                                        String url2 = "smb://" + ipAddressEthernet + "/" + sharedFolder + "/" + houseWelcome;
                                        NtlmPasswordAuthentication auth1 = new NtlmPasswordAuthentication(domain, user, pass);
                                        SmbFile houseInfo;

                                        boolean wirelessFileAvailable = new SmbFile(url1, auth1).exists();
                                        if(wirelessFileAvailable) {
                                            houseInfo  = new SmbFile(url1, auth1);
                                        }
                                        else {
                                            houseInfo = new SmbFile(url2, auth1);
                                        }
                                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new SmbFileInputStream(houseInfo)))) {
                                            String line = reader.readLine();
                                            while (line != null) {
                                                toSpeak += line;
                                                line = reader.readLine();
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    }
                                });
                                thread.start();
                            }
                            catch(Exception e)
                            {
                                e.printStackTrace();
                            }

                            whilebool = true;
                            while(whilebool) {
                                if (!thread.isAlive()) {
                                    textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                                    whilebool = false;
                                }
                            }
                        }
                    }

                    if(!textToSpeech.isSpeaking()) {
                        textToSpeech.stop();
                        textToSpeech.shutdown();
                    }
                }
                break;
            }
        }
    }

    protected String[] keywordSearch(String[] words){
        int count = 0;  //Serves as index for returning keywords Array.
        int tempCount;  //Serves as index for other arrays.

        // Arrays Related to the Solar Decathlon House
        String house[] = {"house", "welcome", "info", "information", "intro", "introduction"};

        // Arrays Related to the Senior Year Design
        String major[]       = {"power", "temperature", "water", "humidity"};
        String quantity[]    = {"total", "average", "max", "maximum", "min", "minimum", "current"};
        String power[]       = {"lighting", "air", "conditioner", "refrigerator", "kitchen", "radiant",
                                "floor", "pump"};
        String temperature[] = {"south", "wall", "interior", "inside", "solar", "panel", "roof", "exterior",
                                "outside", "north"};
        String water[]       = {"usage", "use", "used", "consumption", "consumed", "outlet", "sensor",
                                "one", "1", "two", "2", "too"};

        // Arrays to store various keywords.
        String[] keywords           = new String[(words.length + 7)];
        String[] houseKeywords      = new String[house.length];
        String[] majorKeywords      = new String[major.length];
        String[] quantityKeywords   = new String[quantity.length];

        // Compare the spoken string with the quantity keywords. Store the keyword in an array.
        tempCount = 0;
        for(int i = 0; i < quantity.length; i++) {
            if(Arrays.asList(words).contains(quantity[i])) {
                keywords[count] = quantity[i];
                quantityKeywords[tempCount] = quantity[i];
                count++;
                tempCount++;
            }
        }

        /* In case, we did not have any match with quantity array then
         * we will assume that we are talking about 'total'.
         */
        if(0 == tempCount)
        {
            keywords[count] = "total";
            quantityKeywords[tempCount] = "total";
            count++;
        }

        // Compare the spoken string with the major keywords. Store the keyword in an array.
        tempCount = 0;
        for(int i = 0; i < major.length; i++)
        {
            if(Arrays.asList(words).contains(major[i]))
            {
                keywords[count] = major[i];
                majorKeywords[tempCount] = major[i];
                count++;
                tempCount++;
            }
        }
        /* In case, we did not have any match with major array then
         * we will ask user to use a major keyword.
         */
        if(0 == tempCount)
        {
            keywords[count] = "nomajor";
            majorKeywords[tempCount] = "nomajor";
            count++;
        }

        // Compare the spoken string with the house keywords. Store the keyword in an array.
        tempCount = 0;
        for(int i = 0; i < house.length; i++)
        {
            if(Arrays.asList(words).contains(house[i]))
            {
                keywords[count] = house[i];
                houseKeywords[tempCount] = house[i];
                count++;
                tempCount++;
            }
        }
        /* In case, we did not have any match with house array then
         */
        if(0 == tempCount)
        {
            keywords[count] = "nohouse";
            houseKeywords[tempCount] = "nohouse";
            count++;
        }

        /* The next section is to find more keywords.
         * 1. The 'if' statement works with sensor related keywords.
         * 2. When we talk about the major section, we need to follow some rules.
         *      I. Users can only ask for one major type of sensors at a time.
         *      II. When it comes to 'water', it is a special keyword. It can be a major or a sensor name.
         *          a. Water by itself is a major sensor category.
         *          b. Water can not be combined with anything related to production.
         *          c. Water with heater is power consumption sensor.
         *          d. Water with tank is a temperature sensor.
         *          e. With with solar collector is a temperature sensor.
         */
        if(!(Arrays.asList(majorKeywords).contains("nomajor")))
        {
            if (Arrays.asList(majorKeywords).contains("power"))
            {
                if(Arrays.asList(majorKeywords).contains("temperature") ||
                        Arrays.asList(majorKeywords).contains("humidity"))
                {
                    keywords[count] = "twomajors";
                    count++;
                    return keywords;
                }
                else if (Arrays.asList(majorKeywords).contains("water"))
                {
                    if(Arrays.asList(words).contains("heater") &&
                            !(Arrays.asList(words).contains("production") ||
                                    Arrays.asList(words).contains("produced")))
                    {
                        keywords[count] = "waterheater";
                        count++;
                        return keywords;
                    }
                    else
                    {
                        keywords[count] = "twomajors";
                        count++;
                        return keywords;
                    }
                }
                else
                {
                    if(Arrays.asList(words).contains("production") || Arrays.asList(words).contains("produced"))
                    {
                        keywords[count] = "production";
                        count++;
                        return keywords;
                    }
                    else if(Arrays.asList(words).contains("consumption") || Arrays.asList(words).contains("consumed"))
                    {
                        keywords[count] = "consumption";
                        count++;

                        for(int i = 0; i < power.length; i++)
                        {
                            if(Arrays.asList(words).contains(power[i])) {
                                keywords[count] = power[i];
                                count++;
                            }
                        }

                        return keywords;
                    }
                }
            }
            else if (Arrays.asList(majorKeywords).contains("temperature"))
            {
                if(Arrays.asList(majorKeywords).contains("power") ||
                        Arrays.asList(majorKeywords).contains("humidity"))
                {
                    keywords[count] = "twomajors";
                    count++;
                    return keywords;
                }
                else if(Arrays.asList(majorKeywords).contains("water"))
                {
                    if(Arrays.asList(words).contains("tank"))
                    {
                        keywords[count] = "watertank";
                        count++;
                        return keywords;
                    }
                    else if(Arrays.asList(words).contains("solar") && Arrays.asList(words).contains("collector"))
                    {
                        keywords[count] = "watersolarcollector";
                        count++;
                        return keywords;
                    }
                    else
                    {
                        keywords[count] = "twomajors";
                        count++;
                        return keywords;
                    }
                }
                else
                {
                    for(int i = 0; i < temperature.length; i++) {
                        String tempString = temperature[i];
                        if(Arrays.asList(words).contains(tempString)) {
                            keywords[count] = tempString;
                            count++;
                        }
                    }
                }
            }
            else if (majorKeywords[0].equalsIgnoreCase("water")
                    && !(Arrays.asList(keywords).contains("watertank")
                    || Arrays.asList(keywords).contains("waterheater")
                    || Arrays.asList(keywords).contains("watersolarcollector")))
            {
                if(Arrays.asList(majorKeywords).contains("power") ||
                        Arrays.asList(majorKeywords).contains("humidity") ||
                        Arrays.asList(majorKeywords).contains("temperature"))
                {
                    keywords[count] = "twomajors";
                    count++;
                    return keywords;
                }
                else
                {
                    for(int i = 0; i < water.length; i++) {
                        if(Arrays.asList(words).contains(water[i])) {
                            keywords[count] = water[i];
                            count++;
                        }
                    }
                }
            }
            else if (Arrays.asList(majorKeywords).contains("humidity")) {
                if(Arrays.asList(majorKeywords).contains("power") ||
                        Arrays.asList(majorKeywords).contains("water") ||
                        Arrays.asList(majorKeywords).contains("temperature"))
                {
                    keywords[count] = "twomajors";
                    count++;
                    return keywords;
                }
                else
                {
                    keywords[count] = "humidity";
                    count++;
                }
            }
        }
        return keywords;
    }
}

