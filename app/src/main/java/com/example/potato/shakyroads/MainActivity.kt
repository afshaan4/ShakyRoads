package com.example.potato.shakyroads

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import android.util.Log
import android.view.View
import com.google.android.material.navigation.NavigationView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
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
import android.content.Intent
import android.widget.Toast
import androidx.preference.PreferenceManager


import com.opencsv.CSVWriter

import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.util.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
        SensorEventListener, LocationListener {


    private var mSensorManager: SensorManager? = null
    private var mAccelerometer: Sensor? = null
    private var mLocationManager: LocationManager? = null
    private val myPermissionRequestLocation = 99
    // vars to hold the readings
    private var globX: Double = 0.0
    private var globY: Double = 0.0
    private var globZ: Double = 0.0
    // stores the state of the button that toggles location
    private var isButtonPressed: Int = 0 // 0 = off, 1 = on
    private var currentTime = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        /*Button to toggle the logger*/
        val fab = findViewById<View>(R.id.fab) as FloatingActionButton
        fab.setOnClickListener { view ->
            // switch the state of the button
            isButtonPressed = 1 - isButtonPressed // math tricks: 1 - 0 = 1 and 1 - 1 = 0.
            // TODO: change the button icon rather than showing a SnackBar
            Snackbar.make(view, isButtonPressed.toString(), Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show()
            checkLocationPermission()
            // for timestamping files, worst possible way to do it but hey im dumb
            currentTime = Calendar.getInstance().time.toString()
        }

        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        val navigationView = findViewById<View>(R.id.nav_view) as NavigationView
        navigationView.setNavigationItemSelectedListener(this)

        // get an instance of SensorManager & linear motion accelerometer
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAccelerometer = mSensorManager!!.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

        // get an instance of LocationManager
        mLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }


    /* when the activity resumes */
    override fun onResume() {
        super.onResume()
        // resume reading from the sensors
        startAccelerometer()
        startLocation()
    }

    /* when the activity pauses */
    override fun onPause() {
        super.onPause()
        // stop reading from the sensors
        stopAccelerometer()
        stopLocation()
    }

    /* used to display stuff */
    private fun display(data: Double, viewId: Int) {
        val display = findViewById<View>(viewId) as TextView
        display.text = data.toString()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // stuff that saves the data
    ////////////////////////////////////////////////////////////////////////////////////////////////


    /* Checks if external storage is available to read and write */
    private fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    /* writes the GPS and acceleration data to a csv file */
    private fun saveData(accX: Double, accY: Double, accZ: Double, lat: Double, lng: Double) {
        // filename and path to file
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val fileName = sharedPreferences.getString("filename", "ShakyroadsData")
        val path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS + "/shakyroads/")
        val file = File(path, "$fileName $currentTime.csv")


        if (isExternalStorageWritable()) {
            try {
                // make sure the directory exists
                path.mkdirs()

                // get an instance of CSVWriter
                val writer = CSVWriter(
                        OutputStreamWriter(FileOutputStream(file, true),
                                StandardCharsets.UTF_8), ',', '"', '"', "\n")

                // write em
                val entries = arrayOf(lat.toString(), lng.toString(), accX.toString(), accY.toString(), accZ.toString())
                writer.writeNext(entries)
                writer.close()

            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("holy crap", "FileOutputStream threw an IOException")
            }
        } else {
            Log.e("saveFile", "storage not writable, you probably don't have the privs")
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

            display(x, R.id.X)
            display(y, R.id.Y)
            display(z, R.id.Z)

            globX = x
            globY = y
            globZ = z
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                AlertDialog.Builder(this)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.ok) { _, _ ->
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
                        startLocation()
                    }
                } else {
                    // TODO: disable the GPS, add an obvious UI change like change the buttons icon.
                    stopLocation()
                }
                return
            }
        }
    }


    /* Register the listener with the Location Manager to receive location updates */
    private fun startLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // minTime: milliseconds, minDistance: meters
            mLocationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    (1 * 1000).toLong(), 2f, this)
        }
    }

    /* Remove the location listener to stop receiving location updates */
    private fun stopLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationManager!!.removeUpdates(this)
        }
    }

    /* Called when location changes */
    override fun onLocationChanged(location: Location) {
        val latitude = location.latitude
        val longitude = location.longitude

        display(longitude, R.id.latitude)
        display(latitude, R.id.longitude)

        // the app only adds new entries on new location updates
        if (isButtonPressed == 1) {
            saveData(globX, globY, globZ, latitude, longitude)
        }
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
        when (item.itemId) {
            R.id.nav_camera -> {

            }
            R.id.nav_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
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