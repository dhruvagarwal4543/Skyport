package com.skyport.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.skyport.app.R;
import com.skyport.app.activities.MyAccountActivity;

public class ProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Navigate directly to MyAccountActivity instead of showing an empty fragment
        startActivity(new Intent(getActivity(), MyAccountActivity.class));
        // Return an empty view — activity will cover it
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }
}
