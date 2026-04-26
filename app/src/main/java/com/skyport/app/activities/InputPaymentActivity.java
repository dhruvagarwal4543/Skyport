package com.skyport.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.skyport.app.R;

public class InputPaymentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_payment);

        String method = getIntent().getStringExtra("METHOD");
        String price  = getIntent().getStringExtra("PRICE");
        if (method == null) method = "Credit Card";
        if (price  == null) price  = "₹ 6,000.00";

        ((ImageButton) findViewById(R.id.btnBack)).setOnClickListener(v -> finish());

        // Show UPI field or card fields based on method
        View cardFields = findViewById(R.id.cardFields);
        View upiField   = findViewById(R.id.upiField);
        if ("UPI".equals(method)) {
            cardFields.setVisibility(View.GONE);
            upiField  .setVisibility(View.VISIBLE);
        }

        ((TextView) findViewById(R.id.tvMethodLabel)) .setText(method);
        ((TextView) findViewById(R.id.tvTotalAmount)).setText(price);

        // Live-update card preview
        TextInputEditText etNum  = findViewById(R.id.etCardNumber);
        TextInputEditText etName = findViewById(R.id.etCardName);
        TextInputEditText etExp  = findViewById(R.id.etExpiry);
        TextView tvNumPrev  = findViewById(R.id.tvCardNumberPreview);
        TextView tvNamePrev = findViewById(R.id.tvCardNamePreview);
        TextView tvExpPrev  = findViewById(R.id.tvCardExpiryPreview);

        etNum.addTextChangedListener(new SimpleWatcher() {
            public void afterTextChanged(Editable s) {
                String raw = s.toString().replaceAll(" ", "");
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < raw.length() && i < 16; i++) {
                    if (i > 0 && i % 4 == 0) sb.append("  ");
                    sb.append(raw.charAt(i));
                }
                tvNumPrev.setText(sb.length() > 0 ? sb.toString() : "1234  5678  9123  1237");
            }
        });
        etName.addTextChangedListener(new SimpleWatcher() {
            public void afterTextChanged(Editable s) {
                tvNamePrev.setText(s.length() > 0 ? s.toString().toUpperCase() : "NIKHIL KUMAR");
            }
        });
        etExp.addTextChangedListener(new SimpleWatcher() {
            public void afterTextChanged(Editable s) {
                tvExpPrev.setText(s.length() > 0 ? s.toString() : "00/00");
            }
        });

        // Confirm
        final String fMethod = method;
        final String fPrice  = price;
        findViewById(R.id.btnConfirm).setOnClickListener(v -> {
            Intent intent = new Intent(this, ConfirmPaymentActivity.class);
            intent.putExtra("METHOD", fMethod);
            intent.putExtra("PRICE",  fPrice);
            startActivity(intent);
        });
    }

    abstract static class SimpleWatcher implements TextWatcher {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }
}
