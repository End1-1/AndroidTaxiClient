package com.example.yelloclient.classes;

import android.content.Context;
import android.content.Intent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.yelloclient.Config;
import com.example.yelloclient.TaxiApp;

public class Messanger {

    public static final String IntentId = "event_listener";
    public static final int MSG_SOCKET_CONNECTION = 1;
    public static final int MSG_WEBSOCKET_MESSAGE = 2;

    private Intent mIntent;

    public Messanger() {
        mIntent = new Intent(IntentId);
    }

    public static Messanger create(int message) {
        Messanger m = new Messanger();
        m.putExtra("event", message);
        return m;
    }

    public Messanger putExtra(String name, int value) {
        mIntent.putExtra(name, value);
        return this;
    }

    public Messanger putExtra(String name, String value) {
        mIntent.putExtra(name, value);
        return this;
    }

    public Messanger putExtra(String name, boolean value) {
        mIntent.putExtra(name, value);
        return this;
    }

    public void broadcast() {
        LocalBroadcastManager.getInstance(TaxiApp.getContext()).sendBroadcast(mIntent);
    }
}
