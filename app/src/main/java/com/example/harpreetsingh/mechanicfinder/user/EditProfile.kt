package com.example.harpreetsingh.mechanicfinder.user

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import com.example.harpreetsingh.mechanicfinder.R

class EditProfile : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_edit)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        title = "EditProfile"

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

    }

    override fun onSupportNavigateUp(): Boolean
    {
        onBackPressed()
        return true
    }
}
