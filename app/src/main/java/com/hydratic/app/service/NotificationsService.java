package com.hydratic.app.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.hydratic.app.R;
import com.hydratic.app.activity.LaunchActivity;
import com.hydratic.app.util.Constants.Extras;

import java.util.Calendar;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import static com.hydratic.app.util.Constants.HYDRATION_NOTIFICATION;

public class NotificationsService extends IntentService {

    private AlarmManager mAlarm;

    public NotificationsService() {
        super(NotificationsService.class.getSimpleName());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mAlarm = (AlarmManager) getSystemService(ALARM_SERVICE);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null) {
            return;
        }

        boolean forceStop = false;
        boolean isInitialCall = false;
        int startTime = -1;
        int endTime = -1;
        int repeat = 0;

        final Bundle extras = intent.getExtras();
        if (extras != null) {
            forceStop = extras.getBoolean(Extras.EXTRA_FORCE_STOP);
            isInitialCall = extras.getBoolean(Extras.EXTRA_INITIAL_CALL);
            startTime = extras.getInt(Extras.EXTRA_START_TIME);
            endTime = extras.getInt(Extras.EXTRA_END_TIME);
            repeat = extras.getInt(Extras.EXTRA_REPEAT);
        }

        final Intent repeatIntent = new Intent(this, NotificationsService.class);
        repeatIntent.putExtra(Extras.EXTRA_INITIAL_CALL, false);
        repeatIntent.putExtra(Extras.EXTRA_START_TIME, startTime);
        repeatIntent.putExtra(Extras.EXTRA_END_TIME, endTime);
        repeatIntent.putExtra(Extras.EXTRA_REPEAT, repeat);
        final PendingIntent pendingIntent = PendingIntent.getService(this, 0, repeatIntent, 0);

        if (forceStop) {
            mAlarm.cancel(pendingIntent);
        } else {
            // Do not send notifications if the service has just been turned on
            if (!isInitialCall) {
                setupNotifications(startTime, endTime);
            }

            // Set the Alarm manager to trigger every 'repeat' hours
            mAlarm.set(AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + (1000 * 60 * 60 * repeat), pendingIntent);
        }
    }

    private void setupNotifications(int startTime, int endTime) {
        final Calendar cal = Calendar.getInstance();
        final int hourOfDay = cal.get(Calendar.HOUR_OF_DAY);
        if (hourOfDay >= startTime && hourOfDay <= endTime) {

            final Intent intent = new Intent(getApplicationContext(), LaunchActivity.class);
            PendingIntent contentIntent = PendingIntent.getActivity(
                    getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
                    getApplicationContext(), HYDRATION_NOTIFICATION);

            notificationBuilder.setAutoCancel(true)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setTicker(getString(R.string.app_name))
                    .setContentTitle(getString(R.string.get_hydrated))
                    .setContentText(getString(R.string.do_not_forget_to_hydrate))
                    .setDefaults(Notification.DEFAULT_LIGHTS| Notification.DEFAULT_SOUND)
                    .setContentIntent(contentIntent)
                    .setContentInfo(getString(R.string.info));


            final NotificationManager notificationManager = (NotificationManager) getApplication().getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.notify(1, notificationBuilder.build());
            }
        }
    }
}
