package com.example.yelloclient;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class TaxiService extends Service {

    private final static String CHANNEL_ID = "TAXISERVICECLIENT";
    private final static int NOTIFICATION_ID = 1;

    @Override
    public void onCreate() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_LOW);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(mChannel);
            }
            startForegroundService(new Intent(TaxiService.this, TaxiService.class));
            startForeground(NOTIFICATION_ID, new Notification.Builder(getApplicationContext(),CHANNEL_ID).build());
        } else {
            startService(new Intent(TaxiService.this, TaxiService.class));
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
