package com.example.solar_decathlon_house_numerical;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;

public class WaterFlow extends AppCompatActivity {

    private static final String TAG = WaterFlow.class.getSimpleName();
    Button refresh;

    String units = " Gallons";
    String string;
    boolean while_boolean = true;
    Thread thread = new Thread();

    TextView totalTextView, totalSensor1TextView, totalSensor2TextView;
    Double totalWaterUsage = 0.0;
    Double totalSensor1WaterUsage = 0.0;
    Double totalSensor2WaterUsage = 0.0;

    String totalWaterUsage1;
    String totalSensor1WaterUsage1;
    String totalSensor2WaterUsage1;

    String user = "rpihubteam6";  //Samba User name
    String pass = "raspberrypi";   //Samba Password
    String sharedFolder = "share";  //Samba Shared folder
    String domain = "rpihubteam6";    //Samba domain name
    String fileName1 = "water1app.csv";
    String fileName2 = "water2app.csv";
    String ipAddressWireless = "192.168.1.10"; //IP address for rpihubteam6 when it is wirelessly connected with the router
    String ipAddressEthernet = "192.168.1.11"; //IP address for rpihubteam6 when it is connected with the router via ethernet

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_water_flow);

        totalTextView = findViewById(R.id.water_flow_total_value);
        totalTextView.setVisibility(TextView.INVISIBLE);
        totalSensor1TextView = findViewById(R.id.water_flow_sensor1_total_value);
        totalSensor1TextView.setVisibility(TextView.INVISIBLE);
        totalSensor2TextView = findViewById(R.id.water_flow_sensor2_total_value);
        totalSensor2TextView.setVisibility(TextView.INVISIBLE);

        refresh = findViewById(R.id.one_for_all);
        refresh.setOnClickListener(new View.OnClickListener(){
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
                            String url1 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/" + fileName1;
                            NtlmPasswordAuthentication auth1 = new NtlmPasswordAuthentication(domain, user, pass);
                            InputStream smbWater1ConsumeFile = new SmbFile(url1, auth1).getInputStream();
                            //To create a List Array for Power Consumed
                            CSVReader csv_water1_consumption = new CSVReader(smbWater1ConsumeFile, "water");//CSVReader(inputStream2);
                            List<String[]> waterData = csv_water1_consumption.read();

                            double totalWaterSum;
                            double totalSensor1WaterSum = 0.0;
                            double totalSensor2WaterSum = 0.0;

                            for (int i = 0; i < waterData.size(); i++) {
                                String[] rows = waterData.get(i);
                                totalSensor1WaterSum += Double.parseDouble(rows[1]);
                                totalSensor2WaterSum += Double.parseDouble(rows[2]);
                            }

                            totalWaterSum = totalSensor1WaterSum + totalSensor2WaterSum;

                            totalWaterUsage = Math.round(totalWaterSum * Math.pow(10, 2)) / Math.pow(10, 2); //To round off to two decimal places.
                            totalSensor1WaterUsage = Math.round(totalSensor1WaterSum * Math.pow(10, 2)) / Math.pow(10, 2);
                            totalSensor2WaterUsage = Math.round(totalSensor2WaterSum * Math.pow(10, 2)) / Math.pow(10, 2);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        }
                    });
                    thread.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                while_boolean = true;
                while(while_boolean) {
                    if (!thread.isAlive()) {
                        totalWaterUsage1 = Double.toString(totalWaterUsage);
                        totalSensor1WaterUsage1 = Double.toString(totalSensor1WaterUsage);
                        totalSensor2WaterUsage1 = Double.toString(totalSensor2WaterUsage);

                        totalTextView.setVisibility(TextView.VISIBLE);
                        string = totalWaterUsage1 + units;
                        totalTextView.setText(string);
                        totalSensor1TextView.setVisibility(TextView.VISIBLE);
                        string = totalSensor1WaterUsage1 + units;
                        totalSensor1TextView.setText(string);
                        totalSensor2TextView.setVisibility(TextView.VISIBLE);
                        string = totalSensor2WaterUsage1 + units;
                        totalSensor2TextView.setText(string);

                        while_boolean = false;
                    }
                }
            }
        });
    }
}
