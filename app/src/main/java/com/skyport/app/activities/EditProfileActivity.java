package com.skyport.app.activities;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.skyport.app.R;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        etFullName = findViewById(R.id.etFullName);
        etEmail    = findViewById(R.id.etEmail);
        etPhone    = findViewById(R.id.etPhone);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        loadCurrentData();

        findViewById(R.id.btnUpdateProfile).setOnClickListener(v -> updateProfile());
    }

    private void loadCurrentData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        if (user.getEmail() != null) etEmail.setText(user.getEmail());
        if (user.getDisplayName() != null) etFullName.setText(user.getDisplayName());

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name  = doc.getString("name");
                        String phone = doc.getString("phone");
                        if (name  != null) etFullName.setText(name);
                        if (phone != null) etPhone.setText(phone);
                    }
                });
    }

    private void updateProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String name  = etFullName.getText().toString().trim();
        String phone = etPhone   .getText().toString().trim();

        if (name.isEmpty()) {
            etFullName.setError("Name is required");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("name",  name);
        updates.put("phone", phone);

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .update(updates)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();
                    finish(); // go back to My Account which will onResume → reload
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
