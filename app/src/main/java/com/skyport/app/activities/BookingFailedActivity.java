package com.skyport.app.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.skyport.app.R;

public class BookingFailedActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_failed);

        findViewById(R.id.btnRetry).setOnClickListener(v -> {
            // Go back to payment method selection
            Intent intent = new Intent(this, ChoosePaymentActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
    }
}
