package com.skyport.app.activities;

import android.content.Intent;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.skyport.app.R;
import com.skyport.app.activities.MyTicketsActivity;
import com.skyport.app.fragments.HomeFragment;
import com.skyport.app.fragments.MapFragment;
import com.skyport.app.fragments.ProfileFragment;
import com.skyport.app.fragments.ServicesFragment;

public class HomeActivity extends AppCompatActivity {

    // ── Cached fragment instances ──────────────────────────────────────────────
    private HomeFragment     fragHome;
    private MapFragment      fragMap;
    private ServicesFragment fragServices;

    private Fragment activeFragment;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        bottomNav = findViewById(R.id.bottomNavigationView);

        // ServicesFragment logic removed, keep others
        fragHome     = new HomeFragment();
        fragMap      = new MapFragment();

        // Add all fragments; show only Home initially
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragmentContainer, fragMap,      "map")
                .hide(fragMap)
                .add(R.id.fragmentContainer, fragHome,     "home")   // shown last → visible on top
                .commit();

        activeFragment = fragHome;

        // Highlight the Home item as initially selected
        bottomNav.setSelectedItemId(R.id.nav_home);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            // nav_suitcase = My Tickets → launch as a standalone activity
            if (id == R.id.nav_suitcase) {
                startActivity(new Intent(this, MyTicketsActivity.class));
                return true;
            }
            
            // nav_shop = Shops & Services → launch as a standalone activity
            if (id == R.id.nav_shop) {
                startActivity(new Intent(this, ShopActivity.class));
                return true;
            }

            Fragment target = resolveFragment(id);
            if (target == null || target == activeFragment) return true;
            switchTo(target);
            return true;
        });
    }

    // ── Fragment routing ───────────────────────────────────────────────────────

    private Fragment resolveFragment(int navId) {
        if (navId == R.id.nav_location) return fragMap;       // Location → Airport map
        if (navId == R.id.nav_home)     return fragHome;      // Home icon → Home dashboard
        return null;
    }

    /**
     * Show/hide fragments instead of replace() so state is preserved across tab switches.
     * Fragments are never re-created after the first launch.
     */
    private void switchTo(Fragment next) {
        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        tx.hide(activeFragment);
        tx.show(next);
        tx.commit();
        activeFragment = next;
    }
}
