package com.example.solar_decathlon_house_numerical;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class FeatureSelection extends AppCompatActivity {
    private ImageButton button1, button2, button3, button4, button5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feature_selection);

        //To create the activity for Power.
        this.button1 = findViewById(R.id.button1);
        button1.setOnClickListener(new View.OnClickListener() {
            Intent intent = new Intent(FeatureSelection.this, Power.class);
            @Override
            public void onClick(View button) { startActivity(intent); }
        });

        //To create the activity for Temperature Sensors.
        this.button2 = findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            Intent intent = new Intent(FeatureSelection.this, Temperature.class);
            @Override
            public void onClick(View button) { startActivity(intent); }
        });

        //To create the activity for Water Consumption.
        this.button3 = findViewById(R.id.button3);
        button3.setOnClickListener(new View.OnClickListener() {
            Intent intent = new Intent(FeatureSelection.this, WaterFlow.class);
            @Override
            public void onClick(View button) { startActivity(intent); }
        });

        //To create the activity for Humidity Sensors.
        this.button4 = findViewById(R.id.button4);
        button4.setOnClickListener(new View.OnClickListener() {
            Intent intent = new Intent(FeatureSelection.this, Humidity.class);
            @Override
            public void onClick(View button) { startActivity(intent); }
        });

        //To create the activity for Speech Recognition.
        this.button5 = findViewById(R.id.button5);
        button5.setOnClickListener(new View.OnClickListener() {
            Intent intent = new Intent(FeatureSelection.this, SpeechToTextTextToSpeech.class);
            @Override
            public void onClick(View button) { startActivity(intent); }
        });
    }

}
