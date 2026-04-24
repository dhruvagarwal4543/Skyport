package com.skyport.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.skyport.app.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AirportSearchActivity extends AppCompatActivity {

    private EditText etSearchAirport;
    private ListView lvAirports;
    private ProgressBar progressBar;
    private ExecutorService executorService;
    private ArrayAdapter<String> adapter;
    private List<String> airportNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_airport_search);

        etSearchAirport = findViewById(R.id.etSearchAirport);
        lvAirports = findViewById(R.id.lvAirports);
        progressBar = findViewById(R.id.progressBar);
        
        executorService = Executors.newSingleThreadExecutor();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, airportNames);
        lvAirports.setAdapter(adapter);

        lvAirports.setOnItemClickListener((parent, view, position, id) -> {
            String selectedCity = airportNames.get(position);
            Intent intent = new Intent();
            intent.putExtra("SELECTED_CITY", selectedCity);
            setResult(RESULT_OK, intent);
            finish();
        });

        etSearchAirport.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.length() > 2) {
                    searchAirports(query);
                } else {
                    airportNames.clear();
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void searchAirports(String query) {
        progressBar.setVisibility(View.VISIBLE);
        executorService.execute(() -> {
            try {
                URL url = new URL("https://api.api-ninjas.com/v1/airports?name=" + query);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("X-Api-Key", "YOUR_API_KEY");

                StringBuilder response = new StringBuilder();
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    JSONArray jsonArray = new JSONArray(response.toString());
                    List<String> results = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        String name = obj.optString("name");
                        String city = obj.optString("city");
                        results.add(city + " (" + name + ")");
                    }

                    runOnUiThread(() -> {
                        airportNames.clear();
                        airportNames.addAll(results);
                        if (airportNames.isEmpty()) {
                            airportNames.add("No results found");
                        }
                        adapter.notifyDataSetChanged();
                        progressBar.setVisibility(View.GONE);
                    });
                } else {
                    int responseCode = conn.getResponseCode();
                    // Provide fallback dummy list if API fails (e.g. invalid key)
                    runOnUiThread(() -> loadDummyList("API Error: " + responseCode));
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> loadDummyList("Error fetching airports"));
            }
        });
    }

    private void loadDummyList(String error) {
        airportNames.clear();
        airportNames.add(error);
        airportNames.add("Delhi (Indira Gandhi Int'l)");
        airportNames.add("Mumbai (Chhatrapati Shivaji Maharaj)");
        airportNames.add("London (Heathrow)");
        airportNames.add("New York (JFK)");
        adapter.notifyDataSetChanged();
        progressBar.setVisibility(View.GONE);
    }
}
