package com.example.yelloclient.classes;

import com.yandex.mapkit.geometry.Point;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class GeocoderAnswer {

    public boolean isValid = false;

    public String mAddressLine;
    public String mStreet;
    public String mHouse;
    public String mName;
    public Point mPoint;
    public String mTitle;
    public String mSubTitle;

    public GeocoderAnswer(String s) {
        try {
            JSONObject jo = new JSONObject(s);
            Iterator<?> it = jo.keys();
            JSONArray com = (JSONArray) find(jo, "Components");
            if (com == null || com.length() == 0) {
                return;
            }
            JSONObject addr = (JSONObject) find(jo,"Address");
            if (addr != null) {
                mAddressLine = addr.getString("formatted");
            }
            for (int i = 0; i < com.length(); i++) {
                parseComponents(com.getJSONObject(i));
            }
            String jpoint = (String) find(jo, "pos");
            if (jpoint != null) {
                String[] ls = jpoint.split(" ");
                if (ls.length == 2) {
                    mPoint = new Point(Double.valueOf(ls[1]), Double.valueOf(ls[0]));
                }
            }
            isValid = true;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private Object find(JSONObject jo, String key) {
        try {
            Iterator<?> keys = jo.keys();
            while (keys.hasNext()) {
                String k = (String) keys.next();
                if (key.equals(k)) {
                    return jo.get(key);
                }

                if (jo.get(k) instanceof JSONObject) {
                    Object o = find((JSONObject) jo.get(k), key);
                    if (o != null) {
                        return o;
                    }
                }

                if (jo.get(k) instanceof JSONArray) {
                    JSONArray jar = (JSONArray) jo.get(k);
                    for (int i = 0; i < jar.length(); i++) {
                        JSONObject j = jar.getJSONObject(i);
                        Object o = find(j, key);
                        if (o != null) {
                            return o;
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    void parseComponents(JSONObject jo) throws JSONException {
        if (jo.getString("kind").toLowerCase().equals("street")) {
            mStreet = jo.getString("name");
        } else if (jo.getString("kind").toLowerCase().equals("house")) {
            mHouse = jo.getString("name");
        } else if (jo.getString("kind").toLowerCase().equals("airport")) {
            mName = jo.getString("name");
        } else if (jo.getString("kind").toLowerCase().equals("metro")) {
            mName = jo.getString("name");
        }
    }

    public String getShortAddress() {
        if (mStreet == null && mHouse == null) {
            return mName;
        }
        if (mStreet == null) {
            mStreet = "";
        }
        if (mHouse == null) {
            mHouse = "";
        }
        return mStreet + " " + mHouse;
    }
}