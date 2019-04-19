package com.example.harpreetsingh.mechanicfinder.user

import android.app.ProgressDialog
import android.content.Intent
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.example.harpreetsingh.mechanicfinder.R
import com.example.harpreetsingh.mechanicfinder.customer.CustomerActivity
import com.example.harpreetsingh.mechanicfinder.mechanic.MechanicActivity
import org.json.JSONObject
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class Profile : AppCompatActivity()
{

    private var session : Session? = null
    var currentUser : String? = null
    var firstName: String? = null
    var lastName: String? = null
    var mobile: String? = null
    var email: String? = null
    var userType: String? = null
    var text_f_l_name:TextView? = null
    var textEmail:TextView? = null
    var textMobile:TextView? = null
    var textUsertype:TextView? = null
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        text_f_l_name = findViewById(R.id.f_l_name)
        textEmail = findViewById(R.id.email)
        textMobile = findViewById(R.id.mobile)
        textUsertype = findViewById(R.id.user_type)

        title = "Profile"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        session = Session(this@Profile)
        currentUser = session!!.userName

        val loginUser = LoginUser(this@Profile)
        loginUser.execute("http://mechanicfinder.000webhostapp.com/MechanicFinder/fetch_profile.php")


        Log.i("Ayali", session!!.userName)


    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    internal inner class LoginUser(profile: Profile) : AsyncTask<String, Void, Boolean>() {
       private var progressDialog: ProgressDialog? = null


        init {
            progressDialog = ProgressDialog(profile)
        }

        override fun onPreExecute() {
            progressDialog = ProgressDialog(this@Profile)
            progressDialog!!.setMessage("please wait....")
            progressDialog!!.setCancelable(false)
            progressDialog!!.show()
            super.onPreExecute()

        }

        override fun doInBackground(vararg strings: String): Boolean? {
            var line:String?
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
                val post_data = URLEncoder.encode("currentUser", "UTF-8") + "=" + URLEncoder.encode(currentUser, "UTF-8")

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
                    //Log.i("Helllll",line)
                    if (line == null)
                        break
                    result += line
                }while (true)

                val jsonObject = JSONObject(result)
                val resp = jsonObject.getString("response")


                if (resp == "Ok") {
                    firstName = jsonObject.getString("firstName")
                    lastName = jsonObject.getString("lastName")
                    mobile = jsonObject.getString("mobile")
                    email = jsonObject.getString("email")
                    userType = jsonObject.getString("userType")
                    Log.i("Helllll",firstName+lastName)
                    return true
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }

            return false
        }

        override fun onPostExecute(aBoolean: Boolean?)
        {
            if (progressDialog!!.isShowing)
                progressDialog!!.dismiss()
            super.onPostExecute(aBoolean)

            //Toast.makeText(this@Profile,"yoyouhshsdhskjdhskdj",Toast.LENGTH_LONG).show()
            //Toast.makeText(this@Profile,"$firstName+ $lastName+ $email+ $mobile+ $userType",Toast.LENGTH_LONG).show()

            text_f_l_name!!.text = firstName +" "+ lastName
            textEmail!!.text = email
            textMobile!!.text = mobile
            textUsertype!!.text = userType

        }
    }
}
