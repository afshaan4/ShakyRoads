package com.example.potato.shakyroads;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.widget.TextView;
import android.Manifest;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;

import static java.lang.String.valueOf;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SensorEventListener, LocationListener {


    private SensorManager mSensorManager;
    private LocationManager mLocationManager;
    private Sensor mAccelerometer;
    private Context mContext;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "This button does nothing", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // get instances of SensorManager
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // get and instance of the linear motion accelerometer
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        // get an instance of LocationManager
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }


    /* when the activity resumes */
    @Override
    protected void onResume() {
        super.onResume();

        // Start the reading from the accelerometer
        startAccelerometer();
        // start getting location updates
        startLocation();
    }

    /* when the activity pauses */
    @Override
    protected void onPause() {
        super.onPause();

        // When the activity is paused, we make sure to release our sensor resources
        stopAccelerometer();
        // stop the location listeners
        stopLocation();
    }


    /*
    Display functions for testing.
     */
    /* displays the reading */
    public void displayAcceleration(double acceleration) {
        TextView display = (TextView)findViewById(R.id.reading);
        display.setText(valueOf(acceleration));
    }

    /* displays the longitude */
    public void displayLong(double location) {
        TextView display = (TextView)findViewById(R.id.longitude);
        display.setText(valueOf(location));
    }

    /* displays the latitude */
    public void displayLat(double location) {
        TextView display = (TextView)findViewById(R.id.latitude);
        display.setText(valueOf(location));
    }

    /* gets the gps permission if we don't have it, if it has the permission: start the GPS */
    public void startGPS(View view) {
        checkLocationPermission();
        startLocation();
    }


    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Writes the readings to a csv file */
    public void saveData(double lat, double lng, double acc) {
        if (isExternalStorageWritable() == true) {
            try {
                // check if the csv file exists, and make one if it doesn't
                String fileName = "shakyroads.csv";
                File file = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOCUMENTS), fileName);
                if(!file.exists()) {
                    file = new File(Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DOCUMENTS), fileName);
                }
                if (!file.mkdirs()) {
                    Log.e("saveData", "Directory not created");
                    file.mkdirs();
                    // TODO make the directory instead of just complaining.
                }

                CSVWriter writer = new CSVWriter(
                        new OutputStreamWriter(new FileOutputStream(fileName),
                                StandardCharsets.UTF_8), ',', '"', '"', "\n");

                // convert the data to strings
                String latitude = String.valueOf(lat);
                String longitude = String.valueOf(lng);
                String acceleration = String.valueOf(acc);
                // then to an array of strings
                String[] entries = new String[] {latitude, longitude, acceleration};

                // then write it
                writer.writeNext(entries);
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



    ////////////////////////////////////////////////////////////////////////////////////////////////
    // The accelerometer stuff
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /* start the sensor listener */
    public void startAccelerometer() {
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    /* stop the sensor listener */
    public void stopAccelerometer() {
        mSensorManager.unregisterListener(this);
    }

    /* called whenever the accelerometer picks up any movement */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType()==Sensor.TYPE_LINEAR_ACCELERATION) {
            double x = event.values[0];
            double y = event.values[1];
            double z = event.values[2];

            displayAcceleration(y);

            saveData(0, 0, y);
        }
    }

    // leave this here, it's not used
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // The GPS stuff
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /*
    This checks if the app has the permissions to use the GPS, if the app doesn't have
    the permissions then it shows the user a dialog asking for the permission.
    Returns true or false depending on whether it has the permission or not
     */
    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown, used to be MainActivity.this
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    /*
    Checks if the app got permission to use the GPS, if it did get the permission then it
    calls startLocation(). startLocation() has a check as well to see if the app has the permission
    to use the GPS.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, do the location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        //Request location updates:
                        startLocation();
                    }

                } else {

                    // TODO: disable the thing that depends on this permission.
                }
                return;
            }
        }
    }


    /* Register the listener with the Location Manager to receive location updates */
    public void startLocation() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }
    }

    /* Remove the location listener to stop receiving location updates */
    public void stopLocation() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            mLocationManager.removeUpdates(this);
        }

    }

    /* Called when location changes */
    @Override
    public void onLocationChanged(Location location) {
        double latitude = (double) (location.getLatitude());
        double longitude = (double) (location.getLongitude());

        displayLong(longitude);
        displayLat(latitude);
    }

    // these are not used, leave them here
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onProviderDisabled(String provider) {}



    ////////////////////////////////////////////////////////////////////////////////////////////////
    //The UI functions and handlers
    ////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}