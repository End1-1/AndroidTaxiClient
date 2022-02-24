package com.example.yelloclient;

import com.yandex.mapkit.MapKit;
import com.yandex.mapkit.MapKitFactory;

import java.util.Random;

public class Config {

    public static final int StateNone = 1;
    public static final int StatePendingSearch = 2;
    public static final int StateDriverAccept = 3;
    public static final int StateDriverOnWay = 4;
    public static final int StateDriverOnPlace = 5;
    public static final int StateDriverOrderStarted = 6;
    public static final int StateDriverOrderEnd = 7;

    public static final int ACTION_OPEN_WEBSOCKET = 1;

    public static String host() {
        //return "newyellowtaxi.com";
        return "192.168.0.111";
    }

    public static String bearerKey() {
        return Preference.getString("bearer_key");
    }

    public static void setBearerKey(String key) {
        Preference.setString("bearer_key", key);
    }

    public static String yandexGeocodeKey() {
        return Preference.getString("yandex_geocode_key");
    }

    public static void setYandexGeocodeKey(String key) {
        Preference.setString("yandex_geocode_key", key);
    }

    public static String mapkitKey() {
        return "06495363-2976-4cbb-a0b7-f09387554b9d";
    }

    public static String channelName() {
        return String.format("private-client-api-base.%d.%s", Preference.getInt("client_id"),
                Preference.getString("phone")
                        .replace("+", "")
                        .replace("-", "")
                        .replace(")", "")
                        .replace("(", ""));
    }

    public static String socketId() {
        Random _random = new Random();
        int min = 10000000, max = 99999999;
        int  n1 = min + _random.nextInt(max - min);
        min = 100000000;
        max = 999999999;
        int  n2 = min + _random.nextInt(max - min);
        return String.format("%d.%d", n1, n2);
    }
}
