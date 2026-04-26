package com.skyport.app.fragments;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.skyport.app.R;
import com.skyport.app.activities.AirportSearchActivity;
import com.skyport.app.activities.FlightListActivity;
import com.skyport.app.activities.MyAccountActivity;

import java.util.Calendar;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private TextView tvFromCity, tvToCity, tvDepartureDate, tvReturnDate, tvTravellers;
    private TextView tvGreeting;
    private int travellerCount = 1;
    private boolean isSelectingFrom = true;
    // Separate ISO dates (yyyy-MM-dd) for departure and return.
    // Only departureDateIso is sent to Firebase for filtering.
    private String departureDateIso = null;
    private String returnDateIso    = null;

    private final ActivityResultLauncher<Intent> airportSearchLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    String selectedCity = result.getData().getStringExtra("SELECTED_CITY");
                    if (isSelectingFrom) {
                        tvFromCity.setText(selectedCity);
                    } else {
                        tvToCity.setText(selectedCity);
                    }
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvGreeting = view.findViewById(R.id.tvGreeting);

        // Tapping avatar/greeting opens My Account
        ImageView ivAvatar = view.findViewById(R.id.ivAvatar);
        ivAvatar.setOnClickListener(v -> startActivity(new Intent(getActivity(), MyAccountActivity.class)));
        tvGreeting.setOnClickListener(v -> startActivity(new Intent(getActivity(), MyAccountActivity.class)));

        // Bind views
        tvFromCity      = view.findViewById(R.id.tvFromCity);
        tvToCity        = view.findViewById(R.id.tvToCity);
        tvDepartureDate = view.findViewById(R.id.tvDepartureDate);
        tvReturnDate    = view.findViewById(R.id.tvReturnDate);
        tvTravellers    = view.findViewById(R.id.tvTravellers);

        LinearLayout llFrom          = view.findViewById(R.id.llFrom);
        LinearLayout llTo            = view.findViewById(R.id.llTo);
        LinearLayout llDepartureDate = view.findViewById(R.id.llDepartureDate);
        LinearLayout llReturnDate    = view.findViewById(R.id.llReturnDate);

        llFrom.setOnClickListener(v -> {
            isSelectingFrom = true;
            Intent intent = new Intent(getActivity(), AirportSearchActivity.class);
            intent.putExtra("HINT_TYPE", "From");
            airportSearchLauncher.launch(intent);
        });

        llTo.setOnClickListener(v -> {
            isSelectingFrom = false;
            Intent intent = new Intent(getActivity(), AirportSearchActivity.class);
            intent.putExtra("HINT_TYPE", "To");
            airportSearchLauncher.launch(intent);
        });

        llDepartureDate.setOnClickListener(v -> showDatePicker(tvDepartureDate, true));
        llReturnDate   .setOnClickListener(v -> showDatePicker(tvReturnDate,    false));

        TextView btnTravellerMinus = view.findViewById(R.id.btnTravellerMinus);
        TextView btnTravellerPlus  = view.findViewById(R.id.btnTravellerPlus);

        btnTravellerMinus.setOnClickListener(v -> {
            if (travellerCount > 1) {
                travellerCount--;
                tvTravellers.setText(travellerCount + (travellerCount == 1 ? " Adult" : " Adults"));
            }
        });

        btnTravellerPlus.setOnClickListener(v -> {
            if (travellerCount < 6) {
                travellerCount++;
                tvTravellers.setText(travellerCount + (travellerCount == 1 ? " Adult" : " Adults"));
            }
        });

        Button btnFindFlights = view.findViewById(R.id.btnFindFlights);
        btnFindFlights.setOnClickListener(v -> {
            String fromText = tvFromCity.getText().toString();
            String toText   = tvToCity  .getText().toString();
            String dateText      = tvDepartureDate.getText().toString();
            String travellersText = tvTravellers  .getText().toString();

            String fromIata = fromText;
            String toIata   = toText;
            if (fromText.contains("(") && fromText.contains(")"))
                fromIata = fromText.substring(fromText.indexOf("(") + 1, fromText.indexOf(")"));
            if (toText.contains("(") && toText.contains(")"))
                toIata = toText.substring(toText.indexOf("(") + 1, toText.indexOf(")"));

            Intent intent = new Intent(getActivity(), FlightListActivity.class);
            intent.putExtra("FROM_NAME",  fromText);
            intent.putExtra("FROM_IATA",  fromIata);
            intent.putExtra("TO_NAME",    toText);
            intent.putExtra("TO_IATA",    toIata);
            intent.putExtra("DATE",         departureDateIso != null ? departureDateIso : "");
            intent.putExtra("DATE_DISPLAY",  dateText);
            intent.putExtra("TRAVELLERS", travellersText);
            startActivity(intent);
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh greeting every time user returns to the home tab
        loadGreeting();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Greeting helpers
    // ─────────────────────────────────────────────────────────────────────────

    private void loadGreeting() {
        // Show time-based prefix immediately with a placeholder name
        updateGreeting(getTimedGreeting("…"));

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            updateGreeting(getTimedGreeting("Guest"));
            return;
        }

        // Try Firestore first (most up-to-date after Edit Profile)
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    String name = null;
                    if (doc.exists()) name = doc.getString("name");
                    if (name == null || name.isEmpty()) name = user.getDisplayName();
                    if (name == null || name.isEmpty()) name = "there";
                    updateGreeting(getTimedGreeting(name));
                })
                .addOnFailureListener(e -> {
                    // Fall back to Auth display name
                    String name = user.getDisplayName();
                    updateGreeting(getTimedGreeting(name != null && !name.isEmpty() ? name : "there"));
                });
    }

    private void updateGreeting(String text) {
        if (tvGreeting != null) tvGreeting.setText(text);
    }

    /** Returns "Good Morning/Afternoon/Evening, <name>" based on device clock */
    private String getTimedGreeting(String name) {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String prefix;
        if (hour < 12)      prefix = "Good Morning";
        else if (hour < 18) prefix = "Good Afternoon";
        else                prefix = "Good Evening";
        return prefix + ", " + name;
    }

    // ─────────────────────────────────────────────────────────────────────────

    /**
     * @param isDeparture true  → writes to departureDateIso (sent to Firebase)
     *                    false → writes to returnDateIso (display only)
     */
    private void showDatePicker(TextView targetTextView, boolean isDeparture) {
        final Calendar c = Calendar.getInstance();
        int year  = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day   = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(requireContext(),
                (view2, year1, monthOfYear, dayOfMonth) -> {
                    // Display-friendly format shown in the UI  (e.g. "25 Apr 2026")
                    String displayDate = String.format(Locale.getDefault(),
                            "%02d %s %d",
                            dayOfMonth,
                            new java.text.DateFormatSymbols().getShortMonths()[monthOfYear],
                            year1);
                    targetTextView.setText(displayDate);

                    // ISO format for Firebase queries (yyyy-MM-dd)
                    String isoDate = String.format(Locale.US,
                            "%04d-%02d-%02d", year1, monthOfYear + 1, dayOfMonth);

                    if (isDeparture) {
                        departureDateIso = isoDate;
                        android.util.Log.d("DEBUG", "Departure ISO date set: " + departureDateIso);
                    } else {
                        returnDateIso = isoDate;
                    }
                }, year, month, day);
        dialog.show();
    }
}
