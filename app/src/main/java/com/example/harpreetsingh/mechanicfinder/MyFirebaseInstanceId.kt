package com.example.harpreetsingh.mechanicfinder

import android.util.Log

import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService

class MyFirebaseInstanceId : FirebaseInstanceIdService() {
    override fun onTokenRefresh() {

        val refreshedToken = FirebaseInstanceId.getInstance().token
        Log.d("FB", "Refreshed token: " + refreshedToken!!)


    }

}
