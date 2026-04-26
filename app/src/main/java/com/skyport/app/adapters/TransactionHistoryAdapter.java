package com.skyport.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.skyport.app.R;
import com.skyport.app.models.ShopDataStore.OrderItem;

import java.util.ArrayList;
import java.util.List;

public class TransactionHistoryAdapter extends RecyclerView.Adapter<TransactionHistoryAdapter.ViewHolder> {

    public interface OnOrderClickListener {
        void onOrderClick(OrderItem item);
    }

    private List<OrderItem> list = new ArrayList<>();
    private OnOrderClickListener clickListener;

    public TransactionHistoryAdapter(List<OrderItem> list, OnOrderClickListener listener) {
        this.list = list;
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderItem o = list.get(position);
        holder.ivImage.setImageResource(o.imageRes);
        holder.tvShop.setText(o.shopName);
        holder.tvItem.setText(o.itemName);
        holder.tvPrice.setText(o.price);
        holder.tvDate.setText(o.date);
        holder.tvStatus.setText(o.status);

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onOrderClick(o);
        });
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvShop, tvItem, tvPrice, tvDate, tvStatus;

        ViewHolder(View v) {
            super(v);
            ivImage = v.findViewById(R.id.ivOrderImage);
            tvShop = v.findViewById(R.id.tvOrderShop);
            tvItem = v.findViewById(R.id.tvOrderItem);
            tvPrice = v.findViewById(R.id.tvOrderPrice);
            tvDate = v.findViewById(R.id.tvOrderDate);
            tvStatus = v.findViewById(R.id.tvOrderStatus);
        }
    }
}
