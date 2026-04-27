package com.skyport.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.skyport.app.R;
import com.skyport.app.adapters.TicketAdapter;
import com.skyport.app.models.Ticket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MyTicketsActivity extends AppCompatActivity {

    private RecyclerView rvTickets;
    private View         emptyState;
    private TextView     tvEmptyStateMsg;
    private TextView     btnUpcoming, btnPast;
    private boolean      isUpcomingSelected = true;
    private TicketAdapter adapter;
    private ProgressBar  progressBar;

    private List<Ticket> allTickets = new ArrayList<>();
    private com.google.firebase.firestore.ListenerRegistration registration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_tickets);

        // Views
        rvTickets  = findViewById(R.id.rvTickets);
        emptyState = findViewById(R.id.emptyState);
        tvEmptyStateMsg = emptyState.findViewById(R.id.tvEmptyStateMsg); // Need to add ID to XML later if not present
        if (tvEmptyStateMsg == null) {
            tvEmptyStateMsg = (TextView) ((android.widget.LinearLayout)emptyState).getChildAt(1);
        }
        btnUpcoming = findViewById(R.id.btnUpcoming);
        btnPast     = findViewById(R.id.btnPast);
        progressBar = findViewById(R.id.progressBar);

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
        selectTab(true);

        fetchTickets();
    }

    private void fetchTickets() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            android.widget.Toast.makeText(this, "Not logged in", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        rvTickets.setVisibility(View.GONE);
        emptyState.setVisibility(View.GONE);

        registration = FirebaseFirestore.getInstance().collection("users")
                .document(user.getUid())
                .collection("bookings")
                .addSnapshotListener((snapshot, e) -> {
                    progressBar.setVisibility(View.GONE);
                    if (e != null || snapshot == null) {
                        android.widget.Toast.makeText(this, "Failed to load tickets", android.widget.Toast.LENGTH_SHORT).show();
                        return;
                    }
                    allTickets.clear();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Ticket t = doc.toObject(Ticket.class);
                        t.bookingId = doc.getId();
                        allTickets.add(t);
                    }
                    updateList();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (registration != null) {
            registration.remove();
        }
    }

    private void selectTab(boolean upcoming) {
        isUpcomingSelected = upcoming;

        if (upcoming) {
            btnUpcoming.setBackgroundResource(R.drawable.bg_toggle_selected);
            btnUpcoming.setTextColor(getColor(R.color.white));
            btnPast.setBackground(null);
            btnPast.setTextColor(getColor(R.color.skyport_label_gray));
        } else {
            btnPast.setBackgroundResource(R.drawable.bg_toggle_selected);
            btnPast.setTextColor(getColor(R.color.white));
            btnUpcoming.setBackground(null);
            btnUpcoming.setTextColor(getColor(R.color.skyport_label_gray));
        }
        updateList();
    }

    private void updateList() {
        long currentTime = System.currentTimeMillis();
        List<Ticket> filtered = new ArrayList<>();

        for (Ticket t : allTickets) {
            if (t.departureTime == null) continue;
            long depTime = t.departureTime.toDate().getTime();
            if (isUpcomingSelected) {
                if (depTime > currentTime) {
                    filtered.add(t);
                }
            } else {
                if (depTime <= currentTime) {
                    filtered.add(t);
                }
            }
        }

        if (isUpcomingSelected) {
            Collections.sort(filtered, (a, b) -> Long.compare(a.departureTime.toDate().getTime(), b.departureTime.toDate().getTime()));
        } else {
            Collections.sort(filtered, (a, b) -> Long.compare(b.departureTime.toDate().getTime(), a.departureTime.toDate().getTime()));
        }

        if (filtered.isEmpty()) {
            showEmpty();
        } else {
            adapter.setTickets(filtered);
            rvTickets.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }
    }

    private void showEmpty() {
        rvTickets .setVisibility(View.GONE);
        emptyState.setVisibility(View.VISIBLE);
        if (tvEmptyStateMsg != null) {
            tvEmptyStateMsg.setText(isUpcomingSelected ? "No upcoming flights" : "No past flights");
        }
    }
}
