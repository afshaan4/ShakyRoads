package com.example.potato.shakyroads;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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

import static java.lang.String.valueOf;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    ///////////////////////////
    // variables for this class
    ///////////////////////////
    private AccelerometerView mAccelerometerView;
    private LocationView mLocationView;
    private SensorManager mSensorManager;
    private LocationManager mLocationManager;


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

        // get an instance of LocationManager
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // instantiate the accelerometer view
        mAccelerometerView = new AccelerometerView(this);

        // instantiate the location view
        mLocationView = new LocationView(this);
    }


    // when the activity resumes
    @Override
    protected void onResume() {
        super.onResume();

        // Start the reading from the accelerometer
        mAccelerometerView.startAccelerometer();
        // start getting location updates
        mLocationView.startLocation();
    }

    // when the activity pauses
    @Override
    protected void onPause() {
        super.onPause();

        // When the activity is paused, we make sure to release our sensor resources
        mAccelerometerView.stopAccelerometer();
        // stop the location listener
        mLocationView.stopLocation();
    }


    // displays the reading
    public void displayReading(float val) {
        TextView display = (TextView)findViewById(R.id.reading);
        display.setText(valueOf(val));
    }


    //////////////////////////////////////////////////////////////////////////////////////
    // The class for the accelerometer
    //////////////////////////////////////////////////////////////////////////////////////
    public class AccelerometerView extends AppCompatActivity implements SensorEventListener {

        //////////////////////
        // vars for this class
        //////////////////////
        private Sensor mAccelerometer;
        public float reading;


        // start the sensor listener
        public void startAccelerometer() {
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }

        // stop the sensor listener
        public void stopAccelerometer() {
            mSensorManager.unregisterListener(this);
        }

        public AccelerometerView(Context context) {
            // put some stuff here
            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        }


        /*called whenever the accelerometer picks up any movement*/
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType()==Sensor.TYPE_LINEAR_ACCELERATION){
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                reading = y;

                displayReading(reading);

            }
        }

        // leave this here at the bottom of the class, it's not used
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }


    //////////////////////////////////////////////////////////////////////////////////////
    // The class for the GPS
    //////////////////////////////////////////////////////////////////////////////////////
    public class LocationView extends AppCompatActivity implements LocationListener {

        //////////////////////
        // vars for this class
        //////////////////////
        private Location mLocation;


        // Register the listener with the Location Manager to receive location updates
        public void startLocation() {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            // TODO handle this error ^
        }

        // Remove the location listener to stop receiving location updates
        public void stopLocation() {
            mLocationManager.removeUpdates(this);
        }

        public LocationView(Context context) {
            // put some stuff here
        }

        public void onLocationChanged(Location location) {
            // Called when a new location is found by the network location provider.
            double latitude = (double) (location.getLatitude());
            double longitude = (double) (location.getLongitude());
        }

        /////////////////////////////////////////////////////////////////////////
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        public void onProviderEnabled(String provider) {

        }

        public void onProviderDisabled(String provider) {

        }
        /////////////////////////////////////////////////////////////////////////
    }

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

