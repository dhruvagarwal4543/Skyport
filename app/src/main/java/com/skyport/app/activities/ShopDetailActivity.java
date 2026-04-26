package com.skyport.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.skyport.app.R;
import com.skyport.app.adapters.ShopItemAdapter;
import com.skyport.app.models.ShopDataStore;
import com.skyport.app.models.ShopDataStore.Shop;

public class ShopDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_detail);

        Shop shop = (Shop) getIntent().getSerializableExtra("SHOP");
        if (shop == null) {
            finish();
            return;
        }

        ((ImageButton) findViewById(R.id.btnBack)).setOnClickListener(v -> finish());

        ((ImageView) findViewById(R.id.ivDetailBanner)).setImageResource(shop.imageRes);
        ((TextView) findViewById(R.id.tvDetailName)).setText(shop.name);
        ((TextView) findViewById(R.id.tvDetailLocation)).setText(shop.location);
        ((TextView) findViewById(R.id.tvDetailRating)).setText(shop.rating);

        RecyclerView rv = findViewById(R.id.rvItems);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new ShopItemAdapter(ShopDataStore.getShopItems(shop.id)));

        findViewById(R.id.btnCheckout).setOnClickListener(v -> {
            startActivity(new Intent(this, OrderConfirmationActivity.class));
            finish();
        });
    }
}
