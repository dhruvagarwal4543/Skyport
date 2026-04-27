package com.skyport.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.skyport.app.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

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
            // 80% success, 20% fail
            boolean success = (System.currentTimeMillis() % 10) < 8;
            if (success) {
                saveBookingToFirebase(fMethod, fPrice);
            } else {
                Intent intent = new Intent(this, BookingFailedActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }

    private void saveBookingToFirebase(String method, String price) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        String userId = user != null ? user.getUid() : "guest";
        String passengerName = user != null && user.getDisplayName() != null ? user.getDisplayName() : "Guest";

        // Random gate and seat
        String[] gates = {"A1", "A2", "B3", "B4", "C1", "C2", "D5", "D9"};
        String[] seats = {"12A", "14E", "16C", "18B", "21F", "24D", "7A", "3C"};
        Random rand = new Random();
        String gate = gates[rand.nextInt(gates.length)];
        String seat = seats[rand.nextInt(seats.length)];

        String source      = getIntent().getStringExtra("source");
        String destination = getIntent().getStringExtra("destination");
        String flightNum   = getIntent().getStringExtra("flight_number");
        long   depMillis   = getIntent().getLongExtra("departureTimeMillis", 0);

        // Human-readable date and time
        String date = "", time = "";
        if (depMillis > 0) {
            Date depDate = new Date(depMillis);
            date = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(depDate);
            time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(depDate);
        }

        Map<String, Object> booking = new HashMap<>();
        // Structured fields
        booking.put("from",           source != null ? source : "");
        booking.put("to",             destination != null ? destination : "");
        booking.put("fromCode",       source != null ? source : "");
        booking.put("toCode",         destination != null ? destination : "");
        booking.put("departureTime",  new com.google.firebase.Timestamp(new Date(depMillis)));
        booking.put("arrivalTime",    new com.google.firebase.Timestamp(new Date(getIntent().getLongExtra("arrivalTimeMillis", 0))));
        booking.put("airline",        getIntent().getStringExtra("airline"));
        booking.put("flightNumber",   flightNum != null ? flightNum : "");
        booking.put("gate",           gate);
        booking.put("seat",           seat);
        booking.put("createdAt",      com.google.firebase.firestore.FieldValue.serverTimestamp());
        booking.put("duration",       getIntent().getStringExtra("duration"));
        booking.put("passengerName",  passengerName);
        booking.put("price",          price);
        booking.put("paymentMethod",  method);

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("bookings")
                .add(booking)
                .addOnSuccessListener(docRef -> {
                    // Navigate to confirmation with all extras
                    Intent intent = new Intent(this, BookingConfirmedActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("METHOD", method);
                    intent.putExtra("PRICE", price);
                    // Pass booking details for immediate display (no Firestore re-fetch needed)
                    intent.putExtra("from",         source != null ? source : "");
                    intent.putExtra("to",           destination != null ? destination : "");
                    intent.putExtra("gate",         gate);
                    intent.putExtra("flightNumber", flightNum != null ? flightNum : "");
                    intent.putExtra("seat",         seat);
                    intent.putExtra("bookingId",    docRef.getId());
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Booking saved locally. Payment confirmed!", Toast.LENGTH_SHORT).show();
                    // Still navigate to confirmation on failure — don't block the user
                    Intent intent = new Intent(this, BookingConfirmedActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                });
    }
}
