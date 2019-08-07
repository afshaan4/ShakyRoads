package com.example.potato.shakyroads

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.SensorEventListener
import android.location.LocationListener
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.SensorEvent
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.preference.PreferenceManager

import com.opencsv.CSVWriter

import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

class SensorLogic : AppCompatActivity(), SensorEventListener, LocationListener {
    private var mSensorManager: SensorManager? = null
    private var mAccelerometer: Sensor? = null
    private var mLocationManager: LocationManager? = null
    private val myPermissionRequestLocation = 99
    // vars to hold the readings
    private var globX: Double = 0.0
    private var globY: Double = 0.0
    private var globZ: Double = 0.0
    var isButtonPressed = 0
    private var currentTime = ""

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // The init function
    ////////////////////////////////////////////////////////////////////////////////////////////////
    fun SensorLogic() {
        // get an instance of SensorManager & linear motion accelerometer
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAccelerometer = mSensorManager!!.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

        // get an instance of LocationManager
        mLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // The accelerometer stuff
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /* called whenever the accelerometer picks up any movement */
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_LINEAR_ACCELERATION) {
            val x = event.values[0].toDouble()
            val y = event.values[1].toDouble()
            val z = event.values[2].toDouble()
            // update acceleration values
            globX = x
            globY = y
            globZ = z

//            display(x, R.id.X)
//            display(y, R.id.Y)
//            display(z, R.id.Z)
        }
    }

    // leave this here, it's not used
    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // The GPS stuff
    ////////////////////////////////////////////////////////////////////////////////////////////////

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
        mLocationManager!!.removeUpdates(this)
    }

    /* Called when location changes */
    override fun onLocationChanged(location: Location) {
        val latitude = location.latitude
        val longitude = location.longitude

//        display(longitude, R.id.latitude)
//        display(latitude, R.id.longitude)

        if (isButtonPressed == 1) {
            saveData(globX, globY, globZ, latitude, longitude, currentTime)
        }
    }

    // these are not used yet, leave them here
    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}

    override fun onProviderEnabled(provider: String) {}

    override fun onProviderDisabled(provider: String) {
        // tell the user when the location provider is disabled
        stopLocation()
        Toast.makeText(this, "GPS is turned off", Toast.LENGTH_LONG).show()
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // stuff that saves the data
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /* Checks if external storage is available to read and write */
    private fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    /* writes the GPS and acceleration data to a csv file */
    private fun saveData(accX: Double, accY: Double, accZ: Double, lat: Double,
                         lng: Double, time: String) {
        // filename and path to file
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val fileName = sharedPreferences.getString("filename", "ShakyroadsData")
        val path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS + "/shakyroads/")
        val file = File(path, "$fileName $time.csv")


        if (isExternalStorageWritable()) {
            try {
                // make sure the directory exists
                path.mkdirs()

                // get an instance of CSVWriter
                val writer = CSVWriter(
                        OutputStreamWriter(FileOutputStream(file, true),
                                StandardCharsets.UTF_8), ',', '"', '"', "\n")

                // write em
                val entries = arrayOf(lat.toString(), lng.toString(), accX.toString(),
                        accY.toString(), accZ.toString())
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
    // The GPS permissions stuff
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

                // Show an explanation to the user asynchronously. After the user
                // sees the explanation, try again to request the permission.
                AlertDialog.Builder(this)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.ok) { _, _ ->
                            //Prompt the user once explanation has been shown
                            ActivityCompat.requestPermissions(this@SensorLogic,
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
                    // TODO: make it obvious the app can't do its thing
                    stopLocation()
                }
                return
            }
        }
    }
}