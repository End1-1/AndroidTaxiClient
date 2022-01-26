package com.example.yelloclient;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.yelloclient.classes.Messanger;
import com.example.yelloclient.classes.SocketThread;

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

        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcasting, new IntentFilter(Messanger.IntentId));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    BroadcastReceiver mBroadcasting = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getIntExtra("event", 0)) {
                case Messanger.MSG_SOCKET_CONNECTION:
                    if (intent.getBooleanExtra("openconnection", false)) {
                        new Thread(new SocketThread()).start();
                    }
                    break;
            }
        }
    };
}
