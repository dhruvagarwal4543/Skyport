package com.skyport.app.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.skyport.app.R;

public class AirportMapBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_IATA = "iata";
    private static final String ARG_CITY = "city";
    private static final String ARG_NAME = "name";
    private static final String ARG_LAT  = "lat";
    private static final String ARG_LNG  = "lng";
    private static final String ARG_HAS_COORDS = "has_coords";

    public static AirportMapBottomSheet newInstance(
            String iata, String city, String name,
            boolean hasCoords, double lat, double lng) {

        AirportMapBottomSheet sheet = new AirportMapBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_IATA,      iata);
        args.putString(ARG_CITY,      city);
        args.putString(ARG_NAME,      name);
        args.putBoolean(ARG_HAS_COORDS, hasCoords);
        args.putDouble(ARG_LAT,       lat);
        args.putDouble(ARG_LNG,       lng);
        sheet.setArguments(args);
        return sheet;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_airport_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = requireArguments();
        String iata      = args.getString(ARG_IATA,  "");
        String city      = args.getString(ARG_CITY,  "");
        String name      = args.getString(ARG_NAME,  "");
        boolean hasCords = args.getBoolean(ARG_HAS_COORDS, false);
        double lat       = args.getDouble(ARG_LAT, 0);
        double lng       = args.getDouble(ARG_LNG, 0);

        // Bind labels
        ((TextView) view.findViewById(R.id.tvMapIata))        .setText(iata);
        ((TextView) view.findViewById(R.id.tvMapCity))        .setText(city);
        ((TextView) view.findViewById(R.id.tvMapAirportName)) .setText(name);

        WebView webViewMap  = view.findViewById(R.id.webViewMap);
        TextView tvOpenMaps = view.findViewById(R.id.tvOpenInMaps);

        if (!hasCords || (lat == 0 && lng == 0)) {
            webViewMap.setVisibility(View.GONE);
            tvOpenMaps.setVisibility(View.GONE);
            Toast.makeText(requireContext(), "Location not available for " + iata, Toast.LENGTH_SHORT).show();
            return;
        }

        // ── OpenStreetMap embed (free, no API key required) ───────────────
        setupMapWebView(webViewMap, lat, lng);

        // "Open in Maps" → native map app
        tvOpenMaps.setOnClickListener(v -> {
            String geoUri = "geo:" + lat + "," + lng + "?q=" + lat + "," + lng + "(" + name + ")";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(geoUri));
            intent.setPackage("com.google.android.apps.maps");
            if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
                startActivity(intent);
            } else {
                // Fall back to browser
                String mapsUrl = "https://www.openstreetmap.org/?mlat=" + lat + "&mlon=" + lng + "#map=14/" + lat + "/" + lng;
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(mapsUrl)));
            }
        });
    }

    private void setupMapWebView(WebView webView, double lat, double lng) {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        webView.setWebViewClient(new WebViewClient());
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        // Bounding box: ±0.05 degrees around the airport (~5 km)
        double delta = 0.05;
        double minLon = lng - delta;
        double minLat = lat - delta;
        double maxLon = lng + delta;
        double maxLat = lat + delta;

        // OpenStreetMap embed URL — completely free, no key needed
        String mapUrl = "https://www.openstreetmap.org/export/embed.html"
                + "?bbox=" + minLon + "," + minLat + "," + maxLon + "," + maxLat
                + "&layer=mapnik"
                + "&marker=" + lat + "," + lng;

        android.util.Log.d("MAP_DEBUG", "Loading OSM map: " + mapUrl);
        webView.loadUrl(mapUrl);
    }
}
