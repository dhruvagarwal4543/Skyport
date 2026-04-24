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

    private final MutableLiveData<String> email = new MutableLiveData<>("");
    private final MutableLiveData<String> password = new MutableLiveData<>("");
    private final MutableLiveData<String> confirmPassword = new MutableLiveData<>("");
    private final MutableLiveData<String> firstName = new MutableLiveData<>("");
    private final MutableLiveData<String> lastName = new MutableLiveData<>("");
    private final MutableLiveData<String> phone = new MutableLiveData<>("");
    private final MutableLiveData<String> country = new MutableLiveData<>("");
    private final MutableLiveData<String> nationality = new MutableLiveData<>("");
    private final MutableLiveData<String> passport = new MutableLiveData<>("");

    // LiveData for UI feedback
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> signupSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> emailSentSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> resetSuccess = new MutableLiveData<>();

    public AuthViewModel() {
        this.authRepository = new MockAuthRepository(); // Future: inject via factory
    }

    // Getters/Setters
    public MutableLiveData<String> getEmail() { return email; }
    public MutableLiveData<String> getPassword() { return password; }
    public MutableLiveData<String> getConfirmPassword() { return confirmPassword; }
    public MutableLiveData<String> getFirstName() { return firstName; }
    public MutableLiveData<String> getLastName() { return lastName; }
    public MutableLiveData<String> getPhone() { return phone; }
    public MutableLiveData<String> getCountry() { return country; }
    public MutableLiveData<String> getNationality() { return nationality; }
    public MutableLiveData<String> getPassport() { return passport; }

    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getLoginSuccess() { return loginSuccess; }
    public LiveData<Boolean> getSignupSuccess() { return signupSuccess; }
    public LiveData<Boolean> getEmailSentSuccess() { return emailSentSuccess; }
    public LiveData<Boolean> getResetSuccess() { return resetSuccess; }

    public void login() {
        if (validateLogin()) {
            authRepository.login(email.getValue(), password.getValue()).observeForever(success -> {
                if (success) loginSuccess.setValue(true);
            });
        }
    }

    public void signup() {
        if (validateSignup()) {
            com.skyport.app.models.User user = new com.skyport.app.models.User(
                    firstName.getValue(), lastName.getValue(), email.getValue(), phone.getValue());
            user.setCountry(country.getValue());
            user.setNationality(nationality.getValue());
            user.setPassport(passport.getValue());

            authRepository.registerUser(user).observeForever(success -> {
                if (success) signupSuccess.setValue(true);
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

    private boolean validateSignup() {
        if (TextUtils.isEmpty(firstName.getValue())) {
            errorMessage.setValue("First name is required");
            return false;
        }
        if (TextUtils.isEmpty(lastName.getValue())) {
            errorMessage.setValue("Last name is required");
            return false;
        }
        if (!isEmailValid()) {
            errorMessage.setValue("Please enter a valid email address");
            return false;
        }
        if (TextUtils.isEmpty(password.getValue()) || password.getValue().length() < 6) {
            errorMessage.setValue("Password must be at least 6 characters");
            return false;
        }
        if (!password.getValue().equals(confirmPassword.getValue())) {
            errorMessage.setValue("Passwords do not match");
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
