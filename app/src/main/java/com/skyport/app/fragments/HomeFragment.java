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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.skyport.app.R;
import com.skyport.app.activities.AirportSearchActivity;
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
            airportSearchLauncher.launch(new Intent(getActivity(), AirportSearchActivity.class));
        });

        llTo.setOnClickListener(v -> {
            isSelectingFrom = false;
            airportSearchLauncher.launch(new Intent(getActivity(), AirportSearchActivity.class));
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
