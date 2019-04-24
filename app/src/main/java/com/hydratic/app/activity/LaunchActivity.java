package com.hydratic.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseUiException;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hydratic.app.R;
import com.hydratic.app.model.User;
import com.hydratic.app.storage.MemoryStore;

import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;

import static com.hydratic.app.util.Constants.IMPERIAL_UNITS;

public class LaunchActivity extends AppCompatActivity {

    private static final int SIGN_IN_REQUEST_CODE = 200;

    @BindView(R.id.sign_in_description) View mSignInDescription;
    @BindView(R.id.sign_in_button) TextView mSignInButton;

    private DatabaseReference mUsersRef;
    private ValueEventListener mAddUserValueListener;
    private ValueEventListener mUserValueListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if (dataSnapshot.exists()) {
                final User user = dataSnapshot.getValue(User.class);
                // Save user in the internal memory storage and navigate to the MainActivity
                MemoryStore.getInstance().setLoggedInUser(user);
                gotToMainActivity();
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            // TODO
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        ButterKnife.bind(this);

        mSignInDescription.setVisibility(View.GONE);
        mSignInButton.setVisibility(View.GONE);

        mUsersRef = FirebaseDatabase.getInstance().getReference().child("users");

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            // Setup the "Sign In / Sign Up" section of the screen
            mSignInDescription.setVisibility(View.VISIBLE);
            mSignInButton.setVisibility(View.VISIBLE);
            mSignInButton.setOnClickListener(new OnSignInButtonClickListener());
        } else {
            // Get the user data from the database and store it locally
            final DatabaseReference currentUserRef = mUsersRef.child(user.getUid());
            currentUserRef.addValueEventListener(mUserValueListener);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            final DatabaseReference currentUserRef = mUsersRef.child(user.getUid());
            currentUserRef.removeEventListener(mUserValueListener);
            if (mAddUserValueListener != null) {
                currentUserRef.removeEventListener(mAddUserValueListener);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SIGN_IN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Successfully signed in
                mSignInDescription.setVisibility(View.GONE);
                mSignInButton.setVisibility(View.GONE);
                onAuthSuccess();
            } else {
                // Auth error
                IdpResponse response = IdpResponse.fromResultIntent(data);
                FirebaseUiException error = response != null ? response.getError() : null;
                onAuthFailed(error);
            }
        }
    }

    private void signIn() {
        // Authentication providers:
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());

        // Create and launch sign-in intent:
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setTheme(R.style.AppTheme)
                        .setLogo(R.drawable.drop)
                        .build(),
                SIGN_IN_REQUEST_CODE);
    }

    private void onAuthSuccess() {
        // For newly Sign Up users, make sure to store their details in the database
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            addNewUserIfNeeded(user.getUid(), user.getDisplayName(), user.getEmail());
        }
    }

    private void onAuthFailed(FirebaseUiException error) {
        // TODO
    }

    private void addNewUserIfNeeded(String userId, String displayName, String email) {
        final DatabaseReference currentUserRef = mUsersRef.child(userId);

        // Check if the user currently exists in the database
        mAddUserValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    // Remove the current ValueEventListener
                    currentUserRef.removeEventListener(this);

                    // Save user in the internal memory storage
                    final User user = new User(displayName, email, 118, IMPERIAL_UNITS);
                    MemoryStore.getInstance().setLoggedInUser(user);

                    // Add the user to the database if there is not entry present for the current userId.
                    mUsersRef.child(userId).setValue(user);

                    // Since this is a new user, go through the Onboarding Activity first
                    goToOnboardingActivity();
                } else {
                    // Save user in the internal memory storage
                    final User user = dataSnapshot.getValue(User.class);
                    MemoryStore.getInstance().setLoggedInUser(user);

                    // For existing users, go directly to the Main Activity
                    gotToMainActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // TODO
            }
        };

        currentUserRef.addValueEventListener(mAddUserValueListener);
    }

    private void goToOnboardingActivity() {
        final Intent onboardingActivityIntent = new Intent(LaunchActivity.this, OnboardingActivity.class);
        startActivity(onboardingActivityIntent);
        finish();
    }

    private void gotToMainActivity() {
        final Intent mainActivityIntent = new Intent(LaunchActivity.this, MainActivity.class);
        startActivity(mainActivityIntent);
        finish();
    }

    private class OnSignInButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            signIn();
        }
    }
}
