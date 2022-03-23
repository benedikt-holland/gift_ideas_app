package com.example.geschenkapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.widget.Button
import android.widget.Toast
import com.example.geschenkapp.databinding.ActivityLoginBinding
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
import android.os.Build
import android.widget.TextView
import java.util.*

//Class for register page
class RegisterActivity : AppCompatActivity() {

    lateinit var user: ResultSet
    private var db = DbConnector()
    private lateinit var binding: ActivityRegisterBinding
    lateinit var datepicker: TextView
    lateinit var clickView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        db = DbHolder.getInstance().db


        getButtonClick()
        setDate()
    }

    private fun setDate() {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        var mt = "0"
        var d = "0"

        datepicker = findViewById(R.id.tvDateOfBirth)
        clickView = findViewById(R.id.tvForClick)
        clickView.setOnClickListener {
            val dpd = DatePickerDialog(
                this,
                DatePickerDialog.OnDateSetListener { view, mYear, mMonth, mDay ->
                    val month2 = mMonth + 1
                    if (month2 < 10) {
                        mt = "0" + month2
                    } else {
                        mt = month2.toString()
                    }
                    if (mDay < 10) {
                        d = "0" + mDay
                    } else {
                        d = mDay.toString()
                    }
                    datepicker.setText("" + mYear + "-" + mt + "-" + d)
                },
                year,
                month,
                day
            )
            dpd.show()
        }

    }

    private fun getButtonClick() {
        val btnRegister = findViewById(R.id.btnRegister) as Button
        btnRegister.setOnClickListener {

            if (checkForInternet(this)) {
                //read values from frontend
                var firstName = binding.tfFirstname.editText?.text.toString()
                var lastName = binding.tfLastname.editText?.text.toString()
                var dateOfBirthString = binding.tvDateOfBirth.text.toString()
                var email = binding.tfEmail.editText?.text.toString()
                var password = binding.tfPassword.editText?.text.toString()


                //check if values are empty
                if (firstName.equals("") || lastName.equals("") || dateOfBirthString.equals("") || email.equals(
                        ""
                    ) || password.equals("")
                ) {
                    Toast.makeText(
                        this,
                        R.string.emptyString,
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
                            R.string.emailDuplicate,
                            Toast.LENGTH_LONG
                        ).show()
                        Looper.loop()
                        return@launch
                    }


                    var tempUser = db.createUser(firstName, lastName, dateOfBirth, email, password)
                    tempUser.next()
                    user = tempUser
                    DataHolder.getInstance().user = user


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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork =
                connectivityManager.getNetworkCapabilities(network) ?: return false
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
}
