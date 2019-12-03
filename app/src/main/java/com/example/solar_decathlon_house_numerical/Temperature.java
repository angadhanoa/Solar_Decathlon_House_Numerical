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

public class Temperature extends AppCompatActivity {
    private static final String TAG = Temperature.class.getSimpleName();
    Button refresh;
    String temperatureFileName = "heatsignature.csv";

    TextView maxTextView1, maxTextView2, maxTextView3, maxTextView4,
            maxTextView5, maxTextView6, maxTextView7, maxTextView8;
    TextView minTextView1, minTextView2, minTextView3, minTextView4,
            minTextView5, minTextView6, minTextView7, minTextView8;
    TextView instantaneousTextView1, instantaneousTextView2, instantaneousTextView3,
            instantaneousTextView4, instantaneousTextView5, instantaneousTextView6,
            instantaneousTextView7, instantaneousTextView8;

    Double maxExteriorTemperature = 0.0;
    Double minExteriorTemperature = 0.0;
    Double instantaneousExteriorTemperature = 0.0;

    Double maxInteriorTemperature = 0.0;
    Double minInteriorTemperature = 0.0;
    Double instantaneousInteriorTemperature = 0.0;

    Double maxNorthWallTemperature = 0.0;
    Double minNorthWallTemperature = 0.0;
    Double instantaneousNorthWallTemperature = 0.0;

    Double maxSolarPanelTemperature = 0.0;
    Double minSolarPanelTemperature = 0.0;
    Double instantaneousSolarPanelTemperature = 0.0;

    Double maxRoofTemperature = 0.0;
    Double minRoofTemperature = 0.0;
    Double instantaneousRoofTemperature = 0.0;

    Double maxWaterTankTemperature = 0.0;
    Double minWaterTankTemperature = 0.0;
    Double instantaneousWaterTankTemperature = 0.0;

    Double maxWaterSolarCollectorTemperature = 0.0;
    Double minWaterSolarCollectorTemperature = 0.0;
    Double instantaneousWaterSolarCollectorTemperature = 0.0;

    String units = '\u00B0' + " F";
    String string;
    String  maxExteriorTemperature1, minExteriorTemperature1, instantaneousExteriorTemperature1,
            maxInteriorTemperature1, minInteriorTemperature1, instantaneousInteriorTemperature1,
            maxNorthWallTemperature1, minNorthWallTemperature1, instantaneousNorthWallTemperature1,
            maxSolarPanelTemperature1, minSolarPanelTemperature1, instantaneousSolarPanelTemperature1,
            maxRoofTemperature1, minRoofTemperature1, instantaneousRoofTemperature1,
            maxWaterTankTemperature1, minWaterTankTemperature1, instantaneousWaterTankTemperature1,
            maxWaterSolarCollectorTemperature1, minWaterSolarCollectorTemperature1, instantaneousWaterSolarCollectorTemperature1;

    boolean while_boolean_max = true;
    boolean while_boolean_min = true;
    boolean while_boolean_instant = true;

    Thread threadMax = new Thread();
    Thread threadMin = new Thread();
    Thread threadInstant = new Thread();

