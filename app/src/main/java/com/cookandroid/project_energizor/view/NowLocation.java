package com.cookandroid.project_energizor.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.GoogleMap;

import java.util.concurrent.Executor;

public class NowLocation extends AppCompatActivity {
    private static GoogleMap map;
    public static double laitu;
    public static double longit;
    //현재위치를 좌표로 받아오는 함수
    @SuppressLint("MissingPermission")
    public void getNowLocation() {
        LocationServices.getFusedLocationProviderClient(getApplicationContext()).getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 18));
                            laitu =  location.getLatitude();
                            longit = location.getLongitude();
                        }
                    }
                });
    }

    public double getLaitu(){
        return laitu;
    }

    public double getLongit(){
        return longit;
    }

}
