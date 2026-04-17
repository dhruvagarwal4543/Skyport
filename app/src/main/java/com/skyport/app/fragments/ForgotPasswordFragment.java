package com.skyport.app.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.skyport.app.R;
import com.skyport.app.viewmodels.AuthViewModel;

public class ForgotPasswordFragment extends Fragment {

    private AuthViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_forgot_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        EditText etEmail = view.findViewById(R.id.etEmail);
        Button btnConfirm = view.findViewById(R.id.btnConfirm);

        etEmail.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) { viewModel.getEmail().setValue(s.toString()); }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        btnConfirm.setOnClickListener(v -> viewModel.sendResetEmail());

        view.findViewById(R.id.ivBack).setOnClickListener(v -> Navigation.findNavController(view).navigateUp());

        viewModel.getEmailSentSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success) Navigation.findNavController(view).navigate(R.id.action_forgotPasswordFragment_to_emailSentFragment);
        });
    }
}
