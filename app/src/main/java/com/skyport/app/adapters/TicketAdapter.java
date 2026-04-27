package com.skyport.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.skyport.app.R;
import com.skyport.app.models.Ticket;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {

    public interface OnTicketClickListener {
        void onTicketClick(Ticket ticket);
    }

    private List<Ticket> tickets = new ArrayList<>();
    private final OnTicketClickListener listener;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM", Locale.getDefault());

    public TicketAdapter(OnTicketClickListener listener) {
        this.listener = listener;
    }

    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ticket_card, parent, false);
        return new TicketViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        Ticket t = tickets.get(position);

        holder.tvFromCity.setText(t.fromCode != null ? t.fromCode : "");
        holder.tvToCity.setText(t.toCode != null ? t.toCode : "");
        holder.tvAirlineName.setText(getAirlineName(t.airline));
        holder.ivAirlineLogo.setImageResource(getLogoForAirline(t.airline));
        holder.tvFlightNumber.setText(t.flightNumber != null ? t.flightNumber : "");
        holder.tvDuration.setText(t.duration != null ? t.duration : "");

        if (t.departureTime != null) {
            Date depDate = t.departureTime.toDate();
            holder.tvFromTime.setText(timeFormat.format(depDate));
            holder.tvFromDate.setText(dateFormat.format(depDate));
        }

        if (t.arrivalTime != null) {
            Date arrDate = t.arrivalTime.toDate();
            holder.tvToTime.setText(timeFormat.format(arrDate));
            holder.tvToDate.setText(dateFormat.format(arrDate));
        }

        holder.itemView.setOnClickListener(v -> listener.onTicketClick(t));
    }

    @Override
    public int getItemCount() { return tickets.size(); }

    static class TicketViewHolder extends RecyclerView.ViewHolder {
        TextView tvAirlineName, tvFlightNumber;
        ImageView ivAirlineLogo;
        TextView tvFromCity, tvFromTime, tvFromDate;
        TextView tvToCity, tvToTime, tvToDate;
        TextView tvDuration;

        TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAirlineName  = itemView.findViewById(R.id.tvAirlineName);
            tvFlightNumber = itemView.findViewById(R.id.tvFlightNumber);
            ivAirlineLogo  = itemView.findViewById(R.id.ivAirlineLogo);
            tvFromCity     = itemView.findViewById(R.id.tvFromCity);
            tvFromTime     = itemView.findViewById(R.id.tvFromTime);
            tvFromDate     = itemView.findViewById(R.id.tvFromDate);
            tvToCity       = itemView.findViewById(R.id.tvToCity);
            tvToTime       = itemView.findViewById(R.id.tvToTime);
            tvToDate       = itemView.findViewById(R.id.tvToDate);
            tvDuration     = itemView.findViewById(R.id.tvDuration);
        }
    }

    private String getAirlineName(String code) {
        if (code == null) return "Unknown";
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
