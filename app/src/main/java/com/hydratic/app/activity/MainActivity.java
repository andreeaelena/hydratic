package com.hydratic.app.activity;

import android.os.Bundle;

import com.hydratic.app.adapter.MainPagerAdapter;
import com.hydratic.app.R;

import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends FragmentActivity {

    @BindView(R.id.main_view_pager) ViewPager mViewPager;

    private PagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mPagerAdapter = new MainPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);
    }
}
