package com.skyport.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.skyport.app.R;
import com.skyport.app.adapters.FlightAdapter;
import com.skyport.app.models.Flight;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FlightListActivity extends AppCompatActivity {

    private static final int FILTER_CHEAPEST = 0;
    private static final int FILTER_NONSTOP = 1;
    private static final int FILTER_MORNING = 2;
    private static final int FILTER_AFTERNOON = 3;
    private static final int FILTER_EVENING = 4;

    private RecyclerView rvFlights;
    private ProgressBar progressBar;
    private FlightAdapter flightAdapter;
    private FirebaseFirestore db;

    private final List<Flight> allFlights = new ArrayList<>();
    private int activeFilter = FILTER_CHEAPEST;

    private LinearLayout tabCheapest, tabNonStop, tabMorning, tabAfternoon, tabEvening;
    private TextView tvTabCheapPrice, tvTabNonStopInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flight_list);

        rvFlights = findViewById(R.id.rvFlights);
        progressBar = findViewById(R.id.progressBar);

        tabCheapest = findViewById(R.id.tabCheapest);
        tabNonStop = findViewById(R.id.tabNonStop);
        tabMorning = findViewById(R.id.tabMorning);
        tabAfternoon = findViewById(R.id.tabAfternoon);
        tabEvening = findViewById(R.id.tabEvening);

        tvTabCheapPrice = findViewById(R.id.tvTabCheapPrice);
        tvTabNonStopInfo = findViewById(R.id.tvTabNonStopInfo);

        rvFlights.setLayoutManager(new LinearLayoutManager(this));
        flightAdapter = new FlightAdapter();
        
        flightAdapter.setOnFlightClickListener(flight -> {
            Intent intent = new Intent(FlightListActivity.this, FlightDetailActivity.class);
            intent.putExtra("airline", flight.getAirline());
            intent.putExtra("source", flight.getSource());
            intent.putExtra("destination", flight.getDestination());
            intent.putExtra("flight_number", flight.getFlight_number());
            intent.putExtra("departure_time", flight.getDeparture_time());
            intent.putExtra("arrival_time", flight.getArrival_time());
            intent.putExtra("duration", flight.getDuration());
            intent.putExtra("price", flight.getPrice());
            intent.putExtra("departureTimeMillis", flight.getDepartureTimeMillis());
            intent.putExtra("arrivalTimeMillis", flight.getArrivalTimeMillis());
            
            // Forward passenger counts
            intent.putExtra("ADULT_COUNT", getIntent().getIntExtra("ADULT_COUNT", 1));
            intent.putExtra("CHILD_COUNT", getIntent().getIntExtra("CHILD_COUNT", 0));
            intent.putExtra("INFANT_COUNT", getIntent().getIntExtra("INFANT_COUNT", 0));
            
            startActivity(intent);
        });

        rvFlights.setAdapter(flightAdapter);

        tabCheapest.setOnClickListener(v -> setFilter(FILTER_CHEAPEST));
        tabNonStop.setOnClickListener(v -> setFilter(FILTER_NONSTOP));
        tabMorning.setOnClickListener(v -> setFilter(FILTER_MORNING));
        tabAfternoon.setOnClickListener(v -> setFilter(FILTER_AFTERNOON));
        tabEvening.setOnClickListener(v -> setFilter(FILTER_EVENING));

        String source = getIntent().getStringExtra("FROM_IATA");
        String destination = getIntent().getStringExtra("TO_IATA");

        db = FirebaseFirestore.getInstance();
        fetchFlights(source, destination);
    }

    // 🔥 FETCH DATA
    private void fetchFlights(String source, String destination) {

        progressBar.setVisibility(View.VISIBLE);

        Query query = db.collection("flights")
                .whereEqualTo("source", source)
                .whereEqualTo("destination", destination);

        query.get().addOnSuccessListener(snapshot -> {

            allFlights.clear();

            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());

            for (QueryDocumentSnapshot doc : snapshot) {

                Timestamp dep = doc.getTimestamp("departure_time");
                Timestamp arr = doc.getTimestamp("arrival_time");

                String depTime = dep != null ? sdf.format(dep.toDate()) : "";
                String arrTime = arr != null ? sdf.format(arr.toDate()) : "";

                long durationVal = doc.getLong("duration") != null ? doc.getLong("duration") : 0;
                String duration = (durationVal / 60) + "h " + (durationVal % 60) + "m";

                String price = String.valueOf(doc.getLong("price"));

                long depMillis = dep != null ? dep.toDate().getTime() : 0;
                long arrMillis = arr != null ? arr.toDate().getTime() : 0;

                allFlights.add(new Flight(
                        doc.getString("airline"),
                        doc.getString("airline_name"),
                        source,
                        destination,
                        depTime,
                        arrTime,
                        duration,
                        price,
                        doc.getString("flight_number"), // Use real flight number
                        null,
                        0,
                        depMillis,
                        arrMillis
                ));
            }

            progressBar.setVisibility(View.GONE);

            if (allFlights.isEmpty()) {
                Toast.makeText(this, "No flights found", Toast.LENGTH_SHORT).show();
            }

            updateTabLabels();   // ✅ FIXED
            setFilter(FILTER_CHEAPEST);

        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Error loading flights", Toast.LENGTH_SHORT).show();
        });
    }

    // 🔥 FILTER SWITCH
    private void setFilter(int filter) {
        activeFilter = filter;
        highlightActiveTab(filter);
        applyFilter(filter);
    }

    // 🔥 APPLY FILTER
    private void applyFilter(int filter) {

        List<Flight> result;

        switch (filter) {

            case FILTER_CHEAPEST:
                result = new ArrayList<>(allFlights);
                result.sort((a, b) ->
                        priceAsInt(a.getPrice()) - priceAsInt(b.getPrice()));
                break;

            case FILTER_NONSTOP:
                result = new ArrayList<>(allFlights);
                result.sort((a, b) ->
                        durationAsMinutes(a.getDuration()) - durationAsMinutes(b.getDuration()));
                break;

            case FILTER_MORNING:
                result = filterByTimeRange(0, 12);
                break;

            case FILTER_AFTERNOON:
                result = filterByTimeRange(12, 18);
                break;

            case FILTER_EVENING:
                result = filterByTimeRange(18, 24);
                break;

            default:
                result = new ArrayList<>(allFlights);
        }

        flightAdapter.setFlights(result);
    }

    // 🔥 TAB LABELS FIX
    private void updateTabLabels() {

        if (allFlights.isEmpty()) return;

        Flight cheapest = allFlights.get(0);
        Flight fastest = allFlights.get(0);

        for (Flight f : allFlights) {
            if (priceAsInt(f.getPrice()) < priceAsInt(cheapest.getPrice()))
                cheapest = f;

            if (durationAsMinutes(f.getDuration()) < durationAsMinutes(fastest.getDuration()))
                fastest = f;
        }

        tvTabCheapPrice.setText("₹" + cheapest.getPrice() + " | " +
                cheapest.getDeparture_time() + "-" + cheapest.getArrival_time());

        tvTabNonStopInfo.setText(fastest.getDuration() + " | " +
                fastest.getDeparture_time() + "-" + fastest.getArrival_time());
    }

    // 🔥 TAB HIGHLIGHT FIX
    private void highlightActiveTab(int filter) {

        setTabStyle(tabCheapest, false);
        setTabStyle(tabNonStop, false);
        setTabStyle(tabMorning, false);
        setTabStyle(tabAfternoon, false);
        setTabStyle(tabEvening, false);

        switch (filter) {
            case FILTER_CHEAPEST: setTabStyle(tabCheapest, true); break;
            case FILTER_NONSTOP: setTabStyle(tabNonStop, true); break;
            case FILTER_MORNING: setTabStyle(tabMorning, true); break;
            case FILTER_AFTERNOON: setTabStyle(tabAfternoon, true); break;
            case FILTER_EVENING: setTabStyle(tabEvening, true); break;
        }
    }

    private void setTabStyle(LinearLayout tab, boolean active) {

        tab.setBackgroundResource(active
                ? R.drawable.bg_flight_tab_active
                : R.drawable.bg_flight_tab_inactive);

        int color = active
                ? getResources().getColor(R.color.primary, null)
                : getResources().getColor(R.color.skyport_navy, null);

        for (int i = 0; i < tab.getChildCount(); i++) {
            if (tab.getChildAt(i) instanceof TextView) {
                ((TextView) tab.getChildAt(i)).setTextColor(color);
            }
        }
    }

    // 🔥 HELPERS
    private int priceAsInt(String price) {
        try {
            return Integer.parseInt(price.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return Integer.MAX_VALUE;
        }
    }

    private int durationAsMinutes(String duration) {
        try {
            int h = 0, m = 0;
            if (duration.contains("h")) {
                String[] parts = duration.split("h");
                h = Integer.parseInt(parts[0].trim());
                if (parts.length > 1)
                    m = Integer.parseInt(parts[1].replace("m", "").trim());
            }
            return h * 60 + m;
        } catch (Exception e) {
            return Integer.MAX_VALUE;
        }
    }

    private List<Flight> filterByTimeRange(int start, int end) {
        List<Flight> list = new ArrayList<>();
        for (Flight f : allFlights) {
            int hour = getHour(f.getDeparture_time());
            if (hour >= start && hour < end) {
                list.add(f);
            }
        }
        return list;
    }

    private int getHour(String time) {
        try {
            return Integer.parseInt(time.split(":")[0]);
        } catch (Exception e) {
            return -1;
        }
    }
}