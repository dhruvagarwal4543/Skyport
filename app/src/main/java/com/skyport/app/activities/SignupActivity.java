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
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.skyport.app.viewmodels.AuthViewModel;
import com.skyport.app.models.SessionManager;
import com.skyport.app.R;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private View layoutStep1, layoutStep2;
    private Button btnAction;
    private TextView tvSignupSubtitle;
    private ImageView ivStep1, ivStep2;
    private AuthViewModel viewModel;
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

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        initViews();
        setupListeners();
        observeViewModel();
        setupOnBackPressedCallback();
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

        Button btnGoogleLogin = layoutStep1.findViewById(R.id.btnGoogleLogin);
        android.widget.ProgressBar progressBarAuth = layoutStep1.findViewById(R.id.progressBarAuth);
        
        if (btnGoogleLogin != null) {
            com.google.android.gms.auth.api.signin.GoogleSignInOptions gso = new com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();
            com.google.android.gms.auth.api.signin.GoogleSignInClient mGoogleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(this, gso);

            androidx.activity.result.ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
                    new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == android.app.Activity.RESULT_OK) {
                            try {
                                com.google.android.gms.tasks.Task<com.google.android.gms.auth.api.signin.GoogleSignInAccount> task = 
                                    com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                                com.google.android.gms.auth.api.signin.GoogleSignInAccount account = task.getResult(com.google.android.gms.common.api.ApiException.class);
                                
                                com.google.firebase.auth.AuthCredential credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(account.getIdToken(), null);
                                com.google.firebase.auth.FirebaseAuth.getInstance().signInWithCredential(credential)
                                    .addOnCompleteListener(this, authTask -> {
                                        if (authTask.isSuccessful()) {
                                            com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
                                            if (user != null) {
                                                String name = user.getDisplayName() != null ? user.getDisplayName() : "Nikhil";
                                                String email = user.getEmail();
                                                String uid = user.getUid();
                                                
                                                java.util.Map<String, Object> userMap = new java.util.HashMap<>();
                                                userMap.put("name", name);
                                                userMap.put("email", email);
                                                userMap.put("uid", uid);
                                                
                                                com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("users").document(uid)
                                                    .set(userMap)
                                                    .addOnSuccessListener(aVoid -> {
                                                        if (progressBarAuth != null) progressBarAuth.setVisibility(View.GONE);
                                                        Intent intent = new Intent(SignupActivity.this, HomeActivity.class);
                                                        intent.putExtra("PREFILL_NAME", name);
                                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                        startActivity(intent);
                                                        finish();
                                                    });
                                            }
                                        } else {
                                            if (progressBarAuth != null) progressBarAuth.setVisibility(View.GONE);
                                            Toast.makeText(SignupActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                            } catch (com.google.android.gms.common.api.ApiException e) {
                                if (progressBarAuth != null) progressBarAuth.setVisibility(View.GONE);
                                Toast.makeText(SignupActivity.this, "Google sign in failed", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            if (progressBarAuth != null) progressBarAuth.setVisibility(View.GONE);
                        }
                    }
            );

            btnGoogleLogin.setOnClickListener(v -> {
                if (progressBarAuth != null) progressBarAuth.setVisibility(View.VISIBLE);
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                googleSignInLauncher.launch(signInIntent);
            });
        }
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
        String firstName = etFirstName.getText().toString().trim();
        String lastName  = etLastName.getText().toString().trim();
        String email     = etEmail.getText().toString().trim();
        String phone     = etPhone.getText().toString().trim();
        String password  = etPassword.getText().toString();
        String fullName  = firstName + " " + lastName;

        FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user == null) return;

                    // Update Firebase Auth display name
                    UserProfileChangeRequest profileUpdate =
                            new UserProfileChangeRequest.Builder()
                                    .setDisplayName(fullName)
                                    .build();
                    user.updateProfile(profileUpdate);

                    // Save to Firestore users/{uid}
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("uid",   user.getUid());
                    userData.put("name",  fullName);
                    userData.put("email", email);
                    userData.put("phone", phone);

                    FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(user.getUid())
                            .set(userData, SetOptions.merge())
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Account created successfully!", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(this, HomeActivity.class);
                                intent.putExtra("USER_NAME", firstName);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                // Firestore failed but auth succeeded — still navigate home
                                Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(this, HomeActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Sign Up failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void observeViewModel() {
        viewModel.getSignupSuccess().observe(this, success -> {
            if (success) {
                // new SessionManager(this).saveSession(etFirstName.getText().toString());
                Toast.makeText(this, "Account created successfully!", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(this, HomeActivity.class);
                intent.putExtra("USER_NAME", etFirstName.getText().toString());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) showError(error);
        });
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void setupOnBackPressedCallback() {
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (currentStep == 2) {
                    showStep1();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                    setEnabled(true);
                }
            }
        });
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
