package com.example.solar_decathlon_house_numerical;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;

public class WaterFlow extends AppCompatActivity {

    private static final String TAG = WaterFlow.class.getSimpleName();
    private GraphView lineGraph1, lineGraph2;

    TextView txtView;
    Button loadTextButton, loadGraph1Button, loadGraph2Button;
    String user = "rpihubteam6";  //Samba User name
    String pass = "raspberrypi";   //Samba Password
    String sharedFolder = "share";  //Samba Shared folder
    String domain = "rpihubteam6";    //Samba domain name
    String fileName1 = "water1app.csv";
    String fileName2 = "water2app.csv";
    String ipAddressWireless = "192.168.1.10"; //IP address for rpihubteam6 when it is wirelessly connected with the router
    //String ipAddressEthernet = "192.168.1.11";

    double waterSum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_water_flow);

        loadTextButton = findViewById(R.id.load_file_from_raw);
        txtView = findViewById(R.id.textView10);
        txtView.setVisibility(TextView.INVISIBLE);
        loadTextButton.setOnClickListener(new View.OnClickListener(){
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
                                String url1 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/" + fileName1;
                                NtlmPasswordAuthentication auth1 = new NtlmPasswordAuthentication(domain, user, pass);
                                InputStream smbWater1ConsumeFile = new SmbFile(url1, auth1).getInputStream();
                                //To create a List Array for Power Consumed
                                CSVReader csv_water1_consumption = new CSVReader(smbWater1ConsumeFile, "water");//CSVReader(inputStream2);
                                List<String[]> water1Reading = csv_water1_consumption.read();

                                //To get Samba Shared file from the Raspberry Pi
                                String url2 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/" + fileName2;
                                NtlmPasswordAuthentication auth2 = new NtlmPasswordAuthentication(domain, user, pass);
                                InputStream smbWater2ConsumeFile = new SmbFile(url1, auth2).getInputStream();
                                //To create a List Array for Power Consumed
                                CSVReader csv_water2_consumption = new CSVReader(smbWater2ConsumeFile, "water");//CSVReader(inputStream2);
                                List<String[]> water2Reading = csv_water2_consumption.read();

                                double tempWaterSum = 0.0;
                                int size;

                                if(water1Reading.size() < water2Reading.size()) {
                                    size = water1Reading.size();
                                } else {
                                    size = water2Reading.size();
                                }

                                for (int i = 0; i < size; i++) {
                                    String[] rows1 = water1Reading.get(i);
                                    String[] rows2 = water2Reading.get(i);
                                    tempWaterSum += Double.parseDouble(rows1[1]) + Double.parseDouble(rows2[1]);
                                }
                                waterSum = Math.round(tempWaterSum * Math.pow(10, 2)) / Math.pow(10, 2); //To round off to two decimal places.
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    thread.start();

                    String units = " Litres/Min";
                    String value =  Double.toString(waterSum);
                    txtView.setVisibility(TextView.VISIBLE);
                    txtView.setText(value +  units);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        lineGraph1 = findViewById(R.id.graph_for_water1_usage);
        loadGraph1Button = findViewById(R.id.buttonForGraph1);
        loadGraph1Button.setOnClickListener(new View.OnClickListener(){
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
                                String url1 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/" + fileName1;
                                NtlmPasswordAuthentication auth1 = new NtlmPasswordAuthentication(domain, user, pass);
                                InputStream smbWater1ConsumeFile = new SmbFile(url1, auth1).getInputStream();
                                //To create a List Array for Power Consumed
                                CSVReader csv_water1_consumption = new CSVReader(smbWater1ConsumeFile, "water");//CSVReader(inputStream2);
                                List<String[]> water1Reading = csv_water1_consumption.read();

                                //Creating line graphs
                                createLineGraph(water1Reading, "sensor1");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    thread.start();

                    String units = " Litres/Min";
                    String value =  Double.toString(waterSum);
                    txtView.setVisibility(TextView.VISIBLE);
                    txtView.setText(value +  units);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        lineGraph2 = findViewById(R.id.graph_for_water2_usage);
        loadGraph2Button = findViewById(R.id.buttonForGraph2);
        loadGraph2Button.setOnClickListener(new View.OnClickListener(){
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
                                String url2 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/" + fileName2;
                                NtlmPasswordAuthentication auth2 = new NtlmPasswordAuthentication(domain, user, pass);
                                InputStream smbWater2ConsumeFile = new SmbFile(url2, auth2).getInputStream();
                                //To create a List Array for Power Consumed
                                CSVReader csv_water2_consumption = new CSVReader(smbWater2ConsumeFile, "water");//CSVReader(inputStream2);
                                List<String[]> water2Reading = csv_water2_consumption.read();

                                //Creating line graphs
                                createLineGraph(water2Reading, "sensor2");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    thread.start();

                    String units = " Litres/Min";
                    String value =  Double.toString(waterSum);
                    txtView.setVisibility(TextView.VISIBLE);
                    txtView.setText(value +  units);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void createLineGraph(List<String[]> waterReading, String sensor) throws ParseException {

        DataPoint[] water_dataPoints = new DataPoint[waterReading.size()];
        if(sensor.equals("sensor1")){
            lineGraph1.removeAllSeries();
            lineGraph1.getViewport().setScalable(true);
            lineGraph1.getViewport().setScalableY(true);
            lineGraph1.getGridLabelRenderer().setHorizontalAxisTitle("Time Stamp");
            lineGraph1.getGridLabelRenderer().setVerticalAxisTitle("Power Consumption [Watts]");
            lineGraph1.getGridLabelRenderer().setLabelsSpace(10);
            lineGraph1.getGridLabelRenderer().setHorizontalLabelsAngle(135);

            Date date = new Date();
            final SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");

            //Sensor 1
            for (int i = 0; i < waterReading.size(); i++){
                String [] rows = waterReading.get(i);
                Log.d(TAG, "Output: " + rows[0] + " " + Double.parseDouble(rows[1]));
                date = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy").parse(rows[0]);
                water_dataPoints[i] = new DataPoint(date, Double.parseDouble(rows[1]));
            }
            LineGraphSeries<DataPoint> series1 = new LineGraphSeries<DataPoint>(water_dataPoints);
            series1.setTitle("Water Sensor 1");
            series1.setDrawDataPoints(true);
            series1.setDataPointsRadius(8);
            series1.setColor(Color.BLACK);
            series1.setThickness(8);
            series1.setDrawBackground(true);
            series1.setOnDataPointTapListener(new OnDataPointTapListener() {
                @Override
                public void onTap(Series series1, DataPointInterface water_dataPoints) {
                    Toast.makeText(getApplicationContext(), "Water Sensor 1: ["+ sdf.format(new Date((long) water_dataPoints.getX())) + "/" + water_dataPoints.getY() +" Gallons]", Toast.LENGTH_LONG).show();
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

        if(sensor.equals("sensor2")){
            lineGraph2.removeAllSeries();
            lineGraph2.getViewport().setScalable(true);
            lineGraph2.getViewport().setScalableY(true);
            lineGraph2.getGridLabelRenderer().setHorizontalAxisTitle("Time Stamp");
            lineGraph2.getGridLabelRenderer().setVerticalAxisTitle("Power Consumption [Watts]");
            lineGraph2.getGridLabelRenderer().setLabelsSpace(10);
            lineGraph2.getGridLabelRenderer().setHorizontalLabelsAngle(135);

            Date date = new Date();
            final SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");

            //Sensor 2
            for (int i = 0; i < waterReading.size(); i++){
                String [] rows = waterReading.get(i);
                Log.d(TAG, "Output: " + rows[0] + " " + Double.parseDouble(rows[1]));
                date = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy").parse(rows[0]);
                water_dataPoints[i] = new DataPoint(date, Double.parseDouble(rows[1]));
            }
            LineGraphSeries<DataPoint> series2 = new LineGraphSeries<DataPoint>(water_dataPoints);
            series2.setTitle("Water Sensor 2");
            series2.setDrawDataPoints(true);
            series2.setDataPointsRadius(8);
            series2.setColor(Color.RED);
            series2.setThickness(8);
            series2.setDrawBackground(true);
            series2.setOnDataPointTapListener(new OnDataPointTapListener() {
                @Override
                public void onTap(Series series2, DataPointInterface water_dataPoints) {
                    Toast.makeText(getApplicationContext(), "Water Sensor 2: ["+ sdf.format(new Date((long) water_dataPoints.getX())) + "/" + water_dataPoints.getY() +" Gallons]", Toast.LENGTH_LONG).show();
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
    }
}
