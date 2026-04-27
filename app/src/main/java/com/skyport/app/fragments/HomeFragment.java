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
import android.os.Handler;
import android.os.Looper;

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
import com.skyport.app.adapters.BannerAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private TextView tvFromCity, tvToCity, tvDepartureDate, tvReturnDate, tvTravellers;
    private TextView tvGreeting;
    private int adultCount = 1;
    private int childCount = 0;
    private int infantCount = 0;
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

        // Setup ViewPager2
        ViewPager2 vpBanner = view.findViewById(R.id.vpBanner);
        setupBannerSlideshow(vpBanner);

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

        LinearLayout llTraveller = view.findViewById(R.id.llTraveller);
        llTraveller.setOnClickListener(v -> showTravellerBottomSheet());

        Button btnFindFlights = view.findViewById(R.id.btnFindFlights);
        btnFindFlights.setOnClickListener(v -> {
            String fromText = tvFromCity.getText().toString();
            String toText   = tvToCity  .getText().toString();
            String dateText      = tvDepartureDate.getText().toString();
            String travellersText = tvTravellers  .getText().toString();

            if (fromText.equals("Select Origin") || toText.equals("Select Destination") ||
                dateText.equals("Select Date") || travellersText.equals("Select Travellers")) {
                android.widget.Toast.makeText(getActivity(), "Please select all required fields", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }

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
            intent.putExtra("ADULT_COUNT", adultCount);
            intent.putExtra("CHILD_COUNT", childCount);
            intent.putExtra("INFANT_COUNT", infantCount);
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

    private Handler sliderHandler = new Handler(Looper.getMainLooper());
    private Runnable sliderRunnable = null;

    private void setupBannerSlideshow(ViewPager2 vpBanner) {
        List<BannerAdapter.BannerItem> items = new ArrayList<>();
        // Keep existing image
        items.add(new BannerAdapter.BannerItem(R.drawable.bg_travel_banner, "All You Need to Know About Travel During the Pandemic"));
        // New images
        items.add(new BannerAdapter.BannerItem(R.drawable.bg_planning_holiday, "Plan your perfect getaway with ease"));
        items.add(new BannerAdapter.BannerItem(R.drawable.bg_visa_planning, "Get your travel documents ready, stress-free"));
        items.add(new BannerAdapter.BannerItem(R.drawable.bg_flight_safety, "Your safety is our top priority, always"));

        BannerAdapter adapter = new BannerAdapter(items);
        vpBanner.setAdapter(adapter);

        // Start in the middle to allow scrolling left immediately
        vpBanner.setCurrentItem(1000 * items.size(), false);

        sliderRunnable = new Runnable() {
            @Override
            public void run() {
                vpBanner.setCurrentItem(vpBanner.getCurrentItem() + 1, true);
                sliderHandler.postDelayed(sliderRunnable, 3500);
            }
        };

        vpBanner.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable, 3500);
            }
        });
    }

    private void showTravellerBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View sheetView = LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet_travellers, null);
        dialog.setContentView(sheetView);

        ImageView btnAdultMinus = sheetView.findViewById(R.id.btnAdultMinus);
        ImageView btnAdultPlus = sheetView.findViewById(R.id.btnAdultPlus);
        TextView tvAdultCount = sheetView.findViewById(R.id.tvAdultCount);

        ImageView btnChildMinus = sheetView.findViewById(R.id.btnChildMinus);
        ImageView btnChildPlus = sheetView.findViewById(R.id.btnChildPlus);
        TextView tvChildCount = sheetView.findViewById(R.id.tvChildCount);

        ImageView btnInfantMinus = sheetView.findViewById(R.id.btnInfantMinus);
        ImageView btnInfantPlus = sheetView.findViewById(R.id.btnInfantPlus);
        TextView tvInfantCount = sheetView.findViewById(R.id.tvInfantCount);

        Button btnConfirm = sheetView.findViewById(R.id.btnConfirmTravellers);

        int[] counts = new int[]{adultCount, childCount, infantCount};

        Runnable updateUi = () -> {
            tvAdultCount.setText(String.valueOf(counts[0]));
            tvChildCount.setText(String.valueOf(counts[1]));
            tvInfantCount.setText(String.valueOf(counts[2]));

            int total = counts[0] + counts[1] + counts[2];

            btnAdultMinus.setAlpha(counts[0] <= 1 ? 0.5f : 1.0f);
            btnAdultMinus.setEnabled(counts[0] > 1);
            btnAdultPlus.setAlpha(total >= 6 ? 0.5f : 1.0f);
            btnAdultPlus.setEnabled(total < 6);

            btnChildMinus.setAlpha(counts[1] <= 0 ? 0.5f : 1.0f);
            btnChildMinus.setEnabled(counts[1] > 0);
            btnChildPlus.setAlpha(total >= 6 ? 0.5f : 1.0f);
            btnChildPlus.setEnabled(total < 6);

            btnInfantMinus.setAlpha(counts[2] <= 0 ? 0.5f : 1.0f);
            btnInfantMinus.setEnabled(counts[2] > 0);
            btnInfantPlus.setAlpha(total >= 6 ? 0.5f : 1.0f);
            btnInfantPlus.setEnabled(total < 6);
        };
        updateUi.run();

        btnAdultMinus.setOnClickListener(v -> { if (counts[0] > 1) { counts[0]--; updateUi.run(); } });
        btnAdultPlus.setOnClickListener(v -> { if (counts[0] + counts[1] + counts[2] < 6) { counts[0]++; updateUi.run(); } });

        btnChildMinus.setOnClickListener(v -> { if (counts[1] > 0) { counts[1]--; updateUi.run(); } });
        btnChildPlus.setOnClickListener(v -> { if (counts[0] + counts[1] + counts[2] < 6) { counts[1]++; updateUi.run(); } });

        btnInfantMinus.setOnClickListener(v -> { if (counts[2] > 0) { counts[2]--; updateUi.run(); } });
        btnInfantPlus.setOnClickListener(v -> { if (counts[0] + counts[1] + counts[2] < 6) { counts[2]++; updateUi.run(); } });

        btnConfirm.setOnClickListener(v -> {
            adultCount = counts[0];
            childCount = counts[1];
            infantCount = counts[2];
            int totalCount = adultCount + childCount + infantCount;
            tvTravellers.setText(totalCount + (totalCount == 1 ? " Traveller" : " Travellers"));
            dialog.dismiss();
        });

        dialog.show();
    }
}
