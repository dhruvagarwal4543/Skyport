package com.skyport.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.skyport.app.R;
import com.skyport.app.adapters.ShopAdapter;
import com.skyport.app.models.ShopDataStore;

public class ShopActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        setupRecyclerView(R.id.rvPopularShops, ShopDataStore.getPopularShops());
        setupRecyclerView(R.id.rvRestaurants, ShopDataStore.getRestaurantRecommendations());
        setupRecyclerView(R.id.rvPromotions, ShopDataStore.getSpecialPromotions());

        findViewById(R.id.btnFilter).setOnClickListener(v -> 
            Toast.makeText(this, "Filter logic not implemented", Toast.LENGTH_SHORT).show()
        );

        // Optionally bind transaction history launcher somewhere, maybe "See All Shops"
        findViewById(R.id.btnSeeAllShops).setOnClickListener(v -> {
             startActivity(new Intent(this, TransactionHistoryActivity.class));
        });
        
        findViewById(R.id.btnSeeAllRest).setOnClickListener(v -> {
             startActivity(new Intent(this, TransactionHistoryActivity.class));
        });
    }

    private void setupRecyclerView(int rvId, java.util.List<ShopDataStore.Shop> data) {
        RecyclerView rv = findViewById(rvId);
        rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        ShopAdapter adapter = new ShopAdapter(data, shop -> {
            Intent intent = new Intent(this, ShopDetailActivity.class);
            intent.putExtra("SHOP", shop);
            startActivity(intent);
        });
        rv.setAdapter(adapter);
    }
}
