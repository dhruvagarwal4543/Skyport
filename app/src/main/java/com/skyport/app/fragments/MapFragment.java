package com.skyport.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.skyport.app.R;

public class MapFragment extends Fragment {

    private ImageView  ivMap;
    private ScaleGestureDetector scaleDetector;

    // Track accumulated scale for the map image
    private float currentScale = 1.0f;
    private static final float MIN_SCALE = 0.8f;
    private static final float MAX_SCALE = 4.0f;

    // Active chip tracking for highlight toggle
    private CardView activeChip = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ivMap = view.findViewById(R.id.ivAirportMap);

        // ── Pinch-to-zoom using ScaleGestureDetector ─────────────────────────
        scaleDetector = new ScaleGestureDetector(requireContext(),
                new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    @Override
                    public boolean onScale(ScaleGestureDetector detector) {
                        currentScale *= detector.getScaleFactor();
                        currentScale = Math.max(MIN_SCALE, Math.min(currentScale, MAX_SCALE));
                        ivMap.setScaleX(currentScale);
                        ivMap.setScaleY(currentScale);
                        return true;
                    }
                });

        ivMap.setOnTouchListener((v, event) -> {
            scaleDetector.onTouchEvent(event);
            return true;
        });

        // ── Filter chip click listeners ───────────────────────────────────────
        CardView chipRestaurants = view.findViewById(R.id.chipRestaurants);
        CardView chipShopping    = view.findViewById(R.id.chipShopping);
        CardView chipFilter      = view.findViewById(R.id.chipFilter);

        chipRestaurants.setOnClickListener(v -> {
            toggleChip(chipRestaurants);
            Toast.makeText(requireContext(), "Showing restaurants", Toast.LENGTH_SHORT).show();
        });

        chipShopping.setOnClickListener(v -> {
            toggleChip(chipShopping);
            Toast.makeText(requireContext(), "Showing shops", Toast.LENGTH_SHORT).show();
        });

        chipFilter.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Filter options", Toast.LENGTH_SHORT).show()
        );

        // ── Search bar tap: hint (no keyboard needed for map) ─────────────────
        view.findViewById(R.id.tvSearchHint).setOnClickListener(v ->
                Toast.makeText(requireContext(), "Search coming soon", Toast.LENGTH_SHORT).show()
        );
    }

    /**
     * Toggle chip selection state: highlight active chip navy, revert previous one to white.
     */
    private void toggleChip(CardView tapped) {
        if (tapped == activeChip) {
            // Deselect
            tapped.setCardBackgroundColor(
                    requireContext().getColor(R.color.white));
            activeChip = null;
        } else {
            // Deselect previous
            if (activeChip != null) {
                activeChip.setCardBackgroundColor(
                        requireContext().getColor(R.color.white));
            }
            // Select tapped
            tapped.setCardBackgroundColor(
                    requireContext().getColor(R.color.skyport_navy));
            // Update chip text/icon colors would need a custom chip — keeping it simple here
            activeChip = tapped;
        }
    }
}
