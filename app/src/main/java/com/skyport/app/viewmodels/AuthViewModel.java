package com.skyport.app.viewmodels;

import android.text.TextUtils;
import android.util.Patterns;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.skyport.app.repository.AuthRepository;
import com.skyport.app.repository.MockAuthRepository;

public class AuthViewModel extends ViewModel {

    private final AuthRepository authRepository;

    // States
    private final MutableLiveData<String> email = new MutableLiveData<>("");
    private final MutableLiveData<String> password = new MutableLiveData<>("");
    private final MutableLiveData<String> confirmPassword = new MutableLiveData<>("");

    // LiveData for UI feedback
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> emailSentSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> resetSuccess = new MutableLiveData<>();

    public AuthViewModel() {
        this.authRepository = new MockAuthRepository(); // Future: inject via factory
    }

    // Getters/Setters
    public MutableLiveData<String> getEmail() { return email; }
    public MutableLiveData<String> getPassword() { return password; }
    public MutableLiveData<String> getConfirmPassword() { return confirmPassword; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getLoginSuccess() { return loginSuccess; }
    public LiveData<Boolean> getEmailSentSuccess() { return emailSentSuccess; }
    public LiveData<Boolean> getResetSuccess() { return resetSuccess; }

    public void login() {
        if (validateLogin()) {
            authRepository.login(email.getValue(), password.getValue()).observeForever(success -> {
                if (success) loginSuccess.setValue(true);
            });
        }
    }

    public void sendResetEmail() {
        if (validateEmailOnly()) {
            authRepository.sendResetEmail(email.getValue()).observeForever(success -> {
                if (success) emailSentSuccess.setValue(true);
            });
        }
    }

    public void resetPassword() {
        if (validateResetPassword()) {
            authRepository.resetPassword(password.getValue()).observeForever(success -> {
                if (success) resetSuccess.setValue(true);
            });
        }
    }

    private boolean validateLogin() {
        if (!isEmailValid()) {
            errorMessage.setValue("Please enter a valid email address");
            return false;
        }
        if (TextUtils.isEmpty(password.getValue())) {
            errorMessage.setValue("Password cannot be empty");
            return false;
        }
        return true;
    }

    private boolean validateEmailOnly() {
        if (!isEmailValid()) {
            errorMessage.setValue("Please enter a valid email address");
            return false;
        }
        return true;
    }

    private boolean validateResetPassword() {
        String p = password.getValue();
        String cp = confirmPassword.getValue();

        if (TextUtils.isEmpty(p) || p.length() < 8) {
            errorMessage.setValue("Password must be at least 8 characters");
            return false;
        }
        if (!containsUpperAndLower(p)) {
            errorMessage.setValue("Password must contain both upper and lower case letters");
            return false;
        }
        if (!containsNumberOrSymbol(p)) {
            errorMessage.setValue("Password must contain at least one number or symbol");
            return false;
        }
        if (!p.equals(cp)) {
            errorMessage.setValue("Passwords do not match");
            return false;
        }
        return true;
    }

    private boolean isEmailValid() {
        String e = email.getValue();
        return !TextUtils.isEmpty(e) && Patterns.EMAIL_ADDRESS.matcher(e).matches();
    }

    private boolean containsUpperAndLower(String s) {
        boolean hasUpper = false;
        boolean hasLower = false;
        for (char c : s.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            if (Character.isLowerCase(c)) hasLower = true;
        }
        return hasUpper && hasLower;
    }

    private boolean containsNumberOrSymbol(String s) {
        for (char c : s.toCharArray()) {
            if (!Character.isLetter(c)) return true;
        }
        return false;
    }
}
