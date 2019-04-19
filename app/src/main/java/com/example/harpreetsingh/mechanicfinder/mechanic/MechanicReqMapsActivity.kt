package com.example.harpreetsingh.mechanicfinder.mechanic

import android.Manifest
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast

import com.example.harpreetsingh.mechanicfinder.R
import com.example.harpreetsingh.mechanicfinder.customer.CustomerMapsActivity
import com.example.harpreetsingh.mechanicfinder.user.Session
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

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

class MechanicReqMapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private var mMap: GoogleMap? = null
    private var mmMap: GoogleMap? = null
    lateinit var locationManager: LocationManager
    lateinit var locationListener: LocationListener
    internal var user_long: Double? = null
    internal var user_lat: Double? = null
    lateinit var session: Session
    lateinit var acceptReq: Button
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
        title = "Customer Location"
        setContentView(R.layout.activity_mechanic_req_maps)

        acceptReq = findViewById(R.id.acceptReq)
        acceptReq.setOnClickListener { view ->
            val acptReq = AcceptReq(this)
            acptReq.execute("http://mechanicfinder.000webhostapp.com/MechanicFinder/receive_firebase.php")

        }

        val intent = intent

        user_lat = java.lang.Double.parseDouble(intent.getStringExtra("user_lat"))
        user_long = java.lang.Double.parseDouble(intent.getStringExtra("user_long"))

        Toast.makeText(this, intent.getStringExtra("user_lat") + ":" + intent.getStringExtra("user_long"), Toast.LENGTH_SHORT).show()


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
                mMap!!.clear()
                mMap!!.addMarker(MarkerOptions().position(userLocation).title("Your Location"))
                mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))

                val latLng = LatLng(user_lat!!, user_long!!)
                mmMap!!.addMarker(MarkerOptions().position(latLng).title("Customer Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
                mmMap!!.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                mmMap!!.animateCamera(CameraUpdateFactory.zoomTo(15f))

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

    internal inner class AcceptReq(mechanicReqMapsActivity: MechanicReqMapsActivity) : AsyncTask<String, Void, Boolean>() {
        private var progressDialog: ProgressDialog? = null

        init {
            progressDialog = ProgressDialog(mechanicReqMapsActivity)
        }

        override fun onPreExecute() {
            progressDialog = ProgressDialog(this@MechanicReqMapsActivity)
            progressDialog!!.setMessage("Notifying User....")
            progressDialog!!.setCancelable(false)
            progressDialog!!.show()
            super.onPreExecute()
        }

        override fun doInBackground(vararg strings: String): Boolean? {
            session = Session(this@MechanicReqMapsActivity)
            var line: String?
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
                val post_data = URLEncoder.encode("firstName", "UTF-8") + "=" + URLEncoder.encode(session.userName, "UTF-8")
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
//                while ((line = br.readLine()) != null) {
//                    result += line
//
//                }

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
                Toast.makeText(this@MechanicReqMapsActivity, "Request Sent", Toast.LENGTH_SHORT).show()

            }

            super.onPostExecute(aBoolean)
        }
    }
}
