package com.hydratic.app.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.hydratic.app.R;
import com.hydratic.app.activity.LaunchActivity;
import com.hydratic.app.callback.UserSignedOutListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingsFragment extends Fragment {

    @BindView(R.id.sign_out_button) Button mSignOutButton;

    private UserSignedOutListener mUserSignedOutListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_settings, container, false);
        ButterKnife.bind(this, rootView);
        setupUI();
        return rootView;
    }

    public void setOnUserSignedOutListener(UserSignedOutListener userSignedOutListener) {
        mUserSignedOutListener = userSignedOutListener;
    }

    private void setupUI() {
        mSignOutButton.setOnClickListener(new OnSignOutButtonClickListener());
    }

    private void goToLaunchActivity() {
        final Intent launchActivityIntent = new Intent(getActivity(), LaunchActivity.class);
        startActivity(launchActivityIntent);
        if (mUserSignedOutListener != null) {
            mUserSignedOutListener.onUserSignedOut();
        }
    }

    private class OnSignOutButtonClickListener implements View.OnClickListener {
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

    private class OnSignOutCompleteListener implements OnCompleteListener<Void> {
        @Override
        public void onComplete(@NonNull Task task) {
            goToLaunchActivity();
        }
    }
}
