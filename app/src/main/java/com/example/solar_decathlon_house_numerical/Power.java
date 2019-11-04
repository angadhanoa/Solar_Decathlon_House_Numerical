package com.example.solar_decathlon_house_numerical;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;

public class Power extends AppCompatActivity {
    private static final String TAG = Power.class.getSimpleName();
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

    boolean while_boolean = true;
    Thread thread = new Thread();

    String user = "rpihubteam6";  //Samba User name
    String pass = "raspberrypi";   //Samba Password
    String sharedFolder= "share";  //Samba Shared folder
    String domain = "rpihubteam6";    //Samba domain name
    String fileName = "power.csv";
    String ipAddressWireless = "192.168.1.10"; //IP address for rpihubteam6 when it is wirelessly connected with the router
    String ipAddressEthernet = "192.168.1.11"; //IP address for rpihubteam6 when it is wired with the router

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_power);

        //Power Production
        totalTextView1 = findViewById(R.id.power_production_total_value);
        totalTextView1.setVisibility(TextView.INVISIBLE);
        instantaneousTextView1 = findViewById(R.id.power_production_current_value);
        instantaneousTextView1.setVisibility(TextView.INVISIBLE);

        //Lighting
        totalTextView2 = findViewById(R.id.lighting_total_value);
        totalTextView2.setVisibility(TextView.INVISIBLE);
        instantaneousTextView2 = findViewById(R.id.lighting_current_value);
        instantaneousTextView2.setVisibility(TextView.INVISIBLE);

        //Air Conditioner
        totalTextView3 = findViewById(R.id.air_conditioner_total_value);
        totalTextView3.setVisibility(TextView.INVISIBLE);
        instantaneousTextView3 = findViewById(R.id.air_conditioner_current_value);
        instantaneousTextView3.setVisibility(TextView.INVISIBLE);

        //Water Heater
        totalTextView4 = findViewById(R.id.water_heater_total_value);
        totalTextView4.setVisibility(TextView.INVISIBLE);
        instantaneousTextView4 = findViewById(R.id.water_heater_current_value);
        instantaneousTextView4.setVisibility(TextView.INVISIBLE);

        //Refrigerator
        totalTextView5 = findViewById(R.id.refrigerator_total_value);
        totalTextView5.setVisibility(TextView.INVISIBLE);
        instantaneousTextView5 = findViewById(R.id.refrigerator_current_value);
        instantaneousTextView5.setVisibility(TextView.INVISIBLE);

        //Kitchen Outlet
        totalTextView6 = findViewById(R.id.kitchen_outlet_total_value);
        totalTextView6.setVisibility(TextView.INVISIBLE);
        instantaneousTextView6 = findViewById(R.id.kitchen_outlet_current_value);
        instantaneousTextView6.setVisibility(TextView.INVISIBLE);

        //Radiant Floor Pump
        totalTextView7 = findViewById(R.id.radiant_floor_pump_total_value);
        totalTextView7.setVisibility(TextView.INVISIBLE);
        instantaneousTextView7 = findViewById(R.id.radiant_floor_pump_current_value);
        instantaneousTextView7.setVisibility(TextView.INVISIBLE);

        //For Graphing all the sensors at once.
        refresh = (Button)findViewById(R.id.one_for_all);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
            try {
                //Creating a new thread for the file transfer, this takes the load off the main thread.
                thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                    try {
                        //To get Samba Shared file from the Raspberry Pi
                        List<String[]> powerData;

                        String url2 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/" + fileName;
                        NtlmPasswordAuthentication auth2 = new NtlmPasswordAuthentication(domain, user, pass);
                        InputStream smbPowerConsumeFile = new SmbFile(url2, auth2).getInputStream();

                        //To create a List Array for Power Consumed
                        CSVReader csv_consumption = new CSVReader(smbPowerConsumeFile, "power");//CSVReader(inputStream2);
                        powerData = csv_consumption.read();

                        double sumPowerProduction = 0.0;
                        double sumLighting = 0.0;
                        double sumAirConditioner = 0.0;
                        double sumWaterHeater = 0.0;
                        double sumRefrigerator = 0.0;
                        double sumKitchenOutlet = 0.0;
                        double sumRadiantFloorPump = 0.0;

                        double currentPowerProduction = 0.0;
                        double currentLighting = 0.0;
                        double currentAirConditioner = 0.0;
                        double currentWaterHeater = 0.0;
                        double currentRefrigerator = 0.0;
                        double currentKitchenOutlet = 0.0;
                        double currentRadiantFloorPump = 0.0;

                        for (int i = 0; i < powerData.size(); i++) {
                            String[] row = powerData.get(i);

                            sumPowerProduction += Double.parseDouble(row[1]);
                            sumLighting += Double.parseDouble(row[2]);
                            sumAirConditioner += Double.parseDouble(row[3]);
                            sumWaterHeater += Double.parseDouble(row[4]);
                            sumRefrigerator += Double.parseDouble(row[5]);
                            sumKitchenOutlet += Double.parseDouble(row[6]);
                            sumRadiantFloorPump += Double.parseDouble(row[7]);

                            if( i == powerData.size() - 1)
                            {
                                currentPowerProduction = Double.parseDouble(row[1]);
                                currentLighting = Double.parseDouble(row[2]);
                                currentAirConditioner = Double.parseDouble(row[3]);
                                currentWaterHeater = Double.parseDouble(row[4]);
                                currentRefrigerator = Double.parseDouble(row[5]);
                                currentKitchenOutlet = Double.parseDouble(row[6]);
                                currentRadiantFloorPump = Double.parseDouble(row[7]);
                            }
                        }

                        totalPowerProduction = Math.round(sumPowerProduction * Math.pow(10, 2)) / Math.pow(10, 2);
                        totalLighting = Math.round(sumLighting * Math.pow(10, 2)) / Math.pow(10, 2);
                        totalAirConditioner = Math.round(sumAirConditioner * Math.pow(10, 2)) / Math.pow(10, 2);
                        totalWaterHeater = Math.round(sumWaterHeater * Math.pow(10, 2)) / Math.pow(10, 2);
                        totalRefrigerator = Math.round(sumRefrigerator * Math.pow(10, 2)) / Math.pow(10, 2);
                        totalKitchenOutlet = Math.round(sumKitchenOutlet * Math.pow(10, 2)) / Math.pow(10, 2);
                        totalRadiantFloorPump = Math.round(sumRadiantFloorPump * Math.pow(10, 2)) / Math.pow(10, 2);
                        totalPowerConsumption = totalLighting + totalAirConditioner + totalWaterHeater +
                                totalRefrigerator + totalKitchenOutlet + totalRadiantFloorPump;

                        instantaneousPowerProduction = Math.round(currentPowerProduction * Math.pow(10, 2)) / Math.pow(10, 2);
                        instantaneousLighting = Math.round(currentLighting * Math.pow(10, 2)) / Math.pow(10, 2);
                        instantaneousAirConditioner = Math.round(currentAirConditioner * Math.pow(10, 2)) / Math.pow(10, 2);
                        instantaneousWaterHeater = Math.round(currentWaterHeater * Math.pow(10, 2)) / Math.pow(10, 2);
                        instantaneousRefrigerator = Math.round(currentRefrigerator * Math.pow(10, 2)) / Math.pow(10, 2);
                        instantaneousKitchenOutlet = Math.round(currentKitchenOutlet * Math.pow(10, 2)) / Math.pow(10, 2);
                        instantaneousRadiantFloorPump = Math.round(currentRadiantFloorPump * Math.pow(10, 2)) / Math.pow(10, 2);
                        instantaneousPowerConsumption = instantaneousLighting + instantaneousAirConditioner +
                                instantaneousWaterHeater + instantaneousRefrigerator +
                                instantaneousKitchenOutlet + instantaneousRadiantFloorPump;

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    }
                });
                thread.start();
            } catch (Exception e) {
                e.printStackTrace();
            }

            while(while_boolean) {
                if (!thread.isAlive()) {
                    totalPowerProduction1 = Double.toString(totalPowerProduction);
                    totalLighting1 = Double.toString(totalLighting);
                    totalAirConditioner1 = Double.toString(totalAirConditioner);
                    totalWaterHeater1 = Double.toString(totalWaterHeater);
                    totalRefrigerator1 = Double.toString(totalRefrigerator);
                    totalKitchenOutlet1 = Double.toString(totalKitchenOutlet);
                    totalRadiantFloorPump1 = Double.toString(totalRadiantFloorPump);
                    totalPowerConsumption1 = Double.toString(totalPowerConsumption);

                    instantaneousPowerProduction1 = Double.toString(instantaneousPowerProduction);
                    instantaneousLighting1 = Double.toString(instantaneousLighting);
                    instantaneousAirConditioner1 = Double.toString(instantaneousAirConditioner);
                    instantaneousWaterHeater1 = Double.toString(instantaneousWaterHeater);
                    instantaneousRefrigerator1 = Double.toString(instantaneousRefrigerator);
                    instantaneousKitchenOutlet1 = Double.toString(instantaneousKitchenOutlet);
                    instantaneousRadiantFloorPump1 = Double.toString(instantaneousRadiantFloorPump);
                    instantaneousPowerConsumption1 = Double.toString(instantaneousPowerConsumption);

                    totalTextView1.setVisibility(TextView.VISIBLE);
                    totalTextView1.setText(totalPowerProduction1 +  units);
                    instantaneousTextView1.setVisibility(TextView.VISIBLE);
                    instantaneousTextView1.setText(instantaneousPowerProduction1 +  units);

                    totalTextView8.setVisibility(TextView.VISIBLE);
                    totalTextView8.setText(totalPowerConsumption1 +  units);
                    instantaneousTextView8.setVisibility(TextView.VISIBLE);
                    instantaneousTextView8.setText(instantaneousPowerConsumption1 +  units);

                    totalTextView2.setVisibility(TextView.VISIBLE);
                    totalTextView2.setText(totalLighting1 +  units);
                    instantaneousTextView1.setVisibility(TextView.VISIBLE);
                    instantaneousTextView1.setText(instantaneousLighting1 +  units);

                    totalTextView3.setVisibility(TextView.VISIBLE);
                    totalTextView3.setText(totalAirConditioner1 +  units);
                    instantaneousTextView1.setVisibility(TextView.VISIBLE);
                    instantaneousTextView1.setText(instantaneousAirConditioner1 +  units);

                    totalTextView4.setVisibility(TextView.VISIBLE);
                    totalTextView4.setText(totalWaterHeater1 +  units);
                    instantaneousTextView1.setVisibility(TextView.VISIBLE);
                    instantaneousTextView1.setText(instantaneousWaterHeater1 +  units);

                    totalTextView5.setVisibility(TextView.VISIBLE);
                    totalTextView5.setText(totalRefrigerator1 +  units);
                    instantaneousTextView1.setVisibility(TextView.VISIBLE);
                    instantaneousTextView1.setText(instantaneousRefrigerator1 +  units);

                    totalTextView6.setVisibility(TextView.VISIBLE);
                    totalTextView6.setText(totalKitchenOutlet1 +  units);
                    instantaneousTextView1.setVisibility(TextView.VISIBLE);
                    instantaneousTextView1.setText(instantaneousKitchenOutlet1 +  units);

                    totalTextView7.setVisibility(TextView.VISIBLE);
                    totalTextView7.setText(totalRadiantFloorPump1 +  units);
                    instantaneousTextView1.setVisibility(TextView.VISIBLE);
                    instantaneousTextView1.setText(instantaneousRadiantFloorPump1 +  units);

                    while_boolean = false;
                }
            }
            }
        });
    }
}