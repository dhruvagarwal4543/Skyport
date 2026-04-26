package com.skyport.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.skyport.app.R;
import com.skyport.app.models.Flight;

import java.util.ArrayList;
import java.util.List;

public class FlightAdapter extends RecyclerView.Adapter<FlightAdapter.FlightViewHolder> {

    public interface OnFlightClickListener {
        void onFlightClick(Flight flight);
    }

    private List<Flight> flights = new ArrayList<>();
    private OnFlightClickListener clickListener;

    public void setOnFlightClickListener(OnFlightClickListener listener) {
        this.clickListener = listener;
    }

    public void setFlights(List<Flight> flights) {
        this.flights = flights;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FlightViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_flight_card, parent, false);
        return new FlightViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FlightViewHolder holder, int position) {
        Flight flight = flights.get(position);

        // Prefer airline_name directly from Firebase; fall back to code-to-name map
        String airlineCode = flight.getAirline();
        String airlineName = flight.getAirline_name();
        if (airlineName == null || airlineName.isEmpty()) {
            airlineName = getAirlineName(airlineCode);
        }
        holder.tvAirlineName.setText(airlineName);
        holder.ivAirlineLogo.setImageResource(getLogoForAirline(airlineCode));

        holder.tvDepTime.setText(flight.getDeparture_time() != null ? flight.getDeparture_time() : "--:--");
        holder.tvSource.setText(flight.getSource());
        holder.tvArrTime.setText(flight.getArrival_time() != null ? flight.getArrival_time() : "--:--");
        holder.tvDestination.setText(flight.getDestination());
        holder.tvDuration.setText(flight.getDuration() != null ? flight.getDuration() : "");

        String flightNum = flight.getFlight_number();
        holder.tvFlightNumber.setText(flightNum != null ? flightNum : "");

        String price = flight.getPrice();
        if (price == null || price.isEmpty()) price = "₹ -";
        else if (!price.contains("₹")) price = "₹ " + price;
        holder.tvPrice.setText(price);

        // Tap entire card → booking flow
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onFlightClick(flight);
        });
    }


    @Override
    public int getItemCount() {
        return flights.size();
    }

    private String getAirlineName(String code) {
        if (code == null) return "Unknown Airline";
        switch (code.toUpperCase()) {
            case "6E": return "IndiGo";
            case "SG": return "SpiceJet";
            case "AI": return "Air India";
            case "IX": return "Air India Express";
            case "UK": return "Vistara";
            case "G8": return "GoAir";
            default:   return code;  // fallback: show code as-is
        }
    }

    private int getLogoForAirline(String code) {
        if (code == null) return R.drawable.ic_plane;
        switch (code.toUpperCase()) {
            case "6E": return R.drawable.ic_indigo;
            case "SG": return R.drawable.ic_spicejet;
            case "AI":
            case "IX": return R.drawable.ic_airindia;
            default:   return R.drawable.ic_plane;
        }
    }

    static class FlightViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAirlineLogo;
        TextView tvAirlineName, tvFlightNumber, tvDepTime, tvSource,
                 tvDuration, tvArrTime, tvDestination, tvPrice;

        public FlightViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAirlineLogo   = itemView.findViewById(R.id.ivAirlineLogo);
            tvAirlineName   = itemView.findViewById(R.id.tvAirlineName);
            tvFlightNumber  = itemView.findViewById(R.id.tvFlightNumber);
            tvDepTime       = itemView.findViewById(R.id.tvDepTime);
            tvSource        = itemView.findViewById(R.id.tvSource);
            tvDuration      = itemView.findViewById(R.id.tvDuration);
            tvArrTime       = itemView.findViewById(R.id.tvArrTime);
            tvDestination   = itemView.findViewById(R.id.tvDestination);
            tvPrice         = itemView.findViewById(R.id.tvPrice);
        }
    }
}
