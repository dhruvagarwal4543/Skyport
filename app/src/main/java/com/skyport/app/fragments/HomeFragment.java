package com.skyport.app.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.skyport.app.R;
import com.skyport.app.activities.AirportSearchActivity;
import com.skyport.app.activities.FlightListActivity;
import com.skyport.app.activities.MyAccountActivity;
import com.skyport.app.models.SessionManager;

import java.util.Calendar;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private TextView tvFromCity, tvToCity, tvDepartureDate, tvReturnDate, tvTravellers;
    private int travellerCount = 1;

    private boolean isSelectingFrom = true;

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
        
        SessionManager sessionManager = new SessionManager(requireContext());
        String nameFromIntent = null;
        if (getActivity() != null && getActivity().getIntent() != null) {
            nameFromIntent = getActivity().getIntent().getStringExtra("USER_NAME");
        }
        
        String displayName = (nameFromIntent != null && !nameFromIntent.isEmpty()) 
                ? nameFromIntent : sessionManager.getUserName();
                
        // Fallback if both are empty
        if (displayName == null || displayName.isEmpty()) {
            displayName = "Guest";
        }
        
        TextView tvGreeting = view.findViewById(R.id.tvGreeting);
        tvGreeting.setText("Good morning, " + displayName);

        // Tapping avatar/greeting opens My Account
        com.google.android.material.imageview.ShapeableImageView ivAvatar = view.findViewById(R.id.ivAvatar);
        ivAvatar.setOnClickListener(v -> startActivity(new Intent(getActivity(), MyAccountActivity.class)));
        tvGreeting.setOnClickListener(v -> startActivity(new Intent(getActivity(), MyAccountActivity.class)));

        // Bind views
        tvFromCity = view.findViewById(R.id.tvFromCity);
        tvToCity = view.findViewById(R.id.tvToCity);
        tvDepartureDate = view.findViewById(R.id.tvDepartureDate);
        tvReturnDate = view.findViewById(R.id.tvReturnDate);
        tvTravellers = view.findViewById(R.id.tvTravellers);

        LinearLayout llFrom = view.findViewById(R.id.llFrom);
        LinearLayout llTo = view.findViewById(R.id.llTo);
        LinearLayout llDepartureDate = view.findViewById(R.id.llDepartureDate);
        LinearLayout llReturnDate = view.findViewById(R.id.llReturnDate);
        LinearLayout llTraveller = view.findViewById(R.id.llTraveller);

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

        llDepartureDate.setOnClickListener(v -> showDatePicker(tvDepartureDate));
        llReturnDate.setOnClickListener(v -> showDatePicker(tvReturnDate));
        
        TextView btnTravellerMinus = view.findViewById(R.id.btnTravellerMinus);
        TextView btnTravellerPlus = view.findViewById(R.id.btnTravellerPlus);

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
            String toText = tvToCity.getText().toString();
            String dateText = tvDepartureDate.getText().toString();
            String travellersText = tvTravellers.getText().toString();

            String fromIata = fromText;
            String toIata = toText;
            if (fromText.contains("(") && fromText.contains(")")) {
                fromIata = fromText.substring(fromText.indexOf("(") + 1, fromText.indexOf(")"));
            }
            if (toText.contains("(") && toText.contains(")")) {
                toIata = toText.substring(toText.indexOf("(") + 1, toText.indexOf(")"));
            }

            Intent intent = new Intent(getActivity(), FlightListActivity.class);
            intent.putExtra("FROM_NAME", fromText);
            intent.putExtra("FROM_IATA", fromIata);
            intent.putExtra("TO_NAME", toText);
            intent.putExtra("TO_IATA", toIata);
            intent.putExtra("DATE", dateText);
            intent.putExtra("TRAVELLERS", travellersText);
            startActivity(intent);
        });

        return view;
    }

    private void showDatePicker(TextView targetTextView) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, monthOfYear + 1, year1);
                    targetTextView.setText(formattedDate);
                }, year, month, day);
        datePickerDialog.show();
    }
}
