package com.skyport.app.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.skyport.app.R;
import com.skyport.app.fragments.AirportMapBottomSheet;
import com.skyport.app.models.Airport;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AirportSearchActivity extends AppCompatActivity {

    private EditText etSearchAirport;
    private ImageButton btnBack;
    private ProgressBar progressBar;
    private LinearLayout llRecentSearches;
    private LinearLayout llPopularCities;
    private LinearLayout sectionRecent;
    private TextView tvListHeader;

    private List<Airport> allAirports = new ArrayList<>();
    private List<Airport> displayedAirports = new ArrayList<>();
    private FirebaseFirestore db;
    private SharedPreferences sharedPreferences;

    private static final String PREFS_NAME = "SkyportPrefs";
    private static final String KEY_RECENT_SEARCHES = "recent_searches";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_airport_search);

        etSearchAirport = findViewById(R.id.etSearchAirport);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);
        llRecentSearches = findViewById(R.id.llRecentSearches);
        llPopularCities = findViewById(R.id.llPopularCities);
        sectionRecent = findViewById(R.id.sectionRecent);
        tvListHeader = findViewById(R.id.tvListHeader);

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        db = FirebaseFirestore.getInstance();

        String hintType = getIntent().getStringExtra("HINT_TYPE");
        if (hintType != null && !hintType.isEmpty()) {
            etSearchAirport.setHint(hintType);
        }

        btnBack.setOnClickListener(v -> finish());

        etSearchAirport.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterAirports(s.toString().trim().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        loadRecentSearches();
        fetchAirports();
    }

    private void fetchAirports() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("airports")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allAirports.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String city = document.getString("city");
                        String iata = document.getString("iata");
                        String name = document.getString("name");
                        if (city != null && iata != null) {
                            if (name == null) name = city + " Airport";

                            // Read coordinates (may be absent in older docs)
                            Double lat = document.getDouble("latitude");
                            Double lng = document.getDouble("longitude");

                            Airport airport;
                            if (lat != null && lng != null) {
                                airport = new Airport(city, iata, name, lat, lng);
                            } else {
                                airport = new Airport(city, iata, name);
                            }
                            allAirports.add(airport);
                        }
                    }
                    filterAirports(etSearchAirport.getText().toString().trim().toLowerCase());
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(AirportSearchActivity.this, "Error fetching airports", Toast.LENGTH_SHORT).show();
                });
    }

    private void filterAirports(String query) {
        displayedAirports.clear();
        if (query.isEmpty()) {
            sectionRecent.setVisibility(getRecentAirports().isEmpty() ? View.GONE : View.VISIBLE);
            tvListHeader.setText("POPULAR CITIES");
            displayedAirports.addAll(allAirports);
        } else {
            sectionRecent.setVisibility(View.GONE);
            tvListHeader.setText("SEARCH RESULTS");
            for (Airport airport : allAirports) {
                if (airport.getCity().toLowerCase().contains(query) ||
                    airport.getIata().toLowerCase().contains(query) ||
                    airport.getName().toLowerCase().contains(query)) {
                    displayedAirports.add(airport);
                }
            }
        }
        populateAirportList(llPopularCities, displayedAirports);
    }

    private void populateAirportList(LinearLayout container, List<Airport> airports) {
        container.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        for (Airport airport : airports) {
            View itemView = inflater.inflate(R.layout.item_airport, container, false);

            TextView tvIata        = itemView.findViewById(R.id.tvIata);
            TextView tvCity        = itemView.findViewById(R.id.tvCity);
            TextView tvAirportName = itemView.findViewById(R.id.tvAirportName);
            View     ivPin         = itemView.findViewById(R.id.ivLocationPin);

            tvIata.setText(airport.getIata());
            tvCity.setText(airport.getCity());
            tvAirportName.setText(airport.getName());

            // Row tap → select airport
            itemView.setOnClickListener(v -> handleAirportSelection(airport));

            // Location pin tap → show map bottom sheet
            ivPin.setOnClickListener(v -> {
                AirportMapBottomSheet sheet = AirportMapBottomSheet.newInstance(
                        airport.getIata(),
                        airport.getCity(),
                        airport.getName(),
                        airport.hasCoordinates(),
                        airport.getLatitude(),
                        airport.getLongitude()
                );
                sheet.show(getSupportFragmentManager(), "AirportMap");
            });

            container.addView(itemView);
        }
    }

    private void handleAirportSelection(Airport airport) {
        saveRecentSearch(airport);
        String selectedStr = airport.getCity() + " (" + airport.getIata() + ")";
        Intent intent = new Intent();
        intent.putExtra("SELECTED_CITY", selectedStr);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void loadRecentSearches() {
        List<Airport> recents = getRecentAirports();
        if (recents.isEmpty()) {
            sectionRecent.setVisibility(View.GONE);
        } else {
            sectionRecent.setVisibility(View.VISIBLE);
            populateAirportList(llRecentSearches, recents);
        }
    }

    private List<Airport> getRecentAirports() {
        List<Airport> recents = new ArrayList<>();
        String json = sharedPreferences.getString(KEY_RECENT_SEARCHES, null);
        if (json != null) {
            try {
                JSONArray jsonArray = new JSONArray(json);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    recents.add(new Airport(
                            obj.getString("city"),
                            obj.getString("iata"),
                            obj.getString("name")
                    ));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return recents;
    }

    private void saveRecentSearch(Airport airport) {
        List<Airport> recents = getRecentAirports();
        
        // Remove if already exists to move to top
        for (int i = 0; i < recents.size(); i++) {
            if (recents.get(i).getIata().equals(airport.getIata())) {
                recents.remove(i);
                break;
            }
        }
        
        recents.add(0, airport);
        // Requirement specifies keeping '1-2 recent items at top'
        if (recents.size() > 2) {
            recents = recents.subList(0, 2);
        }

        JSONArray jsonArray = new JSONArray();
        try {
            for (Airport a : recents) {
                JSONObject obj = new JSONObject();
                obj.put("city", a.getCity());
                obj.put("iata", a.getIata());
                obj.put("name", a.getName());
                jsonArray.put(obj);
            }
            sharedPreferences.edit().putString(KEY_RECENT_SEARCHES, jsonArray.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
