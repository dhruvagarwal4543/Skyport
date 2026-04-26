package com.skyport.app.activities;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.skyport.app.R;

public class QRActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);

        ((ImageButton) findViewById(R.id.btnBack)).setOnClickListener(v -> finish());
    }
}
