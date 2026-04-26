package com.skyport.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.skyport.app.R;
import com.skyport.app.adapters.TransactionHistoryAdapter;
import com.skyport.app.models.ShopDataStore;

public class TransactionHistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_history);

        ((ImageButton) findViewById(R.id.btnBack)).setOnClickListener(v -> finish());

        RecyclerView rv = findViewById(R.id.rvHistory);
        rv.setLayoutManager(new LinearLayoutManager(this));
        
        TransactionHistoryAdapter adapter = new TransactionHistoryAdapter(ShopDataStore.getPastOrders(), item -> {
            Intent intent = new Intent(this, TransactionSummaryActivity.class);
            intent.putExtra("ORDER", item);
            startActivity(intent);
        });
        
        rv.setAdapter(adapter);
    }
}
