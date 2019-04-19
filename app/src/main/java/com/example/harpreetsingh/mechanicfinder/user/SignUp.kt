package com.example.harpreetsingh.mechanicfinder.user

import android.app.ProgressDialog
import android.content.Intent
import android.os.AsyncTask
import android.support.design.widget.TextInputLayout
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast

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

class SignUp : AppCompatActivity() {
    lateinit var signUp_btn: Button
    private var textInputFirstName: TextInputLayout? = null
    private var textInputLastName: TextInputLayout? = null
    private var textInputMobile: TextInputLayout? = null
    private var textInputEmail: TextInputLayout? = null
    private var textInputPassword: TextInputLayout? = null
    private var textInputConfirmPassword: TextInputLayout? = null
    private var radioGroupGender: RadioGroup? = null
    private var radioGroupUserType: RadioGroup? = null
    private var radioGender: RadioButton? = null
    private var radioUser: RadioButton? = null
    private var firstName: String? = null
    private var lastName: String? = null
    private var mobile: String? = null
    private var email: String? = null
    private var password: String? = null
    private var confirmPassword: String? = null
    private var gender: String? = null
    private var userType: String? = null
    private var selectedGenderId: Int = 0
    private var selectedUserId: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        textInputFirstName = findViewById(R.id.frst_name)
        textInputLastName = findViewById(R.id.last_name)
        textInputMobile = findViewById(R.id.mobile)
        textInputEmail = findViewById(R.id.email)
        textInputPassword = findViewById(R.id.pswd)
        textInputConfirmPassword = findViewById(R.id.confirm_pswd)
        radioGroupGender = findViewById(R.id.gender)
        radioGroupUserType = findViewById(R.id.user_type)


        signUp_btn = findViewById(R.id.signUp_btn)

