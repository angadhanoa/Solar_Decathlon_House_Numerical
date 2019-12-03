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
    Button refresh, calculateTable, calculate;
    String temperatureFileName = "heatsignature.csv";

    // Text Views for Auto Analysis table
    TextView textView21, textView22, textView23, textView24, textView25, textView26, textView27, textView28, textView29,
             textView31, textView32, textView33, textView34, textView35, textView36, textView37, textView38, textView39,
             textView41, textView42, textView43, textView44, textView45, textView46, textView47, textView48, textView49,
             textView51, textView52, textView53, textView54, textView55, textView56, textView57, textView58, textView59,
             textView66, textView67, textView68, textView69;

    // Text Views for Manual Analysis Table
    TextView textView26Manual, textView27Manual, textView28Manual, textView29Manual,
             textView36Manual, textView37Manual, textView38Manual, textView39Manual,
             textView46Manual, textView47Manual, textView48Manual, textView49Manual,
             textView56Manual, textView57Manual, textView58Manual, textView59Manual,
             textView66Manual, textView67Manual, textView68Manual, textView69Manual;

    // Edit Views for Manual Analysis Table
    EditText editText21, editText22, editText23, editText24, editText25,
             editText31, editText32, editText33, editText34, editText35,
             editText41, editText42, editText43, editText44, editText45,
             editText51, editText52, editText53, editText54, editText55;

    // Edit Views for Single Analysis
    EditText editText1, editText2, editText3, editText4, editText5, editText6, editText7, editText8, editText9;

    Double exteriorTemperature = 0.0;
    Double interiorTemperature = 0.0;
    Double roofTemperature = 0.0;

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
        setContentView(R.layout.activity_analysis_table);

        //Auto Filled Table

        //Row 2
        textView21 = findViewById(R.id.textView21);
        textView22 = findViewById(R.id.textView22);
        textView23 = findViewById(R.id.textView23);
        textView24 = findViewById(R.id.textView24);
        textView25 = findViewById(R.id.textView25);
        textView26 = findViewById(R.id.textView26);
        textView27 = findViewById(R.id.textView27);
        textView28 = findViewById(R.id.textView28);
        textView29 = findViewById(R.id.textView29);

        //Row 3
        textView31 = findViewById(R.id.textView31);
        textView32 = findViewById(R.id.textView32);
        textView33 = findViewById(R.id.textView33);
        textView34 = findViewById(R.id.textView34);
        textView35 = findViewById(R.id.textView35);
        textView36 = findViewById(R.id.textView36);
        textView37 = findViewById(R.id.textView37);
        textView38 = findViewById(R.id.textView38);
        textView39 = findViewById(R.id.textView39);

        //Row 4
        textView41 = findViewById(R.id.textView41);
        textView42 = findViewById(R.id.textView42);
        textView43 = findViewById(R.id.textView43);
        textView44 = findViewById(R.id.textView44);
        textView45 = findViewById(R.id.textView45);
        textView46 = findViewById(R.id.textView46);
        textView47 = findViewById(R.id.textView47);
        textView48 = findViewById(R.id.textView48);
        textView49 = findViewById(R.id.textView49);

       //Row 5
        textView51 = findViewById(R.id.textView51);
        textView52 = findViewById(R.id.textView52);
        textView53 = findViewById(R.id.textView53);
        textView54 = findViewById(R.id.textView54);
        textView55 = findViewById(R.id.textView55);
        textView56 = findViewById(R.id.textView56);
        textView57 = findViewById(R.id.textView57);
        textView58 = findViewById(R.id.textView58);
        textView59 = findViewById(R.id.textView59);

        //Row 6
        textView66 = findViewById(R.id.textView66);
        textView67 = findViewById(R.id.textView67);
        textView68 = findViewById(R.id.textView68);
        textView69 = findViewById(R.id.textView69);

        //Manually Filled Table

        //Row 2
        editText21 = findViewById(R.id.editTextView21Manual);
        editText22 = findViewById(R.id.editTextView22Manual);
        editText23 = findViewById(R.id.editTextView23Manual);
        editText24 = findViewById(R.id.editTextView24Manual);
        editText25 = findViewById(R.id.editTextView25Manual);
        textView26Manual = findViewById(R.id.textView26Manual);
        textView27Manual = findViewById(R.id.textView27Manual);
        textView28Manual = findViewById(R.id.textView28Manual);
        textView29Manual = findViewById(R.id.textView29Manual);

        //Row 3
        editText31 = findViewById(R.id.editTextView31Manual);
        editText32 = findViewById(R.id.editTextView32Manual);
        editText33 = findViewById(R.id.editTextView33Manual);
        editText34 = findViewById(R.id.editTextView34Manual);
        editText35 = findViewById(R.id.editTextView35Manual);
        textView36Manual = findViewById(R.id.textView36Manual);
        textView37Manual = findViewById(R.id.textView37Manual);
        textView38Manual = findViewById(R.id.textView38Manual);
        textView39Manual = findViewById(R.id.textView39Manual);

        //Row 4
        editText41 = findViewById(R.id.editTextView41Manual);
        editText42 = findViewById(R.id.editTextView42Manual);
        editText43 = findViewById(R.id.editTextView43Manual);
        editText44 = findViewById(R.id.editTextView44Manual);
        editText45 = findViewById(R.id.editTextView45Manual);
        textView46Manual = findViewById(R.id.textView46Manual);
        textView47Manual = findViewById(R.id.textView47Manual);
        textView48Manual = findViewById(R.id.textView48Manual);
        textView49Manual = findViewById(R.id.textView49Manual);

        //Row 5
        editText51 = findViewById(R.id.editTextView51Manual);
        editText52 = findViewById(R.id.editTextView52Manual);
        editText53 = findViewById(R.id.editTextView53Manual);
        editText54 = findViewById(R.id.editTextView54Manual);
        editText55 = findViewById(R.id.editTextView55Manual);
        textView56Manual = findViewById(R.id.textView56Manual);
        textView57Manual = findViewById(R.id.textView57Manual);
        textView58Manual = findViewById(R.id.textView58Manual);
        textView59Manual = findViewById(R.id.textView59Manual);

        //Row 6
        textView66Manual = findViewById(R.id.textView66Manual);
        textView67Manual = findViewById(R.id.textView67Manual);
        textView68Manual = findViewById(R.id.textView68Manual);
        textView69Manual = findViewById(R.id.textView69Manual);

        // Single Manual Analysis
        editText1 = findViewById(R.id.r_value_edit);
        editText2 = findViewById(R.id.exterior_temperature_edit);
        editText3 = findViewById(R.id.interior_temperature_edit);
        editText5 = findViewById(R.id.length_edit);
        editText6 = findViewById(R.id.width_edit);
        editText4 = findViewById(R.id.q1_edit);
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
                                double currentInteriorTemperature = Double.parseDouble(latestRow[2]);
                                double currentExteriorTemperature = Double.parseDouble(latestRow[5]);
                                double currentRoofTemperature = Double.parseDouble(latestRow[4]);

                                exteriorTemperature = Math.round(currentExteriorTemperature * Math.pow(10, 2)) / Math.pow(10, 2);
                                interiorTemperature = Math.round(currentInteriorTemperature * Math.pow(10, 2)) / Math.pow(10, 2);
                                roofTemperature = Math.round(currentRoofTemperature * Math.pow(10, 2)) / Math.pow(10, 2);
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

                        //Calculations for Wall 1, 3
                        double q1Wall13           = (exteriorTemperature - interiorTemperature)/(rValueRoom);   // Btu/[h*ft^2]
                        double q2Wall13           = 2 * q1Wall13 * length1 * width1;                            // Btu/hr
                        double q3Wall13           = q2Wall13 * constantKWhr;                                    // kW
                        double q4Wall13           = Math.abs(q3Wall13 * constantDollarsPerKWhr);                // $/kWh
                        double q1roundedWall13    = Math.round(q1Wall13 * Math.pow(10, 4)) / Math.pow(10, 4);
                        double q2roundedWall13    = Math.round(q2Wall13 * Math.pow(10, 4)) / Math.pow(10, 4);
                        double q3roundedWall13    = Math.abs(Math.round(q3Wall13 * Math.pow(10, 4)) / Math.pow(10, 4));
                        double q4roundedWall13    = Math.abs(Math.round(q4Wall13 * Math.pow(10, 4)) / Math.pow(10, 4));

                        //Calculations for Wall 2, 4
                        double q1Wall24           = (exteriorTemperature - interiorTemperature)/(rValueRoom);   // Btu/[h*ft^2]
                        double q2Wall24           = 2 * q1Wall24 * length2 * width1;                            // Btu/hr
                        double q3Wall24           = q2Wall24 * constantKWhr;                                    // kW
                        double q4Wall24           = Math.abs(q3Wall24 * constantDollarsPerKWhr);                // $/kWh
                        double q1roundedWall24    = Math.round(q1Wall24 * Math.pow(10, 4)) / Math.pow(10, 4);
                        double q2roundedWall24    = Math.round(q2Wall24 * Math.pow(10, 4)) / Math.pow(10, 4);
                        double q3roundedWall24    = Math.abs(Math.round(q3Wall24 * Math.pow(10, 4)) / Math.pow(10, 4));
                        double q4roundedWall24    = Math.abs(Math.round(q4Wall24 * Math.pow(10, 4)) / Math.pow(10, 4));

                        //Calculations for roof
                        double q1Roof           = (exteriorTemperature - roofTemperature)/(rValueRoof);     // Btu/[h*ft^2]
                        double q2Roof           = 2 * q1Roof * length1 * width2;                            // Btu/hr
                        double q3Roof           = q2Roof * constantKWhr;                                    // kW
                        double q4Roof           = Math.abs(q3Roof * constantDollarsPerKWhr);                // $/kWh
                        double q1roundedRoof    = Math.round(q1Roof * Math.pow(10, 4)) / Math.pow(10, 4);
                        double q2roundedRoof    = Math.round(q2Roof * Math.pow(10, 4)) / Math.pow(10, 4);
                        double q3roundedRoof    = Math.abs(Math.round(q3Roof * Math.pow(10, 4)) / Math.pow(10, 4));
                        double q4roundedRoof    = Math.abs(Math.round(q4Roof * Math.pow(10, 4)) / Math.pow(10, 4));

                        //Calculations for floor
                        double q1Floor           = (exteriorTemperature - interiorTemperature)/(rValueFloor);   // Btu/[h*ft^2]
                        double q2Floor           = 2 * q1Floor * length1 * width2;                              // Btu/hr
                        double q3Floor           = q2Floor * constantKWhr;                                      // kW
                        double q4Floor           = Math.abs(q3Floor * constantDollarsPerKWhr);                  // $/kWh
                        double q1roundedFloor    = Math.round(q1Floor * Math.pow(10, 4)) / Math.pow(10, 4);
                        double q2roundedFloor    = Math.round(q2Floor * Math.pow(10, 4)) / Math.pow(10, 4);
                        double q3roundedFloor    = Math.abs(Math.round(q3Floor * Math.pow(10, 4)) / Math.pow(10, 4));
                        double q4roundedFloor    = Math.abs(Math.round(q4Floor * Math.pow(10, 4)) / Math.pow(10, 4));

                        //Calculations for Q Totals
                        double q1Total           = q1Wall13 + q1Wall24 + q1Roof + q1Floor; // Btu/[h*ft^2]
                        double q2Total           = q2Wall13 + q2Wall24 + q2Roof + q2Floor; // Btu/hr
                        double q3Total           = q3Wall13 + q3Wall24 + q3Roof + q3Floor; // kW
                        double q4Total           = q4Wall13 + q4Wall24 + q4Roof + q4Floor; // $/kWh
                        double q1TotalRounded    = Math.round(q1Total * Math.pow(10, 4)) / Math.pow(10, 4);
                        double q2TotalRounded    = Math.round(q2Total * Math.pow(10, 4)) / Math.pow(10, 4);
                        double q3TotalRounded    = Math.abs(Math.round(q3Total * Math.pow(10, 4)) / Math.pow(10, 4));
                        double q4TotalRounded    = Math.abs(Math.round(q4Total * Math.pow(10, 4)) / Math.pow(10, 4));

                        //Converting Double to String

                        //Wall 1, 3
                        String rValueRoomWall13           = Double.toString(rValueRoom);
                        String exteriorTempStringWall13   = Double.toString(exteriorTemperature);
                        String interiorTempStringWall13   = Double.toString(interiorTemperature);
                        String q1stringWall13             = Double.toString(q1roundedWall13);
                        String lengthStringWall13         = Double.toString(length1);
                        String widthStringWall13          = Double.toString(width1);
                        String q2stringWall13             = Double.toString(q2roundedWall13);
                        String q3stringWall13             = Double.toString(q3roundedWall13);
                        String q4stringWall13             = Double.toString(q4roundedWall13);

                        //Wall 2, 4
                        String rValueRoomWall24           = Double.toString(rValueRoom);
                        String exteriorTempStringWall24   = Double.toString(exteriorTemperature);
                        String interiorTempStringWall24   = Double.toString(interiorTemperature);
                        String q1stringWall24             = Double.toString(q1roundedWall24);
                        String lengthStringWall24         = Double.toString(length2);
                        String widthStringWall24          = Double.toString(width1);
                        String q2stringWall24             = Double.toString(q2roundedWall24);
                        String q3stringWall24             = Double.toString(q3roundedWall24);
                        String q4stringWall24             = Double.toString(q4roundedWall24);

                        //Roof
                        String rValueRoofString           = Double.toString(rValueRoof);
                        String exteriorTempStringRoof     = Double.toString(exteriorTemperature);
                        String interiorTempStringRoof     = Double.toString(roofTemperature);
                        String q1stringRoof               = Double.toString(q1roundedRoof);
                        String lengthStringRoof           = Double.toString(length1);
                        String widthStringRoof            = Double.toString(width2);
                        String q2stringRoof               = Double.toString(q2roundedRoof);
                        String q3stringRoof               = Double.toString(q3roundedRoof);
                        String q4stringRoof               = Double.toString(q4roundedRoof);

                        //Floor
                        String rValueFloorString        = Double.toString(rValueFloor);
                        String exteriorTempStringFloor  = Double.toString(exteriorTemperature);
                        String interiorTempStringFloor  = Double.toString(interiorTemperature);
                        String q1stringFloor            = Double.toString(q1roundedFloor);
                        String lengthStringFloor        = Double.toString(length1);
                        String widthStringFloor         = Double.toString(width2);
                        String q2stringFloor            = Double.toString(q2roundedFloor);
                        String q3stringFloor            = Double.toString(q3roundedFloor);
                        String q4stringFloor            = Double.toString(q4roundedFloor);

                        //Total
                        String q1stringTotal = Double.toString(q1TotalRounded);
                        String q2stringTotal = Double.toString(q2TotalRounded);
                        String q3stringTotal = Double.toString(q3TotalRounded);
                        String q4stringTotal = Double.toString(q4TotalRounded);

                        //Populating the Table cells

                        //Row 2
                        textView21.setText(rValueRoomWall13);
                        textView22.setText(exteriorTempStringWall13);
                        textView23.setText(interiorTempStringWall13);
                        textView24.setText(lengthStringWall13);
                        textView25.setText(widthStringWall13);
                        textView26.setText(q1stringWall13);
                        textView27.setText(q2stringWall13);
                        textView28.setText(q3stringWall13);
                        textView29.setText(q4stringWall13);

                        //Row 3
                        textView31.setText(rValueRoomWall24);
                        textView32.setText(exteriorTempStringWall24);
                        textView33.setText(interiorTempStringWall24);
                        textView34.setText(lengthStringWall24);
                        textView35.setText(widthStringWall24);
                        textView36.setText(q1stringWall24);
                        textView37.setText(q2stringWall24);
                        textView38.setText(q3stringWall24);
                        textView39.setText(q4stringWall24);

                        //Row 4
                        textView41.setText(rValueRoofString);
                        textView42.setText(exteriorTempStringRoof);
                        textView43.setText(interiorTempStringRoof);
                        textView44.setText(lengthStringRoof);
                        textView45.setText(widthStringRoof);
                        textView46.setText(q1stringRoof);
                        textView47.setText(q2stringRoof);
                        textView48.setText(q3stringRoof);
                        textView49.setText(q4stringRoof);

                        //Row 5
                        textView51.setText(rValueFloorString);
                        textView52.setText(exteriorTempStringFloor);
                        textView53.setText(interiorTempStringFloor);
                        textView54.setText(lengthStringFloor);
                        textView55.setText(widthStringFloor);
                        textView56.setText(q1stringFloor);
                        textView57.setText(q2stringFloor);
                        textView58.setText(q3stringFloor);
                        textView59.setText(q4stringFloor);

                        //Row 6
                        textView66.setText(q1stringTotal);
                        textView67.setText(q2stringTotal);
                        textView68.setText(q3stringTotal);
                        textView69.setText(q4stringTotal);

                        while_boolean_instant = false;
                    }
                }
            }
        });

        calculateTable = findViewById(R.id.calculate_for_manual_table);
        calculateTable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(editText21.getText().length() == 0){ editText21.setText("0"); }
                if(editText31.getText().length() == 0){ editText31.setText("0"); }
                if(editText41.getText().length() == 0){ editText41.setText("0"); }
                if(editText51.getText().length() == 0){ editText51.setText("0"); }
                double r_value_wall13 = Double.parseDouble(editText21.getText().toString().trim())*1.00;
                double r_value_wall24 = Double.parseDouble(editText31.getText().toString().trim())*1.00;
                double r_value_roof   = Double.parseDouble(editText41.getText().toString().trim())*1.00;
                double r_value_floor  = Double.parseDouble(editText51.getText().toString().trim())*1.00;

                if(editText22.getText().length() == 0){ editText22.setText("0"); }
                if(editText32.getText().length() == 0){ editText32.setText("0"); }
                if(editText42.getText().length() == 0){ editText42.setText("0"); }
                if(editText52.getText().length() == 0){ editText52.setText("0"); }
                double exterior_temperature_wall13 = Double.parseDouble(editText22.getText().toString().trim())*1.00;
                double exterior_temperature_wall24 = Double.parseDouble(editText32.getText().toString().trim())*1.00;
                double exterior_temperature_roof   = Double.parseDouble(editText42.getText().toString().trim())*1.00;
                double exterior_temperature_floor  = Double.parseDouble(editText52.getText().toString().trim())*1.00;

                if(editText23.getText().length() == 0){ editText23.setText("0"); }
                if(editText33.getText().length() == 0){ editText33.setText("0"); }
                if(editText43.getText().length() == 0){ editText43.setText("0"); }
                if(editText53.getText().length() == 0){ editText53.setText("0"); }
                double interior_temperature_wall13 = Double.parseDouble(editText23.getText().toString().trim())*1.00;
                double interior_temperature_wall24 = Double.parseDouble(editText33.getText().toString().trim())*1.00;
                double roof_temperature            = Double.parseDouble(editText43.getText().toString().trim())*1.00;
                double floor_temperature           = Double.parseDouble(editText53.getText().toString().trim())*1.00;

                if(editText24.getText().length() == 0){ editText24.setText("0"); }
                if(editText34.getText().length() == 0){ editText34.setText("0"); }
                if(editText44.getText().length() == 0){ editText44.setText("0"); }
                if(editText54.getText().length() == 0){ editText54.setText("0"); }
                double length_wall13 = Double.parseDouble(editText24.getText().toString().trim())*1.00;
                double length_wall24 = Double.parseDouble(editText34.getText().toString().trim())*1.00;
                double length_roof   = Double.parseDouble(editText44.getText().toString().trim())*1.00;
                double length_floor  = Double.parseDouble(editText54.getText().toString().trim())*1.00;

                if(editText25.getText().length() == 0){ editText25.setText("0"); }
                if(editText35.getText().length() == 0){ editText35.setText("0"); }
                if(editText45.getText().length() == 0){ editText45.setText("0"); }
                if(editText55.getText().length() == 0){ editText55.setText("0"); }
                double width_wall13 = Double.parseDouble(editText25.getText().toString().trim())*1.00;
                double width_wall24 = Double.parseDouble(editText35.getText().toString().trim())*1.00;
                double width_roof   = Double.parseDouble(editText45.getText().toString().trim())*1.00;
                double width_floor  = Double.parseDouble(editText55.getText().toString().trim())*1.00;

                //Calculations for Wall 1, 3
                double q1Wall13           = (exterior_temperature_wall13 - interior_temperature_wall13)/(r_value_wall13); // Btu/[h*ft^2]
                double q2Wall13           = 2 * q1Wall13 * length_wall13 * width_wall13;        // Btu/hr
                double q3Wall13           = q2Wall13 * constantKWhr;            // kW
                double q4Wall13           = Math.abs(q3Wall13 * constantDollarsPerKWhr);  // $/kWh
                double q1roundedWall13    = Math.round(q1Wall13 * Math.pow(10, 4)) / Math.pow(10, 4);
                double q2roundedWall13    = Math.round(q2Wall13 * Math.pow(10, 4)) / Math.pow(10, 4);
                double q3roundedWall13    = Math.abs(Math.round(q3Wall13 * Math.pow(10, 4)) / Math.pow(10, 4));
                double q4roundedWall13    = Math.abs(Math.round(q4Wall13 * Math.pow(10, 4)) / Math.pow(10, 4));

                //Calculations for Wall 2, 4
                double q1Wall24           = (exterior_temperature_wall24 - interior_temperature_wall24)/(r_value_wall24); // Btu/[h*ft^2]
                double q2Wall24           = 2 * q1Wall24 * length_wall24 * width_wall24;        // Btu/hr
                double q3Wall24           = q2Wall24 * constantKWhr;            // kW
                double q4Wall24           = Math.abs(q3Wall24 * constantDollarsPerKWhr);  // $/kWh
                double q1roundedWall24    = Math.round(q1Wall24 * Math.pow(10, 4)) / Math.pow(10, 4);
                double q2roundedWall24    = Math.round(q2Wall24 * Math.pow(10, 4)) / Math.pow(10, 4);
                double q3roundedWall24    = Math.abs(Math.round(q3Wall24 * Math.pow(10, 4)) / Math.pow(10, 4));
                double q4roundedWall24    = Math.abs(Math.round(q4Wall24 * Math.pow(10, 4)) / Math.pow(10, 4));

                //Calculations for roof
                double q1Roof           = (exterior_temperature_roof - roof_temperature)/(r_value_roof); // Btu/[h*ft^2]
                double q2Roof           = 2 * q1Roof * length_roof * width_roof;        // Btu/hr
                double q3Roof           = q2Roof * constantKWhr;            // kW
                double q4Roof           = Math.abs(q3Roof * constantDollarsPerKWhr);  // $/kWh
                double q1roundedRoof    = Math.round(q1Roof * Math.pow(10, 4)) / Math.pow(10, 4);
                double q2roundedRoof    = Math.round(q2Roof * Math.pow(10, 4)) / Math.pow(10, 4);
                double q3roundedRoof    = Math.abs(Math.round(q3Roof * Math.pow(10, 4)) / Math.pow(10, 4));
                double q4roundedRoof    = Math.abs(Math.round(q4Roof * Math.pow(10, 4)) / Math.pow(10, 4));

                //Calculations for floor
                double q1Floor           = (exterior_temperature_floor - floor_temperature)/(r_value_floor); // Btu/[h*ft^2]
                double q2Floor           = 2 * q1Floor * length_floor * width_floor;        // Btu/hr
                double q3Floor           = q2Floor * constantKWhr;            // kW
                double q4Floor           = Math.abs(q3Floor * constantDollarsPerKWhr);  // $/kWh
                double q1roundedFloor    = Math.round(q1Floor * Math.pow(10, 4)) / Math.pow(10, 4);
                double q2roundedFloor    = Math.round(q2Floor * Math.pow(10, 4)) / Math.pow(10, 4);
                double q3roundedFloor    = Math.abs(Math.round(q3Floor * Math.pow(10, 4)) / Math.pow(10, 4));
                double q4roundedFloor    = Math.abs(Math.round(q4Floor * Math.pow(10, 4)) / Math.pow(10, 4));

                //Calculations for Q Totals
                double q1Total           = q1roundedWall13 + q1roundedWall24 + q1roundedRoof + q1roundedFloor; // Btu/[h*ft^2]
                double q2Total           = q2roundedWall13 + q2roundedWall24 + q2roundedRoof + q2roundedFloor;        // Btu/hr
                double q3Total           = q3roundedWall13 + q3roundedWall24 + q3roundedRoof + q3roundedFloor;            // kW
                double q4Total           = q4roundedWall13 + q4roundedWall24 + q4roundedRoof + q4roundedFloor;  // $/kWh

                //Converting Double to String

                //Wall 1, 3
                String q1stringWall13   = Double.toString(q1roundedWall13);
                String q2stringWall13   = Double.toString(q2roundedWall13);
                String q3stringWall13   = Double.toString(q3roundedWall13);
                String q4stringWall13   = Double.toString(q4roundedWall13);

                //Wall 2, 4
                String q1stringWall24   = Double.toString(q1roundedWall24);
                String q2stringWall24   = Double.toString(q2roundedWall24);
                String q3stringWall24   = Double.toString(q3roundedWall24);
                String q4stringWall24   = Double.toString(q4roundedWall24);

                //Roof
                String q1stringRoof     = Double.toString(q1roundedRoof);
                String q2stringRoof     = Double.toString(q2roundedRoof);
                String q3stringRoof     = Double.toString(q3roundedRoof);
                String q4stringRoof     = Double.toString(q4roundedRoof);

                //Floor
                String q1stringFloor    = Double.toString(q1roundedFloor);
                String q2stringFloor    = Double.toString(q2roundedFloor);
                String q3stringFloor    = Double.toString(q3roundedFloor);
                String q4stringFloor    = Double.toString(q4roundedFloor);

                //Total
                String q1stringTotal = Double.toString(q1Total);
                String q2stringTotal = Double.toString(q2Total);
                String q3stringTotal = Double.toString(q3Total);
                String q4stringTotal = Double.toString(q4Total);

                //Populating the Table cells

                //Row 2
                textView26Manual.setText(q1stringWall13);
                textView27Manual.setText(q2stringWall13);
                textView28Manual.setText(q3stringWall13);
                textView29Manual.setText(q4stringWall13);

                //Row 3
                textView36Manual.setText(q1stringWall24);
                textView37Manual.setText(q2stringWall24);
                textView38Manual.setText(q3stringWall24);
                textView39Manual.setText(q4stringWall24);

                //Row 4
                textView46Manual.setText(q1stringRoof);
                textView47Manual.setText(q2stringRoof);
                textView48Manual.setText(q3stringRoof);
                textView49Manual.setText(q4stringRoof);

                //Row 5
                textView56Manual.setText(q1stringFloor);
                textView57Manual.setText(q2stringFloor);
                textView58Manual.setText(q3stringFloor);
                textView59Manual.setText(q4stringFloor);

                //Row 6
                textView66Manual.setText(q1stringTotal);
                textView67Manual.setText(q2stringTotal);
                textView68Manual.setText(q3stringTotal);
                textView69Manual.setText(q4stringTotal);
            }
        });

        calculate = findViewById(R.id.calculate_for_all);
        calculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(editText1.getText().length() == 0){ editText1.setText("0"); }
                if(editText2.getText().length() == 0){ editText2.setText("0"); }
                if(editText3.getText().length() == 0){ editText3.setText("0"); }
                if(editText5.getText().length() == 0){ editText5.setText("0"); }
                if(editText6.getText().length() == 0){ editText6.setText("0"); }
                editText4.setText("0");
                editText7.setText("0");
                editText8.setText("0");
                editText9.setText("0");
                double r_value_editText = Double.parseDouble(editText1.getText().toString().trim())*1.00;
                double exterior_temperature_editText = Double.parseDouble(editText2.getText().toString().trim())*1.00;
                double interior_temperature_editText = Double.parseDouble(editText3.getText().toString().trim())*1.00;
                double length_editText = Double.parseDouble(editText5.getText().toString().trim())*1.00;
                double width_editText = Double.parseDouble(editText6.getText().toString().trim())*1.00;

                double q1           = (exterior_temperature_editText - interior_temperature_editText) / r_value_editText; // Btu/[h*ft^2]
                double q2           = 2 * q1 * length_editText * width_editText;        // Btu/hr
                double q3           = q2 * constantKWhr;            // kW
                double q4           = q3 * constantDollarsPerKWhr;  // $/kWh
                double q1rounded    = Math.round(q1 * Math.pow(10, 2)) / Math.pow(10, 2);
                double q2rounded    = Math.round(q2 * Math.pow(10, 2)) / Math.pow(10, 2);
                double q3rounded    = Math.abs(Math.round(q3 * Math.pow(10, 4)) / Math.pow(10, 4));
                double q4rounded    = Math.abs(Math.round(q4 * Math.pow(10, 4)) / Math.pow(10, 4));

                editText4.setText(Double.toString(q1rounded), TextView.BufferType.EDITABLE);
                editText7.setText(Double.toString(q2rounded), TextView.BufferType.EDITABLE);
                editText8.setText(Double.toString(q3rounded), TextView.BufferType.EDITABLE);
                editText9.setText(Double.toString(q4rounded), TextView.BufferType.EDITABLE);
            }
        });
    }
}