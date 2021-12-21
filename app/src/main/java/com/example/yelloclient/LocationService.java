package com.example.yelloclient;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import java.util.function.Consumer;

public class LocationService {

    public interface LocationChangeListener {
        public void location(Location l);
    }

    @SuppressLint("MissingPermission")
    public static void getSingleLocation(LocationChangeListener locationListener) {
        LocationManager locationManager = (LocationManager)
                TaxiApp.getContext().getSystemService(Context.LOCATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            locationManager.getCurrentLocation(LocationManager.NETWORK_PROVIDER, null, TaxiApp.getContext().getMainExecutor(), new Consumer<Location>() {
                @Override
                public void accept(Location location) {
                    locationListener.location(location);
                }
            });
        } else {
            locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, location -> locationListener.location(location), null);
        }
    }


    private boolean checkGPS() {
        final LocationManager manager = (LocationManager) TaxiApp.getContext().getSystemService( Context.LOCATION_SERVICE );
        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
        }
        return false;
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(TaxiApp.getContext());
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        TaxiApp.getContext().startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }
}
