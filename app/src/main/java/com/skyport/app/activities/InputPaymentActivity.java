package com.skyport.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.skyport.app.R;

import java.util.HashMap;
import java.util.Map;

public class InputPaymentActivity extends AppCompatActivity {

    private String method;
    private String price;

    private TextInputEditText etNum, etName, etExp, etUpiId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_payment);

        method = getIntent().getStringExtra("METHOD");
        price  = getIntent().getStringExtra("PRICE");
        if (method == null) method = "Credit Card";
        if (price  == null) price  = "₹ 6,000.00";

        ((ImageButton) findViewById(R.id.btnBack)).setOnClickListener(v -> finish());

        View cardFields    = findViewById(R.id.cardFields);
        View upiSection    = findViewById(R.id.upiSection);
        View amazonPayView = findViewById(R.id.amazonPayView);
        View cardPreview   = findViewById(R.id.cardPreviewView);

        // Hide everything first
        cardFields   .setVisibility(View.GONE);
        upiSection   .setVisibility(View.GONE);
        amazonPayView.setVisibility(View.GONE);

        if ("UPI".equals(method)) {
            upiSection  .setVisibility(View.VISIBLE);
            cardPreview .setVisibility(View.GONE);
        } else if ("Amazon Pay".equals(method)) {
            amazonPayView.setVisibility(View.VISIBLE);
            cardPreview .setVisibility(View.GONE);
        } else {
            cardFields  .setVisibility(View.VISIBLE);
            cardPreview .setVisibility(View.VISIBLE);
        }

        ((TextView) findViewById(R.id.tvMethodLabel)).setText(method);
        ((TextView) findViewById(R.id.tvTotalAmount)).setText(price);

        // Card field refs (safe even when gone — just won't be visible)
        etNum  = findViewById(R.id.etCardNumber);
        etName = findViewById(R.id.etCardName);
        etExp  = findViewById(R.id.etExpiry);
        etUpiId = findViewById(R.id.etUpiId);

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
                tvNamePrev.setText(s.length() > 0 ? s.toString().toUpperCase() : "YOUR NAME");
            }
        });
        etExp.addTextChangedListener(new SimpleWatcher() {
            public void afterTextChanged(Editable s) {
                tvExpPrev.setText(s.length() > 0 ? s.toString() : "00/00");
            }
        });

        final String fMethod = method;
        final String fPrice  = price;

        findViewById(R.id.btnConfirm).setOnClickListener(v -> {
            if ("UPI".equals(fMethod)) {
                String upiId = etUpiId != null ? etUpiId.getText().toString().trim() : "";
                if (upiId.isEmpty()) {
                    Toast.makeText(this, "Please enter your UPI ID", Toast.LENGTH_SHORT).show();
                    return;
                }
                CheckBox cbUpi = findViewById(R.id.cbRememberUpi);
                if (cbUpi != null && cbUpi.isChecked()) {
                    saveUpiToFirebase(upiId);
                }
            } else if ("Amazon Pay".equals(fMethod)) {
                CheckBox cbAmazon = findViewById(R.id.cbRememberAmazon);
                if (cbAmazon != null && cbAmazon.isChecked()) {
                    saveAmazonPayToFirebase();
                }
            } else {
                // Card methods
                CheckBox cbCard = findViewById(R.id.cbSaveCard);
                if (cbCard != null && cbCard.isChecked()) {
                    saveCardToFirebase(fMethod,
                            etNum.getText().toString(),
                            etName.getText().toString(),
                            etExp.getText().toString());
                }
            }

            Intent intent = new Intent(this, ConfirmPaymentActivity.class);
            intent.putExtras(getIntent());
            intent.putExtra("METHOD", fMethod);
            intent.putExtra("PRICE", fPrice);

            if (etNum != null) {
                String rawNum = etNum.getText().toString().replaceAll(" ", "");
                if (rawNum.length() >= 4) {
                    intent.putExtra("SAVED_CARD_LAST4", rawNum.substring(rawNum.length() - 4));
                    intent.putExtra("SAVED_CARD_NAME", etName.getText().toString());
                }
            }
            startActivity(intent);
        });
    }

    private void saveCardToFirebase(String type, String number, String name, String expiry) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        String rawNum = number.replaceAll(" ", "");
        if (rawNum.length() < 4) return;

        Map<String, Object> data = new HashMap<>();
        data.put("type",   type);
        data.put("last4",  rawNum.substring(rawNum.length() - 4));
        data.put("value",  rawNum.substring(rawNum.length() - 4));
        data.put("name",   name);
        data.put("expiry", expiry);
        data.put("saved",  true);

        FirebaseFirestore.getInstance()
                .collection("users").document(user.getUid())
                .collection("payment_methods").add(data)
                .addOnFailureListener(e -> { /* silent */ });
    }

    private void saveUpiToFirebase(String upiId) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("type",   "UPI");
        data.put("upi_id", upiId);
        data.put("last4",  upiId.length() > 4 ? upiId.substring(upiId.length() - 4) : upiId);
        data.put("value",  upiId);
        data.put("name",   "");
        data.put("expiry", "");
        data.put("saved",  true);

        FirebaseFirestore.getInstance()
                .collection("users").document(user.getUid())
                .collection("payment_methods").add(data)
                .addOnFailureListener(e -> { /* silent */ });
    }

    private void saveAmazonPayToFirebase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("type",   "Amazon Pay");
        data.put("last4",  "Pay");
        data.put("value",  "Amazon Pay");
        data.put("name",   user.getDisplayName() != null ? user.getDisplayName() : "User");
        data.put("expiry", "");
        data.put("saved",  true);

        FirebaseFirestore.getInstance()
                .collection("users").document(user.getUid())
                .collection("payment_methods").add(data)
                .addOnFailureListener(e -> { /* silent */ });
    }

    abstract static class SimpleWatcher implements TextWatcher {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }
}
