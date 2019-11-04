package com.example.solar_decathlon_house_numerical;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.io.InputStream;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.series.Series;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import android.util.Log;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.jjoe64.graphview.series.OnDataPointTapListener;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;

public class Power extends AppCompatActivity {
    private static final String TAG = Power.class.getSimpleName();
    private GraphView lineGraphForAll, lineGraph1, lineGraph2, lineGraph3, lineGraph4, lineGraph5, lineGraph6;
    String user = "rpihubteam6";  //Samba User name
    String pass ="raspberrypi";   //Samba Password
    String sharedFolder="share";  //Samba Shared folder
    String domain = "rpihubteam6";    //Samba domain name
    String fileName = "power.csv";
    String ipAddressWireless = "192.168.1.10"; //IP address for rpihubteam6 when it is wirelessly connected with the router
    String ipAddressEthernet = "192.168.1.11"; //IP address for rpihubteam6 when it is wired with the router

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_power);

        lineGraphForAll = findViewById(R.id.graph_for_all);
        lineGraph1 = findViewById(R.id.graph_for_lighting);
        lineGraph2 = findViewById(R.id.graph_for_air_conditioner);
        lineGraph3 = findViewById(R.id.graph_for_water_heater);
        lineGraph4 = findViewById(R.id.graph_for_refrigerator);
        lineGraph5 = findViewById(R.id.graph_for_kitchen_outlet);
        lineGraph6 = findViewById(R.id.graph_for_radiant_floor_pump);

        //For Graphing all the sensors at once.
        Button graphForAllButton = (Button)findViewById(R.id.one_for_all);
        graphForAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
            try {
                //Creating a new thread for the file transfer, this takes the load off the main thread.
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                    try {
                        //To get Samba Shared file from the Raspberry Pi
                        List<String[]> consumption;

                        String url2 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/" + fileName;
                        NtlmPasswordAuthentication auth2 = new NtlmPasswordAuthentication(domain, user, pass);
                        InputStream smbPowerConsumeFile = new SmbFile(url2, auth2).getInputStream();

                        //To create a List Array for Power Consumed
                        CSVReader csv_consumption = new CSVReader(smbPowerConsumeFile, "power");//CSVReader(inputStream2);
                        consumption = csv_consumption.read();

                        //Creating line graphs
                        createLineGraph(consumption, "GraphForAll");

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    }
                });
                thread.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
            }
        });

        //For Graphing Sensor 1
        Button graphForSensor1Button = (Button)findViewById(R.id.lighting);
        graphForSensor1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
                try {
                    //Creating a new thread for the file transfer, this takes the load off the main thread.
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                //To get Samba Shared file from the Raspberry Pi
                                List<String[]> consumption;

                                String url2 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/" + fileName;
                                NtlmPasswordAuthentication auth2 = new NtlmPasswordAuthentication(domain, user, pass);
                                InputStream smbPowerConsumeFile = new SmbFile(url2, auth2).getInputStream();

                                //To create a List Array for Power Consumed
                                CSVReader csv_consumption = new CSVReader(smbPowerConsumeFile, "power");//CSVReader(inputStream2);
                                consumption = csv_consumption.read();

                                //Creating line graphs
                                createLineGraph(consumption, "GraphForLighting");

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    thread.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //For Graphing Sensor 2
        Button graphForSensor2Button = (Button)findViewById(R.id.air_conditioner);
        graphForSensor2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
                try {
                    //Creating a new thread for the file transfer, this takes the load off the main thread.
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                //To get Samba Shared file from the Raspberry Pi
                                List<String[]> consumption;

                                String url2 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/" + fileName;
                                NtlmPasswordAuthentication auth2 = new NtlmPasswordAuthentication(domain, user, pass);
                                InputStream smbPowerConsumeFile = new SmbFile(url2, auth2).getInputStream();

                                //To create a List Array for Power Consumed
                                CSVReader csv_consumption = new CSVReader(smbPowerConsumeFile, "power");//CSVReader(inputStream2);
                                consumption = csv_consumption.read();

                                //Creating line graphs
                                createLineGraph(consumption, "GraphForAirConditioner");

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    thread.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //For Graphing Sensor 3
        Button graphForSensor3Button = (Button)findViewById(R.id.water_heater);
        graphForSensor3Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
                try {
                    //Creating a new thread for the file transfer, this takes the load off the main thread.
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                //To get Samba Shared file from the Raspberry Pi
                                List<String[]> consumption;

                                String url2 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/" + fileName;
                                NtlmPasswordAuthentication auth2 = new NtlmPasswordAuthentication(domain, user, pass);
                                InputStream smbPowerConsumeFile = new SmbFile(url2, auth2).getInputStream();
                                //To create a List Array for Power Consumed
                                CSVReader csv_consumption = new CSVReader(smbPowerConsumeFile, "power");//CSVReader(inputStream2);
                                consumption = csv_consumption.read();

                                //Creating line graphs
                                createLineGraph(consumption, "GraphForWaterHeater");

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    thread.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //For Graphing Sensor 4
        Button graphForSensor4Button = (Button)findViewById(R.id.refrigerator);
        graphForSensor4Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
                try {
                    //Creating a new thread for the file transfer, this takes the load off the main thread.
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                //To get Samba Shared file from the Raspberry Pi
                                List<String[]> consumption;

                                String url2 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/" + fileName;
                                NtlmPasswordAuthentication auth2 = new NtlmPasswordAuthentication(domain, user, pass);
                                InputStream smbPowerConsumeFile = new SmbFile(url2, auth2).getInputStream();

                                //To create a List Array for Power Consumed
                                CSVReader csv_consumption = new CSVReader(smbPowerConsumeFile, "power");//CSVReader(inputStream2);
                                consumption = csv_consumption.read();

                                //Creating line graphs
                                createLineGraph(consumption, "GraphForRefrigerator");

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    thread.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //For Graphing Sensor 5
        Button graphForSensor5Button = (Button)findViewById(R.id.kitchen_outlet);
        graphForSensor5Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
                try {
                    //Creating a new thread for the file transfer, this takes the load off the main thread.
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                //To get Samba Shared file from the Raspberry Pi
                                List<String[]> consumption;

                                String url2 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/" + fileName;
                                NtlmPasswordAuthentication auth2 = new NtlmPasswordAuthentication(domain, user, pass);
                                InputStream smbPowerConsumeFile = new SmbFile(url2, auth2).getInputStream();
                                //To create a List Array for Power Consumed
                                CSVReader csv_consumption = new CSVReader(smbPowerConsumeFile, "power");//CSVReader(inputStream2);
                                consumption = csv_consumption.read();

                                //Creating line graphs
                                createLineGraph(consumption, "GraphForKitchenOutlet");

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    thread.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //For Graphing Sensor 6
        Button graphForSensor6Button = (Button)findViewById(R.id.radiant_floor_pump);
        graphForSensor6Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            jcifs.Config.registerSmbURLHandler(); //jcifs is used for handling smb file transfer.
            try {
                //Creating a new thread for the file transfer, this takes the load off the main thread.
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                    try {
                        //To get Samba Shared file from the Raspberry Pi
                        List<String[]> consumption;

                        String url2 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/" + fileName;
                        NtlmPasswordAuthentication auth2 = new NtlmPasswordAuthentication(domain, user, pass);
                        InputStream smbPowerConsumeFile = new SmbFile(url2, auth2).getInputStream();

                        //To create a List Array for Power Consumed
                        CSVReader csv_consumption = new CSVReader(smbPowerConsumeFile, "power");//CSVReader(inputStream2);
                        consumption = csv_consumption.read();

                        //Creating line graphs
                        createLineGraph(consumption, "GraphForRadiantFloorPump");

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    }
                });

                thread.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
            }
        });

    }

    private void createLineGraph(List<String[]> consumption, String graphToPlot) throws ParseException {
        if(graphToPlot.equals("GraphForAll")){
            lineGraphForAll.removeAllSeries();
            lineGraphForAll.getViewport().setScalable(true);
            lineGraphForAll.getViewport().setScalableY(true);
            lineGraphForAll.getGridLabelRenderer().setHorizontalAxisTitle("Time Stamp");
            lineGraphForAll.getGridLabelRenderer().setVerticalAxisTitle("Power Consumption [Watts]");
            lineGraphForAll.getGridLabelRenderer().setLabelsSpace(10);
            lineGraphForAll.getGridLabelRenderer().setHorizontalLabelsAngle(135);

            //Legend
            lineGraphForAll.getLegendRenderer().resetStyles();
            lineGraphForAll.getLegendRenderer().setMargin(10);
            lineGraphForAll.getLegendRenderer().setTextColor(Color.BLACK);
            lineGraphForAll.getLegendRenderer().setBackgroundColor(Color.WHITE);
            lineGraphForAll.getLegendRenderer().setVisible(true);
            //lineGraphForAll.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
            lineGraphForAll.getLegendRenderer().setFixedPosition(20,20);
        }

        DataPoint[] consumption_dataPoints = new DataPoint[consumption.size()];

        //Power Consumption Sensor 1
        if(graphToPlot.equals("GraphForAll") || graphToPlot.equals("GraphForLighting")){
            Date date = new Date();
            final SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
            for (int i = 0; i < consumption.size(); i++){
                String [] rows = consumption.get(i);
                Log.d(TAG, "Output: " + rows[0] + " " + Double.parseDouble(rows[2]));
                date = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy").parse(rows[0]);
                consumption_dataPoints[i] = new DataPoint(date, Double.parseDouble(rows[2]));
            }
            LineGraphSeries<DataPoint> series1 = new LineGraphSeries<DataPoint>(consumption_dataPoints);
            series1.setTitle("Lighting");
            series1.setDrawDataPoints(true);
            series1.setDataPointsRadius(8);
            series1.setColor(Color.BLACK);
            series1.setThickness(8);
            series1.setOnDataPointTapListener(new OnDataPointTapListener() {
                @Override
                public void onTap(Series series1, DataPointInterface consumption_dataPoints) {
                    Toast.makeText(getApplicationContext(), "Lighting: ["+ sdf.format(new Date((long) consumption_dataPoints.getX())) + "/" + consumption_dataPoints.getY() +" Watts]", Toast.LENGTH_LONG).show();
                }
            });

            if(graphToPlot.equals("GraphForAll")) {
                lineGraphForAll.addSeries(series1);
                //Label
                lineGraphForAll.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter(){
                    @Override
                    public String formatLabel(double value, boolean isValueX) {
                        if(isValueX){
                            return sdf.format(new Date((long) value));
                        }else {
                            return super.formatLabel(value, isValueX);
                        }
                    }
                });
            }
            if(graphToPlot.equals("GraphForLighting")) {
                series1.setDrawBackground(true);

                lineGraph1.removeAllSeries();
                lineGraph1.getViewport().setScalable(true);
                lineGraph1.getViewport().setScalableY(true);
                lineGraph1.getGridLabelRenderer().setHorizontalAxisTitle("Time Stamp");
                lineGraph1.getGridLabelRenderer().setVerticalAxisTitle("Power Consumption [Watts]");
                lineGraph1.getGridLabelRenderer().setLabelsSpace(10);
                lineGraph1.getGridLabelRenderer().setHorizontalLabelsAngle(135);

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
        }

        //Power Consumption Sensor 2
        if(graphToPlot.equals("GraphForAll") || graphToPlot.equals("GraphForAirConditioner")){
            Date date = new Date();
            final SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
            for (int i = 0; i < consumption.size(); i++){
                String [] rows = consumption.get(i);
                Log.d(TAG, "Output: " + rows[0] + " " + Double.parseDouble(rows[3]));
                date = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy").parse(rows[0]);
                consumption_dataPoints[i] = new DataPoint(date, Double.parseDouble(rows[3]));
            }
            LineGraphSeries<DataPoint> series2 = new LineGraphSeries<DataPoint>(consumption_dataPoints);
            series2.setTitle("Air Conditioner");
            series2.setDrawDataPoints(true);
            series2.setDataPointsRadius(8);
            series2.setColor(Color.RED);
            series2.setThickness(8);
            series2.setOnDataPointTapListener(new OnDataPointTapListener() {
                @Override
                public void onTap(Series series2, DataPointInterface consumption_dataPoints) {
                    Toast.makeText(getApplicationContext(), "Air Conditioner: ["+ sdf.format(new Date((long) consumption_dataPoints.getX())) + "/" + consumption_dataPoints.getY() +" Watts]", Toast.LENGTH_LONG).show();
                }
            });

            if(graphToPlot.equals("GraphForAll")) {
                lineGraphForAll.addSeries(series2);
                //Label
                lineGraphForAll.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter(){
                    @Override
                    public String formatLabel(double value, boolean isValueX) {
                        if(isValueX){
                            return sdf.format(new Date((long) value));
                        }else {
                            return super.formatLabel(value, isValueX);
                        }
                    }
                });
            }
            if(graphToPlot.equals("GraphForAirConditioner")) {
                series2.setDrawBackground(true);

                lineGraph2.removeAllSeries();
                lineGraph2.getViewport().setScalable(true);
                lineGraph2.getViewport().setScalableY(true);
                lineGraph2.getGridLabelRenderer().setHorizontalAxisTitle("Time Stamp");
                lineGraph2.getGridLabelRenderer().setVerticalAxisTitle("Power Consumption [Watts]");
                lineGraph2.getGridLabelRenderer().setLabelsSpace(10);
                lineGraph2.getGridLabelRenderer().setHorizontalLabelsAngle(135);

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
        }

        //Power Consumption Sensor 3
        if(graphToPlot.equals("GraphForAll") || graphToPlot.equals("GraphForWaterHeater")){
            Date date = new Date();
            final SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
            for (int i = 0; i < consumption.size(); i++){
                String [] rows = consumption.get(i);
                Log.d(TAG, "Output: " + rows[0] + " " + Double.parseDouble(rows[4]));
                date = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy").parse(rows[0]);
                consumption_dataPoints[i] = new DataPoint(date, Double.parseDouble(rows[4]));
            }
            LineGraphSeries<DataPoint> series3 = new LineGraphSeries<DataPoint>(consumption_dataPoints);
            series3.setTitle("Water Heater");
            series3.setDrawDataPoints(true);
            series3.setDataPointsRadius(8);
            series3.setColor(Color.BLUE);
            series3.setThickness(8);
            series3.setOnDataPointTapListener(new OnDataPointTapListener() {
                @Override
                public void onTap(Series series3, DataPointInterface consumption_dataPoints) {
                    Toast.makeText(getApplicationContext(), "Water Heater: ["+ sdf.format(new Date((long) consumption_dataPoints.getX())) + "/" + consumption_dataPoints.getY() +" Watts]", Toast.LENGTH_LONG).show();
                }
            });

            if(graphToPlot.equals("GraphForAll")) {
                lineGraphForAll.addSeries(series3);
                //Label
                lineGraphForAll.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter(){
                    @Override
                    public String formatLabel(double value, boolean isValueX) {
                        if(isValueX){
                            return sdf.format(new Date((long) value));
                        }else {
                            return super.formatLabel(value, isValueX);
                        }
                    }
                });
            }
            if(graphToPlot.equals("GraphForWaterHeater")) {
                series3.setDrawBackground(true);

                lineGraph3.removeAllSeries();
                lineGraph3.getViewport().setScalable(true);
                lineGraph3.getViewport().setScalableY(true);
                lineGraph3.getGridLabelRenderer().setHorizontalAxisTitle("Time Stamp");
                lineGraph3.getGridLabelRenderer().setVerticalAxisTitle("Power Consumption [Watts]");
                lineGraph3.getGridLabelRenderer().setLabelsSpace(10);
                lineGraph3.getGridLabelRenderer().setHorizontalLabelsAngle(135);

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
        }

        //Power Consumption Sensor 4
        if(graphToPlot.equals("GraphForAll") || graphToPlot.equals("GraphForRefrigerator")){
            Date date = new Date();
            final SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
            for (int i = 0; i < consumption.size(); i++){
                String [] rows = consumption.get(i);
                Log.d(TAG, "Output: " + rows[0] + " " + Double.parseDouble(rows[5]));
                date = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy").parse(rows[0]);
                consumption_dataPoints[i] = new DataPoint(date, Double.parseDouble(rows[5]));
            }
            LineGraphSeries<DataPoint> series4 = new LineGraphSeries<DataPoint>(consumption_dataPoints);
            series4.setTitle("Refrigerator");
            series4.setDrawDataPoints(true);
            series4.setDataPointsRadius(8);
            series4.setColor(Color.GRAY);
            series4.setThickness(8);
            series4.setOnDataPointTapListener(new OnDataPointTapListener() {
                @Override
                public void onTap(Series series4, DataPointInterface consumption_dataPoints) {
                    Toast.makeText(getApplicationContext(), "Refrigerator: ["+ sdf.format(new Date((long) consumption_dataPoints.getX())) + "/" + consumption_dataPoints.getY() +" Watts]", Toast.LENGTH_LONG).show();
                }
            });

            if(graphToPlot.equals("GraphForAll")) {
               lineGraphForAll.addSeries(series4);
                //Label
                lineGraphForAll.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter(){
                    @Override
                    public String formatLabel(double value, boolean isValueX) {
                        if(isValueX){
                            return sdf.format(new Date((long) value));
                        }else {
                            return super.formatLabel(value, isValueX);
                        }
                    }
                });
            }
            if(graphToPlot.equals("GraphForRefrigerator")) {
                series4.setDrawBackground(true);

                lineGraph4.removeAllSeries();
                lineGraph4.getViewport().setScalable(true);
                lineGraph4.getViewport().setScalableY(true);
                lineGraph4.getGridLabelRenderer().setHorizontalAxisTitle("Time Stamp");
                lineGraph4.getGridLabelRenderer().setVerticalAxisTitle("Power Consumption [Watts]");
                lineGraph4.getGridLabelRenderer().setLabelsSpace(10);
                lineGraph4.getGridLabelRenderer().setHorizontalLabelsAngle(135);

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
        }

        //Power Consumption Sensor 5
        if(graphToPlot.equals("GraphForAll") || graphToPlot.equals("GraphForKitchenOutlet")){
            Date date = new Date();
            final SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
            for (int i = 0; i < consumption.size(); i++){
                String [] rows = consumption.get(i);
                Log.d(TAG, "Output: " + rows[0] + " " + Double.parseDouble(rows[6]));
                date = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy").parse(rows[0]);
                consumption_dataPoints[i] = new DataPoint(date, Double.parseDouble(rows[6]));
            }
            LineGraphSeries<DataPoint> series5 = new LineGraphSeries<DataPoint>(consumption_dataPoints);
            series5.setTitle("Kitchen Outlet");
            series5.setDrawDataPoints(true);
            series5.setDataPointsRadius(8);
            series5.setColor(Color.GREEN);
            series5.setThickness(8);
            series5.setOnDataPointTapListener(new OnDataPointTapListener() {
                @Override
                public void onTap(Series series5, DataPointInterface consumption_dataPoints) {
                    Toast.makeText(getApplicationContext(), "Kitchen Outlet: ["+ sdf.format(new Date((long) consumption_dataPoints.getX())) + "/" + consumption_dataPoints.getY() +" Watts]", Toast.LENGTH_LONG).show();
                }
            });

            if(graphToPlot.equals("GraphForAll")) {
                lineGraphForAll.addSeries(series5);
                //Label
                lineGraphForAll.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter(){
                    @Override
                    public String formatLabel(double value, boolean isValueX) {
                        if(isValueX){
                            return sdf.format(new Date((long) value));
                        }else {
                            return super.formatLabel(value, isValueX);
                        }
                    }
                });
            }
            if(graphToPlot.equals("GraphForKitchenOutlet")) {
                series5.setDrawBackground(true);

                lineGraph5.removeAllSeries();
                lineGraph5.getViewport().setScalable(true);
                lineGraph5.getViewport().setScalableY(true);
                lineGraph5.getGridLabelRenderer().setHorizontalAxisTitle("Time Stamp");
                lineGraph5.getGridLabelRenderer().setVerticalAxisTitle("Power Consumption [Watts]");
                lineGraph5.getGridLabelRenderer().setLabelsSpace(10);
                lineGraph5.getGridLabelRenderer().setHorizontalLabelsAngle(135);

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
        }

        //Power Consumption Sensor 6
        if(graphToPlot.equals("GraphForAll") || graphToPlot.equals("GraphForRadiantFloorPump")){
            Date date = new Date();
            final SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
            for (int i = 0; i < consumption.size(); i++){
                String [] rows = consumption.get(i);
                Log.d(TAG, "Output: " + rows[0] + " " + Double.parseDouble(rows[7]));
                date = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy").parse(rows[0]);
                consumption_dataPoints[i] = new DataPoint(date, Double.parseDouble(rows[7]));
            }
            LineGraphSeries<DataPoint> series6 = new LineGraphSeries<DataPoint>(consumption_dataPoints);
            series6.setTitle("Radiant Floor Pump");
            series6.setDrawDataPoints(true);
            series6.setDataPointsRadius(8);
            series6.setColor(Color.YELLOW);
            series6.setThickness(8);
            series6.setOnDataPointTapListener(new OnDataPointTapListener() {
                @Override
                public void onTap(Series series6, DataPointInterface consumption_dataPoints) {
                    Toast.makeText(getApplicationContext(), "Radiant Floor Pump: ["+ sdf.format(new Date((long) consumption_dataPoints.getX())) + "/" + consumption_dataPoints.getY() +" Watts]", Toast.LENGTH_LONG).show();
                }
            });

            if(graphToPlot.equals("GraphForAll")) {
                lineGraphForAll.addSeries(series6);
                //Label
                lineGraphForAll.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter(){
                    @Override
                    public String formatLabel(double value, boolean isValueX) {
                        if(isValueX){
                            return sdf.format(new Date((long) value));
                        }else {
                            return super.formatLabel(value, isValueX);
                        }
                    }
                });
            }
            if(graphToPlot.equals("GraphForRadiantFloorPump")) {
                series6.setDrawBackground(true);

                lineGraph6.removeAllSeries();
                lineGraph6.getViewport().setScalable(true);
                lineGraph6.getViewport().setScalableY(true);
                lineGraph6.getGridLabelRenderer().setHorizontalAxisTitle("Time Stamp");
                lineGraph6.getGridLabelRenderer().setVerticalAxisTitle("Power Consumption [Watts]");
                lineGraph6.getGridLabelRenderer().setLabelsSpace(10);
                lineGraph6.getGridLabelRenderer().setHorizontalLabelsAngle(135);

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
        }
    }
}