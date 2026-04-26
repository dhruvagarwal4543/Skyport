package com.skyport.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.skyport.app.R;
import com.skyport.app.models.Ticket;

import java.util.ArrayList;
import java.util.List;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {

    public interface OnTicketClickListener {
        void onTicketClick(Ticket ticket);
    }

    private List<Ticket> tickets = new ArrayList<>();
    private final OnTicketClickListener listener;

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
        holder.tvFromCity    .setText(t.fromCity);
        holder.tvFromTime    .setText(t.fromTime);
        holder.tvFromTerminal.setText(t.fromTerminal);
        holder.tvToCity      .setText(t.toCity);
        holder.tvToTime      .setText(t.toTime);
        holder.tvToTerminal  .setText(t.toTerminal);
        holder.itemView.setOnClickListener(v -> listener.onTicketClick(t));
    }

    @Override
    public int getItemCount() { return tickets.size(); }

    static class TicketViewHolder extends RecyclerView.ViewHolder {
        TextView tvFromCity, tvFromTime, tvFromTerminal;
        TextView tvToCity,   tvToTime,   tvToTerminal;

        TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFromCity     = itemView.findViewById(R.id.tvFromCity);
            tvFromTime     = itemView.findViewById(R.id.tvFromTime);
            tvFromTerminal = itemView.findViewById(R.id.tvFromTerminal);
            tvToCity       = itemView.findViewById(R.id.tvToCity);
            tvToTime       = itemView.findViewById(R.id.tvToTime);
            tvToTerminal   = itemView.findViewById(R.id.tvToTerminal);
        }
    }
}
