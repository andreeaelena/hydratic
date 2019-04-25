package com.hydratic.app.fragment;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hydratic.app.R;
import com.hydratic.app.adapter.LogHistoryAdapter;
import com.hydratic.app.model.DrinkLog;
import com.hydratic.app.model.User;
import com.hydratic.app.storage.MemoryStore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class HistoryFragment extends Fragment {

    @BindView(R.id.empty_view) View mEmptyView;
    @BindView(R.id.log_history_chart) BarChart mLogHistoryBarChart;
    @BindView(R.id.log_history) RecyclerView mLogHistoryRecyclerView;

    private DatabaseReference mDatabase;
    private DatabaseReference mDrinkLogRef;
    private LogHistoryAdapter mAdapter;
    private SimpleDateFormat mSimpleDateFormat;

    private List<DrinkLog> mDrinkLogList;

    private ValueEventListener mDrinkLogValueListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if (dataSnapshot.exists()) {
                mEmptyView.setVisibility(View.GONE);
                mLogHistoryBarChart.setVisibility(View.VISIBLE);
                mLogHistoryRecyclerView.setVisibility(View.VISIBLE);

                mDrinkLogList = new ArrayList<>();
                for(DataSnapshot drinkLogSnapshot: dataSnapshot.getChildren()) {
                    if (drinkLogSnapshot.exists()) {
                        final DrinkLog drinkLog = drinkLogSnapshot.getValue(DrinkLog.class);
                        mDrinkLogList.add(drinkLog);
                    }
                }

                // Generate the Bar Chart values
                final List<BarEntry> values = getBarChartValues();
                final BarDataSet dataSet = (BarDataSet) mLogHistoryBarChart.getData().getDataSetByIndex(0);
                dataSet.setValues(values);

                // Notify the Bar Chart that the data has changed
                mLogHistoryBarChart.getData().notifyDataChanged();
                mLogHistoryBarChart.notifyDataSetChanged();
                mLogHistoryBarChart.invalidate();
            } else {
                mEmptyView.setVisibility(View.VISIBLE);
                mLogHistoryBarChart.setVisibility(View.GONE);
                mLogHistoryRecyclerView.setVisibility(View.GONE);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            // TODO
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_history, container, false);
        ButterKnife.bind(this, rootView);

        final User user = MemoryStore.getInstance().getLoggedInUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDrinkLogRef = mDatabase.child("users").child(user.id).child("drinkLog");

        final Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = getResources().getConfiguration().getLocales().get(0);
        } else {
            locale = getResources().getConfiguration().locale;
        }
        mSimpleDateFormat = new SimpleDateFormat("MM/dd", locale);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setupUI();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mAdapter != null) {
            mAdapter.startListening();
        }
        mDrinkLogRef.addValueEventListener(mDrinkLogValueListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAdapter != null) {
            mAdapter.stopListening();
        }
        mDrinkLogRef.removeEventListener(mDrinkLogValueListener);
    }

    private void setupUI() {
        setupBarChart();
        setupRecyclerView();
    }

    private void setupBarChart() {
        mLogHistoryBarChart.setDrawBarShadow(false);
        mLogHistoryBarChart.setDrawValueAboveBar(true);
        mLogHistoryBarChart.getDescription().setEnabled(false);
        mLogHistoryBarChart.setPinchZoom(false);
        mLogHistoryBarChart.setDrawGridBackground(false);

        // Configure the chart's X axis
        final XAxis xAxis = mLogHistoryBarChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f); // 1 day intervals
        xAxis.setLabelCount(7);

        // Configure the chart's legend
        final Legend legend = mLogHistoryBarChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);

        // Configure the chart's data set
        final BarDataSet barDataSet = new BarDataSet(getBarChartValues(),
                getString(R.string.daily_hydration_levels));
        final BarData barData = new BarData(barDataSet);
        mLogHistoryBarChart.setData(barData);
    }

    private void setupRecyclerView() {
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mLogHistoryRecyclerView.setLayoutManager(layoutManager);
        mLogHistoryRecyclerView.setHasFixedSize(true);

        final DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                mLogHistoryRecyclerView.getContext(), layoutManager.getOrientation());
        mLogHistoryRecyclerView.addItemDecoration(dividerItemDecoration);

        final User user = MemoryStore.getInstance().getLoggedInUser();

        // Create the query to retrieve the drink log for the current user from
        // the Firebase Realtime Database.
        Query logHistoryQuery = mDatabase.child("users").child(user.id).child("drinkLog");

        final FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<DrinkLog>()
                .setQuery(logHistoryQuery, DrinkLog.class)
                .build();

        mAdapter = new LogHistoryAdapter(getActivity(), options);
        mLogHistoryRecyclerView.setAdapter(mAdapter);
    }

    /**
     * Generate the Bar Chart values
     */
    private List<BarEntry> getBarChartValues() {
        final List<BarEntry> barChartValues = new ArrayList<>();
        final Map<String, List<DrinkLog>> drinkLogByDay = getLogByDay();

        final Calendar cal = Calendar.getInstance();
        for (String key : drinkLogByDay.keySet()) {
            final List<DrinkLog> drinkLogList = drinkLogByDay.get(key);

            int day = 0;
            int amount = 0;
            for (DrinkLog drinkLog : drinkLogList) {
                cal.setTimeInMillis(drinkLog.timestamp);
                day = cal.get(Calendar.DATE);
                amount += drinkLog.amount;
            }

            // Create new BarEntry for day
            final BarEntry barEntry = new BarEntry(day, amount);
            barChartValues.add(barEntry);
        }
        return barChartValues;
    }

    /**
     * Generate a Map containing the drink logs sorted by day
     */
    private Map<String, List<DrinkLog>> getLogByDay() {
        final Map<String, List<DrinkLog>> drinkLogByDay = new HashMap<>();

        if (mDrinkLogList != null) {
            final Calendar cal = Calendar.getInstance();
            for (DrinkLog drinkLog : mDrinkLogList) {
                cal.setTimeInMillis(drinkLog.timestamp);
                final String key = mSimpleDateFormat.format(cal.getTime());

                if (drinkLogByDay.containsKey(key)) {
                    final List<DrinkLog> drinkLogsForDate = drinkLogByDay.get(key);
                    drinkLogsForDate.add(drinkLog);
                    drinkLogByDay.put(key, drinkLogsForDate);
                } else {
                    final List<DrinkLog> drinkLogsForDate = new ArrayList<>();
                    drinkLogsForDate.add(drinkLog);
                    drinkLogByDay.put(key, drinkLogsForDate);
                }
            }
        }

        return drinkLogByDay;
    }
}
