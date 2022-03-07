package com.example.geschenkapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Spinner
import com.example.geschenkapp.databinding.ActivityMainBinding
import com.example.geschenkapp.databinding.ActivityProfileSettingsBinding
import java.sql.ResultSet

class ProfileSettingsActivity : AppCompatActivity() {

    lateinit var user: ResultSet
    private var db = DbConnector()
    private lateinit var binding: ActivityProfileSettingsBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_settings)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.apply {
            title = "Profile Settings"
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        binding = ActivityProfileSettingsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        user = DataHolder.getInstance().user
        db = DbHolder.getInstance().db

        binding.tvFirstName.setText(user.getString("first_name"))
        binding.tvLastName.setText(user.getString("last_name"))

        spinnerProfilePrivacy()
        spinnerPostPrivacy()



    }

    private fun spinnerProfilePrivacy(){
        val spinner: Spinner = findViewById(R.id.spProfilePrivacy)
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            this,
            R.array.profile_privacy_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner.adapter = adapter
        }
    }

    private fun spinnerPostPrivacy(){
        val spinner: Spinner = findViewById(R.id.spPostPrivacy)
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            this,
            R.array.post_privacy_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner.adapter = adapter
        }
    }
}