package com.example.licenta;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.firebase.geofire.GeoFire;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ServiceConfigurationError;

public class AppKilled extends Service {
    BroadcastReceiver broadcastReceiver;
    final String LOG = "hatz";
    public String line;
    @Override
    public void onCreate() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equalsIgnoreCase("getting_data")) {
                    line = intent.getStringExtra("value");
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        // set the custom action
        intentFilter.addAction("getting_data"); //Action is just a string used to identify the receiver as there can be many in your app so it helps deciding which receiver should receive the intent.
        // register the receiver
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(broadcastReceiver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        String userId1 = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d(LOG,line);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("driversAvailable").child(line);

        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId1);
    }
}
