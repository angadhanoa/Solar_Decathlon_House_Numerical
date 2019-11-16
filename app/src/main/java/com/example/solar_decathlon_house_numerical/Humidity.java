package com.example.solar_decathlon_house_numerical;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

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
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;

public class Humidity extends AppCompatActivity {
    private static final String TAG = Humidity.class.getSimpleName();
    private GraphView lineGraph1;
    String humidityFileName = "humiditysignature.csv";
    String user = "rpihubteam6";  //Samba User name
    String pass ="raspberrypi";   //Samba Password
    String sharedFolder="share";  //Samba Shared folder
    String domain = "rpihubteam6";    //Samba domain name
    String ipAddressWireless = "192.168.1.10"; //IP address for rpihubteam6 when it is wirelessly connected with the router
    String ipAddressEthernet = "192.168.1.11"; //IP address for rpihubteam6 when it is wired with the router

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_humidity);

        lineGraph1 = findViewById(R.id.graph_for_humidity);

        Button humidityButton = (Button)findViewById(R.id.humidity);
        humidityButton.setOnClickListener(new View.OnClickListener() {
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
                                List<String[]> humidity;

                                String url1 = "smb://" + ipAddressWireless + "/" + sharedFolder + "/" + humidityFileName;
                                NtlmPasswordAuthentication auth1 = new NtlmPasswordAuthentication(domain, user, pass);
                                InputStream smbHumidityFile = new SmbFile(url1, auth1).getInputStream();
                                CSVReader csv_temperature = new CSVReader(smbHumidityFile, "humidity");//CSVReader(inputStream2);
                                humidity = csv_temperature.read();

                                //Creating line graphs
                                createLineGraph(humidity, "humidity");

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

    private void createLineGraph(List<String[]> humidity, String graphToPlot) throws ParseException {
        DataPoint[] humidity_dataPoints = new DataPoint[humidity.size()];

        //humidity
        if(graphToPlot.equals("humidity")) {
            lineGraph1.removeAllSeries();
            lineGraph1.getViewport().setScalable(true);
            lineGraph1.getViewport().setScalableY(true);
            lineGraph1.getGridLabelRenderer().setHorizontalAxisTitle("Time (Minutes)");
            lineGraph1.getGridLabelRenderer().setVerticalAxisTitle("Humidity ( % )");
            lineGraph1.getGridLabelRenderer().setLabelsSpace(10);
            lineGraph1.getGridLabelRenderer().setHorizontalLabelsAngle(135);

            Date date;
            final SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
            for (int i = 0; i < humidity.size(); i++) {
                String[] rows = humidity.get(i);
                Log.d(TAG, "Humidity: " + rows[0] + " " + Double.parseDouble(rows[1]));
                date = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy").parse(rows[0]);
                humidity_dataPoints[i] = new DataPoint(date, Double.parseDouble(rows[1]));
            }
            LineGraphSeries<DataPoint> series1 = new LineGraphSeries<DataPoint>(humidity_dataPoints);
            series1.setTitle("Humidity");
            series1.setDrawDataPoints(true);
            series1.setDataPointsRadius(8);
            series1.setColor(Color.BLACK);
            series1.setDrawBackground(true);
            series1.setThickness(8);
            series1.setOnDataPointTapListener(new OnDataPointTapListener() {
                @Override
                public void onTap(Series series1, DataPointInterface humidity_dataPoints) {
                    Toast.makeText(getApplicationContext(), "Humidity: ["+ sdf.format(new Date((long) humidity_dataPoints.getX())) + "/" + humidity_dataPoints.getY() +" grams per meter cube]", Toast.LENGTH_LONG).show();
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
    }
}