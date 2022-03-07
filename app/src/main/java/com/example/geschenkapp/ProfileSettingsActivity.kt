package com.example.geschenkapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import java.sql.ResultSet

class ProfileSettingsActivity : AppCompatActivity() {

    lateinit var user: ResultSet
    private var db = DbConnector()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_settings)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.apply {
            title = "Profile Settings"
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        user = DataHolder.getInstance().user
        db = DbHolder.getInstance().db

        

    }
}