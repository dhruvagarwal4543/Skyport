package com.skyport.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import android.view.View;
import android.view.LayoutInflater;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.skyport.app.R;
import com.skyport.app.models.PaymentMethod;
import com.skyport.app.utils.PricingUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChoosePaymentActivity extends AppCompatActivity {

    private LinearLayout llPaymentOptions;
    private LinearLayout rowAddNewMethod;
    private ImageView   ivSelectedIcon;
    private TextView    tvSelectedMethod;
    private TextView    tvSelectedPrice;

    private List<PaymentMethod> savedMethods = new ArrayList<>();
    private int selectedIndex = 0; // >=0 means saved method
    private String price = "₹ 6,000.00";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_payment);

        price = getIntent().getStringExtra("PRICE") != null
                ? getIntent().getStringExtra("PRICE") : "₹ 6,000.00";

        int basePrice = getIntent().getIntExtra("BASE_PRICE", 0);
        int adultCount = getIntent().getIntExtra("ADULT_COUNT", 1);
        int childCount = getIntent().getIntExtra("CHILD_COUNT", 0);
        int infantCount = getIntent().getIntExtra("INFANT_COUNT", 0);
        int finalTotal = getIntent().getIntExtra("FINAL_TOTAL", 0);

        ((ImageButton) findViewById(R.id.btnBack)).setOnClickListener(v -> finish());

        llPaymentOptions = findViewById(R.id.llPaymentOptions);
        rowAddNewMethod  = findViewById(R.id.rowAddNewMethod);
        ivSelectedIcon   = findViewById(R.id.ivSelectedIcon);
        tvSelectedMethod = findViewById(R.id.tvSelectedMethod);
        tvSelectedPrice  = findViewById(R.id.tvSelectedPrice);

        tvSelectedPrice.setText(price.replace("₹", "").trim()); // Match Image 1 "6000.000"

        rowAddNewMethod.setOnClickListener(v -> showAddMethodBottomSheet());

        loadSavedMethods();

        // Breakdown code removed

        findViewById(R.id.btnGoToPayment).setOnClickListener(v -> {
            if (savedMethods.isEmpty()) {
                Toast.makeText(this, "Please add a payment method first.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedIndex >= 0 && selectedIndex < savedMethods.size()) {
                // Saved method selected -> go straight to confirm
                Intent intent = new Intent(this, ConfirmPaymentActivity.class);
                intent.putExtras(getIntent());
                
                PaymentMethod selected = savedMethods.get(selectedIndex);
                intent.putExtra("METHOD", selected.getType() != null ? selected.getType() : "Credit Card");
                intent.putExtra("PRICE", price);
                intent.putExtra("SAVED_CARD_LAST4", selected.getLast4());
                intent.putExtra("SAVED_CARD_NAME", selected.getName());
                startActivity(intent);
            }
        });
    }

    private void showAddMethodBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_add_payment, null);
        bottomSheetDialog.setContentView(view);

        view.findViewById(R.id.btnOptCredit).setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            launchInputScreen("Credit Card");
        });
        view.findViewById(R.id.btnOptDebit).setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            launchInputScreen("Debit Card");
        });
        view.findViewById(R.id.btnOptUpi).setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            launchInputScreen("UPI");
        });
        view.findViewById(R.id.btnOptAmazon).setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            saveAmazonPayDirectly();
        });

        bottomSheetDialog.show();
    }

    private void launchInputScreen(String method) {
        Intent intent = new Intent(this, InputPaymentActivity.class);
        intent.putExtras(getIntent());
        intent.putExtra("METHOD", method);
        intent.putExtra("PRICE", price);
        startActivity(intent);
    }

    private void saveAmazonPayDirectly() {
        // Route to InputPaymentActivity so the user can opt-in to "Remember Amazon Pay"
        launchInputScreen("Amazon Pay");
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSavedMethods();
    }

    private void loadSavedMethods() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .collection("payment_methods")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    savedMethods.clear();
                    llPaymentOptions.removeAllViews();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        PaymentMethod method = doc.toObject(PaymentMethod.class);
                        savedMethods.add(method);
                    }

                    if (savedMethods.isEmpty()) {
                        // Show "No saved payment methods"
                        TextView tvEmpty = new TextView(this);
                        tvEmpty.setText("No saved payment methods");
                        tvEmpty.setTextColor(getResources().getColor(R.color.skyport_label_gray, null));
                        tvEmpty.setPadding(32, 32, 32, 32);
                        llPaymentOptions.addView(tvEmpty);
                    } else {
                        // Render rows
                        for (int i = 0; i < savedMethods.size(); i++) {
                            PaymentMethod m = savedMethods.get(i);
                            View row = LayoutInflater.from(this).inflate(R.layout.item_payment_method_selectable, llPaymentOptions, false);
                            
                            TextView tvTypeLast4 = row.findViewById(R.id.tvCardTypeLast4);
                            TextView tvName = row.findViewById(R.id.tvCardName);
                            ImageView ivIcon = row.findViewById(R.id.ivCardIcon);
                            RadioButton rb = row.findViewById(R.id.rbSelect);
                            
                            String type = m.getType() != null ? m.getType() : "Credit Card";
                            tvTypeLast4.setText(type + " •••• " + m.getLast4());
                            tvName.setText(m.getName() != null ? m.getName().toUpperCase() : "");
                            
                            if ("Debit Card".equalsIgnoreCase(type)) {
                                ivIcon.setImageResource(R.drawable.ic_debit_card);
                            } else {
                                ivIcon.setImageResource(R.drawable.ic_credit_card);
                            }

                            final int index = i;
                            row.setOnClickListener(v -> selectMethod(index));
                            
                            // Tag the radio button with the index to easily update it later
                            rb.setTag(index);

                            llPaymentOptions.addView(row);
                        }
                    }
                    
                    // Select first saved method if available
                    if (!savedMethods.isEmpty()) {
                        selectMethod(0);
                    } else {
                        // Empty state handling
                        ivSelectedIcon.setImageResource(R.drawable.ic_add);
                        tvSelectedMethod.setText("None");
                        tvSelectedPrice.setText("");
                    }
                });
    }

    private void selectMethod(int index) {
        if (index < 0 || index >= savedMethods.size()) return;
        selectedIndex = index;
        
        // Update dynamically added RadioButtons
        for (int i = 0; i < llPaymentOptions.getChildCount(); i++) {
            View child = llPaymentOptions.getChildAt(i);
            RadioButton rb = child.findViewById(R.id.rbSelect);
            if (rb != null && rb.getTag() instanceof Integer) {
                int rbIndex = (Integer) rb.getTag();
                rb.setChecked(rbIndex == index);
            }
        }

        // Update Summary Card matching Image 1
        PaymentMethod m = savedMethods.get(index);
        String type = m.getType() != null ? m.getType() : "Credit Card";
        tvSelectedMethod.setText(type);
        
        if ("Debit Card".equalsIgnoreCase(type)) {
            ivSelectedIcon.setImageResource(R.drawable.ic_debit_card);
        } else if ("UPI".equalsIgnoreCase(type)) {
            ivSelectedIcon.setImageResource(R.drawable.ic_upi);
        } else if ("Amazon Pay".equalsIgnoreCase(type)) {
            ivSelectedIcon.setImageResource(R.drawable.ic_amazon_pay);
        } else {
            ivSelectedIcon.setImageResource(R.drawable.ic_credit_card);
        }
    }
}
