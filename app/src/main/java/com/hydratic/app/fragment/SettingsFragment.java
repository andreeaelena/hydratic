package com.hydratic.app.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hydratic.app.R;
import com.hydratic.app.activity.LaunchActivity;
import com.hydratic.app.callback.UserSignedOutListener;
import com.hydratic.app.model.NotificationState;
import com.hydratic.app.model.Units;
import com.hydratic.app.model.User;
import com.hydratic.app.storage.MemoryStore;
import com.hydratic.app.util.Constants.Extras;
import com.hydratic.app.util.Utils;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingsFragment extends Fragment {

    private static final String DAILY_GOAL_FORMAT = "%.1f %s";

    @BindView(R.id.sign_out_item) View mSignOutItem;
    @BindView(R.id.daily_goal_item) View mDailyGoalItem;
    @BindView(R.id.measuring_units_item) RadioGroup mMeasuringUnitsItem;
    @BindView(R.id.notification_state_item) View mNotificationStateItem;
    @BindView(R.id.notification_start_time_item) View mNotificationStartTimeItem;
    @BindView(R.id.notification_end_time_item) View mNotificationEndTimeItem;
    @BindView(R.id.notification_repeat_item) View mNotificationRepeatItem;

    @BindView(R.id.user_name) TextView mUserNameTextView;
    @BindView(R.id.daily_goal) TextView mDailyGoalTextView;
    @BindView(R.id.notification_state) TextView mNotificationStateTextView;
    @BindView(R.id.notification_start_time) TextView mNotificationStartTimeTextView;
    @BindView(R.id.notification_end_time) TextView mNotificationEndTimeTextView;
    @BindView(R.id.notification_repeat) TextView mNotificationRepeatTextView;
    @BindView(R.id.notifications_on_container) View mNotificationsOnContainer;

    private User mUser;
    private Units mPreferredUnits;
    private NotificationState mNotificationState;
    private DatabaseReference mDatabaseUserRef;
    private UserSignedOutListener mUserSignedOutListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_settings, container, false);
        ButterKnife.bind(this, rootView);

        mUser = MemoryStore.getInstance().getLoggedInUser();
        mPreferredUnits = Units.getUnitsFromUser(mUser);

        mDatabaseUserRef = FirebaseDatabase.getInstance().getReference().child("users").child(mUser.id);

        setupUI();
        return rootView;
    }

    public void setOnUserSignedOutListener(UserSignedOutListener userSignedOutListener) {
        mUserSignedOutListener = userSignedOutListener;
    }

    private void setupUI() {
        mUserNameTextView.setText(mUser.displayName);

        mMeasuringUnitsItem.check(mPreferredUnits == Units.IMPERIAL ? R.id.imperial : R.id.metric);
        mMeasuringUnitsItem.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                mPreferredUnits = (i == R.id.metric) ? Units.METRIC : Units.IMPERIAL;
                mUser.preferredUnits = mPreferredUnits.toString();

                // Save to internal memory store
                MemoryStore.getInstance().setLoggedInUser(mUser);
                // Save to database
                mDatabaseUserRef.child("preferredUnits").setValue(mPreferredUnits.toString());

                updateUI();
            }
        });

        mSignOutItem.setOnClickListener(new OnSignOutItemClickListener());
        mDailyGoalItem.setOnClickListener(new OnDailyTargetItemClickListener());
        mNotificationStateItem.setOnClickListener(new OnNotificationStateItemClickListener());
        mNotificationStartTimeItem.setOnClickListener(new OnNotificationStartTimeItemClickListener());
        mNotificationEndTimeItem.setOnClickListener(new OnNotificationEndTimeItemClickListener());
        mNotificationRepeatItem.setOnClickListener(new OnNotificationRepeatItemClickListener());

        updateUI();
    }

    private void updateUI() {
        final Locale locale = Utils.getLocale(getActivity());
        mDailyGoalTextView.setText(String.format(locale, DAILY_GOAL_FORMAT,
                Utils.getHydrationTarget(mUser, mPreferredUnits),
                Utils.getVolumeUnits(mPreferredUnits)));

        mNotificationState = mUser.notifications ? NotificationState.ON : NotificationState.OFF;
        mNotificationStateTextView.setText(mNotificationState.toString());
        mNotificationsOnContainer.setVisibility(mNotificationState == NotificationState.ON
                ? View.VISIBLE : View.GONE);

        final String notifStartTime = Utils.getPrintable12Hours(mUser.notificationsStartTime, locale);
        final String notifEndTime = Utils.getPrintable12Hours(mUser.notificationsEndTime, locale);
        final String notifRepeat = String.format(getString(R.string.every_x_hours), mUser.notificationsRepeat);

        mNotificationStartTimeTextView.setText(notifStartTime);
        mNotificationEndTimeTextView.setText(notifEndTime);
        mNotificationRepeatTextView.setText(notifRepeat);
    }

    private void goToLaunchActivity() {
        final Intent launchActivityIntent = new Intent(getActivity(), LaunchActivity.class);
        startActivity(launchActivityIntent);
        if (mUserSignedOutListener != null) {
            mUserSignedOutListener.onUserSignedOut();
        }
    }

    private class OnSignOutItemClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            final Context context = getActivity();
            if (FirebaseAuth.getInstance().getCurrentUser() != null && context != null) {
                AuthUI.getInstance()
                        .signOut(context)
                        .addOnCompleteListener(new OnSignOutCompleteListener());
            }
        }
    }

    private class OnDailyTargetItemClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            double dailyGoal = Utils.getHydrationTarget(mUser, mPreferredUnits);
            dailyGoal = Utils.getDoubleWithDecimalPlaces(dailyGoal, 1);

            final Bundle args = new Bundle();
            args.putString(Extras.EXTRA_HINT, getString(R.string.daily_goal));
            args.putString(Extras.EXTRA_TEXT, String.valueOf(dailyGoal));

            final SettingsEditTextDialogFragment settingsEditTextDialogFragment =
                    SettingsEditTextDialogFragment.createInstance(args, new SettingsEditTextDialogFragment.OnSettingsDialogPositiveButtonClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, String newValue) {
                            double newDailyGoal = Double.parseDouble(newValue);
                            if (mPreferredUnits == Units.METRIC) {
                                newDailyGoal = Utils.convertMlToFlOz(newDailyGoal);
                            }
                            mUser.hydrationDailyTarget = newDailyGoal;

                            // Save to internal memory store
                            MemoryStore.getInstance().setLoggedInUser(mUser);
                            // Save to database
                            mDatabaseUserRef.child("hydrationDailyTarget").setValue(newDailyGoal);

                            updateUI();
                        }
                    });
            settingsEditTextDialogFragment.show(getFragmentManager(), "daily_target_dialog");
        }
    }

    private class OnNotificationStateItemClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            mUser.notifications = !mUser.notifications;

            // Save to internal memory store
            MemoryStore.getInstance().setLoggedInUser(mUser);
            // Save to database
            mDatabaseUserRef.child("notifications").setValue(mUser.notifications);

            updateUI();

            // Start / Stop NotificationsService
            final Context context = getActivity();
            if (context != null) {
                if (mUser.notifications) {
                    Utils.startNotificationsService(context, mUser);
                } else {
                    Utils.stopNotificationsService(context);
                }
            }
        }
    }

    private class OnNotificationStartTimeItemClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            final Bundle args = new Bundle();
            args.putInt(Extras.EXTRA_SELECTED_ITEM, mUser.notificationsStartTime);

            SettingsHourSpinnerDialogFragment settingsHourSpinnerDialogFragment =
                    SettingsHourSpinnerDialogFragment.createInstance(args, new SettingsHourSpinnerDialogFragment.OnHourSpinnerDialogPositiveButtonClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int newValue) {
                            mUser.notificationsStartTime = newValue;
                            // Save to internal memory store
                            MemoryStore.getInstance().setLoggedInUser(mUser);
                            // Save to database
                            mDatabaseUserRef.child("notificationsStartTime").setValue(mUser.notificationsStartTime);
                            if (mUser.notifications) {
                                // Restart notifications service
                                Utils.restartNotificationsService(getActivity(), mUser);
                            }
                            updateUI();
                        }
                    });
            settingsHourSpinnerDialogFragment.show(getFragmentManager(), "notifications_start_time_dialog");
        }
    }

    private class OnNotificationEndTimeItemClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            final Bundle args = new Bundle();
            args.putInt(Extras.EXTRA_SELECTED_ITEM, mUser.notificationsEndTime);

            SettingsHourSpinnerDialogFragment settingsHourSpinnerDialogFragment =
                    SettingsHourSpinnerDialogFragment.createInstance(args, new SettingsHourSpinnerDialogFragment.OnHourSpinnerDialogPositiveButtonClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int newValue) {
                            mUser.notificationsEndTime = newValue;
                            // Save to internal memory store
                            MemoryStore.getInstance().setLoggedInUser(mUser);
                            // Save to database
                            mDatabaseUserRef.child("notificationsEndTime").setValue(mUser.notificationsEndTime);
                            if (mUser.notifications) {
                                // Restart notifications service
                                Utils.restartNotificationsService(getActivity(), mUser);
                            }
                            updateUI();
                        }
                    });
            settingsHourSpinnerDialogFragment.show(getFragmentManager(), "notifications_end_time_dialog");
        }
    }

    private class OnNotificationRepeatItemClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            final Bundle args = new Bundle();
            args.putString(Extras.EXTRA_HINT, getString(R.string.notification_frequency));
            args.putString(Extras.EXTRA_TEXT, String.valueOf(mUser.notificationsRepeat));

            final SettingsEditTextDialogFragment settingsEditTextDialogFragment =
                    SettingsEditTextDialogFragment.createInstance(args, new SettingsEditTextDialogFragment.OnSettingsDialogPositiveButtonClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, String newValue) {
                            try {
                                mUser.notificationsRepeat = Integer.parseInt(newValue);
                                // Save to internal memory store
                                MemoryStore.getInstance().setLoggedInUser(mUser);
                                // Save to database
                                mDatabaseUserRef.child("notificationsRepeat").setValue(mUser.notificationsRepeat);
                                if (mUser.notifications) {
                                    // Restart notifications service
                                    Utils.restartNotificationsService(getActivity(), mUser);
                                }
                                updateUI();
                            } catch (NumberFormatException e) {
                                Toast.makeText(getActivity(),
                                        R.string.notification_repeat_error,
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    });
            settingsEditTextDialogFragment.show(getFragmentManager(), "notifications_repeat_dialog");
        }
    }

    private class OnSignOutCompleteListener implements OnCompleteListener<Void> {
        @Override
        public void onComplete(@NonNull Task task) {
            goToLaunchActivity();
        }
    }
}
