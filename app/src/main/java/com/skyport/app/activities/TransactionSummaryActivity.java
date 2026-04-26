package com.skyport.app.activities;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.skyport.app.R;
import com.skyport.app.models.ShopDataStore.OrderItem;

public class TransactionSummaryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_summary);

        OrderItem order = (OrderItem) getIntent().getSerializableExtra("ORDER");
        if (order == null) {
            finish();
            return;
        }

        ((ImageButton) findViewById(R.id.btnBack)).setOnClickListener(v -> finish());

        ((ImageView) findViewById(R.id.ivSumImage)).setImageResource(order.imageRes);
        ((TextView) findViewById(R.id.tvSumShop)).setText(order.shopName);
        ((TextView) findViewById(R.id.tvSumStatus)).setText(order.status);
        ((TextView) findViewById(R.id.tvSumDate)).setText(order.date);
        ((TextView) findViewById(R.id.tvSumItem)).setText(order.itemName);
        
        // Simple static parsing for demonstration
        try {
            String rawPrice = order.price.replaceAll("[^0-9]", "");
            int price = Integer.parseInt(rawPrice);
            int tax = (int)(price * 0.12); // 12% tax static demo
            int total = price + tax;
            
            ((TextView) findViewById(R.id.tvSumBase)).setText("₹ " + price);
            ((TextView) findViewById(R.id.tvSumTotal)).setText("₹ " + total);
        } catch (Exception e) {
            ((TextView) findViewById(R.id.tvSumBase)).setText(order.price);
            ((TextView) findViewById(R.id.tvSumTotal)).setText(order.price);
        }

        findViewById(R.id.btnSubmitReview).setOnClickListener(v -> {
            Toast.makeText(this, "Review Submitted", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
