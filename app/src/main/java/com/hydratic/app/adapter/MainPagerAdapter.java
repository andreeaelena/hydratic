package com.hydratic.app.adapter;

import com.hydratic.app.fragment.HistoryFragment;
import com.hydratic.app.fragment.HydrationFragment;
import com.hydratic.app.fragment.SettingsFragment;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class MainPagerAdapter extends FragmentStatePagerAdapter {

    private static final int PAGE_COUNT = 3;

    public MainPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new HistoryFragment();
            case 1:
                return new HydrationFragment();
            case 2:
                return new SettingsFragment();
        }
        return null;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }
}
