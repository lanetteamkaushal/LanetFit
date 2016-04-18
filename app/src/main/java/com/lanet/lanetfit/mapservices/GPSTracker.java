package com.lanet.lanetfit.mapservices;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.maps.model.LatLng;
import com.lanet.lanetfit.Utils;

public class GPSTracker extends Service implements LocationListener {
    private Context mContext;
    boolean GPSEnabled = false;
    boolean NetworkEnabled = false;
    boolean canGetLocation = false;
    Location location;
    double latitude, longitude;
    private static final String TAG = "GPSTracker";
    private static final long MIN_DISTANCE_FOR_UPDATES = 1;
    private static final long MIN_TIME_FOR_UPDATES = 1000 * 60;

    protected LocationManager locationManager;

    public GPSTracker(Context context) {
        this.mContext = context;
        getLocation();
    }

    public GPSTracker() {
    }

    public Location getLocation() {
        try {
            if (!Utils.checkFineLocationPermission(mContext)) {
                return null;
            }
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

            GPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            NetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!GPSEnabled && !NetworkEnabled) {
                //no network
            } else {
                this.canGetLocation = true;
                if (NetworkEnabled) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_FOR_UPDATES, MIN_DISTANCE_FOR_UPDATES, this);
                    Log.d("Network", "Network");
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }

                }
                if (GPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                MIN_TIME_FOR_UPDATES, MIN_DISTANCE_FOR_UPDATES, this);
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return location;
    }

    @Override
    public void onLocationChanged(Location location) {
//        Toast.makeText(GPSTracker.this, "Your location change :" + location.toString(), Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onLocationChanged() called with: " + "location = [" + location + "]");
        if (location != null) {
            Intent intent = new Intent(UtilityService.ACTION_LOCATION_UPDATED1);
            LatLng latLngLocation = new LatLng(location.getLatitude(), location.getLongitude());
            Utils.storeLocation(this, latLngLocation);
            intent.putExtra(
                    FusedLocationProviderApi.KEY_LOCATION_CHANGED, location);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.e(TAG, "onProviderDisabled: ");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.e(TAG, "onProviderEnabled: ");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(TAG, "onStatusChanged() called with: " + "provider = [" + provider + "], status = [" + status + "], extras = [" + extras + "]");
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    //functions
    public double getLatitude() {
        if (location != null) {
            latitude = location.getLatitude();
        }
        return latitude;
    }

    public double getLongitude() {
        if (location != null) {
            longitude = location.getLongitude();
        }
        return longitude;
    }

    public void stopUsingGPS() {
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.removeUpdates(GPSTracker.this);
        }
    }

    public boolean canGetLocation() {
        return this.canGetLocation;
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        alertDialog.setTitle("GPS Settings");
        alertDialog.setMessage("GPS is not enabled. Do you want to enable it in settings?");

        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
                dialog.cancel();
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }


}