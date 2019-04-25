package com.hydratic.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hydratic.app.R;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

public class TipsPagerAdapter extends PagerAdapter {

    private Context mContext;
    private String[] mTipsArray;

    public TipsPagerAdapter(Context context) {
        mContext = context;
        mTipsArray = context.getResources().getStringArray(R.array.tips_array);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        final LayoutInflater inflater = LayoutInflater.from(mContext);
        final ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.tips_page_item, container, false);

        final TextView tipTextView = layout.findViewById(R.id.tip_text);
        tipTextView.setText(mTipsArray[position]);

        container.addView(layout);
        return layout;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return mTipsArray.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }
}
