package com.example.harpreetsingh.mechanicfinder

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessaging : FirebaseMessagingService()
{
    override fun onMessageReceived(remoteMessage: RemoteMessage?) {

        var title = remoteMessage!!.notification!!.title
        var body = remoteMessage!!.notification!!.body
        val username = remoteMessage.data["username"]
        val user_lat = remoteMessage.data["user_lat"]
        val user_long = remoteMessage.data["user_long"]

         Log.i("NotiFire",user_lat+":"+user_long);
        MyNotificationManager.getInstance(applicationContext).displayNotification(title, body, username, user_lat, user_long)
    }
}
