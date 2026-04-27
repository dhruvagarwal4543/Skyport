package com.skyport.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.skyport.app.R;

public class BookingConfirmedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_confirmed);

        // Try reading from intent extras first (passed from ConfirmPaymentActivity)
        String from       = getIntent().getStringExtra("from");
        String to         = getIntent().getStringExtra("to");
        String gate       = getIntent().getStringExtra("gate");
        String flightNum  = getIntent().getStringExtra("flightNumber");
        String seat       = getIntent().getStringExtra("seat");
        String bookingId  = getIntent().getStringExtra("bookingId");

        if (from != null) {
            // Populate directly from extras
            populateUI(from, to, gate, flightNum, seat);
        } else if (bookingId != null) {
            // Fetch from Firestore if only bookingId is given
            fetchAndPopulate(bookingId);
        } else {
            // Fallback: try fetching the latest booking
            fetchLatestBooking();
        }

        findViewById(R.id.btnGoHome).setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });
    }

    private void populateUI(String from, String to, String gate, String flightNum, String seat) {
        TextView tvFrom   = findViewById(R.id.tvSuccessFrom);
        TextView tvTo     = findViewById(R.id.tvSuccessTo);
        TextView tvGate   = findViewById(R.id.tvSuccessGate);
        TextView tvFlight = findViewById(R.id.tvSuccessFlight);
        TextView tvSeat   = findViewById(R.id.tvSuccessSeat);

        if (tvFrom   != null) tvFrom  .setText(from   != null ? from   : "—");
        if (tvTo     != null) tvTo    .setText(to     != null ? to     : "—");
        if (tvGate   != null) tvGate  .setText("Gate: " + (gate   != null ? gate   : "—"));
        if (tvFlight != null) tvFlight.setText(flightNum != null ? flightNum : "—");
        if (tvSeat   != null) tvSeat  .setText("Seat: " + (seat   != null ? seat   : "—"));
    }

    private void fetchAndPopulate(String bookingId) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .collection("bookings")
                .document(bookingId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        populateUI(
                            doc.getString("from"),
                            doc.getString("to"),
                            doc.getString("gate"),
                            doc.getString("flightNumber"),
                            doc.getString("seat")
                        );
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Could not load booking.", Toast.LENGTH_SHORT).show());
    }

    private void fetchLatestBooking() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .collection("bookings")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        com.google.firebase.firestore.QueryDocumentSnapshot doc =
                                (com.google.firebase.firestore.QueryDocumentSnapshot) query.getDocuments().get(0);
                        populateUI(
                            doc.getString("from"),
                            doc.getString("to"),
                            doc.getString("gate"),
                            doc.getString("flightNumber"),
                            doc.getString("seat")
                        );
                    }
                    // If empty, XML default values show — no crash
                })
                .addOnFailureListener(e -> {
                    // Silent — XML defaults remain
                });
    }
}
