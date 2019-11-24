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

public class Power extends AppCompatActivity {
    Button refresh;
    TextView totalTextView1, totalTextView2, totalTextView3, totalTextView4,
            totalTextView5, totalTextView6, totalTextView7, totalTextView8;
    TextView instantaneousTextView1, instantaneousTextView2, instantaneousTextView3,
            instantaneousTextView4, instantaneousTextView5, instantaneousTextView6,
            instantaneousTextView7, instantaneousTextView8;

    double totalPowerProduction = 0.0;
    double totalPowerConsumption = 0.0;
    double totalLighting = 0.0;
    double totalAirConditioner = 0.0;
    double totalWaterHeater = 0.0;
    double totalRefrigerator = 0.0;
    double totalKitchenOutlet = 0.0;
    double totalRadiantFloorPump = 0.0;

    double instantaneousPowerProduction = 0.0;
    double instantaneousPowerConsumption = 0.0;
    double instantaneousLighting = 0.0;
    double instantaneousAirConditioner = 0.0;
    double instantaneousWaterHeater = 0.0;
    double instantaneousRefrigerator = 0.0;
    double instantaneousKitchenOutlet = 0.0;
    double instantaneousRadiantFloorPump = 0.0;

    String units = " KW/hr";
    String totalPowerProduction1, totalLighting1, totalAirConditioner1,
            totalWaterHeater1, totalRefrigerator1, totalKitchenOutlet1,
            totalRadiantFloorPump1, totalPowerConsumption1;
    String instantaneousPowerProduction1, instantaneousLighting1, instantaneousAirConditioner1,
            instantaneousWaterHeater1, instantaneousRefrigerator1, instantaneousKitchenOutlet1,
            instantaneousRadiantFloorPump1, instantaneousPowerConsumption1;

    boolean while_boolean_total = true;
    boolean while_boolean_instant = true;

    Thread threadTotal = new Thread();
    Thread threadInstant = new Thread();

