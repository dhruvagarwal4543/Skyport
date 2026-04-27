package com.skyport.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.skyport.app.R;
import com.skyport.app.models.SessionManager;

public class MyAccountActivity extends AppCompatActivity {

    private TextView tvProfileName, tvProfileEmail;
    private ImageView ivProfileAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account);

        tvProfileName  = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        ivProfileAvatar = findViewById(R.id.ivProfileAvatar);

        // ── Edit Profile ───────────────────────────────────────────────────
        ImageButton btnEdit = findViewById(R.id.btnEditProfile);
        btnEdit.setOnClickListener(v ->
                startActivity(new Intent(this, EditProfileActivity.class)));

        // ── My Activity rows ──────────────────────────────────────────────
        setupRow(R.id.rowNotifications,  R.drawable.ic_notification, "View Notifications",      null);
        setupRow(R.id.rowTransactions,   R.drawable.ic_nav_doc,      "View Transaction History", null);
        setupRow(R.id.rowPaymentMethods, R.drawable.ic_nav_suitcase, "Payment Methods",          () -> startActivity(new Intent(this, PaymentMethodsActivity.class)));

        // ── Other Information ─────────────────────────────────────────────
        setupRow(R.id.rowHelp,   R.drawable.ic_nav_doc,  "Help",     () -> startActivity(new Intent(this, HelpActivity.class)));
        setupRow(R.id.rowLogout, R.drawable.ic_nav_home, "Log out",  this::performLogout);

        loadUserData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh name/email in case user just updated profile
        loadUserData();
    }

    private void loadUserData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        // Immediate fill from FirebaseAuth
        if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
            tvProfileName.setText(user.getDisplayName());
        }
        if (user.getEmail() != null) {
            tvProfileEmail.setText(user.getEmail());
        }

        // Enrich with Firestore data (name / phone saved during signup)
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name  = doc.getString("name");
                        String email = doc.getString("email");
                        if (name  != null && !name.isEmpty())  tvProfileName .setText(name);
                        if (email != null && !email.isEmpty()) tvProfileEmail.setText(email);
                    }
                });
    }

    private void setupRow(int rowId, int iconRes, String label, Runnable onClick) {
        View row = findViewById(rowId);
        if (row == null) return;
        ((ImageView) row.findViewById(R.id.ivRowIcon)).setImageResource(iconRes);
        ((TextView)  row.findViewById(R.id.tvRowLabel)).setText(label);
        if (onClick != null) {
            row.setOnClickListener(v -> onClick.run());
        } else {
            row.setOnClickListener(v ->
                    Toast.makeText(this, label + " — coming soon", Toast.LENGTH_SHORT).show());
        }
    }

    private void performLogout() {
        new SessionManager(this).clearSession();
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, AuthActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
