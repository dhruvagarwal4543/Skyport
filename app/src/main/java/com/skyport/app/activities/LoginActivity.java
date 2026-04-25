package com.skyport.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;
import com.skyport.app.R;

public class LoginActivity extends AppCompatActivity {

    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth mAuth;
    private ActivityResultLauncher<Intent> launcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        // ✅ AUTO LOGIN
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }

        // ✅ Google config
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // ✅ Result handler
        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {

                    if (result.getResultCode() == RESULT_OK) {

                        Task<GoogleSignInAccount> task =
                                GoogleSignIn.getSignedInAccountFromIntent(result.getData());

                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);

                            firebaseAuthWithGoogle(account.getIdToken());

                        } catch (Exception e) {
                            Toast.makeText(this,
                                    "Google Sign-In Failed",
                                    Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // ✅ Button click
        findViewById(R.id.googleBtn).setOnClickListener(v -> {
            launcher.launch(googleSignInClient.getSignInIntent());
        });
    }

    // ✅ Firebase Auth (FINAL CLEAN)
    private void firebaseAuthWithGoogle(String idToken) {

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {

                    if (task.isSuccessful()) {

                        Toast.makeText(this, "Login Success", Toast.LENGTH_SHORT).show();

                        // 👉 ALWAYS go to Home
                        startActivity(new Intent(this, HomeActivity.class));
                        finish();

                    } else {
                        Toast.makeText(this,
                                "Error: " + task.getException(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}