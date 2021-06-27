package com.example.licenta;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.content.Intent;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomerMapActivity extends FragmentActivity implements OnMapReadyCallback, AdapterView.OnItemSelectedListener {

    private GoogleMap mMap;
    private Button Logout, showDriver;
    private Spinner lineCustomer;
    public String selectedLine;
    String LOG = "intrare";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        selectedLine = "no line selected";
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map1);
        mapFragment.getMapAsync(this);

        Logout = (Button) findViewById(R.id.logoutuser);
        Logout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(CustomerMapActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        });

        lineCustomer = (Spinner) findViewById(R.id.lineCustomer);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.line,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        lineCustomer.setAdapter(adapter);
        lineCustomer.setOnItemSelectedListener(this);
        Log.d(LOG, selectedLine);

        showDriver = (Button) findViewById(R.id.showDriverButton);
        showDriver.setOnClickListener(v -> {
            mMap.clear();
            if(selectedLine.equals("Choose a line...")){
                Toast.makeText(CustomerMapActivity.this, "Please select a line!", Toast.LENGTH_SHORT).show();
            }else {
                Log.d(LOG,selectedLine);
                displayDrivers();
            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(false);
        /*mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);*/
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                new android.app.AlertDialog.Builder(this)
                        .setTitle("Location permission request")
                        .setMessage("Do you allow STB Driver app to access your location?")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(CustomerMapActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                        })
                        .create()
                        .show();
            }
            else{
                ActivityCompat.requestPermissions(CustomerMapActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
            return;
        }
        googleMap.setMyLocationEnabled(true);
            statusCheck();

            Criteria criteria = new Criteria();
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            String provider = locationManager.getBestProvider(criteria, false);
            Location location = locationManager.getLastKnownLocation(provider);
        if (location!=null){
            double lat = location.getLatitude();
            double lng = location.getLongitude();
            LatLng coordinate = new LatLng(lat, lng);
            CameraUpdate myLocation = CameraUpdateFactory.newLatLngZoom(coordinate, 15);
            mMap.animateCamera(myLocation);
        }
        else{ //center the map on Bucharest
            double lat = 44.439663;
            double lng = 26.096306;
            LatLng coordinate = new LatLng(lat, lng);
            CameraUpdate myLocation = CameraUpdateFactory.newLatLngZoom(coordinate, 11);
            mMap.animateCamera(myLocation);
        }


    }
    public void statusCheck() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();

        }
    }
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("For a better experience, please turn on device location, which uses Google's location service.")
                .setCancelable(false)
                .setPositiveButton("Go to location settings", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(parent.getItemAtPosition(position).equals("Pick a line...")){
            //do nothing
        }
        markerList = new ArrayList<Marker>();
        selectedLine = parent.getItemAtPosition(position).toString();
        //Toast.makeText(parent.getContext(),text,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void displayDrivers() {
        Log.d(LOG,selectedLine);
        DatabaseReference driversLocation = FirebaseDatabase.getInstance().getReference().child("driversAvailable").child(selectedLine);
        driversLocation.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    //Log.d(LOG,"apel");
                    getDrivers();
                    /*int sizek = driversKeysMap.size();
                    Log.d(LOG,String.valueOf(sizek));*/
                   // mapDrivers();
                } else {
                    Toast.makeText(CustomerMapActivity.this, "No drivers available on line " + selectedLine + "!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private int radius = 15;
    private Boolean driversFound = false;
    List<Marker> markerList;
    private void getDrivers(){
       DatabaseReference driversLocation = FirebaseDatabase.getInstance().getReference().child("driversAvailable").child(selectedLine);

       GeoFire geoFire = new GeoFire(driversLocation);
       double lat = 44.439663;  //lat bucuresti
       double lng = 26.096306; //lng bucuresti
       LatLng centerLocation = new LatLng(lat,lng);

       GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(centerLocation.latitude,centerLocation.latitude),radius);
       //geoQuery.removeAllListeners();
       geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
           @Override
           public void onKeyEntered(String key, GeoLocation location) {//anytime a driver is found within the radius, onKeyEntered is called, and we get the key(driver id) and the location(from db)
               Log.d(LOG,String.valueOf(location.latitude));
               Log.d(LOG,String.valueOf(location.longitude));
               Log.d(LOG,key);
               Log.d(LOG,"found driver!");
               for(Marker markerIterator : markerList){ //see if there is a marker that contains the id that we already added in the list
                   Log.d(LOG,key);
                   if(markerIterator.getTag().equals(key)){
                       return; //if found, return
                   }
               }

               LatLng driverLocation = new LatLng(location.latitude, location.longitude);

               Marker driverMarker = mMap.addMarker(new MarkerOptions().position(driverLocation).title(selectedLine+" STB Driver").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher_mapbus)));
               driverMarker.setTag(key);
               Log.d(LOG,String.valueOf(location.latitude));
               Log.d(LOG,String.valueOf(location.longitude));
               Log.d(LOG,key);
               markerList.add(driverMarker);

                //if(!driversFound) {
               driversFound = true;

              /* Log.d(LOG,String.valueOf(location.latitude));
               Log.d(LOG,String.valueOf(location.longitude));
               Log.d(LOG,key);*/

               /*mMap.addMarker(new MarkerOptions()
                       .position(new LatLng(location.latitude, location.longitude))
                       //.icon(BitmapDescriptorFactory.fromResource(R.drawable.stb))
                       .title(selectedLine + " driver"));

               /*DatabaseReference dbTest = FirebaseDatabase.getInstance().getReference().child("driversAvailable").child(selectedLine).child(key).child("l").child("0");
               dbTest.addListenerForSingleValueEvent(new ValueEventListener() {
                   @Override
                   public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                       if(dataSnapshot.exists()){
                           String lat;
                           lat = dataSnapshot.getValue().toString();
                           Log.d(LOG,lat);
                       }
                   }

                   @Override
                   public void onCancelled(@NonNull DatabaseError databaseError) {

                   }
               });*/

               //driversLocations.put(i,location);

                //}
           }

           @Override
           public void onKeyExited(String key) { //whenever a driver stops working, stop showing him
               for(Marker markerIterator : markerList){ //see if there is a marker that contains the id that we already added in the list
                   if(markerIterator.getTag().equals(key)){
                        markerIterator.remove();
                        markerList.remove(markerIterator);
                        return;
                   }
               }
           }

           @Override
           public void onKeyMoved(String key, GeoLocation location) { //get the movement of the driver; if the driver moves, move his marker
               for(Marker markerIterator : markerList){ //see if there is a marker that contains the id that we already added in the list
                   if(markerIterator.getTag().equals(key)){
                       markerIterator.setPosition(new LatLng(location.latitude, location.longitude));
                   }
               }
           }

           @Override
           public void onGeoQueryReady() { //we know that the query has finished (every driver within the radius has been found) when onGeoQueryReady is called
               if(!driversFound){
                    radius = radius *2;
                    getDrivers();
                }
           }

           @Override
           public void onGeoQueryError(DatabaseError error) {

           }
       });
    }
}

