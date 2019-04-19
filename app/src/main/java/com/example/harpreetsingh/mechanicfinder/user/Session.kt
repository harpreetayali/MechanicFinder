package com.example.harpreetsingh.mechanicfinder.user

import android.content.Context
import android.content.SharedPreferences

class Session(internal var ctx: Context) {
    internal var prefs: SharedPreferences
    internal var editor: SharedPreferences.Editor

    var userType: String
        get() = prefs.getString("User", "null")
        set(userType) {

            editor.putString("User", userType)
            editor.commit()
        }

    var userName: String
        get() = prefs.getString("UserName", "null")
        set(userName) {
            editor.putString("UserName", userName)
            editor.commit()
        }
    var email: String
        get() = prefs.getString("Email", "null")
        set(email) {
            editor.putString("Email", email)
            editor.commit()
        }

    init {
        prefs = ctx.getSharedPreferences("MechanicFinder", Context.MODE_PRIVATE)
        editor = prefs.edit()
    }

    fun setLoggedIn(logIn: Boolean) {
        editor.putBoolean("logInMode", logIn)
        editor.commit()
    }

    fun loggedIn(): Boolean {
        return prefs.getBoolean("logInMode", false)
    }

}
