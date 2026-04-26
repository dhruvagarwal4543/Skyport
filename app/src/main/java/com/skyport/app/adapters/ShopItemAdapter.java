package com.skyport.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.skyport.app.R;
import com.skyport.app.models.ShopDataStore.ShopItem;

import java.util.ArrayList;
import java.util.List;

public class ShopItemAdapter extends RecyclerView.Adapter<ShopItemAdapter.ViewHolder> {

    private List<ShopItem> list = new ArrayList<>();

    public ShopItemAdapter(List<ShopItem> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shop_food, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ShopItem s = list.get(position);
        holder.ivImage.setImageResource(s.imageRes);
        holder.tvName.setText(s.name);
        holder.tvPrice.setText(s.price);

        holder.btnAdd.setOnClickListener(v -> {
            Toast.makeText(v.getContext(), "Added " + s.name, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName, tvPrice;
        ImageButton btnAdd;

        ViewHolder(View v) {
            super(v);
            ivImage = v.findViewById(R.id.ivItemImage);
            tvName = v.findViewById(R.id.tvItemName);
            tvPrice = v.findViewById(R.id.tvItemPrice);
            btnAdd = v.findViewById(R.id.btnAddItem);
        }
    }
}
