package com.example.yelloclient;

import com.yandex.mapkit.MapKit;
import com.yandex.mapkit.MapKitFactory;

public class Config {

    public final int StateNone = 1;
    public final int State_pending_search = 2;
    public final int StateDriverAccept = 3;
    public final int StateDriverOnway = 4;
    public final int StateDriverOnplace = 5;
    public final int StateDriverOrderstarted = 6;
    public final int StateDriverOrderend = 7;

    public static String host() {
        return "newyellowtaxi.com";
        //return "192.168.0.21";
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
}
