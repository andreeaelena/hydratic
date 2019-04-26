package com.hydratic.app.fragment;

import android.animation.LayoutTransition;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import com.hydratic.app.util.Constants.DatabaseFields;
import com.hydratic.app.util.Constants.Dialogs;
import com.hydratic.app.util.Utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;

import static com.hydratic.app.util.Constants.PERCENTAGE_FORMAT;

public class HydrationFragment extends Fragment {

    @BindView(R.id.daily_goal) TextView mDailyGoalTextView;
    @BindView(R.id.progress_container) View mProgressContainer;
    @BindView(R.id.progress_indicator) FrameLayout mProgressIndicator;
    @BindView(R.id.hydration_level) TextView mHydrationLevelTextView;
    @BindView(R.id.log_drink_button) FloatingActionButton mLogDrinkButton;

    private DatabaseReference mDrinkLogRef;

    private int mHydrationAmount;

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
            // Fail silently
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
                    .child(DatabaseFields.DB_FIELD_USERS)
                    .child(currentUser.getUid())
                    .child(DatabaseFields.DB_FIELD_DRINK_LOG);
            mDrinkLogRef.addValueEventListener(mDrinkLogValueListener);
        }

        setupUI();
        return rootView;
    }

    private void setupUI() {
        updateUI();

        final User user = MemoryStore.getInstance().getLoggedInUser();
        final Units units = Units.getUnitsFromUser(user);

        mDailyGoalTextView.setText(String.format(getString(R.string.your_daily_goal),
                Utils.getHydrationTarget(user, units),
                Utils.getVolumeUnits(units)));

        mProgressIndicator.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);

        mLogDrinkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AddDrinkDialogFragment addDrinkDialogFragment = new AddDrinkDialogFragment();
                addDrinkDialogFragment.show(getFragmentManager(), Dialogs.DIALOG_ADD_DRINK);
            }
        });
    }

    private void updateUI() {
        mHydrationLevelTextView.setText(String.format(PERCENTAGE_FORMAT, getHydrationPercentage()));

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
}
