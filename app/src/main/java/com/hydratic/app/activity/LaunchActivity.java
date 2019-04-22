package com.hydratic.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.hydratic.app.R;

import java.util.Arrays;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;

public class LaunchActivity extends AppCompatActivity {

    private static final int SIGN_IN_REQUEST_CODE = 200;

    @BindView(R.id.sign_in_description) View mSignInDescription;
    @BindView(R.id.sign_in_button) TextView mSignInButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        ButterKnife.bind(this);


        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            mSignInDescription.setVisibility(View.VISIBLE);
            mSignInButton.setVisibility(View.VISIBLE);
            mSignInButton.setOnClickListener(new OnSignInButtonClickListener());
        } else {
            gotToMainActivity();
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
                gotToMainActivity();
            } else {
                // TODO: handle sign-in error
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
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
