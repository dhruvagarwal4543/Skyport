package com.skyport.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.skyport.app.R;

import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {

    private final List<BannerItem> items;

    public BannerAdapter(List<BannerItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_banner_slide, parent, false);
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        // Infinite loop mapping
        BannerItem item = items.get(position % items.size());
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        // Return a large number for infinite scroll
        return items.isEmpty() ? 0 : Integer.MAX_VALUE;
    }

    static class BannerViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvQuote;

        BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivBannerImage);
            tvQuote = itemView.findViewById(R.id.tvBannerQuote);
        }

        void bind(BannerItem item) {
            ivImage.setImageResource(item.imageResId);
            if (item.quote == null || item.quote.isEmpty()) {
                tvQuote.setVisibility(View.GONE);
            } else {
                tvQuote.setVisibility(View.VISIBLE);
                tvQuote.setText(item.quote);
            }
        }
    }

    public static class BannerItem {
        public int imageResId;
        public String quote;

        public BannerItem(int imageResId, String quote) {
            this.imageResId = imageResId;
            this.quote = quote;
        }
    }
}
