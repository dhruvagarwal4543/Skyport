package com.skyport.app.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public interface AuthRepository {
    LiveData<Boolean> login(String email, String password);
    LiveData<Boolean> sendResetEmail(String email);
    LiveData<Boolean> resetPassword(String newPassword);
    LiveData<Boolean> registerUser(com.skyport.app.models.User userData);
}
