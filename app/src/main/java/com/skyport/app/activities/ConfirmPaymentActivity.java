package com.skyport.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.skyport.app.R;

public class ConfirmPaymentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_payment);

        String method = getIntent().getStringExtra("METHOD");
        String price  = getIntent().getStringExtra("PRICE");
        if (method == null) method = "Credit Card";
        if (price  == null) price  = "₹ 6,000.00";

        ((ImageButton) findViewById(R.id.btnBack)).setOnClickListener(v -> finish());

        ((TextView) findViewById(R.id.tvConfirmMethod)).setText(method);
        ((TextView) findViewById(R.id.tvConfirmTotal)) .setText(price);
        ((TextView) findViewById(R.id.tvConfirmFare))  .setText(price);

        final String fMethod = method;
        final String fPrice  = price;

        findViewById(R.id.btnComplete).setOnClickListener(v -> {
            // 80% success, 20% fail (simple simulation)
            boolean success = (System.currentTimeMillis() % 10) < 8;
            Intent intent;
            if (success) {
                intent = new Intent(this, BookingConfirmedActivity.class);
                intent.putExtra("METHOD", fMethod);
                intent.putExtra("PRICE",  fPrice);
            } else {
                intent = new Intent(this, BookingFailedActivity.class);
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
    }
}
