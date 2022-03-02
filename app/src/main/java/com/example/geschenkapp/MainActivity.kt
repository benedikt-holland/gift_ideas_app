package com.example.geschenkapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2
import com.example.geschenkapp.db.AppDatabase

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initProfileViewPager()

        var db = DbConnector()
        val user = db.loginUser("Hans@Müller.de", "password")
        print(user.getString("first_name"))
    }

    private fun initProfileViewPager() {
        //var viewPager : ViewPager2 = findViewById(R.id.profileViewPager)
        //var adapter = ViewPAger
    }
}