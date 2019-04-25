package com.hydratic.app.activity;

import android.os.Bundle;
import android.view.MenuItem;

import com.hydratic.app.R;
import com.hydratic.app.adapter.TipsPagerAdapter;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import butterknife.ButterKnife;

public class TipsActivity extends AppCompatActivity {

    @BindView(R.id.tips_toolbar) Toolbar mToolbar;
    @BindView(R.id.tips_view_pager) ViewPager mTipsViewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tips);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        setTitle(R.string.tips);

        setupUI();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle back click
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupUI() {
        final TipsPagerAdapter pagerAdapter = new TipsPagerAdapter(getApplicationContext());
        mTipsViewPager.setAdapter(pagerAdapter);
    }
}
