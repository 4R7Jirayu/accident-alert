package jp4r7.sendsms;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

import static android.content.Context.LOCATION_SERVICE;

/**
 * Created by 4R7 on 27/11/2560.
 */

public class GetLocation extends Activity implements LocationListener {
    Context context;
    LocationManager mLocationManager;

    SharedPreferences dataLoca;
    SharedPreferences settings;

    String log;
    String lat;


    public GetLocation(Context context) {
        this.context = context;
    }

    private Location getLastKnownLocation() {
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return bestLocation;
            }
            Location l = mLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        onLocationChanged(bestLocation);
        return bestLocation;
    }


    @Override
    public void onLocationChanged(Location location) {
        String Loca = "Log:" + String.valueOf(location.getLongitude() +
                ", Lat:" + String.valueOf(location.getLatitude()));
        // Toast.makeText(context,Loca,Toast.LENGTH_SHORT).show();

        dataLoca = context.getSharedPreferences("location", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = dataLoca.edit();
        editor.putString("log", String.format("%.4f",String.valueOf(location.getLongitude())));
        editor.putString("lat", String.format("%.4f",String.valueOf(location.getLatitude())));
        editor.apply();


        setLog(String.valueOf(location.getLongitude()));
        setLat(String.valueOf(location.getLatitude()));


        Log.d("log", " " + getLog());
        Log.d("lat", " " + getLat());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(context, "GPS is Enabled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(context, "GPS is Disabled", Toast.LENGTH_SHORT).show();
    }


    public void setLog(String log) {
        this.log = log;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLog() {
        if (log == null) {
            return "0";
        }
        return log;
    }

    public String getLat() {
        return lat;
    }
}
