package com.example.solar_decathlon_house_numerical;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import android.util.Log;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;

public class Temperature extends AppCompatActivity {
    private static final String TAG = Temperature.class.getSimpleName();
    private GraphView lineGraph1, lineGraph2, lineGraph3, lineGraph4, lineGraph5, lineGraph6, lineGraph7;
    TextView avgTxtView, maxTxtView, minTxtView;
    Button avgTempButton, maxTempButton, minTempButton;
    double avgTemp, roundedMaxTemp, roundedMinTemp;
    String user = "rpihubteam6";  //Samba User name
    String pass ="raspberrypi";   //Samba Password
    String sharedFolder="share";  //Samba Shared folder
    String domain = "rpihubteam6";    //Samba domain name
    String ipAddressWireless = "192.168.1.10"; //IP address for rpihubteam6 when it is wirelessly connected with the router
    //String ipAddressEthernet = "192.168.1.11"; //IP address for rpihubteam6 when it is wired with the router

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature);

        lineGraph1 = findViewById(R.id.graph_for_interior_temperature);
        lineGraph2 = findViewById(R.id.graph_for_solar_panel_temperature);
        lineGraph3 = findViewById(R.id.graph_for_roof_temperature);
        lineGraph4 = findViewById(R.id.graph_for_outside_temperature);
        lineGraph5 = findViewById(R.id.graph_for_north_wall);
        lineGraph6 = findViewById(R.id.graph_for_water_tank);
        lineGraph7 = findViewById(R.id.graph_for_water_solar_collector);


        //Average Temperature on the South Wall Sensor
        avgTempButton = findViewById(R.id.average_south_wall_temperature);
        avgTxtView = findViewById(R.id.textView10);
        avgTxtView.setVisibility(TextView.INVISIBLE);
        avgTempButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
            try{
                //Creating a new thread for the file transfer, this takes the load off the main thread.
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                    try
                    {
                        //To get Samba Shared file from the Raspberry Pi
                        List<String[]> temperature;

                        String url1 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/heatsignature.csv";
                        NtlmPasswordAuthentication auth1 = new NtlmPasswordAuthentication(domain, user, pass);
                        InputStream smbTemperatureFile = new SmbFile(url1, auth1).getInputStream();
                        CSVReader csv_temperature = new CSVReader(smbTemperatureFile, "heat");
                        temperature = csv_temperature.read();

                        double sumOfTemp = 0.0;

                        for (int i = 0; i < temperature.size(); i++) {
                            String[] rows = temperature.get(i);
                            sumOfTemp += Double.parseDouble(rows[4]);
                        }

                        double temp = sumOfTemp/(temperature.size());

                        avgTemp = Math.round(temp * Math.pow(10, 2)) / Math.pow(10, 2); //To round off to two decimal places.

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    }
                });

                thread.start();
                avgTxtView.setVisibility(TextView.VISIBLE);
                String units = " Fahrenheit";
                String value =  Double.toString(avgTemp);
                avgTxtView.setText(value + units);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            }
        });

        maxTempButton = findViewById(R.id.max_south_wall_temperature);
        maxTxtView = findViewById(R.id.textView11);
        maxTxtView.setVisibility(TextView.INVISIBLE);
        maxTempButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
            try{
                //Creating a new thread for the file transfer, this takes the load off the main thread.
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                    try
                    {
                        //To get Samba Shared file from the Raspberry Pi
                        List<String[]> temperature;

                        String url1 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/heatsignature.csv";
                        NtlmPasswordAuthentication auth1 = new NtlmPasswordAuthentication(domain, user, pass);
                        InputStream smbTemperatureFile = new SmbFile(url1, auth1).getInputStream();
                        CSVReader csv_temperature = new CSVReader(smbTemperatureFile, "heat");//CSVReader(inputStream2);
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

                        roundedMaxTemp = Math.round(maxTemp * Math.pow(10, 2)) / Math.pow(10, 2); //To round off to two decimal places.

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    }
                });

                thread.start();
                maxTxtView.setVisibility(TextView.VISIBLE);
                String units = " Fahrenheit";
                String value =  Double.toString(roundedMaxTemp);
                maxTxtView.setText(value +  units);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            }
        });

        minTempButton = findViewById(R.id.min_south_wall_temperature);
        minTxtView = findViewById(R.id.textView12);
        minTxtView.setVisibility(TextView.INVISIBLE);
        minTempButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
            try{
                //Creating a new thread for the file transfer, this takes the load off the main thread.
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                    try
                    {
                        //To get Samba Shared file from the Raspberry Pi
                        List<String[]> temperature;

                        String url1 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/heatsignature.csv";
                        NtlmPasswordAuthentication auth1 = new NtlmPasswordAuthentication(domain, user, pass);
                        InputStream smbTemperatureFile = new SmbFile(url1, auth1).getInputStream();
                        CSVReader csv_temperature = new CSVReader(smbTemperatureFile, "heat");//CSVReader(inputStream2);
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

                        roundedMinTemp = Math.round(minTemp * Math.pow(10, 2)) / Math.pow(10, 2); //To round off to two decimal places.

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    }
                });

                thread.start();
                minTxtView.setVisibility(TextView.VISIBLE);
                String units = " Fahrenheit";
                String value =  Double.toString(roundedMinTemp);
                minTxtView.setText(value +  units);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
        });

        Button interiorTempButton = (Button)findViewById(R.id.interior_temperature);
        interiorTempButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
                try{
                    //Creating a new thread for the file transfer, this takes the load off the main thread.
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                        try
                        {
                            while(true) {
                                int currentTimeMillis = (int) System.currentTimeMillis();
                                int seconds = currentTimeMillis / 1000;

                                if((seconds % 5) == 0) {
                                    //To get Samba Shared file from the Raspberry Pi
                                    List<String[]> temperature;

                                    String url1 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/heatsignature.csv";
                                    NtlmPasswordAuthentication auth1 = new NtlmPasswordAuthentication(domain, user, pass);
                                    InputStream smbTemperatureFile = new SmbFile(url1, auth1).getInputStream();
                                    CSVReader csv_temperature = new CSVReader(smbTemperatureFile, "heat");//CSVReader(inputStream2);
                                    temperature = csv_temperature.read();

                                    //Creating line graphs
                                    createLineGraph(temperature, "interior_temperature");
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        }
                    });

                    thread.start();
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
        });

        Button solarPanelTempButton = findViewById(R.id.solar_panel_temperature);
        solarPanelTempButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
            try{
                //Creating a new thread for the file transfer, this takes the load off the main thread.
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try
                        {
                            //To get Samba Shared file from the Raspberry Pi
                            List<String[]> temperature;

                            String url1 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/heatsignature.csv";
                            NtlmPasswordAuthentication auth1 = new NtlmPasswordAuthentication(domain, user, pass);
                            InputStream smbTemperatureFile = new SmbFile(url1, auth1).getInputStream();
                            CSVReader csv_temperature = new CSVReader(smbTemperatureFile, "heat");//CSVReader(inputStream2);
                            temperature = csv_temperature.read();

                            //Creating line graphs
                            createLineGraph(temperature, "solar_panel_temperature");

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
            }
        });

        Button roofTempButton = (Button)findViewById(R.id.roof_temperature);
        roofTempButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
            try{
                //Creating a new thread for the file transfer, this takes the load off the main thread.
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try
                        {
                            //To get Samba Shared file from the Raspberry Pi
                            List<String[]> temperature;

                            String url1 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/heatsignature.csv";
                            NtlmPasswordAuthentication auth1 = new NtlmPasswordAuthentication(domain, user, pass);
                            InputStream smbTemperatureFile = new SmbFile(url1, auth1).getInputStream();
                            CSVReader csv_temperature = new CSVReader(smbTemperatureFile, "heat");//CSVReader(inputStream2);
                            temperature = csv_temperature.read();

                            //Creating line graphs
                            createLineGraph(temperature, "roof_temperature");

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
            }
        });

        Button outsideTempButton = (Button)findViewById(R.id.outside_temperature);
        outsideTempButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
            try{
                //Creating a new thread for the file transfer, this takes the load off the main thread.
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try
                        {
                            //To get Samba Shared file from the Raspberry Pi
                            List<String[]> temperature;

                            String url1 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/heatsignature.csv";
                            NtlmPasswordAuthentication auth1 = new NtlmPasswordAuthentication(domain, user, pass);
                            InputStream smbTemperatureFile = new SmbFile(url1, auth1).getInputStream();
                            CSVReader csv_temperature = new CSVReader(smbTemperatureFile, "heat");//CSVReader(inputStream2);
                            temperature = csv_temperature.read();

                            //Creating line graphs
                            createLineGraph(temperature, "outside_temperature");

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
            }
        });

        Button northwallTempButton = (Button)findViewById(R.id.north_wall);
        northwallTempButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
            try{
                //Creating a new thread for the file transfer, this takes the load off the main thread.
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try
                        {
                            //To get Samba Shared file from the Raspberry Pi
                            List<String[]> temperature;

                            String url1 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/heatsignature.csv";
                            NtlmPasswordAuthentication auth1 = new NtlmPasswordAuthentication(domain, user, pass);
                            InputStream smbTemperatureFile = new SmbFile(url1, auth1).getInputStream();
                            CSVReader csv_temperature = new CSVReader(smbTemperatureFile, "heat");//CSVReader(inputStream2);
                            temperature = csv_temperature.read();

                            //Creating line graphs
                            createLineGraph(temperature, "north_wall");

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
            }
        });

        Button waterTankTempButton = (Button)findViewById(R.id.water_tank);
        waterTankTempButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
            try{
                //Creating a new thread for the file transfer, this takes the load off the main thread.
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try
                        {
                            //To get Samba Shared file from the Raspberry Pi
                            List<String[]> temperature;

                            String url1 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/heatsignature.csv";
                            NtlmPasswordAuthentication auth1 = new NtlmPasswordAuthentication(domain, user, pass);
                            InputStream smbTemperatureFile = new SmbFile(url1, auth1).getInputStream();
                            CSVReader csv_temperature = new CSVReader(smbTemperatureFile, "heat");//CSVReader(inputStream2);
                            temperature = csv_temperature.read();

                            //Creating line graphs
                            createLineGraph(temperature, "water_tank");

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
            }
        });

        Button waterSolarCollectorTempButton = (Button)findViewById(R.id.water_solar_collector);
        waterSolarCollectorTempButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
            try{
                //Creating a new thread for the file transfer, this takes the load off the main thread.
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try
                        {
                            //To get Samba Shared file from the Raspberry Pi
                            List<String[]> temperature;

                            String url1 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/heatsignature.csv";
                            NtlmPasswordAuthentication auth1 = new NtlmPasswordAuthentication(domain, user, pass);
                            InputStream smbTemperatureFile = new SmbFile(url1, auth1).getInputStream();
                            CSVReader csv_temperature = new CSVReader(smbTemperatureFile, "heat");//CSVReader(inputStream2);
                            temperature = csv_temperature.read();

                            //Creating line graphs
                            createLineGraph(temperature, "water_solar_collector");

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
            }
        });
    }

    private void createLineGraph(List<String[]> temperature, String graphToPlot) throws ParseException {
        DataPoint[] temperature_dataPoints = new DataPoint[temperature.size()];

        //Interior Temperature
        if(graphToPlot.equals("interior_temperature")) {
            lineGraph1.removeAllSeries();
            lineGraph1.getViewport().setScalable(true);
            lineGraph1.getViewport().setScalableY(true);
            lineGraph1.getGridLabelRenderer().setHorizontalAxisTitle("Time (Minutes)");
            lineGraph1.getGridLabelRenderer().setVerticalAxisTitle("Temperature (Fahrenheit)");
            lineGraph1.getGridLabelRenderer().setLabelsSpace(10);
            lineGraph1.getGridLabelRenderer().setHorizontalLabelsAngle(135);

            Date date = new Date();
            final SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
            for (int i = 0; i < temperature.size(); i++) {
                String[] rows = temperature.get(i);
                Log.d(TAG, "Output: " + rows[0] + " " + Double.parseDouble(rows[1]));
                date = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy").parse(rows[0]);
                temperature_dataPoints[i] = new DataPoint(date, Double.parseDouble(rows[1]));
            }
            LineGraphSeries<DataPoint> series1 = new LineGraphSeries<DataPoint>(temperature_dataPoints);
            series1.setTitle("Interior Temperature");
            series1.setDrawDataPoints(true);
            series1.setDataPointsRadius(8);
            series1.setColor(Color.BLACK);
            series1.setDrawBackground(true);
            series1.setThickness(8);
            series1.setOnDataPointTapListener(new OnDataPointTapListener() {
                @Override
                public void onTap(Series series1, DataPointInterface temperature_dataPoints) {
                    Toast.makeText(getApplicationContext(), "Interior Temperature: ["+ sdf.format(new Date((long) temperature_dataPoints.getX())) + "/" + temperature_dataPoints.getY() +" Fahrenheit]", Toast.LENGTH_LONG).show();
                }
            });
            lineGraph1.addSeries(series1);

            //Label
            lineGraph1.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter(){
                @Override
                public String formatLabel(double value, boolean isValueX) {
                    if(isValueX){
                        return sdf.format(new Date((long) value));
                    }else {
                        return super.formatLabel(value, isValueX);
                    }
                }
            });

            //Legend
            lineGraph1.getLegendRenderer().resetStyles();
            lineGraph1.getLegendRenderer().setMargin(10);
            lineGraph1.getLegendRenderer().setTextColor(Color.WHITE);
            lineGraph1.getLegendRenderer().setVisible(true);
            lineGraph1.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        }

        //solar panel temperature
        if(graphToPlot.equals("solar_panel_temperature")) {
            lineGraph2.removeAllSeries();
            lineGraph2.getViewport().setScalable(true);
            lineGraph2.getViewport().setScalableY(true);
            lineGraph2.getGridLabelRenderer().setHorizontalAxisTitle("Time (Minutes)");
            lineGraph2.getGridLabelRenderer().setVerticalAxisTitle("Temperature (Fahrenheit)");
            lineGraph2.getGridLabelRenderer().setLabelsSpace(10);
            lineGraph2.getGridLabelRenderer().setHorizontalLabelsAngle(135);

            Date date = new Date();
            final SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
            for (int i = 0; i < temperature.size(); i++) {
                String[] rows = temperature.get(i);
                Log.d(TAG, "Output: " + rows[0] + " " + Double.parseDouble(rows[1]));
                date = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy").parse(rows[0]);
                temperature_dataPoints[i] = new DataPoint(date, Double.parseDouble(rows[1]));
            }
            LineGraphSeries<DataPoint> series2 = new LineGraphSeries<DataPoint>(temperature_dataPoints);
            series2.setTitle("Solar Panel Temperature");
            series2.setDrawDataPoints(true);
            series2.setDataPointsRadius(8);
            series2.setColor(Color.RED);
            series2.setDrawBackground(true);
            series2.setThickness(8);
            series2.setOnDataPointTapListener(new OnDataPointTapListener() {
                @Override
                public void onTap(Series series2, DataPointInterface temperature_dataPoints) {
                    Toast.makeText(getApplicationContext(), "Solar Panel Temperature: ["+ sdf.format(new Date((long) temperature_dataPoints.getX())) + "/" + temperature_dataPoints.getY() +" Watts]", Toast.LENGTH_LONG).show();
                }
            });
            lineGraph2.addSeries(series2);

            //Label
            lineGraph2.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter(){
                @Override
                public String formatLabel(double value, boolean isValueX) {
                    if(isValueX){
                        return sdf.format(new Date((long) value));
                    }else {
                        return super.formatLabel(value, isValueX);
                    }
                }
            });

            //Legend
            lineGraph2.getLegendRenderer().resetStyles();
            lineGraph2.getLegendRenderer().setMargin(10);
            lineGraph2.getLegendRenderer().setTextColor(Color.WHITE);
            lineGraph2.getLegendRenderer().setVisible(true);
            lineGraph2.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        }

        //roof temp
        if(graphToPlot.equals("roof_temperature")) {
            lineGraph3.removeAllSeries();
            lineGraph3.getViewport().setScalable(true);
            lineGraph3.getViewport().setScalableY(true);
            lineGraph3.getGridLabelRenderer().setHorizontalAxisTitle("Time (Minutes)");
            lineGraph3.getGridLabelRenderer().setVerticalAxisTitle("Temperature (Fahrenheit)");
            lineGraph3.getGridLabelRenderer().setLabelsSpace(10);
            lineGraph3.getGridLabelRenderer().setHorizontalLabelsAngle(135);

            Date date = new Date();
            final SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
            for (int i = 0; i < temperature.size(); i++) {
                String[] rows = temperature.get(i);
                Log.d(TAG, "Output: " + rows[0] + " " + Double.parseDouble(rows[3]));
                date = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy").parse(rows[0]);
                temperature_dataPoints[i] = new DataPoint(date, Double.parseDouble(rows[3]));
            }
            LineGraphSeries<DataPoint> series3 = new LineGraphSeries<DataPoint>(temperature_dataPoints);
            series3.setTitle("Roof Temperature");
            series3.setDrawDataPoints(true);
            series3.setDataPointsRadius(8);
            series3.setColor(Color.BLUE);
            series3.setDrawBackground(true);
            series3.setThickness(8);
            series3.setOnDataPointTapListener(new OnDataPointTapListener() {
                @Override
                public void onTap(Series series3, DataPointInterface temperature_dataPoints) {
                    Toast.makeText(getApplicationContext(), "Roof Temperature: ["+ sdf.format(new Date((long) temperature_dataPoints.getX())) + "/" + temperature_dataPoints.getY() +" Fahrenheit]", Toast.LENGTH_LONG).show();
                }
            });
            lineGraph3.addSeries(series3);
            //Label
            lineGraph3.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter(){
                @Override
                public String formatLabel(double value, boolean isValueX) {
                    if(isValueX){
                        return sdf.format(new Date((long) value));
                    }else {
                        return super.formatLabel(value, isValueX);
                    }
                }
            });

            //Legend
            lineGraph3.getLegendRenderer().resetStyles();
            lineGraph3.getLegendRenderer().setMargin(10);
            lineGraph3.getLegendRenderer().setTextColor(Color.WHITE);
            lineGraph3.getLegendRenderer().setVisible(true);
            lineGraph3.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        }

        //outside temperature
        if(graphToPlot.equals("outside_temperature")) {
            lineGraph4.removeAllSeries();
            lineGraph4.getViewport().setScalable(true);
            lineGraph4.getViewport().setScalableY(true);
            lineGraph4.getGridLabelRenderer().setHorizontalAxisTitle("Time (Minutes)");
            lineGraph4.getGridLabelRenderer().setVerticalAxisTitle("Temperature (Fahrenheit)");
            lineGraph4.getGridLabelRenderer().setLabelsSpace(10);
            lineGraph4.getGridLabelRenderer().setHorizontalLabelsAngle(135);

            Date date = new Date();
            final SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
            for (int i = 0; i < temperature.size(); i++) {
                String[] rows = temperature.get(i);
                Log.d(TAG, "Output: " + rows[0] + " " + Double.parseDouble(rows[4]));
                date = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy").parse(rows[0]);
                temperature_dataPoints[i] = new DataPoint(date, Double.parseDouble(rows[4]));
            }
            LineGraphSeries<DataPoint> series4 = new LineGraphSeries<DataPoint>(temperature_dataPoints);
            series4.setTitle("Outside Temperature");
            series4.setDrawDataPoints(true);
            series4.setDataPointsRadius(8);
            series4.setColor(Color.GRAY);
            series4.setDrawBackground(true);
            series4.setThickness(8);
            series4.setOnDataPointTapListener(new OnDataPointTapListener() {
                @Override
                public void onTap(Series series4, DataPointInterface temperature_dataPoints) {
                    Toast.makeText(getApplicationContext(), "Outside Temperature: ["+ sdf.format(new Date((long) temperature_dataPoints.getX())) + "/" + temperature_dataPoints.getY() +" Fahrenheit]", Toast.LENGTH_LONG).show();
                }
            });
            lineGraph4.addSeries(series4);

            //Label
            lineGraph4.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter(){
                @Override
                public String formatLabel(double value, boolean isValueX) {
                    if(isValueX){
                        return sdf.format(new Date((long) value));
                    }else {
                        return super.formatLabel(value, isValueX);
                    }
                }
            });

            //Legend
            lineGraph4.getLegendRenderer().resetStyles();
            lineGraph4.getLegendRenderer().setMargin(10);
            lineGraph4.getLegendRenderer().setTextColor(Color.WHITE);
            lineGraph4.getLegendRenderer().setVisible(true);
            lineGraph4.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        }

        //north wall
        if(graphToPlot.equals("north_wall")) {
            lineGraph5.removeAllSeries();
            lineGraph5.getViewport().setScalable(true);
            lineGraph5.getViewport().setScalableY(true);
            lineGraph5.getGridLabelRenderer().setHorizontalAxisTitle("Time (Minutes)");
            lineGraph5.getGridLabelRenderer().setVerticalAxisTitle("Temperature (Fahrenheit)");
            lineGraph5.getGridLabelRenderer().setLabelsSpace(10);
            lineGraph5.getGridLabelRenderer().setHorizontalLabelsAngle(135);

            Date date = new Date();
            final SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
            for (int i = 0; i < temperature.size(); i++) {
                String[] rows = temperature.get(i);
                Log.d(TAG, "Output: " + rows[0] + " " + Double.parseDouble(rows[5]));
                date = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy").parse(rows[0]);
                temperature_dataPoints[i] = new DataPoint(date, Double.parseDouble(rows[5]));
            }
            LineGraphSeries<DataPoint> series5 = new LineGraphSeries<DataPoint>(temperature_dataPoints);
            series5.setTitle("North Wall Temperature");
            series5.setDrawDataPoints(true);
            series5.setDataPointsRadius(8);
            series5.setColor(Color.RED);
            series5.setDrawBackground(true);
            series5.setThickness(8);
            series5.setOnDataPointTapListener(new OnDataPointTapListener() {
                @Override
                public void onTap(Series series5, DataPointInterface temperature_dataPoints) {
                    Toast.makeText(getApplicationContext(), "North Wall Temperature: ["+ sdf.format(new Date((long) temperature_dataPoints.getX())) + "/" + temperature_dataPoints.getY() +" Fahrenheit]", Toast.LENGTH_LONG).show();
                }
            });
            lineGraph5.addSeries(series5);

            //Label
            lineGraph5.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter(){
                @Override
                public String formatLabel(double value, boolean isValueX) {
                    if(isValueX){
                        return sdf.format(new Date((long) value));
                    }else {
                        return super.formatLabel(value, isValueX);
                    }
                }
            });

            //Legend
            lineGraph5.getLegendRenderer().resetStyles();
            lineGraph5.getLegendRenderer().setMargin(10);
            lineGraph5.getLegendRenderer().setTextColor(Color.WHITE);
            lineGraph5.getLegendRenderer().setVisible(true);
            lineGraph5.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        }

        //water tank
        if(graphToPlot.equals("water_tank")) {
            lineGraph6.removeAllSeries();
            lineGraph6.getViewport().setScalable(true);
            lineGraph6.getViewport().setScalableY(true);
            lineGraph6.getGridLabelRenderer().setHorizontalAxisTitle("Time (Minutes)");
            lineGraph6.getGridLabelRenderer().setVerticalAxisTitle("Temperature (Fahrenheit)");
            lineGraph6.getGridLabelRenderer().setLabelsSpace(10);
            lineGraph6.getGridLabelRenderer().setHorizontalLabelsAngle(135);

            Date date = new Date();
            final SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
            for (int i = 0; i < temperature.size(); i++) {
                String[] rows = temperature.get(i);
                Log.d(TAG, "Output: " + rows[0] + " " + Double.parseDouble(rows[6]));
                date = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy").parse(rows[0]);
                temperature_dataPoints[i] = new DataPoint(date, Double.parseDouble(rows[6]));
            }
            LineGraphSeries<DataPoint> series6 = new LineGraphSeries<DataPoint>(temperature_dataPoints);
            series6.setTitle("Water Tank Temperature");
            series6.setDrawDataPoints(true);
            series6.setDataPointsRadius(8);
            series6.setColor(Color.BLUE);
            series6.setDrawBackground(true);
            series6.setThickness(8);
            series6.setOnDataPointTapListener(new OnDataPointTapListener() {
                @Override
                public void onTap(Series series6, DataPointInterface temperature_dataPoints) {
                    Toast.makeText(getApplicationContext(), "Water Tank Temperature: ["+ sdf.format(new Date((long) temperature_dataPoints.getX())) + "/" + temperature_dataPoints.getY() +" Fahrenheit]", Toast.LENGTH_LONG).show();
                }
            });
            lineGraph6.addSeries(series6);

            //Label
            lineGraph6.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter(){
                @Override
                public String formatLabel(double value, boolean isValueX) {
                    if(isValueX){
                        return sdf.format(new Date((long) value));
                    }else {
                        return super.formatLabel(value, isValueX);
                    }
                }
            });

            //Legend
            lineGraph6.getLegendRenderer().resetStyles();
            lineGraph6.getLegendRenderer().setMargin(10);
            lineGraph6.getLegendRenderer().setTextColor(Color.WHITE);
            lineGraph6.getLegendRenderer().setVisible(true);
            lineGraph6.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        }

        //water solar collector
        if(graphToPlot.equals("water_solar_collector")) {
            lineGraph7.removeAllSeries();
            lineGraph7.getViewport().setScalable(true);
            lineGraph7.getViewport().setScalableY(true);
            lineGraph7.getGridLabelRenderer().setHorizontalAxisTitle("Time (Minutes)");
            lineGraph7.getGridLabelRenderer().setVerticalAxisTitle("Temperature (Fahrenheit)");
            lineGraph7.getGridLabelRenderer().setLabelsSpace(10);
            lineGraph7.getGridLabelRenderer().setHorizontalLabelsAngle(135);

            Date date = new Date();
            final SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
            for (int i = 0; i < temperature.size(); i++) {
                String[] rows = temperature.get(i);
                Log.d(TAG, "Output: " + rows[0] + " " + Double.parseDouble(rows[7]));
                date = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy").parse(rows[0]);
                temperature_dataPoints[i] = new DataPoint(date, Double.parseDouble(rows[7]));
            }
            LineGraphSeries<DataPoint> series7 = new LineGraphSeries<DataPoint>(temperature_dataPoints);
            series7.setTitle("Water Solar Collector Temperature");
            series7.setDrawDataPoints(true);
            series7.setDataPointsRadius(8);
            series7.setColor(Color.MAGENTA);
            series7.setDrawBackground(true);
            series7.setThickness(8);
            series7.setOnDataPointTapListener(new OnDataPointTapListener() {
                @Override
                public void onTap(Series series7, DataPointInterface temperature_dataPoints) {
                    Toast.makeText(getApplicationContext(), "Water Solar Collector Temperature: ["+ sdf.format(new Date((long) temperature_dataPoints.getX())) + "/" + temperature_dataPoints.getY() +" Fahrenheit]", Toast.LENGTH_LONG).show();
                }
            });
            lineGraph7.addSeries(series7);

            //Label
            lineGraph7.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter(){
                @Override
                public String formatLabel(double value, boolean isValueX) {
                    if(isValueX){
                        return sdf.format(new Date((long) value));
                    }else {
                        return super.formatLabel(value, isValueX);
                    }
                }
            });

            //Legend
            lineGraph7.getLegendRenderer().resetStyles();
            lineGraph7.getLegendRenderer().setMargin(10);
            lineGraph7.getLegendRenderer().setTextColor(Color.WHITE);
            lineGraph7.getLegendRenderer().setVisible(true);
            lineGraph7.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        }
    }
}