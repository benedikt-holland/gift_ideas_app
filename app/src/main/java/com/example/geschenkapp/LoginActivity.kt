package com.example.geschenkapp

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.example.geschenkapp.databinding.ActivityLoginBinding
import com.example.geschenkapp.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import java.io.FileNotFoundException
import java.sql.ResultSet
import java.util.*


//Class for login screen
class LoginActivity : AppCompatActivity() {

    lateinit var user: ResultSet
    private var db = DbConnector()
    private lateinit var binding: ActivityLoginBinding
    private var dbConnected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root


        val viewModelJob = SupervisorJob()
        val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
        uiScope.launch(Dispatchers.IO) {
            connectToDatabase()
        }
        setContentView(view)
        getButtonClick()
    }

    //Register onclick listener for register and login button
    private fun getButtonClick() {
        val btnRegister = findViewById(R.id.btnRegister) as Button
        btnRegister.setOnClickListener {

            if (checkForInternet(this)) {
                val intent = Intent(this, RegisterActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_LONG).show()
            }
        }


        val btnLogin = findViewById(R.id.btnLogin) as Button
        btnLogin.setOnClickListener {

            if (checkForInternet(this)) {
                val viewModelJob = SupervisorJob()
                val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
                uiScope.launch(Dispatchers.IO) {
                    try {
                        db.getUserById(1)
                        dbConnected = true
                    } catch (e: Exception) {
                        dbConnected = false
                    }
                    if (dbConnected == false) {
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
            var inputStream = assets.open("config.properties")
            var props = Properties()
            props.load(inputStream)
            var usr = props.getProperty("MYSQL_USER", "")
            var pwd = props.getProperty("MYSQL_PWD", "")
            var url = props.getProperty("MYSQL_URL", "")
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                else -> false
            }
        } else {
            // if the android version is below M
            @Suppress("DEPRECATION") val networkInfo =
                connectivityManager.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            return networkInfo.isConnected
        }
    }

    private fun login() {
        val email = binding.tfEmail.editText?.text.toString()
        if (email == "") {
            binding.tfEmail.error = getString(R.string.noEmailError)
            binding.tfEmail.isErrorEnabled = true
            return
        } else {
            binding.tfEmail.isErrorEnabled = false
        }
        val password = binding.tfPassword.editText?.text.toString()
        if (password == "") {
            binding.tfPassword.error = getString(R.string.noPasswordError)
            binding.tfPassword.isErrorEnabled = true
            return
        } else {
            binding.tfPassword.isErrorEnabled = false
        }

        val viewModelJob = SupervisorJob()
        val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
        uiScope.launch(Dispatchers.IO) {
            try {
                var tempUser = db.loginUser(email, password)

                if (tempUser != null) {
                    tempUser.next()
                    user = tempUser
                    DataHolder.getInstance().user = user

                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    withContext(Dispatchers.Main) {
                        binding.tfPassword.error = getString(R.string.wrongCredentials)
                        binding.tfPassword.isErrorEnabled = true
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
