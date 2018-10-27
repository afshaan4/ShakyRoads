package com.example.potato.shakyroads

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.widget.TextView
import android.Manifest
import android.widget.Toast

import com.opencsv.CSVWriter

import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets



class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, SensorEventListener, LocationListener {


    private var mSensorManager: SensorManager? = null
    private var mAccelerometer: Sensor? = null
    private var mLocationManager: LocationManager? = null
    private val myPermissionRequestLocation = 99
    // vars to hold the readings
    private var globX = 0.0
    private var globY = 0.0
    private var globZ = 0.0
    private var globLat = 0.0
    private var globLng = 0.0
    // stores the state of the button to start getting location updates
    private var isButtonPressed = 0 // 0 = off, 1 = on
    // TODO: https://kotlinlang.org/docs/reference/properties.html


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        /*Button to start reading from the GPS*/
        val fab = findViewById<View>(R.id.fab) as FloatingActionButton
        fab.setOnClickListener { view ->
            // switch the state of the button
            isButtonPressed = 1 - isButtonPressed // math tricks: 1 - 0 = 1 | 1 - 1 = 0.
            // I gotta have *some* feedback
            Snackbar.make(view, isButtonPressed.toString(), Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show()

            if (checkLocationPermission()) {
                startLocation()
            } else {
                stopLocation()
            }
        }

        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        val navigationView = findViewById<View>(R.id.nav_view) as NavigationView
        navigationView.setNavigationItemSelectedListener(this)

        // get instances of SensorManager
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // get an instance of the linear motion accelerometer
        mAccelerometer = mSensorManager!!.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

        // get an instance of LocationManager
        mLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }


    /* when the activity resumes */
    override fun onResume() {
        super.onResume()

        // Start the reading from the accelerometer
        startAccelerometer()

        // start getting location updates if the button was pressed
        if (isButtonPressed == 1) {
            startLocation()
            Log.d("resuming", "button pressed")
        } else {
            stopLocation()
            Log.d("resuming", "button NOT pressed")
        }
    }

    /* when the activity pauses */
    override fun onPause() {
        super.onPause()

        // When the activity is paused, we make sure to release our sensor resources
        stopAccelerometer()
        // stop the location listeners
        stopLocation()
    }


    /*
    Display functions for testing.
     */
    /* displays the reading */
    private fun displayAcceleration(acceleration: Double) {
        val display = findViewById<View>(R.id.reading) as TextView
        display.text = acceleration.toString()
    }

    /* displays the longitude */
    private fun displayLong(location: Double) {
        val display = findViewById<View>(R.id.longitude) as TextView
        display.text = location.toString()
    }

    /* displays the latitude */
    private fun displayLat(location: Double) {
        val display = findViewById<View>(R.id.latitude) as TextView
        display.text = location.toString()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // stuff that saves the data
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    /* checks if external storage is writeable */
    private val isExternalStorageWritable: Boolean
        get() {
            val state = Environment.getExternalStorageState()
            return Environment.MEDIA_MOUNTED == state
        }

    /* writes the GPS and acceleration data to a csv file */
    private fun saveData(accX: Double, accY: Double, accZ: Double, lat: Double, lng: Double) {
        // filename and path to file
        val fileName = "ShakyroadsData.csv"
        val path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS)

        // make the file object
        val file = File(path, fileName)

        if (isExternalStorageWritable) {
            try {
                // make sure the directory exists
                path.mkdirs()

                // get an instance of CSVWriter
                val writer = CSVWriter(
                        OutputStreamWriter(FileOutputStream(file, true),
                                StandardCharsets.UTF_8), ',', '"', '"', "\n")

                // convert the data to strings
                val latitude = lat.toString()
                val longitude = lng.toString()
                val X = accX.toString()
                val Y = accY.toString()
                val Z = accZ.toString()
                // then to an array of strings
                val entries = arrayOf(latitude, longitude, X, Y, Z)

                // then write it
                writer.writeNext(entries)
                writer.close()

            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("holy crap", "FileOutputStream threw an IOException")
            }

        } else {
            Log.e("saveFile", "storage not writeable, you probably don't have the privs")
            // TODO ask for permission instead of just complaining
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // The accelerometer stuff
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /* start the sensor listener */
    private fun startAccelerometer() {
        mSensorManager!!.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    /* stop the sensor listener */
    private fun stopAccelerometer() {
        mSensorManager!!.unregisterListener(this)
    }

    /* called whenever the accelerometer picks up any movement */
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_LINEAR_ACCELERATION) {
            val x = event.values[0].toDouble()
            val y = event.values[1].toDouble()
            val z = event.values[2].toDouble()

            displayAcceleration(y)

            // update the vals
            globX = x; globY = y; globZ = z
        }
    }

    // leave this here, it's not used
    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // The GPS stuff
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /*
    This checks if the app has the permissions to use the GPS, if the app doesn't have
    the permissions then it shows the user a dialog asking for the permission.
    Returns true or false depending on whether it has the permission or not
     */
    private fun checkLocationPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                AlertDialog.Builder(this)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.ok) { dialogInterface, i ->
                            //Prompt the user once explanation has been shown
                            ActivityCompat.requestPermissions(this@MainActivity,
                                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                    myPermissionRequestLocation)
                        }
                        .create()
                        .show()

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        myPermissionRequestLocation)
            }
            return false
        } else {
            return true
        }
    }

    /*
    Checks if the app got permission to use the GPS, if it did get the permission then it
    calls startLocation(). startLocation() has a check as well to see if the app has the permission
    to use the GPS.
     */
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            myPermissionRequestLocation -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, do the location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                        //Request location updates:
                        startLocation()
                    }

                } else {

                    // TODO: disable the thing that depends on this permission.
                }
                return
            }
        }
    }


    /* Register the listener with the Location Manager to receive location updates */
    private fun startLocation() {
        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            // minTime: milliseconds, minDistance: meters
            mLocationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, (1 * 1000).toLong(), 2f, this)
        }
    }

    /* Remove the location listener to stop receiving location updates */
    private fun stopLocation() {
        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            mLocationManager!!.removeUpdates(this)
        }

    }

    /* Called when location changes */
    override fun onLocationChanged(location: Location) {
        val latitude = location.latitude
        val longitude = location.longitude

        displayLong(longitude)
        displayLat(latitude)

        globLat = latitude; globLng = longitude
        saveData(globX, globY, globZ, globLat, globLng)
    }

    // these are not used yet, leave them here
    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}

    override fun onProviderEnabled(provider: String) {}

    override fun onProviderDisabled(provider: String) {
        // tell the user when the location provider is disabled
        stopLocation()
        Toast.makeText(this, "GPS disabled", Toast.LENGTH_LONG).show()
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //The UI functions and handlers
    ////////////////////////////////////////////////////////////////////////////////////////////////
    override fun onBackPressed() {
        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId


        return if (id == R.id.action_settings) {
            true
        } else super.onOptionsItemSelected(item)

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        val id = item.itemId

        when (id) {
            R.id.nav_camera -> {
                // Handle the camera action
            }
            R.id.nav_gallery -> {

            }
            R.id.nav_slideshow -> {

            }
            R.id.nav_manage -> {

            }
            R.id.nav_share -> {

            }
            R.id.nav_send -> {

            }
        }

        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

}