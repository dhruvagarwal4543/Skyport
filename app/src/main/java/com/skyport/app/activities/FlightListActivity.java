package com.skyport.app.activities;

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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.skyport.app.R;
import com.skyport.app.adapters.FlightAdapter;
import com.skyport.app.models.Flight;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FlightListActivity extends AppCompatActivity {

    // Filter constants
    private static final int FILTER_CHEAPEST  = 0;
    private static final int FILTER_NONSTOP   = 1;
    private static final int FILTER_MORNING   = 2;
    private static final int FILTER_AFTERNOON = 3;
    private static final int FILTER_EVENING   = 4;

    private TextView tvRouteTitle, tvRouteDetails;
    private TextView tvTabCheapPrice, tvTabNonStopInfo;
    private LinearLayout tabCheapest, tabNonStop, tabMorning, tabAfternoon, tabEvening;
    private ImageButton btnBack;
    private RecyclerView rvFlights;
    private ProgressBar progressBar;
    private FlightAdapter flightAdapter;
    private FirebaseFirestore db;

    /** Master list: all fetched flights, never modified after fetch */
    private final List<Flight> allFlights = new ArrayList<>();
    private int activeFilter = FILTER_CHEAPEST;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flight_list);

        // Bind views
        tvRouteTitle      = findViewById(R.id.tvRouteTitle);
        tvRouteDetails    = findViewById(R.id.tvRouteDetails);
        tvTabCheapPrice   = findViewById(R.id.tvTabCheapPrice);
        tvTabNonStopInfo  = findViewById(R.id.tvTabNonStopInfo);
        tabCheapest       = findViewById(R.id.tabCheapest);
        tabNonStop        = findViewById(R.id.tabNonStop);
        tabMorning        = findViewById(R.id.tabMorning);
        tabAfternoon      = findViewById(R.id.tabAfternoon);
        tabEvening        = findViewById(R.id.tabEvening);
        btnBack           = findViewById(R.id.btnBack);
        rvFlights         = findViewById(R.id.rvFlights);
        progressBar       = findViewById(R.id.progressBar);

        btnBack.setOnClickListener(v -> finish());

        // Extract route info
        String fromName   = getIntent().getStringExtra("FROM_NAME");
        String fromIata   = getIntent().getStringExtra("FROM_IATA");
        String toName     = getIntent().getStringExtra("TO_NAME");
        String toIata     = getIntent().getStringExtra("TO_IATA");
        String date       = getIntent().getStringExtra("DATE");
        String travellers = getIntent().getStringExtra("TRAVELLERS");

        if (fromName != null && fromName.contains(" ("))
            fromName = fromName.substring(0, fromName.indexOf(" (")).trim();
        if (toName != null && toName.contains(" ("))
            toName = toName.substring(0, toName.indexOf(" (")).trim();

        String displayFrom = (fromName != null && !fromName.isEmpty()) ? fromName : fromIata;
        String displayTo   = (toName   != null && !toName.isEmpty())   ? toName   : toIata;

        tvRouteTitle.setText(displayFrom + " to " + displayTo);
        tvRouteDetails.setText(
            (date       != null ? date       : "") + " | " +
            (travellers != null ? travellers : "1 Adult") + " | Economy"
        );

        // RecyclerView
        rvFlights.setLayoutManager(new LinearLayoutManager(this));
        flightAdapter = new FlightAdapter();
        rvFlights.setAdapter(flightAdapter);

        // Tab click listeners
        tabCheapest .setOnClickListener(v -> setFilter(FILTER_CHEAPEST));
        tabNonStop  .setOnClickListener(v -> setFilter(FILTER_NONSTOP));
        tabMorning  .setOnClickListener(v -> setFilter(FILTER_MORNING));
        tabAfternoon.setOnClickListener(v -> setFilter(FILTER_AFTERNOON));
        tabEvening  .setOnClickListener(v -> setFilter(FILTER_EVENING));

        db = FirebaseFirestore.getInstance();
        fetchFlights(fromIata, toIata);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Firebase fetch
    // ─────────────────────────────────────────────────────────────────────────

    private void fetchFlights(String source, String destination) {
        if (source == null || destination == null) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Please select From and To airports", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        rvFlights.setVisibility(View.GONE);

        android.util.Log.d("FIREBASE_DEBUG", "Searching route: " + source + " → " + destination);

        // Step 1: Find the matching route document
        db.collection("routes")
                .whereEqualTo("from", source)
                .whereEqualTo("to", destination)
                .get()
                .addOnSuccessListener(routeSnapshots -> {
                    if (routeSnapshots.isEmpty()) {
                        android.util.Log.d("FIREBASE_DEBUG", "No route document found for " + source + " → " + destination);
                        progressBar.setVisibility(View.GONE);
                        rvFlights.setVisibility(View.VISIBLE);
                        Toast.makeText(this, "No flights available for this route.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    String routeId = routeSnapshots.getDocuments().get(0).getId();
                    android.util.Log.d("FIREBASE_DEBUG", "Route found: " + routeId);

                    // Step 2: Fetch flights from the subcollection
                    db.collection("routes")
                            .document(routeId)
                            .collection("flights")
                            .get()
                            .addOnSuccessListener(flightSnapshots -> {
                                android.util.Log.d("FIREBASE_DEBUG", "Flights count: " + flightSnapshots.size());

                                allFlights.clear();
                                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());

                                for (QueryDocumentSnapshot doc : flightSnapshots) {

                                    // departure_time (Timestamp or String)
                                    String depTime = "";
                                    Object depRaw  = doc.get("departure_time");
                                    if (depRaw instanceof Timestamp)
                                        depTime = sdf.format(((Timestamp) depRaw).toDate());
                                    else if (depRaw instanceof String)
                                        depTime = (String) depRaw;

                                    // arrival_time (Timestamp or String)
                                    String arrTime = "";
                                    Object arrRaw  = doc.get("arrival_time");
                                    if (arrRaw instanceof Timestamp)
                                        arrTime = sdf.format(((Timestamp) arrRaw).toDate());
                                    else if (arrRaw instanceof String)
                                        arrTime = (String) arrRaw;

                                    // duration → "Xh Ym"
                                    String duration = "";
                                    Object durRaw = doc.get("duration");
                                    if (durRaw instanceof Long) {
                                        long m = (Long) durRaw;
                                        duration = (m / 60) + "h " + (m % 60) + "m";
                                    } else if (durRaw instanceof Double) {
                                        long m = ((Double) durRaw).longValue();
                                        duration = (m / 60) + "h " + (m % 60) + "m";
                                    } else if (durRaw instanceof String) {
                                        duration = (String) durRaw;
                                    }

                                    // price (Long, Double, or String)
                                    String price = "";
                                    Object priceRaw = doc.get("price");
                                    if (priceRaw instanceof Long)
                                        price = String.valueOf((Long) priceRaw);
                                    else if (priceRaw instanceof Double)
                                        price = String.valueOf(((Double) priceRaw).longValue());
                                    else if (priceRaw instanceof String)
                                        price = (String) priceRaw;

                                    allFlights.add(new Flight(
                                            doc.getString("airline"),
                                            source,
                                            destination,
                                            depTime, arrTime, duration, price,
                                            doc.getString("flight_number")
                                    ));
                                }

                                progressBar.setVisibility(View.GONE);
                                rvFlights.setVisibility(View.VISIBLE);
                                updateTabLabels();
                                applyFilter(activeFilter);

                                if (allFlights.isEmpty()) {
                                    Toast.makeText(this, "No flights available for this route.", Toast.LENGTH_LONG).show();
                                }
                            })
                            .addOnFailureListener(e -> {
                                android.util.Log.e("FIREBASE_DEBUG", "Error fetching flights subcollection: " + e.getMessage());
                                progressBar.setVisibility(View.GONE);
                                rvFlights.setVisibility(View.VISIBLE);
                                Toast.makeText(this, "Error loading flights: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("FIREBASE_DEBUG", "Error fetching route: " + e.getMessage());
                    progressBar.setVisibility(View.GONE);
                    rvFlights.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "Error finding route: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    // ─────────────────────────────────────────────────────────────────────────
    // Dynamic tab labels
    // ─────────────────────────────────────────────────────────────────────────

    private void updateTabLabels() {
        // Cheapest
        Flight cheapest = getCheapestFlight();
        if (cheapest != null) {
            tvTabCheapPrice.setText(
                "₹" + cheapest.getPrice() + " | " + cheapest.getDeparture_time() + "–" + cheapest.getArrival_time()
            );
        }

        // Fastest (shortest duration)
        Flight fastest = getFastestFlight();
        if (fastest != null) {
            tvTabNonStopInfo.setText(
                fastest.getDuration() + " | " + fastest.getDeparture_time() + "–" + fastest.getArrival_time()
            );
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Filter selection
    // ─────────────────────────────────────────────────────────────────────────

    private void setFilter(int filter) {
        activeFilter = filter;
        highlightActiveTab(filter);
        applyFilter(filter);
    }

    private void applyFilter(int filter) {
        List<Flight> result;
        switch (filter) {
            case FILTER_CHEAPEST:
                result = sortByCheapest();
                break;
            case FILTER_NONSTOP:
                result = sortByFastest();
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

        if (result.isEmpty()) {
            Toast.makeText(this, "No flights match this filter.", Toast.LENGTH_SHORT).show();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Filter / Sort helpers
    // ─────────────────────────────────────────────────────────────────────────

    /** Returns all flights sorted cheapest-first */
    private List<Flight> sortByCheapest() {
        List<Flight> sorted = new ArrayList<>(allFlights);
        sorted.sort((a, b) -> priceAsInt(a.getPrice()) - priceAsInt(b.getPrice()));
        return sorted;
    }

    /** Returns all flights sorted by duration (shortest first) */
    private List<Flight> sortByFastest() {
        List<Flight> sorted = new ArrayList<>(allFlights);
        sorted.sort((a, b) -> durationAsMinutes(a.getDuration()) - durationAsMinutes(b.getDuration()));
        return sorted;
    }

    /** Filter flights whose departure_time falls in [startHour, endHour) */
    private List<Flight> filterByTimeRange(int startHour, int endHour) {
        List<Flight> filtered = new ArrayList<>();
        for (Flight f : allFlights) {
            int hour = departureHour(f.getDeparture_time());
            if (hour >= startHour && hour < endHour) {
                filtered.add(f);
            }
        }
        return filtered;
    }

    private Flight getCheapestFlight() {
        if (allFlights.isEmpty()) return null;
        Flight cheapest = allFlights.get(0);
        for (Flight f : allFlights)
            if (priceAsInt(f.getPrice()) < priceAsInt(cheapest.getPrice()))
                cheapest = f;
        return cheapest;
    }

    private Flight getFastestFlight() {
        if (allFlights.isEmpty()) return null;
        Flight fastest = allFlights.get(0);
        for (Flight f : allFlights)
            if (durationAsMinutes(f.getDuration()) < durationAsMinutes(fastest.getDuration()))
                fastest = f;
        return fastest;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Parsing utilities
    // ─────────────────────────────────────────────────────────────────────────

    /** "₹ 4648" or "4648" → 4648 */
    private int priceAsInt(String price) {
        if (price == null) return Integer.MAX_VALUE;
        try {
            return Integer.parseInt(price.replaceAll("[^0-9]", "").trim());
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
    }

    /** "2h 30m" → 150 total minutes; also handles plain numbers stored as strings */
    private int durationAsMinutes(String duration) {
        if (duration == null) return Integer.MAX_VALUE;
        try {
            // format "Xh Ym"
            int h = 0, m = 0;
            if (duration.contains("h")) {
                String[] parts = duration.split("h");
                h = Integer.parseInt(parts[0].trim());
                if (parts.length > 1)
                    m = Integer.parseInt(parts[1].replace("m", "").trim());
            } else {
                // plain minutes number
                m = Integer.parseInt(duration.replaceAll("[^0-9]", "").trim());
            }
            return h * 60 + m;
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
    }

    /** "08:30" → 8 */
    private int departureHour(String time) {
        if (time == null || !time.contains(":")) return -1;
        try {
            return Integer.parseInt(time.split(":")[0].trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Tab highlight
    // ─────────────────────────────────────────────────────────────────────────

    private void highlightActiveTab(int filter) {
        // Reset all tabs to inactive style
        setTabStyle(tabCheapest,  false);
        setTabStyle(tabNonStop,   false);
        setTabStyle(tabMorning,   false);
        setTabStyle(tabAfternoon, false);
        setTabStyle(tabEvening,   false);

        // Activate selected
        switch (filter) {
            case FILTER_CHEAPEST:  setTabStyle(tabCheapest,  true); break;
            case FILTER_NONSTOP:   setTabStyle(tabNonStop,   true); break;
            case FILTER_MORNING:   setTabStyle(tabMorning,   true); break;
            case FILTER_AFTERNOON: setTabStyle(tabAfternoon, true); break;
            case FILTER_EVENING:   setTabStyle(tabEvening,   true); break;
        }
    }

    private void setTabStyle(LinearLayout tab, boolean active) {
        tab.setBackgroundResource(active
                ? R.drawable.bg_flight_tab_active
                : R.drawable.bg_flight_tab_inactive);

        // Update text colours for all child TextViews
        int textColor = active
                ? getResources().getColor(R.color.primary, null)
                : getResources().getColor(R.color.skyport_navy, null);
        int subColor  = active
                ? getResources().getColor(R.color.primary, null)
                : getResources().getColor(R.color.skyport_label_gray, null);

        if (tab.getChildCount() >= 1 && tab.getChildAt(0) instanceof TextView)
            ((TextView) tab.getChildAt(0)).setTextColor(textColor);
        if (tab.getChildCount() >= 2 && tab.getChildAt(1) instanceof TextView)
            ((TextView) tab.getChildAt(1)).setTextColor(subColor);
    }
}