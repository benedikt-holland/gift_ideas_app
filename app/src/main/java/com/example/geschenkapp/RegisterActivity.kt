package com.example.geschenkapp

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.widget.Button
import android.widget.Toast
import com.example.geschenkapp.databinding.ActivityRegisterBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.sql.ResultSet
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import android.app.DatePickerDialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Patterns
import android.widget.TextView
import java.util.*

//Class for register page
class RegisterActivity : AppCompatActivity() {

    lateinit var user: ResultSet
    private var db = DbConnector()
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var tvDateOfBirth: TextView
    private lateinit var tvForClick: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        //Check internet connection
        if (!com.example.geschenkapp.checkForInternet(this)) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        db = DbHolder.getInstance().db


        getButtonClick()
        setDate()
    }

    @SuppressLint("SetTextI18n")
    private fun setDate() {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        var mt: String
        var d: String

        tvDateOfBirth = findViewById(R.id.tvDateOfBirth)
        tvForClick = findViewById(R.id.tvForClick)
        tvForClick.setOnClickListener {
            val dpd = DatePickerDialog(
                this,
                { _, mYear, mMonth, mDay ->
                    val month2 = mMonth + 1
                    mt = if (month2 < 10) {
                        "0$month2"
                    } else {
                        month2.toString()
                    }
                    d = if (mDay < 10) {
                        "0$mDay"
                    } else {
                        mDay.toString()
                    }
                    tvDateOfBirth.text = "$mYear-$mt-$d"
                },
                year,
                month,
                day
            )
            dpd.show()
        }

    }

    private fun getButtonClick() {
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        btnRegister.setOnClickListener {

            if (checkForInternet(this)) {
                //read values from frontend
                val firstName = binding.tfFirstname.editText?.text.toString()
                val lastName = binding.tfLastname.editText?.text.toString()
                val dateOfBirthString = binding.tvDateOfBirth.text.toString()
                val email = binding.tfEmail.editText?.text.toString()
                val password = binding.tfPassword.editText?.text.toString()


                //check if values are empty
                if (firstName == "" || lastName == "" || dateOfBirthString == "" || email == "" || password == ""
                ) {
                    Toast.makeText(
                        this,
                        R.string.empty_string,
                        Toast.LENGTH_LONG
                    ).show()
                    return@setOnClickListener
                }

                val dateOfBirth = LocalDate.parse(dateOfBirthString, DateTimeFormatter.ISO_DATE)

                val viewModelJob = SupervisorJob()
                val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
                uiScope.launch(Dispatchers.IO) {

                    //check if email is already in use
                    if (db.checkIfEmailExistsOnOtherUser(1, email)) {
                        Looper.prepare()
                        Toast.makeText(
                            this@RegisterActivity,
                            R.string.email_duplicate,
                            Toast.LENGTH_LONG
                        ).show()
                        Looper.loop()
                        return@launch
                    }

                    //Check if email is valid
                    if (!email.isValidEmail()) {
                        Looper.prepare()
                        Toast.makeText(
                            this@RegisterActivity,
                            R.string.email_invalid,
                            Toast.LENGTH_LONG
                        ).show()
                        Looper.loop()
                        return@launch
                    }


                    val tempUser = db.createUser(firstName, lastName, dateOfBirth, email, password)
                    tempUser.next()
                    user = tempUser
                    LoginHolder.getInstance().user = user


                    val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                    startActivity(intent)
                }
            } else {
                Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_LONG).show()


            }
        }
    }
    private fun checkForInternet(context: Context): Boolean {

        // register activity with the connectivity manager service
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork =
            connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
        }
    }

    //Function to check for valid email
    fun CharSequence?.isValidEmail() = !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()
}
