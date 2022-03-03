package com.example.geschenkapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initProfileViewPager()


        val viewModelJob = SupervisorJob()
        val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
        uiScope.launch(Dispatchers.IO) {
            var db = DbConnector()
            db.connect()
            val user = db.loginUser("Hans@Müller.de", "password")
            if (user!=null) {
                while (user.next()) {
                    println(user.getString("first_name"))
                }
            } else {
                println("Login failed")
            }
        }
    }

    private fun initProfileViewPager() {
        //var viewPager : ViewPager2 = findViewById(R.id.profileViewPager)
        //var adapter = ViewPAger
    }
}