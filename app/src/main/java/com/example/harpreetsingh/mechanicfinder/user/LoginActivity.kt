package com.example.harpreetsingh.mechanicfinder.user

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.AsyncTask
import android.provider.Settings
import android.support.design.widget.TextInputLayout
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

import com.example.harpreetsingh.mechanicfinder.customer.CustomerActivity
import com.example.harpreetsingh.mechanicfinder.mechanic.MechanicActivity
import com.example.harpreetsingh.mechanicfinder.R

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

class LoginActivity : AppCompatActivity() {
    lateinit var createAcc: TextView
    lateinit var login_btn: Button
    private var textInputEmail: TextInputLayout? = null
    private var textInputPassword: TextInputLayout? = null
    private var email: String? = null
    private var password: String? = null
    private var session: Session? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        textInputEmail = findViewById(R.id.login_email)
        textInputPassword = findViewById(R.id.login_password)
        login_btn = findViewById(R.id.login_btn)
        session = Session(this)

        if (!checkInternet()) {
            buildAlertMessage()
        }
        if (session!!.loggedIn() && session!!.userType == "Customer") {

            startActivity(Intent(this@LoginActivity, CustomerActivity::class.java))
            this@LoginActivity.finish()
        }
        if (session!!.loggedIn() && session!!.userType == "Mechanic") {
            startActivity(Intent(this@LoginActivity, MechanicActivity::class.java))
            this@LoginActivity.finish()

        }


        createAcc = findViewById(R.id.createAcc)
        createAcc.setOnClickListener { view: View ->
            val signup = Intent(this@LoginActivity, SignUp::class.java)
            startActivity(signup)

        }
        login_btn.setOnClickListener { view: View ->
            if (validateLoginForm()) {
                email = textInputEmail!!.editText!!.text.toString().trim { it <= ' ' }
                password = textInputPassword!!.editText!!.text.toString().trim { it <= ' ' }

                val loginUser = LoginUser(this@LoginActivity)
                loginUser.execute("http://mechanicfinder.000webhostapp.com/MechanicFinder/login.php")
                //192.168.43.222
            }

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
                .setNegativeButton("No") { dialogInterface, i -> finish() }
                .setTitle("No Internet !")

        val alert = builder.create()
        alert.show()
    }

//    override fun onBackPressed()
//    {
//        super.onBackPressed()
//        val builder = AlertDialog.Builder(this)
//        builder.setMessage("Your internet connection is off, Do you want to enable it?")
//                .setCancelable(false)
//                .setPositiveButton("Yes") { dialogInterface, i -> finish()  }
//                .setNegativeButton("No") {dialogInterface, i ->  }
//                .setTitle("No Internet !")
//
//        val alert = builder.create()
//        alert.show()
//    }

    private fun validateEmail(): Boolean {
        val emailInput = textInputEmail!!.editText!!.text.toString().trim { it <= ' ' }
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        if (emailInput.isEmpty()) {
            textInputEmail!!.error = "Field can't be empty"
            return false
        } else if (!emailInput.matches(emailPattern.toRegex())) {
            textInputEmail!!.error = "Email is not valid"
            return false
        } else {
            textInputEmail!!.error = null
            return true
        }
    }

    private fun validatePassword(): Boolean {
        val passwordInput = textInputPassword!!.editText!!.text.toString().trim { it <= ' ' }

        if (passwordInput.isEmpty()) {
            textInputPassword!!.error = "Field can't be empty"
            return false
        } else if (passwordInput.length < 8) {
            textInputPassword!!.error = "Must contain minimum 8 characters"
            return false
        } else {
            textInputPassword!!.error = null
            return true
        }
    }

    private fun validateLoginForm(): Boolean {
        return !(!validateEmail() or !validatePassword())

    }

    internal inner class LoginUser(login: LoginActivity) : AsyncTask<String, Void, Boolean>()
    {
        private var progressDialog: ProgressDialog? = null
        lateinit var user: String
        lateinit var userName: String
        lateinit var loggedIn_email: String

        init {
            progressDialog = ProgressDialog(login)
        }

        override fun onPreExecute() {
            progressDialog = ProgressDialog(this@LoginActivity)
            progressDialog!!.setMessage("please wait....")
            progressDialog!!.setCancelable(false)
            progressDialog!!.show()
            super.onPreExecute()

        }

        override fun doInBackground(vararg strings: String): Boolean?
        {
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
                val post_data = URLEncoder.encode("email", "UTF-8") + "=" + URLEncoder.encode(email, "UTF-8") +
                        "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(password, "UTF-8")

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
//                    Log.i("Helllll",line)
                    if (line == null)
                        break
                    result += line
                }while (true)

//                while ((line = br.readLine()) != null) {
//
//                    result += line
//
//                }

                // Log.i("hello",result);
                val jsonObject = JSONObject(result)
                val resp = jsonObject.getString("response")

                if (resp == "Ok") {
                    user = jsonObject.getString("user")
                    userName = jsonObject.getString("userName")
                    loggedIn_email = jsonObject.getString("email")

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

            if (aBoolean!!) {
                Toast.makeText(this@LoginActivity, "Login Success", Toast.LENGTH_LONG).show()
                if (user == "Customer") {
                    session!!.setLoggedIn(true)
                    session!!.userType = "Customer"
                    session!!.userName = userName
                    startActivity(Intent(this@LoginActivity, CustomerActivity::class.java))
                    this@LoginActivity.finish()
                } else {
                    session!!.setLoggedIn(true)
                    session!!.userType = "Mechanic"
                    session!!.userName = userName
                    session!!.email = loggedIn_email
                    startActivity(Intent(this@LoginActivity, MechanicActivity::class.java))
                    this@LoginActivity.finish()
                }
            } else {
                Toast.makeText(this@LoginActivity, "Login Failed", Toast.LENGTH_LONG).show()

            }

        }
    }

}
