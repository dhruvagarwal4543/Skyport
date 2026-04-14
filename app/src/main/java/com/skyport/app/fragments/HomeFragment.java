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

public class HomeFragment extends Fragment {

    private RecyclerView rvFlights;
    private FlightAdapter flightAdapter;
    private List<Flight> flightList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        rvFlights = view.findViewById(R.id.rvFlights);
        rvFlights.setLayoutManager(new LinearLayoutManager(getContext()));

        flightList = new ArrayList<>();
        flightList.add(new Flight("Emirates", "BOM -> DXB", "10:00 AM"));
        flightList.add(new Flight("Qatar Airways", "DEL -> DOH", "14:30 PM"));
        flightList.add(new Flight("Air India", "BLR -> SFO", "23:00 PM"));
        flightList.add(new Flight("Singapore Airlines", "MAA -> SIN", "02:15 AM"));

        flightAdapter = new FlightAdapter(flightList);
        rvFlights.setAdapter(flightAdapter);

        return view;
    }
}
