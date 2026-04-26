package com.skyport.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.skyport.app.R;
import com.skyport.app.adapters.TicketAdapter;
import com.skyport.app.models.Ticket;

import java.util.List;

public class MyTicketsActivity extends AppCompatActivity {

    private RecyclerView rvTickets;
    private View         emptyState;
    private TextView     btnUpcoming, btnPast;
    private boolean      isUpcomingSelected = true;
    private TicketAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_tickets);

        // Views
        rvTickets  = findViewById(R.id.rvTickets);
        emptyState = findViewById(R.id.emptyState);
        btnUpcoming = findViewById(R.id.btnUpcoming);
        btnPast     = findViewById(R.id.btnPast);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // RecyclerView
        rvTickets.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TicketAdapter(ticket -> {
            Intent intent = new Intent(this, TicketDetailActivity.class);
            intent.putExtra("TICKET", ticket);
            startActivity(intent);
        });
        rvTickets.setAdapter(adapter);

        // Bottom nav: highlight suitcase (My Tickets) and route other tabs
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setSelectedItemId(R.id.nav_suitcase);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_suitcase) return true; // already here
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("NAV_ID", id);
            startActivity(intent);
            finish();
            return true;
        });

        // Toggle
        btnUpcoming.setOnClickListener(v -> selectTab(true));
        btnPast    .setOnClickListener(v -> selectTab(false));

        // Show upcoming by default
        showUpcoming();
    }

    private void selectTab(boolean upcoming) {
        isUpcomingSelected = upcoming;

        if (upcoming) {
            btnUpcoming.setBackgroundResource(R.drawable.bg_toggle_selected);
            btnUpcoming.setTextColor(getColor(R.color.white));
            btnPast.setBackground(null);
            btnPast.setTextColor(getColor(R.color.skyport_label_gray));
            showUpcoming();
        } else {
            btnPast.setBackgroundResource(R.drawable.bg_toggle_selected);
            btnPast.setTextColor(getColor(R.color.white));
            btnUpcoming.setBackground(null);
            btnUpcoming.setTextColor(getColor(R.color.skyport_label_gray));
            showEmpty();
        }
    }

    private void showUpcoming() {
        List<Ticket> tickets = Ticket.getUpcomingTickets();
        adapter.setTickets(tickets);
        rvTickets .setVisibility(View.VISIBLE);
        emptyState.setVisibility(View.GONE);
    }

    private void showEmpty() {
        rvTickets .setVisibility(View.GONE);
        emptyState.setVisibility(View.VISIBLE);
    }
}
