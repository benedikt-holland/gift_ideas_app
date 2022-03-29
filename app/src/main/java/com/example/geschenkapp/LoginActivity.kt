package com.example.geschenkapp

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.example.geschenkapp.databinding.ActivityLoginBinding
import kotlinx.coroutines.*
import java.io.FileNotFoundException
import java.sql.ResultSet
import java.util.*


//Class for login screen
@Suppress("BlockingMethodInNonBlockingContext")
class LoginActivity : AppCompatActivity() {

    lateinit var user: ResultSet
    private var db = DbConnector()
    private lateinit var binding: ActivityLoginBinding
    private var dbConnected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    override fun onResume() {
        super.onResume()

        val pref = getSharedPreferences("com.example.geschenkapp", MODE_PRIVATE)

        val email = pref.getString("email", null)
        val password = pref.getString("password", null)

        if (email != null && password != null){
            binding.tfEmail.editText?.setText(email)
            binding.tfPassword.editText?.setText(password)
        }

        val viewModelJob = SupervisorJob()
        val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
        uiScope.launch(Dispatchers.IO) {
            connectToDatabase()
            if (email != null && password != null){
                login()
            }
        }
        getButtonClick()
    }

    //Register onclick listener for register and login button
    private fun getButtonClick() {
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        btnRegister.setOnClickListener {

            if (checkForInternet(this)) {
                val intent = Intent(this, RegisterActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_LONG).show()
            }
        }


        val btnLogin = findViewById<Button>(R.id.btnLogin)
        btnLogin.setOnClickListener {

            if (checkForInternet(this)) {
                val viewModelJob = SupervisorJob()
                val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
                uiScope.launch(Dispatchers.IO) {
                    dbConnected = try {
                        db.getNotificationCount(1)
                        true
                    } catch (e: Exception) {
                        false
                    }
                    if (!dbConnected) {
                        connectToDatabase()
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                applicationContext,
                                R.string.connecting,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    withContext(Dispatchers.Main) {
                        login()
                    }
                }
            } else {
                Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_LONG).show()
                dbConnected = false
            }
        }
    }

    private suspend fun connectToDatabase() {
        try {
            //Initiate database connection and save to data holder
            val inputStream = assets.open("config.properties")
            val props = Properties()
            props.load(inputStream)
            val usr = props.getProperty("MYSQL_USER", "")
            val pwd = props.getProperty("MYSQL_PWD", "")
            val url = props.getProperty("MYSQL_URL", "")
            inputStream.close()
            db.connect(url, usr, pwd)
            DbHolder.getInstance().db = db
            dbConnected = true
        } catch (e: FileNotFoundException) {
            System.err.println("Missing config.properties file in app/src/main/assets/ containing database credentials")
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    applicationContext,
                    R.string.no_internet_connection,
                    Toast.LENGTH_LONG
                ).show()
            }
            dbConnected = false
        }
    }

    private fun checkForInternet(context: Context): Boolean {
        // register activity with the connectivity manager service
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
        }
    }

    private fun login() {
        val email = binding.tfEmail.editText?.text.toString()
        if (email == "") {
            binding.tfEmail.error = getString(R.string.no_email_error)
            binding.tfEmail.isErrorEnabled = true
            return
        } else {
            binding.tfEmail.isErrorEnabled = false
        }
        val password = binding.tfPassword.editText?.text.toString()
        if (password == "") {
            binding.tfPassword.error = getString(R.string.no_password_error)
            binding.tfPassword.isErrorEnabled = true
            return
        } else {
            binding.tfPassword.isErrorEnabled = false
        }

        val viewModelJob = SupervisorJob()
        val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
        uiScope.launch(Dispatchers.IO) {
            try {
                val tempUser = db.loginUser(email, password)

                if (tempUser.metaData.columnCount != 0) {
                    tempUser.next()
                    user = tempUser
                    LoginHolder.getInstance().user = user
                    val pref = getSharedPreferences("com.example.geschenkapp", MODE_PRIVATE)

                    with(pref.edit()){
                        putString("email", email)
                        putString("password", password)
                        apply()
                    }

                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    withContext(Dispatchers.Main) {
                        binding.tfPassword.error = getString(R.string.wrong_credentials)
                        binding.tfPassword.isErrorEnabled = true
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
