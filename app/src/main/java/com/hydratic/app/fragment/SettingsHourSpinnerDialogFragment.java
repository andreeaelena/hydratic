package com.hydratic.app.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.hydratic.app.R;
import com.hydratic.app.util.Constants;

import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class SettingsHourSpinnerDialogFragment extends DialogFragment {

    public interface OnHourSpinnerDialogPositiveButtonClickListener {
        void onClick(DialogInterface dialogInterface, int newValue);
    }

    private OnHourSpinnerDialogPositiveButtonClickListener mOnPositiveButtonClickListener;

    public static SettingsHourSpinnerDialogFragment createInstance(Bundle args, OnHourSpinnerDialogPositiveButtonClickListener onPositiveButtonClickListener) {
        final SettingsHourSpinnerDialogFragment settingsHourSpinnerDialogFragment = new SettingsHourSpinnerDialogFragment();
        settingsHourSpinnerDialogFragment.setArguments(args);
        settingsHourSpinnerDialogFragment.mOnPositiveButtonClickListener = onPositiveButtonClickListener;
        return settingsHourSpinnerDialogFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final List<String> hoursList = Arrays.asList(getResources().getStringArray(R.array.hours));
        final List<String> timeOfDayList = Arrays.asList(getResources().getStringArray(R.array.time_of_day));

        int selected24Hour = 0;

        final Bundle args = getArguments();
        if (args != null) {
            selected24Hour = args.getInt(Constants.Extras.EXTRA_SELECTED_ITEM);
        }

        int selectedHourIndex = 0;
        int selectedTimeOfDayIndex = 0;
        if (selected24Hour > 12) {
            int twelveHour = selected24Hour - 12;
            selectedHourIndex = hoursList.indexOf(String.valueOf(twelveHour));
            selectedTimeOfDayIndex = 1;
        } else {
            selectedHourIndex = hoursList.indexOf(String.valueOf(selected24Hour));
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final LayoutInflater inflater = requireActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.settings_spinner_dialog, null);
        final Spinner hourSpinner = view.findViewById(R.id.dialog_hour_spinner);
        final Spinner timeOfDaySpinner = view.findViewById(R.id.dialog_time_of_day_spinner);

        final ArrayAdapter hourArrayAdapter = ArrayAdapter.createFromResource(
                getActivity(), R.array.hours, R.layout.hour_spinner_item);
        hourArrayAdapter.setDropDownViewResource(R.layout.hour_spinner_item);
        hourSpinner.setAdapter(hourArrayAdapter);
        hourSpinner.setSelection(selectedHourIndex);

        final ArrayAdapter timeOfDayArrayAdapter = ArrayAdapter.createFromResource(
                getActivity(), R.array.time_of_day, R.layout.time_of_day_spinner_item);
        timeOfDayArrayAdapter.setDropDownViewResource(R.layout.time_of_day_spinner_item);
        timeOfDaySpinner.setAdapter(timeOfDayArrayAdapter);
        timeOfDaySpinner.setSelection(selectedTimeOfDayIndex);

        builder.setView(view);
        builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                int hourSpinnerSelectedItemPosition = hourSpinner.getSelectedItemPosition();
                int timeOfDaySelectedItemPosition = timeOfDaySpinner.getSelectedItemPosition();

                int selectedHour = Integer.parseInt(hoursList.get(hourSpinnerSelectedItemPosition));
                String selectedTimeOfDay = timeOfDayList.get(timeOfDaySelectedItemPosition);

                if (selectedTimeOfDay.equalsIgnoreCase("PM")) {
                    selectedHour = selectedHour + 12;
                }

                mOnPositiveButtonClickListener.onClick(dialogInterface, selectedHour);
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        return builder.create();
    }
}
