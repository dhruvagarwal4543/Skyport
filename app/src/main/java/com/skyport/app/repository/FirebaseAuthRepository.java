package com.skyport.app.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.skyport.app.models.User;

import java.util.HashMap;
import java.util.Map;

/**
 * Real Firebase Auth + Firestore repository.
 * Replaces MockAuthRepository for production use.
 */
public class FirebaseAuthRepository implements AuthRepository {

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public LiveData<Boolean> login(String email, String password) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user != null) upsertUserDoc(user, null, null, null);
                    result.setValue(true);
                })
                .addOnFailureListener(e -> result.setValue(false));
        return result;
    }

    @Override
    public LiveData<Boolean> registerUser(User userData) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        String email    = userData.getEmail();
        String password = ""; // Password is passed separately by SignupActivity via ViewModel

        // SignupActivity sets the password via viewModel.getPassword().setValue(...)
        // We read it via FirebaseAuth which doesn't expose it here.
        // So we rely on SignupActivity calling createUser directly.
        // Just signal success for legacy compatibility.
        result.setValue(true);
        return result;
    }

    @Override
    public LiveData<Boolean> sendResetEmail(String email) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(v -> result.setValue(true))
                .addOnFailureListener(e -> result.setValue(false));
        return result;
    }

    @Override
    public LiveData<Boolean> resetPassword(String newPassword) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) { result.setValue(false); return result; }
        user.updatePassword(newPassword)
                .addOnSuccessListener(v -> result.setValue(true))
                .addOnFailureListener(e -> result.setValue(false));
        return result;
    }

    /** Merge user profile into Firestore without overwriting existing fields. */
    public void upsertUserDoc(FirebaseUser user, String name, String phone, String extraEmail) {
        Map<String, Object> data = new HashMap<>();
        data.put("uid",   user.getUid());
        data.put("email", user.getEmail() != null ? user.getEmail() :
                (extraEmail != null ? extraEmail : ""));
        if (name  != null && !name.isEmpty())  data.put("name",  name);
        if (phone != null && !phone.isEmpty())  data.put("phone", phone);

        db.collection("users")
          .document(user.getUid())
          .set(data, SetOptions.merge())
          .addOnFailureListener(e -> { /* silent */ });
    }
}
