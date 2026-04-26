package com.skyport.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.skyport.app.R;

public class ChoosePaymentActivity extends AppCompatActivity {

    // 0=Credit, 1=Debit, 2=UPI, 3=Amazon
    private int selectedMethod = 0;

    private RadioButton rbCredit, rbDebit, rbUpi, rbAmazon;
    private ImageView   ivSelectedIcon;
    private TextView    tvSelectedMethod;

    private String price = "₹ 6,000.00";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_payment);

        price = getIntent().getStringExtra("PRICE") != null
                ? getIntent().getStringExtra("PRICE") : "₹ 6,000.00";

        ((ImageButton) findViewById(R.id.btnBack)).setOnClickListener(v -> finish());

        rbCredit   = findViewById(R.id.rbCreditCard);
        rbDebit    = findViewById(R.id.rbDebitCard);
        rbUpi      = findViewById(R.id.rbUpi);
        rbAmazon   = findViewById(R.id.rbAmazonPay);
        ivSelectedIcon  = findViewById(R.id.ivSelectedIcon);
        tvSelectedMethod = findViewById(R.id.tvSelectedMethod);

        ((TextView) findViewById(R.id.tvSelectedPrice)).setText(price);

        // Row click listeners
        LinearLayout rowCredit = findViewById(R.id.rowCreditCard);
        LinearLayout rowDebit  = findViewById(R.id.rowDebitCard);
        LinearLayout rowUpi    = findViewById(R.id.rowUpi);
        LinearLayout rowAmazon = findViewById(R.id.rowAmazonPay);

        rowCredit.setOnClickListener(v -> select(0));
        rowDebit .setOnClickListener(v -> select(1));
        rowUpi   .setOnClickListener(v -> select(2));
        rowAmazon.setOnClickListener(v -> select(3));

        // Initially highlight Credit Card
        select(0);

        findViewById(R.id.btnGoToPayment).setOnClickListener(v -> {
            if (selectedMethod == 3) {
                // Amazon Pay → skip to confirm
                Intent intent = new Intent(this, ConfirmPaymentActivity.class);
                intent.putExtra("METHOD", "Amazon Pay");
                intent.putExtra("PRICE", price);
                startActivity(intent);
            } else {
                Intent intent = new Intent(this, InputPaymentActivity.class);
                intent.putExtra("METHOD", getMethodName());
                intent.putExtra("METHOD_ICON", getMethodIcon());
                intent.putExtra("PRICE", price);
                startActivity(intent);
            }
        });
    }

    private void select(int method) {
        selectedMethod = method;
        rbCredit.setChecked(method == 0);
        rbDebit .setChecked(method == 1);
        rbUpi   .setChecked(method == 2);
        rbAmazon.setChecked(method == 3);

        ivSelectedIcon .setImageResource(getMethodIcon());
        tvSelectedMethod.setText(getMethodName());
    }

    private String getMethodName() {
        switch (selectedMethod) {
            case 0: return "Credit Card";
            case 1: return "Debit Card";
            case 2: return "UPI";
            case 3: return "Amazon Pay";
            default: return "Credit Card";
        }
    }

    private int getMethodIcon() {
        switch (selectedMethod) {
            case 0: return R.drawable.ic_credit_card;
            case 1: return R.drawable.ic_debit_card;
            case 2: return R.drawable.ic_upi;
            case 3: return R.drawable.ic_amazon_pay;
            default: return R.drawable.ic_credit_card;
        }
    }
}
