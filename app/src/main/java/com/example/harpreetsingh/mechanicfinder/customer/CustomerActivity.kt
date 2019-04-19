package com.example.harpreetsingh.mechanicfinder.customer

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.provider.Settings
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem

import com.example.harpreetsingh.mechanicfinder.R
import com.example.harpreetsingh.mechanicfinder.user.LoginActivity
import com.example.harpreetsingh.mechanicfinder.user.EditProfile
import com.example.harpreetsingh.mechanicfinder.user.Profile
import com.example.harpreetsingh.mechanicfinder.user.Session
import com.google.firebase.messaging.FirebaseMessaging

class CustomerActivity : AppCompatActivity() {

    private var session: Session? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer)

        FirebaseMessaging.getInstance().unsubscribeFromTopic("Request");
        FirebaseMessaging.getInstance().subscribeToTopic("Receive")

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        title = "Customer"

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
        startActivity(Intent(this@CustomerActivity, LoginActivity::class.java))

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.customer_menu, menu)

        return true

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.customerLocation -> startActivity(Intent(this@CustomerActivity, CustomerMapsActivity::class.java))
            R.id.profile_customer -> startActivity(Intent(this@CustomerActivity, Profile::class.java))
            R.id.logout_customer -> logout()
        }
        return super.onOptionsItemSelected(item)

    }
}
