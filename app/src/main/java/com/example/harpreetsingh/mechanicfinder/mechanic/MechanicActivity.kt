package com.example.harpreetsingh.mechanicfinder.mechanic

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.AsyncTask
import android.provider.Settings
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast

import com.example.harpreetsingh.mechanicfinder.Constants
import com.example.harpreetsingh.mechanicfinder.MyFirebaseInstanceId
import com.example.harpreetsingh.mechanicfinder.R
import com.example.harpreetsingh.mechanicfinder.customer.CustomerActivity
import com.example.harpreetsingh.mechanicfinder.user.LoginActivity
import com.example.harpreetsingh.mechanicfinder.user.Profile
import com.example.harpreetsingh.mechanicfinder.user.Session
import com.example.harpreetsingh.mechanicfinder.user.SignUp
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.ui.PlacePicker
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.messaging.FirebaseMessaging

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

class MechanicActivity : AppCompatActivity() {
    private var session: Session? = null
    internal var PLACE_PICKER_REQUEST = 1
    private var latitude: String? = null
    private var longitude: String? = null
    private var userName: String? = null
    private var email: String? = null
    lateinit var result: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mechanic)

        FirebaseMessaging.getInstance().unsubscribeFromTopic("Receive");
        FirebaseMessaging.getInstance().subscribeToTopic("Request")

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        title = "Mechanic Profile"
        result = findViewById(R.id.locationResult)


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val mchannel = NotificationChannel(Constants.CHANNEL_ID, Constants.CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)

            mchannel.description = Constants.CHANNEL_DESCRIPTION
            mchannel.enableLights(true)
            mchannel.lightColor = Color.RED
            mchannel.enableVibration(true)
            mchannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)

            notificationManager.createNotificationChannel(mchannel)
        }



        if (!checkInternet()) {
            buildAlertMessage()
        }
        session = Session(this)
        if (!session!!.loggedIn()) {
            logout()
        }

    }

    private fun checkInternet(): Boolean {
        var connected = false
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connected = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).state == NetworkInfo.State.CONNECTED || connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).state == NetworkInfo.State.CONNECTED
        return connected
    }

    private fun buildAlertMessage() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Your internet connection is off, Do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes") { dialog, id -> startActivity(Intent(Settings.ACTION_SETTINGS)) }
                .setNegativeButton("No") { dialogInterface, i -> finish() }.setTitle("No Internet !")

        val alert = builder.create()
        alert.show()
    }

    private fun logout() {
        session!!.setLoggedIn(false)
        finish()
        startActivity(Intent(this@MechanicActivity, LoginActivity::class.java))

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.mechanic_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.setLocation -> mechanicLoc()
            R.id.profile -> startActivity(Intent(this@MechanicActivity, Profile::class.java))
            R.id.logout_mechanic -> logout()
        }
        return super.onOptionsItemSelected(item)
    }

    fun mechanicLoc() {
        val builder = PlacePicker.IntentBuilder()

        try {
            startActivityForResult(builder.build(this@MechanicActivity), PLACE_PICKER_REQUEST)

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                val place = PlacePicker.getPlace(this@MechanicActivity, data)
                val latLng = place.latLng
                latitude = latLng.latitude.toString()
                longitude = latLng.longitude.toString()
                userName = session!!.userName
                email = session!!.email
                //Log.i("hello",userName+"::"+email);
                val saveLocation = SaveLocation(this@MechanicActivity)
                saveLocation.execute("http://mechanicfinder.000webhostapp.com/MechanicFinder/saveLocation.php")
                result.text = latLng.latitude.toString() + ":" + latLng.longitude
                Toast.makeText(this@MechanicActivity, place.latLng.toString() + "", Toast.LENGTH_SHORT).show()

            }

        }

    }

    internal inner class SaveLocation(mechanicActivity: MechanicActivity) : AsyncTask<String, Void, Boolean>() {
        private var progressDialog: ProgressDialog? = null

        init {
            progressDialog = ProgressDialog(mechanicActivity)
        }

        override fun onPreExecute() {
            progressDialog = ProgressDialog(this@MechanicActivity)
            progressDialog!!.setMessage("Saving Location....")
            progressDialog!!.setCancelable(false)
            progressDialog!!.show()
            super.onPreExecute()
        }

        override fun doInBackground(vararg strings: String): Boolean? {

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
                val post_data = URLEncoder.encode("latitude", "UTF-8") + "=" + URLEncoder.encode(latitude, "UTF-8") +
                        "&" + URLEncoder.encode("longitude", "UTF-8") + "=" + URLEncoder.encode(longitude, "UTF-8") +
                        "&" + URLEncoder.encode("userName", "UTF-8") + "=" + URLEncoder.encode(userName, "UTF-8") +
                        "&" + URLEncoder.encode("loggedIn_email", "UTF-8") + "=" + URLEncoder.encode(email, "UTF-8")
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

                // Log.i("hello",result);
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
            super.onPostExecute(aBoolean)
            if (aBoolean!!)
                Toast.makeText(this@MechanicActivity, "Location saved", Toast.LENGTH_LONG).show()

        }
    }
}
