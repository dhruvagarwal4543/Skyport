package com.skyport.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.skyport.app.R;
import com.skyport.app.models.Flight;

import java.util.List;

public class FlightAdapter extends RecyclerView.Adapter<FlightAdapter.FlightViewHolder> {

    private List<Flight> flightList;

    public FlightAdapter(List<Flight> flightList) {
        this.flightList = flightList;
    }

    @NonNull
    @Override
    public FlightViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_flight, parent, false);
        return new FlightViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FlightViewHolder holder, int position) {
        Flight flight = flightList.get(position);
        holder.tvFlightName.setText(flight.getAirlineName());
        holder.tvRoute.setText(flight.getRoute());
        holder.tvTime.setText(flight.getTime());
    }

    @Override
    public int getItemCount() {
        return flightList != null ? flightList.size() : 0;
    }

    public static class FlightViewHolder extends RecyclerView.ViewHolder {
        TextView tvFlightName, tvRoute, tvTime;

        public FlightViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFlightName = itemView.findViewById(R.id.tvFlightName);
            tvRoute = itemView.findViewById(R.id.tvRoute);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}
