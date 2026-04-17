package com.skyport.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.skyport.app.R;

public class SignupActivity extends AppCompatActivity {

    private View layoutStep1, layoutStep2;
    private Button btnAction;
    private TextView tvSignupSubtitle;
    private ImageView ivStep1, ivStep2;
    private int currentStep = 1;

    // Step 1 Fields
    private EditText etFirstName, etLastName, etEmail, etPhone, etPassword, etConfirmPassword;
    
    // Step 2 Fields
    private EditText etCountry, etNationality, etPassport;
    private CheckBox cbTerms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        initViews();
        setupListeners();
    }

    private void initViews() {
        layoutStep1 = findViewById(R.id.layoutStep1);
        layoutStep2 = findViewById(R.id.layoutStep2);
        btnAction = findViewById(R.id.btnAction);
        tvSignupSubtitle = findViewById(R.id.tvSignupSubtitle);
        ivStep1 = findViewById(R.id.ivStep1);
        ivStep2 = findViewById(R.id.ivStep2);

        // Step 1
        etFirstName = layoutStep1.findViewById(R.id.etFirstName);
        etLastName = layoutStep1.findViewById(R.id.etLastName);
        etEmail = layoutStep1.findViewById(R.id.etEmail);
        etPhone = layoutStep1.findViewById(R.id.etPhone);
        etPassword = layoutStep1.findViewById(R.id.etPassword);
        etConfirmPassword = layoutStep1.findViewById(R.id.etConfirmPassword);

        // Step 2
        etCountry = layoutStep2.findViewById(R.id.etCountry);
        etNationality = layoutStep2.findViewById(R.id.etNationality);
        etPassport = layoutStep2.findViewById(R.id.etPassport);
        cbTerms = layoutStep2.findViewById(R.id.cbTerms);
    }

    private void setupListeners() {
        btnAction.setOnClickListener(v -> {
            if (currentStep == 1) {
                if (validateStep1()) {
                    showStep2();
                }
            } else {
                if (validateStep2()) {
                    performSignup();
                }
            }
        });
    }

    private boolean validateStep1() {
        if (TextUtils.isEmpty(etFirstName.getText())) {
            showError("First Name is required");
            return false;
        }
        if (TextUtils.isEmpty(etLastName.getText())) {
            showError("Last Name is required");
            return false;
        }
        if (TextUtils.isEmpty(etEmail.getText()) || !android.util.Patterns.EMAIL_ADDRESS.matcher(etEmail.getText()).matches()) {
            showError("Valid Email is required");
            return false;
        }
        if (TextUtils.isEmpty(etPassword.getText()) || etPassword.getText().length() < 6) {
            showError("Password must be at least 6 characters");
            return false;
        }
        if (!etPassword.getText().toString().equals(etConfirmPassword.getText().toString())) {
            showError("Passwords do not match");
            return false;
        }
        return true;
    }

    private boolean validateStep2() {
        if (TextUtils.isEmpty(etCountry.getText())) {
            showError("Country is required");
            return false;
        }
        if (TextUtils.isEmpty(etNationality.getText())) {
            showError("Nationality is required");
            return false;
        }
        if (TextUtils.isEmpty(etPassport.getText())) {
            showError("Passport ID is required");
            return false;
        }
        if (!cbTerms.isChecked()) {
            showError("You must agree to the terms");
            return false;
        }
        return true;
    }

    private void showStep2() {
        currentStep = 2;
        layoutStep1.setVisibility(View.GONE);
        layoutStep2.setVisibility(View.VISIBLE);
        btnAction.setText("Sign Up");
        tvSignupSubtitle.setText("You are one step away from having flights at your fingertips.");
        ivStep1.setImageResource(R.drawable.ic_step_inactive);
        ivStep2.setImageResource(R.drawable.ic_step_active);
    }

    private void performSignup() {
        Toast.makeText(this, "Account created successfully!", Toast.LENGTH_LONG).show();
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        if (currentStep == 2) {
            showStep1();
        } else {
            super.onBackPressed();
        }
    }

    private void showStep1() {
        currentStep = 1;
        layoutStep1.setVisibility(View.VISIBLE);
        layoutStep2.setVisibility(View.GONE);
        btnAction.setText("Continue");
        tvSignupSubtitle.setText("You are one step closer to having flights at your fingertips.");
        ivStep1.setImageResource(R.drawable.ic_step_active);
        ivStep2.setImageResource(R.drawable.ic_step_inactive);
    }
}
