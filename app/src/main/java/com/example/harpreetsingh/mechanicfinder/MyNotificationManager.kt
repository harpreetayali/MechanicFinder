package com.example.harpreetsingh.mechanicfinder

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.support.v4.app.NotificationCompat

import com.example.harpreetsingh.mechanicfinder.mechanic.MechanicReqMapsActivity

class MyNotificationManager private constructor(private val mctx: Context) {

    fun displayNotification(title: String?, body: String?, username: String?, user_lat: String?, user_long: String?) {

        val mBuilder = NotificationCompat.Builder(mctx, Constants.CHANNEL_ID)
        mBuilder.setSmallIcon(R.drawable.map)
        mBuilder.setContentTitle(title)
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        mBuilder.setSound(uri)
        mBuilder.setAutoCancel(true)
        mBuilder.setContentText(body)


        val intent = Intent(mctx, MechanicReqMapsActivity::class.java)
        intent.putExtra("username", username)
        intent.putExtra("user_lat", user_lat)
        intent.putExtra("user_long", user_long)
        val pendingIntent = PendingIntent.getActivity(mctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        mBuilder.setContentIntent(pendingIntent)


        val notificationManager = mctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager?.notify(1, mBuilder.build())

    }

    companion object {

        var mInstance: MyNotificationManager? = null


        fun getInstance(context: Context): MyNotificationManager {
            if (mInstance == null) {
                mInstance = MyNotificationManager(context)

            }
            return mInstance as MyNotificationManager
        }
    }

}
