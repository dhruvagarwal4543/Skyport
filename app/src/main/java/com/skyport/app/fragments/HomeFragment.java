package com.skyport.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.skyport.app.R;
import com.skyport.app.adapters.FlightAdapter;
import com.skyport.app.models.Flight;

import java.util.ArrayList;
import java.util.List;

import android.widget.TextView;
import com.skyport.app.models.SessionManager;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        SessionManager sessionManager = new SessionManager(requireContext());
        TextView tvGreeting = view.findViewById(R.id.tvGreeting);
        tvGreeting.setText("Good morning, " + sessionManager.getUserName());

        return view;
    }
}
