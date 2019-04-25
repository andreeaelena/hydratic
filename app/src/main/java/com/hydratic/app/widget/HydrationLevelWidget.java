package com.hydratic.app.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.RemoteViews;

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
import com.hydratic.app.util.Utils;

import java.util.Locale;

import androidx.annotation.NonNull;

/**
 * Implementation of App Widget functionality.
 */
public class HydrationLevelWidget extends AppWidgetProvider {

    private User mUser;
    private DatabaseReference mUserReference;
    private DatabaseReference mDrinkLogReference;
    private ValueEventListener mUserValueListener;
    private ValueEventListener mDrinkLogValueListener;

    public HydrationLevelWidget() {
        final FirebaseUser frebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (frebaseUser != null) {
            mUserReference = FirebaseDatabase.getInstance().getReference()
                    .child("users")
                    .child(frebaseUser.getUid());
            mDrinkLogReference = mUserReference.child("drinkLog");
        }
    }

    public void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        // Construct the RemoteViews object
        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.hydration_level_widget);

        mUserValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mUser = dataSnapshot.getValue(User.class);

                    // Set the ValueEventListener for retrieving the drinkLog for the current user
                    mDrinkLogReference.addValueEventListener(mDrinkLogValueListener);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                views.setViewVisibility(R.id.hydration_level_container, View.GONE);
                views.setViewVisibility(R.id.error_view, View.VISIBLE);
            }
        };

        // Set the ValueEventListener for retrieving the current user data from the db
        mUserReference.addValueEventListener(mUserValueListener);

        mDrinkLogValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (mUser != null && dataSnapshot.exists()) {
                    int hydrationAmount = 0;
                    for(DataSnapshot drinkLogSnapshot: dataSnapshot.getChildren()) {
                        if (drinkLogSnapshot.exists()) {
                            String key = drinkLogSnapshot.getKey();
                            if (key != null && !key.isEmpty()) {
                                long timestamp = Long.valueOf(drinkLogSnapshot.getKey());
                                if (DateUtils.isToday(timestamp)) {
                                    final DrinkLog drinkLog = drinkLogSnapshot.getValue(DrinkLog.class);
                                    hydrationAmount += drinkLog != null ? drinkLog.amount : 0;
                                }
                            }
                        }
                    }

                    // Compute hydration percentage
                    final double hydrationPercentage = ((double) hydrationAmount / mUser.hydrationDailyTarget) * 100;
                    final Locale locale = Utils.getLocale(context);
                    final String formattedPercentage = String.format(locale, "%.0f%%", hydrationPercentage);

                    // Set the hydration level
                    views.setTextViewText(R.id.hydration_level, formattedPercentage);

                    // Instruct the widget manager to update the widget
                    appWidgetManager.updateAppWidget(appWidgetId, views);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                views.setViewVisibility(R.id.hydration_level_container, View.GONE);
                views.setViewVisibility(R.id.error_view, View.VISIBLE);
            }
        };
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        mUserReference.removeEventListener(mUserValueListener);
        mDrinkLogReference.removeEventListener(mDrinkLogValueListener);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String intentAction = intent.getAction();
        if (intentAction != null) {
            final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            final int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, HydrationLevelWidget.class));
            for (int appWidgetId : appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId);
            }
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}
