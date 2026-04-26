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

public class TicketDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_detail);

        ((ImageButton) findViewById(R.id.btnBack)).setOnClickListener(v -> finish());

        Ticket t = (Ticket) getIntent().getSerializableExtra("TICKET");
        if (t == null) { finish(); return; }

        ((TextView) findViewById(R.id.tvFromCity))    .setText(t.fromCity);
        ((TextView) findViewById(R.id.tvFromTime))    .setText(t.fromTime);
        ((TextView) findViewById(R.id.tvFromTerminal)).setText(t.fromTerminal);
        ((TextView) findViewById(R.id.tvToCity))      .setText(t.toCity);
        ((TextView) findViewById(R.id.tvToTime))      .setText(t.toTime);
        ((TextView) findViewById(R.id.tvToTerminal))  .setText(t.toTerminal);
        ((TextView) findViewById(R.id.tvGate))        .setText(t.gate);
        ((TextView) findViewById(R.id.tvFlight))      .setText(t.flight);
        ((TextView) findViewById(R.id.tvSeat))        .setText(t.seat);
        ((TextView) findViewById(R.id.tvTraveller))   .setText(t.traveller);
        ((TextView) findViewById(R.id.tvClass))       .setText(t.travelClass);
        ((TextView) findViewById(R.id.tvTicketNumber)).setText(t.ticketNumber);

        // Mini QR → open QRActivity
        ImageView ivQrMini = findViewById(R.id.ivQrMini);
        ivQrMini.setOnClickListener(v -> {
            Intent intent = new Intent(this, QRActivity.class);
            intent.putExtra("TICKET_NUMBER", t.ticketNumber);
            startActivity(intent);
        });

        // Print Ticket
        findViewById(R.id.btnPrintTicket).setOnClickListener(v ->
                Toast.makeText(this, "Printing ticket…", Toast.LENGTH_SHORT).show()
        );
    }
}