    String user = "rpihubteam6";  //Samba User name
    String pass = "raspberrypi";   //Samba Password
    String sharedFolder= "share";  //Samba Shared folder
    String domain = "rpihubteam6";    //Samba domain name
    String powerFileName = "power.csv";
    String ipAddressWireless = "192.168.1.10"; //IP address for rpihubteam6 when it is wirelessly connected with the router
    String ipAddressEthernet = "192.168.1.11"; //IP address for rpihubteam6 when it is wired with the router

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_power);

        //Power Production
        totalTextView1 = findViewById(R.id.power_production_total_value);
        instantaneousTextView1 = findViewById(R.id.power_production_current_value);

        //Power Consumption
        totalTextView8 = findViewById(R.id.power_consumption_total_value);
        instantaneousTextView8 = findViewById(R.id.power_consumption_current_value);

        //Lighting
        totalTextView2 = findViewById(R.id.lighting_total_value);
        instantaneousTextView2 = findViewById(R.id.lighting_current_value);

        //Air Conditioner
        totalTextView3 = findViewById(R.id.air_conditioner_total_value);
        instantaneousTextView3 = findViewById(R.id.air_conditioner_current_value);

        //Water Heater
        totalTextView4 = findViewById(R.id.water_heater_total_value);
        instantaneousTextView4 = findViewById(R.id.water_heater_current_value);

        //Refrigerator
        totalTextView5 = findViewById(R.id.refrigerator_total_value);
        instantaneousTextView5 = findViewById(R.id.refrigerator_current_value);

        //Kitchen Outlet
        totalTextView6 = findViewById(R.id.kitchen_outlet_total_value);
        instantaneousTextView6 = findViewById(R.id.kitchen_outlet_current_value);

        //Radiant Floor Pump
        totalTextView7 = findViewById(R.id.radiant_floor_pump_total_value);
        instantaneousTextView7 = findViewById(R.id.radiant_floor_pump_current_value);

        //For Graphing all the sensors at once.
        refresh = findViewById(R.id.one_for_all);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
            try {
                //Creating a new thread for the file transfer, this takes the load off the main thread.
                threadTotal = new Thread(new Runnable() {
                    @Override
                    public void run() {
                    try {
                        //To get Samba Shared file from the Raspberry Pi
                        List<String[]> powerData;

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

                        powerData = csv_power.read();

                        double sumPowerProduction = 0.0;
                        double sumLighting = 0.0;
                        double sumAirConditioner = 0.0;
                        double sumWaterHeater = 0.0;
                        double sumRefrigerator = 0.0;
                        double sumKitchenOutlet = 0.0;
                        double sumRadiantFloorPump = 0.0;

                        final DateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
                        int latestData = powerData.size() - 1; //To get the last row's #
                        String[] latestRow = powerData.get(latestData); //To get data from the last row

                        Date date = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy").parse(latestRow[0]); //Extract date
                        String latestDate = df.format(date); //Date to String
                        String latestDateSubstring = latestDate.substring(0, 9);    //Extract "EEE MMM dd" part to compare

                        for (int i = 0; i < powerData.size(); i++) {
                            String[] row = powerData.get(i);

                            date = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy").parse(row[0]); //Extract date
                            String dateFound = df.format(date); //Date to String
                            String dateFoundSubstring = dateFound.substring(0, 9);    //Extract "EEE MMM dd" part to compare

                            //Only get the data if the day is the same.
                            if(latestDateSubstring.equalsIgnoreCase(dateFoundSubstring)) {
                                sumPowerProduction += Double.parseDouble(row[1]);
                                sumLighting += Double.parseDouble(row[2]);
                                sumAirConditioner += Double.parseDouble(row[3]);
                                sumWaterHeater += Double.parseDouble(row[4]);
                                sumRefrigerator += Double.parseDouble(row[5]);
                                sumKitchenOutlet += Double.parseDouble(row[6]);
                                sumRadiantFloorPump += Double.parseDouble(row[7]);
                            }
                        }

                        totalPowerProduction = Math.round(sumPowerProduction * Math.pow(10, 1)) / Math.pow(10, 1);
                        totalLighting = Math.round(sumLighting * Math.pow(10, 1)) / Math.pow(10, 1);
                        totalAirConditioner = Math.round(sumAirConditioner * Math.pow(10, 1)) / Math.pow(10, 1);
                        totalWaterHeater = Math.round(sumWaterHeater * Math.pow(10, 1)) / Math.pow(10, 1);
                        totalRefrigerator = Math.round(sumRefrigerator * Math.pow(10, 1)) / Math.pow(10, 1);
                        totalKitchenOutlet = Math.round(sumKitchenOutlet * Math.pow(10, 1)) / Math.pow(10, 1);
                        totalRadiantFloorPump = Math.round(sumRadiantFloorPump * Math.pow(10, 1)) / Math.pow(10, 1);
                        totalPowerConsumption = totalLighting + totalAirConditioner + totalWaterHeater +
                                totalRefrigerator + totalKitchenOutlet + totalRadiantFloorPump;

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    }
                });

                threadInstant = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //To get Samba Shared file from the Raspberry Pi
                            List<String[]> powerData;

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

                            powerData = csv_power.read();

                            int latestData = powerData.size() - 1; //To get the last row's #
                            String[] latestRow = powerData.get(latestData); //To get data from the last row

                            double currentPowerProduction = Double.parseDouble(latestRow[1]);
                            System.out.println(currentPowerProduction);
                            double currentLighting = Double.parseDouble(latestRow[2]);
                            double currentAirConditioner = Double.parseDouble(latestRow[3]);
                            double currentWaterHeater = Double.parseDouble(latestRow[4]);
                            double currentRefrigerator = Double.parseDouble(latestRow[5]);
                            System.out.println(currentPowerProduction);
                            double currentKitchenOutlet = Double.parseDouble(latestRow[6]);
                            System.out.println(currentPowerProduction);
                            double currentRadiantFloorPump = Double.parseDouble(latestRow[7]);
                            System.out.println(currentPowerProduction);

                            instantaneousPowerProduction = Math.round(currentPowerProduction * Math.pow(10, 1)) / Math.pow(10, 1);
                            instantaneousLighting = Math.round(currentLighting * Math.pow(10, 1)) / Math.pow(10, 1);
                            instantaneousAirConditioner = Math.round(currentAirConditioner * Math.pow(10, 1)) / Math.pow(10, 1);
                            instantaneousWaterHeater = Math.round(currentWaterHeater * Math.pow(10, 1)) / Math.pow(10, 1);
                            instantaneousRefrigerator = Math.round(currentRefrigerator * Math.pow(10, 1)) / Math.pow(10, 1);
                            instantaneousKitchenOutlet = Math.round(currentKitchenOutlet * Math.pow(10, 1)) / Math.pow(10, 1);
                            instantaneousRadiantFloorPump = Math.round(currentRadiantFloorPump * Math.pow(10, 1)) / Math.pow(10, 1);
                            instantaneousPowerConsumption = instantaneousLighting + instantaneousAirConditioner +
                                    instantaneousWaterHeater + instantaneousRefrigerator +
                                    instantaneousKitchenOutlet + instantaneousRadiantFloorPump;

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                threadTotal.start();
                threadInstant.start();
            } catch (Exception e) {
                e.printStackTrace();
            }

            while(while_boolean_total) {
                if (!threadTotal.isAlive()) {
                    String string;
                    totalPowerProduction1 = Double.toString(totalPowerProduction);
                    totalLighting1 = Double.toString(totalLighting);
                    totalAirConditioner1 = Double.toString(totalAirConditioner);
                    totalWaterHeater1 = Double.toString(totalWaterHeater);
                    totalRefrigerator1 = Double.toString(totalRefrigerator);
                    totalKitchenOutlet1 = Double.toString(totalKitchenOutlet);
                    totalRadiantFloorPump1 = Double.toString(totalRadiantFloorPump);
                    totalPowerConsumption1 = Double.toString(totalPowerConsumption);

                    string = totalPowerProduction1 +  units;
                    totalTextView1.setText(string);

                    string = totalPowerConsumption1 +  units;
                    totalTextView8.setText(string);

                    string = totalLighting1 +  units;
                    totalTextView2.setText(string);

                    string = totalAirConditioner1 +  units;
                    totalTextView3.setText(string);

                    string = totalWaterHeater1 +  units;
                    totalTextView4.setText(string);

                    string = totalRefrigerator1 +  units;
                    totalTextView5.setText(string);

                    string = totalKitchenOutlet1 +  units;
                    totalTextView6.setText(string);

                    string = totalRadiantFloorPump1 +  units;
                    totalTextView7.setText(string);

                    while_boolean_total = false;
                }
            }

            while(while_boolean_instant) {
                if (!threadInstant.isAlive()) {
                    String string;
                    instantaneousPowerProduction1 = Double.toString(instantaneousPowerProduction);
                    instantaneousLighting1 = Double.toString(instantaneousLighting);
                    instantaneousAirConditioner1 = Double.toString(instantaneousAirConditioner);
                    instantaneousWaterHeater1 = Double.toString(instantaneousWaterHeater);
                    instantaneousRefrigerator1 = Double.toString(instantaneousRefrigerator);
                    instantaneousKitchenOutlet1 = Double.toString(instantaneousKitchenOutlet);
                    instantaneousRadiantFloorPump1 = Double.toString(instantaneousRadiantFloorPump);
                    instantaneousPowerConsumption1 = Double.toString(instantaneousPowerConsumption);

                    string = instantaneousPowerProduction1 +  units;
                    instantaneousTextView1.setText(string);

                    string = instantaneousPowerConsumption1 +  units;
                    instantaneousTextView8.setText(string);

                    string = instantaneousLighting1 +  units;
                    instantaneousTextView2.setText(string);

                    string = instantaneousAirConditioner1 +  units;
                    instantaneousTextView3.setText(string);

                    string = instantaneousWaterHeater1 +  units;
                    instantaneousTextView4.setText(string);

                    string = instantaneousRefrigerator1 +  units;
                    instantaneousTextView5.setText(string);

                    string = instantaneousKitchenOutlet1 +  units;
                    instantaneousTextView6.setText(string);

                    string = instantaneousRadiantFloorPump1 +  units;
                    instantaneousTextView7.setText(string);

                    while_boolean_instant = false;
                }
            }
            }
        });
    }
}