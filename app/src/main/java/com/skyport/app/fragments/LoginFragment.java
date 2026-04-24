package com.skyport.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.skyport.app.R;
import com.skyport.app.activities.CompleteProfileActivity;
import com.skyport.app.activities.HomeActivity;
import com.skyport.app.viewmodels.AuthViewModel;

public class LoginFragment extends Fragment {

    private AuthViewModel viewModel;
    private boolean isPasswordVisible = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        EditText etEmail = view.findViewById(R.id.etEmail);
        EditText etPassword = view.findViewById(R.id.etPassword);
        Button btnLogin = view.findViewById(R.id.btnLogin);
        TextView tvForgot = view.findViewById(R.id.tvForgotPassword);
        ImageView ivToggle = view.findViewById(R.id.ivTogglePassword);

        // Data binding (manual)
        etEmail.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) { viewModel.getEmail().setValue(s.toString()); }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        etPassword.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) { viewModel.getPassword().setValue(s.toString()); }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        ivToggle.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            if (isPasswordVisible) {
                etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                ivToggle.setImageResource(android.R.drawable.ic_menu_close_clear_cancel); // Mocking icon change
            } else {
                etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                ivToggle.setImageResource(android.R.drawable.ic_menu_view);
            }
            etPassword.setSelection(etPassword.length());
        });

        btnLogin.setOnClickListener(v -> viewModel.login());

        tvForgot.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_loginFragment_to_forgotPasswordFragment));

        view.findViewById(R.id.ivBack).setOnClickListener(v -> requireActivity().finish());
        Button btnGoogleLogin = view.findViewById(R.id.btnGoogleLogin);
        android.widget.ProgressBar progressBarAuth = view.findViewById(R.id.progressBarAuth);
        
        com.google.android.gms.auth.api.signin.GoogleSignInOptions gso = new com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
                
        com.google.android.gms.auth.api.signin.GoogleSignInClient mGoogleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(requireActivity(), gso);

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
                                .addOnCompleteListener(requireActivity(), authTask -> {
                                    if (authTask.isSuccessful()) {
                                        com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
                                        if (user != null) {
                                            String uid = user.getUid();
                                            String name = user.getDisplayName() != null ? user.getDisplayName() : "Nikhil";
                                            String email = user.getEmail();
                                            
                                            // Store in Firestore
                                            java.util.Map<String, Object> userMap = new java.util.HashMap<>();
                                            userMap.put("name", name);
                                            userMap.put("email", email);
                                            userMap.put("uid", uid);
                                            
                                                com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("users").document(uid)
                                                .set(userMap)
                                                .addOnSuccessListener(aVoid -> {
                                                    progressBarAuth.setVisibility(View.GONE);
                                                    Intent intent = new Intent(getActivity(), CompleteProfileActivity.class);
                                                    intent.putExtra("PREFILL_NAME", name);
                                                    startActivity(intent);
                                                    requireActivity().finish();
                                                });
                                        }
                                    } else {
                                        progressBarAuth.setVisibility(View.GONE);
                                        Toast.makeText(getContext(), "Authentication Failed.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                        } catch (com.google.android.gms.common.api.ApiException e) {
                            progressBarAuth.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "Google sign in failed", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        progressBarAuth.setVisibility(View.GONE);
                    }
                }
        );

        btnGoogleLogin.setOnClickListener(v -> {
            progressBarAuth.setVisibility(View.VISIBLE);
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });

        // Observers
        viewModel.getLoginSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success) {
                // new com.skyport.app.models.SessionManager(requireContext()).saveSession("Nikhil");
                String user = etEmail.getText().toString();
                Intent intent = new Intent(getActivity(), HomeActivity.class);
                intent.putExtra("USER_NAME", user.isEmpty() ? "Nikhil" : user.split("@")[0]);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
        });
    }
}