    String user = "rpihubteam6";  //Samba User name
    String pass ="raspberrypi";   //Samba Password
    String sharedFolder="share";  //Samba Shared folder
    String domain = "rpihubteam6";    //Samba domain name
    String ipAddressWireless = "192.168.1.10"; //IP address for rpihubteam6 when it is wirelessly connected with the router
    String ipAddressEthernet = "192.168.1.11"; //IP address for rpihubteam6 when it is wired with the router

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature);

        //Exterior Temperature Sensor
        maxTextView1 = findViewById(R.id.exterior_temperature_maximum_value);
        minTextView1 = findViewById(R.id.exterior_temperature_minimum_value);
        instantaneousTextView1 = findViewById(R.id.exterior_temperature_instantaneous_value);

        //Interior Temperature Sensor
        maxTextView2 = findViewById(R.id.interior_temperature_maximum_value);
        minTextView2 = findViewById(R.id.interior_temperature_minimum_value);
        instantaneousTextView2 = findViewById(R.id.interior_temperature_instantaneous_value);

        //South Wall Temperature Sensor
        maxTextView3 = findViewById(R.id.south_wall_temperature_maximum_value);
        minTextView3 = findViewById(R.id.south_wall_temperature_minimum_value);
        instantaneousTextView3 = findViewById(R.id.south_wall_temperature_instantaneous_value);

        //North Wall Temperature Sensor
        maxTextView4 = findViewById(R.id.north_wall_temperature_maximum_value);
        minTextView4 = findViewById(R.id.north_wall_temperature_minimum_value);
        instantaneousTextView4 = findViewById(R.id.north_wall_temperature_instantaneous_value);

        //Solar Panel Temperature Sensor
        maxTextView5 = findViewById(R.id.solar_panel_temperature_maximum_value);
        minTextView5 = findViewById(R.id.solar_panel_temperature_minimum_value);
        instantaneousTextView5 = findViewById(R.id.solar_panel_temperature_instantaneous_value);

        //Roof Temperature Sensor
        maxTextView6 = findViewById(R.id.roof_temperature_maximum_value);
        minTextView6 = findViewById(R.id.roof_temperature_minimum_value);
        instantaneousTextView6 = findViewById(R.id.roof_temperature_instantaneous_value);

        //Water Tank Temperature Sensor
        maxTextView7 = findViewById(R.id.water_tank_temperature_maximum_value);
        minTextView7 = findViewById(R.id.water_tank_temperature_minimum_value);
        instantaneousTextView7 = findViewById(R.id.water_tank_temperature_instantaneous_value);

        //Water Solar Collector Temperature Sensor
        maxTextView8 = findViewById(R.id.water_solar_collector_temperature_maximum_value);
        minTextView8 = findViewById(R.id.water_solar_collector_temperature_minimum_value);
        instantaneousTextView8 = findViewById(R.id.water_solar_collector_temperature_instantaneous_value);

        refresh = findViewById(R.id.one_for_all);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
                try{
                    //Creating a new thread for the file transfer, this takes the load off the main thread.
                    threadMax = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try
                            {
                                //To get Samba Shared file from the Raspberry Pi
                                List<String[]> temperatureData;

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

                                temperatureData = csv_temperature.read();
                                String[] rows = temperatureData.get(0);

                                double maxiInteriorTemperature              = Double.parseDouble(rows[2]);
                                double maxiSolarPanelTemperature            = Double.parseDouble(rows[3]);
                                double maxiRoofTemperature                  = Double.parseDouble(rows[4]);
                                double maxiExteriorTemperature              = Double.parseDouble(rows[5]);
                                double maxiNorthWallTemperature             = Double.parseDouble(rows[6]);
                                double maxiWaterTankTemperature             = Double.parseDouble(rows[7]);
                                double maxiWaterSolarCollectorTemperature   = Double.parseDouble(rows[8]);

                                final DateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
                                int latestData = temperatureData.size() - 1; //To get the last row's #
                                String[] latestRow = temperatureData.get(latestData); //To get data from the last row

                                Date date = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy").parse(latestRow[0]); //Extract date
                                String latestDate = df.format(date); //Date to String
                                String latestDateSubstring = latestDate.substring(0, 9);    //Extract "EEE MMM dd" part to compare

                                for (int i = 0; i < temperatureData.size(); i++) {
                                    String[] row = temperatureData.get(i);

                                    date = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy").parse(row[0]); //Extract date
                                    String dateFound = df.format(date); //Date to String
                                    String dateFoundSubstring = dateFound.substring(0, 9);    //Extract "EEE MMM dd" part to compare

                                    //Only get the data if the day is the same.
                                    if(latestDateSubstring.equalsIgnoreCase(dateFoundSubstring)) {
                                        if (Double.parseDouble(row[2]) >= maxiInteriorTemperature)
                                            maxiInteriorTemperature = Double.parseDouble(row[2]);
                                        if (Double.parseDouble(row[3]) >= maxiSolarPanelTemperature)
                                            maxiSolarPanelTemperature = Double.parseDouble(row[3]);
                                        if (Double.parseDouble(row[4]) >= maxiRoofTemperature)
                                            maxiRoofTemperature = Double.parseDouble(row[4]);
                                        if (Double.parseDouble(row[5]) >= maxiExteriorTemperature)
                                            maxiExteriorTemperature = Double.parseDouble(row[5]);
                                        if (Double.parseDouble(row[6]) >= maxiNorthWallTemperature)
                                            maxiNorthWallTemperature = Double.parseDouble(row[6]);
                                        if (Double.parseDouble(row[7]) >= maxiWaterTankTemperature)
                                            maxiWaterTankTemperature = Double.parseDouble(row[7]);
                                        if (Double.parseDouble(row[8]) >= maxiWaterSolarCollectorTemperature)
                                            maxiWaterSolarCollectorTemperature = Double.parseDouble(row[8]);
                                    }
                                }

                                maxExteriorTemperature = Math.round(maxiExteriorTemperature * Math.pow(10, 1)) / Math.pow(10, 1);
                                maxInteriorTemperature = Math.round(maxiInteriorTemperature * Math.pow(10, 1)) / Math.pow(10, 1);
                                maxNorthWallTemperature = Math.round(maxiNorthWallTemperature * Math.pow(10, 1)) / Math.pow(10, 1);
                                maxSolarPanelTemperature = Math.round(maxiSolarPanelTemperature * Math.pow(10, 1)) / Math.pow(10, 1);
                                maxRoofTemperature = Math.round(maxiRoofTemperature * Math.pow(10, 1)) / Math.pow(10, 1);
                                maxWaterTankTemperature = Math.round(maxiWaterTankTemperature * Math.pow(10, 1)) / Math.pow(10, 1);
                                maxWaterSolarCollectorTemperature = Math.round(maxiWaterSolarCollectorTemperature * Math.pow(10, 1)) / Math.pow(10, 1);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    threadMin = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try
                            {
                                //To get Samba Shared file from the Raspberry Pi
                                List<String[]> temperatureData;

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
                                    csv_temperature = new CSVReader(smbTemperatureFileEthernet, "heat");;
                                }

                                temperatureData = csv_temperature.read();
                                String[] rows = temperatureData.get(0);

                                double miniInteriorTemperature              = Double.parseDouble(rows[2]);
                                double miniSolarPanelTemperature            = Double.parseDouble(rows[3]);
                                double miniRoofTemperature                  = Double.parseDouble(rows[4]);
                                double miniExteriorTemperature              = Double.parseDouble(rows[5]);
                                double miniNorthWallTemperature             = Double.parseDouble(rows[6]);
                                double miniWaterTankTemperature             = Double.parseDouble(rows[7]);
                                double miniWaterSolarCollectorTemperature   = Double.parseDouble(rows[8]);

                                final DateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
                                int latestData = temperatureData.size() - 1; //To get the last row's #
                                String[] latestRow = temperatureData.get(latestData); //To get data from the last row

                                Date date = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy").parse(latestRow[0]); //Extract date
                                String latestDate = df.format(date); //Date to String
                                String latestDateSubstring = latestDate.substring(0, 9);    //Extract "EEE MMM dd" part to compare

                                for (int i = 0; i < temperatureData.size(); i++) {
                                    String[] row = temperatureData.get(i);

                                    date = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy").parse(row[0]); //Extract date
                                    String dateFound = df.format(date); //Date to String
                                    String dateFoundSubstring = dateFound.substring(0, 9);    //Extract "EEE MMM dd" part to compare

                                    //Only get the data if the day is the same.
                                    if(latestDateSubstring.equalsIgnoreCase(dateFoundSubstring)) {
                                        if (Double.parseDouble(row[2]) <= miniInteriorTemperature)
                                            miniInteriorTemperature = Double.parseDouble(row[2]);
                                        if (Double.parseDouble(row[3]) <= miniSolarPanelTemperature)
                                            miniSolarPanelTemperature = Double.parseDouble(row[3]);
                                        if (Double.parseDouble(row[4]) <= miniRoofTemperature)
                                            miniRoofTemperature = Double.parseDouble(row[4]);
                                        if (Double.parseDouble(row[5]) <= miniExteriorTemperature)
                                            miniExteriorTemperature = Double.parseDouble(row[5]);
                                        if (Double.parseDouble(row[6]) <= miniNorthWallTemperature)
                                            miniNorthWallTemperature = Double.parseDouble(row[6]);
                                        if (Double.parseDouble(row[7]) <= miniWaterTankTemperature)
                                            miniWaterTankTemperature = Double.parseDouble(row[7]);
                                        if (Double.parseDouble(row[8]) <= miniWaterSolarCollectorTemperature)
                                            miniWaterSolarCollectorTemperature = Double.parseDouble(row[8]);
                                    }
                                }

                                minExteriorTemperature = Math.round(miniExteriorTemperature * Math.pow(10, 1)) / Math.pow(10, 1);
                                minInteriorTemperature = Math.round(miniInteriorTemperature * Math.pow(10, 1)) / Math.pow(10, 1);
                                minNorthWallTemperature = Math.round(miniNorthWallTemperature * Math.pow(10, 1)) / Math.pow(10, 1);
                                minSolarPanelTemperature = Math.round(miniSolarPanelTemperature * Math.pow(10, 1)) / Math.pow(10, 1);
                                minRoofTemperature = Math.round(miniRoofTemperature * Math.pow(10, 1)) / Math.pow(10, 1);
                                minWaterTankTemperature = Math.round(miniWaterTankTemperature * Math.pow(10, 1)) / Math.pow(10, 1);
                                minWaterSolarCollectorTemperature = Math.round(miniWaterSolarCollectorTemperature * Math.pow(10, 1)) / Math.pow(10, 1);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    threadInstant = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try
                            {
                                //To get Samba Shared file from the Raspberry Pi
                                List<String[]> temperatureData;

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
                                    csv_temperature = new CSVReader(smbTemperatureFileEthernet, "heat");;
                                }

                                temperatureData = csv_temperature.read();

                                final DateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
                                int latestData = temperatureData.size() - 1; //To get the last row's #
                                String[] latestRow = temperatureData.get(latestData); //To get data from the last row

                                Date date = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy").parse(latestRow[0]); //Extract date
                                String latestDate = df.format(date); //Date to String

                                double currentInteriorTemperature = Double.parseDouble(latestRow[2]);
                                double currentSolarPanelTemperature = Double.parseDouble(latestRow[3]);
                                double currentRoofTemperature = Double.parseDouble(latestRow[4]);
                                double currentExteriorTemperature = Double.parseDouble(latestRow[5]);
                                double currentNorthWallTemperature = Double.parseDouble(latestRow[6]);
                                double currentWaterTankTemperature = Double.parseDouble(latestRow[7]);
                                double currentWaterSolarCollectorTemperature = Double.parseDouble(latestRow[8]);

                                instantaneousExteriorTemperature = Math.round(currentExteriorTemperature * Math.pow(10, 1)) / Math.pow(10, 1);
                                instantaneousInteriorTemperature = Math.round(currentInteriorTemperature * Math.pow(10, 1)) / Math.pow(10, 1);
                                instantaneousNorthWallTemperature = Math.round(currentNorthWallTemperature * Math.pow(10, 1)) / Math.pow(10, 1);
                                instantaneousSolarPanelTemperature = Math.round(currentSolarPanelTemperature * Math.pow(10, 1)) / Math.pow(10, 1);
                                instantaneousRoofTemperature = Math.round(currentRoofTemperature * Math.pow(10, 1)) / Math.pow(10, 1);
                                instantaneousWaterTankTemperature = Math.round(currentWaterTankTemperature * Math.pow(10, 1)) / Math.pow(10, 1);
                                instantaneousWaterSolarCollectorTemperature = Math.round(currentWaterSolarCollectorTemperature * Math.pow(10, 1)) / Math.pow(10, 1);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    threadMax.start();
                    threadMin.start();
                    threadInstant.start();
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }

                while(while_boolean_max == true) {
                    if (!threadMax.isAlive()) {
                        maxExteriorTemperature1 = Double.toString(maxExteriorTemperature);
                        maxInteriorTemperature1 = Double.toString(maxInteriorTemperature);
                        maxNorthWallTemperature1 = Double.toString(maxNorthWallTemperature);
                        maxSolarPanelTemperature1 = Double.toString(maxSolarPanelTemperature);
                        maxRoofTemperature1 = Double.toString(maxRoofTemperature);
                        maxWaterTankTemperature1 = Double.toString(maxWaterTankTemperature);
                        maxWaterSolarCollectorTemperature1 = Double.toString(maxWaterSolarCollectorTemperature);

                        //Exterior Temperature Sensor
                        string = maxExteriorTemperature1 + units;
                        maxTextView1.setText(string);

                        //Interior Temperature Sensor
                        string = maxInteriorTemperature1 + units;
                        maxTextView2.setText(string);

                        //South Wall Temperature Sensor
                        string = maxExteriorTemperature1 + units;
                        maxTextView3.setText(string);

                        //North Wall Temperature Sensor
                        string = maxNorthWallTemperature1 + units;
                        maxTextView4.setText(string);

                        //Solar Panel Temperature Sensor
                        string = maxSolarPanelTemperature1 + units;
                        maxTextView5.setText(string);

                        //Roof Temperature Sensor
                        string = maxRoofTemperature1 + units;
                        maxTextView6.setText(string);

                        //Water Tank Temperature Sensor
                        string = maxWaterTankTemperature1 + units;
                        maxTextView7.setText(string);

                        //Water Solar Collector Temperature Sensor
                        string = maxWaterSolarCollectorTemperature1 + units;
                        maxTextView8.setText(string);

                        while_boolean_max = false;
                    }
                }

                while(while_boolean_min == true) {
                    if (!threadMin.isAlive()) {
                        minExteriorTemperature1 = Double.toString(minExteriorTemperature);
                        minInteriorTemperature1 = Double.toString(minInteriorTemperature);
                        minNorthWallTemperature1 = Double.toString(minNorthWallTemperature);
                        minSolarPanelTemperature1 = Double.toString(minSolarPanelTemperature);
                        minRoofTemperature1 = Double.toString(minRoofTemperature);
                        minWaterTankTemperature1 = Double.toString(minWaterTankTemperature);
                        minWaterSolarCollectorTemperature1 = Double.toString(minWaterSolarCollectorTemperature);

                        //Exterior Temperature Sensor
                        string = minExteriorTemperature1 + units;
                        minTextView1.setText(string);

                        //Interior Temperature Sensor
                        string = minInteriorTemperature1 + units;
                        minTextView2.setText(string);

                        //South Wall Temperature Sensor
                        string = minExteriorTemperature1 + units;
                        minTextView3.setText(string);

                        //North Wall Temperature Sensor
                        string = minNorthWallTemperature1 + units;
                        minTextView4.setText(string);

                        //Solar Panel Temperature Sensor
                        string = minSolarPanelTemperature1 + units;
                        minTextView5.setText(string);

                        //Roof Temperature Sensor
                        string = minRoofTemperature1 + units;
                        minTextView6.setText(string);

                        //Water Tank Temperature Sensor
                        string = minWaterTankTemperature1 + units;
                        minTextView7.setText(string);

                        //Water Solar Collector Temperature Sensor
                        string = minWaterSolarCollectorTemperature1 + units;
                        minTextView8.setText(string);

                        while_boolean_min = false;
                    }
                }

                while(while_boolean_instant == true) {
                    if (!threadInstant.isAlive()) {
                        instantaneousExteriorTemperature1 = Double.toString(instantaneousExteriorTemperature);
                        instantaneousInteriorTemperature1 = Double.toString(instantaneousInteriorTemperature);
                        instantaneousNorthWallTemperature1 = Double.toString(instantaneousNorthWallTemperature);
                        instantaneousSolarPanelTemperature1 = Double.toString(instantaneousSolarPanelTemperature);
                        instantaneousRoofTemperature1 = Double.toString(instantaneousRoofTemperature);
                        instantaneousWaterTankTemperature1 = Double.toString(instantaneousWaterTankTemperature);
                        instantaneousWaterSolarCollectorTemperature1 = Double.toString(instantaneousWaterSolarCollectorTemperature);

                        //Exterior Temperature Sensor
                        string = instantaneousExteriorTemperature1 +  units;
                        instantaneousTextView1.setText(string);

                        //Interior Temperature Sensor
                        string = instantaneousInteriorTemperature1 +  units;
                        instantaneousTextView2.setText(string);

                        //South Wall Temperature Sensor
                        string = instantaneousExteriorTemperature1 +  units;
                        instantaneousTextView3.setText(string);

                        //North Wall Temperature Sensor
                        string = instantaneousNorthWallTemperature1 +  units;
                        instantaneousTextView4.setText(string);

                        //Solar Panel Temperature Sensor
                        string = instantaneousSolarPanelTemperature1 +  units;
                        instantaneousTextView5.setText(string);

                        //Roof Temperature Sensor
                        string = instantaneousRoofTemperature1 +  units;
                        instantaneousTextView6.setText(string);

                        //Water Tank Temperature Sensor
                        string = instantaneousWaterTankTemperature1 +  units;
                        instantaneousTextView7.setText(string);

                        //Water Solar Collector Temperature Sensor
                        string = instantaneousWaterSolarCollectorTemperature1 +  units;
                        instantaneousTextView8.setText(string);

                        while_boolean_instant = false;
                    }
                }
            }
        });
    }
}