        signUp_btn.setOnClickListener { view: View ->
            selectedGenderId = radioGroupGender!!.checkedRadioButtonId
            selectedUserId = radioGroupUserType!!.checkedRadioButtonId

            radioGender = findViewById(selectedGenderId)
            radioUser = findViewById(selectedUserId)
            if (validateForm()) {
                firstName = textInputFirstName!!.editText!!.text.toString().trim { it <= ' ' }
                lastName = textInputLastName!!.editText!!.text.toString().trim { it <= ' ' }
                mobile = textInputMobile!!.editText!!.text.toString().trim{it<= ' '}
                email = textInputEmail!!.editText!!.text.toString().trim { it <= ' ' }
                password = textInputPassword!!.editText!!.text.toString().trim { it <= ' ' }
                confirmPassword = textInputConfirmPassword!!.editText!!.text.toString().trim { it <= ' ' }
                gender = radioGender!!.text.toString()
                userType = radioUser!!.text.toString()

                val SignUp_User = SignUp_User(this@SignUp)
                SignUp_User.execute("http://mechanicfinder.000webhostapp.com/MechanicFinder/insert_user_detail.php")

               // Toast.makeText(this@SignUp,firstName+";"+mobile+";"+lastName+";"+email+";"+password+";"+confirmPassword+";"+gender+";"+userType,Toast.LENGTH_LONG).show();
            }


            //Toast.makeText(SignUp.this,frst_name.getText().toString(),Toast.LENGTH_SHORT).show();
            //SignUp_User SignUp_User = new SignUp_User(SignUp.this);
            //SignUp_User.execute("http://192.168.1.6/MechanicFinder/insert_user_detail.php");

        }

    }

    private fun validateFirstName(): Boolean {
        val firstNameInput = textInputFirstName!!.editText!!.text.toString().trim { it <= ' ' }

        if (firstNameInput.isEmpty()) {
            textInputFirstName!!.error = "Field can't be empty"
            return false
        } else {
            textInputFirstName!!.error = null
            return true
        }
    }

    private fun validateLastName(): Boolean {
        val lastNameInput = textInputLastName!!.editText!!.text.toString().trim { it <= ' ' }

        if (lastNameInput.isEmpty()) {
            textInputLastName!!.error = "Field can't be empty"
            return false
        } else {
            textInputLastName!!.error = null
            return true
        }
    }
    private fun validateMobile(): Boolean {
        val mobileInput = textInputMobile!!.editText!!.text.toString().trim { it <= ' ' }
        val mobilePattern = "^[7-9][0-9]{9}\$"
        if (mobileInput.isEmpty()) {
            textInputMobile!!.error = "Field can't be empty"
            return false
        } else if (!mobileInput.matches(mobilePattern.toRegex())) {
            textInputMobile!!.error = "Mobile is not valid"
            return false
        } else {
            textInputMobile!!.error = null
            return true
        }
    }
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

    private fun validateConfirmPassword(): Boolean {
        val confirmPasswordInput = textInputConfirmPassword!!.editText!!.text.toString().trim { it <= ' ' }

        if (confirmPasswordInput.isEmpty()) {
            textInputConfirmPassword!!.error = "Field can't be empty"
            return false
        } else if (confirmPasswordInput.length < 8) {
            textInputConfirmPassword!!.error = "Must contain minimum 8 characters"
            return false
        } else if (!confirmPasswordInput.matches(textInputPassword!!.editText!!.text.toString().trim { it <= ' ' }.toRegex())) {
            textInputConfirmPassword!!.error = "Password not matched"
            return false
        } else {
            textInputConfirmPassword!!.error = null
            return true
        }
    }

    private fun genderSelect(): Boolean {
        if (radioGroupGender!!.checkedRadioButtonId == -1) {

            Toast.makeText(this@SignUp, "Must select gender", Toast.LENGTH_SHORT).show()
            return false
        } else {
            return true
        }
    }

    private fun userSelect(): Boolean {
        if (radioGroupUserType!!.checkedRadioButtonId == -1) {

            Toast.makeText(this@SignUp, "Must select user", Toast.LENGTH_SHORT).show()
            return false
        } else {
            return true
        }
    }

    private fun validateForm(): Boolean {
        return !(!validateFirstName() or !validateLastName() or !validateMobile() or !validateEmail() or !validatePassword() or !validateConfirmPassword() or !genderSelect() or !userSelect())

    }

    internal inner class SignUp_User(signUp: SignUp) : AsyncTask<String, Void, Boolean>() {
        private var progressDialog: ProgressDialog? = null

        init {
            progressDialog = ProgressDialog(signUp)
        }

        override fun onPreExecute() {
            progressDialog = ProgressDialog(this@SignUp)
            progressDialog!!.setMessage("Creating account....")
            progressDialog!!.setCancelable(false)
            progressDialog!!.show()
            super.onPreExecute()

        }

        override fun doInBackground(vararg strings: String): Boolean? {


            var line: String?
            var result = ""
            //            ArrayList<NameValuePair> arrayList=new ArrayList<>();
            //            arrayList.add(new BasicNameValuePair("k1",strings[0]));
            //            arrayList.add(new BasicNameValuePair("k2",strings[1]));
            try {
                val url = URL(strings[0])
                val urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.requestMethod = "POST"
                urlConnection.doOutput = true
                urlConnection.doInput = true
                val os = urlConnection.outputStream
                val writer = OutputStreamWriter(os)
                val bw = BufferedWriter(writer)
                val post_data = URLEncoder.encode("firstName", "UTF-8") + "=" + URLEncoder.encode(firstName, "UTF-8") +
                        "&" + URLEncoder.encode("lastName", "UTF-8") + "=" + URLEncoder.encode(lastName, "UTF-8") +
                        "&" + URLEncoder.encode("mobile", "UTF-8") + "=" + URLEncoder.encode(mobile, "UTF-8") +
                        "&" + URLEncoder.encode("email", "UTF-8") + "=" + URLEncoder.encode(email, "UTF-8") +
                        "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(password, "UTF-8") +
                        "&" + URLEncoder.encode("gender", "UTF-8") + "=" + URLEncoder.encode(gender, "UTF-8") +
                        "&" + URLEncoder.encode("userType", "UTF-8") + "=" + URLEncoder.encode(userType, "UTF-8")
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

                Log.i("hello123", result)
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
                Toast.makeText(this@SignUp, "Account Created", Toast.LENGTH_LONG).show()
            startActivity(Intent(this@SignUp, LoginActivity::class.java))
        }
    }
}
