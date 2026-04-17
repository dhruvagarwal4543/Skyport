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

        // Observers
        viewModel.getLoginSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success) {
                startActivity(new Intent(getActivity(), HomeActivity.class));
                requireActivity().finish();
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
        });
    }
}
