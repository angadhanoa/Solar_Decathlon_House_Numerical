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

    String units = " Fahrenheit";
    String string;
    String maxExteriorTemperature1, minExteriorTemperature1, instantaneousExteriorTemperature1,
            maxInteriorTemperature1, minInteriorTemperature1, instantaneousInteriorTemperature1,
            maxNorthWallTemperature1, minNorthWallTemperature1, instantaneousNorthWallTemperature1,
            maxSolarPanelTemperature1, minSolarPanelTemperature1, instantaneousSolarPanelTemperature1,
            maxRoofTemperature1, minRoofTemperature1, instantaneousRoofTemperature1,
            maxWaterTankTemperature1, minWaterTankTemperature1, instantaneousWaterTankTemperature1,
            maxWaterSolarCollectorTemperature1, minWaterSolarCollectorTemperature1, instantaneousWaterSolarCollectorTemperature1;

    boolean while_boolean = true;
    Thread thread = new Thread();

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
        maxTextView1.setVisibility(TextView.INVISIBLE);
        minTextView1 = findViewById(R.id.exterior_temperature_minimum_value);
        minTextView1.setVisibility(TextView.INVISIBLE);
        instantaneousTextView1 = findViewById(R.id.exterior_temperature_instantaneous_value);
        instantaneousTextView1.setVisibility(TextView.INVISIBLE);

        //Interior Temperature Sensor
        maxTextView2 = findViewById(R.id.interior_temperature_maximum_value);
        maxTextView2.setVisibility(TextView.INVISIBLE);
        minTextView2 = findViewById(R.id.interior_temperature_minimum_value);
        minTextView2.setVisibility(TextView.INVISIBLE);
        instantaneousTextView2 = findViewById(R.id.interior_temperature_instantaneous_value);
        instantaneousTextView2.setVisibility(TextView.INVISIBLE);

        //South Wall Temperature Sensor
        maxTextView3 = findViewById(R.id.south_wall_temperature_maximum_value);
        maxTextView3.setVisibility(TextView.INVISIBLE);
        minTextView3 = findViewById(R.id.south_wall_temperature_minimum_value);
        minTextView3.setVisibility(TextView.INVISIBLE);
        instantaneousTextView3 = findViewById(R.id.south_wall_temperature_instantaneous_value);
        instantaneousTextView3.setVisibility(TextView.INVISIBLE);

        //North Wall Temperature Sensor
        maxTextView4 = findViewById(R.id.north_wall_temperature_maximum_value);
        maxTextView4.setVisibility(TextView.INVISIBLE);
        minTextView4 = findViewById(R.id.north_wall_temperature_minimum_value);
        minTextView4.setVisibility(TextView.INVISIBLE);
        instantaneousTextView4 = findViewById(R.id.north_wall_temperature_instantaneous_value);
        instantaneousTextView4.setVisibility(TextView.INVISIBLE);

        //Solar Panel Temperature Sensor
        maxTextView5 = findViewById(R.id.solar_panel_temperature_maximum_value);
        maxTextView5.setVisibility(TextView.INVISIBLE);
        minTextView5 = findViewById(R.id.solar_panel_temperature_minimum_value);
        minTextView5.setVisibility(TextView.INVISIBLE);
        instantaneousTextView5 = findViewById(R.id.solar_panel_temperature_instantaneous_value);
        instantaneousTextView5.setVisibility(TextView.INVISIBLE);

        //Roof Temperature Sensor
        maxTextView6 = findViewById(R.id.roof_temperature_maximum_value);
        maxTextView6.setVisibility(TextView.INVISIBLE);
        minTextView6 = findViewById(R.id.roof_temperature_minimum_value);
        minTextView6.setVisibility(TextView.INVISIBLE);
        instantaneousTextView6 = findViewById(R.id.roof_temperature_instantaneous_value);
        instantaneousTextView6.setVisibility(TextView.INVISIBLE);

        //Water Tank Temperature Sensor
        maxTextView7 = findViewById(R.id.water_tank_temperature_maximum_value);
        maxTextView7.setVisibility(TextView.INVISIBLE);
        minTextView7 = findViewById(R.id.water_tank_temperature_minimum_value);
        minTextView7.setVisibility(TextView.INVISIBLE);
        instantaneousTextView7 = findViewById(R.id.water_tank_temperature_instantaneous_value);
        instantaneousTextView7.setVisibility(TextView.INVISIBLE);

        //Water Solar Collector Temperature Sensor
        maxTextView8 = findViewById(R.id.water_solar_collector_temperature_maximum_value);
        maxTextView8.setVisibility(TextView.INVISIBLE);
        minTextView8 = findViewById(R.id.water_solar_collector_temperature_minimum_value);
        minTextView8.setVisibility(TextView.INVISIBLE);
        instantaneousTextView8 = findViewById(R.id.water_solar_collector_temperature_instantaneous_value);
        instantaneousTextView8.setVisibility(TextView.INVISIBLE);

        refresh = findViewById(R.id.one_for_all);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
                try{
                    //Creating a new thread for the file transfer, this takes the load off the main thread.
                    thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try
                            {
                                //To get Samba Shared file from the Raspberry Pi
                                List<String[]> temperatureData;

                                String url1 = "smb://" + ipAddressWireless + "/" + sharedFolder + temperatureFileName;
                                NtlmPasswordAuthentication auth1 = new NtlmPasswordAuthentication(domain, user, pass);
                                InputStream smbTemperatureFile = new SmbFile(url1, auth1).getInputStream();
                                CSVReader csv_temperature = new CSVReader(smbTemperatureFile, "heat");
                                temperatureData = csv_temperature.read();
                                String[] rows = temperatureData.get(0);

                                double maxiExteriorTemperature = Double.parseDouble(rows[4]);
                                double miniExteriorTemperature = Double.parseDouble(rows[4]);
                                double currentExteriorTemperature = 0.0;
                                double maxiInteriorTemperature = Double.parseDouble(rows[4]);
                                double miniInteriorTemperature = Double.parseDouble(rows[4]);
                                double currentInteriorTemperature = 0.0;
                                double maxiNorthWallTemperature = Double.parseDouble(rows[4]);
                                double miniNorthWallTemperature = Double.parseDouble(rows[4]);
                                double currentNorthWallTemperature = 0.0;
                                double maxiSolarPanelTemperature = Double.parseDouble(rows[4]);
                                double miniSolarPanelTemperature = Double.parseDouble(rows[4]);
                                double currentSolarPanelTemperature = 0.0;
                                double maxiRoofTemperature = Double.parseDouble(rows[4]);
                                double miniRoofTemperature = Double.parseDouble(rows[4]);
                                double currentRoofTemperature = 0.0;
                                double maxiWaterTankTemperature = Double.parseDouble(rows[4]);
                                double miniWaterTankTemperature = Double.parseDouble(rows[4]);
                                double currentWaterTankTemperature = 0.0;
                                double maxiWaterSolarCollectorTemperature = Double.parseDouble(rows[4]);
                                double miniWaterSolarCollectorTemperature = Double.parseDouble(rows[4]);
                                double currentWaterSolarCollectorTemperature = 0.0;

                                final DateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
                                int latestData = temperatureData.size() - 1; //To get the last row's #
                                String[] latestRow = temperatureData.get(latestData); //To get data from the last row

                                Date date = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy").parse(latestRow[0]); //Extract date
                                String latestDate = df.format(date); //Date to String
                                String latestDateSubstring = latestDate.substring(0, 9);    //Extract "EEE MMM dd" part to compare

                                currentInteriorTemperature = Double.parseDouble(latestRow[1]);
                                currentSolarPanelTemperature = Double.parseDouble(latestRow[2]);
                                currentRoofTemperature = Double.parseDouble(latestRow[3]);
                                currentExteriorTemperature = Double.parseDouble(latestRow[4]);
                                currentNorthWallTemperature = Double.parseDouble(latestRow[5]);
                                currentWaterTankTemperature = Double.parseDouble(latestRow[6]);
                                currentWaterSolarCollectorTemperature = Double.parseDouble(latestRow[7]);

                                for (int i = 0; i < temperatureData.size(); i++) {
                                    String[] row = temperatureData.get(i);

                                    date = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy").parse(row[0]); //Extract date
                                    String dateFound = df.format(date); //Date to String
                                    String dateFoundSubstring = dateFound.substring(0, 9);    //Extract "EEE MMM dd" part to compare

                                    //Only get the data if the day is the same.
                                    if(latestDateSubstring.equalsIgnoreCase(dateFoundSubstring)) {
                                        //Finding the Maximum Temperature
                                        if (Double.parseDouble(row[1]) > maxiInteriorTemperature)
                                            maxiInteriorTemperature = Double.parseDouble(row[1]);
                                        if (Double.parseDouble(row[2]) > maxiSolarPanelTemperature)
                                            maxiSolarPanelTemperature = Double.parseDouble(row[2]);
                                        if (Double.parseDouble(row[3]) > maxiRoofTemperature)
                                            maxiRoofTemperature = Double.parseDouble(row[3]);
                                        if (Double.parseDouble(row[4]) > maxiExteriorTemperature)
                                            maxiExteriorTemperature = Double.parseDouble(row[4]);
                                        if (Double.parseDouble(row[5]) > maxiNorthWallTemperature)
                                            maxiNorthWallTemperature = Double.parseDouble(row[5]);
                                        if (Double.parseDouble(row[6]) > maxiWaterTankTemperature)
                                            maxiWaterTankTemperature = Double.parseDouble(row[6]);
                                        if (Double.parseDouble(row[7]) > maxiWaterSolarCollectorTemperature)
                                            maxiWaterSolarCollectorTemperature = Double.parseDouble(row[7]);

                                        //Finding the Minimum Temperature
                                        if (Double.parseDouble(row[1]) < miniInteriorTemperature)
                                            miniInteriorTemperature = Double.parseDouble(row[1]);
                                        if (Double.parseDouble(row[2]) < miniSolarPanelTemperature)
                                            miniSolarPanelTemperature = Double.parseDouble(row[2]);
                                        if (Double.parseDouble(row[3]) < miniRoofTemperature)
                                            miniRoofTemperature = Double.parseDouble(row[3]);
                                        if (Double.parseDouble(row[4]) < miniExteriorTemperature)
                                            miniExteriorTemperature = Double.parseDouble(row[4]);
                                        if (Double.parseDouble(row[5]) < miniNorthWallTemperature)
                                            miniNorthWallTemperature = Double.parseDouble(row[5]);
                                        if (Double.parseDouble(row[6]) < miniWaterTankTemperature)
                                            miniWaterTankTemperature = Double.parseDouble(row[6]);
                                        if (Double.parseDouble(row[7]) < miniWaterSolarCollectorTemperature)
                                            miniWaterSolarCollectorTemperature = Double.parseDouble(row[7]);
                                    }
                                }

                                maxExteriorTemperature = Math.round(maxiExteriorTemperature * Math.pow(10, 2)) / Math.pow(10, 2);
                                minExteriorTemperature = Math.round(miniExteriorTemperature * Math.pow(10, 2)) / Math.pow(10, 2);
                                instantaneousExteriorTemperature = Math.round(currentExteriorTemperature * Math.pow(10, 2)) / Math.pow(10, 2);
                                maxInteriorTemperature = Math.round(maxiInteriorTemperature * Math.pow(10, 2)) / Math.pow(10, 2);
                                minInteriorTemperature = Math.round(miniInteriorTemperature * Math.pow(10, 2)) / Math.pow(10, 2);
                                instantaneousInteriorTemperature = Math.round(currentInteriorTemperature * Math.pow(10, 2)) / Math.pow(10, 2);
                                maxNorthWallTemperature = Math.round(maxiNorthWallTemperature * Math.pow(10, 2)) / Math.pow(10, 2);
                                minNorthWallTemperature = Math.round(miniNorthWallTemperature * Math.pow(10, 2)) / Math.pow(10, 2);
                                instantaneousNorthWallTemperature = Math.round(currentNorthWallTemperature * Math.pow(10, 2)) / Math.pow(10, 2);
                                maxSolarPanelTemperature = Math.round(maxiSolarPanelTemperature * Math.pow(10, 2)) / Math.pow(10, 2);
                                minSolarPanelTemperature = Math.round(miniSolarPanelTemperature * Math.pow(10, 2)) / Math.pow(10, 2);
                                instantaneousSolarPanelTemperature = Math.round(currentSolarPanelTemperature * Math.pow(10, 2)) / Math.pow(10, 2);
                                maxRoofTemperature = Math.round(maxiRoofTemperature * Math.pow(10, 2)) / Math.pow(10, 2);
                                minRoofTemperature = Math.round(miniRoofTemperature * Math.pow(10, 2)) / Math.pow(10, 2);
                                instantaneousRoofTemperature = Math.round(currentRoofTemperature * Math.pow(10, 2)) / Math.pow(10, 2);
                                maxWaterTankTemperature = Math.round(maxiWaterTankTemperature * Math.pow(10, 2)) / Math.pow(10, 2);
                                minWaterTankTemperature = Math.round(miniWaterTankTemperature * Math.pow(10, 2)) / Math.pow(10, 2);
                                instantaneousWaterTankTemperature = Math.round(currentWaterTankTemperature * Math.pow(10, 2)) / Math.pow(10, 2);
                                maxWaterSolarCollectorTemperature = Math.round(maxiWaterSolarCollectorTemperature * Math.pow(10, 2)) / Math.pow(10, 2);
                                minWaterSolarCollectorTemperature = Math.round(miniWaterSolarCollectorTemperature * Math.pow(10, 2)) / Math.pow(10, 2);
                                instantaneousWaterSolarCollectorTemperature = Math.round(currentWaterSolarCollectorTemperature * Math.pow(10, 2)) / Math.pow(10, 2);

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

                while_boolean = true;
                while(while_boolean) {
                    if (!thread.isAlive()) {
                        maxExteriorTemperature1 = Double.toString(maxExteriorTemperature);
                        minExteriorTemperature1 = Double.toString(minExteriorTemperature);
                        instantaneousExteriorTemperature1 = Double.toString(instantaneousExteriorTemperature);
                        maxInteriorTemperature1 = Double.toString(maxInteriorTemperature);
                        minInteriorTemperature1 = Double.toString(minInteriorTemperature);
                        instantaneousInteriorTemperature1 = Double.toString(instantaneousInteriorTemperature);
                        maxNorthWallTemperature1 = Double.toString(maxNorthWallTemperature);
                        minNorthWallTemperature1 = Double.toString(minNorthWallTemperature);
                        instantaneousNorthWallTemperature1 = Double.toString(instantaneousNorthWallTemperature);
                        maxSolarPanelTemperature1 = Double.toString(maxSolarPanelTemperature);
                        minSolarPanelTemperature1 = Double.toString(minSolarPanelTemperature);
                        instantaneousSolarPanelTemperature1 = Double.toString(instantaneousSolarPanelTemperature);
                        maxRoofTemperature1 = Double.toString(maxRoofTemperature);
                        minRoofTemperature1 = Double.toString(minRoofTemperature);
                        instantaneousRoofTemperature1 = Double.toString(instantaneousRoofTemperature);
                        maxWaterTankTemperature1 = Double.toString(maxWaterTankTemperature);
                        minWaterTankTemperature1 = Double.toString(minWaterTankTemperature);
                        instantaneousWaterTankTemperature1 = Double.toString(instantaneousWaterTankTemperature);
                        maxWaterSolarCollectorTemperature1 = Double.toString(maxWaterSolarCollectorTemperature);
                        minWaterSolarCollectorTemperature1 = Double.toString(minWaterSolarCollectorTemperature);
                        instantaneousWaterSolarCollectorTemperature1 = Double.toString(instantaneousWaterSolarCollectorTemperature);


                        //Exterior Temperature Sensor
                        maxTextView1.setVisibility(TextView.VISIBLE);
                        string = maxExteriorTemperature1 +  units;
                        maxTextView1.setText(string);
                        minTextView1.setVisibility(TextView.VISIBLE);
                        string = minExteriorTemperature1 +  units;
                        minTextView1.setText(string);
                        instantaneousTextView1.setVisibility(TextView.VISIBLE);
                        string = instantaneousExteriorTemperature1 +  units;
                        instantaneousTextView1.setText(string);

                        //Interior Temperature Sensor
                        maxTextView2.setVisibility(TextView.VISIBLE);
                        string = maxInteriorTemperature1 +  units;
                        maxTextView2.setText(string);
                        minTextView2.setVisibility(TextView.VISIBLE);
                        string = minInteriorTemperature1 +  units;
                        minTextView2.setText(string);
                        instantaneousTextView2.setVisibility(TextView.VISIBLE);
                        string = instantaneousInteriorTemperature1 +  units;
                        instantaneousTextView2.setText(string);

                        //South Wall Temperature Sensor
                        maxTextView3.setVisibility(TextView.VISIBLE);
                        string = maxExteriorTemperature1 +  units;
                        maxTextView3.setText(string);
                        minTextView3.setVisibility(TextView.VISIBLE);
                        string = minExteriorTemperature1 +  units;
                        minTextView3.setText(string);
                        instantaneousTextView3.setVisibility(TextView.VISIBLE);
                        string = instantaneousExteriorTemperature1 +  units;
                        instantaneousTextView3.setText(string);

                        //North Wall Temperature Sensor
                        maxTextView4.setVisibility(TextView.VISIBLE);
                        string = maxNorthWallTemperature1 +  units;
                        maxTextView4.setText(string);
                        minTextView4.setVisibility(TextView.VISIBLE);
                        string = minNorthWallTemperature1 +  units;
                        minTextView4.setText(string);
                        instantaneousTextView4.setVisibility(TextView.VISIBLE);
                        string = instantaneousNorthWallTemperature1 +  units;
                        instantaneousTextView4.setText(string);

                        //Solar Panel Temperature Sensor
                        maxTextView5.setVisibility(TextView.VISIBLE);
                        string = maxSolarPanelTemperature1 +  units;
                        maxTextView5.setText(string);
                        minTextView5.setVisibility(TextView.VISIBLE);
                        string = minSolarPanelTemperature1 +  units;
                        minTextView5.setText(string);
                        instantaneousTextView5.setVisibility(TextView.VISIBLE);
                        string = instantaneousSolarPanelTemperature1 +  units;
                        instantaneousTextView5.setText(string);

                        //Roof Temperature Sensor
                        maxTextView6.setVisibility(TextView.VISIBLE);
                        string = maxRoofTemperature1 +  units;
                        maxTextView6.setText(string);
                        minTextView6.setVisibility(TextView.VISIBLE);
                        string = minRoofTemperature1 +  units;
                        minTextView6.setText(string);
                        instantaneousTextView6.setVisibility(TextView.VISIBLE);
                        string = instantaneousRoofTemperature1 +  units;
                        instantaneousTextView6.setText(string);

                        //Water Tank Temperature Sensor
                        maxTextView7.setVisibility(TextView.VISIBLE);
                        string = maxWaterTankTemperature1 +  units;
                        maxTextView7.setText(string);
                        minTextView7.setVisibility(TextView.VISIBLE);
                        string = minWaterTankTemperature1 +  units;
                        minTextView7.setText(string);
                        instantaneousTextView7.setVisibility(TextView.VISIBLE);
                        string = instantaneousWaterTankTemperature1 +  units;
                        instantaneousTextView7.setText(string);

                        //Water Solar Collector Temperature Sensor
                        maxTextView1.setVisibility(TextView.VISIBLE);
                        string = maxWaterSolarCollectorTemperature1 +  units;
                        maxTextView1.setText(string);
                        minTextView1.setVisibility(TextView.VISIBLE);
                        string = minWaterSolarCollectorTemperature1 +  units;
                        minTextView1.setText(string);
                        instantaneousTextView1.setVisibility(TextView.VISIBLE);
                        string = instantaneousWaterSolarCollectorTemperature1 +  units;
                        instantaneousTextView1.setText(string);

                        while_boolean = false;
                    }
                }
            }
        });
    }
}