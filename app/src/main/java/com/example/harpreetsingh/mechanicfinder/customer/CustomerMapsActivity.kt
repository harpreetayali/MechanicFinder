package com.example.harpreetsingh.mechanicfinder.customer

import android.Manifest
import android.app.ProgressDialog
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.PermissionGroupInfo
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.AsyncTask
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.support.v4.app.FragmentActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import android.widget.Toolbar

import com.example.harpreetsingh.mechanicfinder.R
import com.example.harpreetsingh.mechanicfinder.user.Session
import com.example.harpreetsingh.mechanicfinder.user.SignUp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.messaging.FirebaseMessaging

import org.json.JSONArray
import org.json.JSONObject

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class CustomerMapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private var mMap: GoogleMap? = null
    private var mmMap: GoogleMap? = null
    lateinit var locationManager: LocationManager
    lateinit var locationListener: LocationListener
    lateinit var findMechanic: Button
    lateinit var requestMechanic: Button
    private var firstName: String? = null
    private var lastName: String? = null
    private var latitude: Double = 0.toDouble()
    private var longitude: Double = 0.toDouble()
    lateinit var array_fName: JSONArray
    lateinit var array_lName: JSONArray
    lateinit var array_latitude: JSONArray
    lateinit var array_longitude: JSONArray
    lateinit var session: Session
    lateinit var user_lat: String
    lateinit var user_long: String
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 100f, locationListener)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_maps)

        val toolbar = findViewById<android.support.v7.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        title = "Nearby Mechanics"



        findMechanic = findViewById(R.id.findMechanic)
        findMechanic.setOnClickListener { view ->
            val fetchLocation = FetchLocation(this@CustomerMapsActivity)
            fetchLocation.execute("http://mechanicfinder.000webhostapp.com/MechanicFinder/nearByPlace.php")


        }

        requestMechanic = findViewById(R.id.requestMechanic)
        requestMechanic.setOnClickListener { view ->

            val sendReq = SendReq(this@CustomerMapsActivity)
            sendReq.execute("http://mechanicfinder.000webhostapp.com/MechanicFinder/firebase.php")


        }
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mmMap = googleMap
        locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val userLocation = LatLng(location.latitude, location.longitude)
                user_lat = location.latitude.toString()
                user_long = location.longitude.toString()

                mMap!!.clear()
                mMap!!.addMarker(MarkerOptions().position(userLocation).title("Your Location"))
                mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))


            }

            override fun onStatusChanged(s: String, i: Int, bundle: Bundle) {

            }

            override fun onProviderEnabled(s: String) {

            }

            override fun onProviderDisabled(s: String) {

            }
        }
        if (Build.VERSION.SDK_INT < 22) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 100f, locationListener)
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), 1)
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 100f, locationListener)

                val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (lastLocation != null) {
                    val userLocation = LatLng(lastLocation.latitude, lastLocation.longitude)
                    mMap!!.addMarker(MarkerOptions().position(userLocation).title("Your Location"))
                    mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))

                } else {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 100f, locationListener)

                }

            }
        }

    }

    internal inner class FetchLocation(customerMapsActivity: CustomerMapsActivity) : AsyncTask<String, Void, Boolean>() {
        private var progressDialog: ProgressDialog? = null

        init {
            progressDialog = ProgressDialog(customerMapsActivity)
        }

        override fun onPreExecute() {
            progressDialog = ProgressDialog(this@CustomerMapsActivity)
            progressDialog!!.setMessage("Finding Mechanics....")
            progressDialog!!.setCancelable(false)
            progressDialog!!.show()
            super.onPreExecute()
        }

        override fun doInBackground(vararg strings: String): Boolean? {

            var line: String?
            var result = ""
            try {
                val url = URL(strings[0])
                val urlConnection = url.openConnection() as HttpURLConnection
                val `is` = urlConnection.inputStream
                val reader = InputStreamReader(`is`)
                val br = BufferedReader(reader)

                do
                {
                    line = br.readLine()

                    if (line == null)
                        break
                    result += line
                }while (true)

                Log.i("hello", result)
                val jsonObject = JSONObject(result)
                val resp = jsonObject.getString("response")

                if (resp == "Ok") {
                    array_fName = jsonObject.getJSONArray("firstName")
                    array_lName = jsonObject.getJSONArray("lastName")
                    array_latitude = jsonObject.getJSONArray("latitude")
                    array_longitude = jsonObject.getJSONArray("longitude")

                    return true
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }

            return false
        }

        override fun onPostExecute(aBoolean: Boolean?) {
            if (progressDialog!!.isShowing)
                progressDialog!!.dismiss()

            if (aBoolean!!) {
                for (i in 0 until array_fName.length()) {
                    try {

                        firstName = array_fName.getString(i)
                        lastName = array_lName.getString(i)
                        latitude = java.lang.Double.parseDouble(array_latitude.getString(i))
                        longitude = java.lang.Double.parseDouble(array_longitude.getString(i))
                        // Log.i("Locationteri",firstName+":"+lastName+":"+latitude+":"+longitude);
                        //Log.i("Locationteri",String.valueOf(aBoolean));
                        val latLng = LatLng(latitude, longitude)
                        mmMap!!.addMarker(MarkerOptions().position(latLng).title("$firstName $lastName").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
                        mmMap!!.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                        mmMap!!.animateCamera(CameraUpdateFactory.zoomTo(15f))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }
                //Toast.makeText(CustomerMapsActivity.this,"Option2",Toast.LENGTH_SHORT).show();

            }

            super.onPostExecute(aBoolean)
        }
    }

    internal inner class SendReq(customerMapsActivity: CustomerMapsActivity) : AsyncTask<String, Void, Boolean>() {
        private var progressDialog: ProgressDialog? = null

        init {
            progressDialog = ProgressDialog(customerMapsActivity)
        }

        override fun onPreExecute() {
            progressDialog = ProgressDialog(this@CustomerMapsActivity)
            progressDialog!!.setMessage("Sending Request....")
            progressDialog!!.setCancelable(false)
            progressDialog!!.show()
            super.onPreExecute()
        }

        override fun doInBackground(vararg strings: String): Boolean? {
            session = Session(this@CustomerMapsActivity)
            var line: String
            var result = ""
            try {

                val url = URL(strings[0])
                val urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.requestMethod = "POST"
                urlConnection.doOutput = true
                urlConnection.doInput = true
                val os = urlConnection.outputStream
                val writer = OutputStreamWriter(os)
                val bw = BufferedWriter(writer)
                val post_data = URLEncoder.encode("firstName", "UTF-8") + "=" + URLEncoder.encode(session.userName, "UTF-8") +
                        "&" + URLEncoder.encode("user_lat", "UTF-8") + "=" + URLEncoder.encode(user_lat, "UTF-8") +
                        "&" + URLEncoder.encode("user_long", "UTF-8") + "=" + URLEncoder.encode(user_long, "UTF-8")
                Log.i("LATLONG", "$user_lat:$user_long")
                bw.write(post_data)
                bw.flush()
                bw.close()
                os.close()
                val `is` = urlConnection.inputStream
                val reader = InputStreamReader(`is`)
                val br = BufferedReader(reader)

                do
                {
                    line = br.readLine()

                    if (line == null)
                        break
                    result += line
                }while (true)

                val jsonObject = JSONObject(result)
                val resp = jsonObject.getString("response")

                if (resp == "Ok") {
                    return true
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }

            return false
        }

        override fun onPostExecute(aBoolean: Boolean?) {
            if (progressDialog!!.isShowing)
                progressDialog!!.dismiss()

            if (aBoolean!!) {
                Toast.makeText(this@CustomerMapsActivity, "Request Sent", Toast.LENGTH_SHORT).show()

            }

            super.onPostExecute(aBoolean)
        }
    }

}
