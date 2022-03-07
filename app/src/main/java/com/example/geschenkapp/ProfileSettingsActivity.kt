package com.example.geschenkapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
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

        val items = listOf("1", "2", "3", "4")
        val adapter = ArrayAdapter(this, R.layout.list_profileprivacy_item, items)
        (binding.tvPrivacy.editText as? AutoCompleteTextView)?.setAdapter(adapter)

        binding.tvFirstName.setText(user.getString("first_name"))
        binding.tvLastName.setText(user.getString("last_name"))




    }
}