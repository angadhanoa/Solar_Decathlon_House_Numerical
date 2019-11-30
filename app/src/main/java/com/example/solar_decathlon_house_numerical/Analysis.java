package com.example.solar_decathlon_house_numerical;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.InputStream;
import java.util.List;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;

public class Analysis extends AppCompatActivity {
    Button refresh, calculate;
    String temperatureFileName = "heatsignature.csv";

    TextView textView1, textView2, textView3, textView4, textView5, textView6, textView7, textView8, textView9;
    EditText editText1, editText2, editText3,editText4, editText5, editText6, editText7, editText8, editText9;

    Double exteriorTemperature = 0.0;
    Double interiorTemperature = 0.0;

    String units = '\u00B0' + "F";
    boolean while_boolean_instant = true;
    Thread threadInstant = new Thread();

    Double rValueRoom = 12.33, rValueRoof = 13.44, rValueFloor = 7.46;
    Double length1 = 21.00, length2 = 6.83, width1 = 10.00, width2 = 6.83;
    Double constantKWhr = 0.000293;
    Double constantDollarsPerKWhr = 0.1166;

    String user = "rpihubteam6";  //Samba User name
    String pass ="raspberrypi";   //Samba Password
    String sharedFolder="share";  //Samba Shared folder
    String domain = "rpihubteam6";    //Samba domain name
    String ipAddressWireless = "192.168.1.10"; //IP address for rpihubteam6 when it is wirelessly connected with the router
    String ipAddressEthernet = "192.168.1.11"; //IP address for rpihubteam6 when it is wired with the router

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);

        textView1 = findViewById(R.id.r_value_auto);
        textView2 = findViewById(R.id.exterior_temperature_auto);
        textView3 = findViewById(R.id.interior_temperature_auto);
        textView4 = findViewById(R.id.q1_auto);
        textView5 = findViewById(R.id.length_auto);
        textView6 = findViewById(R.id.width_auto);
        textView7 = findViewById(R.id.q2_auto);
        textView8 = findViewById(R.id.q3_auto);
        textView9 = findViewById(R.id.q4_auto);

        editText1 = findViewById(R.id.r_value_edit);
        editText2 = findViewById(R.id.exterior_temperature_edit);
        editText3 = findViewById(R.id.interior_temperature_edit);
        editText4 = findViewById(R.id.q1_edit);
        editText5 = findViewById(R.id.length_edit);
        editText6 = findViewById(R.id.width_edit);
        editText7 = findViewById(R.id.q2_edit);
        editText8 = findViewById(R.id.q3_edit);
        editText9 = findViewById(R.id.q4_edit);

        refresh = findViewById(R.id.one_for_all);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
                try{
                    //Creating a new thread for the file transfer, this takes the load off the main thread.
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

                                temperatureData     = csv_temperature.read();
                                int latestData      = temperatureData.size() - 1; //To get the last row's #
                                String[] latestRow  = temperatureData.get(latestData); //To get data from the last row
                                double currentInteriorTemperature = Double.parseDouble(latestRow[1]);
                                double currentExteriorTemperature = Double.parseDouble(latestRow[4]);

                                exteriorTemperature = Math.round(currentExteriorTemperature * Math.pow(10, 2)) / Math.pow(10, 2);
                                interiorTemperature = Math.round(currentInteriorTemperature * Math.pow(10, 2)) / Math.pow(10, 2);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    threadInstant.start();
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }

                while(while_boolean_instant == true) {
                    if (!threadInstant.isAlive()) {
                        double q1           = (exteriorTemperature - interiorTemperature)/(rValueRoom); // Btu/[h*ft^2]
                        double q2           = 2*q1*length1*width1;        // Btu/hr
                        double q3           = q2*constantKWhr;            // kW
                        double q4           = Math.abs(q3*constantDollarsPerKWhr);  // $/kWh
                        double q1rounded    = Math.round(q1 * Math.pow(10, 4)) / Math.pow(10, 4);
                        double q2rounded    = Math.round(q2 * Math.pow(10, 4)) / Math.pow(10, 4);
                        double q3rounded    = Math.round(q3 * Math.pow(10, 4)) / Math.pow(10, 4);
                        double q4rounded    = Math.abs(Math.round(q4 * Math.pow(10, 4)) / Math.pow(10, 4));

                        String rValueRoom1          = "R-Value [h*[ft^2]*F/Btu] = " + rValueRoom;
                        String exteriorTempString   = "Ambient Temperature [" + units + "] = " + exteriorTemperature + units;
                        String interiorTempString   = "Interior Temperature [" + units + "] = " + interiorTemperature + units;
                        String q1string             = "q1 [Btu/h*[ft^2]] = " + q1rounded;
                        String lengthString         = "Length [ft] = " + length1;
                        String widthString          = "Width [ft] = " + width1;
                        String q2string             = "q2 [Btu/hr] = " + q2rounded;
                        String q3string             = "q3 [kW] = " + q3rounded;
                        String q4string             = "q4 [$/kWh] = " + q4rounded;

                        textView1.setText(rValueRoom1);
                        textView2.setText(exteriorTempString);
                        textView3.setText(interiorTempString);
                        textView4.setText(q1string);
                        textView5.setText(lengthString);
                        textView6.setText(widthString);
                        textView7.setText(q2string);
                        textView8.setText(q3string);
                        textView9.setText(q4string);

                        while_boolean_instant = false;
                    }
                }
            }
        });

        calculate = findViewById(R.id.calculate_for_all);
        calculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                double r_value_editText = Double.parseDouble(editText1.getText().toString().trim())*1.00;
                double exterior_temperature_editText = Double.parseDouble(editText2.getText().toString().trim())*1.00;
                double interior_temperature_editText = Double.parseDouble(editText3.getText().toString().trim())*1.00;
                double length_editText = Double.parseDouble(editText5.getText().toString().trim())*1.00;
                double width_editText = Double.parseDouble(editText6.getText().toString().trim())*1.00;

                double q1           = (exterior_temperature_editText - interior_temperature_editText) / r_value_editText; // Btu/[h*ft^2]
                double q2           = 2*q1*length_editText*width_editText;        // Btu/hr
                double q3           = q2*constantKWhr;            // kW
                double q4           = q3*constantDollarsPerKWhr;  // $/kWh
                double q1rounded    = Math.round(q1 * Math.pow(10, 2)) / Math.pow(10, 2);
                double q2rounded    = Math.round(q2 * Math.pow(10, 2)) / Math.pow(10, 2);
                double q3rounded    = Math.round(q3 * Math.pow(10, 2)) / Math.pow(10, 2);
                double q4rounded    = Math.abs(Math.round(q4 * Math.pow(10, 2)) / Math.pow(10, 2));

                editText4.setText(Double.toString(q1rounded), TextView.BufferType.EDITABLE);
                editText7.setText(Double.toString(q2rounded), TextView.BufferType.EDITABLE);
                editText8.setText(Double.toString(q3rounded), TextView.BufferType.EDITABLE);
                editText9.setText(Double.toString(q4rounded), TextView.BufferType.EDITABLE);
            }
        });
    }
}