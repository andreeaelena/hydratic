package com.hydratic.app.fragment;

import android.animation.LayoutTransition;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hydratic.app.R;
import com.hydratic.app.model.DrinkLog;
import com.hydratic.app.model.Units;
import com.hydratic.app.model.User;
import com.hydratic.app.storage.MemoryStore;
import com.hydratic.app.util.Utils;

import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;

import static com.hydratic.app.util.Constants.IMPERIAL_UNITS;
import static com.hydratic.app.util.Constants.IMPERIAL_VOLUME_UNIT;
import static com.hydratic.app.util.Constants.METRIC_VOLUME_UNIT;

public class HydrationFragment extends Fragment {

    @BindView(R.id.daily_goal) TextView mDailyGoalTextView;
    @BindView(R.id.progress_container) View mProgressContainer;
    @BindView(R.id.progress_indicator) FrameLayout mProgressIndicator;
    @BindView(R.id.hydration_level) TextView mHydrationLevelTextView;
    @BindView(R.id.log_drink_button) Button mLogDrinkButton;

    private DatabaseReference mDrinkLogRef;

    private int mHydrationAmount;
    private Units mUnits;

    private ValueEventListener mDrinkLogValueListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if (dataSnapshot.exists()) {
                mHydrationAmount = 0;
                for(DataSnapshot drinkLogSnapshot: dataSnapshot.getChildren()) {
                    if (drinkLogSnapshot.exists()) {
                        String key = drinkLogSnapshot.getKey();
                        if (key != null && !key.isEmpty()) {
                            long timestamp = Long.valueOf(drinkLogSnapshot.getKey());
                            if (DateUtils.isToday(timestamp)) {
                                final DrinkLog drinkLog = drinkLogSnapshot.getValue(DrinkLog.class);
                                mHydrationAmount += drinkLog != null ? drinkLog.amount : 0;
                            }
                        }
                    }
                }
                updateUI();
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
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_hydration, container, false);
        ButterKnife.bind(this, rootView);

        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if(currentUser != null) {
            mDrinkLogRef = FirebaseDatabase.getInstance().getReference()
                    .child("users")
                    .child(currentUser.getUid())
                    .child("drinkLog");
            mDrinkLogRef.addValueEventListener(mDrinkLogValueListener);
        }

        setupUI();
        return rootView;
    }

    private void setupUI() {
        updateUI();

        final User user = MemoryStore.getInstance().getLoggedInUser();

        mUnits = user.preferredUnits.equalsIgnoreCase(IMPERIAL_UNITS)
                ? Units.IMPERIAL
                : Units.METRIC;

        mDailyGoalTextView.setText(String.format(getString(R.string.your_daily_goal),
                getHydrationTarget(user), getVolumeUnits()));

        mProgressIndicator.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);

        mLogDrinkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long timestamp = Calendar.getInstance().getTimeInMillis();
                final DrinkLog drinkLog = new DrinkLog(timestamp, 16, "oz", "cup", "coffee");
                mDrinkLogRef.child(String.valueOf(timestamp)).setValue(drinkLog);
            }
        });
    }

    private void updateUI() {
        mHydrationLevelTextView.setText(String.format("%.0f%%", getHydrationPercentage()));

        final ViewTreeObserver vto = mProgressContainer.getViewTreeObserver();
        if (vto.isAlive()) {
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    final User loggedInUser = MemoryStore.getInstance().getLoggedInUser();
                    double hydrationFactor = (double) mHydrationAmount / loggedInUser.hydrationDailyTarget;
                    double newProgress = mProgressContainer.getMeasuredHeight() * hydrationFactor;
                    ViewGroup.LayoutParams params = mProgressIndicator.getLayoutParams();
                    params.height = (int)newProgress;
                    mProgressIndicator.setLayoutParams(params);

                    // Remove the ViewTreeObserver
                    mProgressContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });
        }
    }

    private double getHydrationPercentage() {
        final User loggedInUser = MemoryStore.getInstance().getLoggedInUser();
        return ((double) mHydrationAmount / loggedInUser.hydrationDailyTarget) * 100;
    }

    private double getHydrationTarget(User user) {
        return mUnits == Units.METRIC
                ? Utils.convertFlOzToMl(user.hydrationDailyTarget)
                : user.hydrationDailyTarget;
    }

    private String getVolumeUnits() {
        return mUnits == Units.IMPERIAL
                ? IMPERIAL_VOLUME_UNIT
                : METRIC_VOLUME_UNIT;
    }
}
