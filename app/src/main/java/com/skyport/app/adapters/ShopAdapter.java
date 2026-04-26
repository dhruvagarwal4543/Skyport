package com.skyport.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.skyport.app.R;
import com.skyport.app.models.ShopDataStore.Shop;

import java.util.ArrayList;
import java.util.List;

public class ShopAdapter extends RecyclerView.Adapter<ShopAdapter.ViewHolder> {

    public interface OnShopClickListener {
        void onShopClick(Shop shop);
    }

    private List<Shop> list = new ArrayList<>();
    private OnShopClickListener clickListener;

    public ShopAdapter(List<Shop> list, OnShopClickListener clickListener) {
        this.list = list;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shop_card, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Shop s = list.get(position);
        holder.ivImage.setImageResource(s.imageRes);
        holder.tvName.setText(s.name);
        holder.tvLocation.setText(s.location);
        holder.tvRating.setText(s.rating);

        if (s.discount != null && !s.discount.isEmpty()) {
            holder.llDiscount.setVisibility(View.VISIBLE);
            holder.tvDiscount.setText(s.discount);
        } else {
            holder.llDiscount.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onShopClick(s);
        });
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName, tvLocation, tvRating, tvDiscount;
        LinearLayout llDiscount;

        ViewHolder(View v) {
            super(v);
            ivImage = v.findViewById(R.id.ivShopImage);
            tvName = v.findViewById(R.id.tvShopName);
            tvLocation = v.findViewById(R.id.tvShopLocation);
            tvRating = v.findViewById(R.id.tvShopRating);
            tvDiscount = v.findViewById(R.id.tvDiscount);
            llDiscount = v.findViewById(R.id.llDiscountBadge);
        }
    }
}
