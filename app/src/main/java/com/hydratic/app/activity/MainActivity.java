package com.hydratic.app.activity;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.hydratic.app.R;
import com.hydratic.app.fragment.HistoryFragment;
import com.hydratic.app.fragment.HydrationFragment;
import com.hydratic.app.fragment.SettingsFragment;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends FragmentActivity {

    @BindView(R.id.bottom_navigation_bar) BottomNavigationView mBottomNavigationView;

    private FragmentManager mFragmentManager;
    private Fragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mFragmentManager = getSupportFragmentManager();

        // Set the default fragment
        mFragmentManager.beginTransaction().replace(
                R.id.main_fragment_container,
                new HydrationFragment()).commit();

        mBottomNavigationView.setSelectedItemId(R.id.action_hydration);
        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();
                switch(id) {
                    case R.id.action_history:
                        mFragment = new HistoryFragment();
                        break;
                    case R.id.action_hydration:
                        mFragment = new HydrationFragment();
                        break;
                    case R.id.action_settings:
                        mFragment = new SettingsFragment();
                        break;
                }

                FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.main_fragment_container, mFragment).commit();
                return true;
            }
        });
    }
}
