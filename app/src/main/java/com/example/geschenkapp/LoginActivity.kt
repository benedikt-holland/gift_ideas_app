package com.example.geschenkapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root


        val viewModelJob = SupervisorJob()
        val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
        uiScope.launch(Dispatchers.IO) {
            try {
                //Initate database connection and save to data holder
                var inputStream = assets.open("config.properties")
                var props = Properties()
                props.load(inputStream)
                var usr = props.getProperty("MYSQL_USER", "")
                var pwd = props.getProperty("MYSQL_PWD", "")
                var url = props.getProperty("MYSQL_URL", "")
                inputStream.close()
                db.connect(url, usr, pwd)
                DbHolder.getInstance().db = db
            } catch (e: FileNotFoundException) {
                System.err.println("Missing config.properties file in app/src/main/assets/ containing database credentials")
            }
        }
        setContentView(view)
        getButtonClick()
    }

    //Register onclick listener for register and login button
    private fun getButtonClick() {
        val btnRegister = findViewById(R.id.btnRegister) as Button
        btnRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }


        val btnLogin = findViewById(R.id.btnLogin) as Button
        btnLogin.setOnClickListener {

            val email = binding.tfEmail.editText?.text.toString()
            if (email == "") {
                binding.tfEmail.error = getString(R.string.noEmailError)
                binding.tfEmail.isErrorEnabled = true
                return@setOnClickListener
            } else {
                binding.tfEmail.isErrorEnabled = false
            }
            val password = binding.tfPassword.editText?.text.toString()
            if (password == "") {
                binding.tfPassword.error = getString(R.string.noPasswordError)
                binding.tfPassword.isErrorEnabled = true
                return@setOnClickListener
            } else {
                binding.tfPassword.isErrorEnabled = false
            }

            val viewModelJob = SupervisorJob()
            val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
            uiScope.launch(Dispatchers.IO) {
                try {



                    var tempUser = db.loginUser(email, password)
                    if(tempUser.metaData.columnCount != 0){
                        tempUser.next()
                        user = tempUser
                        DataHolder.getInstance().user = user


                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                    }else{

                        withContext(Dispatchers.Main){
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
}