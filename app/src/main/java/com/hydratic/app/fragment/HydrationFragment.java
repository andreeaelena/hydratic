package com.hydratic.app.fragment;

import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.hydratic.app.model.User;
import com.hydratic.app.storage.MemoryStore;

import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;

public class HydrationFragment extends Fragment {

    @BindView(R.id.hydration_level) TextView mHydrationLevelTextView;
    @BindView(R.id.log_drink_button) Button mLogDrinkButton;

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
        mLogDrinkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long timestamp = Calendar.getInstance().getTimeInMillis();
                final DrinkLog drinkLog = new DrinkLog(16, "oz", "cup", "coffee");
                mDrinkLogRef.child(String.valueOf(timestamp)).setValue(drinkLog);
            }
        });
    }

    private void updateUI() {
        mHydrationLevelTextView.setText(String.format("%.0f%%", getHydrationPercentage()));
    }

    private double getHydrationPercentage() {
        final User loggedInUser = MemoryStore.getInstance().getLoggedInUser();
        return ((double) mHydrationAmount / loggedInUser.hydrationDailyTarget) * 100;
    }
}
