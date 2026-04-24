package com.skyport.app.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class MockAuthRepository implements AuthRepository {

    @Override
    public LiveData<Boolean> login(String email, String password) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        // Mock success
        result.setValue(true);
        return result;
    }

    @Override
    public LiveData<Boolean> sendResetEmail(String email) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        result.setValue(true);
        return result;
    }

    @Override
    public LiveData<Boolean> resetPassword(String newPassword) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        result.setValue(true);
        return result;
    }

    @Override
    public LiveData<Boolean> registerUser(com.skyport.app.models.User userData) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        // Mock success
        result.setValue(true);
        return result;
    }
}
