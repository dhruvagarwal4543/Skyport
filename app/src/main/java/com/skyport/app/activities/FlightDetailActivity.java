package com.skyport.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.skyport.app.R;
import com.skyport.app.utils.PricingUtils;

public class FlightDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flight_detail);

        // Get Intent Extras
        Intent intent = getIntent();
        String airline = intent.getStringExtra("airline");
        String source = intent.getStringExtra("source");
        String destination = intent.getStringExtra("destination");
        String flightNumber = intent.getStringExtra("flight_number");
        String departureTime = intent.getStringExtra("departure_time");
        String arrivalTime = intent.getStringExtra("arrival_time");
        String duration = intent.getStringExtra("duration");
        String price = intent.getStringExtra("price");
        long departureTimeMillis = intent.getLongExtra("departureTimeMillis", 0);
        long arrivalTimeMillis = intent.getLongExtra("arrivalTimeMillis", 0);

        int adultCount = intent.getIntExtra("ADULT_COUNT", 1);
        int childCount = intent.getIntExtra("CHILD_COUNT", 0);
        int infantCount = intent.getIntExtra("INFANT_COUNT", 0);

        // Map UI elements
        ImageButton btnBack = findViewById(R.id.btnBack);
        TextView tvHeaderRoute = findViewById(R.id.tvHeaderRoute);
        ImageView ivDetailLogo = findViewById(R.id.ivDetailLogo);
        TextView tvDetailAirline = findViewById(R.id.tvDetailAirline);
        TextView tvDetailFlightNum = findViewById(R.id.tvDetailFlightNum);
        TextView tvDetailDepTime = findViewById(R.id.tvDetailDepTime);
        TextView tvDetailSource = findViewById(R.id.tvDetailSource);
        TextView tvDetailDuration = findViewById(R.id.tvDetailDuration);
        TextView tvDetailArrTime = findViewById(R.id.tvDetailArrTime);
        TextView tvDetailDest = findViewById(R.id.tvDetailDest);
        TextView tvTotalFare = findViewById(R.id.tvTotalFare);
        Button btnContinue = findViewById(R.id.btnContinue);

        // Set Data
        tvHeaderRoute.setText((source != null ? source : "") + " \u2192 " + (destination != null ? destination : ""));
        
        tvDetailAirline.setText(getAirlineName(airline));
        ivDetailLogo.setImageResource(getLogoForAirline(airline));
        
        tvDetailFlightNum.setText(flightNumber != null ? flightNumber : "");
        tvDetailDepTime.setText(departureTime != null ? departureTime : "--:--");
        tvDetailArrTime.setText(arrivalTime != null ? arrivalTime : "--:--");
        tvDetailSource.setText(source != null ? source : "");
        tvDetailDest.setText(destination != null ? destination : "");
        tvDetailDuration.setText(duration != null ? duration : "--h --m");

        int basePrice = PricingUtils.parsePrice(price);
        int finalTotal = PricingUtils.calculateTotalPayable(basePrice, adultCount, childCount, infantCount);
        tvTotalFare.setText(PricingUtils.formatPrice(finalTotal));

        // Click Listeners
        btnBack.setOnClickListener(v -> finish());

        btnContinue.setOnClickListener(v -> {
            Intent nextIntent = new Intent(FlightDetailActivity.this, ChoosePaymentActivity.class);
            // Forward all details
            nextIntent.putExtra("price", price);
            nextIntent.putExtra("source", source);
            nextIntent.putExtra("destination", destination);
            nextIntent.putExtra("airline", airline);
            nextIntent.putExtra("flight_number", flightNumber);
            nextIntent.putExtra("departure_time", departureTime);
            nextIntent.putExtra("arrival_time", arrivalTime);
            nextIntent.putExtra("duration", duration);
            nextIntent.putExtra("departureTimeMillis", departureTimeMillis);
            nextIntent.putExtra("arrivalTimeMillis", arrivalTimeMillis);
            
            // Forward base price and counts to ChoosePaymentActivity
            nextIntent.putExtra("BASE_PRICE", basePrice);
            nextIntent.putExtra("ADULT_COUNT", adultCount);
            nextIntent.putExtra("CHILD_COUNT", childCount);
            nextIntent.putExtra("INFANT_COUNT", infantCount);
            nextIntent.putExtra("FINAL_TOTAL", finalTotal);
            nextIntent.putExtra("PRICE", PricingUtils.formatPrice(finalTotal)); // formatted
            
            startActivity(nextIntent);
        });
    }

    private String getAirlineName(String code) {
        if (code == null) return "Unknown Airline";
        switch (code.toUpperCase()) {
            case "6E": return "IndiGo";
            case "SG": return "SpiceJet";
            case "AI": return "Air India";
            case "IX": return "Air India Express";
            case "UK": return "Vistara";
            case "QP": return "Akasa Air";
            case "G8": return "GoAir";
            default:   return code;
        }
    }

    private int getLogoForAirline(String code) {
        if (code == null) return R.drawable.ic_plane;
        switch (code.toUpperCase()) {
            case "6E": return R.drawable.ic_indigo;
            case "SG": return R.drawable.img_spicejet;
            case "AI":
            case "IX": return R.drawable.img_airindia;
            default:   return R.drawable.ic_plane;
        }
    }
}
