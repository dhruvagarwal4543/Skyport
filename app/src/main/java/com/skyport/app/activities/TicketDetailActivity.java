package com.skyport.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.skyport.app.R;
import com.skyport.app.models.Ticket;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TicketDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_detail);

        ((ImageButton) findViewById(R.id.btnBack)).setOnClickListener(v -> finish());

        Ticket t = (Ticket) getIntent().getSerializableExtra("TICKET");
        if (t == null) { finish(); return; }

        ((TextView) findViewById(R.id.tvFromCity))    .setText(t.fromCode != null ? t.fromCode : "");
        ((TextView) findViewById(R.id.tvToCity))      .setText(t.toCode != null ? t.toCode : "");
        ((TextView) findViewById(R.id.tvFlight))      .setText(t.flightNumber != null ? t.flightNumber : "");
        ((TextView) findViewById(R.id.tvTraveller))   .setText(t.passengerName != null ? t.passengerName : "");
        ((TextView) findViewById(R.id.tvTicketNumber)).setText(t.bookingId != null ? t.bookingId : "N/A");

        ((TextView) findViewById(R.id.tvFromTerminal)).setText("T-1");
        ((TextView) findViewById(R.id.tvToTerminal))  .setText("T-2");
        ((TextView) findViewById(R.id.tvGate))        .setText(t.gate != null ? t.gate : "TBD");
        ((TextView) findViewById(R.id.tvSeat))        .setText(t.seat != null ? t.seat : "TBD");
        ((TextView) findViewById(R.id.tvClass))       .setText("Economy");

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm EEE, dd MMM", Locale.getDefault());
        if (t.departureTime != null) {
            ((TextView) findViewById(R.id.tvFromTime)).setText(sdf.format(t.departureTime.toDate()));
        }
        if (t.arrivalTime != null) {
            ((TextView) findViewById(R.id.tvToTime)).setText(sdf.format(t.arrivalTime.toDate()));
        }

        // Mini QR → open QRActivity
        ImageView ivQrMini = findViewById(R.id.ivQrMini);
        ivQrMini.setOnClickListener(v -> {
            Intent intent = new Intent(this, QRActivity.class);
            intent.putExtra("TICKET_NUMBER", t.bookingId);
            startActivity(intent);
        });

        // Print Ticket
        findViewById(R.id.btnPrintTicket).setOnClickListener(v ->
                Toast.makeText(this, "Printing ticket…", Toast.LENGTH_SHORT).show()
        );
    }
}
