package com.skyport.app.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.skyport.app.R;
import com.skyport.app.models.PaymentMethod;

import java.util.ArrayList;
import java.util.List;

public class PaymentMethodsActivity extends AppCompatActivity {

    private RecyclerView rvPaymentMethods;
    private ProgressBar progressBar;
    private TextView tvNoMethods;
    private PaymentMethodAdapter adapter;
    private List<PaymentMethod> methodList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_methods);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        rvPaymentMethods = findViewById(R.id.rvPaymentMethods);
        progressBar = findViewById(R.id.progressBar);
        tvNoMethods = findViewById(R.id.tvNoMethods);

        rvPaymentMethods.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PaymentMethodAdapter(methodList, this::showDeleteDialog);
        rvPaymentMethods.setAdapter(adapter);

        Button btnAddMethod = findViewById(R.id.btnAddMethod);
        btnAddMethod.setOnClickListener(v -> showAddMethodBottomSheet());

        loadPaymentMethods();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPaymentMethods();
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
            launchInputScreen("Amazon Pay");
        });

        bottomSheetDialog.show();
    }

    private void launchInputScreen(String method) {
        Intent intent = new Intent(this, InputPaymentActivity.class);
        intent.putExtra("METHOD", method);
        startActivity(intent);
    }

    // Amazon Pay is now handled via InputPaymentActivity with opt-in "Remember" toggle

    private void showDeleteDialog(PaymentMethod method) {
        new AlertDialog.Builder(this)
                .setTitle("Remove Payment Method")
                .setMessage("Are you sure you want to remove this " + method.getType() + "?")
                .setPositiveButton("Remove", (dialog, which) -> deleteMethod(method))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteMethod(PaymentMethod method) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || method.getId() == null) return;

        progressBar.setVisibility(View.VISIBLE);
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .collection("payment_methods")
                .document(method.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Payment method removed", Toast.LENGTH_SHORT).show();
                    loadPaymentMethods();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to remove method", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadPaymentMethods() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        progressBar.setVisibility(View.VISIBLE);
        tvNoMethods.setVisibility(View.GONE);

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .collection("payment_methods")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(View.GONE);
                    methodList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        PaymentMethod method = doc.toObject(PaymentMethod.class);
                        method.setId(doc.getId());
                        methodList.add(method);
                    }

                    if (methodList.isEmpty()) {
                        tvNoMethods.setVisibility(View.VISIBLE);
                        rvPaymentMethods.setVisibility(View.GONE);
                    } else {
                        tvNoMethods.setVisibility(View.GONE);
                        rvPaymentMethods.setVisibility(View.VISIBLE);
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to load payment methods", Toast.LENGTH_SHORT).show();
                });
    }

    private static class PaymentMethodAdapter extends RecyclerView.Adapter<PaymentMethodAdapter.ViewHolder> {
        private List<PaymentMethod> list;
        private OnMethodClickListener listener;

        public interface OnMethodClickListener {
            void onClick(PaymentMethod method);
        }

        public PaymentMethodAdapter(List<PaymentMethod> list, OnMethodClickListener listener) {
            this.list = list;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_payment_method, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            PaymentMethod method = list.get(position);
            String type = method.getType() != null ? method.getType() : "Credit Card";
            
            holder.tvCardType.setText(type);
            holder.tvCardLast4.setText("*** " + method.getLast4());

            if ("Debit Card".equalsIgnoreCase(type)) {
                holder.ivCardIcon.setImageResource(R.drawable.ic_debit_card);
            } else if ("UPI".equalsIgnoreCase(type)) {
                holder.ivCardIcon.setImageResource(R.drawable.ic_upi);
            } else if ("Amazon Pay".equalsIgnoreCase(type)) {
                holder.ivCardIcon.setImageResource(R.drawable.ic_amazon_pay);
            } else {
                holder.ivCardIcon.setImageResource(R.drawable.ic_credit_card);
            }

            holder.itemView.setOnClickListener(v -> listener.onClick(method));
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivCardIcon;
            TextView tvCardType, tvCardLast4;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                ivCardIcon = itemView.findViewById(R.id.ivCardIcon);
                tvCardType = itemView.findViewById(R.id.tvCardType);
                tvCardLast4 = itemView.findViewById(R.id.tvCardLast4);
            }
        }
    }
}
