package energigas.apps.systemstrategy.energigas;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import energigas.apps.systemstrategy.energigas.interfaces.OnLocationListener;

/**
 * Created by kelvi on 15/08/2016.
 */

public class LocationVehiculeListener implements LocationListener {

    private static final String TAG = "LocationVehiculeListener";
    private OnLocationListener onLocationListener;

    private Context context;
    protected LocationManager locationManager;
    int i = 0;
    Long minTime;
    Long minDistance;

    public LocationVehiculeListener(OnLocationListener onLocationListener, Long minTime, Long minDistance) {
        this.onLocationListener = onLocationListener;
        context = onLocationListener.getContextActivity();
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        this.minTime = minTime;
        this.minDistance = minDistance;
        Log.d("GPS Disabled", "GPS Disabled");
        // TODO check is network/gps is enabled and display the system settings
        // see
        // https://github.com/unchiujar/Umbra/blob/master/src/main/java/org/unchiujar/umbra/activities/FogOfExplore.java#L599
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            Log.d("GPS Disabled", "GPS Disabled");
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                minTime, minDistance, this);


        Log.d("GPS Enabled", "GPS Enabled");
        if (locationManager != null) {
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation != null) {
                onLocationListener.setLatAndLong(lastKnownLocation);
            }
        }


    }


    public void stopLocationUpdates() {

        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

                return;
            }
            locationManager.removeUpdates(LocationVehiculeListener.this);
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged");
        onLocationListener.setLatAndLong(location);
        if (i < 3) {
           // Toast.makeText(context, "Latitude: " + location.getLatitude(), Toast.LENGTH_SHORT).show();
        }
        i++;
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        Log.d(TAG, "onStatusChanged");
    }

    @Override
    public void onProviderEnabled(String s) {
        Log.d(TAG, "onProviderEnabled: " + s);
    }

    @Override
    public void onProviderDisabled(String s) {
        Log.d(TAG, "onProviderDisabled: " + s);
    }
}
