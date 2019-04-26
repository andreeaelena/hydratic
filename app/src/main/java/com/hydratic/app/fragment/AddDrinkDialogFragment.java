package com.hydratic.app.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hydratic.app.R;
import com.hydratic.app.model.DrinkLog;
import com.hydratic.app.model.Units;
import com.hydratic.app.model.User;
import com.hydratic.app.storage.MemoryStore;
import com.hydratic.app.util.Constants.DatabaseFields;

import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import static com.hydratic.app.util.Constants.COFFEE;
import static com.hydratic.app.util.Constants.CUP;
import static com.hydratic.app.util.Constants.GLASS;
import static com.hydratic.app.util.Constants.IMPERIAL_VOLUME_UNIT_SHORT;
import static com.hydratic.app.util.Constants.TEA;
import static com.hydratic.app.util.Constants.WATER;

public class AddDrinkDialogFragment extends DialogFragment {

    private Units mPreferredUnits;
    private DatabaseReference mDrinkLogRef;

    public AddDrinkDialogFragment() {
        final User user = MemoryStore.getInstance().getLoggedInUser();
        mPreferredUnits = user.preferredUnits.equalsIgnoreCase(Units.IMPERIAL.toString())
                ? Units.IMPERIAL : Units.METRIC;
        mDrinkLogRef = FirebaseDatabase.getInstance().getReference()
                .child(DatabaseFields.DB_FIELD_USERS)
                .child(user.id)
                .child(DatabaseFields.DB_FIELD_DRINK_LOG);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final LayoutInflater inflater = requireActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.add_drink_custom_dialog, null);

        final TextView largeWaterAmountTextView = view.findViewById(R.id.large_water_amount);
        final TextView smallWaterAmountTextView = view.findViewById(R.id.small_water_amount);
        final TextView coffeeAmountTextView = view.findViewById(R.id.coffee_amount);
        final TextView teaAmountTextView = view.findViewById(R.id.tea_amount);

        final FloatingActionButton logLargeWaterGlassButton = view.findViewById(R.id.log_large_water_glass_button);
        final FloatingActionButton logSmallWaterGlassButton = view.findViewById(R.id.log_small_water_glass_button);
        final FloatingActionButton logCoffeeCupButton = view.findViewById(R.id.log_coffee_cup_button);
        final FloatingActionButton logTeaCupButton = view.findViewById(R.id.log_tea_cup_button);

        switch (mPreferredUnits) {
            case IMPERIAL:
                largeWaterAmountTextView.setText(R.string.sixteen_fl_oz_glass);
                smallWaterAmountTextView.setText(R.string.eight_fl_oz_glass);
                coffeeAmountTextView.setText(R.string.eight_fl_oz_cup);
                teaAmountTextView.setText(R.string.eight_fl_oz_cup);
                break;
            case METRIC:
                largeWaterAmountTextView.setText(R.string.sixteen_fl_oz_glass_ml);
                smallWaterAmountTextView.setText(R.string.eight_fl_oz_glass_ml);
                coffeeAmountTextView.setText(R.string.eight_fl_oz_cup_ml);
                teaAmountTextView.setText(R.string.eight_fl_oz_cup_ml);
                break;
        }

        logLargeWaterGlassButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long timestamp = Calendar.getInstance().getTimeInMillis();
                final DrinkLog drinkLog = new DrinkLog(timestamp, 16,
                        IMPERIAL_VOLUME_UNIT_SHORT, GLASS, WATER);
                mDrinkLogRef.child(String.valueOf(timestamp)).setValue(drinkLog);
                dismiss();
            }
        });

        logSmallWaterGlassButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long timestamp = Calendar.getInstance().getTimeInMillis();
                final DrinkLog drinkLog = new DrinkLog(timestamp, 8,
                        IMPERIAL_VOLUME_UNIT_SHORT, GLASS, WATER);
                mDrinkLogRef.child(String.valueOf(timestamp)).setValue(drinkLog);
                dismiss();
            }
        });

        logCoffeeCupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long timestamp = Calendar.getInstance().getTimeInMillis();
                final DrinkLog drinkLog = new DrinkLog(timestamp, 8,
                        IMPERIAL_VOLUME_UNIT_SHORT, CUP, COFFEE);
                mDrinkLogRef.child(String.valueOf(timestamp)).setValue(drinkLog);
                dismiss();
            }
        });

        logTeaCupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long timestamp = Calendar.getInstance().getTimeInMillis();
                final DrinkLog drinkLog = new DrinkLog(timestamp, 8,
                        IMPERIAL_VOLUME_UNIT_SHORT, CUP, TEA);
                mDrinkLogRef.child(String.valueOf(timestamp)).setValue(drinkLog);
                dismiss();
            }
        });

        builder.setView(view);

        return builder.create();
    }
}
