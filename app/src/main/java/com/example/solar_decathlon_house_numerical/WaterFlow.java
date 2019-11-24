package com.example.solar_decathlon_house_numerical;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;

public class WaterFlow extends AppCompatActivity {
    Button refresh;
    String water1FileName = "water1app.csv";
    String water2FileName = "water2app.csv";

    String units = " Gallons";
    String string;
    boolean while_boolean = true;
    Thread threadSensor1 = new Thread();
    Thread threadSensor2 = new Thread();

    TextView totalTextView, totalSensor1TextView, totalSensor2TextView;
    Double totalWaterUsage = 0.0;
    Double totalSensor1WaterUsage = 0.0;
    Double totalSensor2WaterUsage = 0.0;

    String totalWaterUsage1;
    String totalSensor1WaterUsage1;
    String totalSensor2WaterUsage1;

    String user = "rpihubteam6";    //Samba User name
    String pass = "raspberrypi";    //Samba Password
    String sharedFolder = "share";  //Samba Shared folder
    String domain = "rpihubteam6";  //Samba domain name
    String ipAddressWireless = "192.168.1.10"; //IP address for rpihubteam6 when it is wirelessly connected with the router
    String ipAddressEthernet = "192.168.1.11"; //IP address for rpihubteam6 when it is connected with the router via ethernet

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_water_flow);

        totalTextView = findViewById(R.id.water_flow_total_value);
        totalSensor1TextView = findViewById(R.id.water_flow_sensor1_total_value);
        totalSensor2TextView = findViewById(R.id.water_flow_sensor2_total_value);

        this.refresh = findViewById(R.id.one_for_all);
        refresh.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
                try {
                    //Creating a new thread for the file transfer, this takes the load off the main thread.
                    threadSensor1 = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                //To get Samba Shared file from the Raspberry Pi
                                String url1 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/" + water1FileName;
                                String url2 = "smb://" + ipAddressEthernet + "/" + sharedFolder + "/" + water1FileName;

                                NtlmPasswordAuthentication auth1 = new NtlmPasswordAuthentication(domain, user, pass);
                                InputStream smbWater1FileWireless;
                                InputStream smbWater1FileEthernet;
                                CSVReader csv_water1app;

                                boolean wirelessFileAvailable = new SmbFile(url1, auth1).exists();
                                if(wirelessFileAvailable) {
                                    smbWater1FileWireless = new SmbFile(url1, auth1).getInputStream();
                                    csv_water1app = new CSVReader(smbWater1FileWireless, "water");
                                }
                                else {
                                    smbWater1FileEthernet = new SmbFile(url2, auth1).getInputStream();
                                    csv_water1app = new CSVReader(smbWater1FileEthernet, "water");
                                }

                                List<String[]> waterDataSensor1 = csv_water1app.read();

                                final DateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");

                                double totalSensor1WaterSum = 0.0;

                                int latestDataSensor1 = waterDataSensor1.size() - 1; //To get the last row's #
                                String[] latestRowSensor1 = waterDataSensor1.get(latestDataSensor1); //To get data from the last row
                                Date dateSensor1 = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy").parse(latestRowSensor1[0]); //Extract date
                                String latestDateSensor1 = df.format(dateSensor1); //Date to String
                                String latestDateSubstringSensor1 = latestDateSensor1.substring(0, 9);    //Extract "EEE MMM dd" part to compare

                                for (int i = 0; i < waterDataSensor1.size(); i++) {
                                    String[] rows = waterDataSensor1.get(i);

                                    dateSensor1 = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy").parse(rows[0]); //Extract date
                                    String dateFoundSensor1 = df.format(dateSensor1); //Date to String
                                    String dateFoundSubstringSensor1 = dateFoundSensor1.substring(0, 9);    //Extract "EEE MMM dd" part to compare

                                    //Only get the data if the day is the same.
                                    if(latestDateSubstringSensor1.equalsIgnoreCase(dateFoundSubstringSensor1)) {
                                        totalSensor1WaterSum += Double.parseDouble(rows[1]);
                                    }
                                }

                                totalSensor1WaterUsage = Math.round(totalSensor1WaterSum * Math.pow(10, 1)) / Math.pow(10, 1);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    threadSensor2 = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                //To get Samba Shared file from the Raspberry Pi
                                String url3 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/" + water2FileName;
                                String url4 = "smb://" + ipAddressEthernet + "/" + sharedFolder + "/" + water2FileName;

                                NtlmPasswordAuthentication auth1 = new NtlmPasswordAuthentication(domain, user, pass);
                                InputStream smbWater2FileWireless;
                                InputStream smbWater2FileEthernet;
                                CSVReader csv_water2app;

                                boolean wirelessFileAvailable = new SmbFile(url3, auth1).exists();
                                if(wirelessFileAvailable) {
                                    smbWater2FileWireless = new SmbFile(url3, auth1).getInputStream();
                                    csv_water2app = new CSVReader(smbWater2FileWireless, "water");
                                }
                                else {
                                    smbWater2FileEthernet = new SmbFile(url4, auth1).getInputStream();
                                    csv_water2app = new CSVReader(smbWater2FileEthernet, "water");
                                }

                                List<String[]> waterDataSensor2 = csv_water2app.read();

                                final DateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");

                                double totalSensor2WaterSum = 0.0;

                                int latestDataSensor2 = waterDataSensor2.size() - 1; //To get the last row's #
                                String[] latestRowSensor2 = waterDataSensor2.get(latestDataSensor2); //To get data from the last row
                                Date dateSensor2 = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy").parse(latestRowSensor2[0]); //Extract date
                                String latestDateSensor2 = df.format(dateSensor2); //Date to String
                                String latestDateSubstringSensor2 = latestDateSensor2.substring(0, 9);    //Extract "EEE MMM dd" part to compare

                                for (int i = 0; i < waterDataSensor2.size(); i++) {
                                    String[] rows = waterDataSensor2.get(i);

                                    dateSensor2 = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy").parse(rows[0]); //Extract date
                                    String dateFoundSensor2 = df.format(dateSensor2); //Date to String
                                    String dateFoundSubstringSensor2 = dateFoundSensor2.substring(0, 9);    //Extract "EEE MMM dd" part to compare

                                    //Only get the data if the day is the same.
                                    if(latestDateSubstringSensor2.equalsIgnoreCase(dateFoundSubstringSensor2)) {
                                        totalSensor2WaterSum += Double.parseDouble(rows[1]);
                                    }
                                }

                                totalSensor2WaterUsage = Math.round(totalSensor2WaterSum * Math.pow(10, 1)) / Math.pow(10, 1);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    threadSensor1.start();
                    threadSensor2.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                while(while_boolean) {
                    if (!threadSensor1.isAlive() && !threadSensor2.isAlive()) {
                        totalWaterUsage = totalSensor1WaterUsage + totalSensor2WaterUsage;
                        totalSensor1WaterUsage1 = Double.toString(totalSensor1WaterUsage);
                        string = totalSensor1WaterUsage1 + units;
                        totalSensor1TextView.setText(string);

                        totalSensor2WaterUsage1 = Double.toString(totalSensor2WaterUsage);
                        string = totalSensor2WaterUsage1 + units;
                        totalSensor2TextView.setText(string);

                        totalWaterUsage1 = Double.toString(totalWaterUsage);
                        string = totalWaterUsage1 + units;
                        totalTextView.setText(string);

                        while_boolean = false;
                    }
                }

            }
        });
    }
}