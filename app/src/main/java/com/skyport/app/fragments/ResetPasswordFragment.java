package com.skyport.app.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.skyport.app.R;
import com.skyport.app.viewmodels.AuthViewModel;

public class ResetPasswordFragment extends Fragment {

    private AuthViewModel viewModel;
    private ImageView ivRule8, ivRuleCase, ivRuleNumber;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reset_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        EditText etNewPassword = view.findViewById(R.id.etNewPassword);
        EditText etConfirmPassword = view.findViewById(R.id.etConfirmPassword);
        Button btnConfirm = view.findViewById(R.id.btnConfirm);

        ivRule8 = view.findViewById(R.id.ivRule8);
        ivRuleCase = view.findViewById(R.id.ivRuleCase);
        ivRuleNumber = view.findViewById(R.id.ivRuleNumber);

        etNewPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                String p = s.toString();
                viewModel.getPassword().setValue(p);
                updateRulesUI(p);
            }
        });

        etConfirmPassword.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) { viewModel.getConfirmPassword().setValue(s.toString()); }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        btnConfirm.setOnClickListener(v -> viewModel.resetPassword());

        viewModel.getResetSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success) Navigation.findNavController(view).navigate(R.id.action_resetPasswordFragment_to_successFragment);
        });
    }

    private void updateRulesUI(String password) {
        // Rule 1: 8 characters
        if (password.length() >= 8) {
            ivRule8.setImageResource(android.R.drawable.checkbox_on_background);
            ivRule8.setColorFilter(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            ivRule8.setImageResource(android.R.drawable.checkbox_off_background);
            ivRule8.setColorFilter(getResources().getColor(R.color.skyport_navy));
        }

        // Rule 2: Case
        boolean hasUpper = false;
        boolean hasLower = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            if (Character.isLowerCase(c)) hasLower = true;
        }
        if (hasUpper && hasLower) {
            ivRuleCase.setImageResource(android.R.drawable.checkbox_on_background);
            ivRuleCase.setColorFilter(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            ivRuleCase.setImageResource(android.R.drawable.checkbox_off_background);
            ivRuleCase.setColorFilter(getResources().getColor(R.color.skyport_navy));
        }

        // Rule 3: Number or Symbol
        boolean hasNumberOrSymbol = false;
        for (char c : password.toCharArray()) {
            if (!Character.isLetter(c)) {
                hasNumberOrSymbol = true;
                break;
            }
        }
        if (hasNumberOrSymbol) {
            ivRuleNumber.setImageResource(android.R.drawable.checkbox_on_background);
            ivRuleNumber.setColorFilter(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            ivRuleNumber.setImageResource(android.R.drawable.checkbox_off_background);
            ivRuleNumber.setColorFilter(getResources().getColor(R.color.skyport_navy));
        }
    }
}
