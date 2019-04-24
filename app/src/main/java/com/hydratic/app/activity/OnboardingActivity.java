package com.hydratic.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.hydratic.app.R;
import com.hydratic.app.model.ActivityLevel;
import com.hydratic.app.model.Gender;
import com.hydratic.app.model.Units;

import java.math.BigDecimal;
import java.math.RoundingMode;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;

import static com.hydratic.app.util.Constants.EXTRA_DAILY_ACTIVITY_WATER_AMOUNT_OZ;
import static com.hydratic.app.util.Constants.HYDRATION_LEVEL_DEFAULT_FEMALE;
import static com.hydratic.app.util.Constants.HYDRATION_LEVEL_DEFAULT_MALE;
import static com.hydratic.app.util.Constants.HYDRATION_LEVEL_FACTOR;
import static com.hydratic.app.util.Constants.KG_TO_LB_CONVERSION_FACTOR;

public class OnboardingActivity extends AppCompatActivity {

    @BindView(R.id.weight) TextView mWeightTextView;
    @BindView(R.id.measuring_units) RadioGroup mMeasuringUnits;
    @BindView(R.id.gender) Spinner mGenderSpinner;
    @BindView(R.id.activity_level) Spinner mActivityLevelSpinner;
    @BindView(R.id.button_next) Button mButtonNext;

    private Units mUnits;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);
        ButterKnife.bind(this);

        setupUI();
    }

    private void setupUI() {
        mMeasuringUnits.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.imperial:
                        mUnits = Units.IMPERIAL;
                        mWeightTextView.setHint(R.string.weight_lb);
                        break;
                    case R.id.metric:
                        mUnits = Units.METRIC;
                        mWeightTextView.setHint(R.string.weight_kg);
                        break;
                }
            }
        });

        final ArrayAdapter genderArrayAdapter = ArrayAdapter.createFromResource(
                this, R.array.genders, R.layout.onboarding_spinner_item);
        genderArrayAdapter.setDropDownViewResource(R.layout.onboarding_spinner_item);
        mGenderSpinner.setAdapter(genderArrayAdapter);
        mGenderSpinner.setSelection(0);

        final ArrayAdapter activityLevelArrayAdapter = ArrayAdapter.createFromResource(
                this, R.array.activity_levels, R.layout.onboarding_spinner_item);
        activityLevelArrayAdapter.setDropDownViewResource(R.layout.onboarding_spinner_item);
        mActivityLevelSpinner.setAdapter(activityLevelArrayAdapter);
        mActivityLevelSpinner.setSelection(1);

        mButtonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Gender gender = Gender.FEMALE;
                switch (mGenderSpinner.getSelectedItemPosition()) {
                    case 0:
                        gender = Gender.FEMALE;
                        break;
                    case 1:
                        gender = Gender.MALE;
                        break;
                }

                ActivityLevel activityLevel = ActivityLevel.MEDIUM;
                switch (mActivityLevelSpinner.getSelectedItemPosition()) {
                    case 0:
                        activityLevel = ActivityLevel.LOW;
                        break;
                    case 1:
                        activityLevel = ActivityLevel.MEDIUM;
                        break;
                    case 2:
                        activityLevel = ActivityLevel.HIGH;
                        break;
                }

                double dailyAmount = computeDailyAmount(gender, activityLevel);

                String weightString = mWeightTextView.getText().toString();
                if (!weightString.isEmpty()) {
                    double weight = Double.valueOf(weightString);
                    if (mUnits == Units.METRIC) {
                        weight = convertKgsToLbs(weight);
                    }
                    dailyAmount = computeDailyAmount(weight, activityLevel);
                }

                // Round value to only 1 decimal place
                dailyAmount = getDoubleWithDecimalPlaces(dailyAmount, 1);

                // Save to DB
                saveDailyAmountToDb(dailyAmount);
            }
        });
    }

    private void saveDailyAmountToDb(double dailyAmount) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseDatabase.getInstance().getReference()
                    .child("users")
                    .child(user.getUid())
                    .child("hydrationDailyTarget")
                    .setValue(dailyAmount).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    gotToMainActivity();
                }
            });
        }
    }

    private void gotToMainActivity() {
        final Intent mainActivityIntent = new Intent(OnboardingActivity.this, MainActivity.class);
        startActivity(mainActivityIntent);
        finish();
    }

    private static double computeDailyAmount(double weightInLb, ActivityLevel activityLevel) {
        double exerciseExtraAmount = activityLevel == ActivityLevel.MEDIUM
                ? EXTRA_DAILY_ACTIVITY_WATER_AMOUNT_OZ
                : (activityLevel == ActivityLevel.HIGH ? EXTRA_DAILY_ACTIVITY_WATER_AMOUNT_OZ * 2 : 0);

        return weightInLb * HYDRATION_LEVEL_FACTOR + exerciseExtraAmount;
    }

    private static double computeDailyAmount(Gender gender, ActivityLevel activityLevel) {
        double hydrationAmount = gender == Gender.MALE
                ? HYDRATION_LEVEL_DEFAULT_MALE
                : HYDRATION_LEVEL_DEFAULT_FEMALE;
        double exerciseExtraAmount = activityLevel == ActivityLevel.MEDIUM
                ? EXTRA_DAILY_ACTIVITY_WATER_AMOUNT_OZ
                : (activityLevel == ActivityLevel.HIGH ? EXTRA_DAILY_ACTIVITY_WATER_AMOUNT_OZ * 2 : 0);

        return hydrationAmount + exerciseExtraAmount;
    }

    private static double getDoubleWithDecimalPlaces(double doubleValue, int decimalPlaces) {
        return new BigDecimal(doubleValue).setScale(decimalPlaces, RoundingMode.HALF_UP).doubleValue();
    }

    private static double convertKgsToLbs(double amountInKgs) {
        return amountInKgs / KG_TO_LB_CONVERSION_FACTOR;
    }
}
