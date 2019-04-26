package com.hydratic.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.hydratic.app.R;
import com.hydratic.app.callback.UserSignedOutListener;
import com.hydratic.app.fragment.HistoryFragment;
import com.hydratic.app.fragment.HydrationFragment;
import com.hydratic.app.fragment.SettingsFragment;
import com.hydratic.app.util.Constants.Extras;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements UserSignedOutListener {

    @BindView(R.id.bottom_navigation_bar) BottomNavigationView mBottomNavigationView;
    @BindView(R.id.main_toolbar) Toolbar mToolbar;

    private FragmentManager mFragmentManager;
    private Fragment mFragment;
    private int mSelectedFragmentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mToolbar.setLogo(R.mipmap.ic_launcher);
        setSupportActionBar(mToolbar);
        setTitle(R.string.app_name);

        mSelectedFragmentId = R.id.action_hydration;
        if (savedInstanceState != null) {
            mSelectedFragmentId = savedInstanceState.getInt(Extras.EXTRA_SELECTED_FRAGMENT_ID);
        }

        setupUI();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(Extras.EXTRA_SELECTED_FRAGMENT_ID, mBottomNavigationView.getSelectedItemId());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_tips:
                final Intent tipsActivityIntent = new Intent(MainActivity.this, TipsActivity.class);
                startActivity(tipsActivityIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setupUI() {
        mFragmentManager = getSupportFragmentManager();

        // Set the default fragment
        Fragment defaultFragment = new HydrationFragment();
        switch (mSelectedFragmentId) {
            case R.id.action_history:
                defaultFragment = new HistoryFragment();
                break;
            case R.id.action_settings:
                defaultFragment = new SettingsFragment();
                ((SettingsFragment) defaultFragment).setOnUserSignedOutListener(MainActivity.this);
                break;
        }

        mFragmentManager.beginTransaction()
                .replace(R.id.main_fragment_container, defaultFragment)
                .commit();

        mBottomNavigationView.setSelectedItemId(mSelectedFragmentId);
        mBottomNavigationView.setOnNavigationItemSelectedListener(new OnBottomNavigationItemSelectedListener());
    }

    @Override
    public void onUserSignedOut() {
        finish();
    }

    private class OnBottomNavigationItemSelectedListener implements BottomNavigationView.OnNavigationItemSelectedListener {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            int id = menuItem.getItemId();
            switch(id) {
                case R.id.action_history:
                    mFragment = new HistoryFragment();
                    setTitle(R.string.history);
                    break;
                case R.id.action_hydration:
                    mFragment = new HydrationFragment();
                    setTitle(R.string.hydration);
                    break;
                case R.id.action_settings:
                    mFragment = new SettingsFragment();
                    ((SettingsFragment) mFragment).setOnUserSignedOutListener(MainActivity.this);
                    setTitle(R.string.settings);
                    break;
            }

            FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.main_fragment_container, mFragment).commit();
            return true;
        }
    }
}